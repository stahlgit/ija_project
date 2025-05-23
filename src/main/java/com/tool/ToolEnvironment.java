package com.tool;
/*
 * author: xstahl01
 * This interface represents the environment of the tool in the game.
 */

public interface ToolEnvironment {
    int rows();

    int cols();

    ToolField fieldAt(int var1, int var2);
}
