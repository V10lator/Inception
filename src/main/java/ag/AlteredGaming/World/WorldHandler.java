package ag.AlteredGaming.World;

import ag.AlteredGaming.Inception;
import ag.AlteredGaming.util;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.logging.Level;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockFadeEvent;
import org.bukkit.event.block.BlockFormEvent;
import org.bukkit.event.block.BlockGrowEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.BlockSpreadEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.util.BlockVector;
import org.bukkit.util.Vector;

/**
 *
 * @author Xaymar
 */
public class WorldHandler {

    private Inception objPlugin;
    private World objWorld;
    private File objWorldConfigFile;
    private YamlConfiguration objWorldConfig;
    private boolean bolIsEnabled;
    private boolean bolDoPredictPosition;
    private int intDelayedTicks;
    private HashMap<String, Boolean> ohmOverlapTriggers;
    private World objSyncTimeTo;
    private World objUpperWorld;
    private boolean bolUpperOverlapEnabled;
    private int intUpperOverlapFrom;
    private int intUpperOverlapTo;
    private int intUpperOverlapLayers;
    private boolean bolUpperTeleportEnabled;
    private int intUpperTeleportFrom;
    private int intUpperTeleportTo;
    private boolean bolUpperTeleportPreserveEntityVelocity;
    private boolean bolUpperTeleportPreserveEntityFallDistance;
    private EnumMap<EntityType, Boolean> oemUpperTeleportEntityFilter;
    private World objLowerWorld;
    private boolean bolLowerOverlapEnabled;
    private int intLowerOverlapFrom;
    private int intLowerOverlapTo;
    private int intLowerOverlapLayers;
    private boolean bolLowerTeleportEnabled;
    private int intLowerTeleportFrom;
    private int intLowerTeleportTo;
    private boolean bolLowerTeleportPreserveEntityVelocity;
    private boolean bolLowerTeleportPreserveEntityFallDistance;
    private boolean bolLowerTeleportPreventFallDamage;
    private EnumMap<EntityType, Boolean> oemLowerTeleportEntityFilter;
    private HashMap<String, Boolean> ohmLowerOverlapTriggerFilter;
    private WorldHandlerRunnable objWorldHandlerRunnable;
    private int intWorldHandlerRunnableTask = -1;
    private HashMap<Chunk, HashMap<BlockVector, Material>> mapChunkOverlapChangedBlocksType;
    private HashMap<Chunk, HashMap<BlockVector, Byte>> mapChunkOverlapChangedBlocksData;

    public WorldHandler(Inception objPlugin, World objWorld) {
        this.objPlugin = objPlugin;
        this.objWorld = objWorld;
        this.objWorldConfigFile = new File(objPlugin.getWorldConfigDirectoryFile().getPath() + "/" + objWorld.getName() + ".yml");
        this.objWorldConfig = new YamlConfiguration();

        loadConfig();
    }

    public void saveDefaultConfig() {
        if (!objWorldConfigFile.exists()) {
            objPlugin.getLogger().finest("'" + objWorldConfigFile.getAbsoluteFile() + "' does not exist, unpacking...");
            objPlugin.getEzfPluginFile().unzipPathAs("world-config.yml", objWorldConfigFile);
        }
    }

