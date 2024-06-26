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
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerEvent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Called when a player tries to clone an item.
 */
@SuppressWarnings("unused")
public class PlayerCloneItemEvent extends PlayerEvent implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    protected final int slot;
    protected final ClickType clickType;
    private Result useClone;

    /**
     * Creates a new PlayerCloneItemEvent instance.
     *
     * @param who       - The Player triggering the event.
     * @param slot      - The slot being interacted.
     * @param clickType - The click action.
     */
    public PlayerCloneItemEvent(final @Nonnull Player who, final int slot, final @Nullable ClickType clickType) {
        super(who);
        this.slot = slot;
        this.clickType = clickType;
        this.useClone = clickType == null ? Result.DENY : Result.ALLOW;
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
     * Gets the cancellation state of this event. Set to true if you want to
     * prevent the item cloning from functioning.
     *
     * @return boolean cancellation state.
     */
    public boolean isCancelled() {
        return this.useClone() == Result.DENY;
    }

    /**
     * Sets the cancellation state of this event. A canceled event will not be
     * executed in the server, but will still pass to other plugins.
     * <p>
     * Canceling this event will prevent use of the item cloning feature (clicking an
     * item to item cloning will result in nothing happening, materials will not be lost.)
     *
     * @param cancel true if you wish to cancel this event.
     */
    public void setCancelled(final boolean cancel) {
        this.useClone(cancel ? Result.DENY : this.useClone() == Result.DENY ? Result.DEFAULT : this.useClone());
    }

    /**
     * Returns the Slot represented by this event.
     *
     * @return The Slot the cloning action is referencing.
     */
    public int getSlot() {
        return this.slot;
    }

    /**
     * Returns the ClickType represented by this event.
     *
     * @return The ClickType the cloning action triggered.
     */
    public @Nonnull ClickType getClick() {
        return (this.clickType == null ? ClickType.UNKNOWN : this.clickType);
    }

    /**
     * Returns the InventoryView represented by this event.
     *
     * @return The InventoryView which the cloning took place.
     */
    public @Nonnull Object getView() {
        return CompatUtils.getOpenInventory(player);
    }

    /**
     * Returns the InventoryType represented by this event.
     *
     * @return The InventoryType which the cloning took place.
     */
    public @Nonnull InventoryType getInventoryType() {
        return CompatUtils.getInventoryType(CompatUtils.getOpenInventory(player));
    }

    /**
     * This controls the action to take when a player attempts to clone an item in their inventory.
     *
     * @return The action to take with the item clone.
     */
    public @Nonnull Result useClone() {
        return this.useClone;
    }

    /**
     * Sets the item cloning feature to be enabled or disabled.
     *
     * @param useClone the action to take with the item clone.
     */
    public void useClone(final @Nonnull Result useClone) {
        this.useClone = useClone;
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