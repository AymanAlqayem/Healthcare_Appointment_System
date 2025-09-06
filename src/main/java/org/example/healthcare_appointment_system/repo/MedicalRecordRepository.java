package org.example.healthcare_appointment_system.repo;

import org.example.healthcare_appointment_system.document.MedicalRecord;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface MedicalRecordRepository extends MongoRepository<MedicalRecord, String> {}
