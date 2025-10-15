package com.example.health_care_system.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.beans.factory.annotation.Autowired;
import com.example.health_care_system.service.UserService;
import com.example.health_care_system.model.User;
import jakarta.servlet.http.HttpSession;

@Controller
public class HomeController {

    @Autowired
    private UserService userService;

    @GetMapping("/")
    public String home(HttpSession session) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        User user = userService.findByUsername(username);
        if (user != null) {
            session.setAttribute("loggedInUser", true);
            session.setAttribute("username", user.getUsername());
            session.setAttribute("fullName", user.getFirstName() + " " + user.getLastName());
        } else {
            session.removeAttribute("loggedInUser");
            session.removeAttribute("username");
            session.removeAttribute("fullName");
        }
        return "home"; // This will render the home.html template
    }
}