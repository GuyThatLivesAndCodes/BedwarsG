package com.guythatlives.bedwarsg.arena;

public enum GameMode {
    SOLO("Solo", "bedwarsg.play.solo"),
    DOUBLES("Doubles", "bedwarsg.play.doubles"),
    TRIO("3v3v3v3", "bedwarsg.play.3v3v3v3"),
    QUAD("4v4v4v4", "bedwarsg.play.4v4v4v4"),
    DUEL("1v1", "bedwarsg.play.1v1"),
    CUSTOM("Custom", "bedwarsg.play.custom");

    private final String displayName;
    private final String permission;

    GameMode(String displayName, String permission) {
        this.displayName = displayName;
        this.permission = permission;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getPermission() {
        return permission;
    }

    public static GameMode fromString(String name) {
        for (GameMode mode : values()) {
            if (mode.name().equalsIgnoreCase(name) || mode.displayName.equalsIgnoreCase(name)) {
                return mode;
            }
        }
        return null;
    }
}
