package org.example.healthcare_appointment_system;

import org.example.healthcare_appointment_system.dto.*;
import org.example.healthcare_appointment_system.entity.*;
import org.example.healthcare_appointment_system.enums.AppointmentStatus;
import org.example.healthcare_appointment_system.enums.Gender;
import org.example.healthcare_appointment_system.enums.Role;
import org.example.healthcare_appointment_system.enums.WeekDay;
import org.example.healthcare_appointment_system.repo.*;
import org.example.healthcare_appointment_system.security.SecurityUtils;
import org.example.healthcare_appointment_system.service.PatientService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PatientServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PatientRepository patientRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AppointmentRepository appointmentRepository;

    @InjectMocks
    private PatientService patientService;

    private PatientDto testPatientDto;
    private PatientUpdateDto testPatientUpdateDto;
    private User testUser;
    private Patient testPatient;

    @BeforeEach
    void setUp() {
        testPatientDto = new PatientDto(
                "patient1", "patient@example.com", "1234567890", "password123",
                Gender.MALE, LocalDate.of(1990, 1, 1)
        );

        testPatientUpdateDto = new PatientUpdateDto(
                "new@example.com", "0987654321", Gender.FEMALE, LocalDate.of(1995, 1, 1)
        );

        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("patient1");
        testUser.setEmail("patient@example.com");
        testUser.setPhone("1234567890");
        testUser.setRole(Role.PATIENT);

        testPatient = new Patient();
        testPatient.setId(1L);
        testPatient.setUser(testUser); // Proper association
        testPatient.setGender(Gender.MALE);
        testPatient.setDateOfBirth(LocalDate.of(1990, 1, 1));
    }

    @Test
    void createPatient_Success() {
        // Arrange - Correct validation order (NO password check!)
        when(userRepository.existsByUsername("patient1")).thenReturn(false);
        when(userRepository.existsByEmail("patient@example.com")).thenReturn(false);
        when(userRepository.existsByPhone("1234567890")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(patientRepository.save(any(Patient.class))).thenReturn(testPatient);

        // Act
        PatientResponseDto result = patientService.createPatient(testPatientDto);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.id());
        assertEquals("patient1", result.username());

        verify(userRepository).existsByUsername("patient1");
        verify(userRepository).existsByEmail("patient@example.com");
        verify(userRepository).existsByPhone("1234567890");
    }

    @Test
    void updateInfo_Success() {
        try (MockedStatic<SecurityUtils> securityUtilsMock = mockStatic(SecurityUtils.class)) {
            // Arrange
            securityUtilsMock.when(SecurityUtils::getCurrentUserId).thenReturn(1L);

            // Create a fresh patient for this test to avoid state pollution
            Patient freshPatient = new Patient();
            freshPatient.setId(1L);
            freshPatient.setUser(testUser);
            freshPatient.setGender(Gender.MALE);
            freshPatient.setDateOfBirth(LocalDate.of(1990, 1, 1));

            when(patientRepository.findByUserId(1L)).thenReturn(Optional.of(freshPatient));
            when(patientRepository.save(any(Patient.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            PatientResponseDto result = patientService.updateInfo(testPatientUpdateDto);

            // Assert - Check the actual updated values
            assertEquals("new@example.com", freshPatient.getUser().getEmail());
            assertEquals("0987654321", freshPatient.getUser().getPhone());
            assertEquals(Gender.FEMALE, freshPatient.getGender());

            verify(patientRepository).save(freshPatient);
        }
    }

    @Test
    void getMyAppointments_Success() {
        try (MockedStatic<SecurityUtils> securityUtilsMock = mockStatic(SecurityUtils.class)) {
            // Arrange
            securityUtilsMock.when(SecurityUtils::getCurrentUserId).thenReturn(1L);

            Appointment appointment = new Appointment();
            appointment.setId(1L);
            appointment.setPatient(testPatient);

            Doctor doctor = new Doctor();
            User doctorUser = new User();
            doctorUser.setUsername("doctor1");
            doctor.setUser(doctorUser);
            appointment.setDoctor(doctor);

            AvailabilitySlot slot = new AvailabilitySlot();
            slot.setDayOfWeek(WeekDay.MONDAY);
            slot.setStartTime(LocalTime.of(9, 0));
            slot.setEndTime(LocalTime.of(10, 0));
            appointment.setSlot(slot);
            appointment.setStatus(AppointmentStatus.BOOKED);

            when(patientRepository.findByUserId(1L)).thenReturn(Optional.of(testPatient));
            when(appointmentRepository.findByPatientId(1L)).thenReturn(List.of(appointment));

            // Act
            var result = patientService.getMyAppointments();

            // Assert
            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals("doctor1", result.get(0).doctorName());
        }
    }

    @Test
    void deletePatient_Success() {
        // Arrange
        when(patientRepository.findById(1L)).thenReturn(Optional.of(testPatient));

        // Act
        var result = patientService.deletePatient(1L);

        // Assert
        assertNotNull(result);
        assertEquals("Patient deleted successfully", result.getBody());

        verify(patientRepository).delete(testPatient);
        verify(userRepository).delete(testUser);
    }
}