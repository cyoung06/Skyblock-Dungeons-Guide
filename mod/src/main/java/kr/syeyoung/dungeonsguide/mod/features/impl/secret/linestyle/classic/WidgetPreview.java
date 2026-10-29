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

package kr.syeyoung.dungeonsguide.mod.features.impl.secret.linestyle.classic;

import kr.syeyoung.dungeonsguide.mod.features.impl.secret.PathfindLineProperties;
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
import net.minecraft.world.level.block.StairBlock;
import kr.syeyoung.dungeonsguide.mod.render.world.state.LineStripRenderState;
import net.minecraft.world.phys.Vec3;
import java.util.Arrays;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.core.Direction;

public class WidgetPreview extends Widget implements Renderer {

    private ClassicPathDisplayEngineRegistration.ClassicPathDisplayEngineSetting lineProperties;

    public WidgetPreview(ClassicPathDisplayEngineRegistration.ClassicPathDisplayEngineSetting lineProperties) {
        this.lineProperties = lineProperties;
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
        container.blockModelRenderStates.add(new DGBlockModelRenderState(Blocks.STONE_BRICKS.defaultBlockState(), new Matrix4f().translation(2, 1, 4), true));
        container.blockModelRenderStates.add(new DGBlockModelRenderState(Blocks.STONE_BRICK_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.SOUTH), new Matrix4f().translation(2, 1, 3), true));
        container.blockModelRenderStates.add(new DGBlockModelRenderState(Blocks.STONE_BRICK_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.EAST), new Matrix4f().translation(1, 1, 4), true));
        container.blockModelRenderStates.add(new DGBlockModelRenderState(Blocks.STONE_BRICK_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.WEST), new Matrix4f().translation(3, 1, 4), true));
        container.blockModelRenderStates.add(new DGBlockModelRenderState(Blocks.CHEST.defaultBlockState(), new Matrix4f().translation(2, 2, 4), true));

        float scale = 0.2f;
        RenderUtils.drawTextAtWorld(container, "Destination", 2.5f, 2.5f + scale, 4.5f, 0xFF00FF00, 2f, true, false, partialTicks);
        RenderUtils.drawTextAtWorld(container, "3.0m", 2.5f, 2.5f - scale, 4.5f, 0xFFFFFF00, 2f, true, false, partialTicks);

        if (lineProperties.isBeacon()) {
            RenderUtils._renderBeaconBeam(container, 2.5,2.5,4.5, lineProperties.getBeamColor(), partialTicks);
            RenderUtils._highlightBlock(container, new BlockPos(2, 2, 4), lineProperties.getTargetColor(), partialTicks, false);
        }

        if (lineProperties.isPathfind()) {
            int i = RenderUtils.getColorAt( 1,0, lineProperties.getLineColor());
            List<Vec3> points = Arrays.asList(
                    new Vec3(2.5, 2.1, 4), new Vec3(2.5, 2.1, 3.5), new Vec3(2.5, 1.6, 3.5), new Vec3(2.5, 1.6, 3.0),
                    new Vec3(2.5, 1.1, 3), new Vec3(2.5, 1.1, 0.5), new Vec3(0.5, 1.1, 0.5));
            int[] colors = new int[points.size()];
            Arrays.fill(colors, i);
            container.lineStripRenderStates.add(new LineStripRenderState(points, colors, (float) lineProperties.getLineWidth(), false, true));
        }

        context.drawPictureInPicture(new PreviewSceneRenderState(levelState,
                new Matrix4f().rotateX((float) Math.toRadians(25)).rotateY((float) Math.toRadians(125)).translate(4, -7, 2),
                70, (float) (4 * 16 * Math.sqrt(2)), 0, 0, (int) buildContext.getSize().getWidth(), (int) buildContext.getSize().getHeight()));
    }
}
