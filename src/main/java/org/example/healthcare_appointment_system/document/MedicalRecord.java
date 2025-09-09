package org.example.healthcare_appointment_system.document;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.Instant;

@Document(collection = "medical_records")
@Getter
@Setter
public class MedicalRecord {
    @Id
    private String id;

    private Long patientId;
    private Long doctorId;

    private Instant createdAt = Instant.now();
    private String notes;
}

