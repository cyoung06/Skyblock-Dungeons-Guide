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

package kr.syeyoung.dungeonsguide.mod.render.gui;

import com.mojang.blaze3d.ProjectionType;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import kr.syeyoung.dungeonsguide.mod.render.world.DGRenderStateContainer;
import kr.syeyoung.dungeonsguide.mod.render.world.MainPhaseSubmitter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.render.pip.PictureInPictureRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Projection;
import net.minecraft.client.renderer.ProjectionMatrixBuffer;
import net.minecraft.client.renderer.feature.FeatureRenderDispatcher;
import net.minecraft.client.renderer.fog.FogRenderer;

public class PreviewSceneRenderer extends PictureInPictureRenderer<PreviewSceneRenderState> {
    private final Projection projection = new Projection();
    private final ProjectionMatrixBuffer projectionMatrixBuffer = new ProjectionMatrixBuffer("Dungeons Guide preview scene");

    public PreviewSceneRenderer(MultiBufferSource.BufferSource bufferSource) {
        super(bufferSource);
    }

    @Override
    protected void renderToTexture(PreviewSceneRenderState state, PoseStack poseStack) {
        projection.setupPerspective(0.05F, state.zFar(), state.fov(), state.x1() - state.x0(), state.y1() - state.y0());
        RenderSystem.setProjectionMatrix(projectionMatrixBuffer.getBuffer(projection), ProjectionType.PERSPECTIVE);
        poseStack.setIdentity();
        poseStack.mulPose(state.view());

        GpuBufferSlice previousFog = RenderSystem.getShaderFog();
        RenderSystem.setShaderFog(Minecraft.getInstance().gameRenderer.fogRenderer.getBuffer(FogRenderer.FogMode.NONE));
        Minecraft.getInstance().gameRenderer.getLighting().setupFor(Lighting.Entry.LEVEL);
        FeatureRenderDispatcher featureRenderDispatcher = Minecraft.getInstance().gameRenderer.getFeatureRenderDispatcher();
        MainPhaseSubmitter.INSTANCE.submit(state.levelState().getData(DGRenderStateContainer.KEY), state.levelState().cameraRenderState,
                poseStack, featureRenderDispatcher.getSubmitNodeStorage());
        featureRenderDispatcher.renderAllFeatures();
        if (previousFog != null) RenderSystem.setShaderFog(previousFog);
    }

    @Override
    protected String getTextureLabel() {
        return "dungeonsguide_preview_scene";
    }

    @Override
    public Class<PreviewSceneRenderState> getRenderStateClass() {
        return PreviewSceneRenderState.class;
    }

    @Override
    public void close() {
        super.close();
        projectionMatrixBuffer.close();
    }
}
