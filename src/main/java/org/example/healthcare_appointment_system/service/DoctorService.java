package org.example.healthcare_appointment_system.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.healthcare_appointment_system.cacheTest.CacheService;
import org.example.healthcare_appointment_system.dto.*;
import org.example.healthcare_appointment_system.entity.AvailabilitySlot;
import org.example.healthcare_appointment_system.entity.Doctor;
import org.example.healthcare_appointment_system.entity.User;
import org.example.healthcare_appointment_system.enums.Role;
import org.example.healthcare_appointment_system.repo.DoctorRepository;
import org.example.healthcare_appointment_system.repo.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DoctorService {
    private final UserRepository userRepository;
    private final DoctorRepository doctorRepository;
    private final PasswordEncoder passwordEncoder;
    private final CacheService cacheService;

    @Transactional
    public DoctorResponseDto createDoctor(DoctorDto dto) {
        if (userRepository.existsByUsername(dto.username())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Username already exists");
        }
        if (userRepository.existsByEmail(dto.email())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email already exists");
        }
        if (userRepository.existsByPhone(dto.phone())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Phone already exists");
        }

        // Create User
        User user = User.builder()
                .username(dto.username())
                .email(dto.email())
                .phone(dto.phone())
                .password(passwordEncoder.encode(dto.password()))
                .role(Role.DOCTOR)
                .enabled(true)
                .build();

        userRepository.save(user);

        // Create Doctor profile
        Doctor doctor = new Doctor();
        doctor.setUser(user);
        doctor.setSpecialty(dto.specialty());

        // Handle weekly slots if provided
        if (dto.slots() != null && !dto.slots().isEmpty()) {
            List<AvailabilitySlot> slots = new ArrayList<>();

            for (DaySlotsCreateDto daySlots : dto.slots()) {
                for (TimeRangeDto range : daySlots.slots()) {
                    AvailabilitySlot slot = new AvailabilitySlot();
                    slot.setDoctor(doctor);
                    slot.setDayOfWeek(daySlots.dayOfWeek());
                    slot.setStartTime(range.startTime());
                    slot.setEndTime(range.endTime());
                    slot.setReserved(false);
                    slots.add(slot);
                }
            }

            doctor.setAvailabilitySlots(slots);
        }

        Doctor savedDoctor = doctorRepository.save(doctor);

        // Clear relevant caches after creating a new doctor
        cacheService.evictAllDoctorsCache();
        cacheService.evictDoctorBySpecialtyCache(savedDoctor.getSpecialty());

        List<AvailabilitySlotResponseDto> slots = savedDoctor.getAvailabilitySlots().stream()
                .map(slot -> new AvailabilitySlotResponseDto(
                        slot.getId(),
                        slot.getDayOfWeek().name(),
                            slot.getStartTime().format(DateTimeFormatter.ofPattern("HH:mm")),
                        slot.getEndTime().format(DateTimeFormatter.ofPattern("HH:mm")),
                        slot.isReserved()
                ))
                .toList();

        return new DoctorResponseDto(
                savedDoctor.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getPhone(),
                savedDoctor.getSpecialty(),
                slots
        );
    }

    public List<DoctorResponseDto> getAllDoctors() {
        List<Doctor> doctors = doctorRepository.findAll();

        return doctors.stream()
                .map(doctor -> {
                    List<AvailabilitySlotResponseDto> slots = doctor.getAvailabilitySlots()
                            .stream()
                            .map(slot -> new AvailabilitySlotResponseDto(
                                    slot.getId(),
                                    slot.getDayOfWeek().name(),
                                    slot.getStartTime().format(DateTimeFormatter.ofPattern("HH:mm")),
                                    slot.getEndTime().format(DateTimeFormatter.ofPattern("HH:mm")),
                                    slot.isReserved()
                            ))
                            .toList();

                    return new DoctorResponseDto(
                            doctor.getId(),
                            doctor.getUser().getUsername(),
                            doctor.getUser().getEmail(),
                            doctor.getUser().getPhone(),
                            doctor.getSpecialty(),
                            slots
                    );
                })
                .toList();
    }

    public ResponseEntity<String> deleteDoctor(Long id) {
        Doctor doctor = doctorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Doctor not found with id: " + id));

        User user = doctor.getUser();

        // Clear caches before deletion
        cacheService.evictDoctorCache(id);
        cacheService.evictDoctorBySpecialtyCache(doctor.getSpecialty());
        cacheService.evictAllDoctorsCache();

        doctorRepository.delete(doctor);

        if (user != null) {
            userRepository.delete(user);
        }

        return ResponseEntity.ok("Doctor deleted successfully");
    }

    @Transactional
    public DoctorResponseDto updateDoctor(DoctorUpdateDto dto) {
        Doctor doctor = doctorRepository.findById(dto.id())
                .orElseThrow(() -> new RuntimeException("Doctor not found with id: " + dto.id()));

        String oldSpecialty = doctor.getSpecialty();

        // Update User info
        User user = doctor.getUser();
        if (!user.getEmail().equals(dto.email()) && userRepository.existsByEmail(dto.email())) {
            throw new RuntimeException("Email already exists");
        }
        user.setEmail(dto.email());
        user.setPhone(dto.phone());
        userRepository.save(user);

        // Update Doctor-specific info
        doctor.setSpecialty(dto.specialty());
        Doctor updatedDoctor = doctorRepository.save(doctor);

        // Clear relevant caches after update
        cacheService.evictDoctorCache(doctor.getId());
        cacheService.evictAllDoctorsCache();

        // If specialty changed, clear both old and new specialty caches
        if (!oldSpecialty.equals(dto.specialty())) {
            cacheService.evictDoctorBySpecialtyCache(oldSpecialty);
        }
        cacheService.evictDoctorBySpecialtyCache(dto.specialty());

        List<AvailabilitySlotResponseDto> slots = updatedDoctor.getAvailabilitySlots()
                .stream()
                .map(slot -> new AvailabilitySlotResponseDto(
                        slot.getId(),
                        slot.getDayOfWeek().name(),
                        slot.getStartTime().format(DateTimeFormatter.ofPattern("HH:mm")),
                        slot.getEndTime().format(DateTimeFormatter.ofPattern("HH:mm")),
                        slot.isReserved()
                ))
                .toList();

        return new DoctorResponseDto(
                updatedDoctor.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getPhone(),
                updatedDoctor.getSpecialty(),
                slots
        );
    }

    public List<DoctorResponseDto> searchBySpecialty(String specialty) {
        List<Doctor> doctors = doctorRepository.findBySpecialtyIgnoreCase(specialty);

        if (doctors.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No doctors found with specialty: " + specialty);
        }

        return doctors.stream()
                .map(doctor -> {
                    List<AvailabilitySlotResponseDto> slots = doctor.getAvailabilitySlots()
                            .stream()
                            .map(slot -> new AvailabilitySlotResponseDto(
                                    slot.getId(),
                                    slot.getDayOfWeek().name(),
                                    slot.getStartTime().format(DateTimeFormatter.ofPattern("HH:mm")),
                                    slot.getEndTime().format(DateTimeFormatter.ofPattern("HH:mm")),
                                    slot.isReserved()
                            ))
                            .toList();

                    return new DoctorResponseDto(
                            doctor.getId(),
                            doctor.getUser().getUsername(),
                            doctor.getUser().getEmail(),
                            doctor.getUser().getPhone(),
                            doctor.getSpecialty(),
                            slots
                    );
                })
                .toList();
    }
}