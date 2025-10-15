package com.example.health_care_system.config;

import com.example.health_care_system.model.Doctor;
import com.example.health_care_system.model.AppointmentSlot;
import com.example.health_care_system.repository.DoctorRepository;
import com.example.health_care_system.repository.AppointmentSlotRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;
import java.util.List;

@Configuration
@RequiredArgsConstructor
public class DataSeeder {

    private final DoctorRepository doctorRepository;
    private final AppointmentSlotRepository slotRepository;

    @Bean
    public CommandLineRunner seedData() {
        return args -> {
            if (doctorRepository.count() == 0) {
                Doctor doctor1 = new Doctor("Dr. John Doe", "Cardiology");
                Doctor doctor2 = new Doctor("Dr. Jane Smith", "Dermatology");
                doctorRepository.saveAll(List.of(doctor1, doctor2));
            }

            if (slotRepository.count() == 0) {
                List<Doctor> doctors = doctorRepository.findAll();
                if (!doctors.isEmpty()) {
                    AppointmentSlot slot1 = new AppointmentSlot(LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(1).plusHours(1), false, doctors.get(0).getId());
                    AppointmentSlot slot2 = new AppointmentSlot(LocalDateTime.now().plusDays(2), LocalDateTime.now().plusDays(2).plusHours(1), false, doctors.get(0).getId());
                    AppointmentSlot slot3 = new AppointmentSlot(LocalDateTime.now().plusDays(3), LocalDateTime.now().plusDays(3).plusHours(1), false, doctors.get(1).getId());
                    AppointmentSlot slot4 = new AppointmentSlot(LocalDateTime.now().plusDays(4), LocalDateTime.now().plusDays(4).plusHours(1), false, doctors.get(1).getId());
                    slotRepository.saveAll(List.of(slot1, slot2, slot3, slot4));
                }
            }
        };
    }
}
