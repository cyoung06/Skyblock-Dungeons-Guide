/*
 * Dungeons Guide - The most intelligent Hypixel Skyblock Dungeons Mod
 * Copyright (C) 2026  cyoung06 (syeyoung)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package kr.syeyoung.dungeonsguide.mod.dungeon.data;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mojang.serialization.Dynamic;
import net.minecraft.SharedConstants;
import net.minecraft.commands.arguments.blocks.BlockStateParser;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.util.datafix.DataFixers;
import net.minecraft.util.datafix.fixes.BlockStateData;
import net.minecraft.util.datafix.fixes.References;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.attribute.EnvironmentAttributeReader;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.ticks.BlackholeTickAccess;
import net.minecraft.world.ticks.LevelTickAccess;
import net.minecraft.world.ticks.ScheduledTick;
import net.minecraft.world.ticks.TickPriority;
import org.jetbrains.annotations.Nullable;
import net.minecraft.world.level.block.WallSkullBlock;
import net.minecraft.world.level.block.state.BlockState;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public class RoomDataConverter {
    private static final int FLATTENING_DATA_VERSION = 1451;

    @SuppressWarnings("unchecked")
    public static BlockState[] buildLegacyTable() {
        int current = SharedConstants.getCurrentVersion().dataVersion().version();
        BlockState[] table = new BlockState[4096];
        for (int legacy = 0; legacy < 4096; legacy++) {
            Dynamic<Tag> flattened = (Dynamic<Tag>) BlockStateData.getTag(legacy);
            Dynamic<Tag> upgraded = DataFixers.getDataFixer().update(References.BLOCK_STATE, flattened, FLATTENING_DATA_VERSION, current);
            table[legacy] = NbtUtils.readBlockState(BuiltInRegistries.BLOCK, (CompoundTag) upgraded.getValue());
        }
        for (int meta = 0; meta < 16; meta++) {
            table[144 << 4 | meta] = switch (meta & 7) {
                case 2 -> Blocks.SKELETON_WALL_SKULL.defaultBlockState().setValue(WallSkullBlock.FACING, Direction.NORTH);
                case 3 -> Blocks.SKELETON_WALL_SKULL.defaultBlockState().setValue(WallSkullBlock.FACING, Direction.SOUTH);
                case 4 -> Blocks.SKELETON_WALL_SKULL.defaultBlockState().setValue(WallSkullBlock.FACING, Direction.WEST);
                case 5 -> Blocks.SKELETON_WALL_SKULL.defaultBlockState().setValue(WallSkullBlock.FACING, Direction.EAST);
                default -> Blocks.SKELETON_SKULL.defaultBlockState();
            };
        }
        return table;
    }

    public static void writeTable(BlockState[] table, File file) throws IOException {
        JsonArray entries = new JsonArray();
        for (int legacy = 0; legacy < table.length; legacy++) {
            JsonObject entry = new JsonObject();
            entry.addProperty("id", legacy >> 4);
            entry.addProperty("meta", legacy & 15);
            entry.addProperty("state", BlockStateParser.serialize(table[legacy]));
            entries.add(entry);
        }
        file.getParentFile().mkdirs();
        Files.writeString(file.toPath(), new GsonBuilder().setPrettyPrinting().create().toJson(entries), StandardCharsets.UTF_8);
    }

    public static void convert(DungeonRoomInfo dungeonRoomInfo, BlockState[] table) {
        if (dungeonRoomInfo.getFormatVersion() != 1) return;

        List<String> palette = new ArrayList<>();
        Map<BlockState, Integer> indices = new HashMap<>();

        char[] world = dungeonRoomInfo.getWorld();
        if (world != null) {
            int width = dungeonRoomInfo.getWidth(), length = dungeonRoomInfo.getLength();
            dungeonRoomInfo.setSize(width, length, 256);
            SpongeSchematic spongeSchematic = dungeonRoomInfo.getSpongeSchematic();
            for (int i = 0; i < world.length; i++) {
                spongeSchematic.setBlock(i % width, i / width / length, (i / width) % length, table[world[i] & 4095]);
            }
            fixNeighbourStates(spongeSchematic);
            dungeonRoomInfo.setWorld(null);
        }

        int[][] blocks = dungeonRoomInfo.getBlocks();
        for (int[] row : blocks) {
            for (int i = 0; i < row.length; i++) {
                if (row[i] == -1) continue;
                if (row[i] == 144) {
                    row[i] = -1;
                    continue;
                }
                row[i] = paletteIndex(table[(row[i] << 4) & 4095], palette, indices);
            }
        }

        dungeonRoomInfo.setPalette(palette);
        dungeonRoomInfo.setFormatVersion(2);
    }

    private static int paletteIndex(BlockState state, List<String> palette, Map<BlockState, Integer> indices) {
        return indices.computeIfAbsent(state, s -> {
            palette.add(BlockStateParser.serialize(s));
            return palette.size() - 1;
        });
    }

    private static final Direction[] UPDATE_SHAPE_ORDER = {Direction.WEST, Direction.EAST, Direction.NORTH, Direction.SOUTH, Direction.DOWN, Direction.UP};

    private static void fixNeighbourStates(SpongeSchematic schematic) {
        int width = schematic.getWidth(), height = schematic.getHeight(), length = schematic.getLength();

        for (int y = 0; y < height - 1; y++) {
            for (int z = 0; z < length; z++) {
                for (int x = 0; x < width; x++) {
                    BlockState lower = schematic.getBlock(x, y, z);
                    if (!(lower.getBlock() instanceof DoorBlock) || lower.getValue(BlockStateProperties.DOUBLE_BLOCK_HALF) != DoubleBlockHalf.LOWER) continue;
                    BlockState upper = schematic.getBlock(x, y + 1, z);
                    if (!upper.is(lower.getBlock()) || upper.getValue(BlockStateProperties.DOUBLE_BLOCK_HALF) != DoubleBlockHalf.UPPER) continue;
                    lower = lower.setValue(BlockStateProperties.DOOR_HINGE, upper.getValue(BlockStateProperties.DOOR_HINGE))
                            .setValue(BlockStateProperties.POWERED, upper.getValue(BlockStateProperties.POWERED));
                    schematic.setBlock(x, y, z, lower);
                    schematic.setBlock(x, y + 1, z, lower.setValue(BlockStateProperties.DOUBLE_BLOCK_HALF, DoubleBlockHalf.UPPER));
                }
            }
        }

        for (int y = 0; y < height; y++) {
            for (int z = 0; z < length; z++) {
                for (int x = 0; x < width; x++) {
                    BlockState chest = schematic.getBlock(x, y, z);
                    if (!(chest.getBlock() instanceof ChestBlock) || chest.getValue(ChestBlock.TYPE) != ChestType.SINGLE) continue;
                    Direction facing = chest.getValue(ChestBlock.FACING);
                    Direction toPartner = facing.getClockWise();
                    int px = x + toPartner.getStepX(), pz = z + toPartner.getStepZ();
                    if (px < 0 || px >= width || pz < 0 || pz >= length) continue;
                    BlockState partner = schematic.getBlock(px, y, pz);
                    if (!partner.is(chest.getBlock()) || partner.getValue(ChestBlock.TYPE) != ChestType.SINGLE || partner.getValue(ChestBlock.FACING) != facing) continue;
                    schematic.setBlock(x, y, z, chest.setValue(ChestBlock.TYPE, ChestType.LEFT));
                    schematic.setBlock(px, y, pz, partner.setValue(ChestBlock.TYPE, ChestType.RIGHT));
                }
            }
        }

        SchematicLevel level = new SchematicLevel(schematic);
        RandomSource random = RandomSource.create(0);
        Map<BlockPos, BlockState> updated = new HashMap<>();
        Set<String> unsupported = new TreeSet<>();
        BlockPos.MutableBlockPos neighbourPos = new BlockPos.MutableBlockPos();
        for (BlockPos pos : BlockPos.betweenClosed(0, 0, 0, width - 1, height - 1, length - 1)) {
            BlockState state = level.getBlockState(pos);
            if (state.isAir() || state.getBlock() instanceof LiquidBlock) continue;
            BlockState newState = state;
            try {
                for (Direction direction : UPDATE_SHAPE_ORDER) {
                    neighbourPos.setWithOffset(pos, direction);
                    newState = newState.updateShape(level, level, pos, direction, neighbourPos, level.getBlockState(neighbourPos), random);
                }
            } catch (UnsupportedOperationException e) {
                unsupported.add(BuiltInRegistries.BLOCK.getKey(state.getBlock()).toString());
                continue;
            }
            if (newState != state && newState.is(state.getBlock())) updated.put(pos.immutable(), newState);
        }
        updated.forEach((pos, state) -> schematic.setBlock(pos.getX(), pos.getY(), pos.getZ(), state));
        if (!unsupported.isEmpty()) System.out.println("Kept states of " + unsupported + ", their update needs more than the schematic has");
    }

    private static class SchematicLevel implements LevelReader, ScheduledTickAccess {
        private final SpongeSchematic schematic;

        SchematicLevel(SpongeSchematic schematic) {
            this.schematic = schematic;
        }

        @Override
        public BlockState getBlockState(BlockPos pos) {
            if (pos.getX() < 0 || pos.getX() >= schematic.getWidth() || pos.getY() < 0 || pos.getY() >= schematic.getHeight()
                    || pos.getZ() < 0 || pos.getZ() >= schematic.getLength()) return Blocks.AIR.defaultBlockState();
            return schematic.getBlock(pos.getX(), pos.getY(), pos.getZ());
        }

        @Override
        public FluidState getFluidState(BlockPos pos) {
            return getBlockState(pos).getFluidState();
        }

        @Override
        public @Nullable BlockEntity getBlockEntity(BlockPos pos) {
            return null;
        }

        @Override
        public int getMinY() {
            return 0;
        }

        @Override
        public int getHeight() {
            return schematic.getHeight();
        }

        @Override
        public <T> ScheduledTick<T> createTick(BlockPos pos, T type, int tickDelay, TickPriority priority) {
            return new ScheduledTick<>(type, pos, tickDelay, priority, 0);
        }

        @Override
        public <T> ScheduledTick<T> createTick(BlockPos pos, T type, int tickDelay) {
            return createTick(pos, type, tickDelay, TickPriority.NORMAL);
        }

        @Override
        public LevelTickAccess<Block> getBlockTicks() {
            return BlackholeTickAccess.emptyLevelList();
        }

        @Override
        public LevelTickAccess<Fluid> getFluidTicks() {
            return BlackholeTickAccess.emptyLevelList();
        }

        @Override
        public @Nullable ChunkAccess getChunk(int chunkX, int chunkZ, ChunkStatus targetStatus, boolean loadOrGenerate) {
            return null;
        }

        @Override
        public boolean hasChunk(int chunkX, int chunkZ) {
            return false;
        }

        @Override
        public int getHeight(Heightmap.Types type, int x, int z) {
            return 0;
        }

        @Override
        public int getSkyDarken() {
            return 0;
        }

        @Override
        public BiomeManager getBiomeManager() {
            throw new UnsupportedOperationException();
        }

        @Override
        public Holder<Biome> getUncachedNoiseBiome(int quartX, int quartY, int quartZ) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean isClientSide() {
            return false;
        }

        @Override
        public int getSeaLevel() {
            return 0;
        }

        @Override
        public DimensionType dimensionType() {
            throw new UnsupportedOperationException();
        }

        @Override
        public RegistryAccess registryAccess() {
            throw new UnsupportedOperationException();
        }

        @Override
        public FeatureFlagSet enabledFeatures() {
            throw new UnsupportedOperationException();
        }

        @Override
        public EnvironmentAttributeReader environmentAttributes() {
            throw new UnsupportedOperationException();
        }

        @Override
        public LevelLightEngine getLightEngine() {
            throw new UnsupportedOperationException();
        }

        @Override
        public WorldBorder getWorldBorder() {
            throw new UnsupportedOperationException();
        }

        @Override
        public @Nullable BlockGetter getChunkForCollisions(int chunkX, int chunkZ) {
            return null;
        }

        @Override
        public List<VoxelShape> getEntityCollisions(@Nullable Entity source, AABB testArea) {
            return List.of();
        }
    }
}
