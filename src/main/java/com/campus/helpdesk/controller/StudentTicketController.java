package com.campus.helpdesk.controller;

import com.campus.helpdesk.model.Ticket;
import com.campus.helpdesk.model.TicketDocument;
import com.campus.helpdesk.model.User;
import com.campus.helpdesk.repository.TicketDocumentRepository;
import com.campus.helpdesk.service.CategoryService;
import com.campus.helpdesk.service.FacultyService;
import com.campus.helpdesk.service.StudentTicketService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Controller
@RequestMapping("/student/ticket")
public class StudentTicketController {

    @Autowired
    private StudentTicketService ticketService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private FacultyService facultyService;

    @Autowired
    private TicketDocumentRepository documentRepository;

    @GetMapping("/{action}")
    public String showTickets(@PathVariable("action") String action,
                              @RequestParam(value = "id", required = false) Integer id,
                              HttpSession session, Model model) {
        User loggedUser = (User) session.getAttribute("loggedUser");
        if (loggedUser == null || !"STUDENT".equalsIgnoreCase(loggedUser.getRole())) return "redirect:/login";

        if ("delete".equalsIgnoreCase(action) && id != null) {
            ticketService.deleteTicket(id, loggedUser.getUserId());
            return "redirect:/student/ticket/myTickets";
        }
        if ("escalate".equalsIgnoreCase(action) && id != null) {
            ticketService.escalateTicket(id);
            return "redirect:/student/ticket/myTickets";
        }

        model.addAttribute("ticketList", ticketService.getTicketsByStudent(loggedUser.getUserId()));
        model.addAttribute("categoryList", categoryService.getApprovedCategories());
        model.addAttribute("facultyList", facultyService.getAllFaculties());

        return "myTickets".equalsIgnoreCase(action) ? "my_tickets" : "student_dashboard";
    }

    @PostMapping("/add")
    public String addTicket(@ModelAttribute Ticket ticket,
                            @RequestParam(value = "file", required = false) MultipartFile file,
                            HttpSession session) {
        User loggedUser = (User) session.getAttribute("loggedUser");
        if (loggedUser == null) return "redirect:/login";

        ticket.setStudentId(loggedUser.getUserId());
        ticketService.addTicket(ticket);

        try {
            if (file != null && !file.isEmpty()) {
                TicketDocument doc = new TicketDocument();
                doc.setTicketId(ticket.getTicketId());
                doc.setUserId(loggedUser.getUserId());
                doc.setFileName(file.getOriginalFilename());
                doc.setFileType(file.getContentType());
                doc.setFileData(file.getBytes());

                documentRepository.save(doc);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return "redirect:/student/ticket/myTickets";
    }

    @PostMapping("/edit")
    public String editTicket(@ModelAttribute Ticket ticket,
                             @RequestParam(value = "file", required = false) MultipartFile file, // ෆයිල් එක අල්ලගන්න අලුතින් දැම්මා
                             @RequestParam("redirectPage") String redirectPage,
                             HttpSession session) {

        User loggedUser = (User) session.getAttribute("loggedUser");
        if (loggedUser == null) return "redirect:/login";

        ticket.setStudentId(loggedUser.getUserId());
        ticketService.updateTicket(ticket);

        try {
            if (file != null && !file.isEmpty()) {
                TicketDocument doc = new TicketDocument();
                doc.setTicketId(ticket.getTicketId());
                doc.setUserId(loggedUser.getUserId());
                doc.setFileName(file.getOriginalFilename());
                doc.setFileType(file.getContentType());
                doc.setFileData(file.getBytes());

                documentRepository.save(doc);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return "myTickets".equals(redirectPage) ? "redirect:/student/ticket/myTickets" : "redirect:/student/ticket/dashboard";
    }
}