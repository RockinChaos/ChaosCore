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

import com.google.gson.JsonArray;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import me.RockinChaos.core.Core;
import me.RockinChaos.core.utils.SchedulerUtils;
import me.RockinChaos.core.utils.ServerUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.management.ManagementFactory;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

@SuppressWarnings("unused")
public class PasteAPI {
    private final String pasteData;

    /**
     * Creates a new PasteAPI instance.
     *
     * @param console     - The executor of the paste.
     * @param warnPlugins - An ArrayList of potential plugins that conflict with the plugin.
     * @param files       - A map of files to be added to the paste, String1 being the file name, String2 being the content.
     */
    public PasteAPI(final @Nonnull CommandSender console, final @Nullable List<String> warnPlugins, final @Nullable Map<String, String> files) {
        final Player sender = (console instanceof Player ? (Player) console : null);
        final JsonObject dump = new JsonObject();

        final JsonObject meta = new JsonObject();
        meta.addProperty("timestamp", Instant.now().toEpochMilli());
        meta.addProperty("sender", sender != null ? sender.getName() : null);
        meta.addProperty("senderUuid", sender != null ? sender.getUniqueId().toString() : null);
        dump.add("meta", meta);

        final JsonObject pluginData = new JsonObject();
        pluginData.addProperty("name", Core.getCore().getPlugin().getName());
        pluginData.addProperty("version", Core.getCore().getPlugin().getDescription().getVersion());
        pluginData.addProperty("dev", Core.getCore().getUpdater().isDevVersion());
        dump.add("plugin-data", pluginData);

        final JsonObject serverData = new JsonObject();
        serverData.addProperty("bukkit-version", Bukkit.getBukkitVersion());
        serverData.addProperty("server-version", Bukkit.getVersion());
        serverData.addProperty("server-brand", Bukkit.getName());
        serverData.addProperty("online-mode", Bukkit.getOnlineMode() ? "YES" : "NO");
        final JsonObject supportStatus = new JsonObject();
        supportStatus.addProperty("supported", !Core.getCore().getUpdater().updateNeeded(console, false).updateNeeded);
        serverData.add("support-status", supportStatus);
        dump.add("server-data", serverData);

        final JsonObject environment = new JsonObject();
        environment.addProperty("java-version", System.getProperty("java.version"));
        environment.addProperty("operating-system", System.getProperty("os.name"));
        long uptime = ManagementFactory.getRuntimeMXBean().getUptime();
        environment.addProperty("uptime", String.format("%02d:%02d:%02d:%02d", TimeUnit.MILLISECONDS.toDays(uptime), TimeUnit.MILLISECONDS.toHours(uptime), TimeUnit.MILLISECONDS.toMinutes(uptime) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(uptime)), TimeUnit.MILLISECONDS.toSeconds(uptime) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(uptime)))); // still not working/accurate...
        environment.addProperty("allocated-memory", (Runtime.getRuntime().totalMemory() / 1024 / 1024) + "MB");
        dump.add("environment", environment);

        final JsonArray plugins = new JsonArray();
        final ArrayList<Plugin> alphabetical = new ArrayList<>();
        Collections.addAll(alphabetical, Bukkit.getPluginManager().getPlugins());
        alphabetical.sort(Comparator.comparing(o -> o.getName().toUpperCase(Locale.ENGLISH)));
        for (final Plugin plugin : alphabetical) {
            final JsonObject pluginInfo = new JsonObject();
            final PluginDescriptionFile info = plugin.getDescription();
            final String name = info.getName();

            pluginInfo.addProperty("name", name);
            pluginInfo.addProperty("version", info.getVersion());
            pluginInfo.addProperty("description", info.getDescription());
            pluginInfo.addProperty("main", info.getMain());
            pluginInfo.addProperty("enabled", plugin.isEnabled());
            pluginInfo.addProperty("official", plugin == Core.getCore().getPlugin() || Core.getCore().getData().getOfficialPlugins().contains(name));
            pluginInfo.addProperty("unsupported", warnPlugins != null && warnPlugins.contains(name));

            final JsonArray authors = new JsonArray();
            for (final String author : info.getAuthors()) {
                authors.add(author == null ? JsonNull.INSTANCE : new JsonPrimitive(author));
            }
            pluginInfo.add("authors", authors);
            plugins.add(pluginInfo);
        }
        dump.add("plugins", plugins);

        final JsonObject jsonFiles = new JsonObject();
        if (files != null) {
            for (String file : files.keySet()) {
                jsonFiles.addProperty(file, files.get(file));
            }
        }
        dump.add("files", jsonFiles);
        this.pasteData = dump.toString();
    }

    /**
     * Attempts to get a successful paste connection.
     *
     * @return The successful paste connection.
     */
    private @Nonnull HttpURLConnection getHttpURLConnection(@Nonnull final String postOptions) throws IOException {
        final byte[] postBytes = postOptions.getBytes(StandardCharsets.UTF_8);
        final HttpURLConnection connection = (HttpURLConnection) new URL("https://paste.craftationgaming.com/documents").openConnection();
        connection.setDoOutput(true);
        connection.setFixedLengthStreamingMode(postBytes.length);
        connection.setRequestMethod("POST");
        connection.setRequestProperty("User-Agent", "Mozilla/5.0");
        connection.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
        connection.setConnectTimeout(5000);
        connection.setReadTimeout(5000);
        connection.connect();
        try (OutputStream os = connection.getOutputStream()) {
            os.write(postBytes);
        }
        return connection;
    }

    /**
     * Attempts to get a successful paste URL.
     * This is run asynchronously to prevent the main thread from hanging.
     *
     * @param callback - The successful paste URL result upon completion, this may be null.
     */
    public void getPaste(@Nonnull final Consumer<String> callback) {
        SchedulerUtils.runAsync(() -> {
            try {
                final HttpURLConnection connection = getHttpURLConnection(this.pasteData);
                final BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                final StringBuilder response = new StringBuilder();
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();
                connection.disconnect();
                final JSONObject objectReader = (JSONObject) JSONValue.parseWithException(response.toString());
                final String pasteKey = objectReader.get("key").toString();
                callback.accept("https://ci.craftationgaming.com/dump?id=" + pasteKey);
            } catch (Exception e) {
                ServerUtils.logSevere("{PasteAPI} A severe error has occurred which has prevented the paste URL from generating.");
                ServerUtils.sendSevereTrace(e);
                callback.accept(null);
            }
        });
    }
}