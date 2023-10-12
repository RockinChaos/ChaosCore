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

import me.RockinChaos.core.utils.ServerUtils;
import me.RockinChaos.core.utils.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.Collection;

@SuppressWarnings("unused")
public class UpdateHandler {
    private static UpdateHandler updater;
    private final String NAME;
    private final String HOST;
    private final String versionExact;
    private final String localeVersion;
    private final boolean betaVersion;
    private final boolean devVersion;

    private final File jarRef;

    private final boolean updatesAllowed;
    private String latestVersion;

    /**
     * Initializes the UpdateHandler and Checks for Updates upon initialization.
     */
    public UpdateHandler(final JavaPlugin plugin, final File pluginFile, final boolean updatesAllowed) {
        this.NAME = plugin.getName();
        this.jarRef = pluginFile;
        this.checkUpdates(plugin.getServer().getConsoleSender(), true);
        this.HOST = "https://api.github.com/repos/RockinChaos/" + plugin.getName().toLowerCase() + "/releases/latest";
        this.versionExact = plugin.getDescription().getVersion();
        this.localeVersion = this.versionExact.split("-")[0];
        this.betaVersion = this.versionExact.contains("-SNAPSHOT") || this.versionExact.contains("-EXPERIMENTAL") || this.versionExact.contains("-BETA") || this.versionExact.contains("-ALPHA");
        this.devVersion = this.localeVersion.equals("${project.version}");
        this.updatesAllowed = updatesAllowed;
    }

    /**
     * Gets the instance of the UpdateHandler.
     *
     * @param plugin         - the plugin having its update checked.
     * @param pluginFile     - The jar file directory.
     * @param updatesAllowed - If checking for updates should be allowed to nag.
     * @return The UpdateHandler instance.
     */
    public static UpdateHandler getUpdater(final JavaPlugin plugin, final File pluginFile, final boolean updatesAllowed) {
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
    public void forceUpdates(final CommandSender sender) {
        if (this.updateNeeded(sender, false)) {
            ServerUtils.messageSender(sender, "&aAn update has been found!");
            ServerUtils.messageSender(sender, "&aAttempting to update from " + "&ev" + this.localeVersion + " &ato the new " + "&ev" + this.latestVersion);
            try {
                String uri = this.HOST.replace("repos/", "").replace("api.", "").replace("latest", "download/" + "v" + this.latestVersion + "/" + this.NAME.toLowerCase() + ".jar") + "?_=" + System.currentTimeMillis();
                HttpURLConnection httpConnection = (HttpURLConnection) new URL(uri).openConnection();
                httpConnection.setRequestProperty("User-Agent", "Mozilla/5.0...");
                BufferedInputStream in = new BufferedInputStream(httpConnection.getInputStream());
                FileOutputStream fos = new FileOutputStream(this.jarRef);
                int BYTE_SIZE = 2048;
                BufferedOutputStream bout = new BufferedOutputStream(fos, BYTE_SIZE);
                String progressBar = "&a::::::::::::::::::::::::::::::";
                byte[] data = new byte[BYTE_SIZE];
                long cloudFileSize = httpConnection.getContentLength();
                long fetchedSize = 0;
                int bytesRead;
                while ((bytesRead = in.read(data, 0, BYTE_SIZE)) >= 0) {
                    bout.write(data, 0, bytesRead);
                    fetchedSize += bytesRead;
                    final int currentProgress = (int) (((double) fetchedSize / (double) cloudFileSize) * 30);
                    if ((((fetchedSize * 100) / cloudFileSize) % 25) == 0 && currentProgress > 10) {
                        ServerUtils.messageSender(sender, progressBar.substring(0, currentProgress + 2) + "&c" + progressBar.substring(currentProgress + 2));
                    }
                }
                bout.close();
                in.close();
                fos.close();
                ServerUtils.messageSender(sender, "&aSuccessfully updated to v" + this.latestVersion + "!");
                ServerUtils.messageSender(sender, "&aYou must restart your server for this to take affect.");
            } catch (Exception e) {
                ServerUtils.messageSender(sender, "&cAn error has occurred while trying to update the plugin " + this.NAME + ".");
                ServerUtils.messageSender(sender, "&cPlease try again later, if you continue to see this please contact the plugin developer.");
                ServerUtils.sendDebugTrace(e);
            }
        } else {
            if (this.betaVersion) {
                ServerUtils.messageSender(sender, "&aYou are running a SNAPSHOT!");
                ServerUtils.messageSender(sender, "&aIf you find any bugs please report them!");
            }
            ServerUtils.messageSender(sender, "&aYou are up to date!");
        }
    }

    /**
     * Checks to see if an update is required, notifying the console window and online op players.
     *
     * @param sender  - The executor of the update checking.
     * @param onStart - If it is checking for updates on start.
     */
    public void checkUpdates(final CommandSender sender, final boolean onStart) {
        if (this.updateNeeded(sender, onStart) && this.updatesAllowed) {
            if (this.betaVersion) {
                ServerUtils.messageSender(sender, "&cYour current version: &bv" + this.localeVersion + "-SNAPSHOT");
                ServerUtils.messageSender(sender, "&cThis &bSNAPSHOT &cis outdated and a release version is now available.");
            } else {
                ServerUtils.messageSender(sender, "&cYour current version: &bv" + this.localeVersion + "-RELEASE");
            }
            ServerUtils.messageSender(sender, "&cA new version is available: " + "&av" + this.latestVersion + "-RELEASE");
            ServerUtils.messageSender(sender, "&aGet it from: https://github.com/RockinChaos/" + this.NAME.toLowerCase() + "/releases/latest");
            ServerUtils.messageSender(sender, "&aIf you wish to auto update, please type /" + this.NAME + " Upgrade");
            this.sendNotifications();
        } else if (this.updatesAllowed) {
            if (this.betaVersion) {
                ServerUtils.messageSender(sender, "&aYou are running a SNAPSHOT!");
                ServerUtils.messageSender(sender, "&aIf you find any bugs please report them!");
            } else if (this.devVersion) {
                ServerUtils.messageSender(sender, "&aYou are running a DEVELOPER SNAPSHOT!");
                ServerUtils.messageSender(sender, "&aIf you find any bugs please report them!");
                ServerUtils.messageSender(sender, "&aYou will not receive any updates requiring you to manually update.");
            }
            ServerUtils.messageSender(sender, "&aYou are up to date!");
        }
    }

    /**
     * Directly checks to see if the GitHub host has an update available.
     *
     * @param sender  - The executor of the update checking.
     * @param onStart - If it is checking for updates on start.
     * @return If an update is needed.
     */
    private boolean updateNeeded(final CommandSender sender, final boolean onStart) {
        if (this.updatesAllowed) {
            if (!onStart) {
                ServerUtils.messageSender(sender, "&aChecking for updates...");
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
                    String[] latestSplit = this.latestVersion.split("\\.");
                    String[] localeSplit = this.localeVersion.split("\\.");
                    if (this.devVersion) {
                        return false;
                    } else if ((Integer.parseInt(latestSplit[0]) > Integer.parseInt(localeSplit[0]) || Integer.parseInt(latestSplit[1]) > Integer.parseInt(localeSplit[1]) || Integer.parseInt(latestSplit[2]) > Integer.parseInt(localeSplit[2]))
                            || (this.betaVersion && (Integer.parseInt(latestSplit[0]) == Integer.parseInt(localeSplit[0]) && Integer.parseInt(latestSplit[1]) == Integer.parseInt(localeSplit[1]) && Integer.parseInt(latestSplit[2]) == Integer.parseInt(localeSplit[2])))) {
                        return true;
                    }
                }
            } catch (FileNotFoundException e) {
                return false;
            } catch (Exception e) {
                ServerUtils.messageSender(sender, "&c&l[403] &cFailed to check for updates, GitHub has detected too many access requests, try again later.");
                ServerUtils.sendDebugTrace(e);
                return false;
            }
        } else if (!onStart) {
            ServerUtils.messageSender(sender, "&cUpdate checking is currently disabled in the config.yml");
            ServerUtils.messageSender(sender, "&cIf you wish to use the auto update feature, you will need to enable it.");
        }
        return false;
    }

