package com.campus.helpdesk.model;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.io.Serializable;
import java.sql.Timestamp;

@Entity
@Table(name = "Tickets")
public class Ticket implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ticket_id")
    private int ticketId;

    @Column(name = "title", unique = true, nullable = false)
    private String title;
    private String description;

    private String status;
    private String priority;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id")
    private User student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_staff_id")
    private User assignedStaff;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "faculty_id")
    private Faculty faculty;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Timestamp createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Timestamp updatedAt;

    @Column(name = "staff_response", columnDefinition = "TEXT")
    private String staffResponse;

    @Column(name = "is_escalated")
    private boolean isEscalated;

    @Column(name = "admin_response", columnDefinition = "TEXT")
    private String adminResponse;

    @Column(name = "university_id")
    private String studentUniversityId;

    @Transient
    private String categoryName;

    @Transient
    private boolean canEscalate;

    @Transient
    private Integer attachedDocId;

    @Transient
    private String attachedDocName;


    public Ticket() {}

    public int getTicketId() { return ticketId; }
    public void setTicketId(int ticketId) { this.ticketId = ticketId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }

    public User getStudent() { return student; }
    public void setStudent(User student) { this.student = student; }

    public User getAssignedStaff() { return assignedStaff; }
    public void setAssignedStaff(User assignedStaff) { this.assignedStaff = assignedStaff; }

    public Category getCategory() { return category; }
    public void setCategory(Category category) { this.category = category; }

    public Faculty getFaculty() { return faculty; }
    public void setFaculty(Faculty faculty) { this.faculty = faculty; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public Timestamp getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Timestamp updatedAt) { this.updatedAt = updatedAt; }

    public String getStaffResponse() { return staffResponse; }
    public void setStaffResponse(String staffResponse) { this.staffResponse = staffResponse; }

    public boolean isEscalated() { return isEscalated; }
    public void setEscalated(boolean escalated) { isEscalated = escalated; }
    public boolean getIsEscalated() { return isEscalated; }

    public String getAdminResponse() { return adminResponse; }
    public void setAdminResponse(String adminResponse) { this.adminResponse = adminResponse; }

    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }

    public boolean isCanEscalate() { return canEscalate; }
    public void setCanEscalate(boolean canEscalate) { this.canEscalate = canEscalate; }

    public String getStudentUniversityId() { return studentUniversityId; }
    public void setStudentUniversityId(String studentUniversityId) { this.studentUniversityId = studentUniversityId; }

    public Integer getAttachedDocId() { return attachedDocId; }
    public void setAttachedDocId(Integer attachedDocId) { this.attachedDocId = attachedDocId; }

    public String getAttachedDocName() { return attachedDocName; }
    public void setAttachedDocName(String attachedDocName) { this.attachedDocName = attachedDocName; }
}
