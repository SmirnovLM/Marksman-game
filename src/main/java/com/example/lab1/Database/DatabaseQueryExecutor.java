package com.example.lab1.Database;

import javafx.util.Pair;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DatabaseQueryExecutor {

    // Проверка - существует ли пользователь в бд в таким именем:
    public static boolean checkUsernameExists(String nickname) throws SQLException {
        try (Connection connection = DatabaseConnector.getConnection()) {
            PreparedStatement preparedStatement = connection.prepareStatement("SELECT username FROM users WHERE username = ?");
            preparedStatement.setString(1, nickname);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                return resultSet.next();
            }
        }
    }

    // Добавление пользователя в бд:
    public static void addUser(String username) throws SQLException {
        try (Connection connection = DatabaseConnector.getConnection()) {
            PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO users (username) VALUES (?)");
            preparedStatement.setString(1, username);
            preparedStatement.executeUpdate();
        }
    }

    // Получение количества побед пользователя:
    public static int getWinsByUsername(String username) throws SQLException {
        try (Connection connection = DatabaseConnector.getConnection()) {
            PreparedStatement preparedStatement = connection.prepareStatement("SELECT wins FROM users WHERE username = ?");
            preparedStatement.setString(1, username);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                return resultSet.getInt("wins");
            } else {
                throw new SQLException("User not found");
            }
        }
    }

    public static void updateUserWin(String username, int wins) throws SQLException {
        try (Connection connection = DatabaseConnector.getConnection()) {
            PreparedStatement preparedStatement = connection.prepareStatement("UPDATE users SET wins = ? WHERE username = ?");
            preparedStatement.setInt(1, wins);
            preparedStatement.setString(2, username);
            preparedStatement.executeUpdate();
        }
    }

    public static List<Pair<String, Integer>> getLeaderboard() throws SQLException {
        List<Pair<String, Integer>> leaderboard = new ArrayList<>();
        try (Connection connection = DatabaseConnector.getConnection()) {
            PreparedStatement statement = connection.prepareStatement("SELECT * FROM users ORDER BY wins DESC");
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                String username = resultSet.getString("username");
                int wins = resultSet.getInt("wins");
                Pair<String, Integer> user = new Pair<>(username, wins);
                leaderboard.add(user);
            }
        }
        return leaderboard;
    }
}
