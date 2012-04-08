package ag.AlteredGaming.World;

import ag.AlteredGaming.Inception;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldLoadEvent;

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
        WorldHandler objWorldHandler = new WorldHandler(objPlugin, event.getWorld());
        //Add Handler to global list
        objPlugin.getWorldHandlers().add(objWorldHandler);
    }
}
