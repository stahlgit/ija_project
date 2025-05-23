package com.common;
/*
 * author: xstahl01
 *  This class represents a PowerNode in the game and its functionality.
 */

import java.util.HashSet;
import java.util.Set;
import java.util.StringJoiner;

public class PowerNode extends GameNode {
    public PowerNode(Position position, Side... connectors) {
        super(position);
        if(connectors.length <1){
            throw new IllegalArgumentException("PowerNode must have at least one connector");
        }
        this.connectors = new HashSet<>(Set.of(connectors));
    }

    @Override
    public boolean isPower(){return true;}

    @Override
    public boolean isLink(){return false;}

    @Override
    public boolean isBulb(){return false;}

    @Override
    public boolean isEmpty(){return false;}

    @Override
    public void turn() {
        Set<Side> newConnectors = new HashSet<>();
        for (Side s : connectors) {
            newConnectors.add(switch (s) {
                case NORTH -> Side.EAST;
                case SOUTH -> Side.WEST;
                case WEST -> Side.NORTH;
                case EAST -> Side.SOUTH;
            });
        }
        connectors = newConnectors;
        notifyObservers();
    }

    @Override
    public void reverseTurn() {
        Set<Side> newConnectors = new HashSet<>();
        for (Side s : connectors) {
            newConnectors.add(switch (s) {
                case NORTH -> Side.WEST;
                case SOUTH -> Side.EAST;
                case WEST -> Side.SOUTH;
                case EAST -> Side.NORTH;
            });
        }
        connectors = newConnectors;
        notifyObservers();
    }

    @Override
    public String toString() {
        StringJoiner sj = new StringJoiner(",");
        for (Side side : new Side[] {Side.NORTH, Side.EAST, Side.SOUTH, Side.WEST}) {
            if (containsConnector(side)) {
                sj.add(side.name());
            }
        }
        return String.format("{P[%d@%d][%s]}", position.row(), position.col(), sj);
    }
}
