package ag.AlteredGaming.Events;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.HandlerList;

public class VehicleWorldToWorldTpEvent extends InceptionEvent
{
  private static final HandlerList handlers = new HandlerList();
  private final LivingEntity vehicle;
  private final Entity passenger;
  
  public VehicleWorldToWorldTpEvent(LivingEntity vehicle, Entity passenger, Location to)
  {
	super(vehicle.getLocation(), to);
	this.passenger = passenger;
	this.vehicle = vehicle;
  }
  
  public LivingEntity getVehicle()
  {
	return vehicle;
  }
  
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
