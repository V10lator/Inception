package ag.AlteredGaming.World;

import ag.AlteredGaming.Inception;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.logging.Level;
import org.bukkit.World;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
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
    private Short shtUpperStart;
    private Short shtUpperOverlap;
    private Short shtUpperTeleport;
    private World objLowerWorld;
    private Short shtLowerStart;
    private Short shtLowerOverlap;
    private Short shtLowerTeleport;

    public WorldHandler(Inception objPlugin, World objWorld) {
        this.objPlugin = objPlugin;
        this.objWorld = objWorld;
        this.objWorldConfigFile = new File(objPlugin.getWorldConfigDirectoryFile().getPath() + "\\" + objWorld.getName() + ".yml");
        this.objWorldConfig = new YamlConfiguration();

        //Check if we need to generate default files and generate them
        saveDefaultConfig();

        loadConfig();
    }

    private void saveDefaultConfig() {
        if (!objWorldConfigFile.exists()) {
            objPlugin.getLogger().finest("'" + objWorldConfigFile.getAbsoluteFile() + "' does not exist, unpacking...");
            objPlugin.getEzfPluginFile().unzipPathAs("world-config.yml", objWorldConfigFile);
        }
    }

    private void loadConfig() {
        try {
            objWorldConfig.load(objWorldConfigFile);

        } catch (FileNotFoundException ex) {
            objPlugin.getLogger().log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            objPlugin.getLogger().log(Level.SEVERE, null, ex);
        } catch (InvalidConfigurationException ex) {
            objPlugin.getLogger().log(Level.SEVERE, null, ex);
        }
    }

    private void saveConfig() {
        try {
            objWorldConfig.save(objWorldConfigFile);
        } catch (IOException ex) {
            objPlugin.getLogger().log(Level.SEVERE, null, ex);
        }
    }

    public void onPlayerMove(PlayerMoveEvent event) {
        if (event.getFrom().getWorld() == objWorld) {
            if (objUpperWorld != null) {
                if (event.getFrom().getY() >= (event.getFrom().getWorld().getMaxHeight() - shtUpperTeleport)) {
                }
            }
            if (objLowerWorld != null) {
                if (event.getFrom().getY() <= shtUpperTeleport) {
                }
            }
        }
    }
}
