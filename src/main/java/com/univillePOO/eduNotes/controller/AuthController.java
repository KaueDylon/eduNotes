package com.univillePOO.eduNotes.controller;

import com.univillePOO.eduNotes.entity.User;
import com.univillePOO.eduNotes.service.UserService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @GetMapping("/")
    public String loginPage() {
        return "login";
    }

    @GetMapping("/register")
    public String registerPage(Model model) {

        model.addAttribute("user", new User());

        return "register";
    }

    @PostMapping("/register")
    public String register(@ModelAttribute User user) {

        userService.register(user);

        return "redirect:/";
    }

    @PostMapping("/login")
    public String login(
            String email,
            String password,
            HttpSession session) {

        User user =
                userService.login(email, password);

        if (user == null) {
            return "redirect:/";
        }

        session.setAttribute(
                "loggedUser",
                user);

        return "redirect:/dashboard";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {

        session.invalidate();

        return "redirect:/";
    }
}