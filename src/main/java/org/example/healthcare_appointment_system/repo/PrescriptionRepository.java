package org.example.healthcare_appointment_system.repo;

import org.example.healthcare_appointment_system.document.Prescription;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface PrescriptionRepository extends MongoRepository<Prescription, String> {}
