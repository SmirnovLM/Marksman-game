package com.example.lab1.Interaction;

import java.util.ArrayList;
import java.util.List;

public class GameInformation {
    public static final double BIG_TARGET_DEFAULT_X = 440.0;
    public static final double BIG_TARGET_DEFAULT_Y = 137.0;
    public static final double BIG_TARGET_RADIUS = 40;
    public static final double BIG_TARGET_SPEED = 2;
    public static final int BIG_TARGET_DIRECTION = 1;

    public static final double SMALL_TARGET_DEFAULT_X = 537.0;
    public static final double SMALL_TARGET_DEFAULT_Y = 333.0;
    public static final double SMALL_TARGET_RADIUS = 20;
    public static final double SMALL_TARGET_SPEED = 4;
    public static final int SMALL_TARGET_DIRECTION = -1;

    public final TargetInformation bigTarget;
    public final TargetInformation smallTarget;
    public final List<ClientInformation> playersList = new ArrayList<>();

    public GameInformation() {
        bigTarget = new TargetInformation(
                BIG_TARGET_RADIUS,
                BIG_TARGET_SPEED,
                BIG_TARGET_DEFAULT_X,
                BIG_TARGET_DEFAULT_Y,
                BIG_TARGET_DIRECTION);

        smallTarget = new TargetInformation(
                SMALL_TARGET_RADIUS,
                SMALL_TARGET_SPEED,
                SMALL_TARGET_DEFAULT_X,
                SMALL_TARGET_DEFAULT_Y,
                SMALL_TARGET_DIRECTION);
    }
}
