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
import kr.syeyoung.dungeonsguide.mod.render.world.CapturingSubmitNodeCollector;
import kr.syeyoung.dungeonsguide.mod.render.world.DGRenderTypes;
import kr.syeyoung.dungeonsguide.mod.render.world.RenderState;
import kr.syeyoung.dungeonsguide.mod.render.world.RenderStateSubmitter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.BlockModelRenderState;
import net.minecraft.client.renderer.entity.DisplayRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.util.LightCoordsUtil;

import java.util.ArrayList;
import java.util.List;

public class BlockHighlightRenderState implements RenderState, RenderStateSubmitter<BlockHighlightRenderState> {
    public List<BlockPos> blocks;
    public final List<BlockModelRenderState> models = new ArrayList<>();
    public int color;
    public boolean depth;

    public BlockHighlightRenderState(List<BlockPos> blocks, int color, boolean depth) {
        this.blocks = blocks;
        this.color = color;
        this.depth = depth;
        for (BlockPos pos : blocks) {
            BlockModelRenderState model = new BlockModelRenderState();
            Minecraft.getInstance().blockModelResolver.update(model, Minecraft.getInstance().level.getBlockState(pos), DisplayRenderer.BLOCK_DISPLAY_CONTEXT);
            models.add(model);
        }
    }

    @Override
    public void submit(BlockHighlightRenderState state, CameraRenderState camera, PoseStack poseStack, SubmitNodeCollector collector) {
        if (state.depth) {
            submit(state, camera, poseStack, collector, DGRenderTypes.blockHighlight(), DGRenderTypes.modelHighlight());
        } else {
            submit(state, camera, poseStack, collector, DGRenderTypes.blockHighlightDepth(), DGRenderTypes.modelHighlightDepth());
            submit(state, camera, poseStack, collector, DGRenderTypes.blockHighlightSeeThrough(), DGRenderTypes.modelHighlightSeeThrough());
        }
    }

    private void submit(BlockHighlightRenderState state, CameraRenderState camera, PoseStack poseStack, SubmitNodeCollector collector,
                        RenderType blockParts, RenderType models) {
        for (int i = 0; i < state.blocks.size(); i++) {
            BlockPos pos = state.blocks.get(i);
            BlockModelRenderState model = state.models.get(i);
            poseStack.pushPose();
            poseStack.translate(pos.getX() - camera.pos.x, pos.getY() - camera.pos.y, pos.getZ() - camera.pos.z);
            collector.submitCustomGeometry(poseStack, blockParts, (pose, consumer) -> {
                PoseStack modelPose = new PoseStack();
                modelPose.last().set(pose);
                model.submit(modelPose, new CapturingSubmitNodeCollector(consumer, null, state.color), LightCoordsUtil.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, 0);
            });
            collector.submitCustomGeometry(poseStack, models, (pose, consumer) -> {
                PoseStack modelPose = new PoseStack();
                modelPose.last().set(pose);
                model.submit(modelPose, new CapturingSubmitNodeCollector(null, consumer, state.color), LightCoordsUtil.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, 0);
            });
            poseStack.popPose();
        }
    }
}
