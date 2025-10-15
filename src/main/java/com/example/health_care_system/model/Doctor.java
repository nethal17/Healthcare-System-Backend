package com.example.health_care_system.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.List;

@Document(collection = "doctors")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Doctor {
    @Id
    private String id;

    private String name;
    private String specialization;
    private String email;
    private String phone;

    private List<String> slotIds; // optional for future use

    // Constructor without id for seeding purposes
    public Doctor(String name, String specialization) {
        this.name = name;
        this.specialization = specialization;
    }
}
