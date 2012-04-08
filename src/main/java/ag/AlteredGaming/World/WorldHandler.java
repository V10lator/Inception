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
        loadConfiguration();
    }
    
    private void loadConfiguration() {
        try {
            objWorldConfig.load(objWorldConfigFile);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(WorldHandler.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(WorldHandler.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InvalidConfigurationException ex) {
            Logger.getLogger(WorldHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
