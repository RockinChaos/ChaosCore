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
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Called when a player tries to pick entity using the middle mouse button.
 */
@SuppressWarnings("unused")
public class PlayerPickEntityEvent extends PlayerEvent implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private final int entityId;
    private final World entityWorld;
    private final int slot;
    private final ItemStack heldItem;
    private ItemStack itemStack = null;
    protected Inventory inventory;
    private Result usePickEntity;

    /**
     * Creates a new PlayerPickEntityEvent instance.
     *
     * @param who       - The Player triggering the event.
     * @param entityId  - The id of the entity that was picked.
     * @param slot      - The hotbar slot index.
     * @param inventory - The inventory being interacted.
     */
    public PlayerPickEntityEvent(final @Nonnull Player who, final int entityId, final int slot, final @Nullable Inventory inventory) {
        super(who);
        this.entityId = entityId;
        this.entityWorld = who.getWorld();
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
        this.usePickEntity = inventory == null ? Result.DENY : Result.ALLOW;
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
     * prevent the pick entity action from shifting materials in the players inventory, materials will not be lost.
     *
     * @return boolean cancellation state.
     */
    public boolean isCancelled() {
        return this.usePickEntity() == Result.DENY;
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
        this.usePickEntity(cancel ? Result.DENY : this.usePickEntity() == Result.DENY ? Result.DEFAULT : this.usePickEntity());
    }

    /**
     * Gets the hotbar slot index where the picked item will be placed.
     * <p>
     * This represents the destination slot in the player's hotbar (0-8)
     * where the picked entity item is being moved to.
     *
     * @return The hotbar slot index (0-8)
     */
    public int getSlot() {
        return this.slot;
    }

    /**
     * Finds which inventory slot received the picked entity item by comparing the
     * current inventory state against a pre-event snapshot.
     * <p>
     * Must be called after the pick event has modified the inventory.
     * NOTE: This event must be run synchronously or an Exception will be thrown.
     *
     * @return The slot index (8-35) where the entity item was placed, or -1 if not found
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
     * Returns the id of the Entity being targeted by the Player.
     *
     * @return The id of the Entity being targeted.
     */
    public int getEntityId() {
        return this.entityId;
    }

    /**
     * Returns the ItemStack of the Entity being targeted by the Player.
     * NOTE: This event must be run synchronously or an Exception will be thrown.
     *
     * @return The ItemStack of the Entity being targeted.
     */
    public @Nullable ItemStack getItemStack() throws Exception {
        if (this.itemStack != null) return this.itemStack;
        else if (Bukkit.isPrimaryThread()) {
            for (final Entity entity : this.entityWorld.getEntities()) {
                if (entity.getEntityId() == this.entityId) {
                    this.itemStack = ItemHandler.getEntityItem(entity);
                    return this.itemStack;
                }
            }
            return null;
        }
        throw new Exception("Cannot get ItemStack of an Entity asynchronously!");
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
     * This represents the inventory state before the pick entity action was processed.
     *
     * @return The inventory contents snapshot
     */
    public @Nonnull ItemStack[] getContents() {
        return this.inventory != null ? this.inventory.getContents() : new ItemStack[0];
    }

    /**
     * This controls the action to take with the pick entity action.
     * When this is set to default, it will be allowed if no action
     * is taken on the pick entity action.
     *
     * @return The action to take with the pick entity action.
     */
    public @Nonnull Result usePickEntity() {
        return this.usePickEntity;
    }

    /**
     * Sets the pick entity action to be enabled or disabled.
     *
     * @param usePickEntity the action to take with the pick entity action.
     */
    public void usePickEntity(final @Nonnull Result usePickEntity) {
        this.usePickEntity = usePickEntity;
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