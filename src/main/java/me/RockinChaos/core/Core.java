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
package me.RockinChaos.core;

import me.RockinChaos.core.handlers.ConfigHandler;
import me.RockinChaos.core.handlers.LogHandler;
import me.RockinChaos.core.handlers.UpdateHandler;
import me.RockinChaos.core.utils.ServerUtils;
import me.RockinChaos.core.utils.api.BungeeAPI;
import me.RockinChaos.core.utils.api.ChanceAPI;
import me.RockinChaos.core.utils.api.DependAPI;
import me.RockinChaos.core.utils.api.LanguageAPI;
import me.RockinChaos.core.utils.sql.SQL;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import javax.annotation.Nonnull;
import java.io.File;

@SuppressWarnings("unused")
public class Core {

    private static Core core;
    private final JavaPlugin plugin;
    private final File pluginFile;

    /**
     * Creates a new ChaosCore instance.
     *
     * @param plugin         - The plugin instance creating the Core.
     * @param pluginFile     - The plugin file reference.
     * @param prefix         - The visual display prefix for the plugin.
     */
    public Core(final @Nonnull JavaPlugin plugin, final @Nonnull File pluginFile, final @Nonnull String prefix) {
        core = this;
        this.plugin = plugin;
        this.pluginFile = pluginFile;
        core.getData().setPluginPrefix(prefix);
    }

    /**
     * Gets the instance of the ChaosCore.
     *
     * @return The ChaosCore instance.
     */
    public static @Nonnull Core getCore() {
        return core;
    }

    /**
     * Checks if ChaosCore is enabled.
     * Server Version must be at least 1.8
     *
     * @return If ChaosCore is enabled.
     */
    public boolean isEnabled() {
        final boolean isSupported = ServerUtils.hasSpecificUpdate("1_8");
        if (!isSupported) {
            ServerUtils.logSevere("detected incompatible server version.");
            ServerUtils.logSevere("only Minecraft 1.8 and above is supported... disabling...");
            getPlugin().getPluginLoader().disablePlugin(getPlugin());
        }
        return isSupported;
    }

    /**
     * The Java Plugin that created this core.
     *
     * @return The Java Plugin.
     */
    public @Nonnull JavaPlugin getPlugin() {
        return this.plugin;
    }

    /**
     * Checks if the plugin has fully loaded.
     *
     * @return If the plugin has fully started.
     */
    public boolean isStarted() {
        return CoreData.getData().isStarted();
    }

    /**
     * Gets the SQL for the Plugin.
     *
     * @return The cached SQL.
     */
    public @Nonnull SQL getSQL() {
        return SQL.getSQL();
    }

    /**
     * Gets the DataHandler for the Plugin.
     *
     * @return The cached DataHandler.
     */
    public @Nonnull CoreData getData() {
        return CoreData.getData();
    }

    /**
     * Gets the LanguageAPI for the Plugin.
     *
     * @return The cached LanguageAPI.
     */
    public @Nonnull LanguageAPI getLang() {
        return LanguageAPI.getLang();
    }

    /**
     * Gets the LogHandler for the Plugin.
     *
     * @return The cached LogHandler.
     */
    public @Nonnull LogHandler getFilter() {
        return LogHandler.getFilter();
    }

    /**
     * Gets the BungeeAPI for the Plugin.
     *
     * @return The cached BungeeAPI.
     */
    public @Nonnull BungeeAPI getBungee() {
        return BungeeAPI.getBungee();
    }

    /**
     * Gets the ChanceAPI for the Plugin.
     *
     * @return The cached ChanceAPI.
     */
    public @Nonnull ChanceAPI getChances() {
        return ChanceAPI.getChances();
    }

    /**
     * Gets the DependAPI for the Plugin.
     *
     * @return The cached DependAPI.
     */
    public @Nonnull DependAPI getDependencies() {
        return DependAPI.getDepends();
    }

    /**
     * Gets the ConfigHandler for the Plugin.
     *
     * @return The cached ConfigHandler.
     */
    public @Nonnull ConfigHandler getConfiguration() {
        return ConfigHandler.getConfig();
    }

    /**
     * Gets the specified config file for the Plugin.
     *
     * @return The cached specified config file as a FileConfiguration.
     */
    public @Nonnull FileConfiguration getConfig(final @Nonnull String configFile) {
        return ConfigHandler.getConfig().getFile(configFile);
    }

    /**
     * Gets the UpdateHandler for the Plugin.
     *
     * @return The cached UpdateHandler.
     */
    public @Nonnull UpdateHandler getUpdater() {
        return UpdateHandler.getUpdater(this.plugin, this.pluginFile, getData().checkForUpdates());
    }
}