package com.example.lab1.Client;

import com.example.lab1.Interaction.ActionInformation;
import com.example.lab1.Interaction.ClientInformation;
import com.example.lab1.Server.Server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.sql.SQLException;

import static com.example.lab1.Server.Server.gson;

public class ClientHandler extends Thread {

    private final Server gameServer;
    private final Socket clientSocket;
    private final DataInputStream in;
    private final DataOutputStream out;

    public ClientInformation clientInformation;

    public ClientHandler(Server server, Socket clientSocket) throws IOException {
        gameServer = server;
        this.clientSocket = clientSocket;
        in = new DataInputStream(clientSocket.getInputStream());
        out = new DataOutputStream(clientSocket.getOutputStream());
        setDaemon(true);
        start();
    }

    @Override
    public void run() {
        try {
            checkPlayerAvailabilityAndAdd();
            handlingMessage();
        } catch (IOException | SQLException e) {
            closeSocket();
        }
    }

    private void checkPlayerAvailabilityAndAdd() throws IOException, SQLException {
        String nicknameTextField;
        while (true) {
            nicknameTextField = in.readUTF();
            if (!gameServer.isNicknameAvailable(nicknameTextField)) {
                out.writeUTF(nicknameTextField + " is already in use.");
            } else if (gameServer.isGameStarted() || gameServer.isGameFull()) {
                out.writeUTF("The game has already started or is full");
            } else {
                out.writeUTF("OK");
                gameServer.addPlayer(nicknameTextField, this);
                break;
            }
        }
    }

    private void handlingMessage() throws IOException {
        while (true) {
            String json = in.readUTF();
            ActionInformation.Type actionType = gson.fromJson(json, ActionInformation.Type.class);
            switch (actionType) {
                case WantToStart:
                    clientInformation.userWantToStart = true;
                    clientInformation.userWantToPause = false;
                    gameServer.sendGameInformation(ActionInformation.Type.WantToStart);
                    gameServer.startGameProcess();
                    break;
                case Shoot:
                    if (!clientInformation.userHasShoot) {
                        clientInformation.userShotsCount++;
                        gameServer.sendGameInformation(ActionInformation.Type.UpdateTable);
                    }
                    clientInformation.userHasShoot = true;
                    break;
                case WantToPause:
                    clientInformation.userWantToStart = false;
                    clientInformation.userWantToPause = true;
                    gameServer.sendGameInformation(ActionInformation.Type.WantToPause);
                    gameServer.pauseGameProcess();
                    break;
            }
        }
    }

    private void closeSocket() {
        try {
            clientSocket.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            gameServer.removePlayer(this);
        }
    }

    public void sendMessage(String msg) {
        try {
            out.writeUTF(msg);
        } catch (IOException e) {
            closeSocket();
        }
    }

}
