package com.campus.helpdesk.service;

import com.campus.helpdesk.model.Faculty;
import com.campus.helpdesk.model.User;
import com.campus.helpdesk.repository.FacultyRepository;
import com.campus.helpdesk.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private FacultyRepository facultyRepository;

    @InjectMocks
    private UserService userService;

    private List<User> existingUsers;

    @BeforeEach
    void setUp() {
        existingUsers = new ArrayList<>();
        lenient().when(userRepository.findAll()).thenAnswer(invocation -> existingUsers);
    }

    @Test
    void testGenerateAdminIdFirstUser() {
        String adminId = userService.generateUniversityId("ADMIN", 0);
        assertEquals("ADM1", adminId);
    }

    @Test
    void testGenerateAdminIdIncremental() {
        User u1 = new User();
        u1.setUniversityId("ADM1");
        existingUsers.add(u1);

        User u2 = new User();
        u2.setUniversityId("ADM2");
        existingUsers.add(u2);

        String adminId = userService.generateUniversityId("ADMIN", 0);
        assertEquals("ADM3", adminId);
    }

    @Test
    void testGenerateStaffIdIncremental() {
        User u1 = new User();
        u1.setUniversityId("STF1");
        existingUsers.add(u1);

        String staffId = userService.generateUniversityId("STAFF", 1);
        assertEquals("STF2", staffId);
    }

    @Test
    void testGenerateStudentId() {
        Faculty fac = new Faculty(1, "Faculty of Computing");
        when(facultyRepository.findById(1)).thenReturn(Optional.of(fac));

        LocalDate now = LocalDate.now();
        int year = now.getYear() % 100;
        int month = now.getMonthValue();
        String intake = (month <= 4) ? "10" : (month <= 8) ? "20" : "30";

        String expectedPrefix = String.format("IT%02d%s", year, intake);
        String expectedId = expectedPrefix + "0001";

        String studentId = userService.generateUniversityId("STUDENT", 1);
        assertEquals(expectedId, studentId);

        // Add 100 students to test sequence increment (e.g. 0100)
        User existingStudent = new User();
        existingStudent.setUniversityId(expectedPrefix + "0099");
        existingUsers.add(existingStudent);

        String nextStudentId = userService.generateUniversityId("STUDENT", 1);
        assertEquals(expectedPrefix + "0100", nextStudentId);
    }

    @Test
    void testExtractFacultyCode() {
        assertEquals("IT", userService.extractFacultyCode("Faculty of Computing"));
        assertEquals("IT", userService.extractFacultyCode("Information Technology"));
        assertEquals("EN", userService.extractFacultyCode("Faculty of Engineering"));
        assertEquals("BM", userService.extractFacultyCode("Faculty of Business Management"));
        assertEquals("HS", userService.extractFacultyCode("Humanities and Sciences"));
    }

    @Test
    void testIsEmailExists() {
        when(userRepository.existsByEmailIgnoreCase("nimal@gmail.com")).thenReturn(true);
        assertTrue(userService.isEmailExists("nimal@gmail.com"));
        assertFalse(userService.isEmailExists("unique@gmail.com"));
    }

    @Test
    void testAddUserFailsOnDuplicateEmail() {
        when(userRepository.existsByEmailIgnoreCase("nimal@gmail.com")).thenReturn(true);

        User newUser = new User();
        newUser.setEmail("nimal@gmail.com");
        newUser.setRole("STUDENT");

        boolean result = userService.addUser(newUser, "SUPER_ADMIN");
        assertFalse(result);
        verify(userRepository, never()).save(any(User.class));
    }
}
