package com.guythatlives.bedwarsg.bot;

public enum BotDifficulty {
    EASY,
    MEDIUM,
    HARD,
    EXPERT;

    public static BotDifficulty fromString(String str) {
        try {
            return valueOf(str.toUpperCase());
        } catch (IllegalArgumentException e) {
            return MEDIUM;
        }
    }
}
