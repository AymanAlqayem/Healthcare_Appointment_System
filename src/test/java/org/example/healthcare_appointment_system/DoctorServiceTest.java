package org.example.healthcare_appointment_system;

import org.example.healthcare_appointment_system.dto.*;
import org.example.healthcare_appointment_system.entity.Doctor;
import org.example.healthcare_appointment_system.entity.User;
import org.example.healthcare_appointment_system.enums.Role;
import org.example.healthcare_appointment_system.enums.WeekDay;
import org.example.healthcare_appointment_system.repo.DoctorRepository;
import org.example.healthcare_appointment_system.repo.UserRepository;
import org.example.healthcare_appointment_system.cacheTest.CacheService;
import org.example.healthcare_appointment_system.service.DoctorService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DoctorServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private DoctorRepository doctorRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private CacheService cacheService;

    @InjectMocks
    private DoctorService doctorService;

    private DoctorDto doctorDto;
    private User user;
    private Doctor doctor;

    @BeforeEach
    void setUp() {
        List<DaySlotsCreateDto> slots = List.of(
                new DaySlotsCreateDto(
                        WeekDay.MONDAY,
                        List.of(new TimeRangeDto(LocalTime.of(9, 0), LocalTime.of(10, 0)))
                )
        );

        doctorDto = new DoctorDto(
                "doctorUser",
                "doctor@example.com",
                "1234567890",
                "password123",
                "Cardiology",
                slots
        );

        user = User.builder()
                .id(1L)
                .username("doctorUser")
                .email("doctor@example.com")
                .phone("1234567890")
                .password("encodedPassword")
                .role(Role.DOCTOR)
                .enabled(true)
                .build();

        doctor = new Doctor();
        doctor.setId(1L);
        doctor.setUser(user);
        doctor.setSpecialty("Cardiology");
        doctor.setAvailabilitySlots(new ArrayList<>());
    }

    @Test
    void createDoctor_Success() {
        // Arrange
        when(userRepository.existsByUsername(doctorDto.username())).thenReturn(false);
        when(userRepository.existsByEmail(doctorDto.email())).thenReturn(false);
        when(userRepository.existsByPhone(doctorDto.phone())).thenReturn(false);
        when(passwordEncoder.encode(doctorDto.password())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(doctorRepository.save(any(Doctor.class))).thenReturn(doctor);

        // Act
        DoctorResponseDto result = doctorService.createDoctor(doctorDto);

        // Assert
        assertNotNull(result);
        assertEquals("doctorUser", result.username());
        assertEquals("Cardiology", result.specialty());
        verify(userRepository, times(1)).save(any(User.class));
        verify(doctorRepository, times(1)).save(any(Doctor.class));
        verify(cacheService, times(1)).evictAllDoctorsCache();
        verify(cacheService, times(1)).evictDoctorBySpecialtyCache("Cardiology");
    }

    @Test
    void createDoctor_UsernameExists_ThrowsException() {
        // Arrange
        when(userRepository.existsByUsername(doctorDto.username())).thenReturn(true);

        // Act & Assert
        assertThrows(ResponseStatusException.class, () -> doctorService.createDoctor(doctorDto));
        verify(userRepository, never()).save(any(User.class));
        verify(doctorRepository, never()).save(any(Doctor.class));
    }

    @Test
    void getAllDoctors_Success() {
        // Arrange
        List<Doctor> doctors = List.of(doctor);
        when(doctorRepository.findAll()).thenReturn(doctors);

        // Act
        List<DoctorResponseDto> result = doctorService.getAllDoctors();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("doctorUser", result.get(0).username());
        verify(doctorRepository, times(1)).findAll();
    }

    @Test
    void deleteDoctor_Success() {
        // Arrange
        when(doctorRepository.findById(1L)).thenReturn(Optional.of(doctor));

        // Act
        var result = doctorService.deleteDoctor(1L);

        // Assert
        assertNotNull(result);
        assertEquals(200, result.getStatusCodeValue());
        verify(doctorRepository, times(1)).delete(doctor);
        verify(userRepository, times(1)).delete(user);
        verify(cacheService, times(1)).evictDoctorCache(1L);
        verify(cacheService, times(1)).evictDoctorBySpecialtyCache("Cardiology");
        verify(cacheService, times(1)).evictAllDoctorsCache();
    }

    @Test
    void deleteDoctor_NotFound_ThrowsException() {
        // Arrange
        when(doctorRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> doctorService.deleteDoctor(1L));
        verify(doctorRepository, never()).delete(any());
        verify(userRepository, never()).delete(any());
    }

//    @Test
//    void updateDoctor_Success() {
//        // Arrange
//        DoctorUpdateDto updateDto = new DoctorUpdateDto(
//                1L, "updated@example.com", "0987654321", "Neurology"
//        );
//
//        when(doctorRepository.findById(1L)).thenReturn(Optional.of(doctor));
//        when(userRepository.existsByEmail("updated@example.com")).thenReturn(false);
//        when(userRepository.save(any(User.class))).thenReturn(user);
//        when(doctorRepository.save(any(Doctor.class))).thenReturn(doctor);
//
//        // Act
//        DoctorResponseDto result = doctorService.updateDoctor(updateDto);
//
//        // Assert
//        assertNotNull(result);
//        verify(userRepository, times(1)).save(user);
//        verify(doctorRepository, times(1)).save(doctor);
//        verify(cacheService, times(1)).evictDoctorCache(1L);
//        verify(cacheService, times(1)).evictAllDoctorsCache();
//        verify(cacheService, times(1)).evictDoctorBySpecialtyCache("Cardiology");
//        verify(cacheService, times(1)).evictDoctorBySpecialtyCache("Neurology");
//    }

    @Test
    void updateDoctor_Success() {
        // Arrange - CORRECTED parameter order: id, specialty, phone, email
        DoctorUpdateDto updateDto = new DoctorUpdateDto(
                1L, "Neurology", "0987654321", "updated@example.com"
        );

        when(doctorRepository.findById(1L)).thenReturn(Optional.of(doctor));
        // Email is the same as current, so existsByEmail shouldn't be called
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(doctorRepository.save(any(Doctor.class))).thenReturn(doctor);

        // Act
        DoctorResponseDto result = doctorService.updateDoctor(updateDto);

        // Assert
        assertNotNull(result);
        verify(userRepository, times(1)).save(user);
        verify(doctorRepository, times(1)).save(doctor);
        verify(cacheService, times(1)).evictDoctorCache(1L);
        verify(cacheService, times(1)).evictAllDoctorsCache();
        verify(cacheService, times(1)).evictDoctorBySpecialtyCache("Cardiology");
        verify(cacheService, times(1)).evictDoctorBySpecialtyCache("Neurology");
    }

    @Test
    void searchBySpecialty_Success() {
        // Arrange
        List<Doctor> doctors = List.of(doctor);
        when(doctorRepository.findBySpecialtyIgnoreCase("Cardiology")).thenReturn(doctors);

        // Act
        List<DoctorResponseDto> result = doctorService.searchBySpecialty("Cardiology");

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Cardiology", result.get(0).specialty());
        verify(doctorRepository, times(1)).findBySpecialtyIgnoreCase("Cardiology");
    }

    @Test
    void searchBySpecialty_NotFound_ThrowsException() {
        // Arrange
        when(doctorRepository.findBySpecialtyIgnoreCase("Unknown")).thenReturn(Collections.emptyList());

        // Act & Assert
        assertThrows(ResponseStatusException.class, () -> doctorService.searchBySpecialty("Unknown"));
    }

    @Test
    void updateDoctor_WithNewEmail_Success() {
        // Arrange - CORRECTED parameter order: id, specialty, phone, email
        DoctorUpdateDto updateDto = new DoctorUpdateDto(
                1L, "Neurology", "0987654321", "newemail@example.com"
        );

        when(doctorRepository.findById(1L)).thenReturn(Optional.of(doctor));
        when(userRepository.existsByEmail("newemail@example.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(doctorRepository.save(any(Doctor.class))).thenReturn(doctor);

        // Act
        DoctorResponseDto result = doctorService.updateDoctor(updateDto);

        // Assert
        assertNotNull(result);
        verify(userRepository, times(1)).existsByEmail("newemail@example.com");
        verify(userRepository, times(1)).save(user);
        verify(doctorRepository, times(1)).save(doctor);
    }

    @Test
    void updateDoctor_EmailExists_ThrowsException() {
        // Arrange - CORRECTED parameter order: id, specialty, phone, email
        DoctorUpdateDto updateDto = new DoctorUpdateDto(
                1L, "Neurology", "0987654321", "existing@example.com"
        );

        when(doctorRepository.findById(1L)).thenReturn(Optional.of(doctor));
        when(userRepository.existsByEmail("existing@example.com")).thenReturn(true);

        // Act & Assert
        assertThrows(RuntimeException.class, () -> doctorService.updateDoctor(updateDto));
        verify(userRepository, never()).save(any(User.class));
        verify(doctorRepository, never()).save(any(Doctor.class));
    }
}