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
package me.RockinChaos.core.utils.keys;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * A utility class that represents a primary key composed of one object: a primary key.
 * The equality and hash code of the primary key are based on the primary key value.
 * <p>
 * MetaData is not considered in the PrimaryKey.
 */
@SuppressWarnings({"unused"})
public class PrimaryKey {
    private final Object primary;
    private final Map<String, Object> metaData = new HashMap<>();

    /**
     * Constructs a PrimaryKey with the specified primary and secondary objects.
     *
     * @param primary   The primary object that forms the primary key.
     */
    public PrimaryKey(final Object primary) {
        this.primary = primary;
    }

    /**
     * Retrieves the primary component of the primary key.
     *
     * @return The primary object of the primary key.
     */
    public Object getPrimary() {
        return this.primary;
    }

    /**
     * Adds a metadata object with a specified key.
     *
     * @param key   the key for the metadata object.
     * @param value the sub-object to be stored
     * @return The primary key instance.
     */
    public PrimaryKey addMetaData(final String key, final Object value) {
        this.metaData.put(key, value);
        return this;
    }

    /**
     * Retrieves a metadata object by its key.
     *
     * @param key the key of the metadata object
     * @return the metadata object associated with the key, or null if not found
     */
    public Object getMetaData(final String key) {
        return this.metaData.get(key);
    }

    /**
     * Checks if this PrimaryKey is equal to another object.
     * Two PrimaryKey objects are considered equal if their primary components are equal.
     *
     * @param object The object to compare with this PrimaryKey.
     * @return true if the specified object is equal to this PrimaryKey, false otherwise.
     */
    @Override
    public boolean equals(final Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        final PrimaryKey that = (PrimaryKey) object;
        return Objects.equals(this.primary, that.primary);
    }

    /**
     * Computes the hash code for this PrimaryKey based on the hash codes of its primary component.
     * This allows PrimaryKey objects to be used efficiently in hash-based collections like HashMap and HashSet.
     *
     * @return The hash code of this PrimaryKey.
     */
    @Override
    public int hashCode() {
        return Objects.hash(this.primary);
    }
}