package com.utils;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;

import com.common.Position;
import com.game.Game;
/*
 * author: xstahl01
 * This class handles all the logging of game moves, time, and node states.
 */


public class GameLogger {
    private final Path logPath;
    private final boolean enableLogging;
    private final List<String> lines = new ArrayList<>();
    private long bestTime = Long.MAX_VALUE;
    private long accumulatedTime = 0; 
    private String gridSize = null;
    private String difficulty;
    private String actualdifficulty;
    private int levelNumber;

    private Deque<Position> undoStack = new ArrayDeque<>();
    private Deque<Position> redoStack = new ArrayDeque<>();
    private List<String> nodeStates = new ArrayList<>();


    public GameLogger(String difficulty, int levelNumber, String actualdifficulty){
        this.enableLogging = (difficulty != null);
        this.difficulty = difficulty;
        this.actualdifficulty = actualdifficulty;
        this.levelNumber = levelNumber;
        
        if(enableLogging){
            this.logPath = Paths.get("data", "log", difficulty, "level" + levelNumber + ".log");
            try {
                Files.createDirectories(logPath.getParent());
                
                if(Files.notExists(logPath)) {
                    initializeNewLog(difficulty);
                } else {
                    loadExistingLog();
                }
                validateLogStructure();
            } catch (IOException e){
                e.printStackTrace();
            }
        }
        else {
            this.logPath = null;
        }
    }

    /** NODE LOGGING */
    private void initializeNewLog(String difficulty) throws IOException {
        Files.createFile(logPath);
        if(difficulty.equals("Easy")){
            lines.add("grid:[4,4]");
        }else if (difficulty.equals("Medium")){
            lines.add("grid:[6,6]");
        }else if (difficulty.equals("Hard")){
            lines.add("grid:[8,8]");
        }
        lines.add("time: ");
        lines.add("accumulated: 0");
        lines.add("difficulty: " + actualdifficulty);

        if (!nodeStates.isEmpty()) {
            lines.addAll(nodeStates);
        }
        save();
    }

    private void loadExistingLog() throws IOException {
        // Read all lines and separate node states from other data
        List<String> allLines = Files.readAllLines(logPath);
        
        for(String line : allLines) {
            String trimmed = line.trim();
            if(trimmed.isEmpty()) continue;
            
            if(trimmed.startsWith("grid:")){
                gridSize= trimmed;
            }
            if(trimmed.startsWith("time: ")) {
                handleTimeLine(trimmed);
            } else if(trimmed.startsWith("accumulated: ")) {
                handleAccumulatedLine(trimmed);
            } else if (trimmed.startsWith("difficulty: ")) {
                this.difficulty = trimmed.substring(11).trim();
            } else if (trimmed.matches("\\{[A-Z]\\[\\d+@\\d+\\].*}")) { // Matches {E[1@1][]}, {P[2@2][SOUTH]}, etc.
                nodeStates.add(trimmed);
            } else if(trimmed.matches("\\d+,\\d+")) { 
                handleMoveLine(trimmed);
            }
        }
        
        lines.addAll(allLines);
    }

    private void handleAccumulatedLine(String line) {
        String timePart = line.substring(13).trim();
        if(!timePart.isEmpty()) accumulatedTime = Long.parseLong(timePart);
    }

    private void handleMoveLine(String line) {
        String[] parts = line.split(",");
        int row = Integer.parseInt(parts[0]);
        int col = Integer.parseInt(parts[1]);
        undoStack.push(new Position(row, col));
    }

    private void validateLogStructure() {
        boolean hasTime = false;
        boolean hasAccumulated = false;
        
        for(String line : lines) {
            if(line.startsWith("time: ")) hasTime = true;
            if(line.startsWith("accumulated: ")) hasAccumulated = true;
        }
        
        if(!hasTime) lines.add(0, "time: ");
        if(!hasAccumulated) lines.add(1, "accumulated: 0");
        save();
    }

    public void logNodeStates(Game game) {
        if(!enableLogging) return;
        
        // Clear existing node states
        lines.removeIf(line -> line.matches("\\{[A-Z]\\[\\d+@\\d+\\].*}"));
        nodeStates.clear();
        
        List<String> currentStates = game.getBoard().values().stream()
            .filter(node -> !node.toString().startsWith("E")) // Exclude empty nodes
            .map(node -> {
                Position pos = node.getPosition();
                String type = node.toString().substring(1, 2); // Extract type from node's toString
                String details = node.toString().split("\\[")[2].replaceAll("[\\[\\]]", "");
                if (details.equals("}")) details = "";

                int rotations = node.getRotations();

                return String.format("{%s[%d@%d][%s][%d]}", type, pos.row(), pos.col(), details, rotations);
            })
            .collect(Collectors.toList());

        nodeStates.addAll(currentStates);
        lines.addAll(currentStates);
        
        gridSize = "grid:[" + game.rows() + "," + game.cols() + "]";

    
        // Rebuild full log structure
        rebuildLogStructure();
        save();
    }

