package org.example.healthcare_appointment_system.service;

import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.healthcare_appointment_system.dto.AdminDto;
import org.example.healthcare_appointment_system.dto.AdminResponseDto;
import org.example.healthcare_appointment_system.dto.SlotCreateDto;
import org.example.healthcare_appointment_system.dto.SlotResponseDto;
import org.example.healthcare_appointment_system.entity.AvailabilitySlot;
import org.example.healthcare_appointment_system.entity.Doctor;
import org.example.healthcare_appointment_system.entity.User;
import org.example.healthcare_appointment_system.enums.Role;
import org.example.healthcare_appointment_system.repo.AvailabilitySlotRepository;
import org.example.healthcare_appointment_system.repo.DoctorRepository;
import org.example.healthcare_appointment_system.repo.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AdminService {
    private final UserRepository userRepository;
    private final AvailabilitySlotRepository slotRepository;
    private final DoctorRepository doctorRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public AdminResponseDto createAdmin(AdminDto dto) {
        if (userRepository.existsByUsername(dto.username())) {
            throw new RuntimeException("Username already exists");
        }
        if (userRepository.existsByEmail(dto.email())) {
            throw new RuntimeException("Email already exists");
        }

        if (userRepository.existsByPhone(dto.phone())) {
            throw new RuntimeException("Phone already exists");
        }

        // Create admin user
        User admin = User.builder()
                .username(dto.username())
                .email(dto.email())
                .phone(dto.phone())
                .password(passwordEncoder.encode(dto.password()))
                .role(Role.ADMIN)
                .enabled(true)
                .build();

        // Save to DB
        User savedAdmin = userRepository.save(admin);

        // Build response DTO manually
        return new AdminResponseDto(
                savedAdmin.getId(),
                savedAdmin.getUsername(),
                savedAdmin.getEmail(),
                savedAdmin.getPhone()
        );
    }

    @Transactional
    public SlotResponseDto addSlot(Long doctorId, SlotCreateDto dto) {
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new RuntimeException("Doctor not found"));

        // Start time must be before end time
        if (!dto.startTime().isBefore(dto.endTime())) {
            throw new RuntimeException("Start time must be before end time");
        }

        // Prevent duplicates (exact same slot)
        if (slotRepository.existsByDoctorIdAndDateAndStartTimeAndEndTime(
                doctorId, dto.date(), dto.startTime(), dto.endTime())) {
            throw new RuntimeException("This slot already exists for the doctor");
        }

        // Prevent overlapping slots
        List<AvailabilitySlot> existingSlots = slotRepository.findByDoctorIdAndDate(doctorId, dto.date());

        boolean overlaps = existingSlots.stream().anyMatch(slot ->
                // (New start < existing end) AND (New end > existing start) = overlap
                dto.startTime().isBefore(slot.getEndTime()) &&
                        dto.endTime().isAfter(slot.getStartTime())
        );

        if (overlaps) {
            throw new RuntimeException("Slot overlaps with an existing slot");
        }

        // Save slot
        AvailabilitySlot slot = new AvailabilitySlot();
        slot.setDoctor(doctor);
        slot.setDate(dto.date());
        slot.setStartTime(dto.startTime());
        slot.setEndTime(dto.endTime());
        slot.setReserved(false);

        AvailabilitySlot saved = slotRepository.save(slot);

        return new SlotResponseDto(
                saved.getId(),
                saved.getDate().format(DateTimeFormatter.ofPattern("dd-MM-yyyy")),
                saved.getStartTime().format(DateTimeFormatter.ofPattern("HH:mm")),
                saved.getEndTime().format(DateTimeFormatter.ofPattern("HH:mm")),
                saved.isReserved()
        );
    }
}

