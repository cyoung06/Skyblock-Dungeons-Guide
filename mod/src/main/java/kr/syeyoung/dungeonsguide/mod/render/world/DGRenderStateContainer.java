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

package kr.syeyoung.dungeonsguide.mod.render.world;

import kr.syeyoung.dungeonsguide.mod.render.world.state.BeaconBeamRenderState;
import kr.syeyoung.dungeonsguide.mod.render.world.state.DGBlockModelRenderState;
import kr.syeyoung.dungeonsguide.mod.render.world.state.FilledBoxRenderState;
import kr.syeyoung.dungeonsguide.mod.render.world.state.LineStripRenderState;
import kr.syeyoung.dungeonsguide.mod.render.world.state.QuadRenderState;
import kr.syeyoung.dungeonsguide.mod.render.world.state.ShaderQuadRenderState;
import kr.syeyoung.dungeonsguide.mod.render.world.state.TexturedQuadRenderState;
import kr.syeyoung.dungeonsguide.mod.render.world.state.WorldImageRenderState;
import kr.syeyoung.dungeonsguide.mod.render.world.state.WorldTextRenderState;
import net.fabricmc.fabric.api.client.rendering.v1.RenderStateDataKey;

import java.util.ArrayList;
import java.util.List;

public class DGRenderStateContainer {
    public static final RenderStateDataKey<DGRenderStateContainer> KEY = RenderStateDataKey.create(() -> "dungeonsguide:render_states");

    public final List<LineStripRenderState> lineStripRenderStates = new ArrayList<>();
    public final List<QuadRenderState> quadRenderStates = new ArrayList<>();
    public final List<FilledBoxRenderState> filledBoxRenderStates = new ArrayList<>();
    public final List<TexturedQuadRenderState> texturedQuadRenderStates = new ArrayList<>();
    public final List<ShaderQuadRenderState> shaderQuadRenderStates = new ArrayList<>();
    public final List<WorldTextRenderState> worldTextRenderStates = new ArrayList<>();
    public final List<BeaconBeamRenderState> beaconBeamRenderStates = new ArrayList<>();
    public final List<DGBlockModelRenderState> blockModelRenderStates = new ArrayList<>();
    public final List<WorldImageRenderState> worldImageRenderStates = new ArrayList<>();
}
