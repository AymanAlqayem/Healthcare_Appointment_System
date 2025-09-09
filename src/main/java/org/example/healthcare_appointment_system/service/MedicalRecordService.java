package org.example.healthcare_appointment_system.service;

import lombok.RequiredArgsConstructor;
import org.example.healthcare_appointment_system.document.Prescription;
import org.example.healthcare_appointment_system.dto.MedicalHistoryResponseDto;
import org.example.healthcare_appointment_system.dto.PrescriptionHistoryDto;
import org.example.healthcare_appointment_system.entity.Appointment;
import org.example.healthcare_appointment_system.entity.Patient;
import org.example.healthcare_appointment_system.repo.AppointmentRepository;
import org.example.healthcare_appointment_system.repo.PatientRepository;
import org.example.healthcare_appointment_system.repo.PrescriptionRepository;
import org.springframework.stereotype.Service;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MedicalRecordService {
    private final PrescriptionRepository prescriptionRepository;
    private final PatientRepository patientRepository;
    private final AppointmentRepository appointmentRepository;

    public MedicalHistoryResponseDto getPatientMedicalHistory(Long patientId) {
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new RuntimeException("Patient not found with id: " + patientId));

        List<Prescription> prescriptions = prescriptionRepository.findByPatientId(patientId);

        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

        List<PrescriptionHistoryDto> history = prescriptions.stream()
                .map(p -> {
                    Appointment appointment = appointmentRepository.findById(p.getAppointmentId())
                            .orElseThrow(() -> new RuntimeException("Appointment not found: " + p.getAppointmentId()));

                    String day = appointment.getSlot().getDayOfWeek().name();
                    String time = appointment.getSlot().getStartTime().format(timeFormatter) +
                            " - " + appointment.getSlot().getEndTime().format(timeFormatter);

                    String createdAt = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm")
                            .withZone(ZoneId.systemDefault())
                            .format(p.getCreatedAt());

                    return new PrescriptionHistoryDto(
                            day,
                            time,
                            createdAt,
                            p.getNotes(),
                            p.getMedicines()
                    );
                })
                .toList();

        return new MedicalHistoryResponseDto(
                patient.getId(),
                patient.getUser().getUsername(),
                history
        );
    }
}

