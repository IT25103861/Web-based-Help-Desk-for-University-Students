package com.campus.helpdesk.model;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.io.Serializable;
import java.sql.Timestamp;

@Entity
@Table(name = "Users")
public class User implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private int userId;

    @Column(name = "full_name")
    private String fullName;

    @Column(name = "email", unique = true, nullable = false)
    private String email;

    @Column(name = "university_id", unique = true, nullable = false)
    private String universityId;

    @Column(name = "nic", unique = true, nullable = false)
    private String nic;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "faculty_id")
    private Faculty faculty;

    @Column(name = "password_hash")
    private String passwordHash;

    @Column(name = "role")
    private String role;

    @Column(name = "account_status")
    private String accountStatus;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Timestamp createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Timestamp updatedAt;

    public User() {
    }

    public User(int userId, String fullName, String email, String universityId, String nic, Faculty faculty, String passwordHash, String role, String accountStatus) {
        this.userId = userId;
        this.fullName = fullName;
        this.email = email;
        this.universityId = universityId;
        this.nic = nic;
        this.faculty = faculty;
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
    public String getNic() { return nic; }
    public void setUniversityId(String universityId) { this.universityId = universityId; }
    public void setNic(String nic) { this.nic = nic; }

    public Faculty getFaculty() { return faculty; }
    public void setFaculty(Faculty faculty) { this.faculty = faculty; }

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
