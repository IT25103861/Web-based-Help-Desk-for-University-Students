package com.campus.helpdesk.repository;

import com.campus.helpdesk.model.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Integer> {

    List<Appointment> findByStudentIdOrderByAppointmentIdDesc(int studentId);

    List<Appointment> findByFacultyIdOrderByAppointmentIdDesc(int facultyId);

    List<Appointment> findAllByOrderByAppointmentIdDesc();
}
