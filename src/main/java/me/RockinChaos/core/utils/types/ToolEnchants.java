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

import me.RockinChaos.core.utils.ServerUtils;
import org.bukkit.enchantments.Enchantment;

import javax.annotation.Nonnull;

@SuppressWarnings("unused")
public enum ToolEnchants {
    DIG_SPEED("EFFICIENCY"),
    SILK_TOUCH("SILK_TOUCH"),
    DURABILITY("UNBREAKING"),
    LOOT_BONUS_BLOCKS("FORTUNE"),
    LUCK("LUCK_OF_THE_SEA"),
    LURE("LURE"),
    MENDING("MENDING"),
    VANISHING_CURSE("VANISHING_CURSE"),
    DENSITY("DENSITY"),
    BREACH("BREACH"),
    WIND_BURST("WIND_BURST");

    final String key;

    ToolEnchants(final String key) {
        this.key = key;
    }

    /**
     * Checks if the Enchantment is a Tool Enchant type.
     *
     * @param enchant - The Enchantment being checked.
     * @return If the Enchantment is a Tool Enchant type.
     */
    public static boolean isEnchant(final @Nonnull Enchantment enchant) {
        for (ToolEnchants ench : ToolEnchants.values()) {
            if (ServerUtils.hasPreciseUpdate("1_20_3")) {
                if (enchant.toString().split(":")[1].replace("]", "").equalsIgnoreCase(ench.key())) {
                    return true;
                }
            } else if (enchant.toString().split(", ")[1].replace("]", "").equalsIgnoreCase(ench.name())) {
                return true;
            }
        }
        return false;
    }

    public String key() {
        return this.key;
    }
}