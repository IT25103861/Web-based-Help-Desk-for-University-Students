-- ============================================================
-- PORTABLE DATABASE INITIALIZATION SCRIPT
-- Database Name: University_Help_Desk
-- Includes: Schema, Sample Data, Procedures, and Triggers
-- ============================================================

-- ============================================================
-- DATABASE DDL SCRIPT: UNIVERSITY HELP DESK SYSTEM
-- DBMS: Microsoft SQL Server (T-SQL / SSMS)
-- Database Name: University_Help_Desk
-- ============================================================

SET ANSI_NULLS ON;
SET QUOTED_IDENTIFIER ON;
GO

IF NOT EXISTS (SELECT * FROM sys.databases WHERE name = 'University_Help_Desk')
BEGIN
    CREATE DATABASE University_Help_Desk;
END;
GO

USE University_Help_Desk;
GO

-- Drop tables in reverse dependency order if re-executing
IF OBJECT_ID('NOTIFICATION', 'U') IS NOT NULL DROP TABLE NOTIFICATION;
IF OBJECT_ID('APPOINTMENT', 'U') IS NOT NULL DROP TABLE APPOINTMENT;
IF OBJECT_ID('ESCALATION', 'U') IS NOT NULL DROP TABLE ESCALATION;
IF OBJECT_ID('ESCALATION_RULE', 'U') IS NOT NULL DROP TABLE ESCALATION_RULE;
IF OBJECT_ID('TICKET_HISTORY', 'U') IS NOT NULL DROP TABLE TICKET_HISTORY;
IF OBJECT_ID('ATTACHMENT', 'U') IS NOT NULL DROP TABLE ATTACHMENT;
IF OBJECT_ID('TICKET_ASSIGNMENT', 'U') IS NOT NULL DROP TABLE TICKET_ASSIGNMENT;
IF OBJECT_ID('TICKET', 'U') IS NOT NULL DROP TABLE TICKET;
IF OBJECT_ID('CATEGORY', 'U') IS NOT NULL DROP TABLE CATEGORY;
IF OBJECT_ID('DEPARTMENT_OFFICER', 'U') IS NOT NULL DROP TABLE DEPARTMENT_OFFICER;
IF OBJECT_ID('SUPPORT_STAFF', 'U') IS NOT NULL DROP TABLE SUPPORT_STAFF;
IF OBJECT_ID('ADMINISTRATOR', 'U') IS NOT NULL DROP TABLE ADMINISTRATOR;
IF OBJECT_ID('STUDENT', 'U') IS NOT NULL DROP TABLE STUDENT;
IF OBJECT_ID('DEPARTMENT', 'U') IS NOT NULL DROP TABLE DEPARTMENT;
IF OBJECT_ID('USER', 'U') IS NOT NULL DROP TABLE [USER];
GO

-- 1. USER TABLE (Supertype Entity)
CREATE TABLE [USER] (
    UserID              VARCHAR(10) PRIMARY KEY,
    FullName            VARCHAR(100) NOT NULL,
    Email               VARCHAR(100) UNIQUE NOT NULL,
    PasswordHash        VARCHAR(255) NOT NULL,
    AccountStatus       VARCHAR(20) NOT NULL DEFAULT 'Pending'
                         CHECK (AccountStatus IN ('Pending','Active','Suspended','Deactivated')),
    MustChangePassword  BIT NOT NULL DEFAULT 1,
    EmailVerified       BIT NOT NULL DEFAULT 0,
    CreatedByUserID     VARCHAR(10) NULL CONSTRAINT fk_user_created_by REFERENCES [USER](UserID),
    CreatedAt           DATETIME2 NOT NULL DEFAULT CURRENT_TIMESTAMP,
    LastLoginAt         DATETIME2 NULL,

    CONSTRAINT ck_user_email_format CHECK (Email LIKE '%@%.%'),
    CONSTRAINT ck_user_login_date CHECK (LastLoginAt IS NULL OR LastLoginAt >= CreatedAt)
);
GO

-- 2. DEPARTMENT TABLE
CREATE TABLE DEPARTMENT (
    DepartmentID     VARCHAR(10) PRIMARY KEY,
    DepartmentName   VARCHAR(100) UNIQUE NOT NULL,
    Description      NVARCHAR(MAX),
    ContactEmail     VARCHAR(100),
    DepartmentStatus VARCHAR(20) NOT NULL DEFAULT 'Active'
                      CHECK (DepartmentStatus IN ('Active','Inactive')),

    CONSTRAINT ck_dept_email_format CHECK (ContactEmail IS NULL OR ContactEmail LIKE '%@%.%')
);
GO

-- 3. STUDENT TABLE (Subtype of USER)
CREATE TABLE STUDENT (
    UserID        VARCHAR(10) PRIMARY KEY CONSTRAINT fk_student_user REFERENCES [USER](UserID) ON DELETE CASCADE,
    StudentNumber VARCHAR(20) UNIQUE NOT NULL,
    Faculty       VARCHAR(100) NOT NULL,
    Programme     VARCHAR(100) NOT NULL,
    AcademicYear  INT NOT NULL CHECK (AcademicYear BETWEEN 1 AND 4),

    CONSTRAINT ck_student_num_not_empty CHECK (LEN(LTRIM(RTRIM(StudentNumber))) > 0)
);
GO

-- 4. ADMINISTRATOR TABLE (Subtype of USER)
CREATE TABLE ADMINISTRATOR (
    UserID    VARCHAR(10) PRIMARY KEY CONSTRAINT fk_admin_user REFERENCES [USER](UserID) ON DELETE CASCADE,
    AdminType VARCHAR(20) NOT NULL CHECK (AdminType IN ('ADMIN','SUPER_ADMIN'))
);
GO

