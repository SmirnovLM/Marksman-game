package com.example.lab1;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;

public class HelloController {
    private Thread animationTargets;
    private int SpeedTargetBig = 2;
    private int SpeedTargetSmall = 4;
    private boolean animationRunningFlag = true;

    private int shots = 0;
    private int scale = 0;

    @FXML
    private AnchorPane GameArea;
    @FXML
    private Label ScaleLabel;
    @FXML
    private Button ShotButton;
    @FXML
    private Label ShotsLabel;
    @FXML
    private Button StartButton;
    @FXML
    private Button StopButton;
    @FXML
    private Button ContinueButton;

    @FXML
    private Circle TargetBig;

    @FXML
    private Circle TargetSmall;

    @FXML
    void StartButtonClick(ActionEvent event) {

        scale = 0; shots = 0;
        ScaleLabel.setText(String.valueOf(scale));
        ShotsLabel.setText(String.valueOf(shots));

        if(animationTargets == null) {

            animationTargets = new Thread(() -> {
                while (true) {

                    synchronized (this) {
                        while (!animationRunningFlag) {
                            try {
                                wait();
                            } catch (InterruptedException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    }

                    Platform.runLater(() ->
                    {
                        double newY = TargetSmall.getLayoutY() + SpeedTargetSmall;
                        double radius = TargetSmall.getRadius();
                        double heightArea = GameArea.getHeight();

                        if (newY <= (0 + radius) || newY >= (heightArea - radius)) {
                            SpeedTargetSmall *= -1;
                        }
                        TargetSmall.setLayoutY(newY);

                        newY = TargetBig.getLayoutY() + SpeedTargetBig;
                        radius = TargetBig.getRadius();
                        if (newY <= (0 + radius) || newY >= (heightArea - radius)) {
                            SpeedTargetBig *= -1;
                        }
                        TargetBig.setLayoutY(newY);
                    });
                    try {
                        java.lang.Thread.sleep(20);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            });
            animationTargets.start();
        }
    }

    @FXML
    void StopButtonClick(ActionEvent event) {
        synchronized (this) {
            animationRunningFlag = false;
        }
    }

    @FXML
    void ContinueButtonClick(ActionEvent event) {
        synchronized (this) {
            animationRunningFlag = true;
            notifyAll(); // Оповещаем поток о возобновлении анимации
        }
    }

    @FXML
    void ShotButtonClick(ActionEvent event) {
        ShotsLabel.setText(String.valueOf(++shots));
        Rectangle arrow = new Rectangle(30,3, Color.BLACK);
        arrow.setLayoutX(30);
        arrow.setLayoutY(GameArea.getHeight() / 2);

        GameArea.getChildren().add(arrow);

        new Thread(() -> {
            while (arrow.getParent() != null) {

                synchronized (this) {
                    while (!animationRunningFlag) {
                        try {
                            wait();
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }

                Platform.runLater(() -> {
                    double newX = arrow.getLayoutX() + 2;
                    if (newX > GameArea.getWidth() - 50) {
                        GameArea.getChildren().remove(arrow);
                    }
                    arrow.setLayoutX(newX);

                    if (arrow.getBoundsInParent().intersects(TargetBig.getBoundsInParent())) {
                        //if (arrow.getLayoutX() + arrow.getWidth() < TargetBig.getCenterX()) {
                            scale += 1;
                            ScaleLabel.setText(String.valueOf(scale));
                            GameArea.getChildren().remove(arrow);
                        //}
                    }

                    if (arrow.getBoundsInParent().intersects(TargetSmall.getBoundsInParent())) {
                        scale += 2;
                        ScaleLabel.setText(String.valueOf(scale));
                        GameArea.getChildren().remove(arrow);
                    }
                });

                try {
                    Thread.sleep(20);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }).start();
    }

}


