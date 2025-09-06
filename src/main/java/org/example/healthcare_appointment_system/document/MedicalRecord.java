package org.example.healthcare_appointment_system.document;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Document(collection = "medical_records")
@Getter
@Setter
public class MedicalRecord {

    @Id
    private String id;

    private Long patientId;  // reference Patient.id
    private Long doctorId;

    private Instant createdAt = Instant.now();
    private String notes;
    private List<String> attachments; // e.g., URLs or file paths
    private Map<String, String> labResults;

}

