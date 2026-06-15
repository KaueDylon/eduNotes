package com.univillePOO.eduNotes.service;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AiService {

    private final ChatClient chatClient;

    public String generateSummary(String content) {

        String prompt = """
                Faça um resumo organizado do conteúdo abaixo.

                Organize em:
                - Conceitos principais
                - Resumo geral
                - Pontos importantes

                Conteúdo:

                %s
                """.formatted(content);

        return chatClient.prompt()
                .user(prompt)
                .call()
                .content();
    }

    public String generateQuiz(String summary) {

        String prompt = """
                Com base no conteúdo abaixo, gere EXATAMENTE 10 questões de múltipla escolha.
                
                Retorne SOMENTE um JSON válido, sem texto adicional, sem markdown, sem explicações.
                O formato deve ser exatamente este:
                
                {
                  "questions": [
                    {
                      "question": "Texto da pergunta?",
                      "options": {
                        "A": "Texto da opção A",
                        "B": "Texto da opção B",
                        "C": "Texto da opção C",
                        "D": "Texto da opção D"
                      },
                      "answer": "A"
                    }
                  ]
                }
                
                Regras:
                - O campo "answer" deve conter apenas a letra correta (A, B, C ou D)
                - Gere exatamente 10 questões
                - Não inclua nada fora do JSON
                
                Conteúdo:
                
                %s
                """.formatted(summary);

        return chatClient.prompt()
                .user(prompt)
                .call()
                .content();
    }
}