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
package me.RockinChaos.core.handlers;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.filter.AbstractFilter;
import org.apache.logging.log4j.message.Message;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.HashMap;

@SuppressWarnings("unused")
public class LogHandler extends AbstractFilter {

    private static LogHandler filter;
    private final HashMap<String, ArrayList<String>> hiddenExecutors = new HashMap<>();

    /**
     * Gets the instance of the LogHandler.
     *
     * @return The LogHandler instance.
     */
    public static @Nonnull LogHandler getFilter() {
        if (filter == null) {
            filter = new LogHandler();
            ((LoggerContext) LogManager.getContext(false)).getConfiguration().getLoggerConfig(LogManager.ROOT_LOGGER_NAME).addFilter(filter);
        }
        return filter;
    }

    /**
     * Sets the Result of the filter,
     * attempts to prevent the hiddenExecutors from being chat logged.
     *
     * @param message - The message being checked for hiddenExecutors.
     */
    private @Nonnull Result handle(final String message) {
        if (message == null) {
            return Result.NEUTRAL;
        }
        if (!this.hiddenExecutors.isEmpty() && this.hiddenExecutors.containsKey("commands-list")) {
            for (String word : this.hiddenExecutors.get("commands-list")) {
                if (message.toLowerCase().contains(word.toLowerCase())) {
                    return Result.DENY;
                }
            }
        }
        return Result.NEUTRAL;
    }

    /**
     * Attempts to hide the hiddenExecutors from the chat logger.
     *
     * @param event - The logger handling the message.
     */
    @Override
    public @Nonnull Result filter(final @Nonnull LogEvent event) {
        return this.handle(event.getMessage().getFormattedMessage());
    }

    /**
     * Attempts to hide the hiddenExecutors from the chat logger.
     *
     * @param logger - The logger handling the message.
     * @param level  - The level of execution.
     * @param marker - The filter marker.
     * @param msg    - The message caught by the filter.
     * @param t      - The cached Throwable.
     * @return The result of the filter.
     */
    @Override
    public @Nonnull Result filter(final @Nonnull Logger logger, final @Nonnull Level level, final @Nonnull Marker marker, final Message msg, final Throwable t) {
        return this.handle(msg.getFormattedMessage());
    }

    /**
     * Attempts to hide the hiddenExecutors from the chat logger.
     *
     * @param logger - The logger handling the message.
     * @param level  - The level of execution.
     * @param marker - The filter marker.
     * @param msg    - The message caught by the filter.
     * @param t      - The cached Throwable.
     * @return The result of the filter.
     */
    @Override
    public @Nonnull Result filter(final @Nonnull Logger logger, final @Nonnull Level level, final @Nonnull Marker marker, final Object msg, final Throwable t) {
        return this.handle(msg.toString());
    }

    /**
     * Attempts to hide the hiddenExecutors from the chat logger.
     *
     * @param logger - The logger handling the message.
     * @param level  - The level of execution.
     * @param marker - The filter marker.
     * @param msg    - The message caught by the filter.
     * @param params - The filter parameters.
     * @return The result of the filter.
     */
    @Override
    public @Nonnull Result filter(final @Nonnull Logger logger, final @Nonnull Level level, final @Nonnull Marker marker, final String msg, final Object... params) {
        return this.handle(msg);
    }

    /**
     * Adds an executor to be hidden from chat logging.
     *
     * @param log     - The log identifier.
     * @param logList - The executor to be hidden.
     */
    public void addHidden(final @Nonnull String log, final @Nonnull ArrayList<String> logList) {
        this.hiddenExecutors.put(log, logList);
    }

    /**
     * Gets the currently hiddenExecutors HashMap.
     *
     * @return The current hiddenExecutors HashMap.
     */
    public @Nonnull HashMap<String, ArrayList<String>> getHidden() {
        return this.hiddenExecutors;
    }

    /**
     * Attempts to refresh the LogHandler instance.
     */
    public void refresh() {
        filter = new LogHandler();
        ((LoggerContext) LogManager.getContext(false)).getConfiguration().getLoggerConfig(LogManager.ROOT_LOGGER_NAME).addFilter(filter);
    }
}