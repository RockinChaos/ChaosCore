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

import java.io.File;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import me.RockinChaos.core.handlers.ConfigHandler;
import me.RockinChaos.core.handlers.LogHandler;
import me.RockinChaos.core.handlers.UpdateHandler;
import me.RockinChaos.core.utils.api.BungeeAPI;
import me.RockinChaos.core.utils.api.ChanceAPI;
import me.RockinChaos.core.utils.api.DependAPI;
import me.RockinChaos.core.utils.api.LanguageAPI;
import me.RockinChaos.core.utils.enchants.Enchantments;
import me.RockinChaos.core.utils.sql.SQL;

public class Core {
	
	private JavaPlugin plugin;
	private File pluginFile;
	private boolean updatesAllowed;
	
	private static Core core;
	
   /**
    * Creates a new ChaosCore instance.
    * 
    * @param plugin - The plugin instance creating the Core.
    * @param pluginFile - The plugin file reference.
    * @param prefix - The visual display prefix for the plugin.
    * @param updatesAllowed - If checking for updates is enabled.
    */
	public Core(final JavaPlugin plugin, final File pluginFile, final String prefix, final boolean updatesAllowed) {
		core = this;
		this.plugin = plugin;
		this.pluginFile = pluginFile;
		this.updatesAllowed = updatesAllowed;
		core.getData().setPluginPrefix(prefix);
	}
	
   /**
	* The Java Plugin that created this core.
	* 
	* @return The Java Plugin.
	*/
	public JavaPlugin getPlugin() {
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
	public SQL getSQL() {
		return SQL.getSQL();
	}
	
   /**
    * Gets the DataHandler for the Plugin.
    * 
    * @return The cached DataHandler.
    */
	public CoreData getData() {
		return CoreData.getData();
	}
	
   /**
    * Gets the LanguageAPI for the Plugin.
    * 
    * @return The cached LanguageAPI.
    */
	public LanguageAPI getLang() {
		return LanguageAPI.getLang();
	}
	
   /**
    * Gets the LogHandler for the Plugin.
    * 
    * @return The cached LogHandler.
    */
	public LogHandler getFilter() {
		return LogHandler.getFilter();
	}
	
   /**
    * Gets the BungeeAPI for the Plugin.
    * 
    * @return The cached BungeeAPI.
    */
	public BungeeAPI getBungee() {
		return BungeeAPI.getBungee();
	}
	
   /**
    * Gets the ChanceAPI for the Plugin.
    * 
    * @return The cached ChanceAPI.
    */
	public ChanceAPI getChances() {
		return ChanceAPI.getChances();
	}
	
   /**
    * Gets the Enchantments for the Plugin.
    * 
    * @return The cached Enchantments.
    */
	public Enchantments getEnchants() {
		return Enchantments.getEnchants();
	}
	
   /**
    * Gets the DependAPI for the Plugin.
    * 
    * @return The cached DependAPI.
    */
	public DependAPI getDependencies() {
		return DependAPI.getDepends();
	}
	
   /**
    * Gets the ConfigHandler for the Plugin.
    * 
    * @return The cached ConfigHandler.
    */
	public ConfigHandler getConfiguration() {
		return ConfigHandler.getConfig();
	}
	
   /**
    * Gets the specified config file for the Plugin.
    * 
    * @return The cached specified config file as a FileConfiguration.
    */
	public FileConfiguration getConfig(final String configFile) {
		return ConfigHandler.getConfig().getFile(configFile);
	}
	
   /**
    * Gets the UpdateHandler for the Plugin.
    * 
    * @return The cached UpdateHandler.
    */
	public UpdateHandler getUpdater() {
		return UpdateHandler.getUpdater(this.plugin, this.pluginFile, this.updatesAllowed);
	}

   /**
    * Gets the instance of the ChaosCOre.
    * 
    * @return The ChaosCore instance.
    */
	public static Core getCore() {
        return core; 
	}
}