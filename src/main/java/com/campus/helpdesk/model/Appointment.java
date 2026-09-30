package com.campus.helpdesk.model;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.io.Serializable;
import java.sql.Date;
import java.sql.Timestamp;

@Entity
@Table(name = "Appointments")
public class Appointment implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "appointment_id")
    private int appointmentId;

    @Column(name = "student_id")
    private int studentId;

    @Column(name = "faculty_id")
    private int facultyId;

    @Column(name = "purpose", columnDefinition = "TEXT")
    private String purpose;

    @Column(name = "status")
    private String status;

    @Column(name = "scheduled_date")
    private Date scheduledDate;

    @Column(name = "scheduled_time")
    private String scheduledTime;

    @Column(name = "staff_notes", columnDefinition = "TEXT")
    private String staffNotes;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Timestamp createdAt;

    @Column(name = "meet_type")
    private String meetType;

    @Transient
    private String studentName;

    @Transient
    private String studentUniId;

    @Transient
    private String facultyName;

    public Appointment() {}

    public int getAppointmentId() { return appointmentId; }
    public void setAppointmentId(int appointmentId) { this.appointmentId = appointmentId; }

    public int getStudentId() { return studentId; }
    public void setStudentId(int studentId) { this.studentId = studentId; }

    public int getFacultyId() { return facultyId; }
    public void setFacultyId(int facultyId) { this.facultyId = facultyId; }

    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }

    public String getStudentUniId() { return studentUniId; }
    public void setStudentUniId(String studentUniId) { this.studentUniId = studentUniId; }

    public String getFacultyName() { return facultyName; }
    public void setFacultyName(String facultyName) { this.facultyName = facultyName; }

    public String getPurpose() { return purpose; }
    public void setPurpose(String purpose) { this.purpose = purpose; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Date getScheduledDate() { return scheduledDate; }
    public void setScheduledDate(Date scheduledDate) { this.scheduledDate = scheduledDate; }

    public String getScheduledTime() { return scheduledTime; }
    public void setScheduledTime(String scheduledTime) { this.scheduledTime = scheduledTime; }

    public String getStaffNotes() { return staffNotes; }
    public void setStaffNotes(String staffNotes) { this.staffNotes = staffNotes; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public String getMeetType() { return meetType; }
    public void setMeetType(String meetType) { this.meetType = meetType; }
}