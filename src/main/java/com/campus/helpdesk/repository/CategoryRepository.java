package com.campus.helpdesk.repository;

import com.campus.helpdesk.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Integer> {

    List<Category> findByStatusOrderByCategoryIdDesc(String status);
}