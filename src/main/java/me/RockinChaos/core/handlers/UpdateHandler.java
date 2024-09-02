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
import me.RockinChaos.core.utils.ChatComponent;
import me.RockinChaos.core.utils.SchedulerUtils;
import me.RockinChaos.core.utils.ServerUtils;
import me.RockinChaos.core.utils.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import javax.annotation.Nonnull;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.util.Collection;

@SuppressWarnings("unused")
public class UpdateHandler {
    private static UpdateHandler updater;
    private final String NAME;
    private final String HOST;
    private final String DEV_HOST;
    private String latestVersion;
    private final String versionExact;
    private final String localeVersion;
    private final boolean betaVersion;
    private final boolean devVersion;
    private final String buildNumber;
    private String latestDev;
    private String latestBuild;
    private String artifactPath;
    private final File jarRef;
    private final boolean updatesAllowed;

    /**
     * Initializes the UpdateHandler and Checks for Updates upon initialization.
     */
    public UpdateHandler(final @Nonnull JavaPlugin plugin, final @Nonnull File pluginFile, final boolean updatesAllowed) {
        this.NAME = plugin.getName();
        this.jarRef = pluginFile;
        this.HOST = "https://api.github.com/repos/RockinChaos/" + plugin.getName().toLowerCase() + "/releases/latest";
        this.DEV_HOST = "https://ci-dev.craftationgaming.com/job/" + plugin.getName() + "/lastSuccessfulBuild";
        this.versionExact = plugin.getDescription().getVersion();
        this.localeVersion = this.versionExact.split("-")[0];
        this.betaVersion = this.versionExact.contains("-SNAPSHOT") || this.versionExact.contains("-EXPERIMENTAL") || this.versionExact.contains("-BETA") || this.versionExact.contains("-ALPHA");
        this.devVersion = this.localeVersion.equals("${project.version}");
        this.buildNumber = this.versionExact.split("-b")[1];
        this.updatesAllowed = updatesAllowed;
        SchedulerUtils.runAsync(() -> {
            this.checkUpdates(plugin.getServer().getConsoleSender(), true);
        });
    }

    /**
     * Gets the instance of the UpdateHandler.
     *
     * @param plugin         - the plugin having its update checked.
     * @param pluginFile     - The jar file directory.
     * @param updatesAllowed - If checking for updates should be allowed to nag.
     * @return The UpdateHandler instance.
     */
    public static @Nonnull UpdateHandler getUpdater(final @Nonnull JavaPlugin plugin, final @Nonnull File pluginFile, final boolean updatesAllowed) {
        if (updater == null) {
            updater = new UpdateHandler(plugin, pluginFile, updatesAllowed);
        }
        return updater;
    }

