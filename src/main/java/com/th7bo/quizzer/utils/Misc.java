package com.th7bo.quizzer.utils;

import com.th7bo.quizzer.Quizzer;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.entity.Player;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class Misc {

    public static String translateHexCodes(String textToTranslate) {
        Pattern HEX_PATTERN = Pattern.compile("&#(\\w{5}[0-9a-fA-F])");

        Matcher matcher = HEX_PATTERN.matcher(textToTranslate);
        StringBuilder buffer = new StringBuilder();

        while (matcher.find())
            matcher.appendReplacement(buffer, ChatColor.of("#" + matcher.group(1)).toString().toLowerCase(Locale.ROOT));

        return ChatColor.translateAlternateColorCodes('&', matcher.appendTail(buffer).toString());
    }

    public static String Color(String s) {
        return translateHexCodes(s);
    }

    public static String[] Color(String[] s){
        ArrayList<String> strings = new ArrayList<>();
        for(String string : s){
            strings.add(Color(string));
        }
        return strings.toArray(new String[0]);
    }

    public static List<String> Color(List<String> s){
        List<String> strings = new ArrayList<>();
        for(String string : s){
            strings.add(Color(string));
        }
        return strings;
    }
}