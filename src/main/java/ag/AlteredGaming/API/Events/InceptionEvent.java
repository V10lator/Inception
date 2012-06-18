package ag.AlteredGaming.API.Events;

import org.bukkit.Location;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;

/**
 * Other events extend this event.
 * You are able to listen to either this event or the specific event,
 * depending on your needs.
 * @author V10lator
 *
 */
public abstract class InceptionEvent extends Event implements Cancellable
{
  private final Location from;
  Location to;
  boolean cancelled = false;
  
  /**
   * The default constructor.
   * @param from - The location to teleport from.
   * @param to - The location to teleport to.
   */
  public InceptionEvent(Location from,  Location to)
  {
	this.from = from;
	this.to = to;
  }
  
  /**
   * To get the location to teleport from.
   * Don't change this.
   * @return The location to teleport from.
   */
  public Location getFrom()
  {
	return from;
  }
  
  /**
   * To get the location to teleport to.
   * @return The location to teleport to.
   */
  public Location getTo()
  {
	return to;
  }
  
  /**
   * To set the location to teleport to.
   * @param loc - The location to teleport to.
   */
  public void setTo(Location loc)
  {
	if(loc != null)
	  to = loc;
  }
  
  /**
   * @link(http://jd.bukkit.org/apidocs/org/bukkit/event/Cancellable.html#isCancelled())
   */
  @Override
  public boolean isCancelled()
  {
	return cancelled;
  }
  
  /**
   * @link(http://jd.bukkit.org/apidocs/org/bukkit/event/Cancellable.html#setCancelled(boolean))
   */
  @Override
  public void setCancelled(boolean cancelled)
  {
	this.cancelled = cancelled;
  }
}
