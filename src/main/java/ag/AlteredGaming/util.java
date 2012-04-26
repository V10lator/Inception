package ag.AlteredGaming;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.bind.TypeConstraintException;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Ageable;
import org.bukkit.entity.Boat;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Enderman;
import org.bukkit.entity.Entity;
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
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Sheep;
import org.bukkit.entity.Slime;
import org.bukkit.entity.StorageMinecart;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.entity.Tameable;
import org.bukkit.entity.Vehicle;
import org.bukkit.entity.Villager;
import org.bukkit.entity.Wolf;
import org.bukkit.util.Vector;

/**
 *
 * @author Xaymar
 */
public class util {

    public static void copyInputStream(InputStream in, OutputStream out)
            throws IOException {
        copyInputStream(in, out, 1024);
    }

    public static void copyInputStream(InputStream in, OutputStream out, int BufferLength)
            throws IOException {
        byte[] buffer = new byte[BufferLength];
        int len;
        while ((len = in.read(buffer)) >= 0) {
            out.write(buffer, 0, len);
        }
    }

    public static String[] arraySplit(String split, String delimiter) {
        return split.split(delimiter);
    }

    public static String arrayCombine(String[] array, String delimiter) {
        String output = "";
        for (String word : array) {
            output += (output.isEmpty() ? "" : delimiter) + word;
        }
        return output;
    }

    public static String[] smartSplit(String[] args) {
        return smartSplit(arrayCombine(args, " "));
    }

    public static String[] smartSplit(String text) {
        ArrayList<String> list = new ArrayList<String>();
        Matcher match = Pattern.compile("[^\\s\"']+|\"([^\"]*)\"|'([^']*)'").matcher(text);
        while (match.find()) {
            list.add(match.group(1) != null ? match.group(1) : match.group(2) != null ? match.group(2) : match.group());
        }
        return list.toArray(new String[list.size()]);
    }

    public static String[] reparseArgs(String[] args) {
        return smartSplit(args);
    }

    public static String substitude(String On, String[] What, String[] With) {
        if (What.length != With.length) {
            throw new java.lang.ArrayIndexOutOfBoundsException();
        }

        for (int count = 0; count < What.length; count++) {
            if (What[count].contains(",")) {
                String[] WhatArgs = What[count].split(",");
                for (String arg : WhatArgs) {
                    On = On.replace(arg, With[count]);
                }
            } else {
                On = On.replace(What[count], With[count]);
            }
        }

        return On;
    }

    public static String colorize(String On) {
        return ChatColor.translateAlternateColorCodes("&".charAt(0), On);
    }

    public static <T> CommandSender senderFromName(T player) {
        if (player instanceof CommandSender) {
            return (CommandSender) player;
        } else if (player instanceof Player) {
            return (Player) player;
        } else if (player instanceof String) {
            return Bukkit.getPlayerExact((String) player);
        } else {
            throw new TypeConstraintException("'player' must be CommandSender, Player or String");
        }
    }

    public static Entity entityTeleportEx(Entity ent, Location loc) {
        /*
         * We skip the following Classes due to being unsure if these actually
         * work out well:
         * - ComplexEntityPart(bail-out)
         * - Player(uses normal teleport method)
         * - Unknown(bail-out)
         * - Weather(bail-out)
         */
        Entity newEnt = null;
        //Bukkit logic: Almost all things can be directly created but some. Fucking get some standards up, bukkit team!
        switch (ent.getType()) {
            /*
             * Early-Exit because these have no Entity ID. We'll still try to
             * teleport these.
             */
            case PLAYER:
            case SPLASH_POTION:
            case EGG:
            case FISHING_HOOK:
            case LIGHTNING:
            case WEATHER:
            case COMPLEX_PART:
            case UNKNOWN:
                float flFallDistance = ent.getFallDistance();
                Vector vtVelocity = ent.getVelocity();
                ent.teleport(loc);
                ent.setFallDistance(flFallDistance);
                ent.setVelocity(vtVelocity);
                return ent;
            /*
             * Mobs and NPCs
             */
            case CREEPER:
            case SKELETON:
            case SPIDER:
            case GIANT:
            case ZOMBIE:
            case SLIME:
            case GHAST:
            case PIG_ZOMBIE:
            case ENDERMAN:
            case CAVE_SPIDER:
            case SILVERFISH:
            case BLAZE:
            case MAGMA_CUBE:
            case ENDER_DRAGON:
            case PIG:
            case SHEEP:
            case COW:
            case CHICKEN:
            case SQUID:
            case WOLF:
            case MUSHROOM_COW:
            case SNOWMAN:
            case OCELOT:
            case IRON_GOLEM:
            case VILLAGER:
                newEnt = loc.getWorld().spawnCreature(loc, ent.getType());
                break;
            /*
             * Items can't be spawned and need to be dropped instead.
             */
            case DROPPED_ITEM:
                newEnt = loc.getWorld().dropItem(loc, ((Item) ent).getItemStack());
                break;
            /*
             * Now we can continue with whats left...
             */
            default:
                newEnt = loc.getWorld().spawn(loc, ent.getClass());
                break;
        }

        newEnt.setFallDistance(ent.getFallDistance());
        newEnt.setFireTicks(ent.getFireTicks());
        newEnt.setLastDamageCause(ent.getLastDamageCause());
        newEnt.setPassenger(ent.getPassenger());
        newEnt.setTicksLived((ent.getTicksLived() > 0 ? ent.getTicksLived() : 1));
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
            ((LivingEntity) newEnt).setHealth((((LivingEntity) ent).getHealth() >= 0 ? ((LivingEntity) ent).getHealth() : 0 ));
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

        ent.remove();

        return newEnt;
    }
}
