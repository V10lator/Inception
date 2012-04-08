package ag.AlteredGaming;

import ag.AlteredGaming.World.PlayerListener;
import ag.AlteredGaming.World.WorldHandler;
import ag.AlteredGaming.World.WorldListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipException;
import org.bukkit.World;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class Inception
        extends JavaPlugin {

    @SuppressWarnings("NonConstantLogger")
    private Logger objLogger;
    private EasyZipFile ezfPluginFile;
    //FPlugin files(with String path)
    private String strPluginDirectory;
    private String strWorldConfigDirectory;
    private String strPluginConfig;
    private File objPluginDirectory;
    private File objWorldConfigDirectory;
    private File objPluginConfig;
    //Configuration Stuff
    private YamlConfiguration objConfiguration;
    //WorldListener to catch world events
    private WorldListener objWorldListener;
    private PlayerListener objPlayerListener;
    //Holds all WorldHandlers that exist
    private HashMap<World, WorldHandler> ohmWorldHandlers;

    @Override
    public void onEnable() {
        objLogger = super.getLogger();//Logger.getLogger(Inception.class.getName());

        //Plugin files and folders
        try {
            ezfPluginFile = new EasyZipFile(this.getFile());
        } catch (ZipException ex) {
            objLogger.log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            objLogger.log(Level.SEVERE, null, ex);
        } catch (NullPointerException ex) {
            objLogger.log(Level.SEVERE, null, ex);
        }
        strPluginDirectory = this.getDataFolder().getPath();
        objPluginDirectory = new File(strPluginDirectory);
        strWorldConfigDirectory = strPluginDirectory + "\\per-world\\";
        objWorldConfigDirectory = new File(strWorldConfigDirectory);
        strPluginConfig = strPluginDirectory + "\\config.yml";
        objPluginConfig = new File(strPluginConfig);
        
        /*
         * Create base folder structure
         */
        if (!objPluginDirectory.exists()) {
            objLogger.finest("'" + strPluginDirectory + "' does not exist, creating...");
            if (objPluginDirectory.mkdir()) {
                objLogger.finest("Created '" + strPluginDirectory + "'.");
            } else {
                objLogger.warning("Unable to create '" + strPluginDirectory + "'.");
            }
        }
        if (!objWorldConfigDirectory.exists()) {
            objLogger.finest("'" + strWorldConfigDirectory + "' does not exist, creating...");
            if (objWorldConfigDirectory.mkdir()) {
                objLogger.info("Created '" + strWorldConfigDirectory + "'.");
            } else {
                objLogger.warning("Unable to create '" + strWorldConfigDirectory + "'.");
            }
        }
        //Check if we need to create the default configuration file
        saveDefaultConfig();

        //Configuration Stuff
        objConfiguration = new YamlConfiguration();
        loadConfig();

        //Hashmap for WorldHandler storage
        ohmWorldHandlers = new HashMap<World, WorldHandler>();
        
        //Event Listeners
        objLogger.fine("Registering World Listener...");
        objWorldListener = new WorldListener(this);
        getServer().getPluginManager().registerEvents(objWorldListener, this);
        objLogger.fine("Registering Player Listener...");
        objPlayerListener = new PlayerListener(this);
        getServer().getPluginManager().registerEvents(objPlayerListener, this);

        objLogger.info("Enabled.");
    }

    @Override
    public void onDisable() {
        //Null all variable references to allow the GC to delete these
        ohmWorldHandlers.clear();
        ohmWorldHandlers = null;
        objPlayerListener = null;
        objWorldListener = null;
        objConfiguration = null;
        objPluginConfig = null;
        strPluginConfig = null;
        objWorldConfigDirectory = null;
        strWorldConfigDirectory = null;
        objPluginDirectory = null;
        strPluginDirectory = null;
        try {
            ezfPluginFile.close();
        } catch (IOException ex) {
            objLogger.log(Level.SEVERE, null, ex);
        }
        ezfPluginFile = null;

        objLogger.info("Disabled.");
        objLogger = null;
    }

    @Override
    public void saveDefaultConfig() {
        if (!objPluginConfig.exists()) {
            objLogger.finest("'" + strPluginConfig + "' does not exist, unpacking...");
            ezfPluginFile.unzipPathAs("config.yml", objPluginConfig);
        }
    }

    public void loadConfig() {
        if (!objPluginConfig.exists()) {
            saveDefaultConfig();
        }
        try {
            objConfiguration.load(objPluginConfig);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Inception.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Inception.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InvalidConfigurationException ex) {
            Logger.getLogger(Inception.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void saveConfig() {
        try {
            objConfiguration.save(objPluginConfig);
        } catch (IOException ex) {
            Logger.getLogger(Inception.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public Logger getLogger() {
        if (objLogger != null) {
            return objLogger;
        } else {
            return Logger.getLogger(Inception.class.getName());
        }
    }
    
    public EasyZipFile getEzfPluginFile() {
        return ezfPluginFile;
    }

    public WorldListener getWorldListener() {
        return objWorldListener;
    }

    public HashMap<World, WorldHandler> getWorldHandlers() {
        return ohmWorldHandlers;
    }

    public String getPluginConfigPath() {
        return strPluginConfig;
    }

    public File getPluginConfigFile() {
        return objPluginConfig;
    }

    public String getPluginDirectoryPath() {
        return strPluginDirectory;
    }

    public File getPluginDirectoryFile() {
        return objPluginDirectory;
    }

    public String getWorldConfigDirectoryPath() {
        return strWorldConfigDirectory;
    }

    public File getWorldConfigDirectoryFile() {
        return objWorldConfigDirectory;
    }
}
