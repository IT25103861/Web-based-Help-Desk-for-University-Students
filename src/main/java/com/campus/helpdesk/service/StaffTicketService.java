package com.campus.helpdesk.service;

import com.campus.helpdesk.model.Category;
import com.campus.helpdesk.model.Ticket;
import com.campus.helpdesk.model.TicketDocument;
import com.campus.helpdesk.model.User;
import com.campus.helpdesk.repository.CategoryRepository;
import com.campus.helpdesk.repository.TicketDocumentRepository;
import com.campus.helpdesk.repository.TicketRepository;
import com.campus.helpdesk.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class StaffTicketService {

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private UserRepository userRepository;

    // අලුතින් එකතු කළ Document Repository එක
    @Autowired
    private TicketDocumentRepository documentRepository;

    public List<Ticket> getAllTickets(int facultyId, String role) {
        List<Ticket> tickets;

        if ("STAFF".equalsIgnoreCase(role) && facultyId > 0) {
            tickets = ticketRepository.findByFacultyIdOrderByTicketIdDesc(facultyId);
        } else {
            tickets = ticketRepository.findAllByOrderByTicketIdDesc(); // Admin සඳහා
        }

        for (Ticket t : tickets) {
            categoryRepository.findById(t.getCategoryId()).ifPresent(c -> t.setCategoryName(c.getCategoryName()));
            userRepository.findById(t.getStudentId()).ifPresent(u -> t.setStudentUniversityId(u.getUniversityId()));

            // --- Document එකක් තියෙනවද කියලා හොයලා ID එක සෙට් කරන කෑල්ල ---
            TicketDocument doc = documentRepository.findByTicketId(t.getTicketId());
            if (doc != null) {
                // සටහන: ඔයාගේ TicketDocument මොඩල් එකේ ID එක ගන්න Method එක getId() නම් මේක හරි.
                // වෙනස් නම් (උදා: getDocumentId() වගේ නම්) ඒක මෙතන මාරු කරගන්න.
                t.setAttachedDocId(doc.getDocumentId());
            }

            long diffHours = (System.currentTimeMillis() - t.getCreatedAt().getTime()) / (1000 * 60 * 60);
            boolean isResolved = "RESOLVED".equals(t.getStatus()) || "CLOSED".equals(t.getStatus());
            t.setCanEscalate(diffHours >= 24 && !t.isEscalated() && !isResolved);
        }
        return tickets;
    }

    public boolean updateTicketStatus(int ticketId, String newStatus, int staffId) {
        Optional<Ticket> opt = ticketRepository.findById(ticketId);
        if (opt.isPresent()) {
            Ticket t = opt.get();
            t.setStatus(newStatus);
            t.setAssignedStaffId(staffId);
            ticketRepository.save(t);
            return true;
        }
        return false;
    }

    public boolean replyAndResolveTicket(int ticketId, String newStatus, int staffId, String responseMessage) {
        Optional<Ticket> opt = ticketRepository.findById(ticketId);
        if (opt.isPresent()) {
            Ticket t = opt.get();
            String formattedMessage = "[ " + new java.sql.Timestamp(System.currentTimeMillis()) + " ] : " + responseMessage + "\n---SEPARATOR---\n";
            t.setStatus(newStatus);
            t.setAssignedStaffId(staffId);
            t.setStaffResponse((t.getStaffResponse() == null ? "" : t.getStaffResponse()) + formattedMessage);
            ticketRepository.save(t);
            return true;
        }
        return false;
    }

    public boolean adminReplyTicket(int ticketId, String message) {
        Optional<Ticket> opt = ticketRepository.findById(ticketId);
        if (opt.isPresent()) {
            Ticket t = opt.get();
            t.setAdminResponse(message);
            t.setStatus("RESOLVED");
            ticketRepository.save(t);
            return true;
        }
        return false;
    }

    public boolean deleteTicketByStaff(int ticketId) {
        Optional<Ticket> opt = ticketRepository.findById(ticketId);
        if (opt.isPresent()) {
            Ticket t = opt.get();

            // --- අලුත් Security Check එක ---
            // "OPEN" තත්වයේ නැත්නම් Delete වෙන්නෙ නෑ, කෙලින්ම False යවනවා.
            if (!"OPEN".equalsIgnoreCase(t.getStatus())) {
                return false;
            }

            ticketRepository.deleteById(ticketId);
            return true;
        }
        return false;
    }
}