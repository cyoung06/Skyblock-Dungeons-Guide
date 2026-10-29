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

package kr.syeyoung.dungeonsguide.mod.render.gui.state;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.VertexConsumer;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.renderer.state.gui.BlitRenderState;
import net.minecraft.client.renderer.state.gui.pip.PictureInPictureRenderState;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3x2f;

import java.awt.*;

public class PictureInPictureState implements DGRenderState {
    @Getter
    private final Matrix3x2f pose;
    @Getter
    private final PictureInPictureRenderState pictureInPicture;
    private final Rectangle scissor;
    private final boolean stencilMask;
    @Getter @Setter
    private BlitRenderState blit;

    public PictureInPictureState(Matrix3x2f pose, PictureInPictureRenderState pictureInPicture, Rectangle scissor, boolean stencilMask) {
        this.pose = pose;
        this.pictureInPicture = pictureInPicture;
        this.scissor = scissor;
        this.stencilMask = stencilMask;
    }

    @Override
    public RenderPipeline pipeline() {
        return blit.pipeline();
    }

    @Override
    public TextureSetup textureSetup() {
        return blit.textureSetup();
    }

    @Override
    public @Nullable Rectangle scissor() {
        return scissor;
    }

    @Override
    public boolean stencilMask() {
        return stencilMask;
    }

    @Override
    public void buildVertices(VertexConsumer consumer) {
        consumer.addVertexWith2DPose(pose, blit.x0(), blit.y0()).setUv(blit.u0(), blit.v0()).setColor(blit.color());
        consumer.addVertexWith2DPose(pose, blit.x0(), blit.y1()).setUv(blit.u0(), blit.v1()).setColor(blit.color());
        consumer.addVertexWith2DPose(pose, blit.x1(), blit.y1()).setUv(blit.u1(), blit.v1()).setColor(blit.color());
        consumer.addVertexWith2DPose(pose, blit.x1(), blit.y0()).setUv(blit.u1(), blit.v0()).setColor(blit.color());
    }
}
