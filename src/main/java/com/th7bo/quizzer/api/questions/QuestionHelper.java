package com.th7bo.quizzer.api.questions;

import com.th7bo.quizzer.Quizzer;
import com.th7bo.quizzer.utils.Misc;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

public class QuestionHelper {

    Quizzer instance;
    ArrayList<Category> categories = new ArrayList<Category>();

    @Getter @Setter
    long started = 0;
    @Getter
    BossBar bossbar;

    @Getter @Setter
    Question currentQuestion = null;

    BukkitTask run;
    BukkitTask main;

    public QuestionHelper() {
        instance = Quizzer.getInstance();
        bossbar = Bukkit.createBossBar("test", BarColor.YELLOW, BarStyle.SOLID);
        new BukkitRunnable() {
            @Override
            public void run() {
                startQuestionRunnable();
            }
        }.runTaskLater(instance, 20 * 5);
    }

    private void startQuestionRunnable() {
        startQuestion();
        main = new BukkitRunnable() {
            @Override
            public void run() {
                startQuestion();
            }
        }.runTaskTimerAsynchronously(instance, 20 * 60 * 5, 20 * 60 * 5);
    }

    public void reload() throws IOException {
        currentQuestion = null;
        if(run != null) {
            run.cancel();
            run = null;
        }
        bossbar.removeAll();
        loadConfiguration();
    }

    public void startQuestion() {
        if ((System.currentTimeMillis() - started) / 1000 <= 2) return;
        if(run != null) {
            run.cancel();
            run = null;
            main.cancel();
            main = null;
            startQuestionRunnable();
            return;
        }
        bossbar.removeAll();
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
        ArrayList<String> dots = new ArrayList<>();
        for(int x = 0; x <= currentQuestion.getAnswer().length() - 1; x++) {
            if (currentQuestion.getAnswer().charAt(x) == ' ') dots.add(" ");
            else dots.add(".");
        }
        StringBuilder title = new StringBuilder();
        for (String s : dots) {
            title.append(s);
        }
        bossbar.setTitle(Misc.Color("&6Hints: &e" + title.toString() + " &7(04:00)"));
        run = new BukkitRunnable() {
            @Override
            public void run() {
                String lastUpdate = "";
                if ((System.currentTimeMillis() - started) / 1000 >= 60 * 4) {
                    bossbar.removeAll();
                    Bukkit.broadcastMessage(Misc.Color(""));
                    Bukkit.broadcastMessage(Misc.Color("&6&lQuizzer &8- &7:("));
                    Bukkit.broadcastMessage(Misc.Color(""));
                    Bukkit.broadcastMessage(Misc.Color("&8| &eNo-one got the answer on time..."));
                    Bukkit.broadcastMessage(Misc.Color("&8| &eAnswer: &f" + currentQuestion.getAnswer()));
                    Bukkit.broadcastMessage(Misc.Color(""));
                    currentQuestion = null;
                    cancel();
                } else if (currentQuestion == null ) {
                    bossbar.removeAll();
                    cancel();
                } else {
                    Bukkit.getOnlinePlayers().forEach((p) -> {
                        bossbar.addPlayer(p);
                    });

                    long time_left = 60 * 4 - (System.currentTimeMillis() - started) / 1000;
                    int minutes = (int) Math.floor(time_left / 60);
                    long seconds = (time_left) - (minutes * 60L);
                    String secondsString = seconds < 10 ? "0" + seconds : "" + seconds;
                    StringBuilder title = new StringBuilder();
                    if ((seconds == 30 || (seconds == 0 && minutes != 4)) && dots.contains(".") && !lastUpdate.equalsIgnoreCase(minutes + ":" + secondsString)) {
                        int change = new Random().nextInt(dots.size());
                        if (!dots.get(change).equalsIgnoreCase(".")) {
                            change = dots.indexOf(".");
                        }
                        dots.set(change, currentQuestion.getAnswer().charAt(change) + "");
                        for (String s : dots) {
                            title.append(s);
                        }
                        bossbar.setTitle(Misc.Color("&6Hints: &e" + title.toString() + " &7(0" + minutes + ":" + secondsString + ")"));
                        lastUpdate = minutes + ":" + secondsString;
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

    public void startQuestion(String category) {
        if ((System.currentTimeMillis() - started) / 1000 <= 2) return;
        if(run != null) {
            run.cancel();
            run = null;
            main.cancel();
            main = null;
            startQuestionRunnable();
            return;
        }
        AtomicInteger a = new AtomicInteger(-1);
        Category cat = null;
        for (Category cats : categories) {
            if (cats.getName().equalsIgnoreCase(category)) {
                cat = cats;
            }
        }
        if (cat == null) return;
        bossbar.removeAll();
        started = System.currentTimeMillis();
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
            else dots.add(".");
        }
        StringBuilder title = new StringBuilder();
        for (String s : dots) {
            title.append(s);
        }
        bossbar.setTitle(Misc.Color("&6Hints: &e" + title.toString() + " &7(04:00)"));
        run = new BukkitRunnable() {
            @Override
            public void run() {
                String lastUpdate = "";
                if ((System.currentTimeMillis() - started) / 1000 >= 60 * 4) {
                    bossbar.removeAll();
                    Bukkit.broadcastMessage(Misc.Color(""));
                    Bukkit.broadcastMessage(Misc.Color("&6&lQuizzer &8- &7:("));
                    Bukkit.broadcastMessage(Misc.Color(""));
                    Bukkit.broadcastMessage(Misc.Color("&8| &eNo-one got the answer on time..."));
                    Bukkit.broadcastMessage(Misc.Color("&8| &eAnswer: &f" + currentQuestion.getAnswer()));
                    Bukkit.broadcastMessage(Misc.Color(""));
                    currentQuestion = null;
                    cancel();
                } else if (currentQuestion == null ) {
                    bossbar.removeAll();
                    cancel();
                } else {
                    Bukkit.getOnlinePlayers().forEach((p) -> {
                        bossbar.addPlayer(p);
                    });

                    long time_left = 60 * 4 - (System.currentTimeMillis() - started) / 1000;
                    int minutes = (int) Math.floor(time_left / 60);
                    long seconds = (time_left) - (minutes * 60L);
                    String secondsString = seconds < 10 ? "0" + seconds : "" + seconds;
                    StringBuilder title = new StringBuilder();
                    if ((seconds == 30 || (seconds == 0 && minutes != 4)) && dots.contains(".") && !lastUpdate.equalsIgnoreCase(minutes + ":" + secondsString)) {
                        int change = new Random().nextInt(dots.size());
                        if (!dots.get(change).equalsIgnoreCase(".")) {
                            change = dots.indexOf(".");
                        }
                        dots.set(change, currentQuestion.getAnswer().charAt(change) + "");
                        for (String s : dots) {
                            title.append(s);
                        }
                        bossbar.setTitle(Misc.Color("&6Hints: &e" + title.toString() + " &7(0" + minutes + ":" + secondsString + ")"));
                        lastUpdate = minutes + ":" + secondsString;
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
        File folder = new File("plugins", "Quizzer");
        if(!folder.exists()) folder.mkdirs();
        File f = new File(instance.getDataFolder(), "config.yml");
        if(!f.exists()) {
            if(!f.createNewFile()) return this;
        }
        FileConfiguration fileConfiguration = YamlConfiguration.loadConfiguration(f);
        if (fileConfiguration.getConfigurationSection("categories") == null) {
            throw new RuntimeException("Categories are not set-up");
        }
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
