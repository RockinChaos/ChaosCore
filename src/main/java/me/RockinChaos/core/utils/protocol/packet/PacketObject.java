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
package me.RockinChaos.core.utils.protocol.packet;

import javax.annotation.Nonnull;

@SuppressWarnings("unused")
public class PacketObject {

    private final String field;
    private final Object data;

    /**
     * Creates a new PacketObject instance.
     *
     * @param field - The field of the Packet.
     * @param data  - The data of the Packet.
     */
    public PacketObject(final @Nonnull String field, final @Nonnull Object data) {
        this.field = field;
        this.data = data;
    }

    /**
     * Gets the Packet Field.
     *
     * @return The Packet Field.
     */
    public @Nonnull String getField() {
        return this.field;
    }

    /**
     * Gets the Packet Object.
     *
     * @return The Packets Data Object.
     */
    public @Nonnull Object getData() {
        return this.data;
    }
}