-- 5. SUPPORT STAFF TABLE (Subtype of USER)
CREATE TABLE SUPPORT_STAFF (
    UserID       VARCHAR(10) PRIMARY KEY CONSTRAINT fk_staff_user REFERENCES [USER](UserID) ON DELETE CASCADE,
    DepartmentID VARCHAR(10) NOT NULL CONSTRAINT fk_staff_dept REFERENCES DEPARTMENT(DepartmentID) ON DELETE NO ACTION
);
GO

-- 6. DEPARTMENT OFFICER TABLE (Subtype of USER)
CREATE TABLE DEPARTMENT_OFFICER (
    UserID       VARCHAR(10) PRIMARY KEY CONSTRAINT fk_officer_user REFERENCES [USER](UserID) ON DELETE CASCADE,
    DepartmentID VARCHAR(10) NOT NULL CONSTRAINT fk_officer_dept REFERENCES DEPARTMENT(DepartmentID) ON DELETE NO ACTION
);
GO

-- 7. CATEGORY TABLE
CREATE TABLE CATEGORY (
    CategoryID     VARCHAR(10) PRIMARY KEY,
    DepartmentID   VARCHAR(10) NOT NULL CONSTRAINT fk_category_dept REFERENCES DEPARTMENT(DepartmentID),
    CategoryName   VARCHAR(100) UNIQUE NOT NULL,
    Description    NVARCHAR(MAX),
    CategoryStatus VARCHAR(20) NOT NULL DEFAULT 'Active'
                    CHECK (CategoryStatus IN ('Active','Inactive'))
);
GO

-- 8. TICKET TABLE
CREATE TABLE TICKET (
    TicketID      VARCHAR(10) PRIMARY KEY,
    StudentUserID VARCHAR(10) NOT NULL CONSTRAINT fk_ticket_student REFERENCES STUDENT(UserID),
    CategoryID    VARCHAR(10) NOT NULL CONSTRAINT fk_ticket_category REFERENCES CATEGORY(CategoryID),
    Subject       VARCHAR(200) NOT NULL,
    Description   NVARCHAR(MAX) NOT NULL,
    Priority      VARCHAR(10) NOT NULL CHECK (Priority IN ('Low','Medium','High','Critical')),
    Status        VARCHAR(30) NOT NULL DEFAULT 'New'
                   CHECK (Status IN ('New','Open','In Progress','Waiting for Student Response','Escalated','Resolved','Closed','Cancelled')),
    CreatedAt     DATETIME2 NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UpdatedAt     DATETIME2 NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ResolvedAt    DATETIME2 NULL,
    ClosedAt      DATETIME2 NULL,

    CONSTRAINT ck_ticket_subject_not_empty CHECK (LEN(LTRIM(RTRIM(Subject))) > 0),
    CONSTRAINT ck_ticket_resolved_date CHECK (ResolvedAt IS NULL OR ResolvedAt >= CreatedAt),
    CONSTRAINT ck_ticket_closed_date CHECK (ClosedAt IS NULL OR ClosedAt >= CreatedAt),
    CONSTRAINT ck_ticket_resolved_status CHECK (
        (Status IN ('Resolved', 'Closed') AND ResolvedAt IS NOT NULL) OR
        (Status NOT IN ('Resolved', 'Closed') AND ResolvedAt IS NULL)
    ),
    CONSTRAINT ck_ticket_closed_status CHECK (
        (Status = 'Closed' AND ClosedAt IS NOT NULL) OR
        (Status <> 'Closed' AND ClosedAt IS NULL)
    ),
    CONSTRAINT ck_ticket_date_order CHECK (
        ClosedAt IS NULL OR (ResolvedAt IS NOT NULL AND ClosedAt >= ResolvedAt)
    )
);
GO

-- 9. TICKET ASSIGNMENT TABLE
CREATE TABLE TICKET_ASSIGNMENT (
    AssignmentID            VARCHAR(10) PRIMARY KEY,
    TicketID                VARCHAR(10) NOT NULL CONSTRAINT fk_assign_ticket REFERENCES TICKET(TicketID) ON DELETE CASCADE,
    SupportStaffUserID      VARCHAR(10) NULL CONSTRAINT fk_assign_staff REFERENCES SUPPORT_STAFF(UserID) ON DELETE NO ACTION,
    DepartmentOfficerUserID VARCHAR(10) NULL CONSTRAINT fk_assign_officer REFERENCES DEPARTMENT_OFFICER(UserID) ON DELETE NO ACTION,
    AssignedByUserID        VARCHAR(10) NOT NULL CONSTRAINT fk_assign_by REFERENCES [USER](UserID) ON DELETE NO ACTION,
    AssignedAt              DATETIME2 NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UnassignedAt            DATETIME2 NULL,
    AssignmentStatus        VARCHAR(10) NOT NULL DEFAULT 'Active' CHECK (AssignmentStatus IN ('Active', 'Ended')),

    CONSTRAINT ck_one_assignment_target CHECK (
        (SupportStaffUserID IS NOT NULL AND DepartmentOfficerUserID IS NULL) OR
        (SupportStaffUserID IS NULL AND DepartmentOfficerUserID IS NOT NULL)
    ),
    CONSTRAINT ck_assignment_status_dates CHECK (
        (AssignmentStatus = 'Active' AND UnassignedAt IS NULL) OR
        (AssignmentStatus = 'Ended' AND UnassignedAt IS NOT NULL)
    ),
    CONSTRAINT ck_assignment_chronology CHECK (UnassignedAt IS NULL OR UnassignedAt >= AssignedAt)
);
GO

SET ANSI_NULLS ON;
SET QUOTED_IDENTIFIER ON;
GO

-- Filtered Unique Index in SQL Server
CREATE UNIQUE INDEX one_active_assignment_per_ticket
    ON TICKET_ASSIGNMENT (TicketID)
    WHERE AssignmentStatus = 'Active';
GO

