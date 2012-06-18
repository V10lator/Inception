/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ag.AlteredGaming.API;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;

import ag.AlteredGaming.Inception;
import ag.AlteredGaming.API.Events.EntityWorldToWorldTpEvent;
import ag.AlteredGaming.API.Events.InceptionEvent;
import ag.AlteredGaming.API.Events.ItemWorldToWorldTpEvent;
import ag.AlteredGaming.API.Events.VehicleWorldToWorldTpEvent;
import ag.AlteredGaming.Other.util;

/**
 *
 * @author Xaymar
 */
public class InceptionAPI {
    private Inception objPlugin;
    private final double version = 0.1D;
    
    public InceptionAPI(Inception objPlugin) {
        this.objPlugin = objPlugin;
    }
    
    public boolean isEnabled()
    {
        return objPlugin.isEnabled() && objPlugin.bolGeneralAPIEnabled();
    }
    
    public double getApiVersion()
    {
        return version;
    }
    
    /**
     * Teleports every entity from one world to another.
     * Entity must not be null.
     * @param ent - The entity to teleport.
     * @param to - The location to teleport the entity to.
     * @return - True if the teleport was successful.
     */
    public boolean teleport(Entity ent, Location to)
    {
        //If the entity is is a player bukkits function is just fine.
        if(ent instanceof Player)
            return ent.teleport(to);
        
        InceptionEvent event = null;
        if(isEnabled())
        {
            //Create the event
            if(!ent.isEmpty()) // If the entity is not empty (has a passenger)
                event = new VehicleWorldToWorldTpEvent(ent, ent.getPassenger(), to); // create a new vehicle event
            else if(ent instanceof Item) // else if the entity is a item
  	            event = new ItemWorldToWorldTpEvent((Item)ent, to); // create a item event
            else // else 
  	            event = new EntityWorldToWorldTpEvent(ent, to); // create a entity event
  	    
            //Call the event
            Bukkit.getPluginManager().callEvent(event);
        }
        //Return and work with the result
  	    return event != null && event.isCancelled() ? false : util.entityTeleportEx(ent, event.getTo());
    }
    
}
