package com.univillePOO.eduNotes.controller;

import com.univillePOO.eduNotes.entity.Study;
import com.univillePOO.eduNotes.entity.User;
import com.univillePOO.eduNotes.repository.CategoryRepository;
import com.univillePOO.eduNotes.service.StudyService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/studies")
@RequiredArgsConstructor
public class StudyController {

    private final StudyService studyService;
    private final CategoryRepository categoryRepository;

    @GetMapping("/new")
    public String newStudy(Model model, HttpSession session) {

        User user = (User) session.getAttribute("loggedUser");
        if (user == null) {
            return "redirect:/";
        }

        model.addAttribute("study", new Study());
        model.addAttribute("categories", categoryRepository.findAll());

        return "study-form";
    }

    @PostMapping
    public String save(
            @ModelAttribute Study study,
            HttpSession session) {

        User user = (User) session.getAttribute("loggedUser");

        if (user == null) {
            return "redirect:/";
        }

        study.setUser(user);
        studyService.save(study);

        return "redirect:/dashboard";
    }

    @GetMapping("/{id}")
    public String details(
            @PathVariable Long id,
            HttpSession session,
            Model model) {

        User user = (User) session.getAttribute("loggedUser");

        if (user == null) {
            return "redirect:/";
        }

        Study study = studyService.findByIdAndUser(id, user);

        model.addAttribute("study", study);
        model.addAttribute("summary", study.getAiSummary());

        return "study-view";
    }
}