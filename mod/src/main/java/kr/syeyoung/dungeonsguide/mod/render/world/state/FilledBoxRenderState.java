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
import com.mojang.blaze3d.vertex.VertexConsumer;
import kr.syeyoung.dungeonsguide.mod.render.world.DGRenderTypes;
import kr.syeyoung.dungeonsguide.mod.render.world.RenderState;
import kr.syeyoung.dungeonsguide.mod.render.world.RenderStateSubmitter;
import lombok.AllArgsConstructor;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.phys.AABB;

@AllArgsConstructor
public class FilledBoxRenderState implements RenderState, RenderStateSubmitter<FilledBoxRenderState> {
    public AABB box;
    public int color;
    public boolean depth;

    @Override
    public void submit(FilledBoxRenderState state, CameraRenderState camera, PoseStack poseStack, SubmitNodeCollector collector) {
        AABB box = state.box;
        int color = state.color;
        float x0 = (float) (box.minX - camera.pos.x), y0 = (float) (box.minY - camera.pos.y), z0 = (float) (box.minZ - camera.pos.z);
        float x1 = (float) (box.maxX - camera.pos.x), y1 = (float) (box.maxY - camera.pos.y), z1 = (float) (box.maxZ - camera.pos.z);
        collector.submitCustomGeometry(poseStack, state.depth ? RenderTypes.debugQuads() : DGRenderTypes.filledSeeThrough(), (pose, consumer) -> {
            quad(consumer, pose, color, x0, y0, z0, x0, y0, z1, x0, y1, z1, x0, y1, z0);
            quad(consumer, pose, color, x1, y0, z1, x1, y0, z0, x1, y1, z0, x1, y1, z1);
            quad(consumer, pose, color, x0, y1, z1, x0, y0, z1, x1, y0, z1, x1, y1, z1);
            quad(consumer, pose, color, x0, y0, z0, x0, y1, z0, x1, y1, z0, x1, y0, z0);
            quad(consumer, pose, color, x0, y1, z0, x0, y1, z1, x1, y1, z1, x1, y1, z0);
            quad(consumer, pose, color, x0, y0, z1, x0, y0, z0, x1, y0, z0, x1, y0, z1);
        });
    }

    private static void quad(VertexConsumer consumer, PoseStack.Pose pose, int color,
                             float ax, float ay, float az, float bx, float by, float bz,
                             float cx, float cy, float cz, float dx, float dy, float dz) {
        consumer.addVertex(pose, ax, ay, az).setColor(color);
        consumer.addVertex(pose, bx, by, bz).setColor(color);
        consumer.addVertex(pose, cx, cy, cz).setColor(color);
        consumer.addVertex(pose, dx, dy, dz).setColor(color);
    }
}
