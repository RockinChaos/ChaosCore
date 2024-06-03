package me.RockinChaos.core.utils.protocol.events;
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

import org.bukkit.World;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import javax.annotation.Nonnull;

/**
 * Called when the current day reaches midnight.
 */
@SuppressWarnings("unused")
public class NextDayEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private final boolean isDay;
    private final World world;
    private Result allowChange;

    /**
     * Creates a new NextDayEvent instance.
     *
     * @param isDay - If it is Day or Night.
     */
    public NextDayEvent(final boolean isDay, final @Nonnull World world) {
        this.isDay = isDay;
        this.world = world;
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
     * Checks if it is Day or Night.
     *
     * @return If it is Day or Night.
     */
    public boolean isDay() {
        return this.isDay;
    }

    /**
     * Gets the World related to the Day/Night.
     *
     * @return The World related to the Day/Night.
     */
    public @Nonnull World getWorld() {
        return this.world;
    }

    /**
     * Gets the cancellation state of this event. Set to true if you want to
     * prevent the time from changing either from day to night or night to day.
     *
     * @return boolean cancellation state.
     */
    public boolean isCancelled() {
        return this.allowChange() == Result.DENY;
    }

    /**
     * Sets the cancellation state of this event. A canceled event will not be
     * executed in the server, but will still pass to other plugins.
     * <p>
     * Canceling this event will prevent the time from changing.
     *
     * @param cancel true if you wish to cancel this event.
     */
    public void setCancelled(boolean cancel) {
        this.allowChange(cancel ? Result.DENY : this.allowChange() == Result.DENY ? Result.DEFAULT : this.allowChange());
    }

    /**
     * This controls the action to take with the day change action.
     * When this is set to default, it will be allowed if no action
     * is taken on the day change action.
     *
     * @return The action to take with the day change action.
     */
    public @Nonnull Result allowChange() {
        return this.allowChange;
    }

    /**
     * Sets the time change to be allowed or not.
     *
     * @param allowChange - The action to take with the next day action.
     */
    public void allowChange(final @Nonnull Result allowChange) {
        this.allowChange = allowChange;
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
