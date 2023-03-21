package com.th7bo.quizzer.commands;

import com.th7bo.quizzer.Quizzer;
import com.th7bo.quizzer.utils.Misc;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class QuizzerCommand implements CommandExecutor, TabCompleter {

    public QuizzerCommand() {
        Objects.requireNonNull(Quizzer.getInstance().getCommand("quizzer")).setExecutor(this);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }
        if (args[0].equalsIgnoreCase("reload")) {
            sender.sendMessage(Misc.Color("&aReloading!"));
            try {
                Quizzer.getInstance().getQuestionHelper().reload();
            } catch (IOException e) {
                sender.sendMessage(Misc.Color("&cSomething went wrong, please notify an admin to check console!"));
                throw new RuntimeException(e);
            }
            new BukkitRunnable() {
                @Override
                public void run() {
                    sender.sendMessage(Misc.Color("&aReloaded!"));
                }
            }.runTaskLater(Quizzer.getInstance(), 40);
        } else if (args[0].equalsIgnoreCase("start")) {
            if(args.length >= 2) {
                StringBuilder cat = new StringBuilder();
                for (int x = 1; x < args.length; x++) {
                    System.out.println(args[x]);
                    cat.append(args[x]);
                    if (x != args.length - 1)
                        cat.append(" ");
                }

                Quizzer.getInstance().getQuestionHelper().startQuestion(cat.toString());
            } else
                Quizzer.getInstance().getQuestionHelper().startQuestion();
        } else if (args[0].equalsIgnoreCase("stats") || args[0].equalsIgnoreCase("score")) {
            OfflinePlayer pl;
            String uuid;
            if (args.length >= 2) {
                uuid = Bukkit.getOfflinePlayer(args[1]).getUniqueId().toString();
                pl = Bukkit.getOfflinePlayer(args[1]);
            } else {
                if(!(sender instanceof Player)) return true;
                uuid = ((Player) sender).getUniqueId().toString();
                pl = Bukkit.getOfflinePlayer(((Player) sender).getUniqueId());
            }
            if (uuid.equalsIgnoreCase("")) {
                sender.sendMessage(Misc.Color("Please specify a valid player!"));
            }
            sender.sendMessage(Misc.Color("&6&lQuizzer &8- &e" + pl.getName() + " &7has &6" + (Quizzer.getInstance().getScore(uuid).isPresent() ? Quizzer.getInstance().getScore(uuid).getAsInt() : 0) + " &7score!"));
        } else {
            sendHelp(sender);
        }
        return true;
    }

    private void sendHelp(@NotNull CommandSender sender) {
        ArrayList<String> text = new ArrayList<>();
        text.add("/quizzer help");
        text.add("/quizzer reload");
        text.add("/quizzer start (category)");
        text.add("/quizzer stats [player]");
        ArrayList<String> hover = new ArrayList<>();
        hover.add("&7Shows this message");
        hover.add("&7Reloads the &econfig&7!");
        hover.add("&7Starts a new event &eand cancels&7 the current one!");
        hover.add("&7Show &escore &7of the given player (defaults to yourself)");

        sender.sendMessage(Misc.Color("&6&lQuizzer"));
        sender.sendMessage(Misc.Color(""));
        int index = -1;
        for(String ignored : text) {
            index += 1;

            final TextComponent textComponent = Component.text(text.get(index))
                    .color(TextColor.color(0xFFD700))
                    .hoverEvent(HoverEvent.showText(Component.text(Misc.Color(hover.get(index)))));
            sender.sendMessage(textComponent);
        }

        sender.sendMessage(Misc.Color(""));
        sender.sendMessage(Misc.Color("&8&oHover for more info!"));
        sender.sendMessage(Misc.Color(""));
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        return null;
    }
}