-- 10. ATTACHMENT TABLE
CREATE TABLE ATTACHMENT (
    AttachmentID     VARCHAR(10) PRIMARY KEY,
    TicketID         VARCHAR(10) NOT NULL CONSTRAINT fk_att_ticket REFERENCES TICKET(TicketID) ON DELETE CASCADE,
    UploadedByUserID VARCHAR(10) NOT NULL CONSTRAINT fk_att_user REFERENCES [USER](UserID),
    FileName         VARCHAR(150) NOT NULL,
    FileType         VARCHAR(20) NOT NULL,
    FilePath         VARCHAR(255) NOT NULL,
    UploadedAt       DATETIME2 NOT NULL DEFAULT CURRENT_TIMESTAMP
);
GO

-- 11. TICKET HISTORY TABLE
CREATE TABLE TICKET_HISTORY (
    HistoryID       VARCHAR(10) PRIMARY KEY,
    TicketID        VARCHAR(10) NOT NULL CONSTRAINT fk_hist_ticket REFERENCES TICKET(TicketID) ON DELETE CASCADE,
    ChangedByUserID VARCHAR(10) NOT NULL CONSTRAINT fk_hist_user REFERENCES [USER](UserID),
    ActionType      VARCHAR(30) NOT NULL,
    OldStatus       VARCHAR(30) NULL,
    NewStatus       VARCHAR(30) NULL,
    OldPriority     VARCHAR(10) NULL,
    NewPriority     VARCHAR(10) NULL,
    Remarks         NVARCHAR(MAX) NULL,
    ChangedAt       DATETIME2 NOT NULL DEFAULT CURRENT_TIMESTAMP
);
GO

-- 12. ESCALATION RULE TABLE
CREATE TABLE ESCALATION_RULE (
    RuleID          VARCHAR(10) PRIMARY KEY,
    DepartmentID    VARCHAR(10) NOT NULL CONSTRAINT fk_erule_dept REFERENCES DEPARTMENT(DepartmentID),
    CategoryID      VARCHAR(10) NULL CONSTRAINT fk_erule_cat REFERENCES CATEGORY(CategoryID),
    ThresholdHours  INT NOT NULL CHECK (ThresholdHours > 0),
    EscalateToRole  VARCHAR(20) NOT NULL CHECK (EscalateToRole IN ('Department Officer', 'Administrator')),
    IsActive        BIT NOT NULL DEFAULT 1,
    CreatedByUserID VARCHAR(10) NOT NULL CONSTRAINT fk_erule_user REFERENCES ADMINISTRATOR(UserID),
    CreatedAt       DATETIME2 NOT NULL DEFAULT CURRENT_TIMESTAMP
);
GO

-- 13. ESCALATION TABLE
CREATE TABLE ESCALATION (
    EscalationID            VARCHAR(10) PRIMARY KEY,
    TicketID                VARCHAR(10) NOT NULL CONSTRAINT fk_esc_ticket REFERENCES TICKET(TicketID) ON DELETE CASCADE,
    RuleID                  VARCHAR(10) NULL CONSTRAINT fk_esc_rule REFERENCES ESCALATION_RULE(RuleID) ON DELETE SET NULL,
    EscalatedByUserID       VARCHAR(10) NULL CONSTRAINT fk_esc_by REFERENCES [USER](UserID) ON DELETE NO ACTION,
    DepartmentOfficerUserID VARCHAR(10) NULL CONSTRAINT fk_esc_officer REFERENCES DEPARTMENT_OFFICER(UserID) ON DELETE NO ACTION,
    AdministratorUserID     VARCHAR(10) NULL CONSTRAINT fk_esc_admin REFERENCES ADMINISTRATOR(UserID) ON DELETE NO ACTION,
    Reason                  NVARCHAR(MAX) NOT NULL,
    EscalatedAt             DATETIME2 NOT NULL DEFAULT CURRENT_TIMESTAMP,
    EscalationStatus        VARCHAR(15) NOT NULL DEFAULT 'Pending'
                             CHECK (EscalationStatus IN ('Pending','Accepted','In Progress','Resolved','Cancelled')),

    CONSTRAINT ck_one_escalation_target CHECK (
        (DepartmentOfficerUserID IS NOT NULL AND AdministratorUserID IS NULL) OR
        (DepartmentOfficerUserID IS NULL AND AdministratorUserID IS NOT NULL)
    ),
    CONSTRAINT ck_escalation_different_users CHECK (
        EscalatedByUserID IS NULL OR (
            (DepartmentOfficerUserID IS NULL OR EscalatedByUserID <> DepartmentOfficerUserID) AND
            (AdministratorUserID IS NULL OR EscalatedByUserID <> AdministratorUserID)
        )
    )
);
GO

-- 14. APPOINTMENT TABLE
CREATE TABLE APPOINTMENT (
    AppointmentID     VARCHAR(10) PRIMARY KEY,
    TicketID          VARCHAR(10) NULL CONSTRAINT fk_app_ticket REFERENCES TICKET(TicketID),
    StudentUserID     VARCHAR(10) NOT NULL CONSTRAINT fk_app_student REFERENCES STUDENT(UserID),
    OfficerUserID     VARCHAR(10) NOT NULL CONSTRAINT fk_app_officer REFERENCES DEPARTMENT_OFFICER(UserID),
    RequestedAt       DATETIME2 NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ScheduledAt       DATETIME2 NULL,
    Status            VARCHAR(15) NOT NULL DEFAULT 'Pending'
                       CHECK (Status IN ('Pending','Approved','Rejected','Rescheduled','Cancelled','Completed')),
    Reason            NVARCHAR(MAX) NOT NULL,
    OutcomeNotes      NVARCHAR(MAX) NULL,
    AgreedActions     NVARCHAR(MAX) NULL,
    OutcomeRecordedAt DATETIME2 NULL,

    CONSTRAINT ck_appointment_outcome CHECK (
        (Status = 'Completed' AND OutcomeRecordedAt IS NOT NULL) OR
        (Status <> 'Completed' AND OutcomeRecordedAt IS NULL)
    ),
    CONSTRAINT ck_appointment_schedule CHECK (
        Status IN ('Pending', 'Rejected', 'Cancelled') OR ScheduledAt IS NOT NULL
    ),
    CONSTRAINT ck_appointment_different_users CHECK (StudentUserID <> OfficerUserID),
    CONSTRAINT ck_appointment_scheduled_date CHECK (ScheduledAt IS NULL OR ScheduledAt >= RequestedAt),
    CONSTRAINT ck_appointment_outcome_date CHECK (
        OutcomeRecordedAt IS NULL OR (
            OutcomeRecordedAt >= RequestedAt AND
            (ScheduledAt IS NULL OR OutcomeRecordedAt >= ScheduledAt)
        )
    )
);
GO

