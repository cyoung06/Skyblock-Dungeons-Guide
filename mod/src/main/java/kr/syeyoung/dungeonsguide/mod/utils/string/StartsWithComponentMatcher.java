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

package kr.syeyoung.dungeonsguide.mod.utils.string;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FontDescription;

public class StartsWithComponentMatcher extends AbstractComponentMatcher {
    StartsWithComponentMatcher(int[] codePoints, Spec[] specs) {
        super(codePoints, specs);
    }

    @Override
    public boolean matches(Component component) {
        return codePoints.length == 0 || walkForward(component) == MATCHED;
    }

    @Override
    protected int step(int state, int cp, int rgb, int flags, FontDescription font, Integer shadow) {
        if (!accepts(state, cp, rgb, flags, font, shadow)) return FAILED;
        return state + 1 == codePoints.length ? MATCHED : state + 1;
    }
}
