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
package me.RockinChaos.core.utils;

import me.RockinChaos.core.Core;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unused")
public class SchedulerUtils {

    private static final List<Runnable> SINGLE_QUEUE = new ArrayList<>();
    private static Boolean SINGLE_ACTIVE = false;

    /**
     * Runs the task on the main thread
     *
     * @param runnable - The task to be performed.
     */
    public static void run(final Runnable runnable) {
        if (Core.getCore().getPlugin().isEnabled()) {
            Bukkit.getScheduler().runTask(Core.getCore().getPlugin(), runnable);
        }
    }

    /**
     * Runs the task on the main thread.
     *
     * @param runnable - The task to be performed.
     * @param delay    - The ticks to wait before performing the task.
     */
    public static void runLater(final long delay, final Runnable runnable) {
        if (Core.getCore().getPlugin().isEnabled()) {
            Bukkit.getScheduler().runTaskLater(Core.getCore().getPlugin(), runnable, delay);
        }
    }

    /**
     * Runs the repeating task on the main thread.
     * @param runnable - The task to be performed.
     * @param delay - The ticks to wait before performing the task.
     * @param interval - The interval in which to run the task.
     */
    public static void runRepeatingTask(final long delay, final long interval, final Runnable runnable) {
        if (Core.getCore().getPlugin().isEnabled()) {
            Bukkit.getScheduler().scheduleSyncRepeatingTask(Core.getCore().getPlugin(), runnable, interval, delay);
        }
    }

    /**
     * Runs the task repeating on the main thread.
     *
     * @param runnable - The task to be performed.
     * @param delay    - The ticks to wait before performing the task.
     * @param interval - The interval in which to run the task.
     * @return The repeating task identifier.
     */
    public static int runAsyncAtInterval(final long delay, final long interval, final Runnable runnable) {
        if (Core.getCore().getPlugin().isEnabled()) {
            return Bukkit.getScheduler().runTaskTimerAsynchronously(Core.getCore().getPlugin(), runnable, interval, delay).getTaskId();
        }
        return 0;
    }

    /**
     * Runs the task on another thread.
     *
     * @param runnable - The task to be performed.
     */
    public static void runAsync(final Runnable runnable) {
        if (Core.getCore().getPlugin().isEnabled()) {
            Bukkit.getScheduler().runTaskAsynchronously(Core.getCore().getPlugin(), runnable);
        }
    }

    /**
     * Runs the task on another thread.
     *
     * @param runnable - The task to be performed.
     * @param delay    - The ticks to wait before performing the task.
     */
    public static void runAsyncLater(final long delay, final Runnable runnable) {
        if (Core.getCore().getPlugin().isEnabled()) {
            Bukkit.getScheduler().runTaskLaterAsynchronously(Core.getCore().getPlugin(), runnable, delay);
        }
    }

    /**
     * Runs the task timer on the another thread.
     *
     * @param runnable - The task to be performed.
     * @param delay    - The ticks to wait before performing the task.
     * @param interval - The interval in which to run the task.
     */
    public static void runAsyncTimer(final long delay, final long interval, final Runnable runnable) {
        if (Core.getCore().getPlugin().isEnabled()) {
            Bukkit.getScheduler().runTaskTimerAsynchronously(Core.getCore().getPlugin(), runnable, interval, delay);
        }
    }

    /**
     * Runs the task on another thread without duplication.
     *
     * @param runnable - The task to be performed.
     */
    public static void runSingleAsync(final Runnable runnable) {
        SINGLE_QUEUE.add(runnable);
        if (!SINGLE_ACTIVE) {
            SINGLE_ACTIVE = true;
            {
                cycleAsync();
            }
        }
    }

    /**
     * Runs the task on another thread without duplication.
     */
    public static void cycleAsync() {
        if (Core.getCore().getPlugin().isEnabled()) {
            if (!SINGLE_QUEUE.isEmpty()) {
                final Runnable runnable = SINGLE_QUEUE.get(0);
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        runnable.run();
                        {
                            SINGLE_QUEUE.remove(runnable);
                            {
                                cycleAsync();
                            }
                        }
                    }
                }.runTaskAsynchronously(Core.getCore().getPlugin());
            } else {
                SINGLE_ACTIVE = false;
            }
        }
    }
}