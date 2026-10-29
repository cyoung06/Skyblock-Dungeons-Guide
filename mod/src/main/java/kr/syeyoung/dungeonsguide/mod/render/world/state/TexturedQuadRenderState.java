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

package kr.syeyoung.dungeonsguide.mod.render.world.state;

import com.mojang.blaze3d.vertex.PoseStack;
import kr.syeyoung.dungeonsguide.mod.render.world.DGRenderTypes;
import kr.syeyoung.dungeonsguide.mod.render.world.RenderState;
import kr.syeyoung.dungeonsguide.mod.render.world.RenderStateSubmitter;
import lombok.AllArgsConstructor;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;

@AllArgsConstructor
public class TexturedQuadRenderState implements RenderState, RenderStateSubmitter<TexturedQuadRenderState> {
    public Identifier texture;
    public Vec3[] corners;
    public float[][] uvs;
    public int color;
    public boolean depth;

    @Override
    public void submit(TexturedQuadRenderState state, CameraRenderState camera, PoseStack poseStack, SubmitNodeCollector collector) {
        collector.submitCustomGeometry(poseStack,
                state.depth ? DGRenderTypes.texturedQuad(state.texture) : DGRenderTypes.texturedQuadSeeThrough(state.texture),
                (pose, consumer) -> {
                    for (int i = 0; i < 4; i++) {
                        Vec3 corner = state.corners[i];
                        consumer.addVertex(pose, (float) (corner.x - camera.pos.x), (float) (corner.y - camera.pos.y), (float) (corner.z - camera.pos.z))
                                .setUv(state.uvs[i][0], state.uvs[i][1]).setColor(state.color);
                    }
                });
    }
}