-- 15. NOTIFICATION TABLE
CREATE TABLE NOTIFICATION (
    NotificationID VARCHAR(10) PRIMARY KEY,
    UserID         VARCHAR(10) NOT NULL CONSTRAINT fk_notif_user REFERENCES [USER](UserID),
    TicketID       VARCHAR(10) NULL CONSTRAINT fk_notif_ticket REFERENCES TICKET(TicketID),
    AppointmentID  VARCHAR(10) NULL CONSTRAINT fk_notif_app REFERENCES APPOINTMENT(AppointmentID),
    Type           VARCHAR(50) NOT NULL,
    Message        NVARCHAR(255) NOT NULL,
    IsRead         BIT NOT NULL DEFAULT 0,
    CreatedAt      DATETIME2 NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ReadAt         DATETIME2 NULL,

    CONSTRAINT ck_notification_target CHECK (TicketID IS NULL OR AppointmentID IS NULL),
    CONSTRAINT ck_notification_read_date CHECK (ReadAt IS NULL OR ReadAt >= CreatedAt),
    CONSTRAINT ck_notification_read_status CHECK (
        (IsRead = 1 AND ReadAt IS NOT NULL) OR
        (IsRead = 0 AND ReadAt IS NULL)
    )
);
GO

-- 17. AUTOMATIC TRIGGER FOR TICKET.UpdatedAt IN SQL SERVER
IF OBJECT_ID('trg_ticket_updated_at', 'TR') IS NOT NULL DROP TRIGGER trg_ticket_updated_at;
GO

CREATE TRIGGER trg_ticket_updated_at
ON TICKET
AFTER UPDATE
AS
BEGIN
    SET NOCOUNT ON;
    UPDATE TICKET
    SET UpdatedAt = CURRENT_TIMESTAMP
    FROM TICKET t
    INNER JOIN inserted i ON t.TicketID = i.TicketID;
END;
GO


GO
-- ============================================================
-- INSERTING SAMPLE DATA
-- ============================================================

SET ANSI_NULLS ON;
SET QUOTED_IDENTIFIER ON;

-- ============================================================
-- PART C: SAMPLE DATA INSERTION (UPDATED CREATOR HIERARCHY)
-- University Help Desk System
-- Hierarchy Rule for CreatedByUserID:
--   - Super Admins (SADM1, SADM2): CreatedByUserID IS NULL
--   - Admins (ADM01-ADM03): Created by Super Admins (SADM1/SADM2)
--   - Officers (DEO01-DEO05): Created by Super Admins / Admins
--   - Support Staff (STF01-STF05): Created by Admins (ADM01-ADM03)
--   - Students (ST001-ST005): Created by Support Staff / Officers / Admins
-- ============================================================

-- 1. USER TABLE (Insertion order respects CreatedByUserID self-references)
INSERT INTO [USER] (UserID, FullName, Email, PasswordHash, AccountStatus, MustChangePassword, EmailVerified, CreatedByUserID, CreatedAt, LastLoginAt) VALUES
-- Super Administrators (Top Level - CreatedByUserID IS NULL)
('SADM1', 'Dr. Ruwan Wickramasinghe', 'ruwan.w@sliit.lk', 'hash_admin_201', 'Active', 0, 1, NULL, '2025-12-01 08:00:00', '2026-09-27 08:00:00'),
('SADM2', 'Prof. Anura Kumara', 'anura.k@sliit.lk', 'hash_admin_202', 'Active', 0, 1, NULL, '2025-12-01 08:00:00', '2026-09-26 18:00:00'),

-- Administrators (Created by Super Admins)
('ADM01', 'Chathuri Peiris', 'chathuri.p@sliit.lk', 'hash_admin_203', 'Active', 0, 1, 'SADM1', '2026-01-05 09:00:00', '2026-09-25 11:00:00'),
('ADM02', 'Mahesh Samarasinghe', 'mahesh.s@sliit.lk', 'hash_admin_204', 'Active', 0, 1, 'SADM1', '2026-01-05 09:00:00', '2026-09-24 15:30:00'),
('ADM03', 'Thilini Fonseka', 'thilini.f@sliit.lk', 'hash_admin_205', 'Active', 0, 1, 'SADM2', '2026-01-06 10:00:00', '2026-09-20 12:00:00'),

-- Department Officers (Created by Super Admins & Admins)
('DEO01', 'Mr. Asanka Mendis', 'asanka.m@sliit.lk', 'hash_officer_401', 'Active', 0, 1, 'SADM1', '2026-01-02 08:00:00', '2026-09-27 08:30:00'),
('DEO02', 'Mrs. Rashmi Senanayake', 'rashmi.s@sliit.lk', 'hash_officer_402', 'Active', 0, 1, 'SADM1', '2026-01-02 08:00:00', '2026-09-26 15:00:00'),
('DEO03', 'Mr. Dammika Cooray', 'dammika.c@sliit.lk', 'hash_officer_403', 'Active', 0, 1, 'SADM2', '2026-01-03 09:00:00', '2026-09-25 14:00:00'),
('DEO04', 'Mrs. Nadeesha Pathirana', 'nadeesha.p@sliit.lk', 'hash_officer_404', 'Active', 0, 1, 'ADM01', '2026-01-03 09:00:00', '2026-09-24 10:00:00'),
('DEO05', 'Mr. Malith Gunawardena', 'malith.g@sliit.lk', 'hash_officer_405', 'Active', 0, 1, 'ADM02', '2026-01-04 10:00:00', '2026-09-26 13:00:00'),

