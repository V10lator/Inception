package ag.AlteredGaming.World;

import ag.AlteredGaming.Inception;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
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
            objPlugin.getWorldHandlers().get(event.getWorld()).overlapUnload();
            objPlugin.getWorldHandlers().remove(event.getWorld());
        }
    }
    
    @EventHandler(priority = EventPriority.MONITOR) 
    public void onChunkLoad(ChunkLoadEvent event) {
        if (objPlugin.getWorldHandlers().containsKey(event.getWorld())) {
            objPlugin.getWorldHandlers().get(event.getWorld()).chunkLoadEvent(event);
        }
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onChunkUnload(ChunkUnloadEvent event) {
        if (objPlugin.getWorldHandlers().containsKey(event.getWorld())) {
            objPlugin.getWorldHandlers().get(event.getWorld()).chunkUnloadEvent(event);
        }
    }
}
