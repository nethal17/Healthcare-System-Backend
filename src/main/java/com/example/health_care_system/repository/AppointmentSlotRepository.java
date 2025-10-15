package com.example.health_care_system.repository;

import com.example.health_care_system.model.AppointmentSlot;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface AppointmentSlotRepository extends MongoRepository<AppointmentSlot,String> {
    List<AppointmentSlot> findByDoctorIdAndBookedFalse(String doctorId);

}
