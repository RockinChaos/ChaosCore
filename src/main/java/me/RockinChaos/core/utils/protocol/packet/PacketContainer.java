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

import me.RockinChaos.core.utils.ServerUtils;

import javax.annotation.Nonnull;
import java.lang.reflect.Field;
import java.util.HashMap;

@SuppressWarnings("unused")
public class PacketContainer {

    protected final Object packet;
    final HashMap<Integer, PacketObject> dataFields = new HashMap<>();

    /**
     * Creates a new PacketContainer instance.
     *
     * @param packet - The packet being deciphered.
     */
    public PacketContainer(final @Nonnull Object packet) {
        this.packet = packet;
        int fieldNumber = 0;
        try {
            for (Field field : this.packet.getClass().getDeclaredFields()) {
                field.setAccessible(true);
                dataFields.put(fieldNumber, new PacketObject(field.getName(), field.get(this.packet)));
                fieldNumber++;
            }
        } catch (Exception e) {
            ServerUtils.sendSevereTrace(e);
        }
    }

    /**
     * Gets the data fields of the Packet Object.
     *
     * @return The HashMap of the Fields for the Packet Object.
     */
    public @Nonnull HashMap<Integer, PacketObject> getStrings() {
        return this.dataFields;
    }

    /**
     * Attempts to read a line from the PacketObject.
     *
     * @param readable - The line to be read.
     * @return The found PacketObject of the Data Field.
     */
    public @Nonnull PacketObject read(final int readable) {
        return this.dataFields.get(readable);
    }
}