    public void loadConfig() {
        try {
            saveDefaultConfig();
            objWorldConfig.load(objWorldConfigFile);

            bolIsEnabled = objWorldConfig.getBoolean("World.Enabled", objPlugin.bolDefaultIsEnabled());
            bolDoPredictPosition = objWorldConfig.getBoolean("World.DoPredictPosition", objPlugin.bolDefaultDoPredictPosition());
            intDelayedTicks = objWorldConfig.getInt("World.DelayedTicks", objPlugin.intDefaultDelayedTicks());
            objSyncTimeTo = objPlugin.getServer().getWorld(objWorldConfig.getString("World.SyncTimeTo", objPlugin.strDefaultSyncTimeTo()));
            
            if (bolIsEnabled == true) {
                if (ohmOverlapTriggers != null) {
                    ohmOverlapTriggers.clear();
                    ohmOverlapTriggers = null;
                }
                ohmOverlapTriggers = new HashMap<String, Boolean>();
                ohmOverlapTriggers.put("ChunkLoadUnload", objWorldConfig.getBoolean("World.OverlapTriggers.ChunkLoadUnload", objPlugin.ohmDefaultOverlapTriggers().get("ChunkLoadUnload")));
                ohmOverlapTriggers.put("BlockPlace", objWorldConfig.getBoolean("World.OverlapTriggers.BlockPlace", objPlugin.ohmDefaultOverlapTriggers().get("BlockPlace")));
                ohmOverlapTriggers.put("BlockBreak", objWorldConfig.getBoolean("World.OverlapTriggers.BlockBreak", objPlugin.ohmDefaultOverlapTriggers().get("BlockBreak")));
                ohmOverlapTriggers.put("BlockBurn", objWorldConfig.getBoolean("World.OverlapTriggers.BlockBurn", objPlugin.ohmDefaultOverlapTriggers().get("BlockBurn")));
                ohmOverlapTriggers.put("BlockFade", objWorldConfig.getBoolean("World.OverlapTriggers.BlockFade", objPlugin.ohmDefaultOverlapTriggers().get("BlockFade")));
                ohmOverlapTriggers.put("BlockForm", objWorldConfig.getBoolean("World.OverlapTriggers.BlockForm", objPlugin.ohmDefaultOverlapTriggers().get("BlockForm")));
                ohmOverlapTriggers.put("BlockGrow", objWorldConfig.getBoolean("World.OverlapTriggers.BlockGrow", objPlugin.ohmDefaultOverlapTriggers().get("BlockGrow")));
                ohmOverlapTriggers.put("BlockSpread", objWorldConfig.getBoolean("World.OverlapTriggers.BlockSpread", objPlugin.ohmDefaultOverlapTriggers().get("BlockSpread")));

                objUpperWorld = objPlugin.getServer().getWorld(objWorldConfig.getString("Upper.World", objPlugin.strDefaultUpperWorld()));
                bolUpperOverlapEnabled = objWorldConfig.getBoolean("Upper.Overlap.Enabled", objPlugin.bolDefaultUpperOverlapEnabled());
                intUpperOverlapFrom = objWorldConfig.getInt("Upper.Overlap.From", objPlugin.intDefaultUpperOverlapFrom());
                intUpperOverlapTo = objWorldConfig.getInt("Upper.Overlap.To", objPlugin.intDefaultUpperOverlapTo());
                intUpperOverlapLayers = objWorldConfig.getInt("Upper.Overlap.Layers", objPlugin.intDefaultUpperOverlapLayers());
                bolUpperTeleportEnabled = objWorldConfig.getBoolean("Upper.Teleport.Enabled", objPlugin.bolDefaultUpperTeleportEnabled());
                intUpperTeleportFrom = objWorldConfig.getInt("Upper.Teleport.From", objPlugin.intDefaultUpperTeleportFrom());
                intUpperTeleportTo = objWorldConfig.getInt("Upper.Teleport.To", objPlugin.intDefaultUpperTeleportTo());
                bolUpperTeleportPreserveEntityVelocity = objWorldConfig.getBoolean("Upper.PreserveEntityVelocity", objPlugin.bolDefaultUpperTeleportPreserveEntityVelocity());
                bolUpperTeleportPreserveEntityFallDistance = objWorldConfig.getBoolean("Upper.PreserveEntityFallDistance", objPlugin.bolDefaultUpperTeleportPreserveEntityFallDistance());
                if (oemUpperTeleportEntityFilter != null) {
                    oemUpperTeleportEntityFilter.clear();
                    oemUpperTeleportEntityFilter = null;
                }
                oemUpperTeleportEntityFilter = new EnumMap<EntityType, Boolean>(EntityType.class);
                for (EntityType et : EntityType.values()) {
                    oemUpperTeleportEntityFilter.put(et, objWorldConfig.getBoolean("Upper.Teleport.EntityFilter." + et.getName(), objPlugin.oemDefaultUpperTeleportEntityFilter().get(et)));
                }

                objLowerWorld = objPlugin.getServer().getWorld(objWorldConfig.getString("Lower.World", objPlugin.strDefaultLowerWorld()));
                bolLowerOverlapEnabled = objWorldConfig.getBoolean("Lower.Overlap.Enabled", objPlugin.bolDefaultLowerOverlapEnabled());
                intLowerOverlapFrom = objWorldConfig.getInt("Lower.Overlap.From", objPlugin.intDefaultLowerOverlapFrom());
                intLowerOverlapTo = objWorldConfig.getInt("Lower.Overlap.To", objPlugin.intDefaultLowerOverlapTo());
                intLowerOverlapLayers = objWorldConfig.getInt("Lower.Overlap.Layers", objPlugin.intDefaultLowerOverlapLayers());
                bolLowerTeleportEnabled = objWorldConfig.getBoolean("Lower.Teleport.Enabled", objPlugin.bolDefaultLowerTeleportEnabled());
                intLowerTeleportFrom = objWorldConfig.getInt("Lower.Teleport.From", objPlugin.intDefaultLowerTeleportFrom());
                intLowerTeleportTo = objWorldConfig.getInt("Lower.Teleport.To", objPlugin.intDefaultLowerTeleportTo());
                bolLowerTeleportPreserveEntityVelocity = objWorldConfig.getBoolean("Lower.PreserveEntityVelocity", objPlugin.bolDefaultLowerTeleportPreserveEntityVelocity());
                bolLowerTeleportPreserveEntityFallDistance = objWorldConfig.getBoolean("Lower.PreserveEntityFallDistance", objPlugin.bolDefaultLowerTeleportPreserveEntityFallDistance());
                bolLowerTeleportPreventFallDamage = objWorldConfig.getBoolean("Lower.Teleport.PreventFallDamage", objPlugin.bolDefaultLowerTeleportPreventFallDamage());
                if (oemLowerTeleportEntityFilter != null) {
                    oemLowerTeleportEntityFilter.clear();
                    oemLowerTeleportEntityFilter = null;
                }
                oemLowerTeleportEntityFilter = new EnumMap<EntityType, Boolean>(EntityType.class);
                for (EntityType et : EntityType.values()) {
                    oemLowerTeleportEntityFilter.put(et, objWorldConfig.getBoolean("Lower.Teleport.EntityFilter." + et.getName(), objPlugin.oemDefaultLowerTeleportEntityFilter().get(et)));
                }

                //This contains all changed blocks in this world.
                overlapCreateChunkMap();

                //This creates a runnable that calls code in this class
                if (objWorldHandlerRunnable == null) {
                    objWorldHandlerRunnable = new WorldHandlerRunnable(objPlugin, this);
                }
                if ((intDelayedTicks > 0)
                    && (((objUpperWorld != null) && (bolUpperTeleportEnabled == true))
                        || ((objLowerWorld != null) && (bolLowerTeleportEnabled == true)))) {
                    if (intWorldHandlerRunnableTask != -1) {
                        objPlugin.getServer().getScheduler().cancelTask(intWorldHandlerRunnableTask);
                    }
                    intWorldHandlerRunnableTask = objPlugin.getServer().getScheduler().scheduleSyncRepeatingTask(objPlugin, objWorldHandlerRunnable, intDelayedTicks, intDelayedTicks);
                    if (intWorldHandlerRunnableTask == -1) {
                        objPlugin.getLogger().warning("<" + objWorld.getName() + "> Could not register synchronized repeating task. Entities can not be teleported!");
                    } else {
                        objPlugin.getLogger().info("<" + objWorld.getName() + "> WorldHandler enabled.");
                    }
                }
                if (intWorldHandlerRunnableTask == -1) {
                    objPlugin.getLogger().info("<" + objWorld.getName() + "> Teleportation disabled.");
                }
            } else {
                if (intWorldHandlerRunnableTask != -1) {
                    objPlugin.getServer().getScheduler().cancelTask(intWorldHandlerRunnableTask);
                    intWorldHandlerRunnableTask = -1;
                }
                objPlugin.getLogger().info("<" + objWorld.getName() + "> WorldHandler disabled.");
            }
        } catch (FileNotFoundException ex) {
            objPlugin.getLogger().log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            objPlugin.getLogger().log(Level.SEVERE, null, ex);
        } catch (InvalidConfigurationException ex) {
            objPlugin.getLogger().log(Level.SEVERE, null, ex);
        }
    }

