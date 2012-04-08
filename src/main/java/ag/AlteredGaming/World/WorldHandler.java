/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ag.AlteredGaming.World;

import ag.AlteredGaming.Inception;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;


/**
 *
 * @author Xaymar
 */
public class WorldHandler {
    private Inception objPlugin;
    private World objWorld;
    private File objWorldConfigFile;
    private YamlConfiguration objWorldConfig;

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
}
