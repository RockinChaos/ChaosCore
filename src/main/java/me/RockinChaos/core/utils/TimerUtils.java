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
package me.RockinChaos.core.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * A utility class that handles unique id entries for creating timers that expire in a given duration.
 */
@SuppressWarnings({"unused"})
public class TimerUtils {
    private static final Map<String, Map<Object, Long>> timedMap = new HashMap<>();

    /**
     * Sets an object with a unique ID and the current time.
     *
     * @param uniqueId  the unique identifier for the Object
     * @param object    the Object to be stored
     * @param duration  the duration (default is Ticks)
     * @param timeUnit  the TimeUnit of the duration, (optional) will default to Ticks if undefined
     */
    public static void setExpiry(final String uniqueId, final Object object, final int duration, final TimeUnit... timeUnit) {
        final long future = (timeUnit == null || timeUnit.length == 0 ? StringUtils.ticksToMillis(duration) : timeUnit[0].toMillis(duration));
        final long expiryTime = System.currentTimeMillis() + future;
        timedMap.computeIfAbsent(uniqueId, k -> new HashMap<>()).put(object, expiryTime);
    }

    /**
     * Gets the object associated with the unique ID and checks if it's within the allowed duration.
     *
     * @param uniqueId the unique identifier for the object
     * @param object   the object to be retrieved
     * @return true if the object is within the duration, false otherwise
     */
    public static boolean isExpired(final String uniqueId, final Object object) {
        final Map<Object, Long> objectMap = timedMap.get(uniqueId);
        if (objectMap == null) {
            return true;
        }
        final Long expiryTime = objectMap.get(object);
        if (expiryTime == null) {
            return true;
        }
        return expiryTime <= System.currentTimeMillis();
    }

    /**
     * Gets the object associated with the unique ID if it is not expired.
     *
     * @param uniqueId the unique identifier for the object
     * @param object   the object to be retrieved
     * @return the exact Object instance stored if its not expired.
     */
    public static Object getAlive(final String uniqueId, final Object object) {
        if (!isExpired(uniqueId, object)) {
            final Map<Object, Long> objectMap = timedMap.get(uniqueId);
            for (Map.Entry<Object, Long> entry : objectMap.entrySet()) {
                Object storedObject = entry.getKey();
                if (storedObject.equals(object)) {
                    return storedObject;
                }
            }
        }
        return null;
    }

    /**
     * Removes the expired object from the map based on the unique ID.
     *
     * @param uniqueId the unique identifier for the object
     * @param object   the object to be removed
     */
    public static void removeExpiry(final String uniqueId, final Object object) {
        if (isExpired(uniqueId, object)) {
            final Map<Object, Long> objectMap = timedMap.get(uniqueId);
            if (objectMap != null) {
                objectMap.remove(object);
                if (objectMap.isEmpty()) {
                    timedMap.remove(uniqueId);
                }
            }
        }
    }

    /**
     * Clears all expired objects from the map.
     */
    public static void clearExpired() {
        long currentTime = System.currentTimeMillis();
        timedMap.forEach((id, objectMap) -> objectMap.values().removeIf(expiryTime -> expiryTime <= currentTime));
        timedMap.entrySet().removeIf(entry -> entry.getValue().isEmpty());
    }
}