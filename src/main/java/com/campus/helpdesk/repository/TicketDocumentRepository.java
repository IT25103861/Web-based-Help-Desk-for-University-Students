package com.campus.helpdesk.repository;

import com.campus.helpdesk.model.TicketDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TicketDocumentRepository extends JpaRepository<TicketDocument, Integer> {
    TicketDocument findByTicket_TicketId(int ticketId);
}