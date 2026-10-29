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

import java.util.List;

@AllArgsConstructor
public class LineStripRenderState implements RenderState, RenderStateSubmitter<LineStripRenderState> {
    public List<Vec3> points;
    public int[] colors;
    public float width;
    public boolean loop;
    public boolean depth;

    @Override
    public void submit(LineStripRenderState state, CameraRenderState camera, PoseStack poseStack, SubmitNodeCollector collector) {
        collector.submitCustomGeometry(poseStack, state.depth ? RenderTypes.lines() : DGRenderTypes.linesSeeThrough(), (pose, consumer) -> {
            int segments = state.loop ? state.points.size() : state.points.size() - 1;
            for (int i = 0; i < segments; i++) {
                int j = (i + 1) % state.points.size();
                Vec3 from = state.points.get(i), to = state.points.get(j);
                Vec3 normal = to.subtract(from).normalize();
                consumer.addVertex(pose, (float) (from.x - camera.pos.x), (float) (from.y - camera.pos.y), (float) (from.z - camera.pos.z))
                        .setColor(state.colors[i]).setNormal(pose, (float) normal.x, (float) normal.y, (float) normal.z).setLineWidth(state.width);
                consumer.addVertex(pose, (float) (to.x - camera.pos.x), (float) (to.y - camera.pos.y), (float) (to.z - camera.pos.z))
                        .setColor(state.colors[j]).setNormal(pose, (float) normal.x, (float) normal.y, (float) normal.z).setLineWidth(state.width);
            }
        });
    }
}
