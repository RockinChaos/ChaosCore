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
import me.RockinChaos.core.handlers.PlayerHandler;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.plugin.SimplePluginManager;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@SuppressWarnings({"unused", "CallToPrintStackTrace"})
public class ServerUtils {

    private static final String packageName = Bukkit.getServer().getClass().getPackage().getName();
    private static final String serverVersion = packageName.substring(packageName.lastIndexOf('.') + 1).replace("_", "").replace("R0", "").replace("R1", "").replace("R2", "").replace("R3", "").replace("R4", "").replace("R5", "").replaceAll("[a-z]", "");
    private static final String serverPreciseVersion = packageName.substring(packageName.lastIndexOf('.') + 1).replace("_", "").replace("R", "").replaceAll("[a-z]", "");
    private static final List<String> errorStatements = new ArrayList<>();
    private static final String devPlayer = (hasSpecificUpdate("1_8") ? "ad6e8c0e-6c47-4e7a-a23d-8a2266d7baee" : "RockinChaos");

    /**
     * Checks if the server is running the specified version.
     *
     * @param versionString - The version to compare against the server version, example: '1_13'.
     * @return If the server version is greater than or equal to the specified version.
     */
    public static boolean hasSpecificUpdate(final String versionString) {
        return Integer.parseInt(serverVersion) >= Integer.parseInt(versionString.replace("_", ""));
    }

    /**
     * Checks if the server is running the specified version.
     *
     * @param versionString - The version to compare against the server version, example: '1_13'.
     * @return If the server version is greater than or equal to the specified version.
     */
    public static boolean hasPreciseUpdate(final String versionString) {
        return Integer.parseInt(serverPreciseVersion) >= Integer.parseInt(versionString.replace("_", ""));
    }

    /**
     * Sends a low priority log message as the plugin header.
     *
     * @param message - The unformatted message text to be sent.
     */
    public static void logInfo(String message) {
        String prefix = "[" + Core.getCore().getPlugin().getName() + "] ";
        message = prefix + message;
        Bukkit.getServer().getLogger().info(message);
    }

    /**
     * Sends a warning message as the plugin header.
     *
     * @param message - The unformatted message text to be sent.
     */
    public static void logWarn(String message) {
        String prefix = "[" + Core.getCore().getPlugin().getName() + "_WARN] ";
        message = prefix + message;
        Bukkit.getServer().getLogger().warning(message);
    }

    /**
     * Sends a developer warning message as the plugin header.
     *
     * @param message - The unformatted message text to be sent.
     */
    public static void logDev(String message) {
        String prefix = "[" + Core.getCore().getPlugin().getName() + "_DEVELOPER] ";
        message = prefix + message;
        Bukkit.getServer().getLogger().warning(message);
    }

    /**
     * Sends a error message as the plugin header.
     *
     * @param message - The unformatted message text to be sent.
     */
    public static void logSevere(String message) {
        String prefix = "[" + Core.getCore().getPlugin().getName() + "_ERROR] ";
        if (message.isEmpty()) {
            message = "";
        }
        Bukkit.getServer().getLogger().severe(prefix + message);
        if (!errorStatements.contains(message)) {
            errorStatements.add(message);
        }
    }

    /**
     * Sends a debug message as a loggable warning as the plugin header.
     *
     * @param message - The unformatted message text to be sent.
     */
    public static void logDebug(String message) {
        if (Core.getCore().getData().debugEnabled()) {
            String prefix = "[" + Core.getCore().getPlugin().getName() + "_DEBUG] ";
            message = prefix + message;
            Bukkit.getServer().getLogger().warning(message);
            Player player = PlayerHandler.getPlayerString(devPlayer);
            if (player != null && player.isOnline()) {
                player.sendMessage(message);
            }
        }
    }

    /**
     * Sends the StackTrace of an Exception if debugging is enabled.
     *
     * @param e - The exception to be sent.
     */
    public static void sendDebugTrace(final Exception e) {
        if (Core.getCore().getData().debugEnabled()) {
            e.printStackTrace();
            Player player = PlayerHandler.getPlayerString(devPlayer);
            if (player != null && player.isOnline()) {
                player.sendMessage(e.toString());
            }
        }
    }

