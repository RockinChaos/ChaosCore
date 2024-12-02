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

import com.google.common.collect.ImmutableList;
import me.RockinChaos.core.utils.api.LegacyAPI;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.banner.Pattern;
import org.bukkit.block.banner.PatternType;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import javax.annotation.Nonnull;
import java.lang.reflect.Method;
import java.util.List;
import java.util.function.Supplier;

/**
 * Welcome to the magical land of hopes and dreams.
 * These methods exist to bridge the gap between newer and older Minecraft versions
 * with the goal to be able to compile using the latest Minecraft version.
 */
@SuppressWarnings({"unchecked", "ConstantExpression", "ConstantValue"})
public class CompatUtils {

    /**
     * Attempts to get the Open Inventory of the Player.
     * In API versions 1.21 (and above), InventoryView is an interface.
     * In versions 1.20.6 and below, InventoryView is a class.
     *
     * @param object The object being referenced, this is a Player object.
     * @return The open inventory view from the object, this is an InventoryView so only an object will be returned.
     */
    public static @Nonnull Object getOpenInventory(final @Nonnull Object object) {
        try {
            final Method getOpenInventory = object.getClass().getMethod("getOpenInventory");
            getOpenInventory.setAccessible(true);
            return getOpenInventory.invoke(object);
        } catch (Exception e) {
            ServerUtils.logSevere("{CompatUtils} An error has occurred with Player#getOpenInventory!");
            ServerUtils.sendSevereTrace(e);
            throw new RuntimeException(object.getClass().getName(), e);
        }
    }

    /**
     * Attempts to get the Top Inventory of the Player, InventoryView, or InventoryEvent.
     * In API versions 1.21 (and above), InventoryView is an interface.
     * In versions 1.20.6 and below, InventoryView is a class.
     *
     * @param object The object being referenced, this can be either a Player object,InventoryView object, or InventoryEvent object.
     * @return The top Inventory object from the object.
     */
    public static @Nonnull Inventory getTopInventory(final @Nonnull Object object) {
        try {
            final Object view = (object instanceof Player ? getOpenInventory(object)
                    : object instanceof InventoryEvent ? ((InventoryEvent)object).getView()
                    : object);
            final Method getTopInventory = view.getClass().getMethod("getTopInventory");
            getTopInventory.setAccessible(true);
            return (Inventory) getTopInventory.invoke(view);
        } catch (Exception e) {
            ServerUtils.logSevere("{CompatUtils} An error has occurred with InventoryView#getTopInventory!");
            ServerUtils.sendSevereTrace(e);
            throw new RuntimeException(object.getClass().getName(), e);
        }
    }

    /**
     * Attempts to get the Bottom Inventory of the Player, InventoryView, or InventoryEvent.
     * In API versions 1.21 (and above), InventoryView is an interface.
     * In versions 1.20.6 and below, InventoryView is a class.
     *
     * @param object The object being referenced, this can be either a Player object, InventoryView object, or InventoryEvent object.
     * @return The bottom Inventory object from the object.
     */
    public static @Nonnull Inventory getBottomInventory(final @Nonnull Object object) {
        try {
            final Object view = (object instanceof Player ? getOpenInventory(object)
                    : object instanceof InventoryEvent ? ((InventoryEvent)object).getView()
                    : object);
            final Method getBottomInventory = view.getClass().getMethod("getBottomInventory");
            getBottomInventory.setAccessible(true);
            return (Inventory) getBottomInventory.invoke(view);
        } catch (Exception e) {
            ServerUtils.logSevere("{CompatUtils} An error has occurred with InventoryView#getBottomInventory!");
            ServerUtils.sendSevereTrace(e);
            throw new RuntimeException(object.getClass().getName(), e);
        }
    }

    /**
     * Attempts to get the Player from the InventoryView or InventoryEvent.
     * In API versions 1.21 (and above), InventoryView is an interface.
     * In versions 1.20.6 and below, InventoryView is a class.
     *
     * @param object The object being referenced, this can be either a InventoryView object or InventoryEvent object.
     * @return The Player object from the object.
     */
    public static @Nonnull Player getPlayer(final @Nonnull Object object) {
        try {
            final Object view = (object instanceof InventoryEvent ? object.getClass().getMethod("getView").invoke(object)
                    : object);
            final Method getPlayer = view.getClass().getMethod("getPlayer");
            getPlayer.setAccessible(true);
            return (Player) getPlayer.invoke(view);
        } catch (Exception e) {
            ServerUtils.logSevere("{CompatUtils} An error has occurred with InventoryView#getPlayer!");
            ServerUtils.sendSevereTrace(e);
            throw new RuntimeException(object.getClass().getName(), e);
        }
    }

