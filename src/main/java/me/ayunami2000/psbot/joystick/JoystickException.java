/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package me.ayunami2000.psbot.joystick;

import java.io.IOException;

/**
 *
 * @author albus
 */
public class JoystickException extends IOException {
    public JoystickException() {
        super();
    }

    public JoystickException(final String message) {
        super(message);
    }
}