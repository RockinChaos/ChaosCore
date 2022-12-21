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

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

import me.RockinChaos.core.Core;
import me.RockinChaos.core.utils.SchedulerUtils;
import me.RockinChaos.core.utils.ServerUtils;
import me.RockinChaos.core.utils.StringUtils;

public class GuardAPI {
	
    private Object worldGuard = null;
    private WorldGuardPlugin worldGuardPlugin = null;
    private Object regionContainer = null;
    private Method getRegionContainer = null;
    private Method getWorldAdapter = null;
    private Method getRegionManager = null;
    private Constructor<?> vectorConstructor = null;
    private Method getVector = null;

	private int guardVersion = 0;
	private List < String > localeRegions = new ArrayList < String > ();
	
	private static GuardAPI guard;
	
   /**
	* Creates a new WorldGuard instance.
	* 
	*/
	public GuardAPI() {
		this.setPlatform();
	}
	
	private void setPlatform() {
		if (Core.getCore().isStarted()) {
			if (this.guardEnabled()) {
				this.enableGuard();
				if (Bukkit.getServer().getPluginManager().getPlugin("WorldGuard") instanceof WorldGuardPlugin) {
					this.worldGuardPlugin = (WorldGuardPlugin) Bukkit.getServer().getPluginManager().getPlugin("WorldGuard");
					try {
						Class < ? > worldGuard = Class.forName("com.sk89q.worldguard.WorldGuard");
						Method getInstance = worldGuard.getMethod("getInstance");
						this.worldGuard = getInstance.invoke(null);
					} catch (Exception e) {}
				}
				if (this.worldGuard != null) {
					try {
						Method getPlatForm = this.worldGuard.getClass().getMethod("getPlatform");
						Object platform = getPlatForm.invoke(this.worldGuard);
						Method getRegionContainer = platform.getClass().getMethod("getRegionContainer");
						this.regionContainer = getRegionContainer.invoke(platform);
						Class < ? > getWorldEditWorld = Class.forName("com.sk89q.worldedit.world.World");
						Class < ? > getWorldEditAdapter = Class.forName("com.sk89q.worldedit.bukkit.BukkitAdapter");
						this.getWorldAdapter = getWorldEditAdapter.getMethod("adapt", World.class);
						this.getRegionContainer = this.regionContainer.getClass().getMethod("get", getWorldEditWorld);
					} catch (Exception e) {
						ServerUtils.logSevere("{GuardAPI} Failed to bind to WorldGuard, integration will not work!");
						ServerUtils.sendDebugTrace(e);
						this.regionContainer = null;
						return;
					}
				} else {
					try {
						Method getRegionContainer = this.worldGuardPlugin.getClass().getMethod("getRegionContainer");
						this.regionContainer = getRegionContainer.invoke(this.worldGuardPlugin);
						this.getRegionContainer = this.regionContainer.getClass().getMethod("get", World.class);
					} catch (Exception e) {
						ServerUtils.logSevere("{GuardAPI} Failed to bind to WorldGuard, integration will not work!");
						ServerUtils.sendDebugTrace(e);
						this.regionContainer = null;
						return;
					}
				}
				try {
					Class < ? > vectorClass = Class.forName("com.sk89q.worldedit.Vector");
					this.vectorConstructor = vectorClass.getConstructor(Double.TYPE, Double.TYPE, Double.TYPE);
					this.getRegionManager = RegionManager.class.getMethod("getApplicableRegions", vectorClass);
				} catch (Exception e) {
					try {
						Class < ? > vectorClass = Class.forName("com.sk89q.worldedit.math.BlockVector3");
						this.getVector = vectorClass.getMethod("at", Double.TYPE, Double.TYPE, Double.TYPE);
						this.getRegionManager = RegionManager.class.getMethod("getApplicableRegions", vectorClass);
					} catch (Exception e2) {
						ServerUtils.logSevere("{GuardAPI} Failed to bind to WorldGuard (no Vector class?), integration will not work!");
						ServerUtils.sendDebugTrace(e);
						this.regionContainer = null;
						return;
					}
				}
				if (this.regionContainer == null) { ServerUtils.logSevere("{GuardAPI} Failed to find RegionContainer, WorldGuard integration will not function!"); }
			}
		} else {
			SchedulerUtils.runLater(1L, () -> {
				this.setPlatform();
			});
		}
	}
	
   /**
	* Enables WorldGuard if it is found.
	* 
	*/
	private void enableGuard() {
		try { this.guardVersion = Integer.parseInt(Bukkit.getServer().getPluginManager().getPlugin("WorldGuard").getDescription().getVersion().replace(".", "").substring(0, 3));
		} catch (Exception e) { this.guardVersion = 622; }
	}

