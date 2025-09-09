package org.example.healthcare_appointment_system.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.healthcare_appointment_system.document.Prescription;
import org.example.healthcare_appointment_system.dto.PrescriptionDto;
import org.example.healthcare_appointment_system.dto.PrescriptionResponseDto;
import org.example.healthcare_appointment_system.entity.Appointment;
import org.example.healthcare_appointment_system.entity.Doctor;
import org.example.healthcare_appointment_system.repo.AppointmentRepository;
import org.example.healthcare_appointment_system.repo.DoctorRepository;
import org.example.healthcare_appointment_system.repo.PrescriptionRepository;
import org.example.healthcare_appointment_system.security.SecurityUtils;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PrescriptionService {

    private final PrescriptionRepository prescriptionRepository;
    private final AppointmentRepository appointmentRepository;
    private final DoctorRepository doctorRepository;

    @Transactional
    public PrescriptionResponseDto createPrescription(Long patientId, PrescriptionDto dto) {
        // Get logged-in doctor
        Long currentUserId = SecurityUtils.getCurrentUserId();
        Doctor doctor = doctorRepository.findByUserId(currentUserId)
                .orElseThrow(() -> new RuntimeException("Doctor not found for current user"));

        // Validate appointment belongs to patient + doctor
        Appointment appointment = appointmentRepository.findById(dto.appointmentId())
                .orElseThrow(() -> new RuntimeException("Appointment not found"));

        if (!appointment.getPatient().getId().equals(patientId)) {
            throw new RuntimeException("Appointment does not belong to this patient");
        }
        if (!appointment.getDoctor().getId().equals(doctor.getId())) {
            throw new RuntimeException("You can only prescribe for your own appointments");
        }

        // Create prescription (Mongo)
        Prescription prescription = new Prescription();
        prescription.setPatientId(patientId);
        prescription.setDoctorId(doctor.getId());
        prescription.setAppointmentId(dto.appointmentId());
        prescription.setNotes(dto.notes());
        prescription.setMedicines(dto.medicines());

        Prescription saved = prescriptionRepository.save(prescription);

        return new PrescriptionResponseDto(
                saved.getId(),
                saved.getPatientId(),
                saved.getDoctorId(),
                saved.getAppointmentId(),
                saved.getCreatedAt().toString(),
                saved.getNotes(),
                saved.getMedicines()
        );
    }
}

