//package org.example.healthcare_appointment_system.repo;
//
//import org.example.healthcare_appointment_system.entity.Appointment;
//import org.example.healthcare_appointment_system.entity.Doctor;
//import org.example.healthcare_appointment_system.entity.Patient;
//import org.springframework.data.jpa.repository.JpaRepository;
//
//import java.time.LocalDateTime;
//import java.util.List;
//
//public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
//    List<Appointment> findByDoctor(Doctor doctor);
//
//    List<Appointment> findByPatient(Patient patient);
//
//    List<Appointment> findByDoctorAndDateTime(Doctor doctor, LocalDateTime dateTime);
//}
//
