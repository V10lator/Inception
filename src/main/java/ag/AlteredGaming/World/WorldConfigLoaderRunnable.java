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
public class WorldConfigLoaderRunnable
        implements Runnable {

    private Inception objPlugin;

    public WorldConfigLoaderRunnable(Inception objPlugin) {
        this.objPlugin = objPlugin;
    }
    
    @Override
    public void run() {
        objPlugin.loadWorlds();
    }
}
