package com.alexei.spaceshooter;

/**
 * Created by Alex on 16/06/2015.
 *
 * Simply keeps track of the game mode: Menu, Playing, etc.
 */
public class GameModeManager {
    private static GameMode mode = GameMode.Playing;

    public GameModeManager() {

    }

    public static void setMode(GameMode newMode) {
        mode = newMode;
    }

    public static GameMode getMode() {
        return mode;
    }

    public enum GameMode {
        Menu,
        Playing, // when the player is actively playing the game
        Paused,  // when the game was in Playing mode and was paused
        Died, // when player dies while in Playing mode, this mode is set
        Ready; // the mode that the player is in when they select to play a level, but before they actually begin playing.

    }
}
