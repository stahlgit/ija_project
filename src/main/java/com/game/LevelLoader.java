package com.game;
/*
 * author: xstahl01
 *  This class is responsible for loading the game level from a files in data directory.
 */

import com.common.Position;
import com.common.Side;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class LevelLoader {

    public static Game loadLevel(String filePath) throws IOException {
        List<String> lines = Files.readAllLines(Paths.get(filePath));
        List<String> nodeLines = lines.stream()
            .filter(line -> line.matches("\\{[A-Z]\\[\\d+@\\d+\\].*}"))
            .collect(Collectors.toList());

        if (nodeLines.isEmpty()) {
            throw new IOException("No valid node data in log file");
        }

        int maxRow = 0;
        int maxCol = 0;
        String firstLine = lines.get(0).trim();
        // skipping time if it's from log file
        if (firstLine.matches("grid:\\[\\d+,\\d+]")) {
            firstLine = firstLine.substring(6, firstLine.length() - 1); // Remove "grid:[" and "]"
            String[] parts = firstLine.split(",");
            // System.out.println("Grid size: " + parts[0] + "x" + parts[1]);
            maxRow = Integer.parseInt(parts[0]);
            maxCol = Integer.parseInt(parts[1]);
        } else {
            // Fallback: infer grid size from node positions
            maxRow = nodeLines.stream()
                .mapToInt(line -> Integer.parseInt(line.split(",")[1]))
                .max()
                .orElse(0);
            maxCol = nodeLines.stream()
                .mapToInt(line -> Integer.parseInt(line.split(",")[2]))
                .max()
                .orElse(0);
        }
        
        Game game = Game.create(maxRow, maxCol);

        Pattern nodePattern = Pattern.compile("([A-Z])\\[([^\\]]+)\\](?:\\[([^\\]]*)\\])?");

        for (String line : nodeLines) {
            line = line.substring(1, line.length() - 1); // Remove braces

            Matcher matcher = nodePattern.matcher(line);
            if (!matcher.matches()) {
                System.err.println("Invalid line format: " + line);
                continue;
            }

            String type = matcher.group(1);
            String posPart = matcher.group(2);
            String sidesPart = matcher.group(3); // Could be null

            String[] posParts = posPart.split("@");
            int row = Integer.parseInt(posParts[0]);
            int col = Integer.parseInt(posParts[1]);
            Position pos = new Position(row, col);

            List<Side> sides = new ArrayList<>();
            if (sidesPart != null && !sidesPart.isEmpty()) {
                for (String sideStr : sidesPart.split(",")) {
                    try {
                        sides.add(Side.valueOf(sideStr.trim()));
                    } catch (IllegalArgumentException e) {
                        System.err.println("Invalid side: " + sideStr);
                    }
                }
            }

            try {
                switch (type) {
                    case "P":
                        game.createPowerNode(pos, sides.toArray(new Side[0]));
                        break;
                    case "B":
                        if (sides.size() != 1) 
                            throw new IllegalArgumentException("Bulb requires one side");
                        game.createBulbNode(pos, sides.get(0));
                        break;
                    case "L":
                        game.createLinkNode(pos, sides.toArray(new Side[0]));
                        break;
                    case "E":
                        break;
                    default:
                        System.err.println("Unknown node type: " + type);
                }
            } catch (Exception e) {
                System.err.println("Error creating node: " + e.getMessage());
            }
        }

        game.init();
        return game;
    }
}