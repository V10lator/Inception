package Inception.API.Events;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.event.HandlerList;

public class EntityWorldToWorldTpEvent extends InceptionEvent
{
  private static final HandlerList handlers = new HandlerList();
  private final Entity entity;
  
  /**
   * The default constructor.
   * @param entity - The entity to teleport.
   * @param to - The location to teleport to.
   */
  public EntityWorldToWorldTpEvent(Entity entity, Location to)
  {
	super(entity.getLocation(), to);
	this.entity = entity;
  }
  
  /**
   * To get the entity involved in this event.
   * @return The entity to teleport.
   */
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
