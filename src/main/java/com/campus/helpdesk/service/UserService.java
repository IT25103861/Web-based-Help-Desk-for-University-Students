package com.campus.helpdesk.service;

import com.campus.helpdesk.model.Faculty;
import com.campus.helpdesk.model.User;
import com.campus.helpdesk.repository.FacultyRepository;
import com.campus.helpdesk.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FacultyRepository facultyRepository;

    public List<User> getAllUsers() {
        return userRepository.findAll(Sort.by(Sort.Direction.DESC, "userId"));
    }

    public boolean isEmailExists(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        return userRepository.existsByEmailIgnoreCase(email.trim());
    }

    public boolean addUser(User user, String creatorRole) {
        if (!"SUPER_ADMIN".equalsIgnoreCase(creatorRole) &&
                ("ADMIN".equalsIgnoreCase(user.getRole()) || "SUPER_ADMIN".equalsIgnoreCase(user.getRole()))) {
            return false;
        }

        if (isEmailExists(user.getEmail())) {
            return false;
        }

        if (user.getUniversityId() == null || user.getUniversityId().trim().isEmpty()) {
            String autoGenId = generateUniversityId(user.getRole(), user.getFacultyId());
            user.setUniversityId(autoGenId);
        }

        user.setAccountStatus("ACTIVE");

        userRepository.save(user);
        return true;
    }

    public synchronized String generateUniversityId(String role, Integer facultyId) {
        if (role == null) {
            role = "STUDENT";
        }
        String upperRole = role.toUpperCase();

        if ("ADMIN".equalsIgnoreCase(upperRole) || "SUPER_ADMIN".equalsIgnoreCase(upperRole)) {
            return generateAdminId();
        } else if ("STAFF".equalsIgnoreCase(upperRole) || upperRole.contains("OFFICER")) {
            return generateStaffId();
        } else {
            return generateStudentId(facultyId);
        }
    }

    private String generateAdminId() {
        List<User> allUsers = userRepository.findAll();
        int maxSeq = 0;
        for (User u : allUsers) {
            if (u.getUniversityId() != null && u.getUniversityId().toUpperCase().startsWith("ADM")) {
                String numPart = u.getUniversityId().substring(3).trim();
                try {
                    int seq = Integer.parseInt(numPart);
                    if (seq > maxSeq) {
                        maxSeq = seq;
                    }
                } catch (NumberFormatException ignored) {}
            }
        }
        return "ADM" + (maxSeq + 1);
    }

    private String generateStaffId() {
        List<User> allUsers = userRepository.findAll();
        int maxSeq = 0;
        for (User u : allUsers) {
            if (u.getUniversityId() != null && u.getUniversityId().toUpperCase().startsWith("STF")) {
                String numPart = u.getUniversityId().substring(3).trim();
                try {
                    int seq = Integer.parseInt(numPart);
                    if (seq > maxSeq) {
                        maxSeq = seq;
                    }
                } catch (NumberFormatException ignored) {}
            }
        }
        return "STF" + (maxSeq + 1);
    }

    private String generateStudentId(Integer facultyId) {
        String facultyCode = "IT";
        if (facultyId != null && facultyId > 0) {
            Optional<Faculty> facOpt = facultyRepository.findById(facultyId);
            if (facOpt.isPresent()) {
                facultyCode = extractFacultyCode(facOpt.get().getFacultyName());
            }
        }

        LocalDate now = LocalDate.now();
        int yearTwoDigits = now.getYear() % 100;
        String yearStr = String.format("%02d", yearTwoDigits);

        int month = now.getMonthValue();
        String intakeCode;
        if (month <= 4) {
            intakeCode = "10";
        } else if (month <= 8) {
            intakeCode = "20";
        } else {
            intakeCode = "30";
        }

        String prefix = facultyCode + yearStr + intakeCode;

        List<User> allUsers = userRepository.findAll();
        int maxSeq = 0;
        for (User u : allUsers) {
            if (u.getUniversityId() != null && u.getUniversityId().toUpperCase().startsWith(prefix.toUpperCase())) {
                String numPart = u.getUniversityId().substring(prefix.length()).trim();
                try {
                    int seq = Integer.parseInt(numPart);
                    if (seq > maxSeq) {
                        maxSeq = seq;
                    }
                } catch (NumberFormatException ignored) {}
            }
        }

        int nextSeq = maxSeq + 1;
        return prefix + String.format("%04d", nextSeq);
    }

    public String extractFacultyCode(String facultyName) {
        if (facultyName == null || facultyName.trim().isEmpty()) {
            return "IT";
        }
        String name = facultyName.trim();

        if (name.contains("(") && name.contains(")")) {
            String inside = name.substring(name.indexOf("(") + 1, name.indexOf(")")).trim();
            if (inside.length() >= 2) {
                return inside.substring(0, 2).toUpperCase();
            }
        }

        String lower = name.toLowerCase();
        if (lower.contains("computing") || lower.contains("information") || lower.contains("computer") || lower.contains("software") || lower.matches(".*\\b(it|foc)\\b.*")) {
            return "IT";
        } else if (lower.contains("engineering")) {
            return "EN";
        } else if (lower.contains("business") || lower.contains("management")) {
            return "BM";
        } else if (lower.contains("humanities") || lower.contains("sciences") || lower.contains("science")) {
            return "HS";
        } else if (lower.contains("architecture")) {
            return "AR";
        } else if (lower.contains("law")) {
            return "LW";
        }

        String cleaned = name.replaceAll("(?i)\\b(faculty|of|department|school|and|&)\\b", "").trim();
        String[] words = cleaned.split("\\s+");
        StringBuilder code = new StringBuilder();
        for (String w : words) {
            if (!w.isEmpty()) {
                code.append(Character.toUpperCase(w.charAt(0)));
            }
        }
        if (code.length() >= 2) {
            return code.substring(0, 2);
        }
        if (cleaned.length() >= 2) {
            return cleaned.substring(0, 2).toUpperCase();
        }
        return "IT";
    }

    public boolean updateUserStatus(int userId, String newStatus, String modifierRole) {
        Optional<User> optionalUser = userRepository.findById(userId);

        if (optionalUser.isPresent()) {
            User user = optionalUser.get();

            if ("SUPER_ADMIN".equalsIgnoreCase(user.getRole()) && !"SUPER_ADMIN".equalsIgnoreCase(modifierRole)) {
                System.out.println("Security Alert: Normal Admin cannot deactivate a Super Admin account!");
                return false;
            }

            user.setAccountStatus(newStatus);
            userRepository.save(user);
            return true;
        }
        return false;
    }

    public boolean deleteUser(int userId, String creatorRole) {
        if (!"SUPER_ADMIN".equalsIgnoreCase(creatorRole)) {
            return false;
        }

        if (userRepository.existsById(userId)) {
            userRepository.deleteById(userId);
            return true;
        }
        return false;
    }

    public boolean changePassword(int userId, String oldPassword, String newPassword) {
        Optional<User> optionalUser = userRepository.findById(userId);

        if (optionalUser.isPresent()) {
            User user = optionalUser.get();

            if (user.getPasswordHash().equals(oldPassword)) {
                user.setPasswordHash(newPassword);
                userRepository.save(user);
                return true;
            }
        }
        return false;
    }
}