    /**
     * Attempts to get the Title of the InventoryView.
     * In API versions 1.21 (and above), InventoryView is an interface.
     * In versions 1.20.6 and below, InventoryView is a class.
     *
     * @param object The object being referenced, this can be either a Player object, InventoryView object, or InventoryEvent object.
     * @return The Title String from the object.
     */
    public static @Nonnull String getInventoryTitle(final @Nonnull Object object) {
        try {
            final Object view = (object instanceof Player ? getOpenInventory(object)
                    : object instanceof InventoryEvent ? ((InventoryEvent)object).getView()
                    : object);
            final Method getTitle = view.getClass().getMethod("getTitle");
            getTitle.setAccessible(true);
            return (String) getTitle.invoke(view);
        } catch (Exception e) {
            ServerUtils.logSevere("{CompatUtils} An error has occurred with InventoryView#getTitle!");
            ServerUtils.sendSevereTrace(e);
            throw new RuntimeException(object.getClass().getName(), e);
        }
    }

    /**
     * Attempts to get the InventoryType of the InventoryView.
     * In API versions 1.21 (and above), InventoryView is an interface.
     * In versions 1.20.6 and below, InventoryView is a class.
     *
     * @param object The object being referenced, this can be either a Player object, InventoryView object, or InventoryEvent object.
     * @return The InventoryType from the object.
     */
    public static @Nonnull InventoryType getInventoryType(final @Nonnull Object object) {
        try {
            final Object view = (object instanceof Player ? getOpenInventory(object)
                    : object instanceof InventoryEvent ? ((InventoryEvent)object).getView()
                    : object);
            final Method getType = view.getClass().getMethod("getType");
            getType.setAccessible(true);
            return (InventoryType) getType.invoke(view);
        } catch (Exception e) {
            ServerUtils.logSevere("{CompatUtils} An error has occurred with InventoryView#getType!");
            ServerUtils.sendSevereTrace(e);
            throw new RuntimeException(object.getClass().getName(), e);
        }
    }

    /**
     * Attempts to get the Cursor ItemStack of the InventoryView.
     * In API versions 1.21 (and above), InventoryView is an interface.
     * In versions 1.20.6 and below, InventoryView is a class.
     *
     * @param object The object being referenced, this can be either a Player object, InventoryView object, or InventoryEvent object.
     * @return The Cursor ItemStack from the object.
     */
    public static @Nonnull ItemStack getCursor(final @Nonnull Object object) {
        try {
            final Object view = (object instanceof Player ? getOpenInventory(object)
                    : object instanceof InventoryEvent ? ((InventoryEvent)object).getView()
                    : object);
            final Method getCursor = view.getClass().getMethod("getCursor");
            getCursor.setAccessible(true);
            return (ItemStack) getCursor.invoke(view);
        } catch (Exception e) {
            ServerUtils.logSevere("{CompatUtils} An error has occurred with InventoryView#getCursor!");
            ServerUtils.sendSevereTrace(e);
            throw new RuntimeException(object.getClass().getName(), e);
        }
    }

    /**
     * Attempts to set the Cursor ItemStack of the InventoryView.
     * In API versions 1.21 (and above), InventoryView is an interface.
     * In versions 1.20.6 and below, InventoryView is a class.
     *
     * @param object The object being referenced, this can be either a Player object, InventoryView object, or InventoryEvent object.
     * @param itemStack The ItemStack to be set on the cursor.
     */
    public static void setCursor(final @Nonnull Object object, final @Nonnull ItemStack itemStack) {
        try {
            final Object view = (object instanceof Player ? getOpenInventory(object)
                    : object instanceof InventoryEvent ? ((InventoryEvent)object).getView()
                    : object);
            final Method setCursor = view.getClass().getMethod("setCursor", ItemStack.class);
            setCursor.setAccessible(true);
            setCursor.invoke(view, itemStack);
        } catch (Exception e) {
            ServerUtils.logSevere("{CompatUtils} An error has occurred with InventoryView#setCursor!");
            ServerUtils.sendSevereTrace(e);
            throw new RuntimeException(object.getClass().getName(), e);
        }
    }

