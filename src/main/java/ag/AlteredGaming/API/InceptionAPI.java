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
import ag.AlteredGaming.Events.EntityWorldToWorldTeleportEvent;
import ag.AlteredGaming.Events.InceptionEvent;
import ag.AlteredGaming.Events.ItemWorldToWorldTeleportEvent;
import ag.AlteredGaming.Events.VehicleWorldToWorldTeleportEvent;
import ag.AlteredGaming.Other.util;

/**
 *
 * @author Xaymar
 */
public class InceptionAPI {

    private Inception objPlugin;

    public InceptionAPI(Inception objPlugin) {
        this.objPlugin = objPlugin;
    }

    /**
     * Teleports every entity from one world to another.
     * Entity must not be null.
     * <p/>
     * @param ent - The entity to teleport.
     * @param to  - The location to teleport the entity to.
     * <p/>
     * @return - True if the teleport was successful.
     */
    public boolean teleport(Entity ent, Location to) {
        if (ent instanceof Player) {
            return ent.teleport(to);
        }

        InceptionEvent event;

        //Create the event
        if (!ent.isEmpty()) {
            event = new VehicleWorldToWorldTeleportEvent(ent, ent.getPassenger(), to);
        } else if (ent instanceof Item) {
            event = new ItemWorldToWorldTeleportEvent((Item) ent, to);
        } else {
            event = new EntityWorldToWorldTeleportEvent(ent, to);
        }
        //Call and parse it
        Bukkit.getPluginManager().callEvent(event);
        return event.isCancelled() ? false : util.entityTeleportEx(ent, event.getTo());
    }
}
