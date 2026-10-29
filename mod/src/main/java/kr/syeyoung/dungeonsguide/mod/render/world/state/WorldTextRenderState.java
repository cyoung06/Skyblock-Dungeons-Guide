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
import lombok.AllArgsConstructor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.network.chat.Style;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.util.StringDecomposer;
import net.minecraft.world.phys.Vec3;

@AllArgsConstructor
public class WorldTextRenderState implements RenderState, RenderStateSubmitter<WorldTextRenderState> {
    public String text;
    public Vec3 pos;
    public int color;
    public float scale;
    public boolean increase;
    public boolean blackBox;
    public boolean depth;

    @Override
    public void submit(WorldTextRenderState state, CameraRenderState camera, PoseStack poseStack, SubmitNodeCollector collector) {
        Font font = Minecraft.getInstance().font;
        Vec3 renderPos = state.pos.subtract(camera.pos);

        float lScale = state.scale;
        if (state.increase) {
            double distance = renderPos.length();
            double multiplier = distance / 120f; //mobs only render ~120 blocks away
            lScale *= 0.45f * multiplier;
        }

        poseStack.pushPose();
        poseStack.translate(renderPos.x, renderPos.y, renderPos.z);
        poseStack.mulPose(camera.orientation);
        poseStack.scale(lScale, -lScale, lScale);

        String text = state.text;
        int textWidth = font.width(text);
        collector.submitText(poseStack, -textWidth / 2, 0, sink -> StringDecomposer.iterateFormatted(text, Style.EMPTY, sink), false,
                state.depth ? Font.DisplayMode.NORMAL : Font.DisplayMode.SEE_THROUGH, LightCoordsUtil.FULL_BRIGHT, state.color,
                state.blackBox ? 0x40000000 : 0, 0);
        poseStack.popPose();
    }
}
