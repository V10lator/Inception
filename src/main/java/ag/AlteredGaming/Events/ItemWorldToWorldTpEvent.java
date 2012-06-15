package ag.AlteredGaming.Events;

import org.bukkit.Location;
import org.bukkit.entity.Item;
import org.bukkit.event.HandlerList;

public class ItemWorldToWorldTpEvent extends InceptionEvent
{
  private static final HandlerList handlers = new HandlerList();
  private final Item item;
  
  public ItemWorldToWorldTpEvent(Item item, Location to)
  {
	super(item.getLocation(), to);
	this.item = item;
  }
  
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
