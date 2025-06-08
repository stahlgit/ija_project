package com.game;
/*
 * author: xstahl01
 * This class is responsible for generating random levels of the game. (Custom)
 */

import com.common.*;
import com.ui.Difficulty;

import java.util.*;

public class LevelGenerator {
    private final Random random = new Random();

    public Game generateLevel(int rows, int cols, Difficulty difficulty) {
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

        Map<Position, Position> parentMap = new HashMap<>();
        Map<Position, List<Position>> childrenMap = new HashMap<>();
        parentMap.put(startPos, null);
        parentMap.put(firstLinkPos, startPos);
        childrenMap.computeIfAbsent(startPos, k -> new ArrayList<>()).add(firstLinkPos);

        Side incoming = getOpposite(startDir);
        int depth = Math.max(rows, cols);

        growTree(game, firstLinkPos, startPos, incoming, visited, 0, depth, parentMap, childrenMap);

        // Remove unnecessary connectors from LinkNodes
        for (Position pos : new HashSet<>(parentMap.keySet())) {
            GameNode node = game.node(pos);
            if (node != null && node.isLink()) {
                Set<Side> usedDirs = new HashSet<>();

                // Add parent direction
                Position parent = parentMap.get(pos);
                if (parent != null) {
                    usedDirs.add(getDirection(pos, parent));
                }

                // Add child directions
                List<Position> children = childrenMap.get(pos);
                if (children != null) {
                    for (Position child : children) {
                        usedDirs.add(getDirection(pos, child));
                    }
                }

                // Reset connectors to only used directions
                LinkNode linkNode = (LinkNode) node;
                linkNode.removeAllConnectors();
                for (Side dir : usedDirs) {
                    linkNode.addConnector(dir);
                }
            }
        }

        // Save pre-scrambled state for hinting
        game.saveSolutionState();
        scrambleLinks(game);

        if (difficulty != Difficulty.Easy){
            addFakesNodes(game, difficulty);
        }
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

    private void growTree(Game game, Position currentPos, Position parentPos, Side entryDir,
                          Set<Position> visited, int depth, int maxDepth,
                          Map<Position, Position> parentMap, Map<Position, List<Position>> childrenMap) {
        if (depth >= maxDepth) {
            game.createBulbNode(currentPos, entryDir);
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
            game.createBulbNode(currentPos, entryDir);
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
            visited.add(next);
            parentMap.put(next, currentPos);
            childrenMap.computeIfAbsent(currentPos, k -> new ArrayList<>()).add(next);

            Side nextIncoming = getOpposite(outDir);
            growTree(game, next, currentPos, nextIncoming, visited, depth + 1, maxDepth, parentMap, childrenMap);
        }
    }


    private Side getDirection(Position from, Position to) {
        if (to.row() < from.row()) return Side.NORTH;
        if (to.row() > from.row()) return Side.SOUTH;
        if (to.col() < from.col()) return Side.WEST;
        if (to.col() > from.col()) return Side.EAST;
        throw new IllegalArgumentException("Positions must be adjacent");
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

    private void addFakesNodes(Game game, Difficulty difficulty) {
        Set<Position> emptyPositions = game.findEmptyNodes();
        if(emptyPositions.isEmpty()) return;

        List<Position> emptyList = new ArrayList<>(emptyPositions);
        Collections.shuffle(emptyList);

        int fakeCount;
        if(difficulty == Difficulty.Medium){
            double percentage = 0.2 + random.nextDouble() * 0.2; // min 1, max 5;
            fakeCount = (int) Math.ceil(emptyList.size() * percentage);
            fakeCount = Math.max(1, Math.min(fakeCount, 5));
        } else {
            //add fake nodes to all empty cells
            fakeCount = emptyList.size();
        }
        for (int i = 0; i < Math.min(fakeCount, emptyList.size()); i++) {
            Position pos = emptyList.get(i);
            addRandomFakeLinkNode(game, pos);
        }
    }

    private void addRandomFakeLinkNode(Game game, Position pos) {
        // Determine number of connectors (2-3)
        int numConnectors = random.nextInt(2) + 2;
        List<Side> allSides = new ArrayList<>(Arrays.asList(Side.values()));
        Collections.shuffle(allSides);

        // Create the node with the first two connectors
        game.createLinkNode(pos, allSides.get(0), allSides.get(1));
        GameNode node = game.node(pos);

        // Add remaining connectors (if any)
        for (int i = 2; i < numConnectors; i++) {
            node.addConnector(allSides.get(i));
        }
    }

}
