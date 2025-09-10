package org.example.healthcare_appointment_system;

import org.example.healthcare_appointment_system.dto.*;
import org.example.healthcare_appointment_system.entity.Patient;
import org.example.healthcare_appointment_system.entity.User;
import org.example.healthcare_appointment_system.enums.Gender;
import org.example.healthcare_appointment_system.enums.Role;
import org.example.healthcare_appointment_system.repo.PatientRepository;
import org.example.healthcare_appointment_system.repo.UserRepository;
import org.example.healthcare_appointment_system.security.SecurityUtils;
import org.example.healthcare_appointment_system.service.PatientService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.*;

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

    @InjectMocks
    private PatientService patientService;

    private PatientDto patientDto;
    private User user;
    private Patient patient;
    private MockedStatic<SecurityUtils> securityUtilsMock;

    @BeforeEach
    void setUp() {
        patientDto = new PatientDto(
                "patientUser",
                "password123",
                "1234567890",
                "patient@example.com",
                Gender.MALE,
                LocalDate.of(1990, 1, 1)
        );

        user = User.builder()
                .id(1L)
                .username("patientUser")
                .email("patient@example.com")
                .phone("1234567890")
                .password("encodedPassword")
                .role(Role.PATIENT)
                .enabled(true)
                .build();

        patient = new Patient();
        patient.setId(1L);
        patient.setUser(user);
        patient.setGender(Gender.MALE);
        patient.setDateOfBirth(LocalDate.of(1990, 1, 1));

        securityUtilsMock = Mockito.mockStatic(SecurityUtils.class);
        securityUtilsMock.when(SecurityUtils::getCurrentUserId).thenReturn(1L);
    }

    @AfterEach
    void tearDown() {
        if (securityUtilsMock != null) {
            securityUtilsMock.close();
        }
    }

    @Test
    void createPatient_Success() {
        when(userRepository.existsByUsername(patientDto.username())).thenReturn(false);
        when(userRepository.existsByEmail(patientDto.email())).thenReturn(false);
        when(userRepository.existsByPhone(patientDto.phone())).thenReturn(false);
        when(passwordEncoder.encode(patientDto.password())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(patientRepository.save(any(Patient.class))).thenReturn(patient);

        PatientResponseDto result = patientService.createPatient(patientDto);

        assertNotNull(result);
        assertEquals("patientUser", result.username());
        assertEquals("MALE", result.gender());
        verify(userRepository, times(1)).save(any(User.class));
        verify(patientRepository, times(1)).save(any(Patient.class));
    }

    @Test
    void createPatient_UsernameExists_ThrowsException() {
        when(userRepository.existsByUsername(patientDto.username())).thenReturn(true);

        assertThrows(RuntimeException.class, () -> patientService.createPatient(patientDto));
        verify(userRepository, never()).save(any(User.class));
        verify(patientRepository, never()).save(any(Patient.class));
    }

    @Test
    void deletePatient_Success() {
        when(patientRepository.findById(1L)).thenReturn(Optional.of(patient));

        ResponseEntity<String> result = patientService.deletePatient(1L);

        assertNotNull(result);
        assertEquals(200, result.getStatusCodeValue());
        assertEquals("Patient deleted successfully", result.getBody());
        verify(patientRepository, times(1)).delete(patient);
        verify(userRepository, times(1)).delete(user);
    }

    @Test
    void deletePatient_NotFound_ThrowsException() {
        when(patientRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> patientService.deletePatient(1L));
        verify(patientRepository, never()).delete(any());
        verify(userRepository, never()).delete(any());
    }

    @Test
    void getAllPatients_Success() {
        List<Patient> patients = List.of(patient);
        when(patientRepository.findAll()).thenReturn(patients);

        List<PatientResponseDto> result = patientService.getAllPatients();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("patientUser", result.get(0).username());
        verify(patientRepository, times(1)).findAll();
    }


    @Test
    void updateInfo_Success() {
        PatientUpdateDto updateDto = new PatientUpdateDto(
                "0987654321",
                "updated@example.com",
                Gender.FEMALE,
                LocalDate.of(1995, 1, 1)
        );

        when(patientRepository.findByUserId(1L)).thenReturn(Optional.of(patient));
        when(patientRepository.save(any(Patient.class))).thenReturn(patient);

        PatientResponseDto result = patientService.updateInfo(updateDto);

        assertNotNull(result);
        verify(patientRepository, times(1)).save(patient);
        assertEquals("updated@example.com", patient.getUser().getEmail());
        assertEquals("0987654321", patient.getUser().getPhone());
        assertEquals(Gender.FEMALE, patient.getGender());
    }

    @Test
    void findPatient_Success() {
        when(userRepository.findByUsername("patientUser")).thenReturn(Optional.of(user));
        when(patientRepository.findByUserId(1L)).thenReturn(Optional.of(patient));

        ResponseEntity<PatientResponseDto> result = patientService.findPatient("patientUser");

        assertNotNull(result);
        assertEquals(200, result.getStatusCodeValue());
        assertNotNull(result.getBody());
        assertEquals("patientUser", result.getBody().username());
        verify(userRepository, times(1)).findByUsername("patientUser");
        verify(patientRepository, times(1)).findByUserId(1L);
    }

    @Test
    void findPatient_UserNotFound_ThrowsException() {
        when(userRepository.findByUsername("unknownUser")).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> patientService.findPatient("unknownUser"));
    }
}