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
    private int intUpperOverlap;
    private int intUpperTeleport;
    private int intUpperTeleportTarget;
    private boolean bolUpperPreserveEntityVelocity;
    private boolean bolUpperPreserveEntityFallDistance;
    private EnumMap<EntityType, Boolean> ohmUpperEntityFilter;
    private World objLowerWorld;
    private int intLowerOverlapStart;
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
        this.objWorldConfigFile = new File(objPlugin.getWorldConfigDirectoryFile().getPath() + "\\" + objWorld.getName() + ".yml");
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

            objUpperWorld = objPlugin.getServer().getWorld(objWorldConfig.getString("upper.world", ""));
            intUpperOverlapStart = objWorldConfig.getInt("upper.overlapstart", 0);
            intUpperOverlap = objWorldConfig.getInt("upper.overlap", 0);
            intUpperTeleport = objWorldConfig.getInt("upper.teleport", 255);
            intUpperTeleportTarget = objWorldConfig.getInt("upper.teleporttarget", 0);
            bolUpperPreserveEntityVelocity = objWorldConfig.getBoolean("upper.preserveentityvelocity", true);
            bolUpperPreserveEntityFallDistance = objWorldConfig.getBoolean("upper.preserveentityfalldistance", true);
            ohmUpperEntityFilter = new EnumMap<EntityType, Boolean>(EntityType.class);
            for (EntityType et : EntityType.values()) {
                ohmUpperEntityFilter.put(et, objWorldConfig.getBoolean("upper.entityfilter." + et.getName(), false));
            }
            objLowerWorld = objPlugin.getServer().getWorld(objWorldConfig.getString("lower.world", ""));
            intLowerOverlapStart = objWorldConfig.getInt("lower.overlapstart", 255);
            intLowerOverlap = objWorldConfig.getInt("lower.overlap", 0);
            intLowerTeleport = objWorldConfig.getInt("lower.teleport", 0);
            intLowerTeleportTarget = objWorldConfig.getInt("lower.teleporttarget", 255);
            bolLowerPreserveEntityVelocity = objWorldConfig.getBoolean("lower.preserveentityvelocity", true);
            bolLowerPreserveEntityFallDistance = objWorldConfig.getBoolean("lower.preserveentityfalldistance", true);
            ohmLowerEntityFilter = new EnumMap<EntityType, Boolean>(EntityType.class);
            for (EntityType et : EntityType.values()) {
                ohmLowerEntityFilter.put(et, objWorldConfig.getBoolean("lower.entityfilter." + et.getName(), false));
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
        Vector _EntityVelocity = null;
        float _EntityDistanceFallen = 0.0f;
        for (Entity ent : objWorld.getEntities()) {
            if (objUpperWorld != null) {
                if (ohmUpperEntityFilter.get(ent.getType()) == true) {
                    continue;
                }
                if (ent.getLocation().getY() >= intUpperTeleport) {
                    if (bolUpperPreserveEntityVelocity) {
                        _EntityVelocity = ent.getVelocity();
                    }
                    if (bolUpperPreserveEntityFallDistance) {
                        _EntityDistanceFallen = ent.getFallDistance();
                    }

                    Location _UpperWorldExit = new Location(objUpperWorld,
                                                            ent.getLocation().getX(),
                                                            intUpperTeleportTarget - (ent.getLocation().getY() - intUpperTeleport),
                                                            ent.getLocation().getZ());
                    _UpperWorldExit.setPitch(ent.getLocation().getPitch());
                    _UpperWorldExit.setYaw(ent.getLocation().getYaw());
                    /*
                     * Figure out a better way to do this! TODO
                     * //Make space for the entity to stand in.
                     * objUpperWorld.getBlockAt(_UpperWorldExit).setType(Material.AIR);
                     * objUpperWorld.getBlockAt(_UpperWorldExit.add(0, 1,
                     * 0)).setType(Material.AIR);
                     */
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
                if (ent.getLocation().getY() <= intLowerTeleport) {
                    if (bolLowerPreserveEntityVelocity) {
                        _EntityVelocity = ent.getVelocity();
                    }
                    if (bolLowerPreserveEntityFallDistance) {
                        _EntityDistanceFallen = ent.getFallDistance();
                    }

                    Location _LowerWorldExit = new Location(objLowerWorld,
                                                            ent.getLocation().getX(),
                                                            intLowerTeleportTarget + (ent.getLocation().getY() - intLowerTeleport),
                                                            ent.getLocation().getZ());
                    _LowerWorldExit.setPitch(ent.getLocation().getPitch());
                    _LowerWorldExit.setYaw(ent.getLocation().getYaw());
                    /*
                     * Figure out a better way to do this! TODO
                     * //Make space for the entity to stand in.
                     * objLowerWorld.getBlockAt(_LowerWorldExit).setType(Material.AIR);
                     * objLowerWorld.getBlockAt(_LowerWorldExit.add(0, 1,
                     * 0)).setType(Material.AIR);
                     */
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
