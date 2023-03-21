package com.th7bo.quizzer.api.questions;

import com.th7bo.quizzer.Quizzer;
import com.th7bo.quizzer.utils.Misc;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Random;

public class QuestionHelper {

    Quizzer instance;
    ArrayList<Category> categories = new ArrayList<Category>();
    long started;
    @Getter
    BossBar bossbar;


    Question currentQuestion;

    public QuestionHelper() {
        instance = Quizzer.getInstance();
        bossbar = Bukkit.createBossBar("test", BarColor.BLUE, BarStyle.SOLID);
        new BukkitRunnable() {
            @Override
            public void run() {
                startQuestionRunnable();
            }
        }.runTaskLater(instance, 20 * 5);
    }

    private void startQuestionRunnable() {
        startQuestion();
        new BukkitRunnable() {
            @Override
            public void run() {
                startQuestion();
            }
        }.runTaskTimerAsynchronously(instance, 20 * 60 * 5, 20 * 60 * 5);
    }

    private void startQuestion() {
        started = System.currentTimeMillis();
        Category cat = categories.get(new Random().nextInt(categories.size()));
        currentQuestion = cat.getQuestions().get(new Random().nextInt(cat.getQuestions().size()));
        Bukkit.broadcastMessage(Misc.Color(""));
        Bukkit.broadcastMessage(Misc.Color("&6&lQuizzer &8- &7A new quiz has started! &8(" + cat.getName() + ")"));
        Bukkit.broadcastMessage(Misc.Color(""));
        Bukkit.broadcastMessage(Misc.Color("&8| &eQuestion: &f" + currentQuestion.getQuestion()));
        Bukkit.broadcastMessage(Misc.Color("&8| &eLetters: &f" + currentQuestion.getAnswer().length()));
        Bukkit.broadcastMessage(Misc.Color(""));
        Bukkit.getOnlinePlayers().forEach((p) -> {
            bossbar.addPlayer(p);
        });
        ArrayList<String> dots = new ArrayList<String>();
        for(int x = 0; x <= currentQuestion.getAnswer().length() - 1; x++) {
            if (currentQuestion.getAnswer().charAt(x) == ' ') dots.add(" ");
            else dots.add("*");
        }
        StringBuilder title = new StringBuilder();
        for (String s : dots) {
            title.append(s);
        }
        bossbar.setTitle(Misc.Color("&6Hints: &e" + title.toString() + " &7(05:00)"));
        new BukkitRunnable() {
            @Override
            public void run() {
                if ((System.currentTimeMillis() - started) / 1000 >= 60 * 4) {
                    bossbar.removeAll();
                    cancel();
                } else {
                    Bukkit.getOnlinePlayers().forEach((p) -> {
                        bossbar.addPlayer(p);
                    });

                    long time_left = 60 * 5 - (System.currentTimeMillis() - started) / 1000;
                    int minutes = (int) Math.floor(time_left / 60);
                    long seconds = (time_left) - (minutes * 60L);
                    String secondsString = seconds < 10 ? "0" + seconds : "" + seconds;
                    StringBuilder title = new StringBuilder();
                    if (seconds == 30 || (seconds == 0 && minutes != 5)) {
                        System.out.println("yes");
                        int change = new Random().nextInt(dots.size());
                        System.out.println("Change: " + change);
                        dots.forEach((s) -> {
                            System.out.println("String: " + s);
                        });
                        if (!dots.get(change).equalsIgnoreCase("*")) {
                            change = dots.indexOf("*");
                            System.out.println("IndexOf *: " + change);
                        }
                        dots.set(change, currentQuestion.getAnswer().charAt(change) + "");
                        for (String s : dots) {
                            title.append(s);
                        }
                        bossbar.setTitle(Misc.Color("&6Hints: &e" + title.toString() + " &7(0" + minutes + ":" + secondsString + ")"));
                    } else {
                        for (String s : dots) {
                            title.append(s);
                        }
                        bossbar.setTitle(Misc.Color("&6Hints: &e" + title.toString() + " &7(0" + minutes + ":" + secondsString + ")"));
                    }
                }
            }
        }.runTaskTimer(instance, 20, 20);
    }

    public QuestionHelper loadConfiguration() throws IOException {
        File f = new File(instance.getDataFolder(), "config.yml");
        if(!f.exists()) {
            if(!f.createNewFile()) return this;
        }
        FileConfiguration fileConfiguration = YamlConfiguration.loadConfiguration(f);
        if (fileConfiguration.getConfigurationSection("categories") == null) return this;
        for (String category : Objects.requireNonNull(fileConfiguration.getConfigurationSection("categories")).getKeys(false)) {
            ArrayList<Question> questions = new ArrayList<Question>();
            for (String questionString : Objects.requireNonNull(fileConfiguration.getConfigurationSection("categories." + category)).getKeys(false)) {
                String answer = Objects.requireNonNull(Objects.requireNonNull(fileConfiguration.getConfigurationSection("categories." + category)).get(questionString)).toString();
                questions.add(new Question(questionString, answer));
            }
            if (questions.size() == 0) continue;
            categories.add(new Category(category, questions));
        }
        return this;
    }



}
