package com.tool;
/*
 * author: xstahl01
 * This interface represents an observable object in the game.
 */

public interface Observable {
    void addObserver(Observer var1);

    void removeObserver(Observer var1);

    void notifyObservers();

    public interface Observer {
        void update(Observable var1);
    }
}
