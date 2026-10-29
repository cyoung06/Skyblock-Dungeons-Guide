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
import kr.syeyoung.dungeonsguide.mod.render.world.RenderState;
import kr.syeyoung.dungeonsguide.mod.render.world.RenderStateSubmitter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.BlockModelRenderState;
import net.minecraft.client.renderer.entity.DisplayRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Matrix4f;

public class DGBlockModelRenderState implements RenderState, RenderStateSubmitter<DGBlockModelRenderState> {
    public final BlockModelRenderState renderState = new BlockModelRenderState();
    public Matrix4f transform;
    public boolean depth;

    public DGBlockModelRenderState(BlockState blockState, Matrix4f transform, boolean depth) {
        Minecraft.getInstance().blockModelResolver.update(renderState, blockState, DisplayRenderer.BLOCK_DISPLAY_CONTEXT);
        this.transform = transform;
        this.depth = depth;
    }

    @Override
    public void submit(DGBlockModelRenderState state, CameraRenderState camera, PoseStack poseStack, SubmitNodeCollector collector) {
        // TODO: see-through models (depth false, 1.8.9 drew them with depth off) are depth tested: vanilla's block model
        // submit picks its own render type. decided later
        poseStack.pushPose();
        poseStack.translate(-camera.pos.x, -camera.pos.y, -camera.pos.z);
        poseStack.mulPose(state.transform);
        state.renderState.submit(poseStack, collector, LightCoordsUtil.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, 0);
        poseStack.popPose();
    }
}
