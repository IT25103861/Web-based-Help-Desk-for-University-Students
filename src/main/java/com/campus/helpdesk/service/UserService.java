package com.campus.helpdesk.service;

import com.campus.helpdesk.model.User;
import com.campus.helpdesk.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public List<User> getAllUsers() {
        return userRepository.findAll(Sort.by(Sort.Direction.DESC, "userId"));
    }

    public boolean addUser(User user, String creatorRole) {
        if (!"SUPER_ADMIN".equalsIgnoreCase(creatorRole) &&
                ("ADMIN".equalsIgnoreCase(user.getRole()) || "SUPER_ADMIN".equalsIgnoreCase(user.getRole()))) {
            return false;
        }

        user.setAccountStatus("ACTIVE");

        userRepository.save(user);
        return true;
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
