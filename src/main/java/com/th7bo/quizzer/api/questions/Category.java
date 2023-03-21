package com.th7bo.quizzer.api.questions;

import lombok.Getter;

import java.util.ArrayList;

public class Category {

    @Getter
    private String name;

    @Getter
    private ArrayList<Question> questions;

    public Category(String name, ArrayList<Question> questions) {
        this.name = name;
        this.questions = questions;
    }

}
