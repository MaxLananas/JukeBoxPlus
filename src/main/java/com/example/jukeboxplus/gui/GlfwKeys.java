package com.example.jukeboxplus.gui;

/** GLFW key codes used by every version up to 1.21.11 (26.x maps through InputConstants instead). */
final class GlfwKeys {
    private GlfwKeys() {}

    static PlayerUi.Key map(int glfwKey) {
        switch (glfwKey) {
            case 32:  return PlayerUi.Key.SPACE;
            case 262: return PlayerUi.Key.RIGHT;
            case 263: return PlayerUi.Key.LEFT;
            case 264: return PlayerUi.Key.DOWN;
            case 265: return PlayerUi.Key.UP;
            default:  return null;
        }
    }
}
