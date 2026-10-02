package com.campus.helpdesk.service;

import com.campus.helpdesk.model.Category;
import com.campus.helpdesk.model.Ticket;
import com.campus.helpdesk.repository.CategoryRepository;
import com.campus.helpdesk.repository.TicketRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class StudentTicketService {

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    public List<Ticket> getTicketsByStudent(int studentId) {
        List<Ticket> tickets = ticketRepository.findByStudent_UserIdOrderByTicketIdDesc(studentId);

        for (Ticket t : tickets) {
            Optional<Category> optCat = categoryRepository.findById(t.getCategory().getCategoryId());

            int slaHours = 72;
            if (optCat.isPresent()) {
                t.setCategoryName(optCat.get().getCategoryName());
                if (optCat.get().getSlaHours() != null) {
                    slaHours = optCat.get().getSlaHours();
                }
            }


            long diffHours = (System.currentTimeMillis() - t.getCreatedAt().getTime()) / (1000 * 60 * 60);
            boolean isResolved = "RESOLVED".equals(t.getStatus()) || "CLOSED".equals(t.getStatus());

            t.setCanEscalate(diffHours >= slaHours && !t.isEscalated() && !isResolved);
        }
        return tickets;
    }

    public boolean addTicket(Ticket ticket) {
        ticket.setStatus("OPEN");
        ticketRepository.save(ticket);
        return true;
    }

    public boolean deleteTicket(int ticketId, int studentId) {
        Optional<Ticket> opt = ticketRepository.findById(ticketId);
        if (opt.isPresent() && opt.get().getStudent().getUserId() == studentId && "OPEN".equals(opt.get().getStatus())) {
            ticketRepository.deleteById(ticketId);
            return true;
        }
        return false;
    }

    public boolean escalateTicket(int ticketId) {
        Optional<Ticket> opt = ticketRepository.findById(ticketId);
        if (opt.isPresent()) {
            Ticket t = opt.get();
            t.setEscalated(true);
            t.setPriority("CRITICAL");
            ticketRepository.save(t);
            return true;
        }
        return false;
    }

    public boolean updateTicket(Ticket ticket) {
        Optional<Ticket> opt = ticketRepository.findById(ticket.getTicketId());
        if (opt.isPresent() && opt.get().getStudent().getUserId() == ticket.getStudent().getUserId() && "OPEN".equals(opt.get().getStatus())) {
            Ticket existing = opt.get();
            existing.setTitle(ticket.getTitle());
            existing.setDescription(ticket.getDescription());
            existing.setPriority(ticket.getPriority());
            existing.setCategory(ticket.getCategory());
            existing.setFaculty(ticket.getFaculty());
            ticketRepository.save(existing);
            return true;
        }
        return false;
    }
}
