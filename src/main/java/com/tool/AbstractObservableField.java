package com.tool;
/*
 * author: xstahl01
 * This class represents an abstract observable field in the game and its functionality.
 */


import java.util.HashSet;
import java.util.Set;

public abstract class AbstractObservableField implements ToolField {
    private final Set<Observer> observers = new HashSet();

    public AbstractObservableField() {
    }

    public void addObserver(Observable.Observer var1) {
        this.observers.add(var1);
    }

    public void removeObserver(Observable.Observer var1) {
        this.observers.remove(var1);
    }

    public void notifyObservers() {
        this.observers.forEach((var1) -> var1.update(this));
    }
}
