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
package me.RockinChaos.core.utils.api;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import me.RockinChaos.core.Core;
import me.RockinChaos.core.utils.ServerUtils;
import me.RockinChaos.core.utils.StringUtils;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.Messenger;
import org.bukkit.plugin.messaging.PluginMessageListener;

import javax.annotation.Nonnull;

@SuppressWarnings({"unused", "UnstableApiUsage"})
public class BungeeAPI implements PluginMessageListener {

    private static BungeeAPI bungee;
    private final String PLUGIN_CHANNEL = "plugin:cloudsync";
    private boolean detectFailure = false;

    /**
     * Initializes the BungeeCord Listener.
     */
    public BungeeAPI() {
        final Messenger messenger = Core.getCore().getPlugin().getServer().getMessenger();
        if (!messenger.isOutgoingChannelRegistered(Core.getCore().getPlugin(), this.PLUGIN_CHANNEL)) {
            messenger.registerOutgoingPluginChannel(Core.getCore().getPlugin(), this.PLUGIN_CHANNEL);
            messenger.registerOutgoingPluginChannel(Core.getCore().getPlugin(), "BungeeCord");
        }
        if (!messenger.isIncomingChannelRegistered(Core.getCore().getPlugin(), this.PLUGIN_CHANNEL)) {
            messenger.registerIncomingPluginChannel(Core.getCore().getPlugin(), this.PLUGIN_CHANNEL, this);
            messenger.registerIncomingPluginChannel(Core.getCore().getPlugin(), "BungeeCord", this);
        }
    }

    /**
     * Gets the instance of the BungeeAPI.
     *
     * @return The BungeeAPI instance.
     */
    public static BungeeAPI getBungee() {
        if (bungee == null) {
            bungee = new BungeeAPI();
        }
        return bungee;
    }

    /**
     * Sends the specified Player to the specified Server.
     *
     * @param player - The Player switching servers.
     * @param server - The String name of the server that the Player is connecting to.
     */
    public void SwitchServers(final Player player, final String server) {
        final ByteArrayDataOutput out = ByteStreams.newDataOutput();
        try {
            out.writeUTF("Connect");
            out.writeUTF(server);
        } catch (Exception e) {
            ServerUtils.sendDebugTrace(e);
        }
        player.sendPluginMessage(Core.getCore().getPlugin(), "BungeeCord", out.toByteArray());
    }

    /**
     * Executes the BungeeCord Command as the Player instance.
     *
     * @param player  - The Player executing the Bungee Command.
     * @param command - The Bungee Command the Player is executing.
     */
    public void ExecuteCommand(final Player player, final String command) {
        if (StringUtils.containsIgnoreCase(player.getListeningPluginChannels().toString(), "plugin:cloudsync")) {
            final ByteArrayDataOutput out = ByteStreams.newDataOutput();
            try {
                out.writeUTF(player.getName());
                out.writeUTF(command);
            } catch (Exception e) {
                ServerUtils.sendDebugTrace(e);
            }
            player.sendPluginMessage(Core.getCore().getPlugin(), this.PLUGIN_CHANNEL, out.toByteArray());
        } else {
            if (!this.detectFailure) {
                ServerUtils.logSevere("Tried to execute the Bungee command /" + command + " but, CloudSync was not detected on your BungeeCord server.");
                this.detectFailure = true;
            }
        }
    }

    /**
     * Sends the Server Switch message when attempting to switch servers.
     *
     * @param channel - The channel receiving the message.
     * @param player  - The Player switching servers.
     * @param message - The message being sent to the Player.
     */
    @Override
    public void onPluginMessageReceived(final String channel, @Nonnull final Player player, @Nonnull final byte[] message) {
        if (!channel.equals(this.PLUGIN_CHANNEL)) {
            return;
        }
        final ByteArrayDataInput in = ByteStreams.newDataInput(message);
        final String subChannel = in.readUTF();
        if (subChannel.equals("ConnectOther") || subChannel.equals("Connect")) {
            player.sendMessage(subChannel + " " + in.readByte());
        }
    }

    /**
     * Attempts to refresh the BungeeAPI instance.
     */
    public void refresh() {
        bungee = new BungeeAPI();
    }
}