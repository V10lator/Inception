/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Inception.World;

import Inception.Main.Inception;

/**
 *
 * @author Xaymar
 */
public class WorldHandlerRunnable implements Runnable {
    private Inception objPlugin;
    private WorldHandler objWorldHandler;

    public WorldHandlerRunnable(Inception objPlugin, WorldHandler objWorldHandler) {
        this.objPlugin = objPlugin;
        this.objWorldHandler = objWorldHandler;
    }

    @Override
    public void run() {
        objWorldHandler.tickWorldUpdateCheck();
    }
}
