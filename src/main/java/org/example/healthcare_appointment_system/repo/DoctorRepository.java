package org.example.healthcare_appointment_system.repo;

import org.example.healthcare_appointment_system.entity.Doctor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface DoctorRepository extends JpaRepository<Doctor, Long> {

    @Query("SELECT d FROM Doctor d WHERE LOWER(d.specialty) = LOWER(:specialty)")
    @org.hibernate.annotations.Cache(usage = org.hibernate.annotations.CacheConcurrencyStrategy.READ_WRITE, region = "doctorBySpecialty")
    List<Doctor> findBySpecialtyIgnoreCase(@Param("specialty") String specialty);

    Optional<Doctor> findByUserId(Long userId);

    @Override
    @org.hibernate.annotations.Cache(usage = org.hibernate.annotations.CacheConcurrencyStrategy.READ_WRITE, region = "doctors")
    Optional<Doctor> findById(Long id);

    @Override
    @Query("SELECT d FROM Doctor d LEFT JOIN FETCH d.availabilitySlots")
    @org.hibernate.annotations.Cache(usage = org.hibernate.annotations.CacheConcurrencyStrategy.READ_WRITE, region = "allDoctors")
    List<Doctor> findAll();
}