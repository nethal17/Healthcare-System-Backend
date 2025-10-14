package com.example.health_care_system.controller;

import com.example.health_care_system.dto.ChangePasswordDto;
import com.example.health_care_system.dto.ForgotPasswordDto;
import com.example.health_care_system.dto.LoginDto;
import com.example.health_care_system.dto.ResetPasswordDto;
import com.example.health_care_system.dto.UserProfileDto;
import com.example.health_care_system.dto.UserRegistrationDto;
import com.example.health_care_system.model.PasswordResetToken;
import com.example.health_care_system.model.User;
import com.example.health_care_system.service.UserService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import jakarta.servlet.http.HttpServletRequest;

@Controller
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("user", new UserRegistrationDto());
        return "register";
    }

    @PostMapping("/register")
    public String registerUser(@Valid @ModelAttribute("user") UserRegistrationDto registrationDto,
                               BindingResult result,
                               Model model,
                               RedirectAttributes redirectAttributes) {

        // Check for validation errors
        if (result.hasErrors()) {
            return "register";
        }

        try {
            User user = userService.registerUser(registrationDto);
            redirectAttributes.addFlashAttribute("success",
                    "Registration successful! Welcome " + user.getFirstName() + "!");
            return "redirect:/register?success";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "register";
        }
    }

    @GetMapping("/login")
    public String showLoginForm(Model model, HttpSession session) {
        // Check if user is already logged in
        if (session.getAttribute("loggedInUser") != null) {
            return "redirect:/home";
        }

        model.addAttribute("loginUser", new LoginDto());
        return "login";
    }

    @PostMapping("/login")
    public String loginUser(@Valid @ModelAttribute("loginUser") LoginDto loginDto,
                            BindingResult result,
                            Model model,
                            HttpSession session,
                            RedirectAttributes redirectAttributes) {

        // Check for validation errors
        if (result.hasErrors()) {
            return "login";
        }

        try {
            User user = userService.authenticateUser(loginDto);

            // Store user in session
            session.setAttribute("loggedInUser", user);
            session.setAttribute("username", user.getUsername());
            session.setAttribute("fullName", user.getFirstName() + " " + user.getLastName());

            redirectAttributes.addFlashAttribute("success",
                    "Welcome back, " + user.getFirstName() + "!");
            return "redirect:/home";

        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "login";
        }
    }

    @GetMapping("/logout")
    public String logout(HttpSession session, RedirectAttributes redirectAttributes) {
        session.invalidate();
        redirectAttributes.addFlashAttribute("success", "You have been logged out successfully!");
        return "redirect:/login";
    }

    @GetMapping("/home")
    public String home(HttpSession session, Model model) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null) {
            return "redirect:/login";
        }

        model.addAttribute("user", loggedInUser);
        return "home";
    }

    @GetMapping("/users")
    public String listUsers(Model model) {
        // This is optional - to list all users
        return "users";
    }

    @GetMapping("/forgot-password")
    public String showForgotPasswordForm(Model model) {
        model.addAttribute("forgotPassword", new ForgotPasswordDto());
        return "forgot-password";
    }

    @PostMapping("/forgot-password")
    public String processForgotPassword(@Valid @ModelAttribute("forgotPassword") ForgotPasswordDto forgotPasswordDto,
                                        BindingResult result,
                                        Model model,
                                        RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {
            return "forgot-password";
        }

        try {
            userService.initiatePasswordReset(forgotPasswordDto.getEmail());
            redirectAttributes.addFlashAttribute("success",
                    "Password reset instructions have been sent to your email address.");
            return "redirect:/forgot-password?success";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "forgot-password";
        }
    }

    @GetMapping("/reset-password")
    public String showResetPasswordForm(@RequestParam("token") String token, Model model,
                                        RedirectAttributes redirectAttributes) {

        try {
            // Validate the reset token
            PasswordResetToken resetToken = userService.validateResetToken(token);

            ResetPasswordDto resetPasswordDto = new ResetPasswordDto();
            resetPasswordDto.setToken(token);

            model.addAttribute("resetPassword", resetPasswordDto);
            model.addAttribute("email", resetToken.getEmail());
            return "reset-password";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/login";
        }
    }

    @PostMapping("/reset-password")
    public String processResetPassword(@Valid @ModelAttribute("resetPassword") ResetPasswordDto resetPasswordDto,
                                       BindingResult result,
                                       Model model,
                                       RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {
            try {
                PasswordResetToken resetToken = userService.validateResetToken(resetPasswordDto.getToken());
                model.addAttribute("email", resetToken.getEmail());
            } catch (Exception e) {
                redirectAttributes.addFlashAttribute("error", "Invalid or expired token.");
                return "redirect:/login";
            }
            return "reset-password";
        }

        if (!resetPasswordDto.isPasswordMatch()) {
            model.addAttribute("error", "Passwords do not match");
            try {
                PasswordResetToken resetToken = userService.validateResetToken(resetPasswordDto.getToken());
                model.addAttribute("email", resetToken.getEmail());
            } catch (Exception e) {
                redirectAttributes.addFlashAttribute("error", "Invalid or expired token.");
                return "redirect:/login";
            }
            return "reset-password";
        }

        try {
            userService.resetPassword(resetPasswordDto.getToken(), resetPasswordDto.getNewPassword());
            redirectAttributes.addFlashAttribute("success",
                    "Your password has been successfully reset! Please log in with your new password.");
            return "redirect:/login";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            try {
                PasswordResetToken resetToken = userService.validateResetToken(resetPasswordDto.getToken());
                model.addAttribute("email", resetToken.getEmail());
            } catch (Exception ex) {
                redirectAttributes.addFlashAttribute("error", "Invalid or expired token.");
                return "redirect:/login";
            }
            return "reset-password";
        }
    }

    @GetMapping("/profile")
    public String showProfile(Model model, HttpSession session, RedirectAttributes redirectAttributes) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null) {
            redirectAttributes.addFlashAttribute("error", "Please log in to access your profile.");
            return "redirect:/login";
        }

        try {
            UserProfileDto profileDto = userService.getUserProfile(loggedInUser.getId());
            model.addAttribute("userProfile", profileDto);
            model.addAttribute("user", loggedInUser);
            return "profile";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error loading profile: " + e.getMessage());
            return "redirect:/home";
        }
    }

    @PostMapping("/profile")
    public String updateProfile(@Valid @ModelAttribute("userProfile") UserProfileDto profileDto,
                                BindingResult result,
                                Model model,
                                HttpSession session,
                                RedirectAttributes redirectAttributes) {

        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null) {
            redirectAttributes.addFlashAttribute("error", "Please log in to access your profile.");
            return "redirect:/login";
        }

        if (result.hasErrors()) {
            model.addAttribute("user", loggedInUser);
            return "profile";
        }

        try {
            User updatedUser = userService.updateUserProfile(loggedInUser.getId(), profileDto);

            // Update session with new user data
            session.setAttribute("loggedInUser", updatedUser);
            session.setAttribute("username", updatedUser.getUsername());
            session.setAttribute("fullName", updatedUser.getFirstName() + " " + updatedUser.getLastName());

            redirectAttributes.addFlashAttribute("success", "Profile updated successfully!");
            return "redirect:/profile";

        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("user", loggedInUser);
            return "profile";
        }
    }

    @GetMapping("/change-password")
    public String showChangePasswordForm(Model model, HttpSession session, RedirectAttributes redirectAttributes) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null) {
            redirectAttributes.addFlashAttribute("error", "Please log in to change your password.");
            return "redirect:/login";
        }

        model.addAttribute("changePassword", new ChangePasswordDto());
        model.addAttribute("user", loggedInUser);
        return "change-password";
    }

    @PostMapping("/change-password")
    public String changePassword(@Valid @ModelAttribute("changePassword") ChangePasswordDto changePasswordDto,
                                 BindingResult result,
                                 Model model,
                                 HttpSession session,
                                 RedirectAttributes redirectAttributes,
                                 HttpServletRequest request) {

        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null) {
            redirectAttributes.addFlashAttribute("error", "Please log in to change your password.");
            return "redirect:/login";
        }

        boolean isAjax = "XMLHttpRequest".equals(request.getHeader("X-Requested-With"));

        if (result.hasErrors()) {
            if (isAjax) {
                model.addAttribute("error", "Please fill in all required fields correctly.");
                model.addAttribute("user", loggedInUser);
                return "change-password";
            } else {
                model.addAttribute("user", loggedInUser);
                return "change-password";
            }
        }

        if (!changePasswordDto.isPasswordMatch()) {
            String errorMsg = "New passwords do not match";
            if (isAjax) {
                model.addAttribute("error", errorMsg);
                model.addAttribute("user", loggedInUser);
                return "change-password";
            } else {
                model.addAttribute("error", errorMsg);
                model.addAttribute("user", loggedInUser);
                return "change-password";
            }
        }

        try {
            userService.changePassword(loggedInUser.getId(), changePasswordDto);

            if (isAjax) {
                redirectAttributes.addFlashAttribute("success", "Password changed successfully!");
                return "redirect:/profile?success";
            } else {
                redirectAttributes.addFlashAttribute("success", "Password changed successfully!");
                return "redirect:/profile";
            }

        } catch (Exception e) {
            if (isAjax) {
                model.addAttribute("error", e.getMessage());
                model.addAttribute("user", loggedInUser);
                return "change-password";
            } else {
                model.addAttribute("error", e.getMessage());
                model.addAttribute("user", loggedInUser);
                return "change-password";
            }
        }
    }
}