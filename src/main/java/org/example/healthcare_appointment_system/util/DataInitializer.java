package org.example.healthcare_appointment_system.util;

import org.example.healthcare_appointment_system.document.MedicalRecord;
import org.example.healthcare_appointment_system.document.Prescription;
import org.example.healthcare_appointment_system.entity.User;
import org.example.healthcare_appointment_system.enums.Role;
import org.example.healthcare_appointment_system.repo.MedicalRecordRepository;
import org.example.healthcare_appointment_system.repo.PrescriptionRepository;
import org.example.healthcare_appointment_system.repo.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Component
public class DataInitializer implements CommandLineRunner {

    private final PrescriptionRepository prescriptionRepository;
    private final MedicalRecordRepository medicalRecordRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(PrescriptionRepository prescriptionRepository,
                           MedicalRecordRepository medicalRecordRepository,
                           UserRepository userRepository,
                           PasswordEncoder passwordEncoder) {
        this.prescriptionRepository = prescriptionRepository;
        this.medicalRecordRepository = medicalRecordRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        // ✅ Ensure admin user exists
        if (userRepository.findByUsername("ayman").isEmpty()) {
            User admin = User.builder()
                    .username("ayman")
                    .password(passwordEncoder.encode("123456"))
                    .email("ayman@example.com")
                    .phone("1234567890")
                    .enabled(true)
                    .role(Role.ADMIN)
                    .build();

            userRepository.save(admin);
            System.out.println("✅ Admin user created: ayman / 123456");
        } else {
            System.out.println("ℹ️ Admin user already exists, skipping creation.");
        }

        // ✅ Clear existing MongoDB data
        prescriptionRepository.deleteAll();
        medicalRecordRepository.deleteAll();

        // Sample Prescriptions
        Prescription prescription1 = new Prescription();
        prescription1.setPatientId(1L);
        prescription1.setDoctorId(1L);
        prescription1.setAppointmentId(1L);
        prescription1.setNotes("Take after meals");
        prescription1.setMedicines(List.of("Paracetamol", "Vitamin D"));
        prescription1.setCreatedAt(Instant.now());

        Prescription prescription2 = new Prescription();
        prescription2.setPatientId(2L);
        prescription2.setDoctorId(1L);
        prescription2.setAppointmentId(2L);
        prescription2.setNotes("Morning dosage");
        prescription2.setMedicines(List.of("Ibuprofen"));
        prescription2.setCreatedAt(Instant.now());

        prescriptionRepository.saveAll(List.of(prescription1, prescription2));

        // Sample Medical Records
        MedicalRecord record1 = new MedicalRecord();
        record1.setPatientId(1L);
        record1.setDoctorId(1L);
        record1.setNotes("Patient shows improvement");
        record1.setLabResults(Map.of(
                "Hemoglobin", "13.5 g/dL",
                "Cholesterol", "200 mg/dL"
        ));
        record1.setAttachments(List.of("scan1.pdf", "xray1.png"));
        record1.setCreatedAt(Instant.now());

        MedicalRecord record2 = new MedicalRecord();
        record2.setPatientId(2L);
        record2.setDoctorId(1L);
        record2.setNotes("Requires follow-up");
        record2.setLabResults(Map.of(
                "Blood Sugar", "95 mg/dL",
                "Vitamin D", "30 ng/mL"
        ));
        record2.setAttachments(List.of("scan2.pdf"));
        record2.setCreatedAt(Instant.now());

        medicalRecordRepository.saveAll(List.of(record1, record2));

        System.out.println("✅ MongoDB sample data initialized!");
    }
}
