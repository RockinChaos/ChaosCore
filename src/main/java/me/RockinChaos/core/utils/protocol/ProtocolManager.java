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
package me.RockinChaos.core.utils.protocol;

import io.netty.channel.Channel;
import me.RockinChaos.core.Core;
import me.RockinChaos.core.handlers.PlayerHandler;
import me.RockinChaos.core.utils.SchedulerUtils;
import me.RockinChaos.core.utils.ServerUtils;
import me.RockinChaos.core.utils.protocol.events.*;
import me.RockinChaos.core.utils.protocol.packet.PacketContainer;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.plugin.AuthorNagException;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredListener;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.logging.Level;

@SuppressWarnings("unused")
public class ProtocolManager {

    private static TinyProtocol protocol;
    private static int permissionTask;

    /**
     * Handles both server side and client side protocol packets.
     */
    public static void handleProtocols() {
        if (protocol != null) {
            closeProtocol();
        }
        protocol = new TinyProtocol(Core.getCore().getPlugin()) {

            /**
             * Handles all incoming client packets.
             *
             * @param player - the player tied to the packet.
             * @param channel - the channel the packet was called on.
             * @param packet - the packet object.
             */
            @Override
            public Object onPacketInAsync(final Player player, final Channel channel, final Object packet) {
                if (packet != null && manageEvents(player, packet.getClass().getSimpleName(), protocol.getContainer(packet))) {
                    return null;
                }
                return super.onPacketInAsync(player, channel, packet);
            }

            /**
             * Handles all outgoing server packets.
             *
             * @param player - the player tied to the packet.
             * @param channel - the channel the packet was called on.
             * @param packet - the packet object.
             */
            @Override
            public Object onPacketOutAsync(final Player player, final Channel channel, final Object packet) {
                return packet;
            }
        };
    }

    /**
     * Handles the repeating task to check all players on the server to see
     * if their permissions have changed.
     */
    public static void handlePermissions() {
        if (permissionTask == 0) {
            final Map<Player, List<String>> playerPermissions = new HashMap<>();
            permissionTask = SchedulerUtils.runAsyncAtInterval(5L, 0L, () -> {
                PlayerHandler.forOnlinePlayers(player -> {
                    final Iterator<PermissionAttachmentInfo> iterator = player.getEffectivePermissions().iterator();
                    final List<String> currentPermissions = new ArrayList<>();
                    while (iterator.hasNext()) {
                        final PermissionAttachmentInfo it = iterator.next();
                        currentPermissions.add(it.getPermission() + ":" + it.getValue());
                    }
                    if (!playerPermissions.containsKey(player) || (playerPermissions.containsKey(player) && !(new HashSet<>(playerPermissions.get(player)).containsAll(currentPermissions)))) {
                        final List<String> changedPermissions = new ArrayList<>(currentPermissions);
                        if (playerPermissions.containsKey(player)) {
                            changedPermissions.removeAll(playerPermissions.get(player));
                            callEvent(new PermissionChangedEvent(player, changedPermissions));
                        }
                        playerPermissions.put(player, new ArrayList<>(currentPermissions));
                    }
                });
            });
        }
    }

    /**
     * Handles the custom plugin events corresponding to their packet names.
     *
     * @param player          - the player tied to the packet.
     * @param packetName      - the packet name.
     * @param packetContainer - the PacketContainer.
     */
    public static boolean manageEvents(final @Nonnull Player player, final @Nonnull String packetName, final @Nonnull PacketContainer packetContainer) {
        try {
            if (packetName.equalsIgnoreCase("PacketPlayInPickItem")) {
                final PlayerPickItemEvent PickItem = new PlayerPickItemEvent(player, player.getInventory());
                callEvent(PickItem);
                return PickItem.isCancelled();
            } else if (packetName.equalsIgnoreCase("PacketPlayInAutoRecipe")) {
                final PlayerAutoCraftEvent AutoCraft = new PlayerAutoCraftEvent(player, player.getOpenInventory().getTopInventory(), (boolean) packetContainer.read(2).getData());
                callEvent(AutoCraft);
                return AutoCraft.isCancelled();
            } else if (packetName.equalsIgnoreCase("PacketPlayInCloseWindow")) {
                final InventoryCloseEvent CloseInventory = new InventoryCloseEvent(player.getOpenInventory());
                callEvent(CloseInventory);
                return CloseInventory.isCancelled();
            } else if (packetName.equalsIgnoreCase("PacketPlayInCustomPayload")) {
                if (packetContainer.read(0).getData().toString().equalsIgnoreCase("MC|ItemName") && player.getOpenInventory().getType().name().equalsIgnoreCase("ANVIL")) {
                    final Object UnbufferedPayload = packetContainer.read(1).getData();
                    final String renameText = (String) UnbufferedPayload.getClass().getMethod(ServerUtils.hasSpecificUpdate("1_9") ? "e" : "c", int.class).invoke(UnbufferedPayload, 31);
                    final PrepareAnvilEvent PrepareAnvil = new PrepareAnvilEvent(player.getOpenInventory(), renameText);
                    callEvent(PrepareAnvil);
                    return PrepareAnvil.isCancelled();
                }
            } else if (packetName.equalsIgnoreCase("PacketPlayInWindowClick")) {
                if (packetContainer.read(5).getData().toString().equalsIgnoreCase("QUICK_CRAFT")) {
                    final int slot = (ServerUtils.hasSpecificUpdate("1_17") ? (int) packetContainer.read(3).getData() : (int) packetContainer.read(1).getData());
                    if (slot >= 0) {
                        final PlayerCloneItemEvent CloneItem = new PlayerCloneItemEvent(player, slot, ClickType.MIDDLE);
                        callEvent(CloneItem);
                        return CloneItem.isCancelled();
                    }
                }
            }
        } catch (Exception ignored) {
        }
        return false;
    }

    /**
     * Allows an event to be called on a different Async Thread.
     * Functions the same as PluginManager.callEvent(event);
     *
     * @param event - The event to be triggered.
     */
    public static void callEvent(final @Nonnull Event event) {
        final HandlerList handlers = event.getHandlers();
        final RegisteredListener[] listeners = handlers.getRegisteredListeners();
        for (final RegisteredListener registration : listeners) {
            if (!registration.getPlugin().isEnabled()) {
                continue;
            }
            try {
                registration.getPlugin();
                registration.callEvent(event);
            } catch (AuthorNagException e) {
                final Plugin plugin = registration.getPlugin();
                if (plugin.isNaggable()) {
                    plugin.setNaggable(false);
                    Core.getCore().getPlugin().getLogger().log(Level.SEVERE, String.format("Nag author(s): '%s' of '%s' about the following: %s", plugin.getDescription().getAuthors(), plugin.getDescription().getFullName(), e.getMessage()));
                }
            } catch (Throwable e) {
                Core.getCore().getPlugin().getLogger().log(Level.SEVERE, "Could not pass event " + event.getEventName() + " to " + registration.getPlugin().getDescription().getFullName(), e);
            }
        }
    }

    /**
     * Closes the currently open protocol handler(s).
     */
    public static void closeProtocol() {
        if (protocol != null) {
            protocol.close();
        }
    }

    /**
     * Checks if the protocol handler(s) are open.
     *
     * @return If the protocol handler(s) are open.
     */
    public static boolean isDead() {
        return (protocol == null);
    }
}