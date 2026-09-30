package com.campus.helpdesk.controller;

import com.campus.helpdesk.model.Ticket;
import com.campus.helpdesk.model.User;
import com.campus.helpdesk.service.UserService;
import com.campus.helpdesk.service.StaffTicketService;
import com.campus.helpdesk.service.FacultyService;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private UserService userService;

    @Autowired
    private StaffTicketService ticketService;

    @Autowired
    private FacultyService facultyService;

    @GetMapping("/dashboard")
    public String showDashboard(HttpSession session, Model model) {
        User loggedUser = (User) session.getAttribute("loggedUser");

        if (loggedUser == null || (!"ADMIN".equalsIgnoreCase(loggedUser.getRole()) &&
                !"SUPER_ADMIN".equalsIgnoreCase(loggedUser.getRole()))) {
            return "redirect:/login";
        }

        List<User> userList = userService.getAllUsers();
        long studentCount = userList.stream().filter(u -> "STUDENT".equalsIgnoreCase(u.getRole())).count();
        long activeStaffCount = userList.stream().filter(u -> "STAFF".equalsIgnoreCase(u.getRole()) && "ACTIVE".equalsIgnoreCase(u.getAccountStatus())).count();

        List<Ticket> ticketList = ticketService.getAllTickets(0, loggedUser.getRole());
        long pendingTicketCount = ticketList.stream().filter(t -> "OPEN".equalsIgnoreCase(t.getStatus()) || "IN_PROGRESS".equalsIgnoreCase(t.getStatus())).count();
        long criticalIssueCount = ticketList.stream().filter(t -> "CRITICAL".equalsIgnoreCase(t.getPriority()) && !"RESOLVED".equalsIgnoreCase(t.getStatus()) && !"CLOSED".equalsIgnoreCase(t.getStatus())).count();

        model.addAttribute("studentCount", studentCount);
        model.addAttribute("activeStaffCount", activeStaffCount);
        model.addAttribute("pendingTicketCount", pendingTicketCount);
        model.addAttribute("criticalIssueCount", criticalIssueCount);
        model.addAttribute("ticketList", ticketList);
        model.addAttribute("facultyList", facultyService.getAllFaculties());

        return "admin_dashboard";
    }

    @PostMapping("/addFaculty")
    public String addFaculty(@RequestParam("facultyName") String facultyName, HttpSession session) {
        User loggedUser = checkAdminAuth(session);
        if (loggedUser == null) return "redirect:/login";

        boolean success = facultyService.addFaculty(facultyName);
        return success ? "redirect:/admin/dashboard?msg=FacultyAdded" : "redirect:/admin/dashboard?error=FacultyAddFailed";
    }

    @PostMapping("/replyTicket")
    public String adminReply(@RequestParam("ticketId") int ticketId,
                             @RequestParam("message") String message,
                             HttpSession session) {
        User loggedUser = checkAdminAuth(session);
        if (loggedUser == null) return "redirect:/login";

        ticketService.adminReplyTicket(ticketId, message);
        return "redirect:/admin/dashboard";
    }

    private User checkAdminAuth(HttpSession session) {
        User user = (User) session.getAttribute("loggedUser");
        if (user != null && ("ADMIN".equalsIgnoreCase(user.getRole()) || "SUPER_ADMIN".equalsIgnoreCase(user.getRole()))) {
            return user;
        }
        return null;
    }
}
