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

package kr.syeyoung.dungeonsguide.mod.gui.elements;

import kr.syeyoung.dungeonsguide.mod.gui.BindableAttribute;
import kr.syeyoung.dungeonsguide.mod.gui.DomElement;
import kr.syeyoung.dungeonsguide.mod.gui.Widget;
import kr.syeyoung.dungeonsguide.mod.gui.elements.popups.MinecraftTooltip;
import kr.syeyoung.dungeonsguide.mod.gui.elements.popups.PopupMgr;
import kr.syeyoung.dungeonsguide.mod.gui.layouter.Layouter;
import kr.syeyoung.dungeonsguide.mod.gui.primitive.ConstraintBox;
import kr.syeyoung.dungeonsguide.mod.gui.primitive.Size;
import kr.syeyoung.dungeonsguide.mod.gui.renderer.Renderer;
import kr.syeyoung.dungeonsguide.mod.gui.renderer.RenderingContext;
import kr.syeyoung.dungeonsguide.mod.gui.xml.AnnotatedExportOnlyWidget;
import kr.syeyoung.dungeonsguide.mod.gui.xml.annotations.Export;
import net.minecraft.client.Minecraft;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ItemStackRender extends AnnotatedExportOnlyWidget implements Renderer, Layouter {
    @Export(attributeName="itemstack")
    public final BindableAttribute<ItemStack> itemstack = new BindableAttribute<ItemStack>(ItemStack.class);

    @Export(attributeName = "hover")
    public final BindableAttribute<Boolean> hover = new BindableAttribute<Boolean>(Boolean.class, false);

    @Override
    public List<Widget> build(DomElement buildContext) {
        return Collections.emptyList();
    }

    @Override
    public void doRender(float partialTicks, RenderingContext context, DomElement buildContext) {
        if (itemstack.getValue() == null) return;

        double min = Math.min(getDomElement().getSize().getWidth(),
                getDomElement().getSize().getHeight());


        context.pose().pushMatrix();
        context.pose().scale((float) (min/18.0), (float) (min/18.0));
        context.drawItem(itemstack.getValue(), 0,0);
        context.pose().popMatrix();
    }

    @Override
    public Size layout(DomElement buildContext, ConstraintBox constraintBox) {
        if (itemstack.getValue() == null) return new Size(constraintBox.getMinWidth(), constraintBox.getMinHeight());

        double size = Math.min(constraintBox.getMaxWidth(), constraintBox.getMaxHeight());

        return new Size(Layouter.clamp(size, constraintBox.getMinWidth(), constraintBox.getMaxWidth()),
                Layouter.clamp(size, constraintBox.getMinHeight(), constraintBox.getMaxHeight()));
    }

    @Override
    public double getMaxIntrinsicWidth(DomElement buildContext, double height) {
        return height;
    }

    @Override
    public double getMaxIntrinsicHeight(DomElement buildContext, double width) {
        return width;
    }


    private final MinecraftTooltip tooltip = new MinecraftTooltip();
    private boolean tooltipOpen;
    @Override
    public boolean mouseMoved(int absMouseX, int absMouseY, double relMouseX, double relMouseY, boolean childHandled) {
        if (hover.getValue() == null || !hover.getValue()) return true;

        List<String> toHover = null;
        if (getDomElement().getAbsBounds().contains(absMouseX, absMouseY)) {
            ItemStack toHoverStack = itemstack.getValue();

            if (toHoverStack != null) {
                // TODO: D5, the lines lose their own formatting until DG's text model is decided
                List<Component> lines = Screen.getTooltipFromItem(Minecraft.getInstance(), toHoverStack);
                List<String> list = new ArrayList<>();
                for (int i = 0; i < lines.size(); ++i) {
                    if (i == 0) {
                        list.add(toHoverStack.getRarity().color() + lines.get(i).getString());
                    } else {
                        list.add(ChatFormatting.GRAY + lines.get(i).getString());
                    }
                }
                toHover= list;
            }
        }

        if (toHover != null)
            tooltip.setTooltip(toHover);

        if (toHover == null && tooltipOpen) {
            PopupMgr.getPopupMgr(getDomElement())
                    .closePopup(tooltip, null);
            tooltipOpen = false;
        } else if (toHover != null && !tooltipOpen) {
            tooltipOpen = true;
            PopupMgr.getPopupMgr(getDomElement())
                    .openPopup(tooltip, (a) -> {
                        tooltipOpen = false;
                    });
        }
        return true;
    }

    @Override
    public void mouseExited(int absMouseX, int absMouseY, double relMouseX, double relMouseY) {
        if (tooltipOpen) {
            PopupMgr.getPopupMgr(getDomElement())
                    .closePopup(tooltip, null);
            tooltipOpen = false;
        }
    }

    @Override
    public void onUnmount() {
        if (tooltipOpen) {
            PopupMgr.getPopupMgr(getDomElement())
                    .closePopup(tooltip, null);
            tooltipOpen = false;
        }
        super.onUnmount();
    }
}
