/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package me.ayunami2000.psbot.joystick.jna;

import com.sun.jna.Structure;

/**
 *
 * @author albus
 */
@Structure.FieldOrder({"somevalue"})
public class TempStruct extends Structure {
    public int somevalue = 0;
}