    public void saveConfig() {
        try {
            objWorldConfig.save(objWorldConfigFile);
        } catch (IOException ex) {
            objPlugin.getLogger().log(Level.SEVERE, null, ex);
        }
    }

    public World getWorld() {
        return objWorld;
    }

    /*
     * Internal Update Methods
     */
    public void tickWorldUpdateCheck() {
        if (objSyncTimeTo != null) {
            objWorld.setTime(objSyncTimeTo.getTime());
        }
        
        for (Entity ent : objWorld.getEntities()) {
            Location _EntityLocation = ent.getLocation();
            Vector _EntityVelocity = ent.getVelocity();
            float _EntityDistanceFallen = ent.getFallDistance();

            if (bolDoPredictPosition) {
                //Advance the entites position by their velocity * objPlugin.getDelayedTicks().
                _EntityLocation.setX(_EntityLocation.getX() + _EntityVelocity.getX() * intDelayedTicks);
                _EntityLocation.setY(_EntityLocation.getY() + _EntityVelocity.getY() * intDelayedTicks);
                _EntityLocation.setZ(_EntityLocation.getZ() + _EntityVelocity.getZ() * intDelayedTicks);
            }

            //1. Step: Check if we can skip this entity. Helps save CPU time.
            if ((objUpperWorld == null) && (objLowerWorld == null)) {
                continue;
            } else {
                if (objUpperWorld != null) {
                    if (oemUpperTeleportEntityFilter.get(ent.getType()) == true) {
                        continue;
                    }
                    if (_EntityLocation.getY() >= intUpperTeleportFrom) {
                        //2. Step: We can't skip it so let's just do what is needed
                        Location _UpperWorldExit = new Location(objUpperWorld,
                                                                ent.getLocation().getX(),
                                                                intUpperTeleportTo - (ent.getLocation().getY() - intUpperTeleportFrom),
                                                                ent.getLocation().getZ());
                        _UpperWorldExit.setPitch(ent.getLocation().getPitch());
                        _UpperWorldExit.setYaw(ent.getLocation().getYaw());
                        Entity tent = util.entityTeleportEx(ent, _UpperWorldExit);

                        if (!bolUpperTeleportPreserveEntityVelocity) {
                            tent.setVelocity(new Vector(0, 0, 0));
                        }
                        if (!bolUpperTeleportPreserveEntityFallDistance) {
                            tent.setFallDistance(0);
                        }
                    }
                }
                if (objLowerWorld != null) {
                    if (oemLowerTeleportEntityFilter.get(ent.getType()) == true) {
                        continue;
                    }
                    if (_EntityLocation.getY() <= intLowerTeleportFrom) {
                        //2. Step: We can't skip it so let's just do what is needed
                        Location _LowerWorldExit = new Location(objLowerWorld,
                                                                ent.getLocation().getX(),
                                                                intLowerTeleportTo + (ent.getLocation().getY() - intLowerTeleportFrom),
                                                                ent.getLocation().getZ());
                        _LowerWorldExit.setPitch(ent.getLocation().getPitch());
                        _LowerWorldExit.setYaw(ent.getLocation().getYaw());
                        Entity tent = util.entityTeleportEx(ent, _LowerWorldExit);

                        if (!bolLowerTeleportPreserveEntityVelocity) {
                            tent.setVelocity(new Vector(0, 0, 0));
                        }
                        if (!bolLowerTeleportPreserveEntityFallDistance) {
                            tent.setFallDistance(0);
                        }
                        if (bolLowerTeleportPreventFallDamage) {
                            tent.setMetadata("takeFallDamage", new FixedMetadataValue(objPlugin, true));
                        }
                    }
                }
            }
        }
    }

