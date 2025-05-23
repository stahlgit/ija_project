package com.common;
/*
 * author: xstahl01
 * This class represents an EmptyNode in the game and its functionality.
 */

import java.util.Set;

public class EmptyNode extends GameNode {
    public EmptyNode(Position position) {
        super(position);
    }

    @Override
    public boolean isBulb() {
        return false;
    }

    @Override
    public boolean isLink() {
        return false;
    }

    @Override
    public boolean isPower() {
        return false;
    }

    @Override
    public boolean isEmpty() {
        return true;
    }

    @Override
    public void turn() {
        // This method is intentionally left empty because an EmptyNode cannot be turned.

    }

    @Override
    public void reverseTurn() {
        // This method is intentionally left empty because an EmptyNode cannot be reversed tuned.
    }


    @Override
    public boolean containsConnector(Side s) {
        return false;
    }

    @Override
    public Set<Side> getConnectors() {
        return Set.of();
    }

    @Override
    public String toString() {
        return String.format("{E[%d@%d][]}", position.row(), position.col());
    }
}