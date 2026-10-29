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

package kr.syeyoung.dungeonsguide.mod;

import java.util.List;
import java.util.ArrayList;
import net.minecraft.network.chat.Component;
import kr.syeyoung.dungeonsguide.mod.utils.string.ComponentMatcher;
import kr.syeyoung.dungeonsguide.mod.events.impl.ClientTickEvent;
import com.google.common.collect.Sets;
import kr.syeyoung.dungeonsguide.mod.dungeon.DungeonContext;
import kr.syeyoung.dungeonsguide.mod.events.impl.DungeonLeftEvent;
import kr.syeyoung.dungeonsguide.mod.events.impl.HypixelJoinedEvent;
import kr.syeyoung.dungeonsguide.mod.events.impl.SkyblockJoinedEvent;
import kr.syeyoung.dungeonsguide.mod.events.impl.SkyblockLeftEvent;
// import kr.syeyoung.dungeonsguide.mod.fakeserver.DungeonServerLaunchUtils;
import kr.syeyoung.dungeonsguide.mod.parallelUniverse.scoreboard.Objective;
import kr.syeyoung.dungeonsguide.mod.parallelUniverse.scoreboard.Score;
import kr.syeyoung.dungeonsguide.mod.parallelUniverse.scoreboard.ScoreboardManager;
import kr.syeyoung.dungeonsguide.mod.utils.string.TextUtils;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.Minecraft;
import kr.syeyoung.dungeonsguide.mod.events.annotations.SubscribeEvent;

import java.util.Collection;
import java.util.Set;

public class SkyblockStatus {
    boolean wasOnHypixel = false;
    private static String locationName;

    public static String getLocationName() {
        return locationName;
    }

    public static void setLocationName(String locationName) {
        SkyblockStatus.locationName = locationName;
    }


    @SubscribeEvent
    public void onTick(ClientTickEvent ev) {
        if (ev.phase != ClientTickEvent.Phase.START) return;

        SkyblockStatus skyblockStatus = DungeonsGuide.getDungeonsGuide().getSkyblockStatus();
        boolean isOnDungeonPrev = isOnDungeon();
        boolean isOnSkyblockPrev = isOnSkyblock();
        boolean isRunningDungeonPrev = isDungeonRunning;
        skyblockStatus.updateStatus();

        if (!wasOnHypixel && skyblockStatus.isOnHypixel()) {
            DungeonsGuide.getDungeonsGuide().getEventBus().fireEvent(new HypixelJoinedEvent());
        }
        wasOnHypixel = skyblockStatus.isOnHypixel();

        if (isOnSkyblockPrev && !isOnSkyblock()) {
            DungeonsGuide.getDungeonsGuide().getEventBus().fireEvent(new SkyblockLeftEvent());
        } else if (!isOnSkyblockPrev && isOnSkyblock()) {
            DungeonsGuide.getDungeonsGuide().getEventBus().fireEvent(new SkyblockJoinedEvent());
        }

        if (isOnDungeonPrev && !isOnDungeon() || isRunningDungeonPrev && !isDungeonRunning) {
            DungeonsGuide.getDungeonsGuide().getEventBus().fireEvent(new DungeonLeftEvent());
        }



    }



    public static boolean isOnSkyblock(){
        SkyblockStatus skyblockStatus = DungeonsGuide.getDungeonsGuide().getSkyblockStatus();

        return skyblockStatus != null && (skyblockStatus.isOnSkyblock || skyblockStatus.forceIsOnDungeon);
    }

    public static boolean isOnDungeon() {
        SkyblockStatus skyblockStatus = DungeonsGuide.getDungeonsGuide().getSkyblockStatus();

        return skyblockStatus != null && (skyblockStatus.forceIsOnDungeon || skyblockStatus.isOnDungeon);
    }


    private boolean isOnSkyblock;
    private boolean isOnDungeon;

    private boolean isDungeonRunning;

    @Getter
    @Setter
    private boolean forceIsOnDungeon;

    @Getter
    @Setter
    private boolean forceIsOnDungeon2;

    public static boolean isOnHypixel() {
        Minecraft mc = Minecraft.getInstance();
        if (mc == null || mc.player == null) return false;
        String clientBrand = mc.getConnection().serverBrand();
        if (clientBrand == null) return false;
        if (!mc.isSingleplayer()) {
            return clientBrand.startsWith("Hypixel BungeeCord");
        }
        return false;
    }

    private static final Set<String> SKYBLOCK_IN_ALL_LANGUAGES = Sets.newHashSet("SKYBLOCK");
    private static final List<ComponentMatcher> SKYBLOCK_TITLES = new ArrayList<>();
    static {
        for (String skyblock : SKYBLOCK_IN_ALL_LANGUAGES) SKYBLOCK_TITLES.add(ComponentMatcher.compile("^§?" + skyblock));
    }
    private static final ComponentMatcher CLEARED = ComponentMatcher.compile("§?Cleared: ");
    private static final ComponentMatcher LOCATION = ComponentMatcher.compile("^ §7\uE067");

    public void updateStatus() {
        // TODO: fakeserver is not in the first 26.1 build. was: DungeonServerLaunchUtils.isDungeonIntegratedServerRunning() && Minecraft.getInstance().player != null && Minecraft.getInstance().level != null || forceIsOnDungeon2
        forceIsOnDungeon = forceIsOnDungeon2;
        if (forceIsOnDungeon) {
            locationName = "TEST DG";
        }

        if (!isOnHypixel()) {
            isOnDungeon = false;
            isOnSkyblock = false;
            return;
        }

        Objective objective = ScoreboardManager.INSTANCE.getSidebarObjective();
        if (objective == null) return;

        boolean skyblockFound = false;
        for (ComponentMatcher skyblock : SKYBLOCK_TITLES) {
            // old: TextUtils.stripColor(TextUtils.getFormattedText(objective.getDisplayName())).startsWith(skyblock)
            if (skyblock.matches(objective.getDisplayName())) {
                skyblockFound = true;
                isOnSkyblock = true;
                break;
            }
        }

        if (!skyblockFound) {
            isOnSkyblock = false;
            isOnDungeon = false;
            return;
        }

        boolean foundDungeon = false;

        Collection<Score> scores = objective.getScores();
        for (Score sc : scores) {
            Component line = sc.getLine();
            // old: TextUtils.keepScoreboardCharacters(TextUtils.stripColor(sc.getJustTeam())).trim().contains("Cleared: ")
            if (CLEARED.matches(line)) {
                foundDungeon = true;
                DungeonContext context = DungeonsGuide.getDungeonsGuide().getDungeonFacade().getContext();
                if(context != null){
                    String visible = TextUtils.getVisibleText(line);
                    int start = visible.indexOf("Cleared: ") + 9;
                    context.setPercentage(Integer.parseInt(visible.substring(start, visible.indexOf('%', start))));
                }
            }
            // old: sc.getJustTeam().startsWith(" §7\uE067")
            if (LOCATION.matches(line)) {
                setLocationName(TextUtils.getVisibleText(line).substring(2).trim());
            }
        }

        isDungeonRunning = foundDungeon;

        if (getLocationName() != null)
            isOnDungeon = getLocationName().startsWith("The Catacombs") | foundDungeon;
        else
            isOnDungeon=false;
    }

}
