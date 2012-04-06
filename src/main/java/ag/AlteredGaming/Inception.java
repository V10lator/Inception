package ag.AlteredGaming;

import java.io.File;
import java.util.logging.Logger;
import org.bukkit.plugin.java.JavaPlugin;

public class Inception extends JavaPlugin {

    private Logger objLogger;
    private String strPluginDirectory;
    private String strWorldConfigDirectory;

    @Override
    public void onDisable() {
	objLogger.info("Disabling...");

	objLogger.info("Disabled.");
    }

    @Override
    public void onEnable() {
	objLogger = this.getLogger();

	objLogger.info("Enabling...");
	strPluginDirectory = this.getDataFolder().getPath();
	strWorldConfigDirectory = strPluginDirectory + "world/";

	saveDefaultConfig();

	objLogger.info("Enabled.");
    }

    @Override
    public void saveDefaultConfig() {
	/*
	 * Create directory structure
	 */
	File objPluginDirectory = new File(strPluginDirectory);
	if (objPluginDirectory.mkdir()) {
	    objLogger.fine("Created '" + strPluginDirectory + "'.");
	    File objWorldConfigDirectory = new File(strWorldConfigDirectory);
	    if (objWorldConfigDirectory.mkdir()) {
		objLogger.fine("Created '" + strWorldConfigDirectory + "'.");
	    } else {
		objLogger.warning("Could not create '" + strWorldConfigDirectory + "'.");
	    }
	} else {
	    objLogger.warning("Could not create '" + strPluginDirectory + "'.");
	}
    }
}
