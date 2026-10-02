package com.campus.helpdesk.controller;

import com.campus.helpdesk.model.Category;
import com.campus.helpdesk.model.User;
import com.campus.helpdesk.service.CategoryService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.dao.DataIntegrityViolationException;
import java.util.List;

@Controller
@RequestMapping("/admin/category")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private com.campus.helpdesk.repository.TicketRepository ticketRepository;

    private User checkAdminAuth(HttpSession session) {
        User user = (User) session.getAttribute("loggedUser");
        if (user != null && ("ADMIN".equalsIgnoreCase(user.getRole()) || "SUPER_ADMIN".equalsIgnoreCase(user.getRole()))) {
            return user;
        }
        return null;
    }

    @GetMapping("/list")
    public String listCategories(HttpSession session, Model model) {
        if (checkAdminAuth(session) == null) return "redirect:/login";
        model.addAttribute("categoryList", categoryService.getAllCategories());
        return "manage_categories";
    }

    @PostMapping("/add")
    public String addCategory(@RequestParam("categoryName") String categoryName,
                              @RequestParam("description") String description,
                              @RequestParam("riskLevel") String riskLevel,
                              HttpSession session) {
        if (checkAdminAuth(session) == null) return "redirect:/login";

        Category newCat = new Category();
        newCat.setCategoryName(categoryName);
        newCat.setDescription(description);
        newCat.setRiskLevel(riskLevel);

        try {
            categoryService.addCategory(newCat);
            return "redirect:/admin/category/list";

        } catch (DataIntegrityViolationException e) {
            return "redirect:/admin/category/list?error=duplicateCategory";
        }
    }

    @PostMapping("/edit")
    public String editCategory(@RequestParam("categoryId") int categoryId,
                               @RequestParam("categoryName") String categoryName,
                               @RequestParam("description") String description,
                               @RequestParam("riskLevel") String riskLevel,
                               HttpSession session) {
        if (checkAdminAuth(session) == null) return "redirect:/login";

        Category category = new Category();
        category.setCategoryId(categoryId);
        category.setCategoryName(categoryName);
        category.setDescription(description);
        category.setRiskLevel(riskLevel);

        try {
            categoryService.updateCategory(category);
            return "redirect:/admin/category/list?msg=updated";

        } catch (DataIntegrityViolationException e) {
            return "redirect:/admin/category/list?error=duplicateCategory";
        }
    }

    @PostMapping("/approve")
    public String approveCategory(@RequestParam("categoryId") int categoryId,
                                  @RequestParam("slaHours") int slaHours,
                                  HttpSession session) {
        User loggedUser = checkAdminAuth(session);
        if (loggedUser == null || !"SUPER_ADMIN".equalsIgnoreCase(loggedUser.getRole())) {
            return "redirect:/login";
        }

        categoryService.approveCategory(categoryId, slaHours);
        return "redirect:/admin/category/list?msg=approved";
    }

    @GetMapping("/delete")
    public String deleteCategory(@RequestParam("id") int categoryId, HttpSession session) {

        boolean hasTickets = ticketRepository.existsByCategory_CategoryId(categoryId);
        if (hasTickets) {
            return "redirect:/admin/category/list?error=deleteFailed";
        }
        try {
            categoryService.deleteCategory(categoryId);
            return "redirect:/admin/category/list";
        } catch (Exception e) {
            return "redirect:/admin/category/list?error=deleteFailed";
        }
    }
}