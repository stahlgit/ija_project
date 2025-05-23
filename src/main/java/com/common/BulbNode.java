package com.common;
/*
 * @author: xstahl01
 * This class represents a BulbNode in the game and it's functionality.
 */
import java.util.StringJoiner;

public class BulbNode extends GameNode {
    public BulbNode(Position position, Side side) {
        super(position);
        this.connectors.add(side);
    }

    @Override
    public boolean isBulb(){return true;}

    @Override
    public boolean isLink(){return false;}

    @Override
    public boolean isPower(){return false;}

    @Override
    public boolean isEmpty(){return false;}

    public void turn() {
        // Rotate the connector
        Side current = connectors.iterator().next();
        connectors.clear();
        connectors.add(switch (current) {
            case NORTH -> Side.EAST;
            case SOUTH -> Side.WEST;
            case WEST -> Side.NORTH;
            case EAST -> Side.SOUTH;
        });
        notifyObservers();
    }

    public void reverseTurn() {
        // Rotate the connector in the opposite direction
        Side current = connectors.iterator().next();
        connectors.clear();
        connectors.add(switch (current) {
            case NORTH -> Side.WEST;
            case SOUTH -> Side.EAST;
            case WEST -> Side.SOUTH;
            case EAST -> Side.NORTH;
        });
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
        return String.format("{B[%d@%d][%s]}", position.row(), position.col(), sj);
    }
}
