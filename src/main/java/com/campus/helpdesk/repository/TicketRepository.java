package com.campus.helpdesk.repository;

import com.campus.helpdesk.model.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Integer> {

    List<Ticket> findByStudentIdOrderByTicketIdDesc(int studentId);

    List<Ticket> findByFacultyIdOrderByTicketIdDesc(int facultyId);

    List<Ticket> findAllByOrderByTicketIdDesc();

    boolean existsByCategoryId(int categoryId);
}