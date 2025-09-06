package org.example.healthcare_appointment_system.document;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;

@Document(collection = "prescriptions")
@Getter
@Setter
public class Prescription {
    @Id
    private String id;

    private Long patientId;     // reference Patient.id
    private Long doctorId;      // reference Doctor.id
    private Long appointmentId; // reference Appointment.id

    private Instant createdAt = Instant.now();
    private String notes;
    private List<String> medicines;
}

