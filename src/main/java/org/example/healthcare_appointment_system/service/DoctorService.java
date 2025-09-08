package org.example.healthcare_appointment_system.service;

import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.healthcare_appointment_system.dto.AvailabilitySlotResponseDto;
import org.example.healthcare_appointment_system.dto.DoctorDto;
import org.example.healthcare_appointment_system.dto.DoctorResponseDto;
import org.example.healthcare_appointment_system.dto.DoctorUpdateDto;
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
import java.util.Set;

@Service
@RequiredArgsConstructor
public class DoctorService {
    private final UserRepository userRepository;
    private final DoctorRepository doctorRepository;
    private final PasswordEncoder passwordEncoder;

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

        // Create User account
        User user = User.builder().username(dto.username()).email(dto.email()).phone(dto.phone()).password(passwordEncoder.encode(dto.password())).role(Role.DOCTOR).enabled(true).build();

        userRepository.save(user);

        // Create Doctor profile
        Doctor doctor = new Doctor();
        doctor.setUser(user);
        doctor.setSpecialty(dto.specialty());

        // Handle slots if provided
        if (dto.slots() != null && !dto.slots().isEmpty()) {
            List<AvailabilitySlot> slots = dto.slots().stream().map(slotDto -> {
                AvailabilitySlot slot = new AvailabilitySlot();
                slot.setDate(slotDto.date());
                slot.setStartTime(slotDto.startTime());
                slot.setEndTime(slotDto.endTime());
                slot.setReserved(false);
                slot.setDoctor(doctor);
                return slot;
            }).toList();
            doctor.setAvailabilitySlots(slots);
        }

        Doctor savedDoctor = doctorRepository.save(doctor);

        // Return response
        List<AvailabilitySlotResponseDto> slots = savedDoctor.getAvailabilitySlots()
                .stream()
                .map(slot -> new AvailabilitySlotResponseDto(
                        slot.getId(),
                        slot.getDate().format(DateTimeFormatter.ofPattern("dd-MM-yyyy")),
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
        return doctorRepository.findAll()
                .stream()
                .map(doctor -> {
                    List<AvailabilitySlotResponseDto> slots = doctor.getAvailabilitySlots()
                            .stream()
                            .map(slot -> new AvailabilitySlotResponseDto(
                                    slot.getId(),
                                    slot.getDate().format(DateTimeFormatter.ofPattern("dd-MM-yyyy")),
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
        Doctor doctor = doctorRepository.findById(id).orElseThrow(() -> new RuntimeException("Doctor not found with id: " + id));

        //Also delete the associated User if desired
        User user = doctor.getUser();

        // Delete doctor entity
        doctorRepository.delete(doctor);

        // Delete the associated user
        if (user != null) {
            userRepository.delete(user);
        }
        return ResponseEntity.ok("Doctor deleted successfully");
    }

    @Transactional
    public DoctorResponseDto updateDoctor(DoctorUpdateDto dto) {
        Doctor doctor = doctorRepository.findById(dto.id()).orElseThrow(() -> new RuntimeException("Doctor not found with id: " + dto.id()));

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
        doctorRepository.save(doctor);

        List<AvailabilitySlotResponseDto> slots = doctor.getAvailabilitySlots()
                .stream()
                .map(slot -> new AvailabilitySlotResponseDto(
                        slot.getId(),
                        slot.getDate().format(DateTimeFormatter.ofPattern("dd-MM-yyyy")),
                        slot.getStartTime().format(DateTimeFormatter.ofPattern("HH:mm")),
                        slot.getEndTime().format(DateTimeFormatter.ofPattern("HH:mm")),
                        slot.isReserved()
                ))
                .toList();

        return new DoctorResponseDto(
                doctor.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getPhone(),
                doctor.getSpecialty(),
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
                                    slot.getDate().format(DateTimeFormatter.ofPattern("dd-MM-yyyy")),
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



