package com.th7bo.quizzer.api.questions;

import lombok.Getter;

public class Question {

    @Getter
    String question;
    @Getter
    String answer;

    public Question(String question, String answer) {
        this.question = question;
        this.answer = answer;
    }

}
