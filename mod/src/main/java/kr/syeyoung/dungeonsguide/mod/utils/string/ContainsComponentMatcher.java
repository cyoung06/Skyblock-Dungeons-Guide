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

import java.util.Arrays;

public class ContainsComponentMatcher extends AbstractComponentMatcher {
    private final int[] failure;
    private final boolean styled;

    private final int[] windowRgb;
    private final int[] windowFlags;
    private final FontDescription[] windowFont;
    private final Integer[] windowShadow;
    private int windowEnd;

    ContainsComponentMatcher(int[] codePoints, Spec[] specs) {
        super(codePoints, specs);
        int length = codePoints.length;
        failure = new int[length];
        for (int i = 1, k = 0; i < length; i++) {
            while (k > 0 && codePoints[i] != codePoints[k]) k = failure[k - 1];
            if (codePoints[i] == codePoints[k]) k++;
            failure[i] = k;
        }
        boolean anyStyled = false;
        for (Spec spec : specs) anyStyled |= !spec.any();
        styled = anyStyled;
        windowRgb = new int[length];
        windowFlags = new int[length];
        windowFont = new FontDescription[length];
        windowShadow = new Integer[length];
    }

    @Override
    public boolean matches(Component component) {
        if (codePoints.length == 0) return true;
        windowEnd = 0;
        boolean matched = walkForward(component) == MATCHED;
        Arrays.fill(windowFont, null);
        Arrays.fill(windowShadow, null);
        return matched;
    }

    @Override
    protected int step(int state, int cp, int rgb, int flags, FontDescription font, Integer shadow) {
        int length = codePoints.length;
        if (styled) {
            windowRgb[windowEnd] = rgb;
            windowFlags[windowEnd] = flags;
            windowFont[windowEnd] = font;
            windowShadow[windowEnd] = shadow;
            windowEnd = windowEnd + 1 == length ? 0 : windowEnd + 1;
        }
        if (state == length) state = failure[length - 1];
        while (state > 0 && codePoints[state] != cp) state = failure[state - 1];
        if (codePoints[state] == cp) state++;
        if (state == length) {
            if (!styled || windowMatches()) return MATCHED;
        }
        return state;
    }

    private boolean windowMatches() {
        for (int i = 0, slot = windowEnd; i < codePoints.length; i++, slot = slot + 1 == codePoints.length ? 0 : slot + 1) {
            if (!specs[i].matches(windowRgb[slot], windowFlags[slot], windowFont[slot], windowShadow[slot])) return false;
        }
        return true;
    }
}
