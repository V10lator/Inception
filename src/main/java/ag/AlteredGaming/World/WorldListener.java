package ag.AlteredGaming.World;

import ag.AlteredGaming.Inception;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.event.world.WorldUnloadEvent;

/**
 *
 * @author Xaymar
 */
public class WorldListener
        implements Listener {

    private Inception objPlugin;

    public WorldListener(Inception objPlugin) {
        this.objPlugin = objPlugin;
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onWorldLoad(WorldLoadEvent event) {
        if (!objPlugin.getWorldHandlers().containsKey(event.getWorld())) {
            objPlugin.getWorldHandlers().put(event.getWorld(), new WorldHandler(objPlugin, event.getWorld()));
        }
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onWorldUnload(WorldUnloadEvent event) {
        if (objPlugin.getWorldHandlers().containsKey(event.getWorld())) {
            objPlugin.getWorldHandlers().remove(event.getWorld());
        }
    }
}
