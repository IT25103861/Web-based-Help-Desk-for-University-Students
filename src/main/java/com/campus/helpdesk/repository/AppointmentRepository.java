package com.campus.helpdesk.repository;

import com.campus.helpdesk.model.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Integer> {

    List<Appointment> findByStudent_UserIdOrderByAppointmentIdDesc(int studentId);

    List<Appointment> findByFaculty_FacultyIdOrderByAppointmentIdDesc(int facultyId);

    List<Appointment> findAllByOrderByAppointmentIdDesc();
}