    /**
     * If the GitHub host has an available update, attempts to download the jar file.
     * Downloads and write the new data to the plugin jar file.
     *
     * @param sender - The executor of the update checking.
     */
    public void forceUpdates(final @Nonnull CommandSender sender) {
        final Update update = this.updateNeeded(sender, true);
        if (update.updateNeeded) {
            ServerUtils.messageSender(sender, "&aAn update has been found!", true);
            String updateSuccess;
            String uri;
            if (update == Update.BETA) {
                ServerUtils.messageSender(sender, "&aAttempting to update from " + "&ev" + this.versionExact + " &ato the new " + "&ev" + this.latestDev + "&a.", true);
                updateSuccess = this.latestDev;
                uri = this.DEV_HOST + "/artifact/" +  this.artifactPath + "?_=" + System.currentTimeMillis();
            } else {
                ServerUtils.messageSender(sender, "&aAttempting to update from " + "&ev" + this.versionExact + " &ato the new " + "&ev" + this.latestVersion + "-RELEASE" + "&a.", true);
                updateSuccess = this.latestVersion + "-RELEASE";
                uri = this.HOST.replace("repos/", "").replace("api.", "").replace("latest", "download/" + "v" + this.latestVersion + "/" + this.NAME.toLowerCase() + ".jar") + "?_=" + System.currentTimeMillis();
            }
            final File upgradeFile = new File(Core.getCore().getPlugin().getDataFolder() + "/" + this.NAME + ".jar" + ".tmp");
            try {
                final HttpURLConnection httpConnection = (HttpURLConnection) new URL(uri).openConnection();
                httpConnection.setRequestProperty("User-Agent", "Mozilla/5.0...");
                httpConnection.setConnectTimeout(15000);
                httpConnection.setReadTimeout(15000);
                final int BYTE_SIZE = 2048;
                final long hostFileSize = httpConnection.getContentLength();
                if (hostFileSize <= 0) {
                    throw new Exception("Invalid file size from the host server.");
                }
                try (final BufferedInputStream in = new BufferedInputStream(httpConnection.getInputStream());
                     final FileOutputStream fos = new FileOutputStream(upgradeFile);
                     final BufferedOutputStream bout = new BufferedOutputStream(fos, BYTE_SIZE)) {
                    final String progressBar = "&a::::::::::::::::::::::::::::::";
                    int currentProgress = -1;
                    final byte[] data = new byte[BYTE_SIZE];
                    long fetchedSize = 0;
                    int bytesRead;
                    while ((bytesRead = in.read(data, 0, BYTE_SIZE)) >= 0) {
                        bout.write(data, 0, bytesRead);
                        fetchedSize += bytesRead;
                        final int updateProgress = (int) (((double) fetchedSize / (double) hostFileSize) * 30);
                        if ((((fetchedSize * 100) / hostFileSize) % 25) == 0 && updateProgress > 10) {
                            if (currentProgress != updateProgress) {
                                ServerUtils.messageSender(sender, "&c" + progressBar.substring(0, updateProgress + 2), true);
                            }
                            currentProgress = updateProgress;
                        }
                    }
                    bout.flush();
                }
                if (upgradeFile.length() != hostFileSize) {
                    throw new Exception("Downloaded file size does not match expected size.");
                }
                if (!upgradeFile.renameTo(jarRef)) {
                    try (final InputStream in = Files.newInputStream(upgradeFile.toPath());
                         final OutputStream out = Files.newOutputStream(jarRef.toPath())) {

                        final byte[] buffer = new byte[BYTE_SIZE];
                        int bytesRead;
                        while ((bytesRead = in.read(buffer)) != -1) {
                            out.write(buffer, 0, bytesRead);
                        }
                    }
                    if (!upgradeFile.delete()) {
                        ServerUtils.logSevere("Failed to delete upgrade file " + upgradeFile.getAbsolutePath());
                    }
                }
                ServerUtils.messageSender(sender, "&aSuccessfully updated to &ev" + updateSuccess + "&a!", true);
                ServerUtils.messageSender(sender, "&aYou must restart your server for this to take effect.", true);
            } catch (Exception e) {
                ServerUtils.messageSender(sender, "&cAn error has occurred while trying to update the plugin " + jarRef.getName() + ".", true);
                ServerUtils.messageSender(sender, "&cPlease try again later. If you continue to see this, please contact the plugin developer.", true);
                ServerUtils.logSevere("An error has occurred while trying to update the plugin " + jarRef.getName() + ".");
                ServerUtils.sendDebugTrace(e);
                if (upgradeFile.exists()) {
                    if (!upgradeFile.delete()) {
                        ServerUtils.logSevere("Failed to delete upgrade file " + upgradeFile.getAbsolutePath());
                    }
                }
            }
        } else if (this.updatesAllowed) {
            if (this.betaVersion) {
                ServerUtils.messageSender(sender, "&aYou are running a SNAPSHOT!", true);
                ServerUtils.messageSender(sender, "&aIf you find any bugs please report them!", true);
            }
            ServerUtils.messageSender(sender, "&aYou are up to date!", true);
        }
    }

    /**
     * Checks to see if an update is required, notifying the console window and online op players.
     *
     * @param sender  - The executor of the update checking.
     * @param messages - If message should be sent.
     */
    public void checkUpdates(final @Nonnull CommandSender sender, final boolean messages) {
        final Update update = this.updateNeeded(sender, messages);
        if (update.updateNeeded && this.updatesAllowed) {
            if (update == Update.BETA) {
                ServerUtils.messageSender(sender, "&cYour current version: &bv" + this.versionExact, true);
                ServerUtils.messageSender(sender, "&cA new snapshot build is available: " + "&av" + this.latestDev, true);
                ServerUtils.messageSender(sender, "&aGet it from: " + this.DEV_HOST, true);
                ServerUtils.messageSender(sender, "&aIf you wish to auto update, please type /" + this.NAME + " Upgrade", true);
            } else {
                if (this.betaVersion) {
                    ServerUtils.messageSender(sender, "&cYour current version: &bv" + this.localeVersion + "-SNAPSHOT", true);
                    ServerUtils.messageSender(sender, "&cThis &bSNAPSHOT &cis outdated and a release version is now available.", true);
                } else {
                    ServerUtils.messageSender(sender, "&cYour current version: &bv" + this.localeVersion + "-RELEASE", true);
                }
                ServerUtils.messageSender(sender, "&cA new version is available: " + "&av" + this.latestVersion + "-RELEASE", true);
                ServerUtils.messageSender(sender, "&aGet it from: https://github.com/RockinChaos/" + this.NAME.toLowerCase() + "/releases/latest", true);
                ServerUtils.messageSender(sender, "&aIf you wish to auto update, please type /" + this.NAME + " Upgrade", true);
            }
            this.sendNotifications(update);
        } else if (this.updatesAllowed) {
            if (this.betaVersion) {
                ServerUtils.messageSender(sender, "&aYou are running a SNAPSHOT!", true);
                ServerUtils.messageSender(sender, "&aIf you find any bugs please report them!", true);
            } else if (this.devVersion) {
                ServerUtils.messageSender(sender, "&aYou are running a DEVELOPER SNAPSHOT!", true);
                ServerUtils.messageSender(sender, "&aIf you find any bugs please report them!", true);
                ServerUtils.messageSender(sender, "&aYou will not receive any updates requiring you to manually update.", true);
            }
            ServerUtils.messageSender(sender, "&aYou are up to date!", true);
        }
    }

