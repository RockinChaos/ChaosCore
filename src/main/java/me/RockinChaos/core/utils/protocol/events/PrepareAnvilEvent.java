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
import javax.annotation.Nullable;
import java.util.List;

/**
 * Called when a player attempts type inside an anvil inventory.
 * Only intended for Legacy use i.e. versions below 1.9 that do not have the native PrepareAnvilEvent.
 */
@SuppressWarnings("unused")
public class PrepareAnvilEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    protected final Object transaction;
    protected ItemStack[] topContents;
    protected ItemStack[] bottomContents;

    protected final String textEntry;
    private Result prepareAnvil;

    /**
     * Creates a new PrepareAnvilEvent instance.
     *
     * @param transaction - The InventoryView of the anvil inventory.
     * @param textEntry   - The desired text input for the Anvil.
     */
    public PrepareAnvilEvent(final @Nonnull Object transaction, final @Nonnull String textEntry) {
        this.transaction = transaction;
        this.textEntry = textEntry;
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
     * Returns the current instance of the top inventory.
     *
     * @return The instance of the top inventory.
     */
    public @Nonnull Inventory getTopInventory() {
        return CompatUtils.getTopInventory(this.transaction);
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
     * Returns the item in the specified slot for the anvil inventory.
     *
     * @param slot The slot to get the item from.
     * @return The item inside the specified anvil inventory slot.
     */
    public @Nullable ItemStack getItem(final int slot) {
        return CompatUtils.getTopInventory(this.transaction).getItem(slot);
    }

    /**
     * Returns the current text inside the anvil rename field.
     *
     * @return The text inside the anvil rename field.
     */
    public @Nonnull String getRenameText() {
        return this.textEntry;
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
     * Sets the ItemStack to the anvil inventory result slot.
     *
     * @param stack - The ItemStack to be set.
     */
    public void setResult(@Nonnull final ItemStack stack) {
        CompatUtils.getTopInventory(this.transaction).setItem(2, stack.clone());
    }

    /**
     * Gets the cancellation state of this event.
     *
     * @return boolean cancellation state.
     */
    public boolean isCancelled() {
        return this.prepareAnvil() == Result.DENY;
    }

    /**
     * Sets the cancellation state of this event. A canceled event will not be
     * executed in the server, but will still pass to other plugins.
     *
     * @param cancel true if you wish to cancel this event.
     */
    public void setCancelled(final boolean cancel) {
        this.prepareAnvil(cancel ? Result.DENY : this.prepareAnvil() == Result.DENY ? Result.DEFAULT : this.prepareAnvil());
    }

    /**
     * This controls the action to take with the PrepareAnvilEvent.
     *
     * @return the action to take with the PrepareAnvilEvent.
     */
    public @Nonnull Result prepareAnvil() {
        return prepareAnvil;
    }

    /**
     * Sets the PrepareAnvilEvent to be enabled or disabled.
     *
     * @param prepareAnvil the action to take with the PrepareAnvilEvent.
     */
    public void prepareAnvil(final @Nonnull Result prepareAnvil) {
        this.prepareAnvil = prepareAnvil;
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