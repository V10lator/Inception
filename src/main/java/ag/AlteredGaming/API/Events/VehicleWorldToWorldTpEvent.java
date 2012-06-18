package ag.AlteredGaming.API.Events;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.event.HandlerList;

public class VehicleWorldToWorldTpEvent extends InceptionEvent
{
  private static final HandlerList handlers = new HandlerList();
  private final Entity vehicle;
  private final Entity passenger;
  
  /**
   * The default constructor.
   * @param vehicle - The vehicle to teleport.
   * @param passenger - The passenger inside of the vehicle.
   * @param to - The location to teleport to.
   */
  public VehicleWorldToWorldTpEvent(Entity vehicle, Entity passenger, Location to)
  {
	super(vehicle.getLocation(), to);
	this.passenger = passenger;
	this.vehicle = vehicle;
  }
  
  /**
   * To get the vehicle involved in this event.
   * @return The vehicle to teleport.
   */
  public Entity getVehicle()
  {
	return vehicle;
  }
  
  /**
   * To get the passenger involved in this event.
   * @return The passenger to teleport.
   */
  public Entity getPassenger()
  {
	return passenger;
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
