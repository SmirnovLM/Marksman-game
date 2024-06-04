package com.example.lab1.Client;

import com.example.lab1.HelloApplication;
import com.example.lab1.Server.Server;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class AuthorizationController {

    private Socket clientSocket;
    private DataInputStream in;
    private DataOutputStream out;

    @FXML
    private TextField nicknameTextField;

    @FXML
    private void initialize() {
        try {
            clientSocket = new Socket("localhost", Server.PORT);
            in = new DataInputStream(clientSocket.getInputStream());
            out = new DataOutputStream(clientSocket.getOutputStream());
        } catch (IOException e) {
            showAlert("Connection Error", "Failed to connect to the server.");
            throw new RuntimeException(e);
        }
    }

    @FXML
    void EnterToGameClick(ActionEvent event) {
        String nickname = nicknameTextField.getText();
        if (nickname.isEmpty()) {
            showAlert("Input Error", "Please enter a nickname.");
        } else {
            try {
                out.writeUTF(nickname);
                String serverResponse = in.readUTF();
                if ("OK".equals(serverResponse)) {
                    openGameScene();
                } else {
                    showAlert("Server Response", serverResponse);
                }
            } catch (IOException e) {
                showAlert("Connection Error", "Lost connection to the server.");
            }
        }
    }

    private void openGameScene() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("PlayingField.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        Stage stage = (Stage) nicknameTextField.getScene().getWindow();
        stage.setTitle("Game");
        stage.setScene(scene);
        stage.show();

        ClientController clientController = fxmlLoader.getController();
        clientController.connectServer(clientSocket, in, out);
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

}


