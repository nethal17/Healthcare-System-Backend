package com.example.health_care_system.service;

import com.example.health_care_system.model.*;
import com.example.health_care_system.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final AppointmentSlotRepository slotRepository;

    @Transactional
    public Appointment bookAppointment(String patientId, String slotId, String doctorId) {
        AppointmentSlot slot = slotRepository.findById(slotId)
                .orElseThrow(() -> new RuntimeException("Slot not found"));

        if (slot.isBooked()) {
            throw new RuntimeException("This slot is already booked!");
        }

        slot.setBooked(true);
        slotRepository.save(slot);

        Appointment appointment = new Appointment();
        appointment.setDoctorId(doctorId);
        appointment.setPatientId(patientId);
        appointment.setSlotId(slotId);
        appointment.setStatus("BOOKED");

        return appointmentRepository.save(appointment);
    }

    public void cancelAppointment(String appointmentId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));

        appointment.setStatus("CANCELLED");

        // free up the slot
        AppointmentSlot slot = slotRepository.findById(appointment.getSlotId())
                .orElseThrow(() -> new RuntimeException("Slot not found"));

        slot.setBooked(false);
        slotRepository.save(slot);

        appointmentRepository.save(appointment);
    }

    public List<AppointmentSlot> getAvailableSlots(String doctorId) {
        return slotRepository.findByDoctorIdAndBookedFalse(doctorId);
    }

    public List<Appointment> getAppointmentsByPatient(String patientId) {
        return appointmentRepository.findByPatientId(patientId);
    }
}
