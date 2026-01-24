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

import me.RockinChaos.core.utils.api.LanguageAPI;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PlaceHolder {
    private final Map<Holder, String> keys = new HashMap<>();

    /**
     * Created a new PlaceHolder instance.
     */
    public PlaceHolder() {
        this.with(Holder.PREFIX, LanguageAPI.getLang().getPrefix());
    }

    /**
     * Adds the Holder and its Key to the mapped Holder keys.
     *
     * @param holder The Holder instance being set.
     * @param key    The key being added to the Holder.
     * @return The PlaceHolder instance.
     */
    public PlaceHolder with(final @Nonnull Holder holder, final @Nonnull String key) {
        if (this.keys.containsKey(holder) && this.keys.get(holder) != null && !this.keys.get(holder).trim().isEmpty()) { return this; }
        this.keys.put(holder, key);
        return this;
    }

    /**
     * Gets the currently mapped Holder keys.
     *
     * @return The mapped Holder keys.
     */
    public Map<Holder, String> keys() {
        return this.keys;
    }

    /**
     * Translates the PlaceHolders into the actual Object keys defined for the Message.
     *
     * @param message - The String being translated
     * @return The translated message with placeholders replaced.
     */
    public @Nonnull String setPlaceholders(final @Nonnull String message) {
        final Map<String, String> holderMap = new HashMap<>();
        for (final Holder holder : Holder.values()) {
            String value = this.keys().get(holder);
            holderMap.put(holder.ph().toLowerCase(), (value != null && !value.trim().isEmpty()) ? value : "&lNULL");
        }

        final Pattern pattern = Pattern.compile("%\\s*(\\w+?)\\s*%", Pattern.CASE_INSENSITIVE);
        final Matcher matcher = pattern.matcher(message);
        final StringBuffer result = new StringBuffer();

        while (matcher.find()) {
            final String ph = holderMap.getOrDefault("%" + matcher.group(1).trim().toLowerCase() + "%", "%" + matcher.group(1) + "%");
            matcher.appendReplacement(result, Matcher.quoteReplacement(ph));
        }
        matcher.appendTail(result);

        return result.toString();
    }

    /**
     * PlaceHolder types.
     */
    public enum Holder {
        PLAYERS("%players%"),
        PLAYER("%player%"),
        PLAYER_UUID("%player_uuid%"),
        PLAYER_KILLS("%player_kills%"),
        PLAYER_DEATHS("%player_deaths%"),
        PLAYER_FOOD("%player_food%"),
        PLAYER_HEALTH("%player_health%"),
        PLAYER_LEVEL("%player_level%"),
        PLAYER_LOCATION("%player_location%"),
        PLAYER_INTERACT("%player_interact%"),
        PLAYER_HIT("%player_hit%"),
        TARGET_PLAYER("%target_player%"),
        OWNER("%owner%"),
        OBJECT("%object%"),
        WORLD("%world%"),
        targetWorld("%target_world%"),
        MOB_KILLS("%mob_kills%"),
        FAIL_COUNT("%fail_count%"),
        AMOUNT("%amount%"),
        ITEM("%item%"),
        ITEM_TYPE("%item_type%"),
        ITEM_SLOT("%item_slot%"),
        ITEM_PERMISSION("%item_permission%"),
        BALANCE("%balance%"),
        COST("%cost%"),
        COMMAND("%command%"),
        PURGE_DATA("%purge_data%"),
        TIME_LEFT("%time_left%"),
        INPUT("%input%"),
        INPUT_EXAMPLE("%input_example%"),
        GAMEMODE("%gamemode%"),
        HOTBAR("%hotbar%"),
        STATE("%state%"),
        ACTION("%action%"),
        PREFIX("%prefix%");
        private final String name;

        Holder(final @Nonnull String name) {
            this.name = name;
        }

        /**
         * Gets the Holder String value to be replaced.
         *
         * @return The Holder String value.
         */
        public @Nonnull String ph() {
            return name;
        }
    }
}