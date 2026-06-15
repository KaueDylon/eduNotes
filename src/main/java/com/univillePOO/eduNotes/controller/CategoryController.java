package com.univillePOO.eduNotes.controller;

import com.univillePOO.eduNotes.entity.Category;
import com.univillePOO.eduNotes.repository.CategoryRepository;
import jakarta.servlet.http.HttpSession;
import com.univillePOO.eduNotes.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryRepository repository;

    @GetMapping
    public String page(Model model, HttpSession session) {

        User user = (User) session.getAttribute("loggedUser");
        if (user == null) {
            return "redirect:/";
        }

        model.addAttribute("categories", repository.findAll());
        model.addAttribute("category", new Category());

        return "categories";
    }

    @PostMapping
    public String save(
            @ModelAttribute Category category,
            HttpSession session) {

        User user = (User) session.getAttribute("loggedUser");
        if (user == null) {
            return "redirect:/";
        }

        repository.save(category);

        return "redirect:/categories";
    }
}