    /**
     * Attempts to get the ItemStack of the InventoryView.
     * In API versions 1.21 (and above), InventoryView is an interface.
     * In versions 1.20.6 and below, InventoryView is a class.
     *
     * @param object The object being referenced, this can be either a Player object, InventoryView object, or InventoryEvent object.
     * @param slot The slot of the ItemStack.
     * @return The ItemStack from the object.
     */
    public static @Nonnull ItemStack getItem(final @Nonnull Object object, final int slot) {
        try {
            final Object view = (object instanceof Player ? getOpenInventory(object)
                    : object instanceof InventoryEvent ? ((InventoryEvent)object).getView()
                    : object);
            final Method getCursor = view.getClass().getMethod("getItem", int.class);
            getCursor.setAccessible(true);
            return (ItemStack) getCursor.invoke(view, slot);
        } catch (Exception e) {
            ServerUtils.logSevere("{CompatUtils} An error has occurred with InventoryView#getItem!");
            ServerUtils.sendSevereTrace(e);
            throw new RuntimeException(object.getClass().getName(), e);
        }
    }

    /**
     * Attempts to set the ItemStack of the InventoryView.
     * In API versions 1.21 (and above), InventoryView is an interface.
     * In versions 1.20.6 and below, InventoryView is a class.
     *
     * @param object The object being referenced, this can be either a Player object, InventoryView object, or InventoryEvent object.
     * @param itemStack The ItemStack to be set.
     * @param slot The slot to set the ItemStack.
     */
    public static void setItem(final @Nonnull Object object, final @Nonnull ItemStack itemStack, final int slot) {
        try {
            final Object view = (object instanceof Player ? getOpenInventory(object)
                    : object instanceof InventoryEvent ? ((InventoryEvent)object).getView()
                    : object);
            final Method setItem = view.getClass().getMethod("setItem", int.class, ItemStack.class);
            setItem.setAccessible(true);
            setItem.invoke(view, slot, itemStack);
        } catch (Exception e) {
            ServerUtils.logSevere("{CompatUtils} An error has occurred with InventoryView#setItem!");
            ServerUtils.sendSevereTrace(e);
            throw new RuntimeException(object.getClass().getName(), e);
        }
    }

    /**
     * Attempts to get the Name of the Object.
     * Currently Supported:
     * Attribute#name deprecated in 1.21.
     * Sound#name deprecated in 1.21.
     * Pattern#name deprecated in 1.21.
     * PatternType#name deprecated in 1.21.
     * PotionEffect#getName deprecated in 1.20.3.
     * PotionEffectType#getName deprecated in 1.20.3.
     *
     * @param object The object to have its name fetched, can be an Attribute, Sound, Pattern, PatternType, PotionEffect, or PotionEffectType.
     * @return The name of the Object.
     */
    public static String getName(final @Nonnull Object object) {
        if (object instanceof Attribute) {
            return ((String) resolveByVersion("1_21", () -> ((Attribute) object).getKey().getKey(), () -> LegacyAPI.getAttributeName(object))).toUpperCase();
        } else if (object instanceof Sound) {
            return ((String) resolveByVersion("1_21", () -> ((Sound) object).getKey().getKey(), () -> LegacyAPI.getSoundName(object))).toUpperCase();
        } else if (object instanceof Pattern) {
            return ((String) resolveByVersion( "1_21", () -> ((Pattern) object).getPattern().getKey().getKey(), () -> LegacyAPI.getPatternName(((Pattern) object).getPattern()))).toUpperCase();
        } else if (object instanceof PatternType) {
            return ((String) resolveByVersion("1_21", () -> ((PatternType) object).getKey().getKey(), () -> LegacyAPI.getPatternName(object))).toUpperCase();
        } else if (object instanceof PotionEffect) {
            return ((String) resolveByVersion("1_20_3", () -> ((PotionEffect) object).getType().getKey().getKey(), () -> LegacyAPI.getEffectName(((PotionEffect) object).getType()))).toUpperCase();
        } else if (object instanceof PotionEffectType) {
            return ((String) resolveByVersion( "1_20_3", () -> ((PotionEffectType) object).getKey().getKey(), () -> LegacyAPI.getEffectName(object))).toUpperCase();
        }
        throw new RuntimeException("{CompatUtils} Unable to get name of an unknown class: " + object.getClass().getName());
    }

