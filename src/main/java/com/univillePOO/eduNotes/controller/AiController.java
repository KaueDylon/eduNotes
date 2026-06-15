package com.univillePOO.eduNotes.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.univillePOO.eduNotes.entity.QuizQuestion;
import com.univillePOO.eduNotes.entity.Study;
import com.univillePOO.eduNotes.entity.User;
import com.univillePOO.eduNotes.service.AiService;
import com.univillePOO.eduNotes.service.StudyService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Controller
@RequestMapping("/ai")
@RequiredArgsConstructor
public class AiController {

    private final StudyService studyService;
    private final AiService aiService;

    // ──────────────────────────────────────────
    // RESUMO
    // ──────────────────────────────────────────
    @PostMapping("/summary/{id}")
    public String summary(
            @PathVariable Long id,
            HttpSession session,
            Model model) {

        User user = (User) session.getAttribute("loggedUser");
        if (user == null) return "redirect:/";

        Study study = studyService.findByIdAndUser(id, user);

        if (study.getAiSummary() == null || study.getAiSummary().isBlank()) {
            study.setAiSummary(aiService.generateSummary(study.getContent()));
            studyService.save(study);
        }

        model.addAttribute("study", study);
        model.addAttribute("summary", study.getAiSummary());
        return "study-view";
    }

    // ──────────────────────────────────────────
    // INICIAR QUIZ (gera ou regera)
    // ──────────────────────────────────────────
    @PostMapping("/quiz/{id}")
    public String startQuiz(
            @PathVariable Long id,
            HttpSession session,
            Model model) {

        User user = (User) session.getAttribute("loggedUser");
        if (user == null) return "redirect:/";

        Study study = studyService.findByIdAndUser(id, user);


        if (study.getAiSummary() == null || study.getAiSummary().isBlank()) {
            study.setAiSummary(aiService.generateSummary(study.getContent()));
            studyService.save(study);
            study = studyService.findByIdAndUser(id, user);
        }


        String quizJson = aiService.generateQuiz(study.getAiSummary());
        study.setAiQuiz(quizJson);
        studyService.save(study);


        List<QuizQuestion> questions = parseQuiz(quizJson);
        session.setAttribute("quizQuestions", questions);
        session.setAttribute("quizStudyId", id);
        session.setAttribute("quizAnswers", new HashMap<Integer, String>());

        return "redirect:/ai/quiz/" + id + "/question/0";
    }

    // ──────────────────────────────────────────
    // EXIBIR QUESTÃO
    // ──────────────────────────────────────────
    @GetMapping("/quiz/{id}/question/{index}")
    public String showQuestion(
            @PathVariable Long id,
            @PathVariable int index,
            HttpSession session,
            Model model) {

        User user = (User) session.getAttribute("loggedUser");
        if (user == null) return "redirect:/";

        List<QuizQuestion> questions =
                (List<QuizQuestion>) session.getAttribute("quizQuestions");

        if (questions == null) return "redirect:/studies/" + id;

        Study study = studyService.findByIdAndUser(id, user);
        Map<Integer, String> answers =
                (Map<Integer, String>) session.getAttribute("quizAnswers");

        model.addAttribute("study", study);
        model.addAttribute("question", questions.get(index));
        model.addAttribute("questionIndex", index);
        model.addAttribute("totalQuestions", questions.size());
        model.addAttribute("selectedAnswer", answers.getOrDefault(index, ""));
        model.addAttribute("isLast", index == questions.size() - 1);

        return "quiz";
    }

    // ──────────────────────────────────────────
    // RESPONDER QUESTÃO
    // ──────────────────────────────────────────
    @PostMapping("/quiz/{id}/question/{index}")
    public String answerQuestion(
            @PathVariable Long id,
            @PathVariable int index,
            @RequestParam(required = false) String answer,
            @RequestParam String action,
            HttpSession session) {

        List<QuizQuestion> questions =
                (List<QuizQuestion>) session.getAttribute("quizQuestions");

        Map<Integer, String> answers =
                (Map<Integer, String>) session.getAttribute("quizAnswers");

        // Salva a resposta da questão atual
        if (answer != null && !answer.isBlank()) {
            answers.put(index, answer);
            session.setAttribute("quizAnswers", answers);
        }

        // Navega ou finaliza
        if ("finish".equals(action)) {
            return "redirect:/ai/quiz/" + id + "/result";
        }

        int next = "next".equals(action) ? index + 1 : index - 1;
        next = Math.max(0, Math.min(next, questions.size() - 1));

        return "redirect:/ai/quiz/" + id + "/question/" + next;
    }

    // ──────────────────────────────────────────
    // RESULTADO
    // ──────────────────────────────────────────
    @GetMapping("/quiz/{id}/result")
    public String result(
            @PathVariable Long id,
            HttpSession session,
            Model model) {

        User user = (User) session.getAttribute("loggedUser");
        if (user == null) return "redirect:/";

        List<QuizQuestion> questions =
                (List<QuizQuestion>) session.getAttribute("quizQuestions");

        Map<Integer, String> answers =
                (Map<Integer, String>) session.getAttribute("quizAnswers");

        if (questions == null) return "redirect:/studies/" + id;

        Study study = studyService.findByIdAndUser(id, user);

        // Calcula acertos
        int score = 0;
        List<Map<String, Object>> reviewList = new ArrayList<>();

        for (int i = 0; i < questions.size(); i++) {
            QuizQuestion q = questions.get(i);
            String given = answers.getOrDefault(i, "");
            boolean correct = q.getAnswer().equalsIgnoreCase(given);
            if (correct) score++;

            Map<String, Object> item = new LinkedHashMap<>();
            item.put("index", i + 1);
            item.put("question", q.getQuestion());
            item.put("options", q.getOptions());
            item.put("correctAnswer", q.getAnswer());
            item.put("givenAnswer", given.isBlank() ? "Não respondida" : given);
            item.put("correct", correct);
            reviewList.add(item);
        }

        // Limpa sessão do quiz
        session.removeAttribute("quizQuestions");
        session.removeAttribute("quizAnswers");
        session.removeAttribute("quizStudyId");

        model.addAttribute("study", study);
        model.addAttribute("score", score);
        model.addAttribute("total", questions.size());
        model.addAttribute("reviewList", reviewList);

        return "quiz-result";
    }

    // ──────────────────────────────────────────
    // HELPER: parseia o JSON retornado pela IA
    // ──────────────────────────────────────────
    private List<QuizQuestion> parseQuiz(String json) {
        try {
            String clean = json
                    .replaceAll("(?s)```json\\s*", "")
                    .replaceAll("(?s)```\\s*", "")
                    .trim();

            JsonNode root = new ObjectMapper().readTree(clean);
            JsonNode questionsNode = root.get("questions");

            List<QuizQuestion> list = new ArrayList<>();
            for (JsonNode qNode : questionsNode) {
                QuizQuestion q = new QuizQuestion();
                q.setQuestion(qNode.get("question").asText());
                q.setAnswer(qNode.get("answer").asText());

                Map<String, String> options = new LinkedHashMap<>();
                JsonNode optNode = qNode.get("options");
                optNode.fields().forEachRemaining(e ->
                        options.put(e.getKey(), e.getValue().asText()));
                q.setOptions(options);

                list.add(q);
            }
            return list;

        } catch (Exception e) {
            throw new RuntimeException("Erro ao parsear quiz da IA: " + e.getMessage(), e);
        }
    }
}