    public void overlapUnload() {
        if (mapChunkOverlapChangedBlocksType != null) {
            for (Chunk chunk : mapChunkOverlapChangedBlocksType.keySet()) {
                overlapUnloadChunk(chunk);
            }
            mapChunkOverlapChangedBlocksType.clear();
            mapChunkOverlapChangedBlocksType = null;
        }
        if (mapChunkOverlapChangedBlocksData != null) {
            for (Chunk chunk : mapChunkOverlapChangedBlocksData.keySet()) {
                overlapUnloadChunk(chunk);
            }
            mapChunkOverlapChangedBlocksData.clear();
            mapChunkOverlapChangedBlocksData = null;
        }
    }

    private void overlapCreateChunkMap() {
        overlapUnload();
        mapChunkOverlapChangedBlocksType = new HashMap<Chunk, HashMap<BlockVector, Material>>();
        mapChunkOverlapChangedBlocksData = new HashMap<Chunk, HashMap<BlockVector, Byte>>();
    }

    private void overlapLoadChunk(Chunk chunk) {

        HashMap<BlockVector, Material> changedBlocksType = new HashMap<BlockVector, Material>();
        HashMap<BlockVector, Byte> changedBlocksData = new HashMap<BlockVector, Byte>();
        mapChunkOverlapChangedBlocksType.put(chunk, changedBlocksType);
        mapChunkOverlapChangedBlocksData.put(chunk, changedBlocksData);
        if (bolIsEnabled == true) {
            if ((bolUpperOverlapEnabled == true) && (objUpperWorld != null) && (intUpperOverlapTo <= objWorld.getMaxHeight()) && (intUpperOverlapFrom >= 0)) {
                Chunk chunkUpper = objUpperWorld.getChunkAt(chunk.getX(), chunk.getZ());
                if (chunkUpper != null) {
                    boolean manualLoad = false;
                    if (chunkUpper.isLoaded() == false) {
                        //Quick & Dirty hack to prevent recursive calls due to infinite events...
                        mapChunkOverlapChangedBlocksType.put(chunkUpper, new HashMap<BlockVector, Material>());
                        chunkUpper.load(true);
                        manualLoad = true;
                    }
                    for (int layer = 0; layer < intUpperOverlapLayers; layer++) {
                        for (int x = 0; x < 16; x++) {
                            for (int z = 0; z < 16; z++) {
                                Block block = chunk.getBlock(x, intUpperOverlapTo - (intUpperOverlapLayers - 1) + layer, z);
                                Block blockUpper = chunkUpper.getBlock(x, intUpperOverlapFrom + layer, z);
                                if (block != null) {
                                    if (blockUpper != null) {
                                        if (block.getType() == Material.AIR) {
                                            BlockVector pos = new BlockVector(x, intUpperOverlapTo - layer, z);
                                            changedBlocksType.put(pos, block.getType());
                                            changedBlocksData.put(pos, block.getData());
                                            block.setType(blockUpper.getType());
                                            block.setData(blockUpper.getData());
                                        }
                                    }
                                }
                            }
                        }
                    }
                    if (manualLoad == true) {
                        //Quick & Dirty hack to prevent recursive calls due to infinite events...
                        mapChunkOverlapChangedBlocksType.remove(chunkUpper);
                        chunkUpper.unload(false);
                    }
                }
            }
            if ((bolLowerOverlapEnabled == true && objLowerWorld != null) && (intLowerOverlapTo >= 0) && (intLowerOverlapFrom <= 255)) {
                Chunk chunkLower = objLowerWorld.getChunkAt(chunk.getX(), chunk.getZ());
                if (chunkLower != null) {
                    boolean manualLoad = false;
                    if (chunkLower.isLoaded() == false) {
                        //Quick & Dirty hack to prevent recursive calls due to infinite events...
                        mapChunkOverlapChangedBlocksType.put(chunkLower, new HashMap<BlockVector, Material>());
                        chunkLower.load(true);
                        manualLoad = true;
                    }
                    for (int layer = 0; layer < intLowerOverlapLayers; layer++) {
                        for (int x = 0; x < 16; x++) {
                            for (int z = 0; z < 16; z++) {
                                Block block = chunk.getBlock(x, intLowerOverlapTo + layer, z);
                                Block blockLower = chunkLower.getBlock(x, intLowerOverlapFrom - (intLowerOverlapLayers - 1) + layer, z);
                                if (block != null) {
                                    if (blockLower != null) {
                                        if (block.getType() == Material.AIR) {
                                            BlockVector pos = new BlockVector(x, intLowerOverlapTo + layer, z);
                                            changedBlocksType.put(pos, block.getType()); //This causes a java.lang.OutOfMemoryError. Figure out why.
                                            changedBlocksData.put(pos, block.getData()); 
                                            block.setType(blockLower.getType());
                                            block.setData(blockLower.getData());
                                        }
                                    }
                                }
                            }
                        }
                    }
                    if (manualLoad == true) {
                        //Quick & Dirty hack to prevent recursive calls due to infinite events...
                        mapChunkOverlapChangedBlocksType.remove(chunkLower);
                        chunkLower.unload(false);
                    }
                }
            }
        }
    }

