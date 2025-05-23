package com.game;
/*
 * author: xstahl01
 * This class is responsible for generating random levels of the game. (Custom)
 */

import com.common.*;
import java.util.*;

public class LevelGenerator {
    private final Random random = new Random();

    public Game generateLevel(int rows, int cols) {
        Game game = new Game(rows, cols);
        Position startPos = randomPosition(rows, cols);
        Side startDir = findValidDirection(startPos, game);
        game.createPowerNode(startPos, startDir);

        Set<Position> visited = new HashSet<>();
        visited.add(startPos);

        Position firstLinkPos = getAdjacent(startPos, startDir);
        if (!isValid(firstLinkPos, game)) {
            throw new IllegalStateException("No valid starting position from PowerNode");
        }
    
        Side incoming = getOpposite(startDir);
        // 
        int depth = (rows > cols) ? rows : cols;

        growTree(game, firstLinkPos, incoming, visited, 0, depth);

        scrambleLinks(game);

        game.init();
        return game;
    }


    private Side findValidDirection(Position pos, Game game) {
        List<Side> dirs = new ArrayList<>(Arrays.asList(Side.values()));
        Collections.shuffle(dirs);
        for (Side dir : dirs) {
            Position next = getAdjacent(pos, dir);
            if (isValid(next, game)) {
                return dir;
            }
        }
        throw new IllegalStateException("No valid direction found for PowerNode");
    }

    private void growTree(Game game, Position currentPos, Side entryDir, Set<Position> visited, int depth, int maxDepth) {
        if (depth >= maxDepth) {
            game.createBulbNode(currentPos, getOpposite(entryDir));
            return;
        }
    
        List<Side> directions = new ArrayList<>(Arrays.asList(Side.values()));
        directions.remove(getOpposite(entryDir)); // prevent backtracking
        Collections.shuffle(directions);
    
        List<Side> connectedDirs = new ArrayList<>();
        connectedDirs.add(entryDir); // always connect to parent
    
        List<Side> outgoingDirs = new ArrayList<>();
    
        for (Side dir : directions) {
            Position next = getAdjacent(currentPos, dir);
            if (!isValid(next, game) || visited.contains(next)) continue;
    
            outgoingDirs.add(dir);
            connectedDirs.add(dir);
        }
    
        // No valid children
        if (outgoingDirs.isEmpty()) {
            game.createBulbNode(currentPos, getOpposite(entryDir));
            return;
        }
    
        // Create LinkNode with multiple connectors
        game.createLinkNode(currentPos, connectedDirs.get(0), connectedDirs.get(1));
        for (int i = 2; i < connectedDirs.size(); i++) {
            game.node(currentPos).addConnector(connectedDirs.get(i));
        }
        visited.add(currentPos);
    
        for (Side outDir : outgoingDirs) {
            Position next = getAdjacent(currentPos, outDir);
            Side nextIncoming = getOpposite(outDir);
            growTree(game, next, nextIncoming, visited, depth + 1, maxDepth);
        }
    }
    

    public static void scrambleLinks(Game game) {
        Random random = new Random();
        game.getBoard().values().stream()
                .filter(GameNode::isLink)
                .forEach(node -> {
                    int rotations = random.nextInt(4);
                    for (int i = 0; i < rotations; i++) node.turn();
                });
        game.getBoard().values().stream()
                .filter(GameNode::isBulb)
                .forEach(node -> {
                    int rotations = random.nextInt(4);
                    for (int i = 0; i < rotations; i++) node.turn();
                });
        game.getBoard().values().stream()
                .filter(GameNode::isPower)
                .forEach(node -> {
                    int rotations = random.nextInt(4);
                    for (int i = 0; i < rotations; i++) node.turn();
                });
    }

    // Helper methods
    private Position randomPosition(int rows, int cols) {
        return new Position(random.nextInt(rows) + 1, random.nextInt(cols) + 1);
    }

    private Side randomDirection() {
        return Side.values()[random.nextInt(4)];
    }

    private Position getAdjacent(Position p, Side dir) {
        return switch (dir) {
            case NORTH -> new Position(p.row()-1, p.col());
            case SOUTH -> new Position(p.row()+1, p.col());
            case EAST -> new Position(p.row(), p.col()+1);
            case WEST -> new Position(p.row(), p.col()-1);
        };
    }

    private boolean isValid(Position p, Game game) {
        return p.row() > 0 && p.col() > 0 &&
                p.row() <= game.rows() && p.col() <= game.cols();
    }

    private Side getOpposite(Side dir) {
        return switch (dir) {
            case NORTH -> Side.SOUTH;
            case SOUTH -> Side.NORTH;
            case EAST -> Side.WEST;
            case WEST -> Side.EAST;
        };
    }

    private Side randomNewDirection(Side incoming) {
        List<Side> directions = new ArrayList<>(Arrays.asList(Side.values()));
        directions.remove(getOpposite(incoming)); // Prevent backtracking
        return directions.get(random.nextInt(directions.size()));
    }

    private boolean shouldPlaceBulb(Game game) {
        // 25% chance to place bulb after minimum path length
        return random.nextFloat() < 0.25f;
    }
}
