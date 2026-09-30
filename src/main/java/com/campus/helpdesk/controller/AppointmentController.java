package com.campus.helpdesk.controller;

import com.campus.helpdesk.model.Appointment;
import com.campus.helpdesk.model.User;
import com.campus.helpdesk.service.AppointmentService;
import com.campus.helpdesk.service.FacultyService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.sql.Date;

@Controller
@RequestMapping("/appointment")
public class AppointmentController {

    @Autowired
    private AppointmentService appointmentService;

    @Autowired
    private FacultyService facultyService;

    @GetMapping("/myAppointments")
    public String myAppointments(HttpSession session, Model model) {
        User loggedUser = (User) session.getAttribute("loggedUser");
        if (loggedUser == null || !"STUDENT".equalsIgnoreCase(loggedUser.getRole())) {
            return "redirect:/login";
        }

        model.addAttribute("appList", appointmentService.getAppointmentsByStudent(loggedUser.getUserId()));
        model.addAttribute("facultyList", facultyService.getAllFaculties());

        return "student_appointments";
    }

    @GetMapping("/staffList")
    public String staffList(HttpSession session, Model model) {
        User loggedUser = (User) session.getAttribute("loggedUser");
        if (loggedUser == null || (!"STAFF".equalsIgnoreCase(loggedUser.getRole()) && !"ADMIN".equalsIgnoreCase(loggedUser.getRole()))) {
            return "redirect:/login";
        }

        model.addAttribute("appList", appointmentService.getAllAppointmentsForStaff(loggedUser.getFacultyId(), loggedUser.getRole()));
        return "staff_appointments";
    }

    @GetMapping("/delete")
    public String deleteRequest(@RequestParam("id") int appId, HttpSession session) {
        User loggedUser = (User) session.getAttribute("loggedUser");
        if (loggedUser != null && "STUDENT".equalsIgnoreCase(loggedUser.getRole())) {
            appointmentService.deleteRequest(appId, loggedUser.getUserId());
        }
        return "redirect:/appointment/myAppointments";
    }

    @GetMapping("/markProcessing")
    public String markProcessing(@RequestParam("id") int appId, HttpSession session) {
        User loggedUser = (User) session.getAttribute("loggedUser");
        if (loggedUser != null && "STAFF".equalsIgnoreCase(loggedUser.getRole())) {
            appointmentService.markAsProcessing(appId);
        }
        return "redirect:/appointment/staffList";
    }

    @GetMapping("/requestReschedule")
    public String requestReschedule(@RequestParam("id") int appId, HttpSession session) {
        User loggedUser = (User) session.getAttribute("loggedUser");
        if (loggedUser != null && "STUDENT".equalsIgnoreCase(loggedUser.getRole())) {
            appointmentService.requestReschedule(appId, loggedUser.getUserId());
        }
        return "redirect:/appointment/myAppointments";
    }

    @PostMapping("/request")
    public String requestAppointment(@ModelAttribute Appointment appointment, HttpSession session) {
        User loggedUser = (User) session.getAttribute("loggedUser");
        if (loggedUser == null || !"STUDENT".equalsIgnoreCase(loggedUser.getRole())) {
            return "redirect:/login";
        }

        appointment.setStudentId(loggedUser.getUserId());
        appointmentService.requestAppointment(appointment);
        return "redirect:/appointment/myAppointments";
    }

    @PostMapping("/approve")
    public String approveAppointment(@RequestParam("appointmentId") int appId,
                                     @RequestParam("scheduledDate") Date scheduledDate,
                                     @RequestParam("scheduledTime") String scheduledTime,
                                     @RequestParam("staffNotes") String staffNotes,
                                     HttpSession session) {
        User loggedUser = (User) session.getAttribute("loggedUser");
        if (loggedUser != null && "STAFF".equalsIgnoreCase(loggedUser.getRole())) {
            appointmentService.approveAppointment(appId, scheduledDate, scheduledTime, staffNotes);
        }
        return "redirect:/appointment/staffList";
    }
}