-- Support Staff (Created by Admins)
('STF01', 'Saman Kumara', 'saman.k@sliit.lk', 'hash_staff_301', 'Active', 0, 1, 'ADM01', '2026-01-15 08:30:00', '2026-09-27 09:00:00'),
('STF02', 'Priyani Ratnayake', 'priyani.r@sliit.lk', 'hash_staff_302', 'Active', 0, 1, 'ADM01', '2026-01-15 08:30:00', '2026-09-26 17:00:00'),
('STF03', 'Gayan Rodrigo', 'gayan.r@sliit.lk', 'hash_staff_303', 'Active', 0, 1, 'ADM02', '2026-01-16 09:00:00', '2026-09-25 16:00:00'),
('STF04', 'Kavinda Jayasuriya', 'kavinda.j@sliit.lk', 'hash_staff_304', 'Active', 0, 1, 'ADM02', '2026-01-16 09:00:00', '2026-09-26 11:30:00'),
('STF05', 'Nirosha Abeysekera', 'nirosha.a@sliit.lk', 'hash_staff_305', 'Deactivated', 0, 1, 'ADM03', '2026-01-17 10:00:00', '2026-07-01 10:00:00'),

-- Students (Created/Registered by Support Staff, Officers, and Admins)
('ST001', 'Kamal Perera', 'kamal.p@sliit.lk', 'hash_pass_101', 'Active', 0, 1, 'STF01', '2026-01-10 08:00:00', '2026-09-25 10:30:00'),
('ST002', 'Nimali Silva', 'nimali.s@sliit.lk', 'hash_pass_102', 'Active', 0, 1, 'STF02', '2026-01-11 09:15:00', '2026-09-26 14:20:00'),
('ST003', 'Sunil Fernando', 'sunil.f@sliit.lk', 'hash_pass_103', 'Active', 1, 0, 'DEO01', '2026-02-01 10:00:00', NULL),
('ST004', 'Dilini Jayawardena', 'dilini.j@sliit.lk', 'hash_pass_104', 'Active', 0, 1, 'DEO02', '2026-02-05 11:30:00', '2026-09-24 16:45:00'),
('ST005', 'Kasun Bandara', 'kasun.b@sliit.lk', 'hash_pass_105', 'Suspended', 0, 1, 'ADM01', '2026-02-10 14:00:00', '2026-08-15 09:00:00');

-- 2. DEPARTMENT TABLE (5 Records)
INSERT INTO DEPARTMENT (DepartmentID, DepartmentName, Description, ContactEmail, DepartmentStatus) VALUES
('DEP01', 'Faculty of Computing', 'Department responsible for IT, SE, and CS academic support.', 'computing.help@sliit.lk', 'Active'),
('DEP02', 'Faculty of Business', 'Handles business administration and management inquiries.', 'business.help@sliit.lk', 'Active'),
('DEP03', 'Faculty of Engineering', 'Manages engineering lab inquiries and course assistance.', 'engineering.help@sliit.lk', 'Active'),
('DEP04', 'Examination Bureau', 'Handles exam schedules, results, and grade appeals.', 'exams@sliit.lk', 'Active'),
('DEP05', 'Student Affairs Division', 'Manages scholarships, IDs, and general student welfare.', 'studentaffairs@sliit.lk', 'Active');

-- 3. STUDENT TABLE (5 Records: ST001 - ST005)
INSERT INTO STUDENT (UserID, StudentNumber, Faculty, Programme, AcademicYear) VALUES
('ST001', 'IT21001001', 'Faculty of Computing', 'BSc (Hons) in Information Technology', 2),
('ST002', 'IT21001002', 'Faculty of Computing', 'BSc (Hons) in Software Engineering', 2),
('ST003', 'BM21002001', 'Faculty of Business', 'BBA (Hons) in Financial Management', 1),
('ST004', 'EN21003001', 'Faculty of Engineering', 'BSc (Hons) in Civil Engineering', 3),
('ST005', 'IT21001005', 'Faculty of Computing', 'BSc (Hons) in Cyber Security', 4);

-- 4. ADMINISTRATOR TABLE (5 Records)
INSERT INTO ADMINISTRATOR (UserID, AdminType) VALUES
('SADM1', 'SUPER_ADMIN'),
('SADM2', 'SUPER_ADMIN'),
('ADM01', 'ADMIN'),
('ADM02', 'ADMIN'),
('ADM03', 'ADMIN');

-- 5. SUPPORT STAFF TABLE (5 Records)
INSERT INTO SUPPORT_STAFF (UserID, DepartmentID) VALUES
('STF01', 'DEP01'),
('STF02', 'DEP01'),
('STF03', 'DEP02'),
('STF04', 'DEP03'),
('STF05', 'DEP04');

-- 6. DEPARTMENT OFFICER TABLE (5 Records)
INSERT INTO DEPARTMENT_OFFICER (UserID, DepartmentID) VALUES
('DEO01', 'DEP01'),
('DEO02', 'DEP02'),
('DEO03', 'DEP03'),
('DEO04', 'DEP04'),
('DEO05', 'DEP05');