    /**
     * Directly checks to see if the GitHub host has an update available.
     *
     * @param sender  - The executor of the update checking.
     * @param messages - If message should be sent.
     * @return If an update is needed.
     */
    public Update updateNeeded(final @Nonnull CommandSender sender, final boolean messages) {
        if (this.updatesAllowed) {
            if (messages) {
                ServerUtils.messageSender(sender, "&aChecking for updates...", true);
            }
            try {
                URLConnection connection = new URL(this.HOST + "?_=" + System.currentTimeMillis()).openConnection();
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String JsonString = StringUtils.toString(reader);
                JSONObject objectReader = (JSONObject) JSONValue.parseWithException(JsonString);
                String gitVersion = objectReader.get("tag_name").toString();
                reader.close();
                if (gitVersion.length() <= 7) {
                    this.latestVersion = gitVersion.replaceAll("[a-z]", "").replace("-SNAPSHOT", "").replace("-BETA", "").replace("-ALPHA", "").replace("-RELEASE", "");
                    int latestNumber = !this.devVersion ? Integer.parseInt(this.latestVersion.replace(".", "")) : 0;
                    int localeNumber = !this.devVersion ? Integer.parseInt(this.localeVersion.replace(".", "")) : 0;
                    if (this.devVersion) {
                        return Update.DEV;
                    } else if (latestNumber > localeNumber
                            || (this.betaVersion && latestNumber == localeNumber)) {
                        return Update.RELEASE;
                    } else if (this.betaVersion) {
                        try {
                            URLConnection devConnection = new URL(this.DEV_HOST + "/api/json" + "?_=" + System.currentTimeMillis()).openConnection();
                            devConnection.setConnectTimeout(15000);
                            devConnection.setReadTimeout(15000);
                            BufferedReader devReader = new BufferedReader(new InputStreamReader(devConnection.getInputStream()));
                            String devJsonString = StringUtils.toString(devReader);
                            JSONObject devObjectReader = (JSONObject) JSONValue.parseWithException(devJsonString);
                            String devVersion = ((JSONObject)((JSONArray)devObjectReader.get("artifacts")).get(0)).get("fileName").toString().replace("ItemJoin-", "").replace(".jar", "");
                            String buildVersion = devObjectReader.get("id").toString();
                            if (StringUtils.isInt(this.buildNumber) && Integer.parseInt(this.buildNumber) < Integer.parseInt(buildVersion)) {
                                String artifactPath = ((JSONObject)((JSONArray)devObjectReader.get("artifacts")).get(0)).get("relativePath").toString();
                                this.latestDev = devVersion;
                                this.latestBuild = buildVersion;
                                this.artifactPath = artifactPath;
                                reader.close();
                                return Update.BETA;
                            }
                            reader.close();
                        } catch (Exception e) {
                            ServerUtils.messageSender(sender, "&c&l[403] &cFailed to check for updates, Craftation Labs has detected too many access requests, try again later.", true);
                            ServerUtils.sendDebugTrace(e);
                        }
                    }
                }
            } catch (FileNotFoundException e) {
                return Update.UP_TO_DATE;
            } catch (Exception e) {
                if (messages) {
                    ServerUtils.messageSender(sender, "&c&l[403] &cFailed to check for updates, GitHub has detected too many access requests, try again later.", true);
                }
                ServerUtils.sendDebugTrace(e);
                return Update.UP_TO_DATE;
            }
        } else if (messages) {
            ServerUtils.messageSender(sender, "&cUpdate checking is currently disabled in the config.yml", true);
            ServerUtils.messageSender(sender, "&cIf you wish to use the auto update feature, you will need to enable it.", true);
        }
        return Update.UP_TO_DATE;
    }

