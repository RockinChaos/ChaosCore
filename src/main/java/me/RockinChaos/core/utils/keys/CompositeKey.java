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

import java.util.Objects;

/**
 * A utility class that represents a composite key composed of two objects: a primary key and a secondary key.
 * The equality and hash code of the composite key are based on the combined values of the primary and secondary keys.
 */
@SuppressWarnings({"unused"})
public class CompositeKey {
    private final Object primary;
    private final Object secondary;

    /**
     * Constructs a CompositeKey with the specified primary and secondary objects.
     *
     * @param primary   The primary object that forms the first part of the composite key.
     * @param secondary The secondary object that forms the second part of the composite key.
     */
    public CompositeKey(final Object primary, final Object secondary) {
        this.primary = primary;
        this.secondary = secondary;
    }

    /**
     * Retrieves the primary component of the composite key.
     *
     * @return The primary object of the composite key.
     */
    public Object getPrimary() {
        return this.primary;
    }

    /**
     * Retrieves the secondary component of the composite key.
     *
     * @return The secondary object of the composite key.
     */
    public Object getSecondary() {
        return this.secondary;
    }

    /**
     * Checks if this CompositeKey is equal to another object.
     * Two CompositeKey objects are considered equal if both their primary and secondary components are equal.
     *
     * @param object The object to compare with this CompositeKey.
     * @return true if the specified object is equal to this CompositeKey, false otherwise.
     */
    @Override
    public boolean equals(final Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        final CompositeKey that = (CompositeKey) object;
        return Objects.equals(this.primary, that.primary) &&
                Objects.equals(this.secondary, that.secondary);
    }

    /**
     * Computes the hash code for this CompositeKey based on the hash codes of its primary and secondary components.
     * This allows CompositeKey objects to be used efficiently in hash-based collections like HashMap and HashSet.
     *
     * @return The hash code of this CompositeKey.
     */
    @Override
    public int hashCode() {
        return Objects.hash(this.primary, this.secondary);
    }
}