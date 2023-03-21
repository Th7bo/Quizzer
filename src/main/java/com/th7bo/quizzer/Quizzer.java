package com.th7bo.quizzer;

import com.th7bo.quizzer.api.questions.QuestionHelper;
import com.th7bo.quizzer.commands.QuizzerCommand;
import lombok.Getter;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.util.logging.Level;

public final class Quizzer extends JavaPlugin {

    private static Quizzer instance;

    @Getter
    private QuestionHelper questionHelper;

    @Override
    public void onEnable() {
        instance = this;
        registerCommands();
        registerListeners();
        getLogger().log(Level.INFO, "Loaded successfully!");
    }

    @Override
    public void onDisable() {
        getLogger().log(Level.INFO, "Unloaded successfully!");
        getQuestionHelper().getBossbar().removeAll();
    }

    public void registerCommands() {
        new QuizzerCommand();
    }

    public void registerListeners() {
        try {
            questionHelper = new QuestionHelper()
                    .loadConfiguration();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static Quizzer getInstance() {
        return instance;
    }

}
