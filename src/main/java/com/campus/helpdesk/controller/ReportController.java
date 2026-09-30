package com.campus.helpdesk.controller; // ඔයාගේ package නම හරියටම දාගන්න

import com.campus.helpdesk.model.Ticket;
import com.campus.helpdesk.model.User;
import com.campus.helpdesk.repository.UserRepository;
import com.campus.helpdesk.service.StaffTicketService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin/reports")
public class ReportController {

    @Autowired
    private StaffTicketService ticketService;

    @Autowired
    private UserRepository userRepository;

    @GetMapping
    public String showReports(HttpSession session, Model model) {
        User loggedUser = (User) session.getAttribute("loggedUser");

        if (loggedUser == null || (!"ADMIN".equalsIgnoreCase(loggedUser.getRole()) &&
                !"SUPER_ADMIN".equalsIgnoreCase(loggedUser.getRole()))) {
            return "redirect:/login";
        }

        int facultyId = (loggedUser.getFacultyId() != null) ? loggedUser.getFacultyId() : 0;
        List<Ticket> ticketList = ticketService.getAllTickets(facultyId, loggedUser.getRole());

        int openCount = 0, progressCount = 0, resolvedCount = 0, closedCount = 0;
        Map<String, Integer> categoryCounts = new HashMap<>();

        long totalResponseHours = 0;
        int completedTicketsCount = 0;

        Map<Integer, String> staffNames = new HashMap<>();

        for (Ticket t : ticketList) {
            String status = t.getStatus();
            if ("OPEN".equalsIgnoreCase(status)) openCount++;
            else if ("IN_PROGRESS".equalsIgnoreCase(status)) progressCount++;
            else if ("RESOLVED".equalsIgnoreCase(status)) resolvedCount++;
            else if ("CLOSED".equalsIgnoreCase(status)) closedCount++;

            if (("RESOLVED".equalsIgnoreCase(status) || "CLOSED".equalsIgnoreCase(status))
                    && t.getCreatedAt() != null && t.getUpdatedAt() != null) {
                long diffInMillies = Math.abs(t.getUpdatedAt().getTime() - t.getCreatedAt().getTime());
                long diffInHours = diffInMillies / (60 * 60 * 1000);
                totalResponseHours += diffInHours;
                completedTicketsCount++;
            }

            String catName = t.getCategoryName() != null ? t.getCategoryName() : "Uncategorized";
            categoryCounts.put(catName, categoryCounts.getOrDefault(catName, 0) + 1);

            if (t.getAssignedStaffId() != null && !staffNames.containsKey(t.getAssignedStaffId())) {
                userRepository.findById(t.getAssignedStaffId()).ifPresent(u -> staffNames.put(u.getUserId(), u.getFullName()));
            }
        }

        int totalTickets = ticketList.size();
        int completedTickets = resolvedCount + closedCount;
        int resolutionRate = totalTickets > 0 ? (int) Math.round(((double) completedTickets / totalTickets) * 100) : 0;

        int avgResponseTime = completedTicketsCount > 0 ? (int) (totalResponseHours / completedTicketsCount) : 0;

        StringBuilder catLabels = new StringBuilder("[");
        StringBuilder catData = new StringBuilder("[");
        int i = 0;
        for (Map.Entry<String, Integer> entry : categoryCounts.entrySet()) {
            catLabels.append("'").append(entry.getKey()).append("'");
            catData.append(entry.getValue());
            if (i < categoryCounts.size() - 1) {
                catLabels.append(", ");
                catData.append(", ");
            }
            i++;
        }
        catLabels.append("]");
        catData.append("]");

        model.addAttribute("openCount", openCount);
        model.addAttribute("progressCount", progressCount);
        model.addAttribute("resolvedCount", resolvedCount);
        model.addAttribute("closedCount", closedCount);
        model.addAttribute("resolutionRate", resolutionRate);
        model.addAttribute("avgResponseTime", avgResponseTime);
        model.addAttribute("catLabels", catLabels.toString());
        model.addAttribute("catData", catData.toString());

        model.addAttribute("ticketList", ticketList);
        model.addAttribute("staffNames", staffNames);

        return "admin_reports";
    }
}