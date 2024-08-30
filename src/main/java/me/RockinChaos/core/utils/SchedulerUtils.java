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
import org.bukkit.plugin.Plugin;

import javax.annotation.Nonnull;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

@SuppressWarnings("unused")
public class SchedulerUtils {

    private final static boolean isFolia = ServerUtils.isFolia();
    private final static Object globalScheduler = isFolia ? ReflectionUtils.invokeMethod("getGlobalRegionScheduler", Bukkit.class, Bukkit.getServer()) : null;
    private final static Object asyncScheduler = isFolia ? ReflectionUtils.invokeMethod("getAsyncScheduler", Bukkit.class, Bukkit.getServer()) : null;

    private static final List<Runnable> SINGLE_QUEUE = new ArrayList<>();
    private static boolean SINGLE_ACTIVE = false;

    /**
     * Checks if the current thread is synchronous.
     *
     * @return If the current thread is synchronous.
     */
    public static boolean isMainThread() {
        if (isFolia) {
            try {
                final boolean isRegionThread = Thread.currentThread().getName().contains("Region Scheduler");
                return isRegionThread != (boolean) ReflectionUtils.invokeMethod("isGlobalTickThread", Bukkit.class, Bukkit.getServer());
            } catch (Exception e) {
                ServerUtils.logSevere("{SchedulerUtils (Folia)} Failed to identify if this is not a asynchronous thread.");
                ServerUtils.sendSevereTrace(e);
                return false;
            }
        }
        return Bukkit.isPrimaryThread();
    }

    /**
     * Runs the task on the main thread
     *
     * @param runnable - The task to be performed.
     */
    public static void run(final @Nonnull Runnable runnable) {
        if (Core.getCore().getPlugin().isEnabled()) {
            if (isFolia) {
                try {
                    final Method runMethod = globalScheduler.getClass().getMethod("run", Plugin.class, Consumer.class);
                    runMethod.invoke(globalScheduler, Core.getCore().getPlugin(), (Consumer<?>) task -> runnable.run());
                    return;
                } catch (Exception e) {
                    ServerUtils.logSevere("{SchedulerUtils (Folia)} Failed to run task.");
                    ServerUtils.sendSevereTrace(e);
                }
            }
            Bukkit.getScheduler().runTask(Core.getCore().getPlugin(), runnable);
        }
    }

    /**
     * Runs the task on the main thread.
     *
     * @param runnable - The task to be performed.
     * @param delay    - The ticks to wait before performing the task.
     */
    public static void runLater(final long delay, final @Nonnull Runnable runnable) {
        if (delay <= 0) { run(runnable); return; }
        if (Core.getCore().getPlugin().isEnabled()) {
            if (isFolia) {
                try {
                    final Method runDelayedMethod = globalScheduler.getClass().getMethod("runDelayed", Plugin.class, Consumer.class, long.class);
                    runDelayedMethod.invoke(globalScheduler, Core.getCore().getPlugin(), (Consumer<?>) task -> runnable.run(), delay);
                } catch (Exception e) {
                    ServerUtils.logSevere("{SchedulerUtils (Folia)} Failed to run task later.");
                    ServerUtils.sendSevereTrace(e);
                }
                return;
            }
            Bukkit.getScheduler().runTaskLater(Core.getCore().getPlugin(), runnable, delay);
        }
    }

    /**
     * Runs the repeating task on the main thread.
     *
     * @param runnable - The task to be performed.
     * @param delay    - The ticks to wait before performing the task.
     * @param interval - The interval in which to run the task.
     */
    public static void runRepeatingTask(final long delay, final long interval, final @Nonnull Runnable runnable) {
        if (Core.getCore().getPlugin().isEnabled()) {
            if (isFolia) {
                try {
                    final Method runAtFixedRateMethod = globalScheduler.getClass().getMethod("runAtFixedRate", Plugin.class, Consumer.class, long.class, long.class);
                    runAtFixedRateMethod.invoke(globalScheduler, Core.getCore().getPlugin(), (Consumer<?>) task -> runnable.run(), delay, interval);
                } catch (Exception e) {
                    ServerUtils.logSevere("{SchedulerUtils (Folia)} Failed to run repeating task.");
                    ServerUtils.sendSevereTrace(e);
                }
                return;
            }
            Bukkit.getScheduler().scheduleSyncRepeatingTask(Core.getCore().getPlugin(), runnable, delay, interval);
        }
    }

