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
package me.RockinChaos.core.utils.protocol.events;

import me.RockinChaos.core.utils.CompatUtils;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * Called when a player attempts to close an open inventory.
 */
@SuppressWarnings("unused")
public class InventoryCloseEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    protected final Object transaction;
    protected ItemStack[] topContents;
    protected ItemStack[] bottomContents;
    private Result inventoryClose;

    /**
     * Creates a new InventoryCloseEvent instance.
     *
     * @param transaction - The InventoryView of the closed window.
     */
    public InventoryCloseEvent(final @Nonnull Object transaction) {
        this.transaction = transaction;
    }

    /**
     * Gets the HandlerList for the event.
     *
     * @return The HandlerList for the event.
     */
    public static @Nonnull HandlerList getHandlerList() {
        return handlers;
    }

    /**
     * Saves copies of the top and bottom inventory contents.
     */
    private void saveContents() {
        if (this.topContents == null && this.bottomContents == null) {
            int itr = 0;
            this.topContents = CompatUtils.getTopInventory(this.transaction).getContents();
            this.bottomContents = CompatUtils.getBottomInventory(this.transaction).getContents();
            for (ItemStack stack : this.topContents) {
                if (this.topContents[itr] != null) {
                    this.topContents[itr] = stack.clone();
                }
                itr++;
            }
            itr = 0;
            for (ItemStack stack : this.bottomContents) {
                if (this.bottomContents[itr] != null) {
                    this.bottomContents[itr] = stack.clone();
                }
                itr++;
            }
        }
    }

    /**
     * Returns the player involved in this event
     *
     * @return Player who is involved in this event
     */
    public final @Nonnull Player getPlayer() {
        this.saveContents();
        return CompatUtils.getPlayer(this.transaction);
    }

    /**
     * Returns the previous contents of the top or bottom inventory before the event occurred.
     *
     * @param isTop - If the top inventory should be returned.
     * @return The Previous Contents of the top or bottom inventory.
     */
    public @Nonnull ItemStack[] getPreviousContents(final boolean isTop) {
        return (isTop ? this.topContents : this.bottomContents);
    }

    /**
     * Returns the current contents of the top inventory.
     *
     * @return Contents of the top inventory.
     */
    public @Nonnull ItemStack[] getTopContents() {
        return CompatUtils.getTopInventory(this.transaction).getContents();
    }

    /**
     * Returns the current contents of the bottom inventory.
     *
     * @return Contents of the bottom inventory.
     */
    public @Nonnull ItemStack[] getBottomContents() {
        return CompatUtils.getBottomInventory(this.transaction).getContents();
    }

    /**
     * Returns the current contents of the player inventory.
     *
     * @return Contents of the player inventory.
     */
    public @Nonnull Inventory getInventory() {
        return CompatUtils.getTopInventory(this.transaction);
    }

    /**
     * Returns the list of viewers for the player inventory.
     *
     * @return The list of inventory viewers.
     */
    public @Nonnull List<HumanEntity> getViewers() {
        return CompatUtils.getTopInventory(this.transaction).getViewers();
    }

    /**
     * Returns the InventoryView as an Object.
     *
     * @return The InventoryView as an Object.
     */
    public Object getView() {
        return this.transaction;
    }

    /**
     * Removes the ItemStack from the InventoryView.
     *
     * @param stack - The ItemStack to be removed.
     */
    public void removeItem(final @Nonnull ItemStack stack, final int slot) {
        stack.setAmount(0);
        stack.setType(Material.AIR);
        try {
            CompatUtils.getTopInventory(this.transaction).setItem(slot, new ItemStack(Material.AIR));
        } catch (IllegalStateException ignored) {
        }
    }

    /**
     * Gets the cancellation state of this event.
     *
     * @return boolean cancellation state.
     */
    public boolean isCancelled() {
        return this.inventoryClose() == Result.DENY;
    }

    /**
     * Sets the cancellation state of this event. A canceled event will not be
     * executed in the server, but will still pass to other plugins.
     *
     * @param cancel true if you wish to cancel this event.
     */
    public void setCancelled(final boolean cancel) {
        this.inventoryClose(cancel ? Result.DENY : this.inventoryClose() == Result.DENY ? Result.DEFAULT : this.inventoryClose());
    }

    /**
     * This controls the action to take with the InventoryCloseEvent.
     *
     * @return the action to take with the InventoryCloseEvent.
     */
    public @Nonnull Result inventoryClose() {
        return inventoryClose;
    }

    /**
     * Sets the InventoryCloseEvent to be enabled or disabled.
     *
     * @param inventoryClose the action to take with the InventoryCloseEvent.
     */
    public void inventoryClose(final @Nonnull Result inventoryClose) {
        this.inventoryClose = inventoryClose;
    }

    /**
     * Gets the Handlers for the event.
     *
     * @return The HandlerList for the event.
     */
    @Override
    public @Nonnull HandlerList getHandlers() {
        return handlers;
    }
}