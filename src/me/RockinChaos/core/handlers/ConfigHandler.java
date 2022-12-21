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

import java.io.File;
import java.io.InputStream;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import me.RockinChaos.core.Core;
import me.RockinChaos.core.utils.SchedulerUtils;
import me.RockinChaos.core.utils.ServerUtils;
import me.RockinChaos.core.utils.StringUtils;

public class ConfigHandler {
	
	private JavaPlugin plugin = Core.getCore().getPlugin();
	private boolean Generating = false;
	private HashMap < String, Boolean > noSource = new HashMap < String, Boolean > ();
	private Map <String, YamlConfiguration> configFiles = new HashMap <String, YamlConfiguration>();
	
	private static ConfigHandler config;
	
   /**
    * Copies files into memory.
    * 
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
	public FileConfiguration getFile(final String path) {
		final File file = new File(this.plugin.getDataFolder(), path);
		boolean hasPath = false;
		for (String config : this.configFiles.keySet()) {
			if (path.equalsIgnoreCase(config)) {
				hasPath = true;
			}
		}
		if (!hasPath) { this.getSource(path); }
		try {
			final YamlConfiguration tes = this.getLoadedConfig(file, false);
			return tes;
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
    * @return The source file.
    */
	public FileConfiguration getSource(final String path) {
		final File file = new File(this.plugin.getDataFolder(), path);
		if (!(file).exists()) {
			try {
				InputStream source;
				final File dataDir = this.plugin.getDataFolder();
				if (!dataDir.exists()) { dataDir.mkdir(); }
				if (!path.contains("lang.yml")) { source = this.plugin.getResource("files/configs/" + path); } 
				else { source = this.plugin.getResource("files/locales/" + path); }
        		if (!file.exists()) { Files.copy(source, file.toPath(), new CopyOption[0]); }
				if (path.contains(Core.getCore().getData().getUpdateConfig())) { this.Generating = true; }
			} catch (Exception e) {
				ServerUtils.sendSevereTrace(e);
				ServerUtils.logWarn("Cannot save " + path + " to disk!");
				this.noSource.put(path, true);
				return null;
			}
		}
		try {
			final YamlConfiguration config = this.getLoadedConfig(file, true);
			this.noSource.put(path, false);
			return config;
		} catch (Exception e) {
			ServerUtils.sendSevereTrace(e);
			ServerUtils.logSevere("Cannot load " + file.getName() + " from disk!");
			this.noSource.put(file.getName(), true);
		}
		return null;
	}

   /**
    * Gets the file and loads it into memory if specified.
    * 
    * @param file - The file to be loaded.
    * @param commit - If the File should be committed to memory.
    * @return The Memory loaded config file.
    */
	public YamlConfiguration getLoadedConfig(final File file, final boolean commit) throws Exception {
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
    * @param version - The version String to be checked in the config file.
    * @param id - The expected version id to be found in the config file.
    */
	private void copyFile(final String configFile, final String version, final int id) {
		this.getSource(configFile);
		File File = new File(this.plugin.getDataFolder(), configFile);
		if (File.exists() && !this.noSource.get(configFile) && this.getFile(configFile).getInt(version) != id) {
			InputStream source;
			if (!configFile.contains("lang.yml")) { source = this.plugin.getResource("files/configs/" + configFile); } 
			else { source = this.plugin.getResource("files/locales/" + configFile); }
			if (source != null) {
				String[] namePart = configFile.split("\\.");
				String renameFile = namePart[0] + "-old-" + StringUtils.getRandom(1, 50000) + namePart[1];
				File renamedFile = new File(this.plugin.getDataFolder(), renameFile);
				if (!renamedFile.exists()) {
					File.renameTo(renamedFile);
					File copyFile = new File(this.plugin.getDataFolder(), configFile);
					copyFile.delete();
					this.getSource(configFile);
					ServerUtils.logWarn("Your " + configFile + " is out of date and new options are available, generating a new one!");
				}
			}
		} else if (this.noSource.get(configFile)) {
			ServerUtils.logSevere("Your " + configFile + " is not using proper YAML Syntax and will not be loaded!");
			ServerUtils.logSevere("Check your YAML formatting by using a YAML-PARSER such as http://yaml-online-parser.appspot.com/");
		}
		if (!this.noSource.get(configFile)) { 
			if (this.Generating && configFile.equalsIgnoreCase(Core.getCore().getData().getUpdateConfig())) { 
				SchedulerUtils.run(Core.getCore().getData().runUpdateConfig());
				this.getSource(Core.getCore().getData().getUpdateConfig());
				this.Generating = false;
			}
			this.getFile(configFile).options().copyDefaults(false);
			if (configFile.contains("lang.yml")) { Core.getCore().getLang().setPrefix(); }
		}
	}
	
   /**
    * Saves the changed configuration data to the File.
    * 
    * @param dataFile - The FileConfiguration being modified.
    * @param fileFolder - The folder of the file being modified.
    * @param file - The file name being accessed.
    */
	public void saveFile(final FileConfiguration dataFile, final File fileFolder, final String file) {
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
    * 
    */
	public void reloadFiles() {
		SchedulerUtils.runLater(20L, () -> {
			config = new ConfigHandler(); 
		});
	}
	
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
}