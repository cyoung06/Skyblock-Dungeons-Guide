/*
 * Dungeons Guide - The most intelligent Hypixel Skyblock Dungeons Mod
 * Copyright (C) 2023  cyoung06 (syeyoung)
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

package kr.syeyoung.dungeonsguide.mod.parallelUniverse.scoreboard;

import kr.syeyoung.dungeonsguide.mod.parallelUniverse.teams.Team;
import kr.syeyoung.dungeonsguide.mod.parallelUniverse.teams.TeamManager;
import kr.syeyoung.dungeonsguide.mod.utils.string.TextUtils;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.numbers.NumberFormat;

@Data
public class Score {
    private final String playerName;
    private final int score;
    private final Component display;
    private final NumberFormat numberFormat;

    @EqualsAndHashCode.Exclude @ToString.Exclude @Getter(AccessLevel.NONE) @Setter(AccessLevel.NONE)
    private Component line;
    @EqualsAndHashCode.Exclude @ToString.Exclude @Getter(AccessLevel.NONE) @Setter(AccessLevel.NONE)
    private Team lineTeam;
    @EqualsAndHashCode.Exclude @ToString.Exclude @Getter(AccessLevel.NONE) @Setter(AccessLevel.NONE)
    private int lineVersion;

    public Component getLine() {
        Team team = TeamManager.INSTANCE.getPlayerTeam(playerName);
        if (line == null || team != lineTeam || team != null && team.getVersion() != lineVersion) {
            if (team == null) {
                line = Component.literal(playerName);
            } else {
                line = Component.empty()
                        .append(team.getPrefix() == null ? Component.empty() : team.getPrefix())
                        .append(Component.literal(playerName))
                        .append(team.getSuffix() == null ? Component.empty() : team.getSuffix());
            }
            lineTeam = team;
            lineVersion = team == null ? 0 : team.getVersion();
        }
        return line;
    }

    public String getVisibleName() {
        Team team =  TeamManager.INSTANCE.getPlayerTeam(playerName);
        if (team != null) {
            return TextUtils.getFormattedText(team.getPrefix()) + playerName + TextUtils.getFormattedText(team.getSuffix());
        }
        return playerName;
    }
    public String getJustTeam() {
        Team team =  TeamManager.INSTANCE.getPlayerTeam(playerName);
        if (team != null) {
            return TextUtils.getFormattedText(team.getPrefix())+ TextUtils.getFormattedText(team.getSuffix());
        }
        return playerName;
    }
}
