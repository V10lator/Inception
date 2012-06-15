package ag.AlteredGaming.Events;

import org.bukkit.Location;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;

public abstract class InceptionEvent extends Event implements Cancellable
{
  private final Location from;
  Location to;
  boolean cancelled = false;
  
  public InceptionEvent(Location from,  Location to)
  {
	this.from = from;
	this.to = to;
  }
  
  public Location getFrom()
  {
	return from;
  }
  
  public Location getTo()
  {
	return to;
  }
  
  public void setTo(Location loc)
  {
	if(loc != null)
	  to = loc;
  }
  
  public boolean isCancelled()
  {
	return cancelled;
  }
  
  public void setCancelled(boolean cancelled)
  {
	this.cancelled = cancelled;
  }
}