   /**
	* Checks if WorldGuard is enabled.
	* 
	* @return If WorldGuard is enabled.
	*/
    public boolean guardEnabled() {
    	final boolean pluginEnabled = Bukkit.getServer().getPluginManager().isPluginEnabled("WorldEdit") && Bukkit.getServer().getPluginManager().isPluginEnabled("WorldGuard");
    	final boolean pluginExists = Bukkit.getServer().getPluginManager().getPlugin("WorldEdit") != null && Bukkit.getServer().getPluginManager().getPlugin("WorldGuard") != null;
    	final boolean isHookable = !StringUtils.containsIgnoreCase(Core.getCore().getDependencies().getIgnoreList(), "WorldEdit") && !StringUtils.containsIgnoreCase(Core.getCore().getDependencies().getIgnoreList(), "WorldGuard");
    	return (Core.getCore().isStarted() ? pluginEnabled : pluginExists) && isHookable;
    }
    
   /**
	* Gets the current WorldGuard version.
	* 
	* @return The current WorldGuard version.
	*/
    public int guardVersion() {
    	return this.guardVersion;
    }
    
   /**
	* Gets the WorldGuard regions in the specified world.
	* 
	* @param world - The world to get the regions from.
	* @return The List of Regions for the specified world.
	*/
	public Map<String, ProtectedRegion> getRegions(final World world) {
		return this.getRegionManager(world).getRegions();
	}
	
   /**
	* Gets the current region(s) at the specified location
	* 
	* @param location - The Location to have exiting regions fetched.
	* @return regionSet The applicable regions at the Location.
	*/
	public String getRegionAtLocation(final Location location) {
		ApplicableRegionSet set = null;
		String regionSet = "";
		try { set = this.getRegionSet(location); } 
		catch (Exception e) { ServerUtils.sendDebugTrace(e); }
		if (set == null) { return regionSet; }
		for (ProtectedRegion r: set) {
			if (regionSet.isEmpty()) { regionSet += r.getId(); }
			else { regionSet += ", " + r.getId(); }
		}
		return regionSet;
	}
	
   /**
	* Gets the applicable region(s) set at the players location.
	* 
	* @param location - The exact location of the player.
	* @return ApplicableRegionSet The WorldGuard RegionSet.
	*/
	private ApplicableRegionSet getRegionSet(final Location location) throws Exception {
		RegionManager regionManager = this.getRegionManager(location.getWorld());
		if (regionManager == null) { return null; }
		try {
			Object vector = this.getVector == null ? this.vectorConstructor.newInstance(location.getX(), location.getY(), location.getZ()) 
					        : this.getVector.invoke(null, location.getX(), location.getY(), location.getZ());
			return (ApplicableRegionSet) this.getRegionManager.invoke(regionManager, vector);
		} catch (Exception e) {
			ServerUtils.logSevere("{GuardAPI} An error occurred looking up a WorldGuard ApplicableRegionSet.");
			ServerUtils.sendDebugTrace(e);
		}
		return null;
	}
	
   /**
	* Gets the RegionManager for the Bukkit World.
	* 
	* @param world - The world that the player is currently in.
	* @return The WorldGuard RegionManager for the specified world.
	*/
    private RegionManager getRegionManager(final World world) {
    	if (this.regionContainer == null || this.getRegionContainer == null) { return null; }
    	RegionManager regionManager = null;
    	try {
    		if (this.getWorldAdapter != null) {
    			Object worldEditWorld = this.getWorldAdapter.invoke(null, world);
    			regionManager = (RegionManager) this.getRegionContainer.invoke(this.regionContainer, worldEditWorld);
    		} else {
    			regionManager = (RegionManager) this.getRegionContainer.invoke(this.regionContainer, world);
    		}
    	} catch (Exception e) {
    		ServerUtils.logSevere("{GuardAPI} An error occurred looking up a WorldGuard RegionManager.");
    		ServerUtils.sendDebugTrace(e);
    	}
    	return regionManager;
    }
    
   /**
	* Checks if the player has entered or exited 
	* any region(s) defined against the String.
	* 
	* @param checkRegion - The region that the player entered or exited.
	* @return If the region is defined.
	*/
	private boolean isLocaleRegion(final String checkRegion) {
		for (final String region : this.localeRegions) {
			if (region.equalsIgnoreCase(checkRegion) || region.equalsIgnoreCase("UNDEFINED")) {
				return true;
			}
		}
		return false;
	}

   /**
	* Adds a region to be compared against.
	* 
	* @param region - The region that the String has defined.
	*/
	public void addLocaleRegion(final String region) {
		if (!this.isLocaleRegion(region)) { 
			this.localeRegions.add(region); 
		}
	}
	
   /**
    * Gets the instance of the GuardAPI.
    * 
    * @param regen - If the GuardAPI should have a new instance created.
    * @return The GuardAPI instance.
    */
    public static GuardAPI getGuard(final boolean regen) { 
        if (guard == null || regen) { guard = new GuardAPI(); }
        return guard; 
    } 
}