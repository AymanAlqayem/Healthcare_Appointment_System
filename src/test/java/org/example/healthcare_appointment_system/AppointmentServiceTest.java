package org.example.healthcare_appointment_system;

import org.example.healthcare_appointment_system.dto.BookAppointmentDto;
import org.example.healthcare_appointment_system.entity.*;
import org.example.healthcare_appointment_system.enums.AppointmentStatus;
import org.example.healthcare_appointment_system.enums.Role;
import org.example.healthcare_appointment_system.enums.WeekDay;
import org.example.healthcare_appointment_system.repo.*;
import org.example.healthcare_appointment_system.security.SecurityUtils;
import org.example.healthcare_appointment_system.service.AppointmentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AppointmentServiceTest {

    @Mock
    private AvailabilitySlotRepository slotRepository;

    @Mock
    private PatientRepository patientRepository;

    @Mock
    private DoctorRepository doctorRepository;

    @Mock
    private AppointmentRepository appointmentRepository;

    @InjectMocks
    private AppointmentService appointmentService;

    private Patient testPatient;
    private Doctor testDoctor;
    private AvailabilitySlot testSlot;
    private User patientUser;
    private User doctorUser;

    @BeforeEach
    void setUp() {
        // Setup test data
        patientUser = new User();
        patientUser.setId(1L);
        patientUser.setUsername("patient1");
        patientUser.setRole(Role.PATIENT);

        doctorUser = new User();
        doctorUser.setId(2L);
        doctorUser.setUsername("doctor1");
        doctorUser.setRole(Role.DOCTOR);

        testPatient = new Patient();
        testPatient.setId(1L);
        testPatient.setUser(patientUser);

        testDoctor = new Doctor();
        testDoctor.setId(1L);
        testDoctor.setUser(doctorUser);
        testDoctor.setSpecialty("Cardiology");

        testSlot = new AvailabilitySlot();
        testSlot.setId(1L);
        testSlot.setDoctor(testDoctor);
        testSlot.setDayOfWeek(WeekDay.MONDAY);
        testSlot.setStartTime(LocalTime.of(9, 0));
        testSlot.setEndTime(LocalTime.of(10, 0));
        testSlot.setReserved(false);
    }

    @Test
    void bookAppointment_Success() {
        try (MockedStatic<SecurityUtils> securityUtilsMock = mockStatic(SecurityUtils.class)) {
            // Arrange
            securityUtilsMock.when(SecurityUtils::getCurrentUserId).thenReturn(1L);

            BookAppointmentDto dto = new BookAppointmentDto(1L, 1L);

            when(patientRepository.findByUserId(1L)).thenReturn(Optional.of(testPatient));
            when(doctorRepository.findById(1L)).thenReturn(Optional.of(testDoctor));
            when(slotRepository.findById(1L)).thenReturn(Optional.of(testSlot));
            when(appointmentRepository.existsBySlotIdAndStatus(1L, AppointmentStatus.BOOKED))
                    .thenReturn(false);
            when(appointmentRepository.save(any(Appointment.class))).thenAnswer(invocation -> {
                Appointment appointment = invocation.getArgument(0);
                appointment.setId(1L);
                return appointment;
            });
            when(slotRepository.save(any(AvailabilitySlot.class))).thenReturn(testSlot);

            // Act
            var result = appointmentService.bookAppointment(dto);

            // Assert
            assertNotNull(result);
            assertEquals(1L, result.id());
            assertEquals("doctor1", result.doctorName());
            assertEquals("patient1", result.patientName());
            assertEquals("BOOKED", result.status());

            verify(slotRepository).save(testSlot);
            assertTrue(testSlot.isReserved()); // Slot should be marked as reserved
        }
    }

    @Test
    void bookAppointment_SlotAlreadyBooked_ThrowsException() {
        try (MockedStatic<SecurityUtils> securityUtilsMock = mockStatic(SecurityUtils.class)) {
            // Arrange
            securityUtilsMock.when(SecurityUtils::getCurrentUserId).thenReturn(1L);

            BookAppointmentDto dto = new BookAppointmentDto(1L, 1L);

            when(patientRepository.findByUserId(1L)).thenReturn(Optional.of(testPatient));
            when(appointmentRepository.existsBySlotIdAndStatus(1L, AppointmentStatus.BOOKED))
                    .thenReturn(true); // Slot is already booked

            // Act & Assert
            IllegalStateException exception = assertThrows(IllegalStateException.class,
                    () -> appointmentService.bookAppointment(dto));

            assertEquals("This slot is already booked", exception.getMessage());

            verify(slotRepository, never()).save(any());
            verify(appointmentRepository, never()).save(any());
        }
    }

    @Test
    void bookAppointment_SlotAlreadyReserved_ThrowsException() {
        try (MockedStatic<SecurityUtils> securityUtilsMock = mockStatic(SecurityUtils.class)) {
            // Arrange
            securityUtilsMock.when(SecurityUtils::getCurrentUserId).thenReturn(1L);

            BookAppointmentDto dto = new BookAppointmentDto(1L, 1L);
            testSlot.setReserved(true); // Slot is already reserved

            when(patientRepository.findByUserId(1L)).thenReturn(Optional.of(testPatient));
            when(appointmentRepository.existsBySlotIdAndStatus(1L, AppointmentStatus.BOOKED))
                    .thenReturn(false);
            when(doctorRepository.findById(1L)).thenReturn(Optional.of(testDoctor));
            when(slotRepository.findById(1L)).thenReturn(Optional.of(testSlot));

            // Act & Assert
            RuntimeException exception = assertThrows(RuntimeException.class,
                    () -> appointmentService.bookAppointment(dto));

            assertEquals("This slot is already booked", exception.getMessage());

            verify(slotRepository, never()).save(any());
            verify(appointmentRepository, never()).save(any());
        }
    }

    @Test
    void markAppointmentCompleted_Success() {
        try (MockedStatic<SecurityUtils> securityUtilsMock = mockStatic(SecurityUtils.class)) {
            // Arrange
            securityUtilsMock.when(SecurityUtils::getCurrentUserId).thenReturn(2L);

            Appointment appointment = new Appointment();
            appointment.setId(1L);
            appointment.setDoctor(testDoctor);
            appointment.setPatient(testPatient);
            appointment.setSlot(testSlot);
            appointment.setStatus(AppointmentStatus.BOOKED);
            testSlot.setReserved(true);

            when(doctorRepository.findByUserId(2L)).thenReturn(Optional.of(testDoctor));
            when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));
            when(appointmentRepository.save(any(Appointment.class))).thenReturn(appointment);
            when(slotRepository.save(any(AvailabilitySlot.class))).thenReturn(testSlot);

            // Act
            var result = appointmentService.markAppointmentCompleted(1L);

            // Assert
            assertNotNull(result);
            assertEquals("COMPLETED", result.status());
            assertFalse(testSlot.isReserved()); // Slot should be freed
        }
    }
}
