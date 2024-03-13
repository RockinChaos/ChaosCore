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

import me.RockinChaos.core.Core;
import me.RockinChaos.core.handlers.ItemHandler;
import me.RockinChaos.core.handlers.PlayerHandler;
import me.RockinChaos.core.utils.api.LegacyAPI;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.*;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.RegisteredListener;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SuppressWarnings("unused")
public class StringUtils {

    /**
     * Checks if the condition is met.
     *
     * @param condition - The condition to be compared against the value.
     * @param operand   - The operation to be performed when comparing the condition to the value.
     * @param value     - The value being compared against the condition.
     * @return If the condition has operand against value.
     */
    public static boolean conditionMet(final @Nullable String condition, final @Nullable String operand, final @Nullable String value) {
        if (operand == null) {
            return false;
        }
        if (operand.equalsIgnoreCase("EQUAL") && ((condition != null && condition.equalsIgnoreCase(value)) || (condition == null && value == null))) {
            return true;
        } else if (operand.equalsIgnoreCase("NOTEQUAL") && ((condition != null && value != null && !condition.equalsIgnoreCase(value)) || ((condition == null && value != null) || (condition != null && value == null)))) {
            return true;
        } else if (operand.equalsIgnoreCase("OVER") && isInt(condition) && isInt(value) && Integer.parseInt(condition) > Integer.parseInt(value)) {
            return true;
        } else
            return operand.equalsIgnoreCase("UNDER") && isInt(condition) && isInt(value) && Integer.parseInt(condition) < Integer.parseInt(value);
    }

    /**
     * Checks if string1 contains string2.
     *
     * @param string1 - The String to be checked if it contains string2.
     * @param string2 - The String that should be inside string1.
     * @return If string1 contains string2.
     */
    public static boolean containsIgnoreCase(final @Nullable String string1, final @Nullable String string2) {
        return string1 != null && string2 != null && string1.toLowerCase().contains(string2.toLowerCase());
    }

