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

import me.RockinChaos.core.handlers.ItemHandler;
import me.RockinChaos.core.handlers.PlayerHandler;
import me.RockinChaos.core.utils.ServerUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Called when a player tries to pick block using the middle mouse button.
 */
@SuppressWarnings("unused")
public class PlayerPickBlockEvent extends PlayerEvent implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private final Block block;
    private final ItemStack heldItem;
    private final int slot;
    private ItemStack itemStack = null;
    protected Inventory inventory = null;
    private Result usePickBlock;

    /**
     * Creates a new PlayerPickItemEvent instance.
     *
     * @param who       - The Player triggering the event.
     * @param block  - The block that was picked (if any).
     * @param slot      - The hotbar slot index.
     * @param inventory - The inventory being interacted.
     */
    public PlayerPickBlockEvent(final @Nonnull Player who, final @Nullable Block block, final int slot, final @Nullable Inventory inventory) {
        super(who);
        this.block = block;
        this.slot = slot;
        this.heldItem = PlayerHandler.getHandItem(who);
        if (inventory != null) {
            final Inventory cloneInventory = Bukkit.createInventory(who, 36);
            for (int i = 0; i < 36; i++) {
                if (inventory.getContents()[i] != null && inventory.getContents()[i].getType() != Material.AIR) {
                    cloneInventory.setItem(i, inventory.getContents()[i]);
                }
            }
            this.inventory = cloneInventory;
        }
        this.usePickBlock = inventory == null ? Result.DENY : Result.ALLOW;
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
     * prevent the pick block action from shifting materials in the players inventory, materials will not be lost.
     *
     * @return boolean cancellation state.
     */
    public boolean isCancelled() {
        return this.usePickBlock() == Result.DENY;
    }

    /**
     * Sets the cancellation state of this event. A canceled event will not be
     * executed in the server, but will still pass to other plugins.
     * <p>
     * Canceling this event will prevent use of the pick item feature (middle-clicking an
     * object to pick an item will result in nothing happening, materials will not be lost.)
     *
     * @param cancel true if you wish to cancel this event.
     */
    public void setCancelled(final boolean cancel) {
        if (cancel && Bukkit.isPrimaryThread() && this.inventory != null) {
            final Inventory playerInventory = super.getPlayer().getInventory();
            for (int i = 0; i < 36; i++) {
                playerInventory.setItem(i, this.inventory.getContents()[i]);
            }
            PlayerHandler.updateInventory(super.getPlayer());
        }
        this.usePickBlock(cancel ? Result.DENY : this.usePickBlock() == Result.DENY ? Result.DEFAULT : this.usePickBlock());
    }

    /**
     * Gets the hotbar slot index where the picked item will be placed.
     * <p>
     * This represents the destination slot in the player's hotbar (0-8)
     * where the picked block is being moved to.
     *
     * @return The hotbar slot index (0-8)
     */
    public int getSlot() {
        return this.slot;
    }

    /**
     * Finds which inventory slot received the picked block by comparing the
     * current inventory state against a pre-event snapshot.
     * <p>
     * Must be called after the pick event has modified the inventory.
     *
     * @return The slot index (8-35) where the block was placed, or -1 if not found
     */
    public int getPickSlot() {
        if (this.inventory != null) {
            final Inventory inventory = super.getPlayer().getInventory();
            final ItemStack currentHeldItem = inventory.getContents()[this.slot];
            final ItemStack previousHeldItem = this.inventory.getContents()[this.slot];
            if ((currentHeldItem != null && currentHeldItem.getType() != Material.AIR) && (previousHeldItem == null || previousHeldItem.getType() == Material.AIR || !previousHeldItem.isSimilar(currentHeldItem))) {
                for (int i = 8; i < 36; i++) {
                    final ItemStack previousItem = this.inventory.getContents()[i];
                    final ItemStack currentItem = inventory.getContents()[i];
                    if (previousItem != null && previousItem.getType() != Material.AIR && previousItem.isSimilar(currentHeldItem) && (currentItem == null || currentItem.getType() == Material.AIR || !currentItem.isSimilar(currentHeldItem))) {
                        return i;
                    }
                }
            }
        }
        return -1;
    }

    /**
     * Returns the Block being targeted by the Player.
     *
     * @return The Block being targeted.
     */
    public @Nullable Block getBlock() {
        if (this.block != null) return this.block;
        Block targetBlock = null;
        try {
            targetBlock = super.getPlayer().getTargetBlock(null, 16);
        } catch (Exception ignored) {}
        return targetBlock;
    }

    /**
     * Returns the ItemStack of the Block being targeted by the Player.
     * NOTE: This event must be run synchronously on Minecraft 1.14+ or an Exception will be thrown.
     *
     * @return The ItemStack of the Block being targeted.
     */
    public @Nullable ItemStack getItemStack() throws Exception {
        if (this.block == null) return null;
        else if (this.itemStack != null) return this.itemStack;
        else if (!ServerUtils.hasSpecificUpdate("1_14") || Bukkit.isPrimaryThread()) {
            this.itemStack = ItemHandler.getItemStack(this.block, super.getPlayer());
            return this.itemStack;
        }
        throw new Exception("Cannot get ItemStack of a Block asynchronously!");
    }

    /**
     * Returns the ItemStack that is being moved from the Players hand prior to swapping.
     *
     * @return The ItemStack that is being swapped.
     */
    public @Nonnull ItemStack getHeldItem() {
        return this.heldItem;
    }

    /**
     * Gets the inventory contents represented by this event.
     * <p>
     * This represents the inventory state before the pick block action was processed.
     *
     * @return The inventory contents snapshot
     */
    public @Nonnull ItemStack[] getContents() {
        return this.inventory != null ? this.inventory.getContents() : new ItemStack[0];
    }

    /**
     * This controls the action to take with the pick block action.
     * When this is set to default, it will be allowed if no action
     * is taken on the pick block action.
     *
     * @return The action to take with the pick block action.
     */
    public @Nonnull Result usePickBlock() {
        return this.usePickBlock;
    }

    /**
     * Sets the pick block action to be enabled or disabled.
     *
     * @param usePickBlock the action to take with the pick block action.
     */
    public void usePickBlock(final @Nonnull Result usePickBlock) {
        this.usePickBlock = usePickBlock;
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