-- 7. CATEGORY TABLE (5 Records)
INSERT INTO CATEGORY (CategoryID, DepartmentID, CategoryName, Description, CategoryStatus) VALUES
('CAT01', 'DEP01', 'LMS & Portal Login Issue', 'Problems accessing Courseweb or student portal accounts.', 'Active'),
('CAT02', 'DEP01', 'Software & Lab Equipment', 'Issues with lab software licenses or desktop PCs.', 'Active'),
('CAT03', 'DEP02', 'Course Module Registration', 'Queries regarding elective module selection and approval.', 'Active'),
('CAT04', 'DEP04', 'Exam Repeat & Transcript', 'Requests for official academic transcripts and repeat exams.', 'Active'),
('CAT05', 'DEP05', 'Student Identity Card', 'Loss, renewal, or re-issuance of student ID cards.', 'Active');

-- 8. TICKET TABLE (5 Records)
INSERT INTO TICKET (TicketID, StudentUserID, CategoryID, Subject, Description, Priority, Status, CreatedAt, UpdatedAt, ResolvedAt, ClosedAt) VALUES
('TCK01', 'ST001', 'CAT01', 'Cannot access IT2140 course material', 'Getting 403 Forbidden error when opening Lab Sheet 5 on Courseweb.', 'High', 'Open', '2026-09-20 09:00:00', '2026-09-20 09:00:00', NULL, NULL),
('TCK02', 'ST002', 'CAT02', 'MATLAB License expired in Lab 4', 'Lab PC 12 displays expired license warning when opening MATLAB.', 'Medium', 'In Progress', '2026-09-21 10:15:00', '2026-09-21 11:30:00', NULL, NULL),
('TCK03', 'ST003', 'CAT03', 'Unable to enroll in Accounting II', 'System states prerequisite not met, but I passed Accounting I.', 'High', 'Resolved', '2026-09-18 14:00:00', '2026-09-19 16:00:00', '2026-09-19 16:00:00', NULL),
('TCK04', 'ST004', 'CAT04', 'Official Transcript Request Delay', 'Applied for transcript 2 weeks ago, payment status still pending.', 'Critical', 'Closed', '2026-09-10 11:00:00', '2026-09-15 15:30:00', '2026-09-14 10:00:00', '2026-09-15 15:30:00'),
('TCK05', 'ST005', 'CAT05', 'Replacement ID Card Payment Error', 'Paid fee via online gateway, receipt generated but portal shows unpaid.', 'Low', 'Escalated', '2026-09-22 08:45:00', '2026-09-23 10:00:00', NULL, NULL);

-- 9. TICKET ASSIGNMENT TABLE (5 Records)
INSERT INTO TICKET_ASSIGNMENT (AssignmentID, TicketID, SupportStaffUserID, DepartmentOfficerUserID, AssignedByUserID, AssignedAt, UnassignedAt, AssignmentStatus) VALUES
('TKA01', 'TCK01', 'STF01', NULL, 'DEO01', '2026-09-20 09:30:00', NULL, 'Active'),
('TKA02', 'TCK02', 'STF02', NULL, 'DEO01', '2026-09-21 10:30:00', NULL, 'Active'),
('TKA03', 'TCK03', NULL, 'DEO02', 'SADM1', '2026-09-18 14:30:00', '2026-09-19 16:00:00', 'Ended'),
('TKA04', 'TCK04', 'STF05', NULL, 'DEO04', '2026-09-10 11:30:00', '2026-09-15 15:30:00', 'Ended'),
('TKA05', 'TCK05', NULL, 'DEO05', 'SADM2', '2026-09-23 10:00:00', NULL, 'Active');

-- 10. ATTACHMENT TABLE (5 Records)
INSERT INTO ATTACHMENT (AttachmentID, TicketID, UploadedByUserID, FileName, FileType, FilePath, UploadedAt) VALUES
('ATT01', 'TCK01', 'ST001', 'courseweb_error.png', 'image/png', '/uploads/2026/09/error_403.png', '2026-09-20 09:05:00'),
('ATT02', 'TCK02', 'ST002', 'matlab_license_screen.jpg', 'image/jpeg', '/uploads/2026/09/matlab_exp.jpg', '2026-09-21 10:20:00'),
('ATT03', 'TCK03', 'ST003', 'accounting1_grade_slip.pdf', 'application/pdf', '/uploads/2026/09/grades_acc1.pdf', '2026-09-18 14:10:00'),
('ATT04', 'TCK04', 'ST004', 'bank_payment_receipt.pdf', 'application/pdf', '/uploads/2026/09/receipt_9941.pdf', '2026-09-10 11:15:00'),
('ATT05', 'TCK05', 'ST005', 'id_payment_screenshot.png', 'image/png', '/uploads/2026/09/id_payment.png', '2026-09-22 08:50:00');

-- 11. TICKET HISTORY TABLE (5 Records)
INSERT INTO TICKET_HISTORY (HistoryID, TicketID, ChangedByUserID, ActionType, OldStatus, NewStatus, OldPriority, NewPriority, Remarks, ChangedAt) VALUES
('HIS01', 'TCK01', 'ST001', 'TICKET_CREATED', NULL, 'New', NULL, 'High', 'Ticket submitted by student.', '2026-09-20 09:00:00'),
('HIS02', 'TCK01', 'DEO01', 'ASSIGNED', 'New', 'Open', 'High', 'High', 'Assigned to IT support staff Saman.', '2026-09-20 09:30:00'),
('HIS03', 'TCK02', 'STF02', 'STATUS_CHANGE', 'New', 'In Progress', 'Medium', 'Medium', 'Staff investigating license server.', '2026-09-21 11:30:00'),
('HIS04', 'TCK03', 'DEO02', 'RESOLVED', 'In Progress', 'Resolved', 'High', 'High', 'Manual enrollment granted in portal.', '2026-09-19 16:00:00'),
('HIS05', 'TCK05', 'DEO05', 'ESCALATED', 'Open', 'Escalated', 'Low', 'High', 'Escalated due to payment gateway delay.', '2026-09-23 10:00:00');

