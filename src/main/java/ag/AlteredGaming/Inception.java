package ag.AlteredGaming;

import ag.AlteredGaming.World.WorldHandler;
import ag.AlteredGaming.World.WorldListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.String;
import java.util.Arrays;
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
import org.bukkit.plugin.java.JavaPlugin;

public class Inception
        extends JavaPlugin {

    @SuppressWarnings("NonConstantLogger")
    private Logger objLogger;
    private EasyZipFile ezfPluginFile;
    private String prefix;
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
    //Holds all WorldHandlers that exist
    private HashMap<World, WorldHandler> ohmWorldHandlers;

    @Override
    public void onEnable() {
        objLogger = super.getLogger();//Logger.getLogger(Inception.class.getName());

        prefix = this.getDescription().getPrefix() + " ";
        
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

        objLogger.info("Enabled.");
    }

    @Override
    public void onDisable() {
        //Cancel all tasks
        getServer().getScheduler().cancelTasks(this);

        //Null all variable references to allow the GC to delete these
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
            sendMessage(sender, "Website: "+this.getDescription().getWebsite());
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
                sendMessage(sender, "Reloading world configuration '"+_wh.getWorld().getName()+"'...");
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
}
