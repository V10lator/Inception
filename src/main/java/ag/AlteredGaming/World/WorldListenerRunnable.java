/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ag.AlteredGaming.World;

import ag.AlteredGaming.Inception;

/**
 *
 * @author Xaymar
 */
public class WorldListenerRunnable implements Runnable {
    private Inception objPlugin;
    private WorldHandler objWorldHandler;

    public WorldListenerRunnable(Inception objPlugin, WorldHandler objWorldHandler) {
        this.objPlugin = objPlugin;
        this.objWorldHandler = objWorldHandler;
    }
    
    @Override
    public void run() {
        objWorldHandler.loadConfig();
    }
}
