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
import org.bukkit.entity.Enderman;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.Explosive;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.IronGolem;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Ocelot;
import org.bukkit.entity.Painting;
import org.bukkit.entity.Pig;
import org.bukkit.entity.PigZombie;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Sheep;
import org.bukkit.entity.Slime;
import org.bukkit.entity.StorageMinecart;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.entity.Tameable;
import org.bukkit.entity.Vehicle;
import org.bukkit.entity.Villager;
import org.bukkit.entity.WaterMob;
import org.bukkit.entity.Wolf;
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
        if (newEnt instanceof Ageable) {
            ((Ageable) newEnt).setAge(((Ageable) ent).getAge());
            ((Ageable) newEnt).setAgeLock(((Ageable) ent).getAgeLock());
            ((Ageable) newEnt).setBreed(((Ageable) ent).canBreed());
            if (((Ageable) newEnt).isAdult()) {
                ((Ageable) newEnt).setAdult();
            } else {
                ((Ageable) newEnt).setBaby();
            }
        }
        if (newEnt instanceof Boat) {
            ((Boat) newEnt).setMaxSpeed(((Boat) ent).getMaxSpeed());
            ((Boat) newEnt).setOccupiedDeceleration(((Boat) ent).getOccupiedDeceleration());
            ((Boat) newEnt).setUnoccupiedDeceleration(((Boat) ent).getUnoccupiedDeceleration());
            ((Boat) newEnt).setWorkOnLand(((Boat) ent).getWorkOnLand());
        }
        if (newEnt instanceof Creature) {
            ((Creature) newEnt).setTarget(((Creature) ent).getTarget());
        }
        if (newEnt instanceof Creeper) {
            ((Creeper) newEnt).setPowered(((Creeper) ent).isPowered());
        }
        if (newEnt instanceof Enderman) {
            ((Enderman) newEnt).setCarriedMaterial(((Enderman) ent).getCarriedMaterial());
        }
        if (newEnt instanceof ExperienceOrb) {
            ((ExperienceOrb) newEnt).setExperience(((ExperienceOrb) ent).getExperience());
        }
        if (newEnt instanceof Explosive) {
            ((Explosive) newEnt).setIsIncendiary(((Explosive) ent).isIncendiary());
            ((Explosive) newEnt).setYield(((Explosive) ent).getYield());
        }
        if (newEnt instanceof Fireball) {
            ((Fireball) newEnt).setDirection(((Fireball) ent).getDirection());
        }
        if (newEnt instanceof IronGolem) {
            ((IronGolem) newEnt).setPlayerCreated(((IronGolem) ent).isPlayerCreated());
        }
        if (newEnt instanceof Item) {
            ((Item) newEnt).setItemStack(((Item) ent).getItemStack());
            ((Item) newEnt).setPickupDelay(((Item) ent).getPickupDelay());
        }
        if (newEnt instanceof LivingEntity) {
            ((LivingEntity) newEnt).setHealth(((LivingEntity) ent).getHealth());
            ((LivingEntity) newEnt).setLastDamage(((LivingEntity) ent).getLastDamage());
            ((LivingEntity) newEnt).setMaximumAir(((LivingEntity) ent).getMaximumAir());
            ((LivingEntity) newEnt).setMaximumNoDamageTicks(((LivingEntity) ent).getMaximumNoDamageTicks());
            ((LivingEntity) newEnt).setNoDamageTicks(((LivingEntity) ent).getNoDamageTicks());
            ((LivingEntity) newEnt).setRemainingAir(((LivingEntity) ent).getRemainingAir());
            ((LivingEntity) newEnt).addPotionEffects(((LivingEntity) ent).getActivePotionEffects());
        }
        if (newEnt instanceof Minecart) {
            ((Minecart) newEnt).setDamage(((Minecart) ent).getDamage());
            ((Minecart) newEnt).setDerailedVelocityMod(((Minecart) ent).getDerailedVelocityMod());
            ((Minecart) newEnt).setFlyingVelocityMod(((Minecart) ent).getFlyingVelocityMod());
            ((Minecart) newEnt).setMaxSpeed(((Minecart) ent).getMaxSpeed());
            ((Minecart) newEnt).setSlowWhenEmpty(((Minecart) ent).isSlowWhenEmpty());
        }
        if (newEnt instanceof Ocelot) {
            ((Ocelot) newEnt).setCatType(((Ocelot) ent).getCatType());
            ((Ocelot) newEnt).setSitting(((Ocelot) ent).isSitting());
        }
        if (newEnt instanceof Painting) {
            ((Painting) newEnt).setArt(((Painting) ent).getArt());
            ((Painting) newEnt).setFacingDirection(((Painting) ent).getFacing());
        }
        if (newEnt instanceof Pig) {
            ((Pig) newEnt).setSaddle(((Pig) ent).hasSaddle());
        }
        if (newEnt instanceof PigZombie) {
            ((PigZombie) newEnt).setAnger(((PigZombie) ent).getAnger());
            ((PigZombie) newEnt).setAngry(((PigZombie) ent).isAngry());
        }
        if (newEnt instanceof Projectile) {
            ((Projectile) newEnt).setBounce(((Projectile) ent).doesBounce());
            ((Projectile) newEnt).setShooter(((Projectile) ent).getShooter());
        }
        if (newEnt instanceof Sheep) {
            ((Sheep) newEnt).setSheared(((Sheep) ent).isSheared());
            ((Sheep) newEnt).setColor(((Sheep) ent).getColor());
        }
        if (newEnt instanceof Slime) {
            ((Slime) newEnt).setSize(((Slime) ent).getSize());
        }
        if (newEnt instanceof StorageMinecart) {
            ((StorageMinecart) newEnt).getInventory().setContents(((StorageMinecart) ent).getInventory().getContents());
        }
        if (newEnt instanceof Tameable) {
            ((Tameable) newEnt).setTamed(((Tameable) ent).isTamed());
            ((Tameable) newEnt).setOwner(((Tameable) ent).getOwner());
        }
        if (newEnt instanceof TNTPrimed) {
            ((TNTPrimed) newEnt).setFuseTicks(((TNTPrimed) ent).getFuseTicks());
        }
        if (newEnt instanceof Vehicle) {
            ((Vehicle) newEnt).setVelocity(((Vehicle) ent).getVelocity());
        }
        if (newEnt instanceof Villager) {
            ((Villager) newEnt).setProfession(((Villager) ent).getProfession());
        }
        if (newEnt instanceof Wolf) {
            ((Wolf) newEnt).setAngry(((Wolf) ent).isAngry());
            ((Wolf) newEnt).setSitting(((Wolf) ent).isSitting());
        }
        return newEnt;
    }

    public World getWorld() {
        return objWorld;
    }
}