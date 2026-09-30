package com.campus.helpdesk.model;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.io.Serializable;
import java.sql.Timestamp;

@Entity // මේකෙන් කියන්නේ මේ ක්ලාස් එක Database Table එකක් කියලා
@Table(name = "Users") // Database එකේ තියෙන Table එකේ නම
public class User implements Serializable {

    @Id // මේක Primary Key එක කියලා කියන්න
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Auto Increment වෙන බව කියන්න
    @Column(name = "user_id") // Database එකේ column එකේ නම (user_id)
    private int userId;

    @Column(name = "full_name")
    private String fullName;

    @Column(name = "email")
    private String email;

    @Column(name = "university_id")
    private String universityId;

    @Column(name = "faculty_id")
    private Integer facultyId; // මෙතන int වෙනුවට Integer ලෙස වෙනස් කරන්න

    @Column(name = "password_hash")
    private String passwordHash;

    @Column(name = "role")
    private String role;

    @Column(name = "account_status")
    private String accountStatus;

    @CreationTimestamp // පේළියක් හැදෙද්දී ඉබේම වෙලාව වැටෙන්න
    @Column(name = "created_at", updatable = false)
    private Timestamp createdAt;

    @UpdateTimestamp // පේළියක් Update වෙද්දී ඉබේම වෙලාව වැටෙන්න
    @Column(name = "updated_at")
    private Timestamp updatedAt;

    // --- Constructors ---

    public User() {
    }

    public User(int userId, String fullName, String email, String universityId, int facultyId, String passwordHash, String role, String accountStatus) {
        this.userId = userId;
        this.fullName = fullName;
        this.email = email;
        this.universityId = universityId;
        this.facultyId = facultyId;
        this.passwordHash = passwordHash;
        this.role = role;
        this.accountStatus = accountStatus;
    }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getUniversityId() { return universityId; }
    public void setUniversityId(String universityId) { this.universityId = universityId; }

    public Integer getFacultyId() { return facultyId; }
    public void setFacultyId(Integer facultyId) { this.facultyId = facultyId; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getAccountStatus() { return accountStatus; }
    public void setAccountStatus(String accountStatus) { this.accountStatus = accountStatus; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public Timestamp getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Timestamp updatedAt) { this.updatedAt = updatedAt; }
}