    /**
     * Sends out notifications to all online op players that
     * an update is available at the time of checking for updates.
     */
    private void sendNotifications() {
        try {
            Collection<?> playersOnline;
            Player[] playersOnlineOld;
            if (Bukkit.class.getMethod("getOnlinePlayers").getReturnType() == Collection.class) {
                if (Bukkit.class.getMethod("getOnlinePlayers").getReturnType() == Collection.class) {
                    playersOnline = ((Collection<?>) Bukkit.class.getMethod("getOnlinePlayers").invoke(null, new Object[0]));
                    for (Object objPlayer : playersOnline) {
                        if (((Player) objPlayer).isOp()) {
                            ServerUtils.messageSender(((Player) objPlayer), "&eAn update has been found!");
                            ServerUtils.messageSender(((Player) objPlayer), "&ePlease update to the latest version: v" + this.latestVersion);
                        }
                    }
                }
            } else {
                playersOnlineOld = ((Player[]) Bukkit.class.getMethod("getOnlinePlayers").invoke(null, new Object[0]));
                for (Player objPlayer : playersOnlineOld) {
                    if (objPlayer.isOp()) {
                        ServerUtils.messageSender(objPlayer, "&eAn update has been found!");
                        ServerUtils.messageSender(objPlayer, "&ePlease update to the latest version: v" + this.latestVersion);
                    }
                }
            }
        } catch (Exception e) {
            ServerUtils.sendDebugTrace(e);
        }
    }

    /**
     * Gets the exact string version from the plugin yml file.
     *
     * @return The exact server version.
     */
    public String getVersion() {
        return this.versionExact;
    }

    /**
     * Gets the plugin jar file directly.
     *
     * @return The plugins jar file.
     */
    public File getJarReference() {
        return this.jarRef;
    }
}