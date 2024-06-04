package com.example.lab1.Client;

import com.example.lab1.Database.DatabaseQueryExecutor;
import com.example.lab1.Interaction.*;
import com.example.lab1.Server.ServerHandler;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polygon;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;
import java.sql.SQLException;
import java.util.List;

import com.google.gson.Gson;
import javafx.stage.Stage;
import javafx.util.Pair;


public class ClientController {

    public static final Gson gson = new Gson();

    @FXML
    private AnchorPane GameArea;
    @FXML
    private Circle TargetBig, TargetSmall;
    @FXML
    private VBox playersTableView;

    ServerHandler serverHandler;
    GameStatus gameStatus = GameStatus.OFF;

    public void connectServer(Socket clientSocket, DataInputStream dataInputStream, DataOutputStream dataOutputStream) {
        serverHandler = new ServerHandler(this, clientSocket, dataInputStream, dataOutputStream);
    }

    // Обработчик клика Кнопки - Готов к игре:
    @FXML
    void ReadyButtonClick(ActionEvent event) {
        if (gameStatus != GameStatus.ON) {
            serverHandler.sendMessage(gson.toJson(ActionInformation.Type.WantToStart));
        }
    }

    // Обработчик клика Кнопки - Готов к паузе:
    @FXML
    void PauseButtonClick(ActionEvent event) {
        if (gameStatus == GameStatus.ON) {
            serverHandler.sendMessage(gson.toJson(ActionInformation.Type.WantToPause));
        }
    }

    // Обработчик клика Кнопки - Выстрел:
    @FXML
    void ShotButtonClick(ActionEvent event) {
        if (gameStatus == GameStatus.ON) {
            serverHandler.sendMessage(gson.toJson(ActionInformation.Type.Shoot));
        }
    }



    // Добавление нового клиента на игровое поле:
    public void addPlayer(final GameInformation p) {
        Platform.runLater(() -> {
            TargetBig.setLayoutY(p.bigTarget.positionY);
            TargetSmall.setLayoutY(p.smallTarget.positionY);

            playersTableView.getChildren().clear();
            GameArea.getChildren().removeIf(node -> node instanceof Polygon);

            for (int i = 0; i < p.playersList.size(); i++) {
                ClientInformation clientInformation = p.playersList.get(i);

                // Создание иконки игрока
                Polygon playerTriangle = new Polygon();
                playerTriangle.setId(clientInformation.userNickname + "Triangle");
                playerTriangle.getPoints().addAll(0.0, -20.0, 0.0, 20.0, 20.0, 0.0); // Установка вершин треугольника
                playerTriangle.setFill(Color.web(clientInformation.userIconColor));
                playerTriangle.setLayoutX(20);
                playerTriangle.setTranslateY(clientInformation.userHeightPosition);
                GameArea.getChildren().add(playerTriangle);


                // Создание строки с информацией об игроке
                Label playerInfoLabel = new Label(
                        clientInformation.userNickname +
                                " | Shoots: " + clientInformation.userShotsCount +
                                " | Score: " + clientInformation.userScore +
                                " | Wins: " + clientInformation.userWinsCount);
                playerInfoLabel.setId(clientInformation.userNickname + "Label");
                playerInfoLabel.setTextFill(Color.web(clientInformation.userIconColor));
                playersTableView.getChildren().add(playerInfoLabel);

            }
        });
    }

    // Удаление клиента с игрового поля:
    public void removePlayer(final GameInformation p) {
        Platform.runLater(() -> {
            playersTableView.getChildren().clear();
            GameArea.getChildren().removeIf(node -> node instanceof Polygon);

            for (int i = 0; i < p.playersList.size(); i++) {
                ClientInformation clientInformation = p.playersList.get(i);

                // Создание иконки игрока
                Polygon playerTriangle = new Polygon();
                playerTriangle.setId(clientInformation.userNickname + "Triangle");
                playerTriangle.getPoints().addAll(0.0, -20.0, 0.0, 20.0, 20.0, 0.0); // Установка вершин треугольника
                playerTriangle.setFill(Color.web(clientInformation.userIconColor));
                playerTriangle.setLayoutX(20);
                playerTriangle.setTranslateY(clientInformation.userHeightPosition);
                GameArea.getChildren().add(playerTriangle);


                // Создание строки с информацией об игроке
                Label playerInfoLabel = new Label(
                        clientInformation.userNickname +
                                " | Shoots: " + clientInformation.userShotsCount +
                                " | Score: " + clientInformation.userScore+
                                " | Wins: " + clientInformation.userWinsCount);
                playerInfoLabel.setId(clientInformation.userNickname + "Label");
                playerInfoLabel.setTextFill(Color.web(clientInformation.userIconColor));
                playersTableView.getChildren().add(playerInfoLabel);
            }
        });
    }



    // Пометка клиента, желающего начать игру:
    public void setPlayerWantToStart(final GameInformation gameInformation) {
        for (ClientInformation player : gameInformation.playersList) {
            if (player.userWantToStart) {
                Polygon playerTriangle = (Polygon) GameArea.getScene().lookup("#" + player.userNickname + "Triangle");
                if (playerTriangle != null) {
                    playerTriangle.setStroke(Color.BLACK);
                }
            }
        }
    }