    /**
     * Checks if string1 contains string2.
     *
     * @param string1  - The String to be checked if it contains string2.
     * @param string2  - The String that should be inside string1.
     * @param argument - The argument to be split between the string.
     * @return If string1 contains string2.
     */
    public static boolean splitIgnoreCase(final @Nullable String string1, final @Nullable String string2, final @Nullable String argument) {
        if (string1 == null || string2 == null || argument == null) {
            return false;
        }
        String[] parts = string1.split(argument);
        boolean splitParts = string1.contains(argument);
        for (int i = 0; i < (splitParts ? parts.length : 1); i++) {
            if ((splitParts && parts[i] != null && parts[i].toLowerCase().replace(" ", "").equalsIgnoreCase(string2.replace(" ", "").toLowerCase()))
                    || (!splitParts && string1.toLowerCase().replace(" ", "").equalsIgnoreCase(string2.toLowerCase().replace(" ", "")))) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if the List contains the String.
     *
     * @param list - The List to be checked if it contains the String.
     * @param str  - The String that should be inside the List.
     * @return If the List contained the String.
     */
    public static boolean containsValue(final @Nonnull List<?> list, final @Nonnull String str) {
        boolean bool = false;
        for (Object l : list) {
            if (l.toString().equalsIgnoreCase(str)) {
                bool = true;
                break;
            }
        }
        return bool;
    }

    /**
     * Splits the String with proper formatting and ordering.
     *
     * @param str - The String to be Split.
     * @return The newly formatted String[].
     */
    public static @Nonnull String[] softSplit(final @Nonnull String str) {
        if (str.split(", ").length < 3) {
            return str.split("` ");
        }
        StringBuilder splitTest = new StringBuilder();
        int index = 1;
        for (String sd : str.split(", ")) {
            if (index == 3) {
                splitTest.append(sd).append("` ");
                index = 1;
            } else {
                splitTest.append(sd).append(", ");
                index++;
            }
        }
        if (splitTest.toString().endsWith(", ")) {
            splitTest = new StringBuilder(splitTest.substring(0, splitTest.length() - 2));
        }
        return splitTest.toString().split("` ");
    }

    /**
     * Splits the String to a List.
     *
     * @param str - The String to be Split.
     * @return The split String as a List.
     */
    public static @Nonnull List<String> split(final @Nonnull String str) {
        return new ArrayList<>(Arrays.asList(str.split(", ")));
    }

    /**
     * Checks if the StringBuilder is Empty.
     *
     * @param stringBuilder - The StringBuilder being checked.
     * @return If the StringBuilder is Empty.
     */
    public static boolean isEmpty(final @Nonnull StringBuilder stringBuilder) {
        return stringBuilder.toString().isEmpty();
    }

    /**
     * Converts a Location to a readable String value.
     *
     * @param location - The location to become a String.
     * @return The Location as a String value.
     */
    public static @Nonnull String locationToString(final @Nonnull Location location) {
        return location.getX() + "," + location.getY() + "," + location.getZ() + "," + location.getYaw() + "," + location.getPitch();
    }

    /**
     * Replaces the last occurrence of the regex.
     *
     * @param str  - The String to be checked.
     * @param sub1 - The occurrence to be removed.
     * @param sub2 - The occurrence to be placed.
     * @return The newly replaced String.
     */
    public static @Nonnull String replaceLast(final @Nonnull String str, final @Nonnull String sub1, final @Nonnull String sub2) {
        int pos = str.lastIndexOf(sub1);
        if (pos > -1) {
            return str.substring(0, pos) + sub2 + str.substring(pos + sub1.length());
        } else {
            return str;
        }
    }

    /**
     * Color Encodes a String so that it is completely hidden in color codes,
     * this will be invisible to a normal eye and will not display any text.
     *
     * @param itemStack - The ItemStack being modified.
     * @param endData   - The String to be Color Encoded.
     * @return The Color Encoded String.
     */
    public static @Nonnull ItemStack colorEncode(final @Nonnull ItemStack itemStack, final @Nonnull String endData) {
        final ItemMeta itemMeta = itemStack.getItemMeta();
        if (ServerUtils.hasSpecificUpdate("1_14") && itemMeta != null) {
            final org.bukkit.NamespacedKey key = new org.bukkit.NamespacedKey(Core.getCore().getPlugin(), "Item_Data");
            itemMeta.getPersistentDataContainer().set(key, org.bukkit.persistence.PersistentDataType.STRING, endData);
            itemStack.setItemMeta(itemMeta);
            return itemStack;
        } else if (itemMeta != null) {
            final String encodedData = LegacyAPI.colorEncode(Core.getCore().getPlugin().getName() + endData);
            itemMeta.setDisplayName(encodedData);
            itemStack.setItemMeta(itemMeta);
            return itemStack;
        }
        return itemStack;
    }

    /**
     * Decodes a Color Encoded String.
     *
     * @param itemStack - The ItemStack being Decoded.
     * @return The Color Decoded String.
     */
    public static @Nonnull String colorDecode(final @Nonnull ItemStack itemStack) {
        final ItemMeta itemMeta = itemStack.getItemMeta();
        if (ServerUtils.hasSpecificUpdate("1_14") && itemMeta != null) {
            final org.bukkit.NamespacedKey key = new org.bukkit.NamespacedKey(Core.getCore().getPlugin(), "Item_Data");
            final org.bukkit.persistence.PersistentDataContainer container = itemMeta.getPersistentDataContainer();
            if (container.has(key, org.bukkit.persistence.PersistentDataType.STRING)) {
                final String decoded = container.get(key, org.bukkit.persistence.PersistentDataType.STRING);
                if (decoded != null) {
                    return decoded;
                } else {
                    return "";
                }
            }
        } else if (itemMeta != null && itemMeta.hasDisplayName()) {
            return LegacyAPI.colorDecode(itemMeta.getDisplayName());
        }
        return "";
    }

    /**
     * Gets the Color from the provided HexColor.
     *
     * @param hexString - The HexColor to be converted to Color.
     * @return The Color found from the HexColor.
     */
    public static @Nonnull Color getColorFromHexColor(final @Nonnull String hexString) {
        int hex = Integer.decode("#" + hexString.replace("#", ""));
        int r = (hex & 0xFF0000) >> 16;
        int g = (hex & 0xFF00) >> 8;
        int b = (hex & 0xFF);
        return Color.fromRGB(r, g, b);
    }

    /**
     * Gets the number of characters in the String.
     *
     * @param str - The String to be checked.
     * @return The number of characters in the String.
     */
    public static int countCharacters(final @Nullable String str) {
        int count = 0;
        if (str == null) {
            return count;
        }
        for (int i = 0; i < str.length(); i++) {
            if (Character.isLetter(str.charAt(i)))
                count++;
        }
        return count;
    }

    /**
     * Checks if the specified String is an Integer Value.
     *
     * @param str - The String to be checked.
     * @return If the String is an Integer Value.
     */
    public static boolean isInt(final @Nullable String str) {
        if (str == null) {
            return false;
        }
        try {
            Integer.parseInt(str);
        } catch (NumberFormatException e) {
            return false;
        }
        return true;
    }

    /**
     * Checks if the specified String is a Double Value.
     *
     * @param str - The String to be checked.
     * @return If the String is a Double Value.
     */
    public static boolean isDouble(final @Nullable String str) {
        if (str == null) {
            return false;
        }
        try {
            Double.parseDouble(str);
        } catch (NumberFormatException e) {
            return false;
        }
        return true;
    }

    /**
     * Gets the first found Integer from the specified String.
     *
     * @param str - The String to be checked.
     * @return The first found Integer.
     */
    public static @Nullable Integer returnInteger(final @Nullable String str) {
        if (str == null) {
            return null;
        } else {
            char[] characters = str.toCharArray();
            Integer value = null;
            boolean isPrevDigit = false;
            for (char character : characters) {
                if (!isPrevDigit) {
                    if (Character.isDigit(character)) {
                        isPrevDigit = true;
                        value = Character.getNumericValue(character);
                    }
                } else {
                    if (Character.isDigit(character)) {
                        value = (value * 10) + Character.getNumericValue(character);
                    } else {
                        break;
                    }
                }
            }
            return value;
        }
    }

    /**
     * Gives all custom items to the specified player.
     *
     * @param uuidString - that will receive the items.
     */
    public static @Nullable UUID UUIDConversion(@Nonnull String uuidString) {
        UUID uuid = null;
        if (!uuidString.isEmpty()) {
            uuidString = uuidString.replace("-", "");
            uuid = new UUID(
                    new BigInteger(uuidString.substring(0, 16), 16).longValue(),
                    new BigInteger(uuidString.substring(16), 16).longValue());
        }
        return uuid;
    }

    /**
     * Converts a BufferedReader to a String output.
     *
     * @param reader - the BufferedReader to be converted.
     * @return The resulting appended String.
     */
    public static @Nonnull String toString(final @Nonnull BufferedReader reader) throws IOException {
        final StringBuilder result = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            result.append(line);
        }
        return result.toString();
    }

    /**
     * Encodes the Texture String to Base64.
     *
     * @param url - The texture url to have the Texture fetched.
     * @return The Texture Base64 encoded String.
     */
    public static @Nonnull String toTexture64(@Nonnull String url) {
        url = ItemHandler.cutDelay(url.replace("url-", ""));
        if (!StringUtils.containsIgnoreCase(url, "minecraft.net") && !StringUtils.containsIgnoreCase(url, "http") && !StringUtils.containsIgnoreCase(url, "https")) {
            url = "https://textures.minecraft.net/texture/" + url;
        }
        URI actualUrl = null;
        try {
            actualUrl = new URI(url);
        } catch (URISyntaxException e) {
            ServerUtils.logSevere("{StringUtils} Unable to reach the url " + url + " for a skull-texture.");
            ServerUtils.logSevere("{StringUtils} " + e.getReason());
        }
        if (actualUrl != null) {
            url = "{\"textures\":{\"SKIN\":{\"url\":\"" + actualUrl + "\"}}}";
        }
        return Base64.getEncoder().encodeToString(url.getBytes());
    }

    /**
     * Decodes the Texture Base64 to a URI.
     *
     * @param texture - The texture to have the Texture URL fetched.
     * @return The Texture URI decoded.
     */
    public static URI toTextureURI(@Nonnull String texture) {
        try {
            if (!StringUtils.containsIgnoreCase(texture, "minecraft.net") && !StringUtils.containsIgnoreCase(texture, "http") && !StringUtils.containsIgnoreCase(texture, "https")) {
                String decoded = new String(Base64.getDecoder().decode(texture));
                final JSONObject textureObject = (JSONObject) JSONValue.parseWithException(decoded);
                return new URI(((JSONObject)((JSONObject)textureObject.get("textures")).get("SKIN")).get("url").toString());
            } else {
                return new URI(texture);
            }
        } catch (Exception e) { ServerUtils.sendSevereTrace(e); }
        return null;
    }

    /**
     * Encodes the Player UUID as a texture String.
     *
     * @param player       - The Player to be referenced.
     * @param configName   - The node name of the item.
     * @param skullTexture - The skullTexture being fetched.
     * @return The Player UUID texture String.
     */
    public static @Nonnull String toTextureUUID(final @Nonnull Player player, final @Nonnull String configName, final @Nonnull String skullTexture) {
        if (StringUtils.containsIgnoreCase(skullTexture, "uuid")) {
            String https = ("https://sessionserver.mojang.com/session/minecraft/profile/" + StringUtils.translateLayout(skullTexture, player).replace("uuid-", "").replace("uuid", ""));
            try {
                final URL url = new URL(https);
                final BufferedReader read = new BufferedReader(new InputStreamReader(url.openStream()));
                final StringBuffer stringBuilder = new StringBuffer();
                String valueResult;
                while ((valueResult = read.readLine()) != null) {
                    if (StringUtils.containsIgnoreCase(valueResult, "value")) {
                        stringBuilder.append(valueResult);
                    }
                }
                {
                    Matcher m = Pattern.compile("\"([^\"]*)\"").matcher(stringBuilder);
                    while (m.find()) {
                        final String stringValue = m.group(1);
                        if (!StringUtils.containsIgnoreCase(stringValue, "value")) {
                            valueResult = stringValue;
                        }
                    }
                }
                return (valueResult != null ? valueResult : skullTexture.replace("uuid-", "").replace("uuid", ""));
            } catch (IOException e) {
                ServerUtils.logSevere("{StringUtils} Unable to connect to " + https);
                ServerUtils.logSevere("{StringUtils} " + e.getMessage());
                ServerUtils.logSevere("{StringUtils} The item " + configName + " will NOT have its skull-texture set!");
            }
        }
        return skullTexture.replace("uuid-", "").replace("uuid", "");
    }

    /**
     * Encrypts the String to Base64.
     *
     * @param str - The String to be encrypted.
     * @return The Base64 encoded String.
     */
    public static @Nonnull String encrypt(final @Nonnull String str) {
        return Base64.getEncoder().encodeToString(str.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Decrypts the encoded Base64 String.
     *
     * @param str - The String to be decrypted.
     * @return The decrypted String.
     */
    public static @Nonnull String decrypt(final @Nonnull String str) {
        return new String(Base64.getDecoder().decode(str), StandardCharsets.UTF_8);
    }

    /**
     * Gets a random number of spaces between the upper and lower limit while excluding the comparator.
     *
     * @param lower      - The lowest number of spaces.
     * @param upper      - The highest number of spaces.
     * @param comparator - A number that should never be used.
     */
    public static @Nonnull Map<StringBuilder, Integer> getSpacers(final int lower, final int upper, final int comparator) {
        StringBuilder spaces = new StringBuilder();
        int sNum = StringUtils.getRandom(0, 5);
        while (comparator == sNum) {
            sNum = StringUtils.getRandom(0, 5);
        }
        for (int s = 0; s <= sNum; s++) {
            spaces.append(" ");
        }
        return Collections.singletonMap(spaces, sNum);
    }

    /**
     * Gets a random Integer between the upper and lower limits.
     *
     * @param lower - The lower limit.
     * @param upper - The upper limit.
     * @return The randomly selected Integer between the limits.
     */
    public static int getRandom(final int lower, final int upper) {
        return new Random().nextInt((upper - lower) + 1) + lower;
    }

    /**
     * Randomly selects an Entry from a ArrayList.
     *
     * @param list - The ArrayList to have an entry selected.
     * @return The randomly selected entry.
     */
    public static @Nonnull Object randomEntry(final ArrayList<?> list) {
        Random rand = new Random();
        return list.get(rand.nextInt(list.size()));
    }

    /**
     * Checks if the String contains the location.
     *
     * @param location - The location that the String should contain.
     * @param str      - The String to be checked.
     * @return If the String contains the location.
     */
    public static boolean containsLocation(final @Nonnull String location, final @Nonnull String str) {
        if (str.equalsIgnoreCase("ALL") || str.equalsIgnoreCase("GLOBAL")
                || str.equalsIgnoreCase("ENABLED") || str.equalsIgnoreCase("TRUE")) {
            return true;
        } else {
            for (String eventLoc : str.split(",")) {
                if (eventLoc.equalsIgnoreCase(location)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Gets the Crafting Slot ID Value.
     *
     * @param str - The String to be checked.
     * @return The Crafting Slot Value.
     */
    public static int getSlotConversion(final @Nonnull String str) {
        if (str.equalsIgnoreCase("CRAFTING[0]") || str.equalsIgnoreCase("C[0]")
                || str.equalsIgnoreCase("CRAFTING(0)") || str.equalsIgnoreCase("C(0)")) {
            return 0;
        } else if (str.equalsIgnoreCase("CRAFTING[1]") || str.equalsIgnoreCase("C[1]")
                || str.equalsIgnoreCase("CRAFTING(1)") || str.equalsIgnoreCase("C(1)")) {
            return 1;
        } else if (str.equalsIgnoreCase("CRAFTING[2]") || str.equalsIgnoreCase("C[2]")
                || str.equalsIgnoreCase("CRAFTING(2)") || str.equalsIgnoreCase("C(2)")) {
            return 2;
        } else if (str.equalsIgnoreCase("CRAFTING[3]") || str.equalsIgnoreCase("C[3]")
                || str.equalsIgnoreCase("CRAFTING(3)") || str.equalsIgnoreCase("C(3)")) {
            return 3;
        } else if (str.equalsIgnoreCase("CRAFTING[4]") || str.equalsIgnoreCase("C[4]")
                || str.equalsIgnoreCase("CRAFTING(4)") || str.equalsIgnoreCase("C(4)")) {
            return 4;
        }
        return -1;
    }

    /**
     * Gets the Armor Slot ID.
     *
     * @param slot    - The slot to be checked.
     * @param integer - If the return value should be a String or Integer value.
     * @return The Armor Slot ID.
     */
    public static @Nonnull String getArmorSlot(final @Nonnull String slot, final boolean integer) {
        if (!integer) {
            if (slot.equalsIgnoreCase("39")) {
                return "HELMET";
            } else if (slot.equalsIgnoreCase("38")) {
                return "CHESTPLATE";
            } else if (slot.equalsIgnoreCase("37")) {
                return "LEGGINGS";
            } else if (slot.equalsIgnoreCase("36")) {
                return "BOOTS";
            }
        } else {
            if (slot.equalsIgnoreCase("HELMET") || slot.equalsIgnoreCase("HEAD")) {
                return "39";
            } else if (slot.equalsIgnoreCase("CHESTPLATE")) {
                return "38";
            } else if (slot.equalsIgnoreCase("LEGGINGS")) {
                return "37";
            } else if (slot.equalsIgnoreCase("BOOTS")) {
                return "36";
            }
        }
        return slot;
    }

    /**
     * Checks if the specified Listener is Registered.
     *
     * @param listener - The name of the Listener to be checked.
     * @return If the Listener is Registered.
     */
    public static boolean isRegistered(final @Nonnull String listener) {
        boolean returnValue = false;
        ArrayList<RegisteredListener> rls = HandlerList.getRegisteredListeners(Core.getCore().getPlugin());
        for (RegisteredListener rl : rls) {
            if (rl.getListener().getClass().getSimpleName().equalsIgnoreCase(listener)) {
                returnValue = true;
                break;
            }
        }
        return !returnValue;
    }

    /**
     * Checks if the input is NULL, and returns either NONE for NULL / EMPTY,
     * or the properly formatted list as a String.
     *
     * @param input - The String to be checked.
     * @return The newly formatted String.
     */
    public static @Nonnull String nullCheck(@Nullable String input) {
        if (input == null || input.equalsIgnoreCase("NULL") || input.equalsIgnoreCase("NULL&7") || input.contains("[]") || input.contains("{}") || input.equals("0&7") || input.equals("-1&a%") || input.isEmpty() || input.equals(" ")) {
            return "NONE";
        }
        if (input.startsWith("[") && input.endsWith("]")) {
            input = input.substring(0, input.length() - 1).substring(1);
        }
        if (input.startsWith("{") && input.endsWith("}")) {
            input = input.replace("{", "").replace("}", "").replace("=", ":");
        }
        return input;
    }

    /**
     * Translated the specified String by formatting its hex color codes.
     *
     * @param str - The String to have its Color Codes properly Converted to Mojang Hex Colors.
     * @return The translated string.
     */
    public static @Nonnull String translateHexColorCodes(final @Nonnull String str) {
        final char COLOR_CHAR = ChatColor.COLOR_CHAR;
        Matcher matcher = Pattern.compile("&#([A-Fa-f0-9]{6})").matcher(str);
        StringBuffer buffer = new StringBuffer(str.length() + 4 * 8);
        while (matcher.find()) {
            String group = matcher.group(1);
            matcher.appendReplacement(buffer, COLOR_CHAR + "x" + COLOR_CHAR + group.charAt(0) + COLOR_CHAR + group.charAt(1) + COLOR_CHAR + group.charAt(2) + COLOR_CHAR + group.charAt(3) + COLOR_CHAR + group.charAt(4) + COLOR_CHAR + group.charAt(5));
        }
        return matcher.appendTail(buffer).toString();
    }

    /**
     * Formats any color codes found in the String to their string format so the
     * text will be restored.
     *
     * @param str - The String to have its Color Codes properly Converted to String.
     * @return The newly formatted String.
     */
    public static @Nonnull String restoreColor(final @Nonnull String str) {
        return str.replace(ChatColor.COLOR_CHAR, '&');
    }

    /**
     * Formats any color codes found in the String to Bukkit Colors so the
     * text will be colorfully formatted.
     *
     * @param str - The String to have its Color Codes properly Converted to Bukkit Colors.
     * @return The newly formatted String.
     */
    public static @Nonnull String colorFormat(final @Nonnull String str) {
        return ChatColor.translateAlternateColorCodes('&', translateHexColorCodes(str));
    }

    /**
     * Translates the specified String by formatting its color codes and replacing placeholders.
     *
     * @param str         - The String being translated.
     * @param player      - The Player having their String translated.
     * @param placeHolder - The placeholders to be replaced into the String.
     * @return The newly translated String.
     */
    public static @Nonnull String translateLayout(@Nullable String str, final @Nullable Player player, final @Nonnull String... placeHolder) {
        if (str != null && !str.isEmpty()) {
            String playerName = (player == null ? null : PlayerHandler.getPlayerName(player));
            if (playerName == null || playerName.isEmpty()) {
                playerName = "EXEMPT";
            }
            if (player != null && !(player instanceof ConsoleCommandSender)) {
                try {
                    str = str.replace("%player%", playerName);
                } catch (Exception e) {
                    ServerUtils.sendDebugTrace(e);
                }
                try {
                    str = str.replace("%player_uuid%", player.getUniqueId().toString());
                } catch (Exception e) {
                    ServerUtils.sendDebugTrace(e);
                }
                try {
                    str = str.replace("%mob_kills%", String.valueOf(player.getStatistic(Statistic.MOB_KILLS)));
                } catch (Exception e) {
                    ServerUtils.sendDebugTrace(e);
                }
                try {
                    str = str.replace("%player_kills%", String.valueOf(player.getStatistic(Statistic.PLAYER_KILLS)));
                } catch (Exception e) {
                    ServerUtils.sendDebugTrace(e);
                }
                try {
                    str = str.replace("%player_deaths%", String.valueOf(player.getStatistic(Statistic.DEATHS)));
                } catch (Exception e) {
                    ServerUtils.sendDebugTrace(e);
                }
                try {
                    str = str.replace("%player_food%", String.valueOf(player.getFoodLevel()));
                } catch (Exception e) {
                    ServerUtils.sendDebugTrace(e);
                }
                try {
                    str = str.replace("%player_level%", String.valueOf(player.getLevel()));
                } catch (Exception e) {
                    ServerUtils.sendDebugTrace(e);
                }
                try {
                    final double health = player.getHealth();
                    str = str.replace("%player_health%", String.valueOf(health));
                } catch (Exception e) {
                    ServerUtils.sendDebugTrace(e);
                }
                try {
                    str = str.replace("%player_location%", player.getLocation().getBlockX() + ", " + player.getLocation().getBlockY() + ", " + player.getLocation().getBlockZ());
                } catch (Exception e) {
                    ServerUtils.sendDebugTrace(e);
                }
                try {
                    if (placeHolder.length >= 1 && placeHolder[0] != null) {
                        str = str.replace("%player_hit%", placeHolder[0]);
                    }
                } catch (Exception e) {
                    ServerUtils.sendDebugTrace(e);
                }
                try {
                    if (Bukkit.isPrimaryThread()) {
                        str = str.replace("%player_interact%", PlayerHandler.getNearbyPlayer(player, 3));
                    }
                } catch (Exception e) {
                    ServerUtils.sendDebugTrace(e);
                }
            }
            if (player == null) {
                try {
                    str = str.replace("%player%", "CONSOLE");
                } catch (Exception e) {
                    ServerUtils.sendDebugTrace(e);
                }
            }
            if (Core.getCore().getDependencies().placeHolderEnabled()) {
                try {
                    try {
                        str = PlaceholderAPI.setPlaceholders(player, str);
                    } catch (NoSuchFieldError e) {
                        ServerUtils.logWarn("An error has occurred when setting the Placeholder " + e.getMessage() + ", if this issue persists contact the developer of PlaceholderAPI.");
                    }
                } catch (Exception ignored) {
                }
            }
            return ChatColor.translateAlternateColorCodes('&', translateHexColorCodes(str));
        }
        return (str == null ? "" : str);
    }
}