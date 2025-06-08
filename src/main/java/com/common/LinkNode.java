package com.common;
/*
 * author: xstahl01
 * This class represents a LinkNode in the game and its functionality.
 */


import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.StringJoiner;
import java.util.stream.Collectors;

public class LinkNode extends GameNode {
    public LinkNode(Position position, Side... connectors) {
        super(position);
        if (connectors.length < 2) {
            throw new IllegalArgumentException("LinkNode must have at least two connectors");
        }
        this.connectors = new HashSet<>();
        Collections.addAll(this.connectors, connectors);

    }

    
    @Override
    public boolean isLink() {
        return true;
    }

    @Override
    public boolean isBulb() {
        return false;
    }

    @Override
    public boolean isPower() {
        return false;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public void turn() {
        // other way to do it using streamline solution
        connectors = connectors.stream().map(s -> switch (s) {
            case NORTH -> Side.EAST;
            case SOUTH -> Side.WEST;
            case WEST -> Side.NORTH;
            case EAST -> Side.SOUTH;
        }).collect(Collectors.toSet());
        rotations = (rotations + 1) % 4;
        notifyObservers();
    }

    public void reverseTurn() {
        // Rotate the connector in the opposite direction
        connectors = connectors.stream().map(s -> switch (s) {
            case NORTH -> Side.WEST;
            case SOUTH -> Side.EAST;
            case WEST -> Side.SOUTH;
            case EAST -> Side.NORTH;
        }).collect(Collectors.toSet());
        rotations = (rotations + 3) % 4; // equivalent to -1 mod 4
        notifyObservers();
    }

    

    @Override
    public String toString() {
        StringJoiner sj = new StringJoiner(",");
        for (Side side : new Side[]{Side.NORTH, Side.EAST, Side.SOUTH, Side.WEST}) {
            if (containsConnector(side)) {
                sj.add(side.name());
            }
        }
        return String.format("{L[%d@%d][%s]}", position.row(), position.col(), sj);
    }
}