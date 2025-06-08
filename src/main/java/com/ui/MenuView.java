package com.ui;
/*
 * author: xstahl01
 * This class represents the menu view of the game.
 */


import javafx.scene.layout.VBox;
import javafx.geometry.Pos;
import javafx.scene.control.Button;

import java.util.function.Consumer;
import javafx.scene.text.Text;
import javafx.scene.text.Font;


public class MenuView extends VBox {
        public MenuView(Consumer<String> onDifficultySelected, Runnable onPlayPreloaded) {
        super(20);
        setAlignment(Pos.CENTER);

        Text title = new Text("Bulb Puzzle Game");
        title.setFont(new Font(24));
        getChildren().add(title);

        Button easyButton = createDifficultyButton("Easy", onDifficultySelected);
        Button mediumButton = createDifficultyButton("Medium", onDifficultySelected);
        Button hardButton = createDifficultyButton("Hard", onDifficultySelected);

        Button customBoardButton = new Button("Custom Board");
        customBoardButton.setOnAction(e -> onPlayPreloaded.run());

        getChildren().addAll(easyButton, mediumButton, hardButton, customBoardButton);
    }

    private Button createDifficultyButton(String difficulty, Consumer<String> callback) {
        Button button = new Button(difficulty);
        button.setOnAction(e -> callback.accept(difficulty));
        return button;
    }
}
