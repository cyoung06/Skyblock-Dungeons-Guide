/*
 * Dungeons Guide - The most intelligent Hypixel Skyblock Dungeons Mod
 * Copyright (C) 2024  cyoung06 (syeyoung)
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

package kr.syeyoung.dungeonsguide.mod.features.impl.boss.waypoints;

import kr.syeyoung.dungeonsguide.mod.features.impl.boss.FeatureF7TerminalWaypoints;
import kr.syeyoung.dungeonsguide.mod.features.impl.secret.linestyle.classic.PreviewWorld;
import kr.syeyoung.dungeonsguide.mod.gui.DomElement;
import kr.syeyoung.dungeonsguide.mod.gui.Widget;
import kr.syeyoung.dungeonsguide.mod.gui.renderer.Renderer;
import kr.syeyoung.dungeonsguide.mod.gui.renderer.RenderingContext;
import kr.syeyoung.dungeonsguide.mod.utils.RenderUtils;

import java.util.Collections;
import java.util.List;
import net.minecraft.core.BlockPos;
import kr.syeyoung.dungeonsguide.mod.render.gui.PreviewSceneRenderState;
import kr.syeyoung.dungeonsguide.mod.render.world.DGRenderStateContainer;
import kr.syeyoung.dungeonsguide.mod.render.world.state.DGBlockModelRenderState;
import net.minecraft.client.renderer.state.level.LevelRenderState;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import net.minecraft.world.level.block.Blocks;

public class WidgetPreview extends Widget implements Renderer {

    private FeatureF7TerminalWaypoints waypoints;

    public WidgetPreview(FeatureF7TerminalWaypoints lineProperties) {
        this.waypoints = lineProperties;
    }
    @Override
    public List<Widget> build(DomElement buildContext) {
        return Collections.emptyList();
    }

    @Override
    protected Renderer createRenderer() {
        return this;
    }

    @Override
    public void doRender(float partialTicks, RenderingContext context, DomElement buildContext) {
        LevelRenderState levelState = new LevelRenderState();
        levelState.cameraRenderState.orientation = new Quaternionf().rotateX((float) Math.toRadians(25)).rotateY((float) Math.toRadians(125)).conjugate();
        DGRenderStateContainer container = new DGRenderStateContainer();
        levelState.setData(DGRenderStateContainer.KEY, container);
        for (int x = 0; x < 5; x++) {
            for (int z = 0; z < 5; z++) {
                container.blockModelRenderStates.add(new DGBlockModelRenderState((x + z) % 2 == 0 ? Blocks.SNOW_BLOCK.defaultBlockState() : Blocks.COAL_BLOCK.defaultBlockState(),
                        new Matrix4f().translation(x, 0, z), true));
            }
        }
        container.blockModelRenderStates.add(new DGBlockModelRenderState(Blocks.COMMAND_BLOCK.defaultBlockState(), new Matrix4f().translation(2, 2, 4), true));

        if (waypoints.isBeam())
            RenderUtils._renderBeaconBeam(container, 2.5,2.5,4.5, waypoints.getBeamColor(), partialTicks);
        if (waypoints.isBeacon()) {
            RenderUtils._highlightBlock(container, new BlockPos(2, 2, 4), waypoints.getHighlightColor(), partialTicks, false);
        }

        if (waypoints.isStatus()) {
            float scale = 0.2f;
            int lol = (int) (System.currentTimeMillis() % 6000);
            int type = lol / 2000;
            if (type == 0) {
                RenderUtils.drawTextAtWorld(container, "Done", 2.5f, 2.5f + scale, 4.5f, 0xFF00FF00, 2f, true, false, partialTicks);
            } else if (type == 1) {
                RenderUtils.drawTextAtWorld(container, "syeyoung", 2.5f, 2.5f + scale, 4.5f, 0xFFFFFF00, 2f, true, false, partialTicks);
            } else if (type == 2){
                RenderUtils.drawTextAtWorld(container, "Incomplete", 2.5f, 2.5f + scale, 4.5f, 0xFFFF0000, 2f, true, false, partialTicks);
            }
        }

        context.drawPictureInPicture(new PreviewSceneRenderState(levelState,
                new Matrix4f().rotateX((float) Math.toRadians(25)).rotateY((float) Math.toRadians(125)).translate(4, -7, 2),
                70, (float) (4 * 16 * Math.sqrt(2)), 0, 0, (int) buildContext.getSize().getWidth(), (int) buildContext.getSize().getHeight()));
    }
}
