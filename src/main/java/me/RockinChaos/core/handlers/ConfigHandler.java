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
import me.RockinChaos.core.utils.ServerUtils;
import me.RockinChaos.core.utils.StringUtils;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("unused")
public class ConfigHandler {

    private static ConfigHandler config;
    private final HashMap<String, Boolean> noSource = new HashMap<>();
    private final Map<String, YamlConfiguration> configFiles = new HashMap<>();
    private boolean Generating = false;

    /**
     * Gets the instance of the ConfigHandler.
     *
     * @return The ConfigHandler instance.
     */
    public static ConfigHandler getConfig() {
        if (config == null) {
            config = new ConfigHandler();
        }
        return config;
    }

    /**
     * Copies files into memory.
     */
    public void copyFiles() {
        final Map<String, Integer> configs = Core.getCore().getData().getConfigs();
        for (String config : configs.keySet()) {
            if (config.equalsIgnoreCase("lang.yml")) {
                Core.getCore().getLang().refresh();
                this.copyFile(Core.getCore().getLang().getFile(), Core.getCore().getLang().getFile().split("-")[0] + "-Version", configs.get(config));
            } else {
                this.copyFile(config, config.replace(".yml", "") + "-Version", configs.get(config));
            }
        }
    }

    /**
     * Gets the file from the specified path.
     *
     * @param path - The File to be fetched.
     * @return The file.
     */
    public FileConfiguration getFile(@NonNull final String path) {
        final File file = new File(Core.getCore().getPlugin().getDataFolder(), path);
        boolean hasPath = false;
        for (String config : this.configFiles.keySet()) {
            if (path.equalsIgnoreCase(config)) {
                hasPath = true;
                break;
            }
        }
        if (!hasPath) {
            this.getSource(path);
        }
        try {
            return this.getLoadedConfig(file, false);
        } catch (Exception e) {
            ServerUtils.sendSevereTrace(e);
            ServerUtils.logSevere("Cannot load " + file.getName() + " from disk!");
        }
        return null;
    }

    /**
     * Gets the source file from the specified path.
     *
     * @param path - The File to be loaded.
     */
    public void getSource(@NonNull final String path) {
        final File file = new File(Core.getCore().getPlugin().getDataFolder(), path);
        if (!(file).exists()) {
            try {
                InputStream source;
                final File dataDir = Core.getCore().getPlugin().getDataFolder();
                if (!dataDir.exists()) {
                    boolean madeDir = dataDir.mkdir();
                }
                if (!path.contains("lang.yml")) {
                    source = Core.getCore().getPlugin().getResource("files/configs/" + path);
                } else {
                    source = Core.getCore().getPlugin().getResource("files/locales/" + path);
                }
                if (!file.exists() && source != null) {
                    Files.copy(source, file.toPath());
                }
                if (!Core.getCore().getData().getUpdateConfig().isEmpty() && path.contains(Core.getCore().getData().getUpdateConfig())) {
                    this.Generating = true;
                }
            } catch (Exception e) {
                ServerUtils.sendSevereTrace(e);
                ServerUtils.logWarn("Cannot save " + path + " to disk!");
                this.noSource.put(path, true);
                return;
            }
        }
        try {
            final YamlConfiguration config = this.getLoadedConfig(file, true);
            this.noSource.put(path, false);
        } catch (Exception e) {
            ServerUtils.sendSevereTrace(e);
            ServerUtils.logSevere("Cannot load " + file.getName() + " from disk!");
            this.noSource.put(file.getName(), true);
        }
    }

    /**
     * Gets the file and loads it into memory if specified.
     *
     * @param file   - The file to be loaded.
     * @param commit - If the File should be committed to memory.
     * @return The Memory loaded config file.
     */
    public YamlConfiguration getLoadedConfig(@NonNull final File file, final boolean commit) throws Exception {
        if (commit) {
            final Map<String, Integer> configs = Core.getCore().getData().getConfigs();
            for (final String config : configs.keySet()) {
                if (config.equalsIgnoreCase(file.getName()) || (config.contains("lang.yml") && file.getName().contains("lang.yml"))) {
                    final YamlConfiguration configFile = new YamlConfiguration();
                    configFile.load(file);
                    this.configFiles.put(file.getName(), configFile);
                    return configFile;
                }
            }
        } else {
            for (final String config : this.configFiles.keySet()) {
                if (config.equalsIgnoreCase(file.getName())) {
                    return this.configFiles.get(config);
                }
            }
        }
        return null;
    }

    /**
     * Copies the specified config file to the data folder.
     *
     * @param configFile - The name and extension of the config file to be copied.
     * @param version    - The version String to be checked in the config file.
     * @param id         - The expected version id to be found in the config file.
     */
    private void copyFile(@NonNull final String configFile, @NonNull final String version, final int id) {
        this.getSource(configFile);
        File File = new File(Core.getCore().getPlugin().getDataFolder(), configFile);
        if (File.exists() && !this.noSource.get(configFile) && this.getFile(configFile).getInt(version) != id) {
            InputStream source;
            if (!configFile.contains("lang.yml")) {
                source = Core.getCore().getPlugin().getResource("files/configs/" + configFile);
            } else {
                source = Core.getCore().getPlugin().getResource("files/locales/" + configFile);
            }
            if (source != null) {
                String[] namePart = configFile.split("\\.");
                String renameFile = namePart[0] + "-old-" + StringUtils.getRandom(1, 50000) + namePart[1];
                File renamedFile = new File(Core.getCore().getPlugin().getDataFolder(), renameFile);
                if (!renamedFile.exists()) {
                    if (File.renameTo(renamedFile)) {
                        File copyFile = new File(Core.getCore().getPlugin().getDataFolder(), configFile);
                        if (copyFile.delete()) {
                            this.getSource(configFile);
                            ServerUtils.logWarn("Your " + configFile + " is out of date and new options are available, generating a new one!");
                        }
                    }
                }
            }
        } else if (this.noSource.get(configFile)) {
            ServerUtils.logSevere("Your " + configFile + " is not using proper YAML Syntax and will not be loaded!");
            ServerUtils.logSevere("Check your YAML formatting by using a YAML-PARSER such as http://yaml-online-parser.appspot.com/");
        }
        if (!this.noSource.get(configFile)) {
            if (this.Generating && !Core.getCore().getData().getUpdateConfig().isEmpty() && configFile.equalsIgnoreCase(Core.getCore().getData().getUpdateConfig())) {
                Core.getCore().getData().runUpdateConfig().run();
                this.getSource(Core.getCore().getData().getUpdateConfig());
                this.Generating = false;
            }
            this.getFile(configFile).options().copyDefaults(false);
            if (configFile.contains("lang.yml")) {
                Core.getCore().getLang().setPrefix();
            }
        }
    }

    /**
     * Saves the changed configuration data to the File.
     *
     * @param dataFile   - The FileConfiguration being modified.
     * @param fileFolder - The folder of the file being modified.
     * @param file       - The file name being accessed.
     */
    public void saveFile(@NonNull final FileConfiguration dataFile, @NonNull final File fileFolder, @NonNull final String file) {
        try {
            dataFile.save(fileFolder);
            this.getSource(file);
            this.getFile(file).options().copyDefaults(false);
        } catch (Exception e) {
            ServerUtils.logSevere("Could not save data to the " + file + " data file!");
            ServerUtils.sendDebugTrace(e);
        }
    }

    /**
     * Properly reloads the configuration files.
     */
    public void reloadFiles() {
        config = new ConfigHandler();
        config.copyFiles();
    }
}