    /**
     * Runs the task on another thread.
     *
     * @param runnable - The task to be performed.
     */
    public static void runAsync(final @Nonnull Runnable runnable) {
        if (Core.getCore().getPlugin().isEnabled()) {
            if (isFolia) {
                try {
                    final Method runMethod = asyncScheduler.getClass().getMethod("runNow", Plugin.class, Consumer.class);
                    runMethod.invoke(asyncScheduler, Core.getCore().getPlugin(), (Consumer<?>) task -> runnable.run());
                } catch (Exception e) {
                    ServerUtils.logSevere("{SchedulerUtils (Folia)} Failed to run task asynchronously.");
                    ServerUtils.sendSevereTrace(e);
                }
                return;
            }
            Bukkit.getScheduler().runTaskAsynchronously(Core.getCore().getPlugin(), runnable);
        }
    }

    /**
     * Runs the task on another thread.
     *
     * @param runnable - The task to be performed.
     * @param delay    - The ticks to wait before performing the task.
     */
    public static void runAsyncLater(final long delay, final @Nonnull Runnable runnable) {
        if (delay <= 0) { runAsync(runnable); return; }
        if (Core.getCore().getPlugin().isEnabled()) {
            if (isFolia) {
                try {
                    Method runDelayedMethod = asyncScheduler.getClass().getMethod("runDelayed", Plugin.class, Consumer.class, long.class, TimeUnit.class);
                    runDelayedMethod.invoke(asyncScheduler, Core.getCore().getPlugin(), (Consumer<?>) task -> runnable.run(), StringUtils.ticksToMillis(delay), TimeUnit.MILLISECONDS);
                } catch (Exception e) {
                    ServerUtils.logSevere("{SchedulerUtils (Folia)} Failed to run task later asynchronously.");
                    ServerUtils.sendSevereTrace(e);
                }
                return;
            }
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
    public static void runAsyncTimer(final long delay, final long interval, final @Nonnull Runnable runnable) {
        if (Core.getCore().getPlugin().isEnabled()) {
            if (isFolia) {
                try {
                    final Method runAtFixedRateMethod = asyncScheduler.getClass().getMethod("runAtFixedRate", Plugin.class, Consumer.class, long.class, long.class, TimeUnit.class);
                    runAtFixedRateMethod.invoke(asyncScheduler, Core.getCore().getPlugin(), (Consumer<?>) task -> runnable.run(), StringUtils.ticksToMillis(delay), StringUtils.ticksToMillis(interval), TimeUnit.MILLISECONDS);
                } catch (Exception e) {
                    ServerUtils.logSevere("{SchedulerUtils (Folia)} Failed to run task timer asynchronously.");
                    ServerUtils.sendSevereTrace(e);
                }
                return;
            }
            Bukkit.getScheduler().runTaskTimerAsynchronously(Core.getCore().getPlugin(), runnable, delay, interval);
        }
    }


    /**
     * Runs the task repeating on the another thread.
     *
     * @param runnable - The task to be performed.
     * @param delay    - The ticks to wait before performing the task (the ticks to wait before running the task for the first time).
     * @param interval - The interval in which to run the task (the ticks to wait between runs).
     * @return The repeating task identifier.
     */
    public static int runAsyncAtInterval(final long delay, final long interval, final @Nonnull Runnable runnable) {
        if (Core.getCore().getPlugin().isEnabled()) {
            if (isFolia) {
                try {
                    final Method runAtFixedRateMethod = asyncScheduler.getClass().getMethod("runAtFixedRate", Plugin.class, Consumer.class, long.class, long.class, TimeUnit.class);
                    final Object uniqueTask = runAtFixedRateMethod.invoke(asyncScheduler, Core.getCore().getPlugin(), (Consumer<?>) task -> runnable.run(), StringUtils.ticksToMillis(delay), StringUtils.ticksToMillis(interval), TimeUnit.MILLISECONDS);
                    return (int) uniqueTask.getClass().getMethod("getTaskId").invoke(uniqueTask);
                } catch (Exception e) {
                    ServerUtils.logSevere("{SchedulerUtils (Folia)} Failed to run interval task asynchronously.");
                    ServerUtils.sendSevereTrace(e);
                }
            }
            return Bukkit.getScheduler().runTaskTimerAsynchronously(Core.getCore().getPlugin(), runnable, delay, interval).getTaskId();
        }
        return 0;
    }

    /**
     * Runs the task on another thread without duplication.
     *
     * @param runnable - The task to be performed.
     */
    public static void runSingleAsync(final @Nonnull Runnable runnable) {
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
                final Consumer<Runnable> runnableConsumer = task -> {
                    runnable.run();
                    SINGLE_QUEUE.remove(runnable);
                    cycleAsync();
                };
                if (isFolia) {
                    try {
                        final Method runNowMethod = asyncScheduler.getClass().getMethod("runNow", Plugin.class, Consumer.class);
                        runNowMethod.invoke(asyncScheduler, Core.getCore().getPlugin(), runnableConsumer);
                    } catch (Exception e) {
                        ServerUtils.logSevere("{SchedulerUtils (Folia)} Failed to run single task asynchronously.");
                        ServerUtils.sendSevereTrace(e);
                    }
                    return;
                }
                Bukkit.getScheduler().runTaskAsynchronously(Core.getCore().getPlugin(), () -> runnableConsumer.accept(runnable));
            } else {
                SINGLE_ACTIVE = false;
            }
        }
    }

