package com.th7bo.quizzer.listeners;

import com.th7bo.quizzer.Quizzer;
import com.th7bo.quizzer.api.questions.QuestionHelper;
import com.th7bo.quizzer.utils.Misc;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import static java.sql.DriverManager.getConnection;

public class ChatListener implements Listener {

    @EventHandler
    public void onChat(AsyncChatEvent e) throws SQLException {
        QuestionHelper questionHelper = Quizzer.getInstance().getQuestionHelper();
        if(questionHelper.getCurrentQuestion() == null) return;

        String message = LegacyComponentSerializer.legacyAmpersand().serialize(e.message());
        if(questionHelper.getCurrentQuestion().getAnswer().equalsIgnoreCase(message)) {
            long time = (System.currentTimeMillis() - questionHelper.getStarted()) / 1000;
            int minutes = (int) Math.floor(time / 60);
            long seconds = (time) - (minutes * 60L);
            String secondsString = seconds < 10 ? "0" + seconds : "" + seconds;
            if (!Quizzer.getInstance().addScore(e.getPlayer(), 1)) {
                e.getPlayer().sendMessage(Misc.Color("&cSomething went wrong, please notify an admin to check console!"));
            }

            Bukkit.broadcastMessage(Misc.Color(""));
            Bukkit.broadcastMessage(Misc.Color("&6&lQuizzer &8- &7Answer has been guessed by " + e.getPlayer().getName() + "!"));
            Bukkit.broadcastMessage(Misc.Color(""));
            Bukkit.broadcastMessage(Misc.Color("&8| &eAnswer: &f" + questionHelper.getCurrentQuestion().getAnswer()));
            Bukkit.broadcastMessage(Misc.Color("&8| &eTime: &f0" + minutes + ":" + secondsString));
            Bukkit.broadcastMessage(Misc.Color("&8| &eScore: &f" + (Quizzer.getInstance().getScore(e.getPlayer()).isPresent() ? Quizzer.getInstance().getScore(e.getPlayer()).getAsInt() : 1)));
            Bukkit.broadcastMessage(Misc.Color(""));
            e.setCancelled(true);
            questionHelper.setCurrentQuestion(null);

        } else if ( StringUtils.getJaroWinklerDistance(message, questionHelper.getCurrentQuestion().getAnswer().toString()) >= 0.9) {
            e.setCancelled(true);
            e.getPlayer().sendMessage(Misc.Color("&6&lQuizzer &8- &7Your message is really close to the answer! &8(" + StringUtils.getJaroWinklerDistance(message, questionHelper.getCurrentQuestion().getAnswer().toString()) * 100 + "%)"));
        }

    }

}
