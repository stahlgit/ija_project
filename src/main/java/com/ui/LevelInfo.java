package com.ui;

public class LevelInfo {
    private final String name;
    private final String layout;
    private final String difficulty;

    public LevelInfo(String name, String layout, String difficulty) {
        this.name = name;
        this.layout = layout;
        this.difficulty = difficulty;
    }

    public String getName() { return name; }
    public String getLayout() { return layout; }
    public String getDifficulty() { return difficulty; }
}

