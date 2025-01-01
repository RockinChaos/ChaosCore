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

import me.RockinChaos.core.utils.StringUtils;
import org.bukkit.Material;

import javax.annotation.Nonnull;

@SuppressWarnings("unused")
public enum ActionBlocks {
    FURNACE,
    CHEST,
    BEACON,
    DISPENSER,
    DROPPER,
    HOPPER,
    WORKBENCH,
    ANVIL,
    BED,
    FENCE_GATE,
    DOOR,
    BUTTON,
    TRAP_DOOR,
    DIODE,
    COMPARATOR,
    BREWING_STAND,
    CAULDRON,
    SIGN,
    LEVER,
    SHULKER_BOX,
    DETECTOR,
    BARREL,
    BLAST_FURNACE,
    SMOKER,
    TABLE,
    COMPOSTER,
    GRINDSTONE,
    LECTERN,
    LOOM,
    STONECUTTER,
    BELL;

    enum BlockedBlocks {
        IRON;
    }

    /**
     * Checks if the Material is an Actionable Block type.
     *
     * @param material - The Material being checked.
     * @return If the Material is an Actionable Block type.
     */
    public static boolean isActionBlocks(final @Nonnull Material material) {
        if (!material.isBlock()) {
            return false;
        }
        for (ActionBlocks tag : ActionBlocks.values()) {
            if (StringUtils.containsIgnoreCase(material.name(), tag.name())) {
                boolean allowed = true;
                for (BlockedBlocks blocked : BlockedBlocks.values()) {
                    if (StringUtils.containsIgnoreCase(material.name(), blocked.name())) {
                        allowed = false;
                    }
                }
                return allowed;
            }
        }
        return false;
    }
}