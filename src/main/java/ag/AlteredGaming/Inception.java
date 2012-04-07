package ag.AlteredGaming;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipException;
import org.bukkit.plugin.java.JavaPlugin;

public class Inception
        extends JavaPlugin {

    private Logger objLogger;
    private EasyZipFile ezfPluginFile;
    private File objPluginDirectory;
    private String strPluginDirectory;
    private File objWorldConfigDirectory;
    private String strWorldConfigDirectory;
    private File objPluginConfig;
    private String strPluginConfig;

    @Override
    public void onDisable() {
        //Null all variable references to allow the GC to delete these
        objPluginConfig = null;
        strPluginConfig = null;
        objWorldConfigDirectory = null;
        strWorldConfigDirectory = null;
        objPluginDirectory = null;
        strPluginDirectory = null;
        ezfPluginFile = null;

        objLogger.info("Disabled.");
        objLogger = null;
    }

    @Override
    public void onEnable() {
        objLogger = this.getLogger();

        //Plugin files and folders
        try {
            ezfPluginFile = new EasyZipFile(this.getFile());
        } catch (ZipException ex) {
            Logger.getLogger(Inception.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Inception.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NullPointerException ex) {
            Logger.getLogger(Inception.class.getName()).log(Level.SEVERE, null, ex);
        }
        strPluginDirectory = this.getDataFolder().getPath();
        objPluginDirectory = new File(strPluginDirectory);
        strWorldConfigDirectory = strPluginDirectory + "\\per-world\\";
        objWorldConfigDirectory = new File(strWorldConfigDirectory);
        strPluginConfig = strPluginDirectory + "\\config.yml";
        objPluginConfig = new File(strPluginConfig);

        saveDefaultConfig();

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

        }
    }
}
