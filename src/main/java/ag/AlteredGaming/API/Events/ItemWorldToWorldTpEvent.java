package ag.AlteredGaming.API.Events;

import org.bukkit.Location;
import org.bukkit.entity.Item;
import org.bukkit.event.HandlerList;

public class ItemWorldToWorldTpEvent extends InceptionEvent
{
  private static final HandlerList handlers = new HandlerList();
  private final Item item;
  
  /**
   * The default constructor.
   * @param item - The item to teleport.
   * @param to - The location to teleport to.
   */
  public ItemWorldToWorldTpEvent(Item item, Location to)
  {
	super(item.getLocation(), to);
	this.item = item;
  }
  
  /**
   * To get the item involved in this event.
   * @return The item to teleport.
   */
  public Item getItem()
  {
	return item;
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
