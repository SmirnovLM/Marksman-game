package com.example.lab1.Interaction;

public record ActionInformation(Type type, String actionInformation) {
    public enum Type {
        NewPlayer,
        WantToStart,
        WantToPause,

        Status,
        Update,
        UpdateTable,
        Shoot,

        Winner,
        Reset,
        RemovePlayer,

        ServerError,
    }
}
