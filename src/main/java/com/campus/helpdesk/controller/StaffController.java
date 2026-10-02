package com.campus.helpdesk.controller;

import com.campus.helpdesk.model.Ticket;
import com.campus.helpdesk.model.User;
import com.campus.helpdesk.service.StaffTicketService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/staff")
public class StaffController {

    @Autowired
    private StaffTicketService ticketService;

    @GetMapping("/dashboard")
    public String showDashboard(HttpSession session, Model model) {
        User loggedUser = (User) session.getAttribute("loggedUser");
        if (loggedUser == null || (!"STAFF".equalsIgnoreCase(loggedUser.getRole()) && !"ADMIN".equalsIgnoreCase(loggedUser.getRole()) && !"SUPER_ADMIN".equalsIgnoreCase(loggedUser.getRole()))) {
            return "redirect:/login";
        }

        List<Ticket> ticketList = ticketService.getAllTickets(loggedUser.getFaculty().getFacultyId(), loggedUser.getRole());

        long openCount = ticketList.stream().filter(t -> "OPEN".equalsIgnoreCase(t.getStatus())).count();
        long progressCount = ticketList.stream().filter(t -> "IN_PROGRESS".equalsIgnoreCase(t.getStatus())).count();
        long resolvedCount = ticketList.stream().filter(t -> "RESOLVED".equalsIgnoreCase(t.getStatus()) || "CLOSED".equalsIgnoreCase(t.getStatus())).count();
        long criticalCount = ticketList.stream().filter(t -> "CRITICAL".equalsIgnoreCase(t.getPriority()) && !"RESOLVED".equalsIgnoreCase(t.getStatus())).count();

        model.addAttribute("ticketList", ticketList);
        model.addAttribute("openCount", openCount);
        model.addAttribute("progressCount", progressCount);
        model.addAttribute("resolvedCount", resolvedCount);
        model.addAttribute("criticalCount", criticalCount);

        return "staff_dashboard";
    }

    @GetMapping("/updateStatus")
    public String updateStatus(@RequestParam("id") int ticketId, @RequestParam("status") String status, HttpSession session) {
        User loggedUser = (User) session.getAttribute("loggedUser");
        if (loggedUser == null) return "redirect:/login";

        ticketService.updateTicketStatus(ticketId, status, loggedUser.getUserId());
        return "redirect:/staff/dashboard";
    }

    @GetMapping("/deleteSpam")
    public String deleteSpam(@RequestParam("id") int ticketId, HttpSession session) {
        if (session.getAttribute("loggedUser") == null) return "redirect:/login";

        // --- අලුතින් එකතු කළ Security Check එක ---
        // Ticket එක Database එකෙන් අරන් ඒක "OPEN" ද කියලා බලනවා.
        // (ඔයාගේ Service එකේ මේ Method එකේ නම වෙනස් නම් ඒකට අදාළව වෙනස් කරගන්න)

        /*
        Ticket ticket = ticketService.getTicketById(ticketId);
        if (ticket != null && !"OPEN".equalsIgnoreCase(ticket.getStatus())) {
            // OPEN නෙවෙයි නම් Delete වෙන්නෙ නෑ, කෙලින්ම Dashboard එකට හරවලා යවනවා
            return "redirect:/staff/dashboard";
        }
        */

        ticketService.deleteTicketByStaff(ticketId);
        return "redirect:/staff/dashboard";
    }

    @PostMapping("/replyTicket")
    public String replyTicket(@RequestParam("ticketId") int ticketId, @RequestParam("status") String status, @RequestParam("message") String message, HttpSession session) {
        User loggedUser = (User) session.getAttribute("loggedUser");
        if (loggedUser == null) return "redirect:/login";

        ticketService.replyAndResolveTicket(ticketId, status, loggedUser.getUserId(), message);
        return "redirect:/staff/dashboard";
    }
}