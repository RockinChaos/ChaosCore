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

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Called when a player enters an emulated creative mode.
 */
@SuppressWarnings("unused")
public class PlayerEnterCreativeEvent extends PlayerEvent implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private final CommandSender who;
    private final boolean refresh;
    private final boolean restore;
    private final boolean silent;
    private Result result;

    /**
     * Creates a new PlayerEnterCreativeEvent instance.
     *
     * @param who       - The Sender triggering the event.
     * @param altWho    - The other Player being referenced.
     * @param refresh   - If the gamemode change is a simple refresh.
     * @param restore   - If the player inventory should be restored.
     * @param silent    - If the event should trigger any messages.
     */
    public PlayerEnterCreativeEvent(final @Nonnull CommandSender who, final @Nullable Player altWho, final boolean refresh, final boolean restore, final boolean silent) {
        super((altWho != null ? altWho : (Player) who));
        this.who = who;
        this.refresh = refresh;
        this.restore = restore;
        this.silent = silent;
        this.result = Result.ALLOW;
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
     * Gets the cancellation state of this event.
     * Set to true if you want to prevent the creative mode action.
     *
     * @return boolean cancellation state.
     */
    public boolean isCancelled() {
        return this.returnResult() == Result.DENY;
    }

    /**
     * Sets the cancellation state of this event. A canceled event will not be
     * executed in the server, but will still pass to other plugins.
     * <p>
     * Canceling this event will prevent the player from entering creative mode.
     *
     * @param cancel true if you wish to cancel this event.
     */
    public void setCancelled(final boolean cancel) {
        this.getResult(cancel ? Result.DENY : this.returnResult() == Result.DENY ? Result.DEFAULT : this.returnResult());
    }

    /**
     * This controls the action to take with the creative mode action.
     * When this is set to default, it will be allowed if no action
     * is taken on the creative mode action.
     *
     * @return The action to take with the creative mode action.
     */
    public @Nonnull Result returnResult() {
        return this.result;
    }

    /**
     * Sets the creative mode action to be enabled or disabled.
     *
     * @param setCreative - the action to take with the creative mode action.
     */
    public void getResult(final @Nonnull Result setCreative) {
        this.result = setCreative;
    }

    /**
     * Gets the Sender attempting to get the Player's GameMode.
     *
     * @return The Sender.
     */
    public @Nonnull CommandSender getSender() {
        return this.who;
    }

    /**
     * Gets if the GameMode change is being refreshed.
     *
     * @return If the change should be refreshed.
     */
    public boolean isRefresh() {
        return this.refresh;
    }

    /**
     * Gets if the GameMode change is being restored from a reload or restart.
     *
     * @return If the change should be restored.
     */
    public boolean isRestore() {
        return this.restore;
    }

    /**
     * Gets if the GameMode change should be silent.
     *
     * @return If the change is silent.
     */
    public boolean isSilent() {
        return this.silent;
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