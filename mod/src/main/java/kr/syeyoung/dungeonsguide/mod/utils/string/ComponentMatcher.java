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

// not thread-safe: implementations reuse their walk stack
public interface ComponentMatcher {
    boolean matches(Component component);

    static ComponentMatcher compile(String pattern) {
        AbstractComponentMatcher.ParsedPattern parsed = AbstractComponentMatcher.parse(pattern);
        if (parsed.anchoredStart() && parsed.anchoredEnd()) return new EqualsComponentMatcher(parsed.codePoints(), parsed.specs());
        if (parsed.anchoredStart()) return new StartsWithComponentMatcher(parsed.codePoints(), parsed.specs());
        if (parsed.anchoredEnd()) return new EndsWithComponentMatcher(parsed.codePoints(), parsed.specs());
        return new ContainsComponentMatcher(parsed.codePoints(), parsed.specs());
    }
}
