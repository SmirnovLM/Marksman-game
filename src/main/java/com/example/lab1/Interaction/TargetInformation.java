package com.example.lab1.Interaction;

public class TargetInformation {
    public final double radius;
    public final double speed;

    public double positionX;
    public double positionY;
    public int direction;

    public TargetInformation(double radius, double speed, double positionX, double positionY, int direction) {
        this.radius = radius;
        this.speed = speed;

        this.positionX = positionX;
        this.positionY = positionY;
        this.direction = direction;
    }
}
