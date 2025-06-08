package com.ui;
/*
 * author: xstahl01
 * This class represents the main application for the Light Circuit game. Handles most of the operations (didn't had 
 * time to refractor it. Sorry for the mess.). 
 * 
 */

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import com.common.GameNode;
import com.common.Position;
import com.common.Side;
import com.game.Game;
import com.game.LevelGenerator;
import com.game.LevelLoader;
import com.utils.GameLogger;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.util.Duration;

public class MainApp extends Application {
    private Game game;
    private Stage primaryStage;
    private StackPane root;
    private Runnable backActionOnWin;
    private AnimationHelper animationHelper;
    private GameLogger logger;
    private String currentDifficulty;
    //time label
    private Label timeLabel;
    private long startTime;
    private Timeline timer;
    private Button undoButton;
    private Button redoButton;
    private boolean showHint = false;
    private Timeline hintTimer;
    private boolean isRandomLevel = false;
    private Difficulty selectedRandomDifficulty = null;

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        root = new StackPane();
        Scene scene = new Scene(root, 600, 600);
        primaryStage.setTitle("Light Circuit");
        primaryStage.setScene(scene);
        animationHelper = new AnimationHelper(root); 
        showMenu();
        primaryStage.show();
    }

    private void showMenu() {
        MenuView menu = new MenuView(
            difficulty -> showLevelSelection(difficulty, "right"),
            this::showCustomBoardInput 
        );
        root.getChildren().setAll(menu);
    }
    

    private void showLevelSelection(String difficulty, String direction) {
        this.currentDifficulty = difficulty;
        LevelSelectionMenu levelSelectionMenu = new LevelSelectionMenu(
            difficulty,
            unused -> animationHelper.animateSwitchTo(this::showMenu, "left"),
            this::loadLevel,
            layout -> animationHelper.animateSwitchTo(() -> root.getChildren().setAll(layout), direction)
        );
        levelSelectionMenu.show();
    }
    

    // Start Timer
    private void startTimer() {
        if (timer != null) timer.stop();
        
        if (logger != null && currentDifficulty != null) {
            startTime = System.currentTimeMillis() - logger.getAccumulatedTime();
        } else {
            startTime = System.currentTimeMillis();
        }
        
        timer = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            updateTimerDisplay();
            if (logger != null && currentDifficulty != null) {
                long currentElapsed = System.currentTimeMillis() - startTime;
                logger.saveAccumulatedTime(currentElapsed);
            }
        }));
        timer.setCycleCount(Animation.INDEFINITE);
        timer.play();
    }

    
    // Update Timer Display
    private void updateTimerDisplay() {
        if (timeLabel != null) {
            long currentTime = System.currentTimeMillis() - startTime;
            timeLabel.setText("Time: " + formatTime(currentTime));
        }
    }

    private String formatTime(long millis) {
        long seconds = millis / 1000;
        return String.format("%02d:%02d", seconds / 60, seconds % 60);
    }

    // this function loads the level and applies the moves from the log file
    private void loadLevel(String difficulty, int levelNumber) {
        try {
            this.currentDifficulty = difficulty; 
            Path logPath = Paths.get("data", "log", difficulty, "level" + levelNumber + ".log");
            String filePath = String.format("data/level_layout/%s/level%d.txt", difficulty.toLowerCase(), levelNumber);

            Game originalGame = null;
            try {
                originalGame = LevelLoader.loadLevel(filePath);
                originalGame.saveSolutionState();

                originalGame.recalculateLight(); // optional, in case links affect lighting

            } catch (IOException e) {
                System.err.println("Failed to load original level for hints: " + e.getMessage());
            }

            if (Files.exists(logPath)){
                List<String> logLines = Files.readAllLines(logPath);
                if (logLines.size() <= 3 &&
                logLines.stream().anyMatch(line -> line.startsWith("grid:")) &&
                logLines.stream().anyMatch(line -> line.startsWith("time:")) &&
                logLines.stream().anyMatch(line -> line.startsWith("accumulated:"))) {
                    game = LevelLoader.loadLevel(filePath);
                    LevelGenerator.scrambleLinks(game);
                    game.recalculateLight(); // optional, in case links affect lighting
                    logger = new GameLogger(difficulty, levelNumber, difficulty);
                    logger.logNodeStates(game);
                    String timeLine = logLines.stream().filter(line -> line.startsWith("time:")).findFirst().orElse("time: ");
                    long oldBestTime = 0;
                    if(timeLine.length() > 6){
                        oldBestTime = Long.parseLong(timeLine.substring(6).trim());
                        logger.saveBestTime(oldBestTime);
                    }
                }else{
                    game = LevelLoader.loadLevel(logPath.toString());
                    logger = new GameLogger(difficulty, levelNumber, difficulty);
                    applyLoggedMoves(game, logger);
                }
            } else {
                game = LevelLoader.loadLevel(filePath);
                LevelGenerator.scrambleLinks(game);
                game.recalculateLight(); // optional, in case links affect lighting
                logger = new GameLogger(difficulty, levelNumber, difficulty);
                logger.logNodeStates(game);
            }

            // copy solution state from original level
            if(originalGame != null){
                game.copySolutionState(originalGame);
            }

            this.backActionOnWin = () -> showLevelSelection(difficulty, "left");
            showGameScene();
        } catch (IOException e) {
            System.err.println("Error loading level: " + e.getMessage());
            showMenu();
        }
    }

    // this function creates the game layout with all functionality
    private BorderPane createGameLayout() {
        BorderPane mainLayout = new BorderPane(); 

        HBox topPanel = new HBox(20);
        topPanel.setPadding(new Insets(10));
        topPanel.setAlignment(Pos.CENTER);

        timeLabel = new Label("Time: 00:00");
        Label bestTimeLabel = new Label("Best: " + (logger != null ? logger.getBestTimeFormatted() : "--"));

        undoButton = new Button("Undo");
        redoButton = new Button("Redo");

        undoButton.setStyle("-fx-font-size: 14px; -fx-pref-width: 80px;");
        redoButton.setStyle("-fx-font-size: 14px; -fx-pref-width: 80px;");

        undoButton.setOnAction(e -> undoMove());
        redoButton.setOnAction(e -> redoMove());


        Button hinButton = new Button("Hint");
        hinButton.setStyle("-fx-font-size: 14px; -fx-pref-width: 80px;");
        // hinButton.setDisable(currentDifficulty == null); // Disable if no level is loaded
        hinButton.setOnAction(e-> {
            if (hintTimer != null) hintTimer.stop(); // Reset timer on re-click
    
            showHint = true;
            refreshGrid();
            
            hintTimer = new Timeline(
                new KeyFrame(Duration.seconds(0.3), // Adjust duration here
                event -> {
                    showHint = false;
                    refreshGrid();
                })
            );
            hintTimer.play();
        });


        GridPane gameGrid = new GridPane();
        gameGrid.setAlignment(Pos.CENTER);
        gameGrid.setPadding(new Insets(20)); 

        for(int r = 1; r<=game.rows(); r++){
            for(int c = 1; c<=game.cols(); c++){
                Position pos = new Position(r, c);
                GameNode node = game.node(pos);
                boolean isSolutionNode = game.getSolutionNodes().contains(pos);
                GameCell cell = new GameCell(
                        node,
                        this::refreshGrid,
                        logger,
                        showHint ? node.getRotations() : 0,
                        isSolutionNode);
                gameGrid.add(cell, c-1, r-1);
            }
        }

        // Create back button
        Button backButton = new Button("Back");
        backButton.setOnAction(e -> {
            handleLevelExit();
            if (timer != null) {
                timer.stop();
            }
            if (currentDifficulty != null) {
                showLevelSelection(currentDifficulty, "left");
            } else {
                animationHelper.animateSwitchTo(this::showMenu, "left");
            }
        });

        backButton.setStyle("-fx-font-size: 14px; -fx-pref-width: 100px;");
        BorderPane.setAlignment(backButton, Pos.TOP_LEFT);
        BorderPane.setMargin(backButton, new Insets(10));

        // Add components to main layout
        topPanel.getChildren().addAll(timeLabel, bestTimeLabel, backButton, undoButton, redoButton, hinButton);
        updateUndoRedoButtons();
        mainLayout.setCenter(gameGrid);
        mainLayout.setTop(topPanel); 
        return mainLayout;
    }

    // this function updates the undo and redo buttons
    private void updateUndoRedoButtons() {
        undoButton.setDisable(logger == null || logger.isUndoEmpty());
        redoButton.setDisable(logger == null || logger.isRedoEmpty());
    }
    // this function undoes the last move
    private void undoMove() {
        if (logger != null) {
            Position pos = logger.undoLastMove();
            if (pos != null) {
                GameNode node = game.node(pos);
                if (node != null) node.reverseTurn();
                game.recalculateLight();
                refreshGrid();
                updateUndoRedoButtons();
            }
        }
    }
    // this function redoes the last move
    private void redoMove() {
        if (logger != null) {
            Position pos = logger.redoLastMove();
            if (pos != null) {
                GameNode node = game.node(pos);
                if (node != null) node.turn();
                game.recalculateLight();
                refreshGrid();
                updateUndoRedoButtons();
            }
        }
    }

    // this function shows the game scene (playing field)
    private void showGameScene() {
        BorderPane gameLayout = createGameLayout();
        animationHelper.animateSwitchTo(() -> {
            root.getChildren().setAll(gameLayout);
            startTimer();
        }, "right");        
        primaryStage.setTitle("Light Circuit - Game");
    }

    // this function applies the moves from the log file
    public void applyLoggedMoves(Game game, GameLogger logger) {
        List<Position> moves = logger.getMoves();
        while(!logger.isUndoEmpty()) logger.undoLastMove();

        for (Position move : moves) {
            GameNode node = game.node(move);
            if (node != null && !node.isEmpty()) {
                node.turn(); // Simulate the player's move
                //re-add the move to the logger
                logger.logMove(move);
            }
        }

        game.recalculateLight(); // Recompute lighting after all turns
    }
    // this function defines behavior on the exit of the level 
    private void handleLevelExit() {
        boolean allowSave = true;
        if(isRandomLevel){
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Save Level");
            alert.setHeaderText("Do you want to save this randomly generated level?");
            alert.setContentText("It will be saved under 'Random' difficulty.");

            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                allowSave = true;
            }
            else{
                allowSave = false;
                try{
                    Files.deleteIfExists(logger.getLogPath());

                    int levelNumber = logger.getLevelNumber();
                    Path layoutPath = Paths.get("data", "level_layout", "random", "level" + levelNumber + ".txt");
                    Files.deleteIfExists(layoutPath);
                }catch (IOException e){
                    System.err.println("Failed to delete unsaved random level log: " + e.getMessage());
                }

            }
        }
        if (allowSave && logger != null && currentDifficulty != null) {
            long elapsed = System.currentTimeMillis() - startTime;
            logger.saveAccumulatedTime(elapsed);
            logger.clearStack();
            logger.logNodeStates(game); // Save current node states
        }
    }

    // refreshes the grid, updates the game state and checks for win condition
    private void refreshGrid() {
        game.recalculateLight();
        if (game.isWon()) {
            if (timer != null) {
                handleLevelExit();
                timer.stop();
            }
            if (logger != null) {
                long elapsedTime = System.currentTimeMillis() - startTime;
                logger.saveBestTime(elapsedTime);
                logger.clear();
            }
            if(backActionOnWin != null) {
                animationHelper.animateSwitchTo(backActionOnWin, "left");
            } else {
                animationHelper.animateSwitchTo(this::showMenu, "left");
            }
        } else {
            BorderPane currentLayout = (BorderPane) root.getChildren().get(0);
            GridPane gameGrid = (GridPane) currentLayout.getCenter(); 

            // Always update the grid firstF
            gameGrid.getChildren().clear();
            for (int r = 1; r <= game.rows(); r++) {
                for (int c = 1; c <= game.cols(); c++) {
                    Position pos = new Position(r, c);
                    GameNode node = game.node(pos);
                    boolean isSolutionNode = game.getSolutionNodes().contains(pos);
                    GameCell cell = new GameCell(
                            node,
                            this::refreshGrid,
                            logger,
                            showHint ? node.getRotations() : 0,
                            isSolutionNode);

                    if(showHint){
                        if(game.getSolutionNodes().contains(pos)){
                            Set<Side> expected = game.getSolutionConnectors(pos);

                            if (expected != null && !expected.equals(node.getConnectors())) {
                                cell.setStyle("-fx-background-color: #ffcccc;"); // wrong rotation
                            } else {
                                cell.setStyle("-fx-background-color: #ccffcc;"); // correct rotation
                            }
                        }
                    }

                    gameGrid.add(cell, c - 1, r - 1);
                }
            }
            updateUndoRedoButtons();
        }
    }


    // custom board input, allows the user to enter the size of the board. Separate from the menu
    private void showCustomBoardInput() {
        Label instruction = new Label("Enter board size");

        Label rowsLabel = new Label("Rows:");
        Label colsLabel = new Label("Cols:");
        Label difficultyLabel = new Label("Difficulty:");

        TextField rowField = new TextField();
        TextField colField = new TextField();
        rowField.setPromptText("2-20");
        colField.setPromptText("2-20");

        //Create difficulty selector
        ComboBox<Difficulty> difficultyComboBox = new ComboBox<>();
        difficultyComboBox.getItems().addAll(Difficulty.values());
        difficultyComboBox.setPromptText("Select difficulty");


        Button startButton = new Button("Start Game");
        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: red;");

        startButton.setOnAction(e -> {
            try {
                int rows = Integer.parseInt(rowField.getText());
                int cols = Integer.parseInt(colField.getText());
                Difficulty difficulty = difficultyComboBox.getValue();

                if (rows < 2 || cols < 2 || rows > 20 || cols > 20) {
                    errorLabel.setText("Rows and columns must be between 2 and 20.");
                    return;
                }
                if(difficulty == null){
                    errorLabel.setText("Please select a difficulty level.");
                    return;
                }
                LevelGenerator generator = new LevelGenerator();
                int randomLevelNum = getNextRandomLevelNumber();
                game = generator.generateLevel(rows, cols, difficulty, randomLevelNum);

                this.selectedRandomDifficulty = difficulty;
                this.currentDifficulty = "Random";
                isRandomLevel = true;

                logger = new GameLogger("Random", randomLevelNum, difficulty.name());

                startTimer();
                this.backActionOnWin = this::showMenu;

                animationHelper.animateSwitchTo(() -> root.getChildren().setAll(createGameLayout()), "right");
                primaryStage.setTitle("Light Circuit - Custom Game");
            } catch (NumberFormatException ex) {
                errorLabel.setText("Please enter valid integers.");
            }
        });

        Button backButton = new Button("Back");
        backButton.setOnAction(e -> animationHelper.animateSwitchTo(this::showMenu, "left"));

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setAlignment(Pos.CENTER);
        grid.add(instruction, 0, 0, 2, 1);
        grid.add(rowsLabel, 0, 1);
        grid.add(rowField, 1, 1);
        grid.add(colsLabel, 0, 2);
        grid.add(colField, 1, 2);
        grid.add(difficultyLabel, 0, 3);
        grid.add(difficultyComboBox, 1, 3);
        grid.add(backButton, 0, 4, 2, 1);
        grid.add(startButton, 1, 4, 1, 1);
        grid.add(errorLabel, 0, 5, 2, 1);

        animationHelper.animateSwitchTo(() -> root.getChildren().setAll(grid), "right");
    }

    private int getNextRandomLevelNumber() {
        File dir = Paths.get("data", "log", "Random").toFile();
        if (!dir.exists()) dir.mkdirs();

        File[] files = dir.listFiles((d, name) -> name.matches("level\\d+\\.log"));
        int maxNum = 0;
        if (files != null) {
            for (File file : files) {
                String name = file.getName(); // levelX.log
                String digits = name.replaceAll("[^\\d]", "");
                if (!digits.isEmpty()) {
                    int num = Integer.parseInt(digits);
                    if (num > maxNum) maxNum = num;
                }
            }
        }
        return maxNum + 1;
    }


    public static void main(String[] args) {
        launch(args);
    }
}