-- 12. ESCALATION RULE TABLE (5 Records)
INSERT INTO ESCALATION_RULE (RuleID, DepartmentID, CategoryID, ThresholdHours, EscalateToRole, IsActive, CreatedByUserID, CreatedAt) VALUES
('ESR01', 'DEP01', 'CAT01', 24, 'Department Officer', 1, 'SADM1', '2026-01-01 08:00:00'),
('ESR02', 'DEP01', 'CAT02', 48, 'Department Officer', 1, 'SADM1', '2026-01-01 08:00:00'),
('ESR03', 'DEP02', 'CAT03', 24, 'Department Officer', 1, 'SADM2', '2026-01-02 08:00:00'),
('ESR04', 'DEP04', 'CAT04', 72, 'Administrator', 1, 'SADM1', '2026-01-02 08:00:00'),
('ESR05', 'DEP05', 'CAT05', 48, 'Department Officer', 1, 'SADM2', '2026-01-03 08:00:00');

-- 13. ESCALATION TABLE (5 Records)
INSERT INTO ESCALATION (EscalationID, TicketID, RuleID, EscalatedByUserID, DepartmentOfficerUserID, AdministratorUserID, Reason, EscalatedAt, EscalationStatus) VALUES
('ESC01', 'TCK05', 'ESR05', 'STF01', 'DEO05', NULL, 'Payment status unresolved past 48h threshold.', '2026-09-23 10:00:00', 'In Progress'),
('ESC02', 'TCK01', 'ESR01', 'ST001', 'DEO01', NULL, 'Critical access required for upcoming midterm exam.', '2026-09-21 09:00:00', 'Pending'),
('ESC03', 'TCK04', 'ESR04', 'STF05', NULL, 'SADM1', 'Transcript urgency for foreign university application.', '2026-09-13 14:00:00', 'Resolved'),
('ESC04', 'TCK02', 'ESR02', 'STF02', 'DEO01', NULL, 'Lab exam scheduled tomorrow, software unavailable.', '2026-09-22 09:00:00', 'Accepted'),
('ESC05', 'TCK03', 'ESR03', 'ST003', 'DEO02', NULL, 'Registration deadline expires today.', '2026-09-19 09:00:00', 'Resolved');

-- 14. APPOINTMENT TABLE (5 Records)
INSERT INTO APPOINTMENT (AppointmentID, TicketID, StudentUserID, OfficerUserID, RequestedAt, ScheduledAt, Status, Reason, OutcomeNotes, AgreedActions, OutcomeRecordedAt) VALUES
('APP01', 'TCK03', 'ST003', 'DEO02', '2026-09-18 15:00:00', '2026-09-19 10:00:00', 'Completed', 'In-person verification of Accounting I prerequisite.', 'Student produced physical grade sheet.', 'Officer manually added student to lecture group.', '2026-09-19 11:00:00'),
('APP02', 'TCK04', 'ST004', 'DEO04', '2026-09-11 10:00:00', '2026-09-14 09:00:00', 'Completed', 'Urgent transcript pick-up verification.', 'Verified identity and handed over sealed transcript.', 'Student signed acknowledgment form.', '2026-09-14 09:30:00'),
('APP03', NULL, 'ST001', 'DEO01', '2026-09-24 11:00:00', '2026-09-28 14:00:00', 'Approved', 'Academic advising regarding special repeat exam module.', NULL, NULL, NULL),
('APP04', NULL, 'ST002', 'DEO01', '2026-09-25 09:00:00', NULL, 'Pending', 'Inquiry regarding credit transfer from exchange semester.', NULL, NULL, NULL),
('APP05', 'TCK05', 'ST005', 'DEO05', '2026-09-23 11:00:00', NULL, 'Rejected', 'Requested meeting outside university working hours.', 'Officer offered online consultation during working hours.', NULL, NULL);

-- 15. NOTIFICATION TABLE (5 Records)
INSERT INTO NOTIFICATION (NotificationID, UserID, TicketID, AppointmentID, Type, Message, IsRead, CreatedAt, ReadAt) VALUES
('NOT01', 'ST001', 'TCK01', NULL, 'TICKET_ASSIGNED', 'Your ticket TCK01 has been assigned to Saman Kumara.', 1, '2026-09-20 09:30:00', '2026-09-20 10:00:00'),
('NOT02', 'ST002', 'TCK02', NULL, 'STATUS_UPDATE', 'Ticket TCK02 status changed to In Progress.', 0, '2026-09-21 11:30:00', NULL),
('NOT03', 'ST003', NULL, 'APP01', 'APPOINTMENT_APPROVED', 'Your appointment with Mrs. Rashmi is scheduled for Sep 19 at 10:00 AM.', 1, '2026-09-18 16:00:00', '2026-09-18 17:00:00'),
('NOT04', 'ST004', 'TCK04', NULL, 'TICKET_CLOSED', 'Your ticket TCK04 has been marked as Closed.', 1, '2026-09-15 15:30:00', '2026-09-15 16:00:00'),
('NOT05', 'ST005', 'TCK05', NULL, 'TICKET_ESCALATED', 'Your ticket TCK05 has been escalated to Department Officer.', 0, '2026-09-23 10:00:00', NULL);


GO
-- ============================================================
-- PROCEDURES, FUNCTIONS AND TRIGGERS
-- ============================================================
USE University_Help_Desk;
GO

-- PART E: STORED PROCEDURES & USER-DEFINED FUNCTIONS
-- ============================================================

-- Function: Calculate Ticket Resolution Hours
IF OBJECT_ID('dbo.ufn_GetTicketResolutionHours', 'FN') IS NOT NULL
    DROP FUNCTION dbo.ufn_GetTicketResolutionHours;
GO