    private void overlapUnloadChunk(Chunk chunk) {
        HashMap<BlockVector, Material> changedBlocksType = mapChunkOverlapChangedBlocksType.get(chunk);
        HashMap<BlockVector, Byte> changedBlocksData = mapChunkOverlapChangedBlocksData.get(chunk);
        for (BlockVector vec : changedBlocksType.keySet()) {
            Block block = chunk.getBlock(vec.getBlockX(), vec.getBlockY(), vec.getBlockZ());
            Material oldType = changedBlocksType.get(vec);
            Byte oldData = changedBlocksData.get(vec);
            block.setType(oldType);
            block.setData(oldData);
        }
        mapChunkOverlapChangedBlocksType.remove(chunk);
        mapChunkOverlapChangedBlocksData.remove(chunk);
    }

    private void overlapBlockPlace(Block block) {
        Location localPosition = block.getLocation();
        if (bolUpperOverlapEnabled && (objUpperWorld != null)) {
            if ((localPosition.getBlockY() <= intUpperOverlapTo) && (localPosition.getBlockY() > intUpperOverlapTo - intUpperOverlapLayers)) {
                objPlugin.getWorldHandlers().get(objUpperWorld).replaceBlock(block.getType(), block.getData(), new BlockVector(block.getX(), intUpperOverlapTo - block.getY(), block.getZ()), false);
                removeListedBlock(block.getChunk(), block.getLocation().toVector().toBlockVector());
            }
        }
        if (bolLowerOverlapEnabled && (objLowerWorld != null)) {
            if ((localPosition.getBlockY() >= intLowerOverlapTo) && (localPosition.getBlockY() < intLowerOverlapTo + intLowerOverlapLayers)) {
                objPlugin.getWorldHandlers().get(objLowerWorld).replaceBlock(block.getType(), block.getData(), new BlockVector(block.getX(), block.getY() - intLowerOverlapTo, block.getZ()), true);
                removeListedBlock(block.getChunk(), block.getLocation().toVector().toBlockVector());
            }
        }
    }

