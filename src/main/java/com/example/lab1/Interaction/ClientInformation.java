package com.example.lab1.Interaction;

public class ClientInformation {
    public String userNickname;
    public String userIconColor;

    public int userScore;
    public int userShotsCount;
    public int userWinsCount;

    public double userHeightPosition;

    public boolean userWantToPause = false;
    public boolean userWantToStart = false;
    public boolean userHasShoot = false;

    public ArrowInformation arrow;

    public ClientInformation(String userNickname, String userIconColor) {
        this.userNickname = userNickname;
        this.userIconColor = userIconColor;
        arrow = new ArrowInformation(ArrowInformation.DEFAULT_ARROW_X, userHeightPosition);
    }

}

