package com.example.health_care_system.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "appointments")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Appointment {
    @Id
    private String id;

    private String patientId;   // Reference to User
    private String doctorId;    // Reference to Doctor
    private String slotId;      // Reference to AppointmentSlot

    private String status = "BOOKED"; // BOOKED, CANCELLED, COMPLETED
    private LocalDateTime createdAt = LocalDateTime.now();
}