    private void overlapBlockBreak(Block block) {
        Location localPosition = block.getLocation();
        if (bolUpperOverlapEnabled && (objUpperWorld != null)) {
            if ((localPosition.getBlockY() <= intUpperOverlapTo) && (localPosition.getBlockY() > intUpperOverlapTo - intUpperOverlapLayers)) {
                objPlugin.getWorldHandlers().get(objUpperWorld).replaceBlock(Material.AIR, new Byte((byte) 0), new BlockVector(block.getX(), intUpperOverlapTo - block.getY(), block.getZ()), false);
                removeListedBlock(block.getChunk(), block.getLocation().toVector().toBlockVector());
            }
        }
        if (bolLowerOverlapEnabled && (objLowerWorld != null)) {
            if ((localPosition.getBlockY() >= intLowerOverlapTo) && (localPosition.getBlockY() < intLowerOverlapTo + intLowerOverlapLayers)) {
                objPlugin.getWorldHandlers().get(objLowerWorld).replaceBlock(Material.AIR, new Byte((byte) 0), new BlockVector(block.getX(), block.getY() - intLowerOverlapTo, block.getZ()), true);
                removeListedBlock(block.getChunk(), block.getLocation().toVector().toBlockVector());
            }
        }
    }

    /*
     * External
     */
    public void chunkLoadEvent(ChunkLoadEvent event) {
        if (bolIsEnabled && ohmOverlapTriggers.get("ChunkLoadUnload")) {
            Chunk chunk = event.getChunk();
            //Quick & Dirty hack to prevent recursive calls due to infinite events...
            if (!mapChunkOverlapChangedBlocksType.containsKey(chunk)) {
                overlapLoadChunk(chunk);
            }
        }

    }

