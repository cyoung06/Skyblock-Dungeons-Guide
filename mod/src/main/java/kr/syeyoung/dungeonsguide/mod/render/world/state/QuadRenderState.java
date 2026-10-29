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
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.phys.Vec3;

@AllArgsConstructor
public class QuadRenderState implements RenderState, RenderStateSubmitter<QuadRenderState> {
    public Vec3[] corners;
    public int color;
    public boolean depth;

    @Override
    public void submit(QuadRenderState state, CameraRenderState camera, PoseStack poseStack, SubmitNodeCollector collector) {
        collector.submitCustomGeometry(poseStack, state.depth ? RenderTypes.debugQuads() : DGRenderTypes.filledSeeThrough(), (pose, consumer) -> {
            for (Vec3 corner : state.corners) {
                consumer.addVertex(pose, (float) (corner.x - camera.pos.x), (float) (corner.y - camera.pos.y), (float) (corner.z - camera.pos.z)).setColor(state.color);
            }
        });
    }
}
