package Inception.Main;

import Inception.Other.EasyZipFile;
import Inception.Other.Triggers;
import Inception.Other.util;
import Inception.API.InceptionAPI;
import Inception.World.WorldHandler;
import Inception.World.WorldListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.plugin.java.JavaPlugin;

public class Inception
        extends JavaPlugin {

    private Logger objLogger;
    private String strPrefix;
    /*
     * Internal Inception Values
     */
    private WorldListener objWorldListener;
    private HashMap<World, WorldHandler> mapWorldHandlers = new HashMap<World, WorldHandler>();
    /*
     * Inception Plugin Files
     */
    private EasyZipFile objPluginFile;
    private String strPluginDirectory;
    private String strWorldConfigDirectory;
    private String strPluginConfig;
    private File objPluginDirectory;
    private File objWorldConfigDirectory;
    private File objPluginConfig;
    /*
     * Inception Configuration Variables
     */
    private YamlConfiguration objConfiguration = new YamlConfiguration();
    // Category General
    private boolean bGeneralAPIEnabled;
    // Category Default>World
    private boolean bDefaultWorldIsEnabled;
    private boolean bDefaultWorldDoPredictPosition;
    private int iDefaultWorldDelayedTicks;
    private EnumMap<Triggers, Boolean> mapDefaultWorldOverlapTriggers;
    private String strDefaultWorldSyncTimeTo;
    // Category Default>Upper/Lower
    private String strDefaultUpperWorld;
    private String strDefaultLowerWorld;
    // Category Default>Upper/Lower>Overlap
    private boolean bDefaultUpperOverlapEnabled;
    private boolean bDefaultLowerOverlapEnabled;
    private int iDefaultUpperOverlapFrom;
    private int iDefaultLowerOverlapFrom;
    private int iDefaultUpperOverlapTo;
    private int iDefaultLowerOverlapTo;
    private int iDefaultUpperOverlapLayers;
    private int iDefaultLowerOverlapLayers;
    // Category Default>Upper/Lower>Teleport
    private boolean bDefaultUpperTeleportEnabled;
    private boolean bDefaultLowerTeleportEnabled;
    private int iDefaultUpperTeleportFrom;
    private int iDefaultLowerTeleportFrom;
    private int iDefaultUpperTeleportTo;
    private int iDefaultLowerTeleportTo;
    private boolean bDefaultUpperTeleportPreserveEntityVelocity;
    private boolean bDefaultLowerTeleportPreserveEntityVelocity;
    private boolean bDefaultUpperTeleportPreserveEntityFallDistance;
    private boolean bDefaultLowerTeleportPreserveEntityFallDistance;
    private boolean bDefaultLowerTeleportPreventFallDamage;
    private EnumMap<EntityType, Boolean> mapDefaultUpperTeleportEntityFilter;
    private EnumMap<EntityType, Boolean> mapDefaultLowerTeleportEntityFilter;
    /*
     * API Handlers
     */
    private InceptionAPI objAPI;

    @Override
    public void onEnable() {
        objLogger = super.getLogger();
        strPrefix = "[" + this.getDescription().getPrefix() + "] ";

        /*
         * Plugin Files & Folders
         */
        strPluginDirectory = this.getDataFolder().getAbsolutePath();
        objPluginDirectory = new File(strPluginDirectory);
        strWorldConfigDirectory = strPluginDirectory + "/per-world/";
        objWorldConfigDirectory = new File(strWorldConfigDirectory);
        strPluginConfig = strPluginDirectory + "/config.yml";
        objPluginConfig = new File(strPluginConfig);
        /*
         * Open the Plugin file as an EasyZipFile
         */
        try {
            objPluginFile = new EasyZipFile(this.getFile());
        } catch (Exception ex) {
            objLogger.log(Level.SEVERE, null, ex);
        }

        /*
         * Create base folder structure...
         */
        if (!objPluginDirectory.exists()) {
            objLogger.info("'" + strPluginDirectory + "' does not exist, creating...");
            if (objPluginDirectory.mkdir()) {
                objLogger.info("Created '" + strPluginDirectory + "'.");
            } else {
                objLogger.warning("Unable to create '" + strPluginDirectory + "'.");
            }
        }
        if (!objWorldConfigDirectory.exists()) {
            objLogger.info("'" + strWorldConfigDirectory + "' does not exist, creating...");
            if (objWorldConfigDirectory.mkdir()) {
                objLogger.info("Created '" + strWorldConfigDirectory + "'.");
            } else {
                objLogger.warning("Unable to create '" + strWorldConfigDirectory + "'.");
            }
        }
        // Create the default configuration file if needed...
        saveDefaultConfig();


        // Load global settings...
        loadConfig();

        // Register all WorldHandlers...
        for (World world : getServer().getWorlds()) {
            mapWorldHandlers.put(world, new WorldHandler(this, world));
        }

        // Register Event Listener...
        objLogger.info("Registering World Listener...");
        objWorldListener = new WorldListener(this);
        getServer().getPluginManager().registerEvents(objWorldListener, this);

        // Register API...
        objAPI = new InceptionAPI(this);
        objLogger.info("Registering API...");

        // Done.
        objLogger.info("Enabled.");
    }

    @Override
    public void onDisable() {
        /*
         * Kill critical Inception tasks, otherwise we'll crash in the
         * middle of disabling.
         */
        getServer().getScheduler().cancelTasks(this);

        /*
         * Disable and delete all WorldHandlers.
         */
        for (World wld : mapWorldHandlers.keySet()) {
            mapWorldHandlers.get(wld).onDisable();
            mapWorldHandlers.put(wld, null);
        }

        /*
         * Close Plugin File
         */
        try {
            objPluginFile.close();
        } catch (Exception ex) {
            objLogger.log(Level.SEVERE, null, ex);
        }

        /*
         * Clear all created maps
         */
        mapWorldHandlers.clear();
        mapDefaultWorldOverlapTriggers.clear();
        mapDefaultUpperTeleportEntityFilter.clear();
        mapDefaultLowerTeleportEntityFilter.clear();

        /*
         * Null reference Values for the GC
         */
        mapDefaultUpperTeleportEntityFilter = null;
        mapDefaultLowerTeleportEntityFilter = null;
        mapDefaultWorldOverlapTriggers = null;
        mapWorldHandlers = null;
        objWorldListener = null;
        objConfiguration = null;
        objPluginConfig = null;
        objWorldConfigDirectory = null;
        objPluginDirectory = null;
        strDefaultWorldSyncTimeTo = null;
        strDefaultUpperWorld = null;
        strDefaultLowerWorld = null;
        strPluginConfig = null;
        strWorldConfigDirectory = null;
        strPluginDirectory = null;
        objPluginFile = null;

        objLogger.info("Disabled.");
        objLogger = null;
    }

    /*
     * Region: Configuration
     */
    @Override
    public void saveDefaultConfig() {
        if (!objPluginConfig.exists()) {
            objLogger.info("'" + strPluginConfig + "' does not exist, unpacking...");
            objPluginFile.unzipPathAs("config.yml", objPluginConfig);
        }
    }

    public void loadConfig() {
        saveDefaultConfig();

        try {
            objConfiguration.load(objPluginConfig);

            bGeneralAPIEnabled = objConfiguration.getBoolean("General.APIEnabled", true);

            bDefaultWorldIsEnabled = objConfiguration.getBoolean("Default.World.Enabled", true);
            bDefaultWorldDoPredictPosition = objConfiguration.getBoolean("Default.World.DoPredictPosition", true);
            iDefaultWorldDelayedTicks = objConfiguration.getInt("Default.World.DelayedTicks", 1);
            strDefaultWorldSyncTimeTo = objConfiguration.getString("Default.World.SyncTimeTo", "");

            if (mapDefaultWorldOverlapTriggers != null) {
                mapDefaultWorldOverlapTriggers.clear();
                mapDefaultWorldOverlapTriggers = null;
            }
            mapDefaultWorldOverlapTriggers = new EnumMap<Triggers, Boolean>(Triggers.class);
            for (Triggers trigger : Triggers.values()) {
                mapDefaultWorldOverlapTriggers.put(trigger, objConfiguration.getBoolean("Default.World.OverlapTriggers." + trigger.getName(), false));
            }

            strDefaultUpperWorld = objConfiguration.getString("Default.Upper.World", "");
            strDefaultLowerWorld = objConfiguration.getString("Default.Lower.World", "");
            bDefaultUpperOverlapEnabled = objConfiguration.getBoolean("Default.Upper.Overlap.Enabled", false);
            bDefaultLowerOverlapEnabled = objConfiguration.getBoolean("Default.Lower.Overlap.Enabled", false);
            iDefaultUpperOverlapFrom = objConfiguration.getInt("Default.Upper.Overlap.From", 0);
            iDefaultLowerOverlapFrom = objConfiguration.getInt("Default.Lower.Overlap.From", 255);
            iDefaultUpperOverlapTo = objConfiguration.getInt("Default.Upper.Overlap.To", 255);
            iDefaultLowerOverlapTo = objConfiguration.getInt("Default.Lower.Overlap.To", 0);
            iDefaultUpperOverlapLayers = objConfiguration.getInt("Default.Upper.Overlap.Layers", 0);
            iDefaultLowerOverlapLayers = objConfiguration.getInt("Default.Lower.Overlap.Layers", 0);
            bDefaultUpperTeleportEnabled = objConfiguration.getBoolean("Default.Upper.Teleport.Enabled", false);
            bDefaultLowerTeleportEnabled = objConfiguration.getBoolean("Default.Lower.Teleport.Enabled", false);
            iDefaultUpperTeleportFrom = objConfiguration.getInt("Default.Upper.Teleport.From", 255);
            iDefaultLowerTeleportFrom = objConfiguration.getInt("Default.Lower.Teleport.From", 0);
            iDefaultUpperTeleportTo = objConfiguration.getInt("Default.Upper.Teleport.To", 1);
            iDefaultLowerTeleportTo = objConfiguration.getInt("Default.Lower.Teleport.To", 254);
            bDefaultUpperTeleportPreserveEntityVelocity = objConfiguration.getBoolean("Default.Upper.PreserveEntityVelocity", true);
            bDefaultLowerTeleportPreserveEntityVelocity = objConfiguration.getBoolean("Default.Lower.PreserveEntityVelocity", true);
            bDefaultUpperTeleportPreserveEntityFallDistance = objConfiguration.getBoolean("Default.Upper.PreserveEntityFallDistance", true);
            bDefaultLowerTeleportPreserveEntityFallDistance = objConfiguration.getBoolean("Default.Lower.PreserveEntityFallDistance", true);
            bDefaultLowerTeleportPreventFallDamage = objConfiguration.getBoolean("Default.Lower.PreventFallDamage", true);
            if (mapDefaultUpperTeleportEntityFilter != null) {
                mapDefaultUpperTeleportEntityFilter.clear();
                mapDefaultUpperTeleportEntityFilter = null;
            }
            mapDefaultUpperTeleportEntityFilter = new EnumMap<EntityType, Boolean>(EntityType.class);
            if (mapDefaultLowerTeleportEntityFilter != null) {
                mapDefaultLowerTeleportEntityFilter.clear();
                mapDefaultLowerTeleportEntityFilter = null;
            }
            mapDefaultLowerTeleportEntityFilter = new EnumMap<EntityType, Boolean>(EntityType.class);
            for (EntityType et : EntityType.values()) {
                mapDefaultUpperTeleportEntityFilter.put(et, objConfiguration.getBoolean("Default.Upper.Teleport.EntityFilter." + et.getName(), false));
            }
            for (EntityType et : EntityType.values()) {
                mapDefaultLowerTeleportEntityFilter.put(et, objConfiguration.getBoolean("Default.Lower.Teleport.EntityFilter." + et.getName(), false));
            }
        } catch (FileNotFoundException ex) {
            objLogger.log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            objLogger.log(Level.SEVERE, null, ex);
        } catch (InvalidConfigurationException ex) {
            objLogger.log(Level.SEVERE, null, ex);
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
    /*
     * End Region: Configuration
     */

    /*
     * Region: Command Handler
     */
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        args = util.reparseArgs(args);

        if (label.equals("inception")) {
            return onCommand_inception(sender, command, label, args);
        }

        return false;
    }

    private boolean onCommand_inception(CommandSender sender, Command command, String label, String[] args) {
        String[] newArgs = (args.length > 0) ? (Arrays.copyOfRange(args, 1, args.length)) : (new String[0]);

        if (args.length == 0) {
            sendMessage(sender, this.getDescription().getFullName());
            sendMessage(sender, "Website: " + this.getDescription().getWebsite());
            sendMessage(sender, "Licensed under Creative Commons BY-NC-SA by Michael Dirks (c) 2012");
            return true;
        } else {
            if (!sender.hasPermission("inception")) {
                sendMessage(sender, "You do not have permission to use this command.");
                return true;
            }
            if (args[0].equalsIgnoreCase("reload")) {
                return onCommand_inception_reload(sender, command, label, newArgs);
            }
        }

        return false;
    }

    private boolean onCommand_inception_reload(CommandSender sender, Command command, String label, String[] args) {
        String[] newArgs = (args.length > 0) ? (Arrays.copyOfRange(args, 1, args.length)) : (new String[0]);

        if (!sender.hasPermission("inception.reload")) {
            sendMessage(sender, "You do not have permission to use this command.");
            return true;
        }

        if (args.length == 0) {
            sendMessage(sender, "Reloading plugin configuration...");
            this.loadConfig();
            for (WorldHandler _wh : mapWorldHandlers.values()) {
                sendMessage(sender, "Reloading world configuration '" + _wh.getWorld().getName() + "'...");
                _wh.loadConfig();
            }
            sendMessage(sender, "Done!");
        } else {
            sendMessage(sender, "Usage: /inception reload");
        }

        return false;
    }
    /*
     * End Region: Command Handler
     */

    public <T> void sendMessage(final T reciever, final String msg, final Object... args) {
        sendMessage(true, reciever, msg, args);
    }

    public <T> void sendMessage(final boolean prefix, final T reciever, final String msg, final Object... args) {
        if (reciever != null) {
            if (reciever instanceof List) {
                for (Object entry : (List<?>) reciever) {
                    sendMessage(prefix, entry, msg, args);
                }
            } else {
                for (String line : String.format(msg, args).split("\n")) {
                    util.senderFromName(reciever).sendMessage(util.colorize((prefix ? this.strPrefix : "") + line));
                }
            }
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

    /*
     * Accessors
     */
    public EasyZipFile getEzfPluginFile() {
        return objPluginFile;
    }

    public HashMap<World, WorldHandler> getWorldHandlers() {
        return mapWorldHandlers;
    }

    public WorldHandler getWorldHandler(World forWorld) {
        return mapWorldHandlers.get(forWorld);
    }

    public File getWorldConfigDirectoryFile() {
        return objWorldConfigDirectory;
    }

    //TODO: Refactor method names to fit a human readable style.
    public WorldListener getWorldListener() {
        return objWorldListener;
    }

    /*
     * Properties for Inception Values
     */
    //TODO: Refactor method names to fit a human readable style.
    public boolean bolDefaultDoPredictPosition() {
        return bDefaultWorldDoPredictPosition;
    }

    public boolean bolDefaultIsEnabled() {
        return bDefaultWorldIsEnabled;
    }

    public boolean bolDefaultLowerOverlapEnabled() {
        return bDefaultLowerOverlapEnabled;
    }

    public boolean bolDefaultLowerTeleportEnabled() {
        return bDefaultLowerTeleportEnabled;
    }

    public boolean bolDefaultLowerTeleportPreserveEntityFallDistance() {
        return bDefaultLowerTeleportPreserveEntityFallDistance;
    }

    public boolean bolDefaultLowerTeleportPreserveEntityVelocity() {
        return bDefaultLowerTeleportPreserveEntityVelocity;
    }

    public boolean bolDefaultUpperOverlapEnabled() {
        return bDefaultUpperOverlapEnabled;
    }

    public boolean bolDefaultUpperTeleportEnabled() {
        return bDefaultUpperTeleportEnabled;
    }

    public boolean bolDefaultUpperTeleportPreserveEntityFallDistance() {
        return bDefaultUpperTeleportPreserveEntityFallDistance;
    }

    public boolean bolDefaultUpperTeleportPreserveEntityVelocity() {
        return bDefaultUpperTeleportPreserveEntityVelocity;
    }

    public int intDefaultDelayedTicks() {
        return iDefaultWorldDelayedTicks;
    }

    public int intDefaultLowerOverlapFrom() {
        return iDefaultLowerOverlapFrom;
    }

    public int intDefaultLowerOverlapLayers() {
        return iDefaultLowerOverlapLayers;
    }

    public int intDefaultLowerOverlapTo() {
        return iDefaultLowerOverlapTo;
    }

    public int intDefaultLowerTeleportFrom() {
        return iDefaultLowerTeleportFrom;
    }

    public int intDefaultLowerTeleportTo() {
        return iDefaultLowerTeleportTo;
    }

    public int intDefaultUpperOverlapFrom() {
        return iDefaultUpperOverlapFrom;
    }

    public int intDefaultUpperOverlapLayers() {
        return iDefaultUpperOverlapLayers;
    }

    public int intDefaultUpperOverlapTo() {
        return iDefaultUpperOverlapTo;
    }

    public int intDefaultUpperTeleportFrom() {
        return iDefaultUpperTeleportFrom;
    }

    public int intDefaultUpperTeleportTo() {
        return iDefaultUpperTeleportTo;
    }

    public EnumMap<EntityType, Boolean> oemDefaultLowerTeleportEntityFilter() {
        return mapDefaultLowerTeleportEntityFilter;
    }

    public EnumMap<EntityType, Boolean> oemDefaultUpperTeleportEntityFilter() {
        return mapDefaultUpperTeleportEntityFilter;
    }

    public String strDefaultLowerWorld() {
        return strDefaultLowerWorld;
    }

    public String strDefaultUpperWorld() {
        return strDefaultUpperWorld;
    }

    public EnumMap<Triggers, Boolean> ohmDefaultOverlapTriggers() {
        return mapDefaultWorldOverlapTriggers;
    }

    public String strDefaultSyncTimeTo() {
        return strDefaultWorldSyncTimeTo;
    }

    public boolean bolDefaultLowerTeleportPreventFallDamage() {
        return bDefaultLowerTeleportPreventFallDamage;
    }

    /*
     * API Handlers
     */
    public InceptionAPI getAPI() {
        return objAPI;
    }

    public boolean bolGeneralAPIEnabled() {
	    return bGeneralAPIEnabled;
    }
}