    /**
     * Sends out notifications to all online op players that
     * an update is available at the time of checking for updates.
     *
     * @param update - The update type.
     */
    private void sendNotifications(final Update update) {
        try {
            Collection<?> playersOnline;
            Player[] playersOnlineOld;
            if (Bukkit.class.getMethod("getOnlinePlayers").getReturnType() == Collection.class) {
                playersOnline = ((Collection<?>) Bukkit.class.getMethod("getOnlinePlayers").invoke(null, new Object[0]));
                for (Object objPlayer : playersOnline) {
                    if (((Player) objPlayer).isOp()) {
                        if (update == Update.BETA) {
                            Core.getCore().getLang().dispatchMessage(((Player) objPlayer), "%prefix% &eA new snapshot build is available!", "&eClick to go to the download page.", this.DEV_HOST, ChatComponent.ClickAction.OPEN_URL);
                            Core.getCore().getLang().dispatchMessage(((Player) objPlayer), "%prefix% &ePlease update to the &a&lv" + this.latestDev + "&e.", "&eClick to go to the download page.", this.DEV_HOST, ChatComponent.ClickAction.OPEN_URL);
                        } else {
                            Core.getCore().getLang().dispatchMessage(((Player) objPlayer), "%prefix% &eAn update has been found!", "&eClick to go to the download page.", "https://github.com/RockinChaos/" + this.NAME.toLowerCase() + "/releases/latest", ChatComponent.ClickAction.OPEN_URL);
                            Core.getCore().getLang().dispatchMessage(((Player) objPlayer), "%prefix% &ePlease update to the &a&lv" + this.latestVersion + "-RELEASE&e.", "&eClick to go to the download page.", "https://github.com/RockinChaos/" + this.NAME.toLowerCase() + "/releases/latest", ChatComponent.ClickAction.OPEN_URL);
                        }
                    }
                }
            } else {
                playersOnlineOld = ((Player[]) Bukkit.class.getMethod("getOnlinePlayers").invoke(null, new Object[0]));
                for (Player objPlayer : playersOnlineOld) {
                    if (objPlayer.isOp()) {
                        ServerUtils.messageSender(objPlayer, "&eAn update has been found!", true);
                        if (update == Update.BETA) {
                            Core.getCore().getLang().dispatchMessage(objPlayer, "%prefix% &eA new snapshot build is available!", "&eClick to go to the download page.", this.DEV_HOST, ChatComponent.ClickAction.OPEN_URL);
                            Core.getCore().getLang().dispatchMessage(objPlayer, "%prefix% &ePlease update to the &a&lv" + this.latestDev + "&e.", "&eClick to go to the download page.", this.DEV_HOST, ChatComponent.ClickAction.OPEN_URL);
                        } else {
                            Core.getCore().getLang().dispatchMessage(objPlayer, "%prefix% &eAn update has been found!", "&eClick to go to the download page.", "https://github.com/RockinChaos/" + this.NAME.toLowerCase() + "/releases/latest", ChatComponent.ClickAction.OPEN_URL);
                            Core.getCore().getLang().dispatchMessage(objPlayer, "%prefix% &ePlease update to the &a&lv" + this.latestVersion + "-RELEASE&e.", "&eClick to go to the download page.", "https://github.com/RockinChaos/" + this.NAME.toLowerCase() + "/releases/latest", ChatComponent.ClickAction.OPEN_URL);
                        }
                    }
                }
            }
        } catch (Exception e) {
            ServerUtils.sendDebugTrace(e);
        }
    }

    /**
     * Checks if the plugin version is a development version.
     *
     * @return If the plugin version is in development.
     */
    public boolean isDevVersion() {
        return this.betaVersion || this.devVersion;
    }

    /**
     * Gets the exact string version from the plugin yml file.
     *
     * @return The exact server version.
     */
    public @Nonnull String getVersion() {
        return this.versionExact;
    }

    /**
     * Gets the plugin jar file directly.
     *
     * @return The plugins jar file.
     */
    public @Nonnull File getJarReference() {
        return this.jarRef;
    }

    public enum Update {
        DEV(false),
        BETA(true),
        RELEASE(true),
        UP_TO_DATE(false);
        public final boolean updateNeeded;
        Update(boolean bool) { this.updateNeeded = bool; }
    }
}