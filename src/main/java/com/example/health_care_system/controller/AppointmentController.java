package com.example.health_care_system.controller;

import com.example.health_care_system.model.Appointment;
import com.example.health_care_system.model.AppointmentSlot;
import com.example.health_care_system.service.AppointmentService;
import com.example.health_care_system.model.Doctor;
import com.example.health_care_system.model.User;
import com.example.health_care_system.service.DoctorService;
import com.example.health_care_system.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/appointments")

public class AppointmentController {
    @Autowired
    private final AppointmentService appointmentService;
    @Autowired
    private final DoctorService doctorService;
    @Autowired
    private final UserService userService;

    // 🧭 1. Show the main appointment page
    @GetMapping()
    public String showAppointmentPage(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        User user = userService.findByUsername(username);
        List<Doctor> doctors = doctorService.getAllDoctors();
        List<Appointment> appointments = List.of();
        List<AppointmentViewDto> appointmentViews = List.of();
        if (user != null) {
            appointments = appointmentService.getAppointmentsByPatient(user.getId());
            appointmentViews = appointments.stream().map(appt -> {
                Doctor doc = doctorService.getDoctorById(appt.getDoctorId());
                String doctorName = doc != null ? doc.getName() : appt.getDoctorId();
                java.time.LocalDateTime slotTime = null;
                // Optionally, fetch slot time if needed
                return new AppointmentViewDto(
                    appt.getId(),
                    doctorName,
                    appt.getStatus(),
                    appt.getCreatedAt()
                );
            }).toList();
        }
        model.addAttribute("doctors", doctors);
        model.addAttribute("slots", List.of());
        model.addAttribute("appointments", appointmentViews);
        model.addAttribute("activePage", "appointments");
        return "appointment"; // Thymeleaf template name
    }

    // ⚙️ 2. Load slots dynamically for a selected doctor (AJAX or htmx)
    @GetMapping("/slots/{doctorId}")
    @ResponseBody
    public List<AppointmentSlot> getSlotsForDoctor(@PathVariable String doctorId) {
        return appointmentService.getAvailableSlots(doctorId);
    }

    // ✅ 3. Book an appointment
    @PostMapping("/book")
    public String bookAppointment(
            @RequestParam String doctorId,
            @RequestParam String slotId) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            User user = userService.findByUsername(username);
            if (user == null) {
                throw new RuntimeException("User not authenticated");
            }
            appointmentService.bookAppointment(user.getId(), slotId, doctorId);
            return "redirect:/appointments?success";
        } catch (Exception e) {
            e.printStackTrace();
            return "redirect:/appointments?error=" + e.getMessage();
        }
    }

    // ❌ 4. Cancel appointment
    @PostMapping("/cancel")
    public String cancelAppointment(@RequestParam String appointmentId) {
        try {
            appointmentService.cancelAppointment(appointmentId);
            return "redirect:/appointments?canceled";
        } catch (Exception e) {
            return "redirect:/appointments?error=" + e.getMessage();
        }
    }

    /* ---------------------------
       Optional: Keep REST endpoints
       --------------------------- */

    @RestController
    @RequestMapping("/api/appointments")
    @RequiredArgsConstructor
    static class AppointmentApiController {

        private final AppointmentService appointmentService;

        @GetMapping("/doctor/{doctorId}/slots")
        public ResponseEntity<List<AppointmentSlot>> getAvailableSlots(@PathVariable String doctorId) {
            return ResponseEntity.ok(appointmentService.getAvailableSlots(doctorId));
        }

        @PostMapping("/book")
        public ResponseEntity<Appointment> bookAppointment(
                @RequestParam String patientId,
                @RequestParam String slotId,
                @RequestParam String doctorId) {
            return ResponseEntity.ok(appointmentService.bookAppointment(patientId, slotId, doctorId));
        }

        @PutMapping("/{appointmentId}/cancel")
        public ResponseEntity<Void> cancelAppointment(@PathVariable String appointmentId) {
            appointmentService.cancelAppointment(appointmentId);
            return ResponseEntity.noContent().build();
        }

        @GetMapping("/patient/{patientId}")
        public ResponseEntity<List<Appointment>> getAppointmentsByPatient(@PathVariable String patientId) {
            return ResponseEntity.ok(appointmentService.getAppointmentsByPatient(patientId));
        }
    }

    @Data
    public static class AppointmentViewDto {
        private final String id;
        private final String doctorName;
        private final String status;
        private final java.time.LocalDateTime appointmentTime;
    }
}
