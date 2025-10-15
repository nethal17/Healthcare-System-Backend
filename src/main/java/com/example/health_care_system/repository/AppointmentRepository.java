package com.example.health_care_system.repository;

import com.example.health_care_system.model.Appointment;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface AppointmentRepository extends MongoRepository<Appointment,String> {
    List<Appointment> findByPatientId(String patientId);
    boolean existsBySlotId(String slotId);
}
