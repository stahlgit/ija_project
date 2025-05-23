package com.tool;
/*
 * author: xstahl01
 * This interface represents a tool field in the game. Which derines basic properties of a field.
 */

public interface ToolField extends Observable {
    void turn();

    void reverseTurn();

    boolean north();

    boolean east();

    boolean south();

    boolean west();

    boolean light();

    boolean isLink();

    boolean isBulb();

    boolean isPower();
}
