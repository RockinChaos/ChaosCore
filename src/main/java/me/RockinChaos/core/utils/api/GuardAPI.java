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

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import me.RockinChaos.core.Core;
import me.RockinChaos.core.utils.ReflectionUtils;
import me.RockinChaos.core.utils.SchedulerUtils;
import me.RockinChaos.core.utils.ServerUtils;
import me.RockinChaos.core.utils.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@SuppressWarnings("unused")
public class GuardAPI {

    private static GuardAPI guard;
    private final List<String> localeRegions = new ArrayList<>();
    private Object worldGuard = null;
    private WorldGuardPlugin worldGuardPlugin = null;
    private Object regionContainer = null;
    private ReflectionUtils.MethodInvoker getRegionContainer = null;
    private ReflectionUtils.MethodInvoker getWorldAdapter = null;
    private ReflectionUtils.MethodInvoker getRegionManager = null;
    private ReflectionUtils.ConstructorInvoker vectorConstructor = null;
    private ReflectionUtils.MethodInvoker getVector = null;
    private int guardVersion = 0;

    /**
     * Creates a new WorldGuard instance.
     */
    public GuardAPI() {
        this.setPlatform();
    }

    /**
     * Gets the instance of the GuardAPI.
     *
     * @param regen - If the GuardAPI should have a new instance created.
     * @return The GuardAPI instance.
     */
    public static GuardAPI getGuard(final boolean regen) {
        if (guard == null || regen) {
            guard = new GuardAPI();
        }
        return guard;
    }

    private void setPlatform() {
        if (Core.getCore().isStarted()) {
            if (this.guardEnabled()) {
                this.enableGuard();
                if (Bukkit.getServer().getPluginManager().getPlugin("WorldGuard") instanceof WorldGuardPlugin) {
                    this.worldGuardPlugin = (WorldGuardPlugin) Bukkit.getServer().getPluginManager().getPlugin("WorldGuard");
                    try {
                        final Class<?> worldGuard = ReflectionUtils.getClass("com.sk89q.worldguard.WorldGuard");
                        final ReflectionUtils.MethodInvoker getInstance = ReflectionUtils.getMethod(worldGuard, "getInstance");
                        this.worldGuard = getInstance.invoke(null);
                    } catch (Exception ignored) {
                    }
                }
                if (this.worldGuard != null) {
                    try {
                        final ReflectionUtils.MethodInvoker getPlatForm = ReflectionUtils.getMethod(this.worldGuard.getClass(), "getPlatform");
                        final Object platform = getPlatForm.invoke(this.worldGuard);
                        final ReflectionUtils.MethodInvoker getRegionContainer = ReflectionUtils.getMethod(platform.getClass(), "getRegionContainer");
                        this.regionContainer = getRegionContainer.invoke(platform);
                        final Class<?> getWorldEditWorld = ReflectionUtils.getClass("com.sk89q.worldedit.world.World");
                        final Class<?> getWorldEditAdapter = ReflectionUtils.getClass("com.sk89q.worldedit.bukkit.BukkitAdapter");
                        this.getWorldAdapter = ReflectionUtils.getMethod(getWorldEditAdapter, "adapt", World.class);
                        this.getRegionContainer = ReflectionUtils.getMethod(this.regionContainer.getClass(), "get", getWorldEditWorld);
                    } catch (Exception e) {
                        ServerUtils.logSevere("{GuardAPI} Failed to bind to WorldGuard, integration will not work!");
                        ServerUtils.sendDebugTrace(e);
                        this.regionContainer = null;
                        return;
                    }
                } else {
                    try {
                        final ReflectionUtils.MethodInvoker getRegionContainer = ReflectionUtils.getMethod(this.worldGuardPlugin.getClass(), "getRegionContainer");
                        this.regionContainer = getRegionContainer.invoke(this.worldGuardPlugin);
                        this.getRegionContainer = ReflectionUtils.getMethod(this.regionContainer.getClass(), "get", World.class);
                    } catch (Exception e) {
                        ServerUtils.logSevere("{GuardAPI} Failed to bind to WorldGuard, integration will not work!");
                        ServerUtils.sendDebugTrace(e);
                        this.regionContainer = null;
                        return;
                    }
                }
                try {
                    final Class<?> vectorClass = ReflectionUtils.getClass("com.sk89q.worldedit.Vector");
                    this.vectorConstructor = ReflectionUtils.getConstructor(vectorClass, Double.TYPE, Double.TYPE, Double.TYPE);
                    this.getRegionManager = ReflectionUtils.getMethod(RegionManager.class, "getApplicableRegions", vectorClass);
                } catch (Exception e) {
                    try {
                        final Class<?> vectorClass = ReflectionUtils.getClass("com.sk89q.worldedit.math.BlockVector3");
                        this.getVector = ReflectionUtils.getMethod(vectorClass, "at", Double.TYPE, Double.TYPE, Double.TYPE);
                        this.getRegionManager = ReflectionUtils.getMethod(RegionManager.class, "getApplicableRegions", vectorClass);
                    } catch (Exception e2) {
                        ServerUtils.logSevere("{GuardAPI} Failed to bind to WorldGuard (no Vector class?), integration will not work!");
                        ServerUtils.sendDebugTrace(e);
                        this.regionContainer = null;
                        return;
                    }
                }
                if (this.regionContainer == null) {
                    ServerUtils.logSevere("{GuardAPI} Failed to find RegionContainer, WorldGuard integration will not function!");
                }
            }
        } else {
            SchedulerUtils.runLater(1L, this::setPlatform);
        }
    }

