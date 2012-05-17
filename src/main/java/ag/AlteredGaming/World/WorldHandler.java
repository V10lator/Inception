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
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
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
    private EnumMap<EntityType, Boolean> mapUpperTeleportEntityFilter;
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
    private EnumMap<EntityType, Boolean> ohmLowerTeleportEntityFilter;
    private WorldHandlerRunnable objWorldHandlerRunnable;
    private int intWorldHandlerRunnableTask = -1;
    private HashMap<Chunk, HashMap<Vector, Material>> mapChunkOverlapChangedBlocksType;
    private HashMap<Chunk, HashMap<Vector, Byte>> mapChunkOverlapChangedBlocksData;

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

            if (bolIsEnabled == true) {
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
                if (mapUpperTeleportEntityFilter != null) {
                    mapUpperTeleportEntityFilter.clear();
                    mapUpperTeleportEntityFilter = null;
                }
                mapUpperTeleportEntityFilter = new EnumMap<EntityType, Boolean>(EntityType.class);
                for (EntityType et : EntityType.values()) {
                    mapUpperTeleportEntityFilter.put(et, objWorldConfig.getBoolean("Upper.Teleport.EntityFilter." + et.getName(), objPlugin.ohmDefaultUpperTeleportEntityFilter().get(et)));
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
                if (ohmLowerTeleportEntityFilter != null) {
                    ohmLowerTeleportEntityFilter.clear();
                    ohmLowerTeleportEntityFilter = null;
                }
                ohmLowerTeleportEntityFilter = new EnumMap<EntityType, Boolean>(EntityType.class);
                for (EntityType et : EntityType.values()) {
                    ohmLowerTeleportEntityFilter.put(et, objWorldConfig.getBoolean("Lower.Teleport.EntityFilter." + et.getName(), objPlugin.ohmDefaultLowerTeleportEntityFilter().get(et)));
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
    public void tickEntityMoved() {
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
                    if (mapUpperTeleportEntityFilter.get(ent.getType()) == true) {
                        continue;
                    }
                    if (_EntityLocation.getY() > intUpperTeleportFrom) {
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
                    if (ohmLowerTeleportEntityFilter.get(ent.getType()) == true) {
                        continue;
                    }
                    if (_EntityLocation.getY() < intLowerTeleportFrom) {
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
        mapChunkOverlapChangedBlocksType = new HashMap<Chunk, HashMap<Vector, Material>>();
        mapChunkOverlapChangedBlocksData = new HashMap<Chunk, HashMap<Vector, Byte>>();
    }

    private void overlapLoadChunk(Chunk chunk) {
        HashMap<Vector, Material> changedBlocksType = new HashMap<Vector, Material>();
        HashMap<Vector, Byte> changedBlocksData = new HashMap<Vector, Byte>();
        mapChunkOverlapChangedBlocksType.put(chunk, changedBlocksType);
        mapChunkOverlapChangedBlocksData.put(chunk, changedBlocksData);
        if (bolIsEnabled == true) {
            if ((bolUpperOverlapEnabled == true) && (objUpperWorld != null)
                && (intUpperOverlapTo <= objWorld.getMaxHeight()) && (intUpperOverlapFrom >= 0)) {
                Chunk chunkUpper = objUpperWorld.getChunkAt(chunk.getX(), chunk.getZ());
                if (chunkUpper != null) {
                    boolean manualLoad = false;
                    if (chunkUpper.isLoaded() == false) {
                        //Quick & Dirty hack to prevent recursive calls due to infinite events...
                        mapChunkOverlapChangedBlocksType.put(chunkUpper, new HashMap<Vector, Material>());
                        chunkUpper.load(true);
                        manualLoad = true;
                    }
                    for (int layer = 0; layer < intUpperOverlapLayers; layer++) {
                        for (int x = 0; x < 16; x++) {
                            for (int z = 0; z < 16; z++) {
                                Block block = chunk.getBlock(x, intUpperOverlapTo - layer, z);
                                Block blockUpper = chunkUpper.getBlock(x, intUpperOverlapFrom + layer, z);
                                if (block != null) {
                                    if (blockUpper != null) {
                                        if (block.getType() == Material.AIR) {
                                            Vector pos = new Vector(x, intUpperOverlapTo - layer, z);
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
            if ((bolLowerOverlapEnabled == true && objLowerWorld != null)
                && (intLowerOverlapTo >= 0) && (intLowerOverlapFrom <= 255)) {
                Chunk ChunkLower = objLowerWorld.getChunkAt(chunk.getX(), chunk.getZ());
                if (ChunkLower != null) {
                    boolean manualLoad = false;
                    if (ChunkLower.isLoaded() == false) {
                        //Quick & Dirty hack to prevent recursive calls due to infinite events...
                        mapChunkOverlapChangedBlocksType.put(ChunkLower, new HashMap<Vector, Material>());
                        ChunkLower.load(true);
                        manualLoad = true;
                    }
                    for (int layer = 0; layer < intLowerOverlapLayers; layer++) {
                        for (int x = 0; x < 16; x++) {
                            for (int z = 0; z < 16; z++) {
                                Block block = chunk.getBlock(x, intLowerOverlapTo + layer, z);
                                Block blockUpper = ChunkLower.getBlock(x, intLowerOverlapFrom - layer, z);
                                if (block != null) {
                                    if (blockUpper != null) {
                                        if (block.getType() == Material.AIR) {
                                            Vector pos = new Vector(x, intLowerOverlapTo - layer, z);
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
                        mapChunkOverlapChangedBlocksType.remove(ChunkLower);
                        ChunkLower.unload(false);
                    }
                }
            }
        }
    }

    private void overlapUnloadChunk(Chunk chunk) {
        HashMap<Vector, Material> changedBlocksType = mapChunkOverlapChangedBlocksType.get(chunk);
        HashMap<Vector, Byte> changedBlocksData = mapChunkOverlapChangedBlocksData.get(chunk);
        for (Vector vec : changedBlocksType.keySet()) {
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
        if ((localPosition.getY() <= intUpperOverlapTo) && (localPosition.getY() > intUpperOverlapTo + intUpperOverlapLayers)) {
            objPlugin.getWorldHandlers().get(objUpperWorld).replaceBlock(block, new Vector(block.getX(), intUpperOverlapTo - block.getY(), block.getZ()), false);
        } else if ((localPosition.getY() >= intLowerOverlapTo) && (localPosition.getY() < intLowerOverlapTo + intLowerOverlapLayers)) {
            objPlugin.getWorldHandlers().get(objLowerWorld).replaceBlock(block, new Vector(block.getX(), block.getY() - intLowerOverlapTo, block.getZ()), true);
        }
    }

    private void overlapBlockBreak(Block block) {
        Location localPosition = block.getLocation();
        if ((localPosition.getY() <= intUpperOverlapTo) && (localPosition.getY() > intUpperOverlapTo + intUpperOverlapLayers)) {
            objPlugin.getWorldHandlers().get(objUpperWorld).replaceBlock(block, new Vector(block.getX(), intUpperOverlapTo - block.getY(), block.getZ()), false);
        } else if ((localPosition.getY() >= intLowerOverlapTo) && (localPosition.getY() < intLowerOverlapTo + intLowerOverlapLayers)) {
            objPlugin.getWorldHandlers().get(objLowerWorld).replaceBlock(block, new Vector(block.getX(), block.getY() - intLowerOverlapTo, block.getZ()), true);
        }
    }

    /*
     * External
     */
    public void chunkLoadEvent(ChunkLoadEvent event) {
        if (bolIsEnabled == true) {
            Chunk chunk = event.getChunk();
            //Quick & Dirty hack to prevent recursive calls due to infinite events...
            if (!mapChunkOverlapChangedBlocksType.containsKey(chunk)) {
                overlapLoadChunk(chunk);
            }
        }

    }

    public void chunkUnloadEvent(ChunkUnloadEvent event) {
        if (bolIsEnabled == true) {
            Chunk chunk = event.getChunk();
            if (mapChunkOverlapChangedBlocksType.containsKey(chunk)) {
                overlapUnloadChunk(chunk);
            }
        }
    }

    public void blockPlaceEvent(BlockPlaceEvent event) {
        if (bolIsEnabled == true) {
            overlapBlockPlace(event.getBlock());
        }
    }

    public void blockBreakEvent(BlockBreakEvent event) {
        if (bolIsEnabled == true) {
            overlapBlockBreak(event.getBlock());
        }
    }

    //Listed Temporary Blocks
    private void removeListedBlock(Chunk chunk, Vector Position) {
        HashMap<Vector, Material> changedBlocksType = mapChunkOverlapChangedBlocksType.get(chunk);
        HashMap<Vector, Byte> changedBlocksData = mapChunkOverlapChangedBlocksData.get(chunk);
        if ((changedBlocksType != null) && (changedBlocksData != null)) {
            if (changedBlocksType.containsKey(Position)) {
                changedBlocksType.remove(Position);
                changedBlocksData.remove(Position);
            }
        }
    }

    public void replaceBlock(Block with, Vector Position, boolean cameFromAbove) {
        Vector localPosition = Position.clone();
        if (cameFromAbove) {
            localPosition.setY(intUpperOverlapTo - Position.getBlockY());
        } else {
            localPosition.setY(intLowerOverlapTo + Position.getBlockY());
        }
        objPlugin.getLogger().info(Position.toString());
        objPlugin.getLogger().info(localPosition.toString());

        Block block = objWorld.getBlockAt(localPosition.getBlockX(), localPosition.getBlockY(), localPosition.getBlockZ());
        Chunk chunk = objWorld.getChunkAt(block);
        removeListedBlock(chunk, block.getLocation().toVector());
        block.setData(with.getData());
        block.setType(with.getType());
    }
}