    /**
     * Attempts to get the Values of the Object Class.
     * Currently Supported:
     * Attribute#values deprecated in 1.21.3.
     * Sound#values deprecated in 1.21.3.
     * PatternType#values deprecated in 1.21.
     * PotionEffectType#values deprecated in 1.20.3.
     * Enchantment#values deprecated in 1.20.3.
     *
     * @param clazz The class to have its name fetched, can be an Attribute, Sound, PatternType, PotionEffectType, or Enchantment.
     * @return The values of the Object Class.
     */
    public static <T> List<T> values(final @Nonnull Class<T> clazz) {
        if (clazz.equals(Attribute.class)) {
            return (List<T>) resolveByVersion("1_21_3", () -> ImmutableList.copyOf(Registry.ATTRIBUTE.iterator()), LegacyAPI::getAttributes);
        } else if (clazz.equals(Sound.class)) {
            return (List<T>) resolveByVersion("1_21_3", () -> ImmutableList.copyOf(Registry.SOUNDS.iterator()), LegacyAPI::getSounds);
        } else if (clazz.equals(PatternType.class)) {
            return (List<T>) resolveByVersion("1_21", () -> ImmutableList.copyOf(Registry.BANNER_PATTERN.iterator()), LegacyAPI::getPatterns);
        } else if (clazz.equals(PotionEffectType.class)) {
            return (List<T>) resolveByVersion("1_20_3", () -> ImmutableList.copyOf(Registry.EFFECT.iterator()), LegacyAPI::getEffects);
        } else if (clazz.equals(Enchantment.class)) {
            return (List<T>) resolveByVersion("1_20_3", () -> ImmutableList.copyOf(Registry.ENCHANTMENT.iterator()), LegacyAPI::getEnchants);
        }
        throw new RuntimeException("{CompatUtils} Unable to get values of an unknown class: " + clazz.getName());
    }

    /**
     * Attempts to get the valueOf the Class Name given the Class.
     * Currently Supported:
     * Attribute#valueOf deprecated in 1.21.3.
     * Sound#valueOf deprecated in 1.21.3.
     *
     * @param clazz The class to have its name fetched, can be an Attribute, or Sound.
     * @param clazzName The name of the Object of the Class to fetch.
     * @return The Class Object of the clazzName.
     */
    public static <T> Object valueOf(final @Nonnull Class<T> clazz, final @Nonnull String clazzName) {
        if (clazz.equals(Attribute.class)) {
            return resolveByVersion("1_21_3", () -> Registry.ATTRIBUTE.get(NamespacedKey.minecraft(clazzName.toLowerCase().replace("generic_", ""))), () -> LegacyAPI.getAttribute(clazzName));
        } else if (clazz.equals(Sound.class)) {
            return resolveByVersion("1_21_3", () -> Registry.SOUNDS.get(NamespacedKey.minecraft(clazzName.toLowerCase())), () -> LegacyAPI.getSound(clazzName));
        }
        throw new RuntimeException("{CompatUtils} Unable to get values of an unknown class: " + clazz.getName() + " with the value: " + clazzName);
    }

    /**
     * Checks if the Bukkit Inventory is empty.
     * This exists because some server forks have removed the #isEmpty method for whatever reason.
     *
     * @param inventory The inventory to be checked.
     * @return If the Bukkit Inventory is empty.
     */
    public static boolean isInventoryEmpty(final @Nonnull Inventory inventory) {
        try {
            final Method isEmpty = inventory.getClass().getMethod("isEmpty");
            if (isEmpty != null) {
                return (boolean) isEmpty.invoke(inventory);
            }
        } catch (Exception ignored) { }

        // Fallback method to check if inventory is empty
        for (final ItemStack item : inventory.getContents()) {
            if (item != null && item.getType() != Material.AIR) {
                return false;
            }
        }
        return true;
    }

    /**
     * Resolves a value based on the server version.
     * This utility method attempts to execute modern logic for retrieving a value if the server version supports it.
     * If the modern logic fails or the version does not support it, the fallback legacy logic is executed instead.
     *
     * @param updateVersion The minimum server version required to use the modern logic e.g., 1_21_1)
     * @param modernGetter  A supplier containing the modern logic to execute (e.g., accessing Registry).
     * @param legacyGetter  A supplier containing the legacy fallback logic to execute if the modern logic fails or the server version is insufficient.
     * @return The resolved value, either from the modern logic or the legacy fallback.
     */
    public static Object resolveByVersion(final String updateVersion, final Supplier<Object> modernGetter, final Supplier<Object> legacyGetter) {
        try {
            return ServerUtils.hasPreciseUpdate(updateVersion) ? modernGetter.get() : legacyGetter.get();
        } catch (Throwable t) {
            return legacyGetter.get();
        }
    }
}