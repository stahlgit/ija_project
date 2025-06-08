package com.common;
/*
 * author: xstahl01
 * This is the abstract class for all nodes in the game.
 */

import com.tool.AbstractObservableField;

import java.util.HashSet;
import java.util.Set;

public abstract class GameNode extends AbstractObservableField {
    protected Position position;
    protected boolean lit = false;
    protected Set<Side> connectors = new HashSet<>();
    protected int rotations = 0;

    public abstract boolean isEmpty();
    public abstract String toString();

    public GameNode(Position position) {
        this.position = position;
    }

    public Position getPosition() {
        return this.position;
    }

    public boolean containsConnector(Side s){
        return connectors.contains(s);
    }
    public Set<Side> getConnectors(){
        return new HashSet<>(connectors);
    }

    public boolean north() { return containsConnector(Side.NORTH); }

    public boolean east() { return containsConnector(Side.EAST); }

    public boolean south() { return containsConnector(Side.SOUTH); }

    public boolean west() { return containsConnector(Side.WEST); }

    public boolean light(){
        return lit;
    }

    public void setLit(boolean lit){
        if(this.lit != lit){
            this.lit = lit;
            notifyObservers();
        }
    }

    public void addConnector(Side side) {
        connectors.add(side);
    }

    public int getRotations() {
        return rotations;
    }
    public void setRotations(int rotations) {
        this.rotations = rotations % 4;
    }

}
