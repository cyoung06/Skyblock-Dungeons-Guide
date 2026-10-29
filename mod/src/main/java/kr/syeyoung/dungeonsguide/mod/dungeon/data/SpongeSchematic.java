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

import com.mojang.datafixers.DSL;
import com.mojang.serialization.Dynamic;
import lombok.Getter;
import net.minecraft.SharedConstants;
import net.minecraft.commands.arguments.blocks.BlockStateParser;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.*;
import net.minecraft.util.datafix.DataFixers;
import net.minecraft.util.datafix.fixes.References;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
public class SpongeSchematic {
    private final int width, height, length;
    private final int[] offset;
    private final List<BlockState> palette = new ArrayList<>();
    private final Map<BlockState, Integer> paletteIndices = new HashMap<>();
    private final int[] blocks;
    private ListTag blockEntities = new ListTag();
    private ListTag entities = new ListTag();

    public SpongeSchematic(int width, int height, int length, int[] offset) {
        this.width = width;
        this.height = height;
        this.length = length;
        this.offset = offset;
        this.blocks = new int[width * height * length];
        paletteIndex(Blocks.AIR.defaultBlockState());
    }

    private int paletteIndex(BlockState state) {
        return paletteIndices.computeIfAbsent(state, s -> {
            palette.add(s);
            return palette.size() - 1;
        });
    }

    public BlockState getBlock(int x, int y, int z) {
        return palette.get(blocks[x + (z + y * length) * width]);
    }

    public void setBlock(int x, int y, int z, BlockState state) {
        blocks[x + (z + y * length) * width] = paletteIndex(state);
    }

    // upgrades the block states, block entities and entities to the running game's data version
    public static SpongeSchematic read(byte[] nbt) throws IOException {
        CompoundTag schematic = NbtIo.read(new DataInputStream(new ByteArrayInputStream(nbt)), NbtAccounter.unlimitedHeap())
                .getCompoundOrEmpty("Schematic");
        int dataVersion = schematic.getIntOr("DataVersion", 0);
        int current = SharedConstants.getCurrentVersion().dataVersion().version();

        SpongeSchematic result = new SpongeSchematic(
                schematic.getShortOr("Width", (short) 0) & 0xFFFF,
                schematic.getShortOr("Height", (short) 0) & 0xFFFF,
                schematic.getShortOr("Length", (short) 0) & 0xFFFF,
                schematic.getIntArray("Offset").orElse(new int[3]));

        CompoundTag blockContainer = schematic.getCompoundOrEmpty("Blocks");
        CompoundTag palette = blockContainer.getCompoundOrEmpty("Palette");
        BlockState[] states = new BlockState[palette.size()];
        for (String key : palette.keySet()) {
            CompoundTag stateTag = blockStateTag(key);
            if (dataVersion < current) stateTag = (CompoundTag) update(References.BLOCK_STATE, stateTag, dataVersion, current);
            states[palette.getIntOr(key, 0)] = NbtUtils.readBlockState(BuiltInRegistries.BLOCK, stateTag);
        }

        byte[] data = blockContainer.getByteArray("Data").orElse(new byte[0]);
        int index = 0;
        for (int i = 0; i < data.length && index < result.blocks.length; index++) {
            int value = 0, shift = 0;
            byte b;
            do {
                b = data[i++];
                value |= (b & 0x7F) << shift;
                shift += 7;
            } while ((b & 0x80) != 0);
            result.blocks[index] = result.paletteIndex(states[value]);
        }

        result.blockEntities = blockContainer.getListOrEmpty("BlockEntities");
        result.entities = schematic.getListOrEmpty("Entities");
        if (dataVersion < current) {
            for (Tag tag : result.blockEntities) updateWrapped((CompoundTag) tag, References.BLOCK_ENTITY, dataVersion, current);
            for (Tag tag : result.entities) updateWrapped((CompoundTag) tag, References.ENTITY, dataVersion, current);
        }
        return result;
    }

    public byte[] write() throws IOException {
        CompoundTag palette = new CompoundTag();
        for (int i = 0; i < this.palette.size(); i++) palette.putInt(BlockStateParser.serialize(this.palette.get(i)), i);

        ByteArrayOutputStream data = new ByteArrayOutputStream();
        for (int value : blocks) {
            while ((value & ~0x7F) != 0) {
                data.write((value & 0x7F) | 0x80);
                value >>>= 7;
            }
            data.write(value);
        }

        CompoundTag blockContainer = new CompoundTag();
        blockContainer.put("Palette", palette);
        blockContainer.putByteArray("Data", data.toByteArray());
        blockContainer.put("BlockEntities", blockEntities);

        CompoundTag schematic = new CompoundTag();
        schematic.putInt("Version", 3);
        schematic.putInt("DataVersion", SharedConstants.getCurrentVersion().dataVersion().version());
        schematic.putShort("Width", (short) width);
        schematic.putShort("Height", (short) height);
        schematic.putShort("Length", (short) length);
        schematic.putIntArray("Offset", offset);
        schematic.put("Blocks", blockContainer);
        schematic.put("Entities", entities);

        CompoundTag root = new CompoundTag();
        root.put("Schematic", schematic);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        NbtIo.write(root, new DataOutputStream(out));
        return out.toByteArray();
    }

    private static CompoundTag blockStateTag(String state) {
        CompoundTag tag = new CompoundTag();
        int bracket = state.indexOf('[');
        tag.putString("Name", bracket == -1 ? state : state.substring(0, bracket));
        if (bracket != -1) {
            CompoundTag properties = new CompoundTag();
            for (String property : state.substring(bracket + 1, state.length() - 1).split(",")) {
                int equals = property.indexOf('=');
                if (equals != -1) properties.putString(property.substring(0, equals), property.substring(equals + 1));
            }
            tag.put("Properties", properties);
        }
        return tag;
    }

    private static Tag update(DSL.TypeReference type, Tag tag, int from, int to) {
        return DataFixers.getDataFixer().update(type, new Dynamic<>(NbtOps.INSTANCE, tag), from, to).getValue();
    }

    private static void updateWrapped(CompoundTag wrapper, DSL.TypeReference type, int from, int to) {
        String id = wrapper.getStringOr("Id", "");
        CompoundTag data = wrapper.getCompoundOrEmpty("Data").copy();
        data.putString("id", id);
        CompoundTag fixed = (CompoundTag) update(type, data, from, to);
        wrapper.putString("Id", fixed.getStringOr("id", id));
        fixed.remove("id");
        wrapper.put("Data", fixed);
    }
}
