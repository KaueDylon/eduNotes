package com.univillePOO.eduNotes.entity;

import lombok.*;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class QuizQuestion {

    private String question;

    private Map<String, String> options;

    private String answer;
}