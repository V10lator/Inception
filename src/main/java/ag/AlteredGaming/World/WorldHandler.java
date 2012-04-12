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
    private boolean bolDoPredictPosition;
    private int intDelayedTicks;
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
    private int intWorldHandlerRunnableTask = -1;

    public WorldHandler(Inception objPlugin, World objWorld) {
        this.objPlugin = objPlugin;
        this.objWorld = objWorld;
        this.objWorldConfigFile = new File(objPlugin.getWorldConfigDirectoryFile().getPath() + "/" + objWorld.getName() + ".yml");
        this.objWorldConfig = new YamlConfiguration();

        //Check if we need to generate default files and generate them
        saveDefaultConfig();
        loadConfig();
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

            bolDoPredictPosition = objWorldConfig.getBoolean("World.DoPredictPosition", objPlugin.doPredictPosition());
            intDelayedTicks = objWorldConfig.getInt("World.DelayedTicks", objPlugin.getDelayedTicks());

            objUpperWorld = objPlugin.getServer().getWorld(objWorldConfig.getString("Upper.World", objPlugin.getUpperWorld()));
            intUpperOverlapStart = objWorldConfig.getInt("Upper.OverlapStart", objPlugin.getUpperOverlapStart());
            intUpperOverlapTarget = objWorldConfig.getInt("Upper.OverlapTarget", objPlugin.getUpperOverlapTarget());
            intUpperOverlap = objWorldConfig.getInt("Upper.Overlap", objPlugin.getUpperOverlap());
            intUpperTeleport = objWorldConfig.getInt("Upper.Teleport", objPlugin.getUpperTeleport());
            intUpperTeleportTarget = objWorldConfig.getInt("Upper.TeleportTarget", objPlugin.getUpperTeleportTarget());
            bolUpperPreserveEntityVelocity = objWorldConfig.getBoolean("Upper.PreserveEntityVelocity", objPlugin.doPreserveUpperEntityVelocity());
            bolUpperPreserveEntityFallDistance = objWorldConfig.getBoolean("Upper.PreserveEntityFallDistance", objPlugin.doPreserveUpperEntityFallDistance());
            ohmUpperEntityFilter = new EnumMap<EntityType, Boolean>(EntityType.class);
            for (EntityType et : EntityType.values()) {
                ohmUpperEntityFilter.put(et, objWorldConfig.getBoolean("Upper.EntityFilter." + et.getName(), objPlugin.getUpperEntityFilter().get(et)));
            }

            objLowerWorld = objPlugin.getServer().getWorld(objWorldConfig.getString("Lower.World", objPlugin.getLowerWorld()));
            intLowerOverlapStart = objWorldConfig.getInt("Lower.OverlapStart", objPlugin.getLowerOverlapStart());
            intLowerOverlapTarget = objWorldConfig.getInt("Lower.OverlapTarget", objPlugin.getLowerOverlapTarget());
            intLowerOverlap = objWorldConfig.getInt("Lower.Overlap", objPlugin.getLowerOverlap());
            intLowerTeleport = objWorldConfig.getInt("Lower.Teleport", objPlugin.getLowerTeleport());
            intLowerTeleportTarget = objWorldConfig.getInt("Lower.TeleportTarget", objPlugin.getLowerTeleportTarget());
            bolLowerPreserveEntityVelocity = objWorldConfig.getBoolean("Lower.PreserveEntityVelocity", objPlugin.doPreserveLowerEntityVelocity());
            bolLowerPreserveEntityFallDistance = objWorldConfig.getBoolean("Lower.PreserveEntityFallDistance", objPlugin.doPreserveLowerEntityFallDistance());
            ohmLowerEntityFilter = new EnumMap<EntityType, Boolean>(EntityType.class);
            for (EntityType et : EntityType.values()) {
                ohmLowerEntityFilter.put(et, objWorldConfig.getBoolean("Lower.EntityFilter." + et.getName(), objPlugin.getLowerEntityFilter().get(et)));
            }

            //This creates a runnable that calls code in this class
            if (objWorldHandlerRunnable == null) {
                objWorldHandlerRunnable = new WorldHandlerRunnable(objPlugin, this);
            }
            if ((intDelayedTicks > 0)
                && (((objUpperWorld != null) && ((intUpperTeleport <= objWorld.getMaxHeight()) || (intLowerTeleport >= 0)) && ((intUpperTeleportTarget >= 0) && (intUpperTeleportTarget <= objUpperWorld.getMaxHeight())))
                    || ((objLowerWorld != null) && ((intLowerTeleport <= objWorld.getMaxHeight()) || (intLowerTeleport >= 0)) && ((intLowerTeleportTarget >= 0) && (intLowerTeleportTarget <= objLowerWorld.getMaxHeight()))))) {
                if (intWorldHandlerRunnableTask != -1) {
                    objPlugin.getServer().getScheduler().cancelTask(intWorldHandlerRunnableTask);
                }
                intWorldHandlerRunnableTask = objPlugin.getServer().getScheduler().scheduleSyncRepeatingTask(objPlugin, objWorldHandlerRunnable, intDelayedTicks, intDelayedTicks);
                if (intWorldHandlerRunnableTask == -1) {
                    objPlugin.getLogger().warning("<" + objWorld.getName() + "> Could not register SyncRepeatingTask. Entities may not be teleported!");
                }
            }
            if (intWorldHandlerRunnableTask == -1) {
                objPlugin.getLogger().info("<" + objWorld.getName() + "> Teleportation disabled.");
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
