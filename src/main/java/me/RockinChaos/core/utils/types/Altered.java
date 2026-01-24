/*
 * ChaosCore
 * Copyright (C) CraftationGaming <https://www.craftationgaming.com/>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package me.RockinChaos.core.utils.types;

import me.RockinChaos.core.handlers.ItemHandler;
import org.bukkit.Material;

import javax.annotation.Nonnull;

@SuppressWarnings("unused")
public enum Altered {
    // Plants
    BAMBOO_SAPLING("BAMBOO"),
    KELP_PLANT("KELP"),
    TALL_SEAGRASS("SEAGRASS"),
    SWEET_BERRY_BUSH("SWEET_BERRIES"),

    // Coral fans
    FIRE_CORAL_WALL_FAN("FIRE_CORAL_FAN"),
    BUBBLE_CORAL_WALL_FAN("BUBBLE_CORAL_FAN"),
    BRAIN_CORAL_WALL_FAN("BRAIN_CORAL_FAN"),
    TUBE_CORAL_WALL_FAN("TUBE_CORAL_FAN"),
    HORN_CORAL_WALL_FAN("HORN_CORAL_FAN"),
    DEAD_TUBE_CORAL_WALL_FAN("DEAD_TUBE_CORAL_FAN"),
    DEAD_BRAIN_CORAL_WALL_FAN("DEAD_BRAIN_CORAL_FAN"),
    DEAD_BUBBLE_CORAL_WALL_FAN("DEAD_BUBBLE_CORAL_FAN"),
    DEAD_FIRE_CORAL_WALL_FAN("DEAD_FIRE_CORAL_FAN"),
    DEAD_HORN_CORAL_WALL_FAN("DEAD_HORN_CORAL_FAN"),

    // Wall signs
    OAK_WALL_SIGN("OAK_SIGN"),
    SPRUCE_WALL_SIGN("SPRUCE_SIGN"),
    BIRCH_WALL_SIGN("BIRCH_SIGN"),
    JUNGLE_WALL_SIGN("JUNGLE_SIGN"),
    ACACIA_WALL_SIGN("ACACIA_SIGN"),
    DARK_OAK_WALL_SIGN("DARK_OAK_SIGN"),
    CRIMSON_WALL_SIGN("CRIMSON_SIGN"),
    WARPED_WALL_SIGN("WARPED_SIGN"),
    MANGROVE_WALL_SIGN("MANGROVE_SIGN"),
    CHERRY_WALL_SIGN("CHERRY_SIGN"),
    BAMBOO_WALL_SIGN("BAMBOO_SIGN"),

    // Crops
    CROPS("WHEAT"),
    POTATOES("POTATO"),
    CARROTS("CARROT"),
    BEETROOTS("BEETROOT"),
    COCOA("COCOA_BEANS"),

    // Torches
    WALL_TORCH("TORCH"),
    SOUL_WALL_TORCH("SOUL_TORCH"),
    REDSTONE_WALL_TORCH("REDSTONE_TORCH"),

    // Banners
    WHITE_WALL_BANNER("WHITE_BANNER"),
    ORANGE_WALL_BANNER("ORANGE_BANNER"),
    MAGENTA_WALL_BANNER("MAGENTA_BANNER"),
    LIGHT_BLUE_WALL_BANNER("LIGHT_BLUE_BANNER"),
    YELLOW_WALL_BANNER("YELLOW_BANNER"),
    LIME_WALL_BANNER("LIME_BANNER"),
    PINK_WALL_BANNER("PINK_BANNER"),
    GRAY_WALL_BANNER("GRAY_BANNER"),
    LIGHT_GRAY_WALL_BANNER("LIGHT_GRAY_BANNER"),
    CYAN_WALL_BANNER("CYAN_BANNER"),
    PURPLE_WALL_BANNER("PURPLE_BANNER"),
    BLUE_WALL_BANNER("BLUE_BANNER"),
    BROWN_WALL_BANNER("BROWN_BANNER"),
    GREEN_WALL_BANNER("GREEN_BANNER"),
    RED_WALL_BANNER("RED_BANNER"),
    BLACK_WALL_BANNER("BLACK_BANNER"),

    // Skulls/Heads
    SKELETON_WALL_SKULL("SKELETON_SKULL"),
    WITHER_SKELETON_WALL_SKULL("WITHER_SKELETON_SKULL"),
    ZOMBIE_WALL_HEAD("ZOMBIE_HEAD"),
    PLAYER_WALL_HEAD("PLAYER_HEAD"),
    CREEPER_WALL_HEAD("CREEPER_HEAD"),
    DRAGON_WALL_HEAD("DRAGON_HEAD"),
    PIGLIN_WALL_HEAD("PIGLIN_HEAD"),

    // Redstone components
    REDSTONE_WIRE("REDSTONE"),
    TRIPWIRE("STRING"),

    // Misc
    FROSTED_ICE("ICE"),
    PISTON_HEAD("PISTON"),
    MOVING_PISTON("PISTON"),
    PISTON_EXTENSION("PISTON"),
    ATTACHED_MELON_STEM("MELON_SEEDS"),
    ATTACHED_PUMPKIN_STEM("PUMPKIN_SEEDS"),
    MELON_STEM("MELON_SEEDS"),
    PUMPKIN_STEM("PUMPKIN_SEEDS"),
    NETHER_PORTAL("OBSIDIAN"),
    BUBBLE_COLUMN("WATER"),
    LAVA("LAVA_BUCKET"),
    WATER("WATER_BUCKET"),
    POWDER_SNOW("POWDER_SNOW_BUCKET"),
    FIRE("FLINT_AND_STEEL"),
    SOUL_FIRE("FLINT_AND_STEEL");

    private final String fixedType;

    Altered(final String fixedType) {
        this.fixedType = fixedType;
    }

    /**
     * Checks if the Material is an Altered type.
     *
     * @param material - The Material being checked.
     * @return The correct Material if Altered.
     */
    public static @Nonnull Material getAlter(final @Nonnull Material material) {
        for (Altered tag : Altered.values()) {
            if (tag.name().equalsIgnoreCase(material.name())) {
                return ItemHandler.getMaterial(tag.fixedType, null);
            }
        }
        return material;
    }
}