CREATE FUNCTION dbo.ufn_GetTicketResolutionHours (@TicketID VARCHAR(10))
RETURNS INT
AS
BEGIN
    DECLARE @Hours INT;
    SELECT @Hours = DATEDIFF(HOUR, CreatedAt, ISNULL(ResolvedAt, CURRENT_TIMESTAMP))
    FROM TICKET
    WHERE TicketID = @TicketID;
    
    RETURN ISNULL(@Hours, 0);
END;
GO

PRINT '--- Testing Function ufn_GetTicketResolutionHours ---';
-- SELECT 
--     TicketID, 
--     Subject, 
--     CreatedAt, 
--     ResolvedAt, 
--     dbo.ufn_GetTicketResolutionHours(TicketID) AS ResolutionHours
FROM TICKET;
GO

-- Stored Procedure: Assign Ticket to Support Staff
IF OBJECT_ID('dbo.sp_AssignTicketToStaff', 'P') IS NOT NULL
    DROP PROCEDURE dbo.sp_AssignTicketToStaff;
GO

CREATE PROCEDURE dbo.sp_AssignTicketToStaff
    @TicketID VARCHAR(10),
    @SupportStaffUserID VARCHAR(10),
    @AssignedByUserID VARCHAR(10)
AS
BEGIN
    SET NOCOUNT ON;
    BEGIN TRANSACTION;
    BEGIN TRY
        -- 1. End any existing active assignment for this ticket
        UPDATE TICKET_ASSIGNMENT
        SET AssignmentStatus = 'Ended',
            UnassignedAt = CURRENT_TIMESTAMP
        WHERE TicketID = @TicketID AND AssignmentStatus = 'Active';

        -- 2. Generate new Assignment ID
        DECLARE @NewAssignID VARCHAR(10);
        SELECT @NewAssignID = 'TKA' + RIGHT('00' + CAST(ISNULL(MAX(CAST(SUBSTRING(AssignmentID, 4, 10) AS INT)), 0) + 1 AS VARCHAR(5)), 3)
        FROM TICKET_ASSIGNMENT;

        -- 3. Create new active assignment
        INSERT INTO TICKET_ASSIGNMENT (AssignmentID, TicketID, SupportStaffUserID, DepartmentOfficerUserID, AssignedByUserID, AssignedAt, UnassignedAt, AssignmentStatus)
        VALUES (@NewAssignID, @TicketID, @SupportStaffUserID, NULL, @AssignedByUserID, CURRENT_TIMESTAMP, NULL, 'Active');

        -- 4. Update Ticket status
        UPDATE TICKET
        SET Status = 'In Progress',
            UpdatedAt = CURRENT_TIMESTAMP
        WHERE TicketID = @TicketID;

        -- 5. Add Ticket Audit History
        DECLARE @NewHistID VARCHAR(10);
        SELECT @NewHistID = 'HIS' + RIGHT('00' + CAST(ISNULL(MAX(CAST(SUBSTRING(HistoryID, 4, 10) AS INT)), 0) + 1 AS VARCHAR(5)), 3)
        FROM TICKET_HISTORY;

        INSERT INTO TICKET_HISTORY (HistoryID, TicketID, ChangedByUserID, ActionType, OldStatus, NewStatus, Remarks, ChangedAt)
        VALUES (@NewHistID, @TicketID, @AssignedByUserID, 'Reassigned', 'New', 'In Progress', CONCAT('Reassigned ticket to staff ', @SupportStaffUserID), CURRENT_TIMESTAMP);

        COMMIT TRANSACTION;
        PRINT 'Ticket reassignment completed successfully.';
    END TRY
    BEGIN CATCH
        ROLLBACK TRANSACTION;
        DECLARE @ErrMsg NVARCHAR(4000) = ERROR_MESSAGE();
        RAISERROR(@ErrMsg, 16, 1);
    END CATCH;
END;
GO

-- ============================================================
-- PART F: TRIGGERS
-- ============================================================

-- Trigger 1: Auto Update UpdatedAt Timestamp on Ticket Update
IF OBJECT_ID('dbo.trg_ticket_updated_at', 'TR') IS NOT NULL
    DROP TRIGGER dbo.trg_ticket_updated_at;
GO

CREATE TRIGGER dbo.trg_ticket_updated_at
ON TICKET
AFTER UPDATE
AS
BEGIN
    SET NOCOUNT ON;
    IF NOT UPDATE(UpdatedAt)
    BEGIN
        UPDATE t
        SET UpdatedAt = CURRENT_TIMESTAMP
        FROM TICKET t
        INNER JOIN inserted i ON t.TicketID = i.TicketID;
    END
END;
GO

-- Trigger 2: Automatic Status Audit Log in TICKET_HISTORY
IF OBJECT_ID('dbo.trg_audit_ticket_status_change', 'TR') IS NOT NULL
    DROP TRIGGER dbo.trg_audit_ticket_status_change;
GO

CREATE TRIGGER dbo.trg_audit_ticket_status_change
ON TICKET
AFTER UPDATE
AS
BEGIN
    SET NOCOUNT ON;
    IF UPDATE(Status)
    BEGIN
        INSERT INTO TICKET_HISTORY (HistoryID, TicketID, ChangedByUserID, ActionType, OldStatus, NewStatus, Remarks, ChangedAt)
        SELECT 
            CONCAT('HIS', RIGHT('00' + CAST((ISNULL((SELECT MAX(CAST(SUBSTRING(HistoryID, 4, 10) AS INT)) FROM TICKET_HISTORY), 0) + ROW_NUMBER() OVER (ORDER BY i.TicketID)) AS VARCHAR(5)), 3)),
            i.TicketID,
            i.StudentUserID,
            'Status Change',
            d.Status,
            i.Status,
            CONCAT('Automatic audit: Status changed from ', d.Status, ' to ', i.Status),
            CURRENT_TIMESTAMP
        FROM inserted i
        JOIN deleted d ON i.TicketID = d.TicketID
        WHERE i.Status <> d.Status;
    END
END;
GO

