package org.example.healthcare_appointment_system;

import org.example.healthcare_appointment_system.cacheTest.CacheService;
import org.example.healthcare_appointment_system.dto.*;
import org.example.healthcare_appointment_system.entity.*;
import org.example.healthcare_appointment_system.enums.Role;
import org.example.healthcare_appointment_system.enums.WeekDay;
import org.example.healthcare_appointment_system.repo.DoctorRepository;
import org.example.healthcare_appointment_system.repo.UserRepository;
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
import java.util.List;
import java.util.Optional;

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

    private DoctorDto testDoctorDto;
    private User testUser;
    private Doctor testDoctor;

    @BeforeEach
    void setUp() {
        // Setup test data with proper slots
        TimeRangeDto timeRange = new TimeRangeDto(LocalTime.of(9, 0), LocalTime.of(10, 0));
        DaySlotsCreateDto daySlots = new DaySlotsCreateDto(WeekDay.MONDAY, List.of(timeRange));

        testDoctorDto = new DoctorDto(
                "doctor1", "doctor@example.com", "1234567890", "password123",
                "Cardiology", List.of(daySlots)
        );

        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("doctor1");
        testUser.setEmail("doctor@example.com");
        testUser.setPhone("1234567890");
        testUser.setRole(Role.DOCTOR);

        testDoctor = new Doctor();
        testDoctor.setId(1L);
        testDoctor.setSpecialty("Cardiology");
        testDoctor.setUser(testUser);

        // Create actual availability slots
        AvailabilitySlot slot = new AvailabilitySlot();
        slot.setId(1L);
        slot.setDayOfWeek(WeekDay.MONDAY);
        slot.setStartTime(LocalTime.of(9, 0));
        slot.setEndTime(LocalTime.of(10, 0));
        slot.setReserved(false);
        slot.setDoctor(testDoctor);

        testDoctor.setAvailabilitySlots(List.of(slot));
    }

    @Test
    void createDoctor_Success() {
        // Arrange - Correct validation order (NO password check!)
        when(userRepository.existsByUsername("doctor1")).thenReturn(false);
        when(userRepository.existsByEmail("doctor@example.com")).thenReturn(false);
        when(userRepository.existsByPhone("1234567890")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(doctorRepository.save(any(Doctor.class))).thenReturn(testDoctor);

        // Act
        DoctorResponseDto result = doctorService.createDoctor(testDoctorDto);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.id());
        assertEquals("doctor1", result.username());
        assertEquals("Cardiology", result.specialty());

        verify(userRepository).existsByUsername("doctor1");
        verify(userRepository).existsByEmail("doctor@example.com");
        verify(userRepository).existsByPhone("1234567890");
    }

    @Test
    void createDoctor_UsernameExists_ThrowsException() {
        // Arrange - Remove the unnecessary password email check!
        when(userRepository.existsByUsername("doctor1")).thenReturn(true);

        // Act & Assert
        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> doctorService.createDoctor(testDoctorDto));

        assertTrue(exception.getMessage().contains("Username already exists"));

        verify(userRepository, never()).save(any());
        verify(doctorRepository, never()).save(any());
    }

    @Test
    void searchBySpecialty_Success() {
        // Arrange
        when(doctorRepository.findBySpecialtyIgnoreCase("Cardiology"))
                .thenReturn(List.of(testDoctor));

        // Act
        List<DoctorResponseDto> result = doctorService.searchBySpecialty("Cardiology");

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Cardiology", result.get(0).specialty());
    }

    @Test
    void searchBySpecialty_NotFound_ThrowsException() {
        // Arrange
        when(doctorRepository.findBySpecialtyIgnoreCase("Neurology"))
                .thenReturn(List.of());

        // Act & Assert - Now expecting ResponseStatusException
        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> doctorService.searchBySpecialty("Neurology"));

        assertTrue(exception.getMessage().contains("No doctors found with specialty: Neurology"));
    }

    @Test
    void deleteDoctor_Success() {
        // Arrange
        when(doctorRepository.findById(1L)).thenReturn(Optional.of(testDoctor));

        // Act
        var result = doctorService.deleteDoctor(1L);

        // Assert
        assertNotNull(result);
        assertEquals("Doctor deleted successfully", result.getBody());

        verify(doctorRepository).delete(testDoctor);
        verify(userRepository).delete(testUser);
        verify(cacheService).evictDoctorCache(1L);
        verify(cacheService).evictDoctorBySpecialtyCache("Cardiology");
        verify(cacheService).evictAllDoctorsCache();
    }
}