    /**
     * Enables WorldGuard if it is found.
     */
    private void enableGuard() {
        try {
            this.guardVersion = Integer.parseInt(Objects.requireNonNull(Bukkit.getServer().getPluginManager().getPlugin("WorldGuard")).getDescription().getVersion().replace(".", "").substring(0, 3));
        } catch (Exception e) {
            this.guardVersion = 622;
        }
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
    public @Nonnull Map<String, ProtectedRegion> getRegions(final @Nonnull World world) {
        return Objects.requireNonNull(this.getRegionManager(world)).getRegions();
    }

    /**
     * Gets the current region(s) at the specified location
     *
     * @param location - The Location to have exiting regions fetched.
     * @return regionSet The applicable regions at the Location.
     */
    public @Nonnull String getRegionAtLocation(final @Nonnull Location location) {
        ApplicableRegionSet set = null;
        StringBuilder regionSet = new StringBuilder();
        try {
            set = this.getRegionSet(location);
        } catch (Exception e) {
            ServerUtils.sendDebugTrace(e);
        }
        if (set == null) {
            return regionSet.toString();
        }
        for (ProtectedRegion r : set) {
            if (regionSet.length() == 0) {
                regionSet.append(r.getId());
            } else {
                regionSet.append(", ").append(r.getId());
            }
        }
        return regionSet.toString();
    }

    /**
     * Gets the applicable region(s) set at the players' location.
     *
     * @param location - The exact location of the player.
     * @return ApplicableRegionSet The WorldGuard RegionSet.
     */
    private @Nullable ApplicableRegionSet getRegionSet(final @Nonnull Location location) {
        final RegionManager regionManager = this.getRegionManager(Objects.requireNonNull(location.getWorld()));
        if (regionManager == null || !this.guardEnabled()) {
            return null;
        }
        try {
            final Object vector = this.getVector == null ? this.vectorConstructor.invoke(location.getX(), location.getY(), location.getZ()) : this.getVector.invoke(null, location.getX(), location.getY(), location.getZ());
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
    private @Nullable RegionManager getRegionManager(final @Nonnull World world) {
        if (this.regionContainer == null || this.getRegionContainer == null) {
            return null;
        }
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
    private boolean isLocaleRegion(final @Nonnull String checkRegion) {
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
    public void addLocaleRegion(final @Nonnull String region) {
        if (!this.isLocaleRegion(region)) {
            this.localeRegions.add(region);
        }
    }
}