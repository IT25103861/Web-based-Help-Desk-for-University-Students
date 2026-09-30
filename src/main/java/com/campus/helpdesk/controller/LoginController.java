package com.campus.helpdesk.controller;

import com.campus.helpdesk.model.User;
import com.campus.helpdesk.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class LoginController {

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/")
    public String showHomePage() {
        return "index";
    }

    @GetMapping("/login")
    public String showLoginPage() {
        return "login";
    }

    @PostMapping("/login")
    public String processLogin(@RequestParam("email") String email,
                               @RequestParam("password") String password,
                               HttpSession session,
                               Model model) {

        User user = userRepository.findByEmail(email);

        if (user != null && user.getPasswordHash().equals(password)) {

            if ("INACTIVE".equalsIgnoreCase(user.getAccountStatus())) {
                model.addAttribute("errorMessage", "Your account has been deactivated. Please contact Super Admin.");
                return "login";
            }

            session.setAttribute("loggedUser", user);

            String role = user.getRole();
            if ("STUDENT".equalsIgnoreCase(role)) {
                return "redirect:/student/ticket/dashboard";
            } else if ("STAFF".equalsIgnoreCase(role)) {
                return "redirect:/staff/dashboard";
            } else if ("ADMIN".equalsIgnoreCase(role) || "SUPER_ADMIN".equalsIgnoreCase(role)) {
                return "redirect:/admin/dashboard";
            } else {
                return "redirect:/";
            }
        } else {
            model.addAttribute("errorMessage", "Invalid Email or Password! Please try again.");
            return "login";
        }
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        if (session != null) {
            session.invalidate();
        }
        return "redirect:/";
    }
}