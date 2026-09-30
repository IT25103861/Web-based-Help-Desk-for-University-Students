package com.campus.helpdesk.service;

import com.campus.helpdesk.model.Faculty;
import com.campus.helpdesk.repository.FacultyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FacultyService {

    @Autowired
    private FacultyRepository facultyRepository;

    public List<Faculty> getAllFaculties() {
        return facultyRepository.findAll(Sort.by(Sort.Direction.ASC, "facultyName"));
    }

    public boolean addFaculty(String facultyName) {
        Faculty faculty = new Faculty();
        faculty.setFacultyName(facultyName);
        facultyRepository.save(faculty);
        return true;
    }
}