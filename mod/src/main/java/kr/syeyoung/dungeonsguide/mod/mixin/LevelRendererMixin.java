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
package kr.syeyoung.dungeonsguide.mod.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.framegraph.FrameGraphBuilder;
import com.mojang.blaze3d.framegraph.FramePass;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.resource.RenderTargetDescriptor;
import com.mojang.blaze3d.resource.ResourceHandle;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import kr.syeyoung.dungeonsguide.mod.render.gui.StencilImage;
import kr.syeyoung.dungeonsguide.mod.render.world.DGRenderStateContainer;
import kr.syeyoung.dungeonsguide.mod.render.world.LatePhaseSubmitter;
import kr.syeyoung.dungeonsguide.mod.render.world.state.BlockHighlightRenderState;
import kr.syeyoung.dungeonsguide.mod.render.world.state.WorldImageRenderState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LevelTargetBundle;
import net.minecraft.client.renderer.feature.FeatureRenderDispatcher;
import net.minecraft.client.renderer.fog.FogRenderer;
import net.minecraft.client.renderer.state.level.LevelRenderState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

@Mixin(LevelRenderer.class)
public class LevelRendererMixin {
    @Shadow @Final private LevelRenderState levelRenderState;
    @Shadow @Final private LevelTargetBundle targets;

    @Unique
    private final List<ResourceHandle<StencilImage>> dungeonsguide$worldImages = new ArrayList<>();

    @Inject(method = "renderLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/LevelRenderer;addMainPass(Lcom/mojang/blaze3d/framegraph/FrameGraphBuilder;Lnet/minecraft/client/renderer/culling/Frustum;Lorg/joml/Matrix4fc;Lcom/mojang/blaze3d/buffers/GpuBufferSlice;ZLnet/minecraft/client/renderer/state/level/LevelRenderState;Lnet/minecraft/client/DeltaTracker;Lnet/minecraft/util/profiling/ProfilerFiller;Lnet/minecraft/client/renderer/chunk/ChunkSectionsToRender;)V"))
    private void dungeonsguide$addWorldImagePasses(CallbackInfo ci, @Local FrameGraphBuilder frame) {
        dungeonsguide$worldImages.clear();
        DGRenderStateContainer container = levelRenderState.getData(DGRenderStateContainer.KEY);
        if (container == null) return;
        for (WorldImageRenderState state : container.worldImageRenderStates) {
            FramePass pass = frame.addPass("dungeonsguide_world_image");
            ResourceHandle<StencilImage> image = pass.readsAndWrites(frame.importExternal("dungeonsguide_world_image", state.image));
            pass.executes(() -> {
                GpuBufferSlice noFog = Minecraft.getInstance().gameRenderer.fogRenderer.getBuffer(FogRenderer.FogMode.NONE);
                state.prepare(noFog);
            });
            dungeonsguide$worldImages.add(image);
        }
    }

    @ModifyExpressionValue(method = "addMainPass", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/framegraph/FrameGraphBuilder;addPass(Ljava/lang/String;)Lcom/mojang/blaze3d/framegraph/FramePass;"))
    private FramePass dungeonsguide$readWorldImages(FramePass pass) {
        for (ResourceHandle<StencilImage> image : dungeonsguide$worldImages) {
            pass.reads(image);
        }
        return pass;
    }

    @Inject(method = "renderLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/LevelRenderer;addLateDebugPass(Lcom/mojang/blaze3d/framegraph/FrameGraphBuilder;Lnet/minecraft/client/renderer/state/level/CameraRenderState;Lcom/mojang/blaze3d/buffers/GpuBufferSlice;Lorg/joml/Matrix4fc;)V"))
    private void dungeonsguide$addLatePass(CallbackInfo ci, @Local FrameGraphBuilder frame) {
        DGRenderStateContainer container = levelRenderState.getData(DGRenderStateContainer.KEY);
        if (container == null) return;
        boolean any = false;
        for (BlockHighlightRenderState state : container.blockHighlightRenderStates) {
            if (!state.depth) any = true;
        }
        if (!any) return;

        FramePass pass = frame.addPass("dungeonsguide_late");
        targets.main = pass.readsAndWrites(targets.main);
        ResourceHandle<RenderTarget> main = targets.main;
        RenderTarget mainTarget = Minecraft.getInstance().getMainRenderTarget();
        ResourceHandle<RenderTarget> depth = pass.createsInternal("dungeonsguide_late_depth", new RenderTargetDescriptor(mainTarget.width, mainTarget.height, true, 0));
        pass.executes(() -> {
            RenderSystem.outputColorTextureOverride = main.get().getColorTextureView();
            RenderSystem.outputDepthTextureOverride = depth.get().getDepthTextureView();
            FeatureRenderDispatcher featureRenderDispatcher = Minecraft.getInstance().gameRenderer.getFeatureRenderDispatcher();
            LatePhaseSubmitter.INSTANCE.submit(container, levelRenderState.cameraRenderState, new PoseStack(), featureRenderDispatcher.getSubmitNodeStorage());
            featureRenderDispatcher.renderAllFeatures();
            RenderSystem.outputColorTextureOverride = null;
            RenderSystem.outputDepthTextureOverride = null;
        });
    }
}
