package ag.AlteredGaming.World;

import ag.AlteredGaming.Inception;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.EnumMap;
import java.util.logging.Level;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.util.Vector;

/**
 *
 * @author Xaymar
 */
public class WorldHandler {

    private Inception objPlugin;
    private File objWorldConfigFile;
    private YamlConfiguration objWorldConfig;
    private World objWorld;
    private World objUpperWorld;
    private int intUpperOverlapStart;
    private int intUpperOverlapTarget;
    private int intUpperOverlap;
    private int intUpperTeleport;
    private int intUpperTeleportTarget;
    private boolean bolUpperPreserveEntityVelocity;
    private boolean bolUpperPreserveEntityFallDistance;
    private EnumMap<EntityType, Boolean> ohmUpperEntityFilter;
    private World objLowerWorld;
    private int intLowerOverlapStart;
    private int intLowerOverlapTarget;
    private int intLowerOverlap;
    private int intLowerTeleport;
    private int intLowerTeleportTarget;
    private boolean bolLowerPreserveEntityVelocity;
    private boolean bolLowerPreserveEntityFallDistance;
    private EnumMap<EntityType, Boolean> ohmLowerEntityFilter;
    private WorldHandlerRunnable objWorldHandlerRunnable;
    private int intWorldHandlerRunnableTask;

    public WorldHandler(Inception objPlugin, World objWorld) {
        this.objPlugin = objPlugin;
        this.objWorld = objWorld;
        this.objWorldConfigFile = new File(objPlugin.getWorldConfigDirectoryFile().getPath() + "/" + objWorld.getName() + ".yml");
        this.objWorldConfig = new YamlConfiguration();

        //Check if we need to generate default files and generate them
        saveDefaultConfig();
        loadConfig();

        //Create per-tick check if an Entity is in the teleport area
        objWorldHandlerRunnable = new WorldHandlerRunnable(objPlugin, this);
        intWorldHandlerRunnableTask = objPlugin.getServer().getScheduler().scheduleSyncRepeatingTask(objPlugin, objWorldHandlerRunnable, 0, 1);
        if (intWorldHandlerRunnableTask == -1) {
            objPlugin.getLogger().warning("Could not register WorldHandlerRunnable. Entities will not be teleported!");
        }
    }

    public void saveDefaultConfig() {
        if (!objWorldConfigFile.exists()) {
            objPlugin.getLogger().finest("'" + objWorldConfigFile.getAbsoluteFile() + "' does not exist, unpacking...");
            objPlugin.getEzfPluginFile().unzipPathAs("world-config.yml", objWorldConfigFile);
        }
    }

    public void loadConfig() {
        try {
            objWorldConfig.load(objWorldConfigFile);

            objUpperWorld = objPlugin.getServer().getWorld(objWorldConfig.getString("Upper.World", ""));
            intUpperOverlapStart = objWorldConfig.getInt("Upper.OverlapStart", 255);
            intUpperOverlapTarget = objWorldConfig.getInt("Upper.OverlapTarget", 0);
            intUpperOverlap = objWorldConfig.getInt("Upper.Overlap", 0);
            intUpperTeleport = objWorldConfig.getInt("Upper.Teleport", 255);
            intUpperTeleportTarget = objWorldConfig.getInt("Upper.TeleportTarget", 1);
            bolUpperPreserveEntityVelocity = objWorldConfig.getBoolean("Upper.PreserveEntityVelocity", true);
            bolUpperPreserveEntityFallDistance = objWorldConfig.getBoolean("Upper.PreserveEntityFallDistance", true);
            ohmUpperEntityFilter = new EnumMap<EntityType, Boolean>(EntityType.class);
            for (EntityType et : EntityType.values()) {
                ohmUpperEntityFilter.put(et, objWorldConfig.getBoolean("Upper.EntityFilter." + et.getName(), false));
            }
            
            objLowerWorld = objPlugin.getServer().getWorld(objWorldConfig.getString("Lower.World", ""));
            intLowerOverlapStart = objWorldConfig.getInt("Lower.OverlapStart", 0);
            intLowerOverlapTarget = objWorldConfig.getInt("Lower.OverlapTarget", 255);
            intLowerOverlap = objWorldConfig.getInt("Lower.Overlap", 0);
            intLowerTeleport = objWorldConfig.getInt("Lower.Teleport", 255);
            intLowerTeleportTarget = objWorldConfig.getInt("Lower.TeleportTarget", 1);
            bolLowerPreserveEntityVelocity = objWorldConfig.getBoolean("Lower.PreserveEntityVelocity", true);
            bolLowerPreserveEntityFallDistance = objWorldConfig.getBoolean("Lower.PreserveEntityFallDistance", true);
            ohmLowerEntityFilter = new EnumMap<EntityType, Boolean>(EntityType.class);
            for (EntityType et : EntityType.values()) {
                ohmLowerEntityFilter.put(et, objWorldConfig.getBoolean("Lower.EntityFilter." + et.getName(), false));
            }
        } catch (FileNotFoundException ex) {
            objPlugin.getLogger().log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            objPlugin.getLogger().log(Level.SEVERE, null, ex);
        } catch (InvalidConfigurationException ex) {
            objPlugin.getLogger().log(Level.SEVERE, null, ex);
        }
    }

