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

import javax.annotation.Nonnull;

@SuppressWarnings("unused")
public enum Clear {
    GENERAL("/clear"),
    ESSENTIALS("/eclear"),
    CMI("/cmi clear");

    private final String command;

    Clear(final String command) {
        this.command = command;
    }

    /**
     * Checks if the message was a clear inventory command.
     *
     * @param command - The command being checked.
     * @return If the message was a clear command.
     */
    public static boolean isClear(final @Nonnull String command) {
        for (Clear clear : Clear.values()) {
            if (command.toUpperCase().startsWith(clear.command.toUpperCase())) {
                return true;
            }
        }
        return false;
    }
}