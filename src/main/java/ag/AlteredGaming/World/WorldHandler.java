package ag.AlteredGaming.World;

import ag.AlteredGaming.Inception;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.event.player.PlayerMoveEvent;

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
    private short shtUpperStart;
    private short shtUpperOverlap;
    private short shtUpperTeleport;
    private World objLowerWorld;
    private short shtLowerStart;
    private short shtLowerOverlap;
    private short shtLowerTeleport;
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
            shtUpperStart = (short)objWorldConfig.getInt("upper.start", 0);
            shtUpperOverlap = (short)objWorldConfig.getInt("upper.overlap", 0);
            shtUpperTeleport = (short)objWorldConfig.getInt("upper.teleport", 0);
            
            objLowerWorld = objPlugin.getServer().getWorld(objWorldConfig.getString("lower.world", ""));
            shtLowerStart = (short)objWorldConfig.getInt("lower.start", 0);
            shtLowerOverlap = (short)objWorldConfig.getInt("lower.overlap", 0);
            shtLowerTeleport = (short)objWorldConfig.getInt("lower.teleport", 0);
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

    public void onPlayerMove(PlayerMoveEvent event) {
        if (event.getFrom().getWorld() == objWorld) {
            if (objUpperWorld != null) {
                if (event.getFrom().getY() >= shtUpperTeleport) {
                    Location _UpperWorldExit = new Location(objUpperWorld, event.getFrom().getX(), shtUpperStart + (event.getFrom().getY() - shtUpperTeleport), event.getFrom().getZ());
                    event.getPlayer().teleport(_UpperWorldExit);
                }
            }
            if (objLowerWorld != null) {
                if (event.getFrom().getY() <= shtLowerTeleport) {
                    Location _LowerWorldExit = new Location(objLowerWorld, event.getFrom().getX(), shtLowerStart - (shtLowerTeleport - event.getFrom().getY()), event.getFrom().getZ());
                    event.getPlayer().teleport(_LowerWorldExit);
                }
            }
        }
    }
    
    public void tickEntityMoved() {
        for (Entity ent : objWorld.getEntities()) {
            if (objUpperWorld != null) {
                if (ent.getLocation().getY() >= shtUpperTeleport) {
                    Location _UpperWorldExit = new Location(objUpperWorld, ent.getLocation().getX(), shtUpperStart + (ent.getLocation().getY() - shtUpperTeleport), ent.getLocation().getZ());
                    ent.teleport(_UpperWorldExit);
                }
            }
            if (objLowerWorld != null) {
                if (ent.getLocation().getY() <= shtLowerTeleport) {
                    Location _LowerWorldExit = new Location(objLowerWorld, ent.getLocation().getX(), shtLowerStart - (shtLowerTeleport - ent.getLocation().getY()), ent.getLocation().getZ());
                    ent.teleport(_LowerWorldExit);
                }
            }
        }
    }
}
