package ag.AlteredGaming;

import ag.AlteredGaming.World.WorldHandler;
import ag.AlteredGaming.World.WorldListener;
import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipException;
import org.bukkit.plugin.java.JavaPlugin;

public class Inception
        extends JavaPlugin {

    @SuppressWarnings("NonConstantLogger")
    private Logger objLogger;
    private EasyZipFile ezfPluginFile;
    private File objPluginDirectory;
    private String strPluginDirectory;
    private File objWorldConfigDirectory;
    private String strWorldConfigDirectory;
    private File objPluginConfig;
    private String strPluginConfig;
    //WorldListener to catch world events
    private WorldListener objWorldListener;
    //Holds all WorldHandlers that exist
    private LinkedList<WorldHandler> ollWorldHandlers = new LinkedList<WorldHandler>();

    @Override
    public void onDisable() {
        //Null all variable references to allow the GC to delete these

        objWorldListener = null;

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
    public void onEnable() {
        objLogger = Logger.getLogger(Inception.class.getName());

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

        saveDefaultConfig();



        //Event Listeners
        objWorldListener = new WorldListener(this);
        getServer().getPluginManager().registerEvents(objWorldListener, this);
        objLogger.fine("Registered World Listener...");

        objLogger.info("Enabled.");
    }

    @Override
    public void saveDefaultConfig() {
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
        if (!objPluginConfig.exists()) {
            objLogger.finest("'" + strPluginConfig + "' does not exist, unpacking...");
            ezfPluginFile.unzipPath("config.yml", strPluginDirectory);
        }
    }

    public Logger getLogger() {
        return objLogger;
    }

    public EasyZipFile getEzfPluginFile() {
        return ezfPluginFile;
    }

    public WorldListener getWorldListener() {
        return objWorldListener;
    }

    public LinkedList<WorldHandler> getWorldHandlers() {
        return ollWorldHandlers;
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