    public void chunkUnloadEvent(ChunkUnloadEvent event) {
        if (bolIsEnabled && ohmOverlapTriggers.get("ChunkLoadUnload")) {
            Chunk chunk = event.getChunk();
            if (mapChunkOverlapChangedBlocksType.containsKey(chunk)) {
                overlapUnloadChunk(chunk);
            }
        }
    }

    public void blockPlaceEvent(BlockPlaceEvent event) {
        if (bolIsEnabled && ohmOverlapTriggers.get("BlockPlace") && !event.isCancelled()) {
            overlapBlockPlace(event.getBlockPlaced());

        }
    }

    public void blockBreakEvent(BlockBreakEvent event) {
        if (bolIsEnabled && ohmOverlapTriggers.get("BlockBreak") && !event.isCancelled()) {
            overlapBlockBreak(event.getBlock());
        }
    }

    public void blockBurnEvent(BlockBurnEvent event) {
        if (bolIsEnabled && ohmOverlapTriggers.get("BlockBurn") && !event.isCancelled()) {
            overlapBlockBreak(event.getBlock());
        }
    }

    public void blockFadeEvent(BlockFadeEvent event) {
        if (bolIsEnabled && ohmOverlapTriggers.get("BlockFade") && !event.isCancelled()) {
            overlapBlockBreak(event.getBlock());
        }
    }

    public void blockFormEvent(BlockFormEvent event) {
        if (bolIsEnabled && ohmOverlapTriggers.get("BlockForm") && !event.isCancelled()) {
            overlapBlockPlace(event.getBlock());
        }
    }

    public void blockGrowEvent(BlockGrowEvent event) {
        if (bolIsEnabled && ohmOverlapTriggers.get("BlockGrow") && !event.isCancelled()) {
            overlapBlockPlace(event.getBlock());
        }
    }

    public void blockSpreadEvent(BlockSpreadEvent event) {
        if (bolIsEnabled && ohmOverlapTriggers.get("BlockSpread") && !event.isCancelled()) {
            overlapBlockPlace(event.getBlock());
        }
    }

    //Listed Temporary Blocks
    private void removeListedBlock(Chunk chunk, BlockVector Position) {
        HashMap<BlockVector, Material> changedBlocksType = mapChunkOverlapChangedBlocksType.get(chunk);
        HashMap<BlockVector, Byte> changedBlocksData = mapChunkOverlapChangedBlocksData.get(chunk);
        if ((changedBlocksType != null) && (changedBlocksData != null)) {
            if (changedBlocksType.containsKey(Position)) {
                changedBlocksType.remove(Position);
                changedBlocksData.remove(Position);
            }
        }
    }

    public void replaceBlock(Material withMat, Byte withData, BlockVector Position, boolean cameFromAbove) {
        Vector localPosition = Position.clone();
        if (cameFromAbove) {
            localPosition.setY(intUpperOverlapTo - (intUpperOverlapLayers - 1) + Position.getBlockY());
        } else {
            localPosition.setY(intLowerOverlapTo + (intLowerOverlapLayers - 1) - Position.getBlockY());
        }

        Block block = objWorld.getBlockAt(localPosition.getBlockX(), localPosition.getBlockY(), localPosition.getBlockZ());
        Chunk chunk = objWorld.getChunkAt(block);
        removeListedBlock(chunk, block.getLocation().toVector().toBlockVector());
        block.setType(withMat);
        block.setData(withData);
    }
}