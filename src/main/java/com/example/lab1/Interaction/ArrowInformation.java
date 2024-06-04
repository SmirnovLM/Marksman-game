package com.example.lab1.Interaction;

public class ArrowInformation {
    public static final double DEFAULT_ARROW_X = 50.0;
    public static final double ARROW_SPEED = 5.0;
    public static final double ARROW_WIGHT = 30;

    public double xPosition;
    public double yPosition;

    ArrowInformation(double initialX, double initialY) {
        this.xPosition = initialX;
        this.yPosition = initialY;
    }
}
