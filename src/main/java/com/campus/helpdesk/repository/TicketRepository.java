package com.campus.helpdesk.repository;

import com.campus.helpdesk.model.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Integer> {

    List<Ticket> findByStudent_UserIdOrderByTicketIdDesc(int studentId);

    List<Ticket> findByFaculty_FacultyIdOrderByTicketIdDesc(int facultyId);

    List<Ticket> findAllByOrderByTicketIdDesc();

    boolean existsByCategory_CategoryId(int categoryId);
}