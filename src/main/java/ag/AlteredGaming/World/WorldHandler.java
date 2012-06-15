package ag.AlteredGaming.World;

import ag.AlteredGaming.Other.Triggers;
import ag.AlteredGaming.Inception;
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
    
    private int iWorldDelayedTicks;
    private int iUpperOverlapFrom;
    private int iLowerOverlapFrom;
    private int iUpperOverlapTo;
    private int iLowerOverlapTo;
    private int iUpperOverlapLayers;
    private int iLowerOverlapLayers;
    private int iUpperTeleportFrom;
    private int iLowerTeleportFrom;
    private int iUpperTeleportTo;
    private int iLowerTeleportTo;
    private World objWorldSyncTimeTo;
    private World objUpperWorld;
    private World objLowerWorld;
    private boolean bWorldIsEnabled;
    private boolean bWorldDoPredictPosition;
    private boolean bUpperOverlapEnabled;
    private boolean bLowerOverlapEnabled;
    private boolean bUpperTeleportEnabled;
    private boolean bLowerTeleportEnabled;
    private boolean bUpperTeleportPreserveEntityVelocity;
    private boolean bLowerTeleportPreserveEntityVelocity;
    private boolean bUpperTeleportPreserveEntityFallDistance;
    private boolean bLowerTeleportPreserveEntityFallDistance;
    private boolean bLowerTeleportPreventFallDamage;
    private EnumMap<Triggers, Boolean> mapWorldOverlapTriggers;
    private EnumMap<EntityType, Boolean> mapUpperTeleportEntityFilter;
    private EnumMap<EntityType, Boolean> mapLowerTeleportEntityFilter;
    
    private WorldHandlerRunnable objWorldHandlerRunnable;
    private int iWorldHandlerRunnableTask = -1;
    
    private HashMap<Chunk, HashMap<BlockVector, Material>> mapChunkOverlapChangedBlocksType;
    private HashMap<Chunk, HashMap<BlockVector, Byte>> mapChunkOverlapChangedBlocksData;

    public WorldHandler(Inception objPlugin, World objWorld) {
        this.objPlugin = objPlugin;
        this.objWorld = objWorld;
        this.objWorldConfigFile = new File(objPlugin.getWorldConfigDirectoryFile().getPath() + "/" + objWorld.getName() + ".yml");
        this.objWorldConfig = new YamlConfiguration();
        
        loadConfig();
    }
    
    public void onDisable() {
        this.overlapUnload();
    }
    

    public void saveDefaultConfig() {
        if (!objWorldConfigFile.exists()) {
            objPlugin.getLogger().finest("'" + objWorldConfigFile.getAbsoluteFile() + "' does not exist, unpacking...");
            objPlugin.getPluginZipFile().unzipPathAs("world-config.yml", objWorldConfigFile);
        }
    }

    public void loadConfig() {
        try {
            saveDefaultConfig();
            objWorldConfig.load(objWorldConfigFile);

            bWorldIsEnabled = objWorldConfig.getBoolean("World.Enabled", objPlugin.bolDefaultIsEnabled());
            bWorldDoPredictPosition = objWorldConfig.getBoolean("World.DoPredictPosition", objPlugin.bolDefaultDoPredictPosition());
            iWorldDelayedTicks = objWorldConfig.getInt("World.DelayedTicks", objPlugin.intDefaultDelayedTicks());
            objWorldSyncTimeTo = objPlugin.getServer().getWorld(objWorldConfig.getString("World.SyncTimeTo", objPlugin.strDefaultSyncTimeTo()));
            
            if (bWorldIsEnabled == true) {
                if (mapWorldOverlapTriggers != null) {
                    mapWorldOverlapTriggers.clear();
                    mapWorldOverlapTriggers = null;
                }
                mapWorldOverlapTriggers = new EnumMap<Triggers, Boolean>(Triggers.class);
                for (Triggers trigger : Triggers.values()) {
                    mapWorldOverlapTriggers.put(trigger, objWorldConfig.getBoolean("World.OverlapTriggers." + trigger.getName(), objPlugin.oemDefaultLowerTeleportEntityFilter().get(trigger)));
                }

                objUpperWorld = objPlugin.getServer().getWorld(objWorldConfig.getString("Upper.World", objPlugin.strDefaultUpperWorld()));
                objLowerWorld = objPlugin.getServer().getWorld(objWorldConfig.getString("Lower.World", objPlugin.strDefaultLowerWorld()));
                bUpperOverlapEnabled = objWorldConfig.getBoolean("Upper.Overlap.Enabled", objPlugin.bolDefaultUpperOverlapEnabled());
                bLowerOverlapEnabled = objWorldConfig.getBoolean("Lower.Overlap.Enabled", objPlugin.bolDefaultLowerOverlapEnabled());
                iUpperOverlapFrom = objWorldConfig.getInt("Upper.Overlap.From", objPlugin.intDefaultUpperOverlapFrom());
                iLowerOverlapFrom = objWorldConfig.getInt("Lower.Overlap.From", objPlugin.intDefaultLowerOverlapFrom());
                iUpperOverlapTo = objWorldConfig.getInt("Upper.Overlap.To", objPlugin.intDefaultUpperOverlapTo());
                iLowerOverlapTo = objWorldConfig.getInt("Lower.Overlap.To", objPlugin.intDefaultLowerOverlapTo());
                iUpperOverlapLayers = objWorldConfig.getInt("Upper.Overlap.Layers", objPlugin.intDefaultUpperOverlapLayers());
                iLowerOverlapLayers = objWorldConfig.getInt("Lower.Overlap.Layers", objPlugin.intDefaultLowerOverlapLayers());
                bUpperTeleportEnabled = objWorldConfig.getBoolean("Upper.Teleport.Enabled", objPlugin.bolDefaultUpperTeleportEnabled());
                bLowerTeleportEnabled = objWorldConfig.getBoolean("Lower.Teleport.Enabled", objPlugin.bolDefaultLowerTeleportEnabled());
                iUpperTeleportFrom = objWorldConfig.getInt("Upper.Teleport.From", objPlugin.intDefaultUpperTeleportFrom());
                iLowerTeleportFrom = objWorldConfig.getInt("Lower.Teleport.From", objPlugin.intDefaultLowerTeleportFrom());
                iUpperTeleportTo = objWorldConfig.getInt("Upper.Teleport.To", objPlugin.intDefaultUpperTeleportTo());
                iLowerTeleportTo = objWorldConfig.getInt("Lower.Teleport.To", objPlugin.intDefaultLowerTeleportTo());
                bUpperTeleportPreserveEntityVelocity = objWorldConfig.getBoolean("Upper.PreserveEntityVelocity", objPlugin.bolDefaultUpperTeleportPreserveEntityVelocity());
                bLowerTeleportPreserveEntityVelocity = objWorldConfig.getBoolean("Lower.PreserveEntityVelocity", objPlugin.bolDefaultLowerTeleportPreserveEntityVelocity());
                bUpperTeleportPreserveEntityFallDistance = objWorldConfig.getBoolean("Upper.PreserveEntityFallDistance", objPlugin.bolDefaultUpperTeleportPreserveEntityFallDistance());
                bLowerTeleportPreserveEntityFallDistance = objWorldConfig.getBoolean("Lower.PreserveEntityFallDistance", objPlugin.bolDefaultLowerTeleportPreserveEntityFallDistance());
                bLowerTeleportPreventFallDamage = objWorldConfig.getBoolean("Lower.Teleport.PreventFallDamage", objPlugin.bolDefaultLowerTeleportPreventFallDamage());
                if (mapUpperTeleportEntityFilter != null) {
                    mapUpperTeleportEntityFilter.clear();
                    mapUpperTeleportEntityFilter = null;
                }
                mapUpperTeleportEntityFilter = new EnumMap<EntityType, Boolean>(EntityType.class);
                if (mapLowerTeleportEntityFilter != null) {
                    mapLowerTeleportEntityFilter.clear();
                    mapLowerTeleportEntityFilter = null;
                }
                mapLowerTeleportEntityFilter = new EnumMap<EntityType, Boolean>(EntityType.class);
                for (EntityType et : EntityType.values()) {
                    mapUpperTeleportEntityFilter.put(et, objWorldConfig.getBoolean("Upper.Teleport.EntityFilter." + et.getName(), objPlugin.oemDefaultUpperTeleportEntityFilter().get(et)));
                }
                for (EntityType et : EntityType.values()) {
                    mapLowerTeleportEntityFilter.put(et, objWorldConfig.getBoolean("Lower.Teleport.EntityFilter." + et.getName(), objPlugin.oemDefaultLowerTeleportEntityFilter().get(et)));
                }

                //This contains all changed blocks in this world.
                overlapCreateChunkMap();

                /*
                 * Create a Task that handles some values every x Ticks.
                 */
                if (objWorldHandlerRunnable == null) {
                    objWorldHandlerRunnable = new WorldHandlerRunnable(objPlugin, this);
                }
                if ((iWorldDelayedTicks > 0)
                    && (((objUpperWorld != null) && (bUpperTeleportEnabled == true))
                        || ((objLowerWorld != null) && (bLowerTeleportEnabled == true)))) {
                    if (iWorldHandlerRunnableTask != -1) {
                        objPlugin.getServer().getScheduler().cancelTask(iWorldHandlerRunnableTask);
                    }
                    iWorldHandlerRunnableTask = objPlugin.getServer().getScheduler().scheduleSyncRepeatingTask(objPlugin, objWorldHandlerRunnable, iWorldDelayedTicks, iWorldDelayedTicks);
                    if (iWorldHandlerRunnableTask == -1) {
                        objPlugin.getLogger().warning("<" + objWorld.getName() + "> Could not register synchronized repeating task. Entities can not be teleported!");
                    } else {
                        objPlugin.getLogger().info("<" + objWorld.getName() + "> WorldHandler enabled.");
                    }
                }
                if (iWorldHandlerRunnableTask == -1) {
                    objPlugin.getLogger().info("<" + objWorld.getName() + "> Teleportation disabled.");
                }
            } else {
                if (iWorldHandlerRunnableTask != -1) {
                    objPlugin.getServer().getScheduler().cancelTask(iWorldHandlerRunnableTask);
                    iWorldHandlerRunnableTask = -1;
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
        if (objWorldSyncTimeTo != null) {
            objWorld.setTime(objWorldSyncTimeTo.getTime());
        }
        
        for (Entity ent : objWorld.getEntities()) {
            Location _EntityLocation = ent.getLocation();
            Vector _EntityVelocity = ent.getVelocity();
            float _EntityDistanceFallen = ent.getFallDistance();

            if (bWorldDoPredictPosition) {
                //Advance the entites position by their velocity * objPlugin.getDelayedTicks().
                _EntityLocation.setX(_EntityLocation.getX() + _EntityVelocity.getX() * iWorldDelayedTicks);
                _EntityLocation.setY(_EntityLocation.getY() + _EntityVelocity.getY() * iWorldDelayedTicks);
                _EntityLocation.setZ(_EntityLocation.getZ() + _EntityVelocity.getZ() * iWorldDelayedTicks);
            }

            //1. Step: Check if we can skip this entity. Helps save CPU time.
            if ((objUpperWorld == null) && (objLowerWorld == null)) {
                continue;
            } else {
                if (objUpperWorld != null) {
                    if (mapUpperTeleportEntityFilter.get(ent.getType()) == true) {
                        continue;
                    }
                    if (_EntityLocation.getY() >= iUpperTeleportFrom) {
                        //2. Step: We can't skip it so let's just do what is needed
                        Location _UpperWorldExit = new Location(objUpperWorld,
                                                                ent.getLocation().getX(),
                                                                iUpperTeleportTo - (ent.getLocation().getY() - iUpperTeleportFrom),
                                                                ent.getLocation().getZ());
                        _UpperWorldExit.setPitch(ent.getLocation().getPitch());
                        _UpperWorldExit.setYaw(ent.getLocation().getYaw());

                        if (objPlugin.getAPI().teleport(ent, _UpperWorldExit)) {
                            if (!bUpperTeleportPreserveEntityVelocity) {
                                ent.setVelocity(new Vector(0, 0, 0));
                            }
                            if (!bUpperTeleportPreserveEntityFallDistance) {
                                ent.setFallDistance(0);
                            }
                        }
                    }
                }
                if (objLowerWorld != null) {
                    if (mapLowerTeleportEntityFilter.get(ent.getType()) == true) {
                        continue;
                    }
                    if (_EntityLocation.getY() <= iLowerTeleportFrom) {
                        //2. Step: We can't skip it so let's just do what is needed
                        Location _LowerWorldExit = new Location(objLowerWorld,
                                                                ent.getLocation().getX(),
                                                                iLowerTeleportTo + (ent.getLocation().getY() - iLowerTeleportFrom),
                                                                ent.getLocation().getZ());
                        _LowerWorldExit.setPitch(ent.getLocation().getPitch());
                        _LowerWorldExit.setYaw(ent.getLocation().getYaw());
                        if (objPlugin.getAPI().teleport(ent, _LowerWorldExit)) {
                            if (!bLowerTeleportPreserveEntityVelocity) {
                                ent.setVelocity(new Vector(0, 0, 0));
                            }
                            if (!bLowerTeleportPreserveEntityFallDistance) {
                                ent.setFallDistance(0);
                            }
                            if (bLowerTeleportPreventFallDamage) {
                                ent.setMetadata("takeFallDamage", new FixedMetadataValue(objPlugin, true));
                            }
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
        if (bWorldIsEnabled == true) {
            if ((bUpperOverlapEnabled == true) && (objUpperWorld != null) && (iUpperOverlapTo <= objWorld.getMaxHeight()) && (iUpperOverlapFrom >= 0)) {
                Chunk chunkUpper = objUpperWorld.getChunkAt(chunk.getX(), chunk.getZ());
                if (chunkUpper != null) {
                    boolean manualLoad = false;
                    if (chunkUpper.isLoaded() == false) {
                        //Quick & Dirty hack to prevent recursive calls due to infinite events...
                        mapChunkOverlapChangedBlocksType.put(chunkUpper, new HashMap<BlockVector, Material>());
                        chunkUpper.load(true);
                        manualLoad = true;
                    }
                    for (int layer = 0; layer < iUpperOverlapLayers; layer++) {
                        for (int x = 0; x < 16; x++) {
                            for (int z = 0; z < 16; z++) {
                                Block block = chunk.getBlock(x, iUpperOverlapTo - (iUpperOverlapLayers - 1) + layer, z);
                                Block blockUpper = chunkUpper.getBlock(x, iUpperOverlapFrom + layer, z);
                                if (block != null) {
                                    if (blockUpper != null) {
                                        if (block.getType() == Material.AIR) {
                                            BlockVector pos = new BlockVector(x, iUpperOverlapTo - layer, z);
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
            if ((bLowerOverlapEnabled == true && objLowerWorld != null) && (iLowerOverlapTo >= 0) && (iLowerOverlapFrom <= 255)) {
                Chunk chunkLower = objLowerWorld.getChunkAt(chunk.getX(), chunk.getZ());
                if (chunkLower != null) {
                    boolean manualLoad = false;
                    if (chunkLower.isLoaded() == false) {
                        //Quick & Dirty hack to prevent recursive calls due to infinite events...
                        mapChunkOverlapChangedBlocksType.put(chunkLower, new HashMap<BlockVector, Material>());
                        chunkLower.load(true);
                        manualLoad = true;
                    }
                    for (int layer = 0; layer < iLowerOverlapLayers; layer++) {
                        for (int x = 0; x < 16; x++) {
                            for (int z = 0; z < 16; z++) {
                                Block block = chunk.getBlock(x, iLowerOverlapTo + layer, z);
                                Block blockLower = chunkLower.getBlock(x, iLowerOverlapFrom - (iLowerOverlapLayers - 1) + layer, z);
                                if (block != null) {
                                    if (blockLower != null) {
                                        if (block.getType() == Material.AIR) {
                                            BlockVector pos = new BlockVector(x, iLowerOverlapTo + layer, z);
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
        if (bUpperOverlapEnabled && (objUpperWorld != null)) {
            if ((localPosition.getBlockY() <= iUpperOverlapTo) && (localPosition.getBlockY() > iUpperOverlapTo - iUpperOverlapLayers)) {
                objPlugin.getWorldHandlers().get(objUpperWorld).replaceBlock(block.getType(), block.getData(), new BlockVector(block.getX(), iUpperOverlapTo - block.getY(), block.getZ()), false);
                removeListedBlock(block.getChunk(), block.getLocation().toVector().toBlockVector());
            }
        }
        if (bLowerOverlapEnabled && (objLowerWorld != null)) {
            if ((localPosition.getBlockY() >= iLowerOverlapTo) && (localPosition.getBlockY() < iLowerOverlapTo + iLowerOverlapLayers)) {
                objPlugin.getWorldHandlers().get(objLowerWorld).replaceBlock(block.getType(), block.getData(), new BlockVector(block.getX(), block.getY() - iLowerOverlapTo, block.getZ()), true);
                removeListedBlock(block.getChunk(), block.getLocation().toVector().toBlockVector());
            }
        }
    }

    private void overlapBlockBreak(Block block) {
        Location localPosition = block.getLocation();
        if (bUpperOverlapEnabled && (objUpperWorld != null)) {
            if ((localPosition.getBlockY() <= iUpperOverlapTo) && (localPosition.getBlockY() > iUpperOverlapTo - iUpperOverlapLayers)) {
                objPlugin.getWorldHandlers().get(objUpperWorld).replaceBlock(Material.AIR, new Byte((byte) 0), new BlockVector(block.getX(), iUpperOverlapTo - block.getY(), block.getZ()), false);
                removeListedBlock(block.getChunk(), block.getLocation().toVector().toBlockVector());
            }
        }
        if (bLowerOverlapEnabled && (objLowerWorld != null)) {
            if ((localPosition.getBlockY() >= iLowerOverlapTo) && (localPosition.getBlockY() < iLowerOverlapTo + iLowerOverlapLayers)) {
                objPlugin.getWorldHandlers().get(objLowerWorld).replaceBlock(Material.AIR, new Byte((byte) 0), new BlockVector(block.getX(), block.getY() - iLowerOverlapTo, block.getZ()), true);
                removeListedBlock(block.getChunk(), block.getLocation().toVector().toBlockVector());
            }
        }
    }

    /*
     * External
     */
    public void chunkLoadEvent(ChunkLoadEvent event) {
        if (bWorldIsEnabled && mapWorldOverlapTriggers.get("ChunkLoadUnload")) {
            Chunk chunk = event.getChunk();
            //Quick & Dirty hack to prevent recursive calls due to infinite events...
            if (!mapChunkOverlapChangedBlocksType.containsKey(chunk)) {
                overlapLoadChunk(chunk);
            }
        }

    }

    public void chunkUnloadEvent(ChunkUnloadEvent event) {
        if (bWorldIsEnabled && mapWorldOverlapTriggers.get(Triggers.ChunkLoadUnload)) {
            Chunk chunk = event.getChunk();
            if (mapChunkOverlapChangedBlocksType.containsKey(chunk)) {
                overlapUnloadChunk(chunk);
            }
        }
    }

    public void blockPlaceEvent(BlockPlaceEvent event) {
        if (bWorldIsEnabled && mapWorldOverlapTriggers.get(Triggers.BlockPlace) && !event.isCancelled()) {
            overlapBlockPlace(event.getBlockPlaced());

        }
    }

    public void blockBreakEvent(BlockBreakEvent event) {
        if (bWorldIsEnabled && mapWorldOverlapTriggers.get(Triggers.BlockBreak) && !event.isCancelled()) {
            overlapBlockBreak(event.getBlock());
        }
    }

    public void blockBurnEvent(BlockBurnEvent event) {
        if (bWorldIsEnabled && mapWorldOverlapTriggers.get(Triggers.BlockBurn) && !event.isCancelled()) {
            overlapBlockBreak(event.getBlock());
        }
    }

    public void blockFadeEvent(BlockFadeEvent event) {
        if (bWorldIsEnabled && mapWorldOverlapTriggers.get(Triggers.BlockFade) && !event.isCancelled()) {
            overlapBlockBreak(event.getBlock());
        }
    }

    public void blockFormEvent(BlockFormEvent event) {
        if (bWorldIsEnabled && mapWorldOverlapTriggers.get(Triggers.BlockForm) && !event.isCancelled()) {
            overlapBlockPlace(event.getBlock());
        }
    }

    public void blockGrowEvent(BlockGrowEvent event) {
        if (bWorldIsEnabled && mapWorldOverlapTriggers.get(Triggers.BlockGrow) && !event.isCancelled()) {
            overlapBlockPlace(event.getBlock());
        }
    }

    public void blockSpreadEvent(BlockSpreadEvent event) {
        if (bWorldIsEnabled && mapWorldOverlapTriggers.get(Triggers.BlockSpread) && !event.isCancelled()) {
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
            localPosition.setY(iUpperOverlapTo - (iUpperOverlapLayers - 1) + Position.getBlockY());
        } else {
            localPosition.setY(iLowerOverlapTo + (iLowerOverlapLayers - 1) - Position.getBlockY());
        }

        Block block = objWorld.getBlockAt(localPosition.getBlockX(), localPosition.getBlockY(), localPosition.getBlockZ());
        Chunk chunk = objWorld.getChunkAt(block);
        removeListedBlock(chunk, block.getLocation().toVector().toBlockVector());
        block.setType(withMat);
        block.setData(withData);
    }
}
