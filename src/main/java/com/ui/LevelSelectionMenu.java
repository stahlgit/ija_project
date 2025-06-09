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

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.control.TableView;
import javafx.scene.control.TableColumn;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;


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

        String title = difficulty.equals("Random") ? "Random Saved Levels" : difficulty + " Levels";
        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

        layout.getChildren().add(titleLabel);

        if (difficulty.equals("Random")) {
            TableView<LevelInfo> table = new TableView<>();

            TableColumn<LevelInfo, String> nameCol = new TableColumn<>("Level Name");
            nameCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getName()));

            TableColumn<LevelInfo, String> layoutCol = new TableColumn<>("Layout");
            layoutCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getLayout()));

            TableColumn<LevelInfo, String> diffCol = new TableColumn<>("Difficulty");
            diffCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getDifficulty()));

            table.getColumns().addAll(nameCol, layoutCol, diffCol);
            table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
            table.setPrefHeight(300);

            Button deleteButton = new Button("Delete Selected Level");
            deleteButton.setOnAction(e -> {
                LevelInfo selected = table.getSelectionModel().getSelectedItem();
                if (selected == null) return;

                String levelName = selected.getName(); // e.g., "level3"
                int levelNumber = Integer.parseInt(levelName.replaceAll("[^\\d]", ""));

                try {
                    // Delete .log file
                    Path logPath = Paths.get("data", "log", "Random", levelName + ".log");
                    Files.deleteIfExists(logPath);

                    // Delete .txt file
                    Path layoutPath = Paths.get("data", "level_layout", "random", "level" + levelNumber + ".txt");
                    Files.deleteIfExists(layoutPath);

                    // Remove from table
                    table.getItems().remove(selected);
                } catch (IOException ex) {
                    System.err.println("Failed to delete files for level " + levelName + ": " + ex.getMessage());
                }
            });


            // Load actual random levels
            File dir = Paths.get("data", "log", "Random").toFile();
            File[] files = dir.listFiles((d, name) -> name.matches("level\\d+\\.log"));

            if (files != null) {
                for (File file : files) {

                    List<String> lines = null;
                    try {
                        lines = Files.readAllLines(file.toPath());
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    String name = file.getName().replace(".log", "");
                    String layoutSize = lines.stream().filter(l -> l.startsWith("grid:")).findFirst().orElse("grid:[?,?]").replace("grid:[", "").replace("]", "").replace(",","x");
                    String diffLine = lines.stream().filter(l -> l.startsWith("difficulty:")).findFirst().orElse("difficulty: ?");
                    String levelDiff = diffLine.substring("difficulty:".length()).trim();

                    table.getItems().add(new LevelInfo(name, layoutSize, levelDiff));
                }
            }

            // Double click to load
            table.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2) {
                    LevelInfo selected = table.getSelectionModel().getSelectedItem();
                    if (selected != null) {
                        String name = selected.getName(); // e.g., level3
                        int levelNumber = Integer.parseInt(name.replaceAll("[^\\d]", ""));
                        levelLoader.accept(difficulty, levelNumber);
                    }
                }
            });

            layout.getChildren().addAll(table);
            layout.getChildren().add(deleteButton);

        }
        else{

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
            layout.getChildren().add(levelGrid);
        }

        Button backButton = new Button("Back to Menu");
        backButton.setOnAction(e -> backAction.accept(null));


        layout.getChildren().addAll(backButton);
        return layout;
    }

    public void show() {
        animateSwitchRight.accept(create());
    }
}