    /**
     * Cancels the scheduled task for the specified taskId.
     *
     * @param taskId - The scheduled task to be canceled.
     */
    public static void cancelTask(final int taskId) {
        if (isFolia) {
            try {
                final Method cancelTaskMethod = globalScheduler.getClass().getMethod("cancelTask", int.class);
                final Method cancelAsyncTaskMethod = asyncScheduler.getClass().getMethod("cancelTask", int.class);
                cancelTaskMethod.invoke(globalScheduler, taskId);
                cancelAsyncTaskMethod.invoke(asyncScheduler, taskId);
            } catch (Exception e) {
                ServerUtils.logSevere("{SchedulerUtils (Folia)} Failed to cancel scheduled task with the id " + taskId + ".");
                ServerUtils.sendSevereTrace(e);
            }
            return;
        }
        Bukkit.getScheduler().cancelTask(taskId);
    }

    /**
     * Cancels all scheduled tasks for the plugin.
     */
    public static void cancelTasks() {
        if (isFolia) {
            try {
                final Method cancelTasksMethod = globalScheduler.getClass().getMethod("cancelTasks", Plugin.class);
                final Method cancelAsyncTasksMethod = asyncScheduler.getClass().getMethod("cancelTasks", Plugin.class);
                cancelTasksMethod.invoke(globalScheduler, Core.getCore().getPlugin());
                cancelAsyncTasksMethod.invoke(asyncScheduler, Core.getCore().getPlugin());
            } catch (Exception e) {
                ServerUtils.logSevere("{SchedulerUtils (Folia)} Failed to cancel all scheduled tasks.");
                ServerUtils.sendSevereTrace(e);
            }
            return;
        }
        Bukkit.getScheduler().cancelTasks(Core.getCore().getPlugin());
    }
}