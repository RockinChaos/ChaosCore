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

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;
import java.lang.reflect.Method;

/**
 * Welcome to the magical land of hopes and dreams.
 * These methods exist to bridge the gap between newer and older Minecraft versions
 * with the goal to be able to compile using the latest Minecraft version.
 */
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
}