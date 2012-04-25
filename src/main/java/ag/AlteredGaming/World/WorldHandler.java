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
import org.bukkit.entity.Ageable;
import org.bukkit.entity.Boat;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.Explosive;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Monster;
import org.bukkit.entity.NPC;
import org.bukkit.entity.Painting;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Slime;
import org.bukkit.entity.StorageMinecart;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.entity.Vehicle;
import org.bukkit.entity.WaterMob;
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
                    if (_EntityLocation.getY() < intUpperTeleportFrom) {
                        continue;
                    } else {
                        //2. Step: We can't skip it so let's just do what is needed
                        Location _UpperWorldExit = new Location(objUpperWorld,
                                                                ent.getLocation().getX(),
                                                                intUpperTeleportTo - (ent.getLocation().getY() - intUpperTeleportFrom),
                                                                ent.getLocation().getZ());
                        _UpperWorldExit.setPitch(ent.getLocation().getPitch());
                        _UpperWorldExit.setYaw(ent.getLocation().getYaw());
                        ent = entityTeleportEx(ent, _UpperWorldExit);

                        if (bolUpperTeleportPreserveEntityVelocity) {
                            ent.setVelocity(_EntityVelocity);
                        } else {
                            ent.setVelocity(new Vector(0, 0, 0));
                        }
                        if (bolUpperTeleportPreserveEntityFallDistance) {
                            ent.setFallDistance(_EntityDistanceFallen);
                        } else {
                            ent.setFallDistance(0);
                        }
                    }
                }
                if (objLowerWorld != null) {
                    if (ohmLowerTeleportEntityFilter.get(ent.getType()) == true) {
                        continue;
                    }
                    if (_EntityLocation.getY() < intLowerTeleportFrom) {
                        continue;
                    } else {
                        //2. Step: We can't skip it so let's just do what is needed
                        Location _LowerWorldExit = new Location(objLowerWorld,
                                                                ent.getLocation().getX(),
                                                                intLowerTeleportTo - (ent.getLocation().getY() - intLowerTeleportFrom),
                                                                ent.getLocation().getZ());
                        _LowerWorldExit.setPitch(ent.getLocation().getPitch());
                        _LowerWorldExit.setYaw(ent.getLocation().getYaw());
                        ent = entityTeleportEx(ent, _LowerWorldExit);

                        if (bolLowerTeleportPreserveEntityVelocity) {
                            ent.setVelocity(_EntityVelocity);
                        } else {
                            ent.setVelocity(new Vector(0, 0, 0));
                        }
                        if (bolLowerTeleportPreserveEntityFallDistance) {
                            ent.setFallDistance(_EntityDistanceFallen);
                        } else {
                            ent.setFallDistance(0);
                        }
                    }
                }
            }
        }
    }

    public Entity entityTeleportEx(Entity ent, Location loc) {
        /*
         * We skip the following Classes due to being unsure if these actually
         * work out well:
         * - ComplexEntityPart(bail-out)
         * - Player(uses normal teleport method)
         * - Unknown(bail-out)
         * - Weather(bail-out)
         */
        switch (ent.getType()) {
            case COMPLEX_PART:
                return ent;
            case PLAYER:
                ent.teleport(loc);
                return ent;
            case UNKNOWN:
                return ent;
            case WEATHER:
                return ent;
        }
        /*
         * Why do we do this? Because we don't know what they actually are and
         * if these can teleport without breakage. Except player, which just
         * requires teleport.
         */

        Entity newEnt = loc.getWorld().spawnCreature(loc, ent.getType());

        newEnt.setFallDistance(ent.getFallDistance());
        newEnt.setFireTicks(ent.getFireTicks());
        newEnt.setLastDamageCause(ent.getLastDamageCause());
        newEnt.setPassenger(ent.getPassenger());
        newEnt.setTicksLived(ent.getTicksLived());
        newEnt.setVelocity(ent.getVelocity());

        /*
         * We skip some Classes due to them having no properties of
         * their own or other means
         */
        if ((ExperienceOrb) newEnt != null) {
            ((ExperienceOrb) newEnt).setExperience(((ExperienceOrb) ent).getExperience());
        } else if ((Projectile) newEnt != null) {
            ((Projectile) newEnt).setBounce(((Projectile) ent).doesBounce());
            ((Projectile) newEnt).setShooter(((Projectile) ent).getShooter());
        } else if ((Explosive) newEnt != null) {
            ((Explosive) newEnt).setIsIncendiary(((Explosive) ent).isIncendiary());
            ((Explosive) newEnt).setYield(((Explosive) ent).getYield());
            if ((Fireball) newEnt != null) {
                ((Fireball) newEnt).setDirection(((Fireball) ent).getDirection());
            } else if ((TNTPrimed) newEnt != null) {
                ((TNTPrimed) newEnt).setFuseTicks(((TNTPrimed) ent).getFuseTicks());
            }
        } else if ((Item) newEnt != null) {
            ((Item) newEnt).setItemStack(((Item) ent).getItemStack());
            ((Item) newEnt).setPickupDelay(((Item) ent).getPickupDelay());
        } else if ((Vehicle) newEnt != null) {
            if ((Minecart) newEnt != null) {
                ((Minecart) newEnt).setDamage(((Minecart) ent).getDamage());
                ((Minecart) newEnt).setDerailedVelocityMod(((Minecart) ent).getDerailedVelocityMod());
                ((Minecart) newEnt).setFlyingVelocityMod(((Minecart) ent).getFlyingVelocityMod());
                ((Minecart) newEnt).setMaxSpeed(((Minecart) ent).getMaxSpeed());
                ((Minecart) newEnt).setSlowWhenEmpty(((Minecart) ent).isSlowWhenEmpty());
                if ((StorageMinecart) newEnt != null) {
                    ((StorageMinecart) newEnt).getInventory().setContents(((StorageMinecart) ent).getInventory().getContents());
                }
            }
            if ((Boat) newEnt != null) {
                ((Boat) newEnt).setMaxSpeed(((Boat) ent).getMaxSpeed());
                ((Boat) newEnt).setOccupiedDeceleration(((Boat) ent).getOccupiedDeceleration());
                ((Boat) newEnt).setUnoccupiedDeceleration(((Boat) ent).getUnoccupiedDeceleration());
                ((Boat) newEnt).setWorkOnLand(((Boat) ent).getWorkOnLand());
            }
        } else if ((LivingEntity) newEnt != null) {
            ((LivingEntity) newEnt).setHealth(((LivingEntity) ent).getHealth());
            ((LivingEntity) newEnt).setLastDamage(((LivingEntity) ent).getLastDamage());
            ((LivingEntity) newEnt).setMaximumAir(((LivingEntity) ent).getMaximumAir());
            ((LivingEntity) newEnt).setMaximumNoDamageTicks(((LivingEntity) ent).getMaximumNoDamageTicks());
            ((LivingEntity) newEnt).setNoDamageTicks(((LivingEntity) ent).getNoDamageTicks());
            ((LivingEntity) newEnt).setRemainingAir(((LivingEntity) ent).getRemainingAir());
            if ((Slime) newEnt != null) {
                ((Slime) newEnt).setSize(((Slime) ent).getSize());
            } else if ((Creature) newEnt != null) {
                ((Creature) newEnt).setTarget(((Creature) ent).getTarget());
                if ((Monster) newEnt != null) {
                } else if ((WaterMob) newEnt != null) {
                } else if ((Ageable) newEnt != null) {
                } else if ((NPC) newEnt != null) {
                }
            }
        } else if ((Painting) newEnt != null) {
            ((Painting) newEnt).setArt(((Painting) ent).getArt());
            ((Painting) newEnt).setFacingDirection(((Painting) ent).getFacing());
        }
        return newEnt;
    }

    public World getWorld() {
        return objWorld;
    }
}