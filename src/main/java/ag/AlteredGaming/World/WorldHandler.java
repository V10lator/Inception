package ag.AlteredGaming.World;

import ag.AlteredGaming.Inception;
import ag.AlteredGaming.util;
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
    private World objWorld;
    private File objWorldConfigFile;
    private YamlConfiguration objWorldConfig;
    private boolean bolIsEnabled;
    private boolean bolDoPredictPosition;
    private int intDelayedTicks;
    private World objUpperWorld;
    private boolean bolUpperOverlapEnabled;
    private int intUpperOverlapFrom;
    private int intUpperOverlapTo;
    private int intUpperOverlapLayers;
    private boolean bolUpperTeleportEnabled;
    private int intUpperTeleportFrom;
    private int intUpperTeleportTo;
    private boolean bolUpperTeleportPreserveEntityVelocity;
    private boolean bolUpperTeleportPreserveEntityFallDistance;
    private EnumMap<EntityType, Boolean> ohmUpperTeleportEntityFilter;
    private World objLowerWorld;
    private boolean bolLowerOverlapEnabled;
    private int intLowerOverlapFrom;
    private int intLowerOverlapTo;
    private int intLowerOverlapLayers;
    private boolean bolLowerTeleportEnabled;
    private int intLowerTeleportFrom;
    private int intLowerTeleportTo;
    private boolean bolLowerTeleportPreserveEntityVelocity;
    private boolean bolLowerTeleportPreserveEntityFallDistance;
    private EnumMap<EntityType, Boolean> ohmLowerTeleportEntityFilter;
    private WorldHandlerRunnable objWorldHandlerRunnable;
    private int intWorldHandlerRunnableTask = -1;

    public WorldHandler(Inception objPlugin, World objWorld) {
        this.objPlugin = objPlugin;
        this.objWorld = objWorld;
        this.objWorldConfigFile = new File(objPlugin.getWorldConfigDirectoryFile().getPath() + "/" + objWorld.getName() + ".yml");
        this.objWorldConfig = new YamlConfiguration();

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
            saveDefaultConfig();
            objWorldConfig.load(objWorldConfigFile);

            bolIsEnabled = objWorldConfig.getBoolean("World.Enabled", objPlugin.bolDefaultIsEnabled());
            bolDoPredictPosition = objWorldConfig.getBoolean("World.DoPredictPosition", objPlugin.bolDefaultDoPredictPosition());
            intDelayedTicks = objWorldConfig.getInt("World.DelayedTicks", objPlugin.intDefaultDelayedTicks());

            if (bolIsEnabled == true) {

                objUpperWorld = objPlugin.getServer().getWorld(objWorldConfig.getString("Upper.World", objPlugin.strDefaultUpperWorld()));
                bolUpperOverlapEnabled = objWorldConfig.getBoolean("Upper.Overlap.Enabled", objPlugin.bolDefaultUpperOverlapEnabled());
                intUpperOverlapFrom = objWorldConfig.getInt("Upper.Overlap.From", objPlugin.intDefaultUpperOverlapFrom());
                intUpperOverlapTo = objWorldConfig.getInt("Upper.Overlap.To", objPlugin.intDefaultUpperOverlapTo());
                intUpperOverlapLayers = objWorldConfig.getInt("Upper.Overlap.Layers", objPlugin.intDefaultUpperOverlapLayers());
                bolUpperTeleportEnabled = objWorldConfig.getBoolean("Upper.Teleport.Enabled", objPlugin.bolDefaultUpperTeleportEnabled());
                intUpperTeleportFrom = objWorldConfig.getInt("Upper.Teleport.From", objPlugin.intDefaultUpperTeleportFrom());
                intUpperTeleportTo = objWorldConfig.getInt("Upper.Teleport.To", objPlugin.intDefaultUpperTeleportTo());
                bolUpperTeleportPreserveEntityVelocity = objWorldConfig.getBoolean("Upper.PreserveEntityVelocity", objPlugin.bolDefaultUpperTeleportPreserveEntityVelocity());
                bolUpperTeleportPreserveEntityFallDistance = objWorldConfig.getBoolean("Upper.PreserveEntityFallDistance", objPlugin.bolDefaultUpperTeleportPreserveEntityFallDistance());
                ohmUpperTeleportEntityFilter = new EnumMap<EntityType, Boolean>(EntityType.class);
                for (EntityType et : EntityType.values()) {
                    ohmUpperTeleportEntityFilter.put(et, objWorldConfig.getBoolean("Upper.Teleport.EntityFilter." + et.getName(), objPlugin.ohmDefaultUpperTeleportEntityFilter().get(et)));
                }

                objLowerWorld = objPlugin.getServer().getWorld(objWorldConfig.getString("Lower.World", objPlugin.strDefaultLowerWorld()));
                bolLowerOverlapEnabled = objWorldConfig.getBoolean("Lower.Overlap.Enabled", objPlugin.bolDefaultLowerOverlapEnabled());
                intLowerOverlapFrom = objWorldConfig.getInt("Lower.Overlap.From", objPlugin.intDefaultLowerOverlapFrom());
                intLowerOverlapTo = objWorldConfig.getInt("Lower.Overlap.To", objPlugin.intDefaultLowerOverlapTo());
                intLowerOverlapLayers = objWorldConfig.getInt("Lower.Overlap.Layers", objPlugin.intDefaultLowerOverlapLayers());
                bolLowerTeleportEnabled = objWorldConfig.getBoolean("Lower.Teleport.Enabled", objPlugin.bolDefaultLowerTeleportEnabled());
                intLowerTeleportFrom = objWorldConfig.getInt("Lower.Teleport.From", objPlugin.intDefaultLowerTeleportFrom());
                intLowerTeleportTo = objWorldConfig.getInt("Lower.Teleport.To", objPlugin.intDefaultLowerTeleportTo());
                bolLowerTeleportPreserveEntityVelocity = objWorldConfig.getBoolean("Lower.PreserveEntityVelocity", objPlugin.bolDefaultLowerTeleportPreserveEntityVelocity());
                bolLowerTeleportPreserveEntityFallDistance = objWorldConfig.getBoolean("Lower.PreserveEntityFallDistance", objPlugin.bolDefaultLowerTeleportPreserveEntityFallDistance());
                ohmLowerTeleportEntityFilter = new EnumMap<EntityType, Boolean>(EntityType.class);
                for (EntityType et : EntityType.values()) {
                    ohmLowerTeleportEntityFilter.put(et, objWorldConfig.getBoolean("Lower.Teleport.EntityFilter." + et.getName(), objPlugin.ohmDefaultLowerTeleportEntityFilter().get(et)));
                }
                //This creates a runnable that calls code in this class
                if (objWorldHandlerRunnable == null) {
                    objWorldHandlerRunnable = new WorldHandlerRunnable(objPlugin, this);
                }
                if ((intDelayedTicks > 0)
                    && (((objUpperWorld != null) && (bolUpperTeleportEnabled == true))
                        || ((objLowerWorld != null) && (bolLowerTeleportEnabled == true)))) {
                    if (intWorldHandlerRunnableTask != -1) {
                        objPlugin.getServer().getScheduler().cancelTask(intWorldHandlerRunnableTask);
                    }
                    intWorldHandlerRunnableTask = objPlugin.getServer().getScheduler().scheduleSyncRepeatingTask(objPlugin, objWorldHandlerRunnable, intDelayedTicks, intDelayedTicks);
                    if (intWorldHandlerRunnableTask == -1) {
                        objPlugin.getLogger().warning("<" + objWorld.getName() + "> Could not register synchronized repeating task. Entities can not be teleported!");
                    } else {
                        objPlugin.getLogger().info("<" + objWorld.getName() + "> WorldHandler enabled.");
                    }
                }
                if (intWorldHandlerRunnableTask == -1) {
                    objPlugin.getLogger().info("<" + objWorld.getName() + "> Teleportation disabled.");
                }
            } else {
                if (intWorldHandlerRunnableTask != -1) {
                    objPlugin.getServer().getScheduler().cancelTask(intWorldHandlerRunnableTask);
                    intWorldHandlerRunnableTask = -1;
                }
                objPlugin.getLogger().info("<" + objWorld.getName() + "> WorldHandler disabled.");
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

            if (bolDoPredictPosition) {
                //Advance the entites position by their velocity * objPlugin.getDelayedTicks().
                _EntityLocation.setX(_EntityLocation.getX() + _EntityVelocity.getX() * intDelayedTicks);
                _EntityLocation.setY(_EntityLocation.getY() + _EntityVelocity.getY() * intDelayedTicks);
                _EntityLocation.setZ(_EntityLocation.getZ() + _EntityVelocity.getZ() * intDelayedTicks);
            }

            //1. Step: Check if we can skip this entity. Helps save CPU time.
            if ((objUpperWorld == null) && (objLowerWorld == null)) {
                continue;
            } else {
                if (objUpperWorld != null) {
                    if (ohmUpperTeleportEntityFilter.get(ent.getType()) == true) {
                        continue;
                    }
                    if (_EntityLocation.getY() > intUpperTeleportFrom) {
                        //2. Step: We can't skip it so let's just do what is needed
                        Location _UpperWorldExit = new Location(objUpperWorld,
                                                                ent.getLocation().getX(),
                                                                intUpperTeleportTo - (ent.getLocation().getY() - intUpperTeleportFrom),
                                                                ent.getLocation().getZ());
                        _UpperWorldExit.setPitch(ent.getLocation().getPitch());
                        _UpperWorldExit.setYaw(ent.getLocation().getYaw());
                        Entity tent = util.entityTeleportEx(ent, _UpperWorldExit);

                        if (!bolUpperTeleportPreserveEntityVelocity) {
                            tent.setVelocity(new Vector(0, 0, 0));
                        }
                        if (!bolUpperTeleportPreserveEntityFallDistance) {
                            tent.setFallDistance(0);
                        }
                    }
                }
                if (objLowerWorld != null) {
                    if (ohmLowerTeleportEntityFilter.get(ent.getType()) == true) {
                        continue;
                    }
                    if (_EntityLocation.getY() < intLowerTeleportFrom) {
                        //2. Step: We can't skip it so let's just do what is needed
                        Location _LowerWorldExit = new Location(objLowerWorld,
                                                                ent.getLocation().getX(),
                                                                intLowerTeleportTo - (ent.getLocation().getY() - intLowerTeleportFrom),
                                                                ent.getLocation().getZ());
                        _LowerWorldExit.setPitch(ent.getLocation().getPitch());
                        _LowerWorldExit.setYaw(ent.getLocation().getYaw());
                        Entity tent = util.entityTeleportEx(ent, _LowerWorldExit);

                        if (!bolLowerTeleportPreserveEntityVelocity) {
                            tent.setVelocity(new Vector(0, 0, 0));
                        }
                        if (!bolLowerTeleportPreserveEntityFallDistance) {
                            tent.setFallDistance(0);
                        }
                    }
                }
            }
        }
    }

    public World getWorld() {
        return objWorld;
    }
}