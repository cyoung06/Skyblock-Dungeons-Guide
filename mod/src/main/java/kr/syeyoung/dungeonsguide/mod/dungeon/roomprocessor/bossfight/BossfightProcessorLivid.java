/*
 * Dungeons Guide - The most intelligent Hypixel Skyblock Dungeons Mod
 * Copyright (C) 2021  cyoung06
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

package kr.syeyoung.dungeonsguide.mod.dungeon.roomprocessor.bossfight;

import kr.syeyoung.dungeonsguide.mod.utils.string.ComponentMatcher;
import kr.syeyoung.dungeonsguide.mod.events.impl.LivingTickEvent;
import kr.syeyoung.dungeonsguide.mod.events.impl.BlockUpdateEvent;
import kr.syeyoung.dungeonsguide.mod.utils.string.TextUtils;
import kr.syeyoung.dungeonsguide.mod.dungeon.data.LegacyBlock;
import lombok.Getter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.item.DyeColor;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Tuple;

import java.util.*;

@Getter
// TODO: D5 entity name stopgap (D116), the getFormattedText(entity.getName()) reads
public class BossfightProcessorLivid extends GeneralBossfightProcessor {
    private String realLividName = "Hockey";
    private String prefix = "§c";


    private RemotePlayer realLivid;
    private ArmorStand lividStand;

    private final boolean isMasterMode;

    public BossfightProcessorLivid(boolean isMasterMode) {
        super(isMasterMode ? "MASTERMODE_CATACOMBS_FLOOR_FIVE" : "CATACOMBS_FLOOR_FIVE");
        addPhase(PhaseData.builder().phase("start").build());
        this.isMasterMode = isMasterMode;
    }
    private static final Map<String, String> lividColorPrefix = new HashMap<String, String>() {{
            put("Vendetta", "§f"); // white
            put("Crossed", "§d"); // light purple
            put("Hockey", "§c"); // red
            put("Doctor", "§7"); // gray
            put("Frog", "§2"); // green
            put("Smile", "§a"); // light green
            put("Scream", "§1"); // blue
            put("Purple", "§5"); // dark purple
            put("Arcade", "§e"); // yellow
    }};
    private static final Map<Integer, String> lividMetadata = new HashMap<Integer, String>() {{
        put(0, "Vendetta"); // white
        put(2, "Crossed"); // magenta
        put(4, "Arcade"); // yellow
        put(5, "Smile"); // light green
        put(6, "Crossed"); // pink
        put(7, "Doctor"); // gray
        put(8, "Doctor"); // gray
        put(10, "Purple"); // dark purple
        put(11, "Scream"); // blue
        put(13, "Frog"); // green
        put(14, "Hockey"); // red
    }};
    private static final Map<String, ComponentMatcher> lividNameMatchers = new HashMap<>();
    private static final Map<String, ComponentMatcher> lividStandMatchers = new HashMap<>();
    static {
        for (Map.Entry<String, String> entry : lividColorPrefix.entrySet()) {
            lividNameMatchers.put(entry.getKey(), ComponentMatcher.compile("^" + entry.getKey()));
            lividStandMatchers.put(entry.getKey(), ComponentMatcher.compile("^" + entry.getValue() + "﴾ "));
        }
    }

    private int correctLivid = 14;
    @Override
    public void onEntityUpdate(LivingTickEvent updateEvent) {
        correctLivid = LegacyBlock.meta(Minecraft.getInstance().level.getBlockState(new BlockPos(5, 108, 42)));
        realLividName = lividMetadata.get(correctLivid);
        prefix = lividColorPrefix.get(realLividName);
        // old: TextUtils.getFormattedText(updateEvent.entityLiving.getName()).startsWith(realLividName)
        if (lividNameMatchers.get(realLividName).matches(updateEvent.entityLiving.getName()) && updateEvent.entityLiving instanceof RemotePlayer) {
            realLivid = (RemotePlayer) updateEvent.entityLiving;
        // old: TextUtils.getFormattedText(updateEvent.entityLiving.getName()).startsWith(prefix+"﴾ ")
        } else if (lividStandMatchers.get(realLividName).matches(updateEvent.entityLiving.getName()) && updateEvent.entityLiving instanceof ArmorStand) {
            lividStand = (ArmorStand) updateEvent.entityLiving;
        }
    }

    // §2﴾ §2§lLivid§r§r §a317M§c❤ §2﴿
    @Override
    public List<HealthData> getHealths() {
        List<HealthData> healths = new ArrayList<HealthData>();
        long health = 0;
        if (lividStand != null) {
            try {
                String name = TextUtils.stripColor(TextUtils.getFormattedText(lividStand.getName()));
                String healthPart = name.split(" ")[2];
                health = TextUtils.reverseFormat(healthPart.substring(0, healthPart.length() - 1));
            } catch (Exception e) {e.printStackTrace();}
        }
        healths.add(new HealthData(realLividName == null ? "unknown" : realLividName, (int) health,isMasterMode ? 600000000 : 7000000 , true));
        return healths;
    }

    private static final BlockPos lividPos = new BlockPos(6, 108, 43);

    @Override
    public void onBlockUpdate(BlockUpdateEvent blockUpdateEvent) {
    }

    @Override
    public String getBossName() {
        return realLividName == null ? "Livid" : realLividName;
    }

    private static final ComponentMatcher LIVID = ComponentMatcher.compile("^Livid $");
    private static final ComponentMatcher PURPLE_LIVID = ComponentMatcher.compile("^Purple Livid$");
    private static final ComponentMatcher VENDETTA_LIVID = ComponentMatcher.compile("^Vendetta Livid$");
    private static final ComponentMatcher CROSSED_LIVID = ComponentMatcher.compile("^Crossed Livid$");
    private static final ComponentMatcher FROG_LIVID = ComponentMatcher.compile("^Frog Livid$");
    private static final ComponentMatcher HOCKEY_LIVID = ComponentMatcher.compile("^Hockey Livid$");
    private static final ComponentMatcher SMILE_LIVID = ComponentMatcher.compile("^Smile Livid$");
    private static final ComponentMatcher ARCADE_LIVID = ComponentMatcher.compile("^Arcade Livid$");
    private static final ComponentMatcher DOCTOR_LIVID = ComponentMatcher.compile("^Doctor Livid$");
    private static final ComponentMatcher SCREAM_LIVID = ComponentMatcher.compile("^Scream Livid$");

    @Override
    public MarkerData convertToMarker(Entity entity) {
        if (entity instanceof RemotePlayer) {
            int idx = 0;
            // old: name.equals("Livid ")
            if (LIVID.matches(entity.getName())) {
                return MarkerData.fromEntity(entity, MarkerData.MobType.BOSS, 46);
            // old: "Purple Livid".equals(name)
            } else if (PURPLE_LIVID.matches(entity.getName())) {
                idx = 36;
            // old: "Vendetta Livid".equals(name)
            } else if (VENDETTA_LIVID.matches(entity.getName())) {
                idx = 33;
            // old: "Crossed Livid".equals(name)
            } else if (CROSSED_LIVID.matches(entity.getName())) {
                idx = 38;
            // old: "Frog Livid".equals(name)
            } else if (FROG_LIVID.matches(entity.getName())) {
                idx = 35;
            // old: "Hockey Livid".equals(name)
            } else if (HOCKEY_LIVID.matches(entity.getName())) {
                idx = 37;
            // old: "Smile Livid".equals(name)
            } else if (SMILE_LIVID.matches(entity.getName())) {
                idx = 39;
            // old: "Arcade Livid".equals(name)
            } else if (ARCADE_LIVID.matches(entity.getName())) {
                idx = 32;
            // old: "Doctor Livid".equals(name)
            } else if (DOCTOR_LIVID.matches(entity.getName())) {
                idx = 34;
            // old: "Scream Livid".equals(name)
            } else if (SCREAM_LIVID.matches(entity.getName())) {
                idx = 47;
            }
            if (idx == 0) return null;
            // old: name.startsWith(realLividName)
            return MarkerData.fromEntity(entity, lividNameMatchers.get(realLividName).matches(entity.getName()) ? MarkerData.MobType.BOSS : MarkerData.MobType.WRONG_LIVID, idx);
        }
        return null;
    }
}