    // Пометка клиента, желающего приостановить игровой процесс:
    public void setPlayerWantToPause(final GameInformation gameInformation) {
        for (ClientInformation player : gameInformation.playersList) {
            if (player.userWantToPause) {
                Polygon playerTriangle = (Polygon) GameArea.getScene().lookup("#" + player.userNickname + "Triangle");
                if (playerTriangle != null) {
                    playerTriangle.setStroke(Color.RED);
                }
            }
        }
    }



    // Обновление информации на игровом поле:
    public void updateGameInformation(final GameInformation gameInformation) {
        Platform.runLater(() -> {
            TargetBig.setLayoutY(gameInformation.bigTarget.positionY);
            TargetSmall.setLayoutY(gameInformation.smallTarget.positionY);

            // Очистка всех предыдущих стрел перед добавлением новых
            GameArea.getChildren().removeIf(node -> node instanceof Line);

            for (ClientInformation clientInformation : gameInformation.playersList) {
                if (clientInformation.userHasShoot) {
                    // Создание линии стрелы для каждого игрока
                    Line arrowLine = new Line();
                    arrowLine.setStartX(clientInformation.arrow.xPosition);
                    arrowLine.setStartY(clientInformation.userHeightPosition);
                    arrowLine.setEndX(clientInformation.arrow.xPosition + 30);
                    arrowLine.setEndY(clientInformation.userHeightPosition);
                    arrowLine.setStrokeWidth(2);
                    arrowLine.setStroke(Color.web(clientInformation.userIconColor));


                    // Добавление линии стрелы на игровую область
                    GameArea.getChildren().add(arrowLine);
                }
            }
        });
    }

    // Обновление информационной таблицы успеваимости игроков:
    public void updateGameInfoTable(final GameInformation gameInformation) {
        Platform.runLater(() -> {
            for (ClientInformation clientInformation : gameInformation.playersList) {
                Label playerInfoLabel = (Label) playersTableView.lookup("#" + clientInformation.userNickname + "Label");
                if (playerInfoLabel != null) {
                    playerInfoLabel.setText(
                            clientInformation.userNickname +
                                    " | Shoots: " + clientInformation.userShotsCount +
                                    " | Score: " + clientInformation.userScore +
                                    " | Wins: " + clientInformation.userWinsCount);
                }
            }
        });
    }



    // Установка статуса
    public void setGameStatus(final GameStatus status) {
        this.gameStatus = status;
    }

    // Сброс информации после завершения игры:
    public void resetGameInformation(final GameInformation gameInfo) {
        Platform.runLater(() -> {
            TargetBig.setLayoutY(gameInfo.bigTarget.positionY);
            TargetSmall.setLayoutY(gameInfo.smallTarget.positionY);
            for (ClientInformation clientInformation : gameInfo.playersList) {
                Label playerInfoLabel = (Label) GameArea.getScene().lookup("#" + clientInformation.userNickname + "Label");
                if (playerInfoLabel != null) {
                    playerInfoLabel.setText(
                            clientInformation.userNickname +
                                    " | Shoots: " + clientInformation.userShotsCount +
                                    " | Score: " + clientInformation.userScore +
                                    " | Wins: " + clientInformation.userWinsCount);
                    playerInfoLabel.setTextFill(Color.web(clientInformation.userIconColor));
                }

                Polygon playerTriangle = (Polygon) GameArea.getScene().lookup("#" + clientInformation.userNickname + "Triangle");
                if (playerTriangle != null) {
                    playerTriangle.setStroke(null);
                }
            }
        });
    }



    // Уведомление о победителе:
    public void showWinner(final ClientInformation clientInformation) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION,
                    "'" + clientInformation.userNickname + "'" + " - win!\n" +
                            "shots = " + clientInformation.userShotsCount + "\n" +
                            "score = " + clientInformation.userScore + "\n" +
                            "wins = " + clientInformation.userWinsCount);
            alert.showAndWait();
        });
    }

    // Уведомление об ошибке сервера:
    public void showServerError(String e) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.WARNING, "Server Error: " + e);
            alert.showAndWait();
        });
    }


    @FXML
    void ShowLeadersAction(ActionEvent event) throws SQLException {
        TableView<Pair<String, Integer>> tableView = new TableView<>();
        TableColumn<Pair<String, Integer>, String> usernameColumn = new TableColumn<>("Username");
        usernameColumn.setCellValueFactory(new PropertyValueFactory<>("key"));
        TableColumn<Pair<String, Integer>, Integer> winsColumn = new TableColumn<>("Wins");
        winsColumn.setCellValueFactory(new PropertyValueFactory<>("value"));
        tableView.getColumns().addAll(usernameColumn, winsColumn);

        List<Pair<String, Integer>> leaderboard = DatabaseQueryExecutor.getLeaderboard();
        tableView.setItems(FXCollections.observableArrayList(leaderboard));

        Stage stage = new Stage();
        stage.setScene(new Scene(tableView));
        stage.show();
    }

}


