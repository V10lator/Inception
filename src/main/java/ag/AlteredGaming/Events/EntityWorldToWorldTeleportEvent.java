package ag.AlteredGaming.Events;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.event.HandlerList;

public class EntityWorldToWorldTeleportEvent extends InceptionEvent
{
  private static final HandlerList handlers = new HandlerList();
  private final Entity entity;
  
  public EntityWorldToWorldTeleportEvent(Entity entity, Location to)
  {
	super(entity.getLocation(), to);
	this.entity = entity;
  }
  
  public Entity getEntity()
  {
	return entity;
  }
  
  public HandlerList getHandlers()
  {
    return handlers;
  }
   
  public static HandlerList getHandlerList() 
  {
    return handlers;
  }
}
