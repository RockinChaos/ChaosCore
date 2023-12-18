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

import me.RockinChaos.core.Core;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;

@SuppressWarnings("unused")
public class PermissionsHandler {

    /**
     * Checks if the sender has permission.
     *
     * @param sender     - The entity that is having their permissions checked.
     * @param permission - The permission the sender is expected to have.
     * @return If the entity has the proper permission.
     */
    public static boolean hasPermission(final @Nonnull CommandSender sender, final @Nonnull String permission) {
        if (sender.hasPermission(permission) || sender.hasPermission(Core.getCore().getPlugin().getName().toLowerCase() + ".*") || sender.hasPermission(Core.getCore().getPlugin().getName().toLowerCase() + ".all") || isDeveloper(sender) || (sender instanceof ConsoleCommandSender)) {
            return true;
        } else if (!Core.getCore().getConfig("config.yml").getBoolean("Permissions.Commands-OP") && sender.isOp()) {
            for (String corePermission : Core.getCore().getData().getPermissions()) {
                if (permission.equalsIgnoreCase(corePermission)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Checks if the permission is a custom permission.
     * This fetches the proper permission node, so if a custom permission is not
     * defined for the item, it returns the default permission node itemjoin.world.itemname.
     *
     * @param permissionNode - The custom permission node.
     * @return The permission node of the item.
     */
    public static @Nonnull String customPermissions(final String permissionNode, final String permission) {
        if (permissionNode != null) {
            return permissionNode;
        }
        return Core.getCore().getPlugin().getName().toLowerCase() + "." + permission;
    }

    /**
     * If Debugging Mode is enabled, the plugin developer will be allowed to execute ONLY these plugins commands for help and support purposes.
     *
     * @param sender - The entity executing the plugin command.
     * @return If the command sender is the developer of the plugin.
     */
    private static boolean isDeveloper(final CommandSender sender) {
        if (Core.getCore().getData().debugEnabled()) {
            if (sender instanceof Player) {
                return ((Player) sender).getUniqueId().toString().equalsIgnoreCase("ad6e8c0e-6c47-4e7a-a23d-8a2266d7baee");
            }
        }
        return false;
    }

    /**
     * Checks if the specific permission path is enabled.
     *
     * @return If the specified permission path is enabled.
     */
    public static boolean permissionEnabled(final @Nonnull String permission) {
        return Core.getCore().getConfig("config.yml").getBoolean(permission);
    }
}