    public void saveConfig() {
        try {
            objWorldConfig.save(objWorldConfigFile);
        } catch (IOException ex) {
            objPlugin.getLogger().log(Level.SEVERE, null, ex);
        }
    }

    public void tickEntityMoved() {
        for (Entity ent : objWorld.getEntities()) {
            Location _EntityLocation = ent.getLocation();
            Vector _EntityVelocity = ent.getVelocity();
            float _EntityDistanceFallen = ent.getFallDistance();

            if (objPlugin.doPredictPosition()) {
                //Advance the entites position by their velocity * objPlugin.getDelayedTicks().
                _EntityLocation.setX(_EntityLocation.getX() + _EntityVelocity.getX() * objPlugin.getDelayedTicks());
                _EntityLocation.setY(_EntityLocation.getY() + _EntityVelocity.getY() * objPlugin.getDelayedTicks());
                _EntityLocation.setZ(_EntityLocation.getZ() + _EntityVelocity.getZ() * objPlugin.getDelayedTicks());
            }

            //1. Step: Check if we can skip this entity. Helps save CPU time.
            if ((objUpperWorld == null) && (objLowerWorld == null)) {
                continue;
            } else {
                if (objUpperWorld != null) {
                    if (ohmUpperEntityFilter.get(ent.getType()) == true) {
                        continue;
                    }
                    if (_EntityLocation.getY() < intUpperTeleport) {
                        continue;
                    } else {
                        //2. Step: We can't skip it so let's just do what is needed
                        Location _UpperWorldExit = new Location(objUpperWorld,
                                                                ent.getLocation().getX(),
                                                                intUpperTeleportTarget - (ent.getLocation().getY() - intUpperTeleport),
                                                                ent.getLocation().getZ());
                        _UpperWorldExit.setPitch(ent.getLocation().getPitch());
                        _UpperWorldExit.setYaw(ent.getLocation().getYaw());
                        ent.teleport(_UpperWorldExit);

                        if (bolUpperPreserveEntityVelocity) {
                            ent.setVelocity(_EntityVelocity);
                        }
                        if (bolUpperPreserveEntityFallDistance) {
                            ent.setFallDistance(_EntityDistanceFallen);
                        }
                    }
                }
                if (objLowerWorld != null) {
                    if (ohmLowerEntityFilter.get(ent.getType()) == true) {
                        continue;
                    }
                    if (_EntityLocation.getY() > intLowerTeleport) {
                        continue;
                    } else {
                        //2. Step: We can't skip it so let's just do what is needed
                        Location _LowerWorldExit = new Location(objLowerWorld,
                                                                ent.getLocation().getX(),
                                                                intLowerTeleportTarget + (ent.getLocation().getY() - intLowerTeleport),
                                                                ent.getLocation().getZ());
                        _LowerWorldExit.setPitch(ent.getLocation().getPitch());
                        _LowerWorldExit.setYaw(ent.getLocation().getYaw());
                        ent.teleport(_LowerWorldExit);

                        if (bolLowerPreserveEntityVelocity) {
                            ent.setVelocity(_EntityVelocity);
                        }
                        if (bolLowerPreserveEntityFallDistance) {
                            ent.setFallDistance(_EntityDistanceFallen);
                        }
                    }
                }
            }
        }
    }

    public World getLowerWorld() {
        return objLowerWorld;
    }

    public World getUpperWorld() {
        return objUpperWorld;
    }

    public World getWorld() {
        return objWorld;
    }
}
