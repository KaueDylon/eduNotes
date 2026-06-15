package com.univillePOO.eduNotes.controller;

import com.univillePOO.eduNotes.entity.User;
import com.univillePOO.eduNotes.service.StudyService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class DashboardController {

    private final StudyService studyService;

    @GetMapping("/dashboard")
    public String dashboard(
            HttpSession session,
            Model model) {

        User user =
                (User) session.getAttribute("loggedUser");

        if (user == null) {
            return "redirect:/";
        }

        model.addAttribute(
                "studies",
                studyService.findByUser(user));

        return "dashboard";
    }
}