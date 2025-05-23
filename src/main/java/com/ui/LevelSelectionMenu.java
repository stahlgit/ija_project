package com.ui;
/*
 * author: xstahl01
 * This class represents the level selection menu in the game. For the user to select a level to play. 
 */

import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.geometry.Pos;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class LevelSelectionMenu {

    private final String difficulty;
    private final Consumer<Void> backAction;
    private final BiConsumer<String, Integer> levelLoader;
    private final Consumer<Parent> animateSwitchRight;

    public LevelSelectionMenu(
        String difficulty,
        Consumer<Void> backAction,
        BiConsumer<String, Integer> levelLoader,
        Consumer<Parent> animateSwitchRight
    ) {
        this.difficulty = difficulty;
        this.backAction = backAction;
        this.levelLoader = levelLoader;
        this.animateSwitchRight = animateSwitchRight;
    }

    public Parent create() {
        VBox layout = new VBox(20);
        layout.setAlignment(Pos.CENTER);

        Label titleLabel = new Label(difficulty + " Levels");
        titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

        GridPane levelGrid = new GridPane();
        levelGrid.setAlignment(Pos.CENTER);
        levelGrid.setHgap(10);
        levelGrid.setVgap(10);

        int levelNumber = 1;
        for (int row = 0; row < 2; row++) {
            for (int col = 0; col < 5; col++) {
                Button levelButton = new Button("Level " + levelNumber);
                final int currentLevel = levelNumber;
                levelButton.setOnAction(e -> levelLoader.accept(difficulty, currentLevel));
                levelButton.setPrefSize(100, 50);
                levelGrid.add(levelButton, col, row);
                levelNumber++;
            }
        }

        Button backButton = new Button("Back to Menu");
        backButton.setOnAction(e -> backAction.accept(null));


        layout.getChildren().addAll(titleLabel, levelGrid, backButton);
        return layout;
    }

    public void show() {
        animateSwitchRight.accept(create());
    }
}