    private void rebuildLogStructure() {
        List<String> newLines = new ArrayList<>();
        
        // Header lines
        if(gridSize != null) {
            newLines.add(gridSize);
        }
        newLines.add("time: " + (bestTime == Long.MAX_VALUE ? "" : bestTime));
        newLines.add("accumulated: " + accumulatedTime);
        if(difficulty != null){
            newLines.add("difficulty: " + actualdifficulty);
        }
        // Node states
        newLines.addAll(nodeStates);
        
        // Moves from undo stack (in chronological order)
        undoStack.descendingIterator()
                .forEachRemaining(pos -> newLines.add(pos.row() + "," + pos.col()));
        
        lines.clear();
        lines.addAll(newLines);
    }

    public void logMove(Position pos) {
        undoStack.push(pos);
        redoStack.clear();
        if(enableLogging){
            rebuildLinesFromUndoStack(); // Rebuild lines from updated stack
        }
    }

    public List<Position> getMoves() {
        List<Position> orderedMoves = new ArrayList<>();
        undoStack.descendingIterator().forEachRemaining(orderedMoves::add);
        return orderedMoves;
    }

    /** TIME LOGGING */
    private void handleTimeLine(String line) {
        String timePart = line.substring(6).trim();
        if(!timePart.isEmpty()) bestTime = Long.parseLong(timePart);
    }

    public long getAccumulatedTime() {
        return accumulatedTime;
    }

    public void saveAccumulatedTime(long time) {
        if (!enableLogging) return;
        accumulatedTime = time;
        updateLine("accumulated: ", accumulatedTime);
    }
    
    private void updateLine(String prefix, long value) {
        for (int i = 0; i < lines.size(); i++) {
            if (lines.get(i).startsWith(prefix)) {
                lines.set(i, prefix + value);
                save();
                return;
            }
        }
        lines.add(prefix + value);
        save();
    }

    public void saveBestTime(long time){
        if(!enableLogging) return;
        if(time < bestTime){
            bestTime = time;
            lines.set(0, "time: " + bestTime);
            save();
        }
    }

    public String getBestTimeFormatted() {
        if (!enableLogging || bestTime == Long.MAX_VALUE) return "--";
        return formatTime(bestTime);
    }

    private String formatTime(long millis) {
        long seconds = millis / 1000;
        return String.format("%02d:%02d", seconds / 60, seconds % 60);
    }


    /** STACK LOGGING */
    public Position undoLastMove() {
        if (undoStack.isEmpty()) return null;
        Position lastMove = undoStack.pop();
        redoStack.push(lastMove);
        if(enableLogging) rebuildLinesFromUndoStack();
        return lastMove;
    }

    public Position redoLastMove() {
        if (redoStack.isEmpty()) return null;
        Position nextMove = redoStack.pop();
        undoStack.push(nextMove);
        if(enableLogging) rebuildLinesFromUndoStack();
        return nextMove;
    }

    public boolean isUndoEmpty() {
        return undoStack.isEmpty();
    }
    
    public boolean isRedoEmpty() {
        return redoStack.isEmpty();
    }

    private void rebuildLinesFromUndoStack() {
        if (!enableLogging) return;
        rebuildLogStructure();
        save();
    }

    public void clearStack(){
        if (!enableLogging) return;
        undoStack.clear();
        redoStack.clear();
    }

    /** GENERAL PURPOSE **/
    public void clear() {
        if (!enableLogging) return;
        try {
            // Clear moves but preserve node states and best time
            undoStack.clear();
            redoStack.clear();
            accumulatedTime = 0;
            nodeStates.clear();  // Clear node states
            lines.removeIf(line -> line.matches("\\{[A-Z]\\[\\d+@\\d+\\].*}"));
            
            List<String> newLines = new ArrayList<>();
            newLines.add(gridSize);
            newLines.add("time: " + bestTime);
            newLines.add("accumulated: 0");
            newLines.add("difficulty: " + actualdifficulty);
            newLines.addAll(nodeStates);
            
            lines.clear();
            lines.addAll(newLines);

            Files.write(logPath, newLines);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public int getLevelNumber(){
        return levelNumber;
    }
    private void save() {
        if (!enableLogging) return;
        try {
            Files.write(logPath, lines);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public Path getLogPath(){
        return logPath;
    }
}