    /**
     * Sends the StackTrace of an Exception if it is Severe.
     *
     * @param e - The exception to be sent.
     */
    public static void sendSevereTrace(final Exception e) {
        e.printStackTrace();
    }

    /**
     * Sends the StackTrace of a Throwable if it is Severe.
     *
     * @param e - The exception to be sent.
     */
    public static void sendSevereThrowable(final Throwable e) {
        e.printStackTrace();
    }

    /**
     * Sends a chat message to the specified sender.
     *
     * @param sender  - The entity to have the message sent.
     * @param message - The unformatted message text to be sent.
     */
    public static void messageSender(final CommandSender sender, String message) {
        String prefix = Core.getCore().getData().getPluginPrefix() + " ";
        message = prefix + message;
        message = ChatColor.translateAlternateColorCodes('&', message);
        if (message.contains("blankmessage") || message.isEmpty()) {
            message = "";
        }
        if (sender instanceof ConsoleCommandSender) {
            message = ChatColor.stripColor(message);
        }
        sender.sendMessage(message);
    }

    /**
     * Sends the current error statements to the online admins.
     *
     * @param player - The Player to have the message sent.
     */
    public static void sendErrorStatements(final Player player) {
        if (player != null && player.isOp() && Core.getCore().getData().ignoreErrors()) {
            SchedulerUtils.runLater(60L, () -> {
                for (String statement : errorStatements) {
                    player.sendMessage(StringUtils.translateLayout(Core.getCore().getData().getPluginPrefix() + " &c" + statement, player));
                }
            });
        } else if (Core.getCore().getData().ignoreErrors()) {
            for (String statement : errorStatements) {
                PlayerHandler.forOnlinePlayers(player_2 -> {
                    if (player_2 != null && player_2.isOp()) {
                        player_2.sendMessage(StringUtils.translateLayout(Core.getCore().getData().getPluginPrefix() + " &c" + statement, player_2));
                    }
                });
            }
        }
    }

    /**
     * Clears the current error statements.
     */
    public static void clearErrorStatements() {
        errorStatements.clear();
    }

    /**
     * Attempts to manually register a PluginCommands list for the plugin instance.
     *
     * @param commands - The PluginCommands to be registered.
     */
    public static void registerCommands(final List<PluginCommand> commands) {
        try {
            CommandMap commandMap = null;
            if (Bukkit.getPluginManager() instanceof SimplePluginManager) {
                Field f = SimplePluginManager.class.getDeclaredField("commandMap");
                f.setAccessible(true);
                commandMap = (CommandMap) f.get(Bukkit.getPluginManager());
            }
            {
                if (commandMap != null) {
                    for (PluginCommand command : commands) {
                        commandMap.register(Core.getCore().getPlugin().getDescription().getName(), command);
                    }
                }
            }
        } catch (Exception e) {
            ServerUtils.sendDebugTrace(e);
        }
    }

    /**
     * Attempts to manually unregister a PluginCommands list for the plugin instance.
     *
     * @param commands - The PluginCommands to be unregistered.
     */
    @SuppressWarnings("unchecked")
    public static void unregisterCommands(final List<PluginCommand> commands) {
        Field commandMap = null;
        Field knownCommands = null;
        try {
            if (Bukkit.getPluginManager() instanceof SimplePluginManager) {
                commandMap = Bukkit.getServer().getClass().getDeclaredField("commandMap");
                commandMap.setAccessible(true);
                knownCommands = SimpleCommandMap.class.getDeclaredField("knownCommands");
                knownCommands.setAccessible(true);
            }
            {
                if (knownCommands != null) {
                    for (PluginCommand command : commands) {
                        ((Map<String, Command>) knownCommands.get(commandMap.get(Bukkit.getServer()))).remove(command.getName());
                        command.unregister((CommandMap) commandMap.get(Bukkit.getServer()));
                    }
                }
            }
        } catch (Exception e) {
            ServerUtils.sendDebugTrace(e);
        }
    }
}