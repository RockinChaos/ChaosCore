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

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;

/**
 * Called when a player tries to auto craft using the recipe book.
 */
@SuppressWarnings("unused")
public class PlayerAutoCraftEvent extends PlayerEvent implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    protected Inventory craftingInventory;
    private Result useAutoCraft;

    /**
     * Creates a new PlayerAutoCraftEvent instance.
     *
     * @param who               - The Player triggering the event.
     * @param craftingInventory - The crafting inventory being interacted.
     */
    public PlayerAutoCraftEvent(final Player who, final Inventory craftingInventory) {
        super(who);
        this.craftingInventory = craftingInventory;
        this.useAutoCraft = craftingInventory == null ? Result.DENY : Result.ALLOW;
    }

    /**
     * Gets the HandlerList for the event.
     *
     * @return The HandlerList for the event.
     */
    public static HandlerList getHandlerList() {
        return handlers;
    }

    /**
     * Gets the cancellation state of this event. Set to true if you want to
     * prevent the auto craft from shifting materials from the players inventory
     * to their crafting slots, materials will not be lost.
     *
     * @return boolean cancellation state.
     */
    public boolean isCancelled() {
        return this.useAutoCraft() == Result.DENY;
    }

    /**
     * Sets the cancellation state of this event. A canceled event will not be
     * executed in the server, but will still pass to other plugins.
     * <p>
     * Canceling this event will prevent use of the auto craft feature (clicking an
     * item to auto craft will result in nothing happening, materials will not be lost.)
     *
     * @param cancel true if you wish to cancel this event.
     */
    public void setCancelled(final boolean cancel) {
        this.useAutoCraft(cancel ? Result.DENY : this.useAutoCraft() == Result.DENY ? Result.DEFAULT : this.useAutoCraft());
    }

    /**
     * Returns the crafting inventory represented by this event.
     *
     * @return Crafting inventory of the auto  craft pattern.
     */
    public Inventory getCrafting() {
        return this.craftingInventory;
    }

    /**
     * Convenience method. Returns the contents of the crafting inventory represented by
     * this event.
     *
     * @return Contents the crafting inventory.
     */
    public ItemStack[] getContents() {
        return this.craftingInventory.getContents();
    }

    /**
     * This controls the action to take with the crafting slots the player is trying to auto craft in
     * This includes both the crafting inventory and items (such as flint and steel or
     * records). When this is set to default, it will be allowed if no action
     * is taken on the crafting inventory.
     *
     * @return The action to take with the auto craft pattern.
     */
    public Result useAutoCraft() {
        return this.useAutoCraft;
    }

    /**
     * Sets the auto craft feature to be enabled or disabled.
     *
     * @param useAutoCraft the action to take with the auto craft pattern.
     */
    public void useAutoCraft(final Result useAutoCraft) {
        this.useAutoCraft = useAutoCraft;
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