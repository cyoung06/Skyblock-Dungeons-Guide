/*
 * Dungeons Guide - The most intelligent Hypixel Skyblock Dungeons Mod
 * Copyright (C) 2022  cyoung06 (syeyoung)
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

package kr.syeyoung.dungeonsguide.mod.features.impl.party.playerpreview;

import com.mojang.authlib.GameProfile;
import kr.syeyoung.dungeonsguide.mod.features.impl.party.playerpreview.api.SkinFetcher;
import kr.syeyoung.dungeonsguide.mod.features.impl.party.playerpreview.api.playerprofile.PlayerProfile;
import kr.syeyoung.dungeonsguide.mod.utils.string.TextUtils;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.core.ClientAsset;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.PlayerModelType;
import net.minecraft.world.entity.player.PlayerSkin;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.scores.Team;

public class FakePlayer extends RemotePlayer {
    @Getter
    private PlayerProfile skyblockProfile;

    public void setSkyblockProfile(PlayerProfile skyblockProfile) {
        this.skyblockProfile = skyblockProfile;

        armor = skyblockProfile.getCurrentArmor();
        if (skyblockProfile.getCurrentArmor() != null)
            setArmor(skyblockProfile.getCurrentArmor().getArmorSlots());
        else
            setArmor(new ItemStack[4]);

        int highestDungeonScore = Integer.MIN_VALUE;
        setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
        if (skyblockProfile.getInventory() != null) {
            ItemStack highestItem = null;
            for (ItemStack itemStack : skyblockProfile.getInventory()) {
                if (itemStack == null) continue;
                // TODO: D5 stopgap (D125), 1.8.9 read the display.Lore strings
                for (String str : TextUtils.getFormattedLore(itemStack)) {
                    if (TextUtils.stripColor(str).startsWith("Gear")) {
                        int dungeonScore = Integer.parseInt(TextUtils.keepIntegerCharactersOnly(TextUtils.stripColor(str).split(" ")[2]));
                        if (dungeonScore > highestDungeonScore) {
                            highestItem = itemStack;
                            highestDungeonScore = dungeonScore;
                        }
                    }
                }
            }

            setItemSlot(EquipmentSlot.MAINHAND, highestItem == null ? ItemStack.EMPTY : highestItem);
        }
    }

    private final SkinFetcher.SkinSet skinSet;
    private PlayerProfile.Armor armor;
    private final PlayerTeam team = new PlayerTeam(new Scoreboard(), "");

    public FakePlayer(GameProfile playerProfile, SkinFetcher.SkinSet skinSet, PlayerProfile skyblockProfile) {
        super(Minecraft.getInstance().level, playerProfile);
        this.skinSet = skinSet;
        team.setNameTagVisibility(Team.Visibility.NEVER);
        setSkyblockProfile(skyblockProfile);
    }

    private void setArmor(ItemStack[] armorSlots) {
        EquipmentSlot[] slots = {EquipmentSlot.FEET, EquipmentSlot.LEGS, EquipmentSlot.CHEST, EquipmentSlot.HEAD};
        for (int i = 0; i < slots.length; i++) {
            setItemSlot(slots[i], armorSlots[i] == null ? ItemStack.EMPTY : armorSlots[i]);
        }
    }

    @Override
    public PlayerSkin getSkin() {
        if (skinSet == null || skinSet.getSkinLoc() == null) return DefaultPlayerSkin.get(getGameProfile());
        return PlayerSkin.insecure(
                new ClientAsset.DownloadedTexture(skinSet.getSkinLoc(), ""),
                skinSet.getCapeLoc() == null ? null : new ClientAsset.DownloadedTexture(skinSet.getCapeLoc(), ""),
                null,
                "slim".equals(skinSet.getSkinType()) ? PlayerModelType.SLIM : PlayerModelType.WIDE);
    }

    @Override
    public PlayerTeam getTeam() {
        return team;
    }
}
