package com.campus.helpdesk.service;

import com.campus.helpdesk.model.Appointment;
import com.campus.helpdesk.repository.AppointmentRepository;
import com.campus.helpdesk.repository.FacultyRepository;
import com.campus.helpdesk.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Date;
import java.util.List;
import java.util.Optional;

@Service
public class AppointmentService {

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private FacultyRepository facultyRepository;

    @Autowired
    private UserRepository userRepository;

    public boolean requestAppointment(Appointment app) {
        app.setStatus("PENDING");
        appointmentRepository.save(app);
        return true;
    }

    public boolean deleteRequest(int appointmentId, int studentId) {
        Optional<Appointment> opt = appointmentRepository.findById(appointmentId);
        if (opt.isPresent() && opt.get().getStudentId() == studentId && "PENDING".equals(opt.get().getStatus())) {
            appointmentRepository.deleteById(appointmentId);
            return true;
        }
        return false;
    }

    public List<Appointment> getAppointmentsByStudent(int studentId) {
        List<Appointment> list = appointmentRepository.findByStudentIdOrderByAppointmentIdDesc(studentId);
        for (Appointment app : list) {
            facultyRepository.findById(app.getFacultyId()).ifPresent(f -> app.setFacultyName(f.getFacultyName()));
        }
        return list;
    }

    public List<Appointment> getAllAppointmentsForStaff(int facultyId, String role) {
        List<Appointment> list;
        if ("STAFF".equalsIgnoreCase(role) && facultyId > 0) {
            list = appointmentRepository.findByFacultyIdOrderByAppointmentIdDesc(facultyId);
        } else {
            list = appointmentRepository.findAllByOrderByAppointmentIdDesc();
        }

        for (Appointment app : list) {
            userRepository.findById(app.getStudentId()).ifPresent(u -> {
                app.setStudentName(u.getFullName());
                app.setStudentUniId(u.getUniversityId());
            });
            facultyRepository.findById(app.getFacultyId()).ifPresent(f -> app.setFacultyName(f.getFacultyName()));
        }
        return list;
    }

    public boolean markAsProcessing(int appointmentId) {
        Optional<Appointment> opt = appointmentRepository.findById(appointmentId);
        if (opt.isPresent()) {
            Appointment app = opt.get();
            app.setStatus("PROCESSING");
            appointmentRepository.save(app);
            return true;
        }
        return false;
    }

    public boolean approveAppointment(int appointmentId, Date date, String time, String notes) {
        Optional<Appointment> opt = appointmentRepository.findById(appointmentId);
        if (opt.isPresent()) {
            Appointment app = opt.get();
            app.setStatus("APPROVED");
            app.setScheduledDate(date);
            app.setScheduledTime(time);
            app.setStaffNotes(notes);
            appointmentRepository.save(app);
            return true;
        }
        return false;
    }

    public boolean requestReschedule(int appointmentId, int studentId) {
        Optional<Appointment> opt = appointmentRepository.findById(appointmentId);
        if (opt.isPresent() && opt.get().getStudentId() == studentId && "APPROVED".equals(opt.get().getStatus())) {
            Appointment app = opt.get();
            app.setStatus("RESCHEDULE_REQUESTED");
            appointmentRepository.save(app);
            return true;
        }
        return false;
    }
}