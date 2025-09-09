package org.example.healthcare_appointment_system.repo;

import org.example.healthcare_appointment_system.document.MedicalRecord;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface MedicalRecordRepository extends MongoRepository<MedicalRecord, String> {
    List<MedicalRecord> findByPatientId(Long patientId);
}
