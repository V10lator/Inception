/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ag.AlteredGaming.World;

import ag.AlteredGaming.Inception;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

/**
 *
 * @author Xaymar
 */
public class PlayerListener
        implements Listener {

    private Inception objPlugin;

    public PlayerListener(Inception objPlugin) {
        this.objPlugin = objPlugin;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerMove(PlayerMoveEvent event) {
        objPlugin.getWorldHandlers().get(event.getFrom().getWorld()).onPlayerMove(event);
    }
}
