package ag.AlteredGaming;

import ag.AlteredGaming.World.WorldHandler;
import ag.AlteredGaming.World.WorldListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipException;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.plugin.java.JavaPlugin;

public class Inception
        extends JavaPlugin {

    @SuppressWarnings("NonConstantLogger")
    private Logger objLogger;
    private EasyZipFile ezfPluginFile;
    private String prefix;
    //Plugin files(with String path)
    private String strPluginDirectory;
    private String strWorldConfigDirectory;
    private String strPluginConfig;
    private File objPluginDirectory;
    private File objWorldConfigDirectory;
    private File objPluginConfig;
    //Configuration Stuff
    private YamlConfiguration objConfiguration;
    private boolean bolDefaultIsEnabled;
    private boolean bolDefaultDoPredictPosition;
    private int intDefaultDelayedTicks;
    private HashMap<String, Boolean> ohmDefaultOverlapTriggers;
    private String strDefaultSyncTimeTo;
    private String strDefaultUpperWorld;
    private boolean bolDefaultUpperOverlapEnabled;
    private int intDefaultUpperOverlapFrom;
    private int intDefaultUpperOverlapTo;
    private int intDefaultUpperOverlapLayers;
    private boolean bolDefaultUpperTeleportEnabled;
    private int intDefaultUpperTeleportFrom;
    private int intDefaultUpperTeleportTo;
    private boolean bolDefaultUpperTeleportPreserveEntityVelocity;
    private boolean bolDefaultUpperTeleportPreserveEntityFallDistance;
    private EnumMap<EntityType, Boolean> oemDefaultUpperTeleportEntityFilter;
    private String strDefaultLowerWorld;
    private boolean bolDefaultLowerOverlapEnabled;
    private int intDefaultLowerOverlapFrom;
    private int intDefaultLowerOverlapTo;
    private int intDefaultLowerOverlapLayers;
    private boolean bolDefaultLowerTeleportEnabled;
    private int intDefaultLowerTeleportFrom;
    private int intDefaultLowerTeleportTo;
    private boolean bolDefaultLowerTeleportPreserveEntityVelocity;
    private boolean bolDefaultLowerTeleportPreserveEntityFallDistance;
    private EnumMap<EntityType, Boolean> oemDefaultLowerTeleportEntityFilter;
    //WorldListener to catch world events
    private WorldListener objWorldListener;
    //Holds all WorldHandlers that exist
    private HashMap<World, WorldHandler> ohmWorldHandlers;

    @Override
    public void onEnable() {
        objLogger = super.getLogger();
        prefix = "[" + this.getDescription().getPrefix() + "] ";

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
        strPluginDirectory = this.getDataFolder().getAbsolutePath();
        objPluginDirectory = new File(strPluginDirectory);
        strWorldConfigDirectory = strPluginDirectory + "/per-world/";
        objWorldConfigDirectory = new File(strWorldConfigDirectory);
        strPluginConfig = strPluginDirectory + "/config.yml";
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

        //Add all loaded Worlds as WorldHandlers
        for (World world : getServer().getWorlds()) {
            if (!ohmWorldHandlers.containsKey(world)) {
                ohmWorldHandlers.put(world, new WorldHandler(this, world));
            }
        }

        //Event Listeners
        objLogger.fine("Registering World Listener...");
        objWorldListener = new WorldListener(this);
        getServer().getPluginManager().registerEvents(objWorldListener, this);

        objLogger.info("Enabled.");
    }

    @Override
    public void onDisable() {
        //Cancel all tasks
        getServer().getScheduler().cancelTasks(this);

        //Null all variable references to allow the GC to delete these
        for (World wld : ohmWorldHandlers.keySet()) {
            ohmWorldHandlers.get(wld).overlapUnload();
        }
        ohmWorldHandlers.clear();
        ohmWorldHandlers = null;
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

            bolDefaultIsEnabled = objConfiguration.getBoolean("Default.World.Enabled", true);
            bolDefaultDoPredictPosition = objConfiguration.getBoolean("Default.World.DoPredictPosition", true);
            intDefaultDelayedTicks = objConfiguration.getInt("Default.World.DelayedTicks", 1);
            strDefaultSyncTimeTo = objConfiguration.getString("Default.World.SyncTimeTo", "");

            if (ohmDefaultOverlapTriggers != null) {
                ohmDefaultOverlapTriggers.clear();
                ohmDefaultOverlapTriggers = null;
            }
            ohmDefaultOverlapTriggers = new HashMap<String, Boolean>();
            ohmDefaultOverlapTriggers.put("ChunkLoadUnload", objConfiguration.getBoolean("Default.World.OverlapTriggers.ChunkLoadUnload", true));
            ohmDefaultOverlapTriggers.put("BlockPlace", objConfiguration.getBoolean("Default.World.OverlapTriggers.BlockPlace", true));
            ohmDefaultOverlapTriggers.put("BlockBreak", objConfiguration.getBoolean("Default.World.OverlapTriggers.BlockBreak", true));
            ohmDefaultOverlapTriggers.put("BlockBurn", objConfiguration.getBoolean("Default.World.OverlapTriggers.BlockBurn", true));
            ohmDefaultOverlapTriggers.put("BlockFade", objConfiguration.getBoolean("Default.World.OverlapTriggers.BlockFade", true));
            ohmDefaultOverlapTriggers.put("BlockForm", objConfiguration.getBoolean("Default.World.OverlapTriggers.BlockForm", true));
            ohmDefaultOverlapTriggers.put("BlockGrow", objConfiguration.getBoolean("Default.World.OverlapTriggers.BlockGrow", true));
            ohmDefaultOverlapTriggers.put("BlockSpread", objConfiguration.getBoolean("Default.World.OverlapTriggers.BlockSpread", true));

            strDefaultUpperWorld = objConfiguration.getString("Default.Upper.World", "");
            bolDefaultUpperOverlapEnabled = objConfiguration.getBoolean("Default.Upper.Overlap.Enabled", false);
            intDefaultUpperOverlapFrom = objConfiguration.getInt("Default.Upper.Overlap.From", 0);
            intDefaultUpperOverlapTo = objConfiguration.getInt("Default.Upper.Overlap.To", 255);
            intDefaultUpperOverlapLayers = objConfiguration.getInt("Default.Upper.Overlap.Layers", 0);
            bolDefaultUpperTeleportEnabled = objConfiguration.getBoolean("Default.Upper.Teleport.Enabled", false);
            intDefaultUpperTeleportFrom = objConfiguration.getInt("Default.Upper.Teleport.From", 255);
            intDefaultUpperTeleportTo = objConfiguration.getInt("Default.Upper.Teleport.To", 1);
            bolDefaultUpperTeleportPreserveEntityVelocity = objConfiguration.getBoolean("Default.Upper.PreserveEntityVelocity", true);
            bolDefaultUpperTeleportPreserveEntityFallDistance = objConfiguration.getBoolean("Default.Upper.PreserveEntityFallDistance", true);
            oemDefaultUpperTeleportEntityFilter = new EnumMap<EntityType, Boolean>(EntityType.class);
            for (EntityType et : EntityType.values()) {
                oemDefaultUpperTeleportEntityFilter.put(et, objConfiguration.getBoolean("Default.Upper.Teleport.EntityFilter." + et.getName(), false));
            }
            strDefaultLowerWorld = objConfiguration.getString("Default.Lower.World", "");
            bolDefaultLowerOverlapEnabled = objConfiguration.getBoolean("Default.Lower.Overlap.Enabled", false);
            intDefaultLowerOverlapFrom = objConfiguration.getInt("Default.Lower.Overlap.From", 255);
            intDefaultLowerOverlapTo = objConfiguration.getInt("Default.Lower.Overlap.To", 0);
            intDefaultLowerOverlapLayers = objConfiguration.getInt("Default.Lower.Overlap.Layers", 0);
            bolDefaultLowerTeleportEnabled = objConfiguration.getBoolean("Default.Lower.Teleport.Enabled", false);
            intDefaultLowerTeleportFrom = objConfiguration.getInt("Default.Lower.Teleport.From", 0);
            intDefaultLowerTeleportTo = objConfiguration.getInt("Default.Lower.Teleport.To", 254);
            bolDefaultLowerTeleportPreserveEntityVelocity = objConfiguration.getBoolean("Default.Lower.PreserveEntityVelocity", true);
            bolDefaultLowerTeleportPreserveEntityFallDistance = objConfiguration.getBoolean("Default.Lower.PreserveEntityFallDistance", true);
            oemDefaultLowerTeleportEntityFilter = new EnumMap<EntityType, Boolean>(EntityType.class);
            for (EntityType et : EntityType.values()) {
                oemDefaultLowerTeleportEntityFilter.put(et, objConfiguration.getBoolean("Default.Lower.Teleport.EntityFilter." + et.getName(), false));
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

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        args = util.reparseArgs(args);

        if (label.equals("inception")) {
            return onCommand_inception(sender, command, label, args);
        }

        return false;
    }

    public boolean onCommand_inception(CommandSender sender, Command command, String label, String[] args) {
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
            for (WorldHandler _wh : ohmWorldHandlers.values()) {
                sendMessage(sender, "Reloading world configuration '" + _wh.getWorld().getName() + "'...");
                _wh.loadConfig();
            }
            sendMessage(sender, "Done!");
        } else {
            sendMessage(sender, "Usage: /inception reload");
        }

        return false;
    }

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
                    util.senderFromName(reciever).sendMessage(util.colorize((prefix ? this.prefix : "") + line));
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

    //Default Value Getters
    public boolean bolDefaultDoPredictPosition() {
        return bolDefaultDoPredictPosition;
    }

    public boolean bolDefaultIsEnabled() {
        return bolDefaultIsEnabled;
    }

    public boolean bolDefaultLowerOverlapEnabled() {
        return bolDefaultLowerOverlapEnabled;
    }

    public boolean bolDefaultLowerTeleportEnabled() {
        return bolDefaultLowerTeleportEnabled;
    }

    public boolean bolDefaultLowerTeleportPreserveEntityFallDistance() {
        return bolDefaultLowerTeleportPreserveEntityFallDistance;
    }

    public boolean bolDefaultLowerTeleportPreserveEntityVelocity() {
        return bolDefaultLowerTeleportPreserveEntityVelocity;
    }

    public boolean bolDefaultUpperOverlapEnabled() {
        return bolDefaultUpperOverlapEnabled;
    }

    public boolean bolDefaultUpperTeleportEnabled() {
        return bolDefaultUpperTeleportEnabled;
    }

    public boolean bolDefaultUpperTeleportPreserveEntityFallDistance() {
        return bolDefaultUpperTeleportPreserveEntityFallDistance;
    }

    public boolean bolDefaultUpperTeleportPreserveEntityVelocity() {
        return bolDefaultUpperTeleportPreserveEntityVelocity;
    }

    public int intDefaultDelayedTicks() {
        return intDefaultDelayedTicks;
    }

    public int intDefaultLowerOverlapFrom() {
        return intDefaultLowerOverlapFrom;
    }

    public int intDefaultLowerOverlapLayers() {
        return intDefaultLowerOverlapLayers;
    }

    public int intDefaultLowerOverlapTo() {
        return intDefaultLowerOverlapTo;
    }

    public int intDefaultLowerTeleportFrom() {
        return intDefaultLowerTeleportFrom;
    }

    public int intDefaultLowerTeleportTo() {
        return intDefaultLowerTeleportTo;
    }

    public int intDefaultUpperOverlapFrom() {
        return intDefaultUpperOverlapFrom;
    }

    public int intDefaultUpperOverlapLayers() {
        return intDefaultUpperOverlapLayers;
    }

    public int intDefaultUpperOverlapTo() {
        return intDefaultUpperOverlapTo;
    }

    public int intDefaultUpperTeleportFrom() {
        return intDefaultUpperTeleportFrom;
    }

    public int intDefaultUpperTeleportTo() {
        return intDefaultUpperTeleportTo;
    }

    public EnumMap<EntityType, Boolean> oemDefaultLowerTeleportEntityFilter() {
        return oemDefaultLowerTeleportEntityFilter;
    }

    public EnumMap<EntityType, Boolean> oemDefaultUpperTeleportEntityFilter() {
        return oemDefaultUpperTeleportEntityFilter;
    }

    public String strDefaultLowerWorld() {
        return strDefaultLowerWorld;
    }

    public String strDefaultUpperWorld() {
        return strDefaultUpperWorld;
    }

    public HashMap<String, Boolean> ohmDefaultOverlapTriggers() {
        return ohmDefaultOverlapTriggers;
    }

    public String strDefaultSyncTimeTo() {
        return strDefaultSyncTimeTo;
    }
}
