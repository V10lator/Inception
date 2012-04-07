package ag.AlteredGaming;

import java.io.File;
import java.util.logging.Logger;
import org.bukkit.plugin.java.JavaPlugin;

public class Inception extends JavaPlugin {

    private Logger objLogger;
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

	objLogger.info("Disabled.");
	objLogger = null;
    }

    @Override
    public void onEnable() {
	objLogger = this.getLogger();

	//Files
	strPluginDirectory = this.getDataFolder().getPath();
	objPluginDirectory = new File(strPluginDirectory);
	strWorldConfigDirectory = strPluginDirectory + "/per-world/";
	objWorldConfigDirectory = new File(strWorldConfigDirectory);
	strPluginConfig = strPluginDirectory + "config.yml";
	objPluginConfig = new File(strPluginConfig);

	saveDefaultConfig();

	objLogger.info("Enabled.");
    }

    @Override
    public void saveDefaultConfig() {
	/*
	 * Create base folder structure
	 */
	if (objPluginDirectory.exists()) {
	    if (objPluginDirectory.mkdir()) {
		objLogger.info("Created '" + strPluginDirectory + "'.");
	    } else {
		objLogger.warning("Could not create '" + strPluginDirectory + "'.");
	    }
	}
	if (!objWorldConfigDirectory.exists()) {
	    if (objWorldConfigDirectory.mkdir()) {
		objLogger.info("Created '" + strWorldConfigDirectory + "'.");
	    } else {
		objLogger.warning("Could not create '" + strWorldConfigDirectory + "'.");
	    }
	}
	if (!objPluginConfig.exists()) {
	}
    }
}
