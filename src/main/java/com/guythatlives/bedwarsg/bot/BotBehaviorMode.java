package com.guythatlives.bedwarsg.bot;

public enum BotBehaviorMode {
    /**
     * Bot focuses on gathering resources and staying near base
     */
    PASSIVE,

    /**
     * Bot actively seeks combat and attempts to break enemy beds
     */
    AGGRESSIVE,

    /**
     * Bot defends own bed and base area
     */
    DEFENSIVE
}
