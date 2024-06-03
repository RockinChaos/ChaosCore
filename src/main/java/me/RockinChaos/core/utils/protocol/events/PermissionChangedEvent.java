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

import javax.annotation.Nonnull;
import java.util.List;

/**
 * Called when a player tries to pick block using the middle mouse button.
 */
@SuppressWarnings("unused")
public class PermissionChangedEvent extends PlayerEvent implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private final List<String> changedPermissions;
    private Result result;

    /**
     * Creates a new PermissionChangedEvent instance.
     *
     * @param who                - The Player triggering the event.
     * @param changedPermissions - The List of changed permissions and their previous values.
     */
    public PermissionChangedEvent(final @Nonnull Player who, final @Nonnull List<String> changedPermissions) {
        super(who);
        this.changedPermissions = changedPermissions;
        this.result = changedPermissions.isEmpty() ? Result.DENY : Result.ALLOW;
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
     * Set to true if you want to prevent the permission changed action.
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
     * Canceling this event will prevent the permission changed action from triggering.
     *
     * @param cancel true if you wish to cancel this event.
     */
    public void setCancelled(final boolean cancel) {
        this.getResult(cancel ? Result.DENY : this.returnResult() == Result.DENY ? Result.DEFAULT : this.returnResult());
    }

    /**
     * This controls the action to take with the permission changed action.
     * When this is set to default, it will be allowed if no action
     * is taken on the permission changed action.
     *
     * @return The action to take with the pick block action.
     */
    public @Nonnull Result returnResult() {
        return this.result;
    }

    /**
     * Sets the permission changed action to be enabled or disabled.
     *
     * @param setPermission - the action to take with the permission changed action.
     */
    public void getResult(final @Nonnull Result setPermission) {
        this.result = setPermission;
    }

    /**
     * Gets the list of changed permissions as their previous values.
     *
     * @return The List of changed permissions and their previous values.
     */
    public @Nonnull List<String> getChangedPermissions() {
        return this.changedPermissions;
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