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

import com.mojang.blaze3d.vertex.PoseStack;
import kr.syeyoung.dungeonsguide.mod.render.world.state.BeaconBeamRenderState;
import kr.syeyoung.dungeonsguide.mod.render.world.state.BlockHighlightRenderState;
import kr.syeyoung.dungeonsguide.mod.render.world.state.DGBlockModelRenderState;
import kr.syeyoung.dungeonsguide.mod.render.world.state.FilledBoxRenderState;
import kr.syeyoung.dungeonsguide.mod.render.world.state.LineStripRenderState;
import kr.syeyoung.dungeonsguide.mod.render.world.state.QuadRenderState;
import kr.syeyoung.dungeonsguide.mod.render.world.state.ShaderQuadRenderState;
import kr.syeyoung.dungeonsguide.mod.render.world.state.TexturedQuadRenderState;
import kr.syeyoung.dungeonsguide.mod.render.world.state.WorldImageRenderState;
import kr.syeyoung.dungeonsguide.mod.render.world.state.WorldTextRenderState;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.state.level.CameraRenderState;

public class MainPhaseSubmitter implements PhaseSubmitter {
    public static final MainPhaseSubmitter INSTANCE = new MainPhaseSubmitter();

    @Override
    public void submit(DGRenderStateContainer container, CameraRenderState camera, PoseStack poseStack, SubmitNodeCollector collector) {
        for (LineStripRenderState state : container.lineStripRenderStates) state.submit(state, camera, poseStack, collector);
        for (QuadRenderState state : container.quadRenderStates) state.submit(state, camera, poseStack, collector);
        for (FilledBoxRenderState state : container.filledBoxRenderStates) state.submit(state, camera, poseStack, collector);
        for (TexturedQuadRenderState state : container.texturedQuadRenderStates) state.submit(state, camera, poseStack, collector);
        for (ShaderQuadRenderState state : container.shaderQuadRenderStates) state.submit(state, camera, poseStack, collector);
        for (WorldTextRenderState state : container.worldTextRenderStates) state.submit(state, camera, poseStack, collector);
        for (BeaconBeamRenderState state : container.beaconBeamRenderStates) state.submit(state, camera, poseStack, collector);
        for (DGBlockModelRenderState state : container.blockModelRenderStates) state.submit(state, camera, poseStack, collector);
        for (WorldImageRenderState state : container.worldImageRenderStates) state.submit(state, camera, poseStack, collector);
        for (BlockHighlightRenderState state : container.blockHighlightRenderStates) {
            if (state.depth) state.submit(state, camera, poseStack, collector);
        }
    }
}
