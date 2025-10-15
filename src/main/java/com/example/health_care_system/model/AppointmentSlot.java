package com.example.health_care_system.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "appointment_slots")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AppointmentSlot {
    @Id
    private String id;

    private String doctorId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;

    private boolean booked = false;

    // Constructor for seeding purposes
    public AppointmentSlot(LocalDateTime startTime, LocalDateTime endTime, boolean booked, String doctorId) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.booked = booked;
        this.doctorId = doctorId;
    }
}
