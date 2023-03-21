package com.th7bo.quizzer;

import com.th7bo.quizzer.api.questions.QuestionHelper;
import com.th7bo.quizzer.commands.QuizzerCommand;
import com.th7bo.quizzer.listeners.ChatListener;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.mariadb.jdbc.MariaDbPoolDataSource;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.OptionalInt;
import java.util.OptionalLong;
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
        for(String required : new ArrayList<String>(Arrays.asList("host", "port", "collection", "user", "password"))) {
            if(getConfig().get("database." + required) == null) throw new RuntimeException("database." + required + " is not set!");
        }
        try {
            setupDatabase();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void setupDatabase() throws SQLException {
        try {
            Class.forName("org.mariadb.jdbc.Driver");
            try (Connection conn = getConnection()) {
                if (!conn.isValid(1)) {
                    throw new SQLException("Could not establish database connection.");
                }
            }
        } catch (SQLException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        Connection con = getConnection();
        String sql = "CREATE TABLE IF NOT EXISTS players (\n" +
                "\tuuid VARCHAR(36) NOT NULL,\n" +
                "\tscore INT NOT NULL,\n" +
                "\tPRIMARY KEY (uuid)\n" +
                ") ENGINE=Aria;";
        Statement stmt = con.createStatement();
        stmt.execute(sql);
        con.close();
        stmt.close();
    }

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection("jdbc:mariadb://"+ getConfig().getString("database.host") + ":" + getConfig().getInt("database.port") + "/" + getConfig().getString("database.collection") + "?user=" + getConfig().getString("database.user") + "&password=" + getConfig().getString("database.password"));
    }

    @Override
    public void onDisable() {
        getLogger().log(Level.INFO, "Unloaded successfully!");
        if (getQuestionHelper() != null && getQuestionHelper().getBossbar() != null)
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

        getServer().getPluginManager().registerEvents(new ChatListener(), this);
    }

    public OptionalInt getScore(Player player) {
        try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(
                "SELECT score FROM players WHERE uuid = ?;"
        )) {
            stmt.setString(1, player.getUniqueId().toString());
            ResultSet resultSet = stmt.executeQuery();
            if (resultSet.next()) {
                return OptionalInt.of(resultSet.getInt("score"));
            }
            return OptionalInt.empty();
        } catch (SQLException e) {
            return OptionalInt.empty();
        }
    }

    public OptionalInt getScore(String UUID) {
        try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(
                "SELECT score FROM players WHERE uuid = ?;"
        )) {
            stmt.setString(1, UUID);
            ResultSet resultSet = stmt.executeQuery();
            if (resultSet.next()) {
                return OptionalInt.of(resultSet.getInt("score"));
            }
            return OptionalInt.empty();
        } catch (SQLException e) {
            return OptionalInt.empty();
        }
    }

    public boolean addScore(Player player, int amount) {
        if(!getScore(player).isPresent()) addPlayer(player);
        try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(
                "UPDATE players SET score = ? WHERE uuid = ?"
        )) {
            stmt.setInt(1, getScore(player).getAsInt() + amount);
            stmt.setString(2, player.getUniqueId().toString());
            stmt.execute();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean addScore(String UUID, int amount) {
        if(!getScore(UUID).isPresent()) addPlayer(UUID);
        try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(
                "UPDATE players SET score = ? WHERE uuid = ?"
        )) {
            stmt.setInt(1, getScore(UUID).getAsInt() + amount);
            stmt.setString(2, UUID);
            stmt.execute();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public void addPlayer(Player player) {
        try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(
                "INSERT INTO players(uuid, score) VALUES(?,?)"
        )) {
            stmt.setString(1, player.getUniqueId().toString());
            stmt.setInt(2, 0);
            stmt.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void addPlayer(String UUID) {
        try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(
                "INSERT INTO players(uuid, score) VALUES(?,?)"
        )) {
            stmt.setString(1, UUID);
            stmt.setInt(2, 0);
            stmt.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static Quizzer getInstance() {
        return instance;
    }

}
