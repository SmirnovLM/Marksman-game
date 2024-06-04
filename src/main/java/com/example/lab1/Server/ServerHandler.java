package com.example.lab1.Server;

import com.example.lab1.Client.ClientController;
import com.example.lab1.Interaction.*;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.sql.SQLException;

import static com.example.lab1.Client.ClientController.gson;

public class ServerHandler extends Thread {

    private final ClientController clientController;
    private final Socket clientSocket;
    private final DataInputStream in;
    private final DataOutputStream out;

    public ServerHandler(ClientController clientController, Socket socket,
                         DataInputStream dataInputStream, DataOutputStream dataOutputStream) {
        this.clientController = clientController;
        clientSocket = socket;
        in = dataInputStream;
        out = dataOutputStream;
        setDaemon(true);
        start();
    }

    @Override
    public void run() {
        try {
            handlingMessage();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        finally {
            closeSocket();
        }
    }


    private void handlingMessage() throws IOException {
        while (true) {
            ActionInformation action = gson.fromJson(in.readUTF(), ActionInformation.class);
            switch (action.type()) {
                case NewPlayer -> clientController.addPlayer(
                        gson.fromJson(action.actionInformation(), GameInformation.class));

                case WantToStart -> clientController.setPlayerWantToStart(
                        gson.fromJson(action.actionInformation(), GameInformation.class));

                case WantToPause -> clientController.setPlayerWantToPause(
                        gson.fromJson(action.actionInformation(), GameInformation.class));

                case Status -> clientController.setGameStatus(
                        gson.fromJson(action.actionInformation(), GameStatus.class));

                case Update -> clientController.updateGameInformation(
                        gson.fromJson(action.actionInformation(), GameInformation.class));

                case UpdateTable -> clientController.updateGameInfoTable(
                        gson.fromJson(action.actionInformation(), GameInformation.class));

                case RemovePlayer -> clientController.removePlayer(
                        gson.fromJson(action.actionInformation(), GameInformation.class));

                case Reset -> clientController.resetGameInformation(
                        gson.fromJson(action.actionInformation(), GameInformation.class));

                case Winner -> clientController.showWinner(
                        gson.fromJson(action.actionInformation(), ClientInformation.class));

                case ServerError -> clientController.showServerError(action.actionInformation());
            }
        }
    }

    private void closeSocket() {
        try {
            clientSocket.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void sendMessage(String msg) {
        try {
            out.writeUTF(msg);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
