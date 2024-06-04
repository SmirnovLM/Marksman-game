package com.example.lab1.Server;

import com.example.lab1.Client.ClientHandler;
import com.example.lab1.Database.DatabaseQueryExecutor;
import com.example.lab1.Interaction.*;
import com.google.gson.Gson;

import java.io.IOException;
import java.net.ServerSocket;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static com.example.lab1.Interaction.GameInformation.*;


public class Server {
    public static final int PORT = 36363;
    public static final Gson gson = new Gson();
    private static final Random rand = new Random();

    private static final List<String> colors = new ArrayList<>(List.of(
            "#dc8a78", "#dd7878", "#ea76cb", "#8839ef", "#d20f39",
            "#fe640b", "#df8e1d", "#40a02b", "#f0e68c", "#00c957", "#8b4513"));

    private static final double height = 490, width = 600;

    private GameStatus gameStatus = GameStatus.OFF;
    private static final int WINNING_SCORE_THRESHOLD = 5;

    private final GameInformation gameInformation = new GameInformation();
    private final List<ClientHandler> handlerList = new ArrayList<>();

    private Thread gameAnimThread;

    // Запуск Сервера
    public static void main(String[] args) {
        Server server = new Server();
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                new ClientHandler(server, serverSocket.accept());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // Доступность указанного никнейма для игры:
    public boolean isNicknameAvailable(String nickname) {
        for (ClientInformation p : gameInformation.playersList) {
            if (p.userNickname.equals(nickname)) return false;
        }
        return true;
    }

    // Игра в процессе?:
    public boolean isGameStarted() {
        return gameStatus == GameStatus.ON || gameStatus == GameStatus.PAUSE;
    }

    // Доступность игровой позиции:
    public boolean isGameFull() {
        return gameInformation.playersList.size() >= 4;
    }



    // Добавление клиента в игру:
    public void addPlayer(String nickname, ClientHandler handler) throws IOException, SQLException {
        int indexColor = rand.nextInt(colors.size());
        String color = colors.get(indexColor);
        colors.remove(indexColor);

        ClientInformation newPlayer = new ClientInformation(nickname, color);

        if(DatabaseQueryExecutor.checkUsernameExists(nickname)) {
            newPlayer.userWinsCount = DatabaseQueryExecutor.getWinsByUsername(nickname);
        } else {
            DatabaseQueryExecutor.addUser(nickname);
        }

        gameInformation.playersList.add(newPlayer);
        handler.clientInformation = newPlayer;
        handlerList.add(handler);

        calculatePlayerHeightPositions();

        sendGameInformation(ActionInformation.Type.NewPlayer);
    }

    // Удаление игрока, отключившегося от сервера:
    public void removePlayer(ClientHandler handler) {
        handlerList.remove(handler);
        ClientInformation removedClient = handler.clientInformation;
        gameInformation.playersList.remove(removedClient);
        handler.clientInformation = null;

        calculatePlayerHeightPositions();
        sendGameInformation(ActionInformation.Type.RemovePlayer);

        //System.out.println(handlerList.size());
        if (handlerList.size() == 0) {
            gameStatus = GameStatus.OFF;
        }

        if (gameAnimThread != null && gameAnimThread.isAlive()) {
            gameAnimThread.interrupt();
        } else {
            startGameProcess();
        }
    }


    // Расчет месторасположения игроков на игровом поле:
    private void calculatePlayerHeightPositions() {
        double heightValue = height / (gameInformation.playersList.size() + 1);
        for (int i = 0; i < gameInformation.playersList.size(); i++) {
            gameInformation.playersList.get(i).userHeightPosition = (i + 1) * heightValue;
        }
    }

    // Все игроки хотят играть? или хотят приостановить игру?
    private boolean allPlayersWantPerformedAction(ActionInformation.Type actionType) {
        return gameInformation.playersList.stream().allMatch(player -> switch (actionType) {
            case WantToStart -> player.userWantToStart;
            case WantToPause -> player.userWantToPause;
            default -> false;
        });
    }


    // Отправка Игрового Статуса:
    private void sendGameStatus() {
        String json = gson.toJson(new ActionInformation(ActionInformation.Type.Status, gson.toJson(gameStatus)));
        for (ClientHandler h : handlerList) {
            h.sendMessage(json);
        }
    }

    // Отправка Ошибки Сервера:
    private void sendServerError(String e) {
        String json = gson.toJson(new ActionInformation(ActionInformation.Type.ServerError, e));
        for (ClientHandler h : handlerList) {
            h.sendMessage(json);
        }
    }

    // Отправка Игровой Информации:
    public void sendGameInformation(ActionInformation.Type type) {
        String json = gson.toJson(new ActionInformation(type, gson.toJson(gameInformation)));
        for (ClientHandler h : handlerList) {
            h.sendMessage(json);
        }
    }

    // Отправка Победителя:
    private void sendWinner(ClientInformation clientInformation) {
        String json = gson.toJson(new ActionInformation(ActionInformation.Type.Winner, gson.toJson(clientInformation)));
        for (ClientHandler h : handlerList) {
                h.sendMessage(json);
        }

    }



    // Старт Игрового процесса:
    public void startGameProcess() {
        if (allPlayersWantPerformedAction(ActionInformation.Type.WantToStart) &&
            !gameInformation.playersList.isEmpty()) {
            gameStatus = GameStatus.ON;
            sendGameStatus();
            gameAnimThread = new Thread(() -> {
                try {
                    while (!isGameOver()) {
                        if (gameStatus == GameStatus.PAUSE) pauseThread();
                        processNextStep();
                        sendGameInformation(ActionInformation.Type.Update);
                        Thread.sleep(16);
                    }
                } catch (InterruptedException e) {
                    sendServerError(String.valueOf(e));
                } finally {
                    resetGameInformation();
                    sendGameInformation(ActionInformation.Type.Reset);
                    gameStatus = GameStatus.OFF;
                    sendGameStatus();
                }
            });
            gameAnimThread.setDaemon(true);
            gameAnimThread.start();
        }
    }

    // Управление процессом игры: посыл к остановке или приостановлению:
    public void pauseGameProcess() {
        if (gameStatus == GameStatus.PAUSE && allPlayersWantPerformedAction(ActionInformation.Type.WantToStart)) {
            gameStatus = GameStatus.ON;
            sendGameStatus();
            resumeThread();
        } else if (gameStatus == GameStatus.ON && allPlayersWantPerformedAction(ActionInformation.Type.WantToPause)) {
            gameStatus = GameStatus.PAUSE;
            sendGameStatus();
        }
    }

    // Проверка на достижения конца игры кем-то из игроков:
    private boolean isGameOver() {
        for (ClientInformation p : gameInformation.playersList) {
            if (p.userScore > WINNING_SCORE_THRESHOLD) {
                p.userWinsCount++;
                sendWinner(p);
                try {
                    DatabaseQueryExecutor.updateUserWin(p.userNickname, p.userWinsCount);
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
                return true;
            }

        }
        return false;
    }

    // Обработки следующего шага игрового процесса:
    private void processNextStep() {
        moveTarget(gameInformation.bigTarget);
        moveTarget(gameInformation.smallTarget);
        for (ClientInformation p : gameInformation.playersList) {
            if (p.userHasShoot) {

                // Обновляем координаты начала и конца стрелы
                p.arrow.xPosition += ArrowInformation.ARROW_SPEED;
                double arrowEndX = p.arrow.xPosition + ArrowInformation.ARROW_WIGHT;
                double arrowEndY = p.userHeightPosition;

                // Проверяем попадание стрелы
                if (checkHit(arrowEndX, arrowEndY, gameInformation.bigTarget)) {
                    ++p.userScore;
                    p.userHasShoot = false;
                    p.arrow.xPosition = ArrowInformation.DEFAULT_ARROW_X;
                    sendGameInformation(ActionInformation.Type.UpdateTable);
                } else if (checkHit(arrowEndX, arrowEndY, gameInformation.smallTarget)) {
                    p.userScore += 2;
                    p.userHasShoot = false;
                    p.arrow.xPosition = ArrowInformation.DEFAULT_ARROW_X;
                    sendGameInformation(ActionInformation.Type.UpdateTable);
                } else if (p.arrow.xPosition + ArrowInformation.ARROW_WIGHT > width) {
                    p.userHasShoot = false;
                    p.arrow.xPosition = ArrowInformation.DEFAULT_ARROW_X;
                }
            }
        }
    }

    // Изменение координат для движения каждой из мишеней:
    private void moveTarget(TargetInformation target) {
        if (target.positionY + target.radius + target.speed > height ||
                target.positionY - target.radius - target.speed < 0.0)
            target.direction *= -1;

        target.positionY += target.direction * target.speed;
    }

    // Проверки попадания стрелы в одну из мишеней:
    boolean checkHit(double arrowEndX, double arrowEndY, TargetInformation target) {
        double distanceToTargetCenter = Math.sqrt(Math.pow(arrowEndX - target.positionX, 2) + Math.pow(arrowEndY - target.positionY, 2));
        return distanceToTargetCenter <= target.radius;
    }

    // Сброс информации после завершения игры:
    private void resetGameInformation() {
        for (ClientInformation clientInformation : gameInformation.playersList) {
            clientInformation.userScore = 0;
            clientInformation.userShotsCount = 0;
            clientInformation.userHasShoot = false;
            clientInformation.userWantToPause = false;
            clientInformation.userWantToStart = false;
            clientInformation.arrow.xPosition = ArrowInformation.DEFAULT_ARROW_X;
        }

        gameInformation.bigTarget.positionY = BIG_TARGET_DEFAULT_Y;
        gameInformation.bigTarget.direction = BIG_TARGET_DIRECTION;

        gameInformation.smallTarget.positionY = SMALL_TARGET_DEFAULT_Y;
        gameInformation.smallTarget.direction = SMALL_TARGET_DIRECTION;
    }


    // Управление Потоком: Приостановка и Возобновление действия:
    synchronized void pauseThread() throws InterruptedException {this.wait();}
    synchronized void resumeThread() {this.notifyAll();}
}


