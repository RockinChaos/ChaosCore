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

import me.RockinChaos.core.utils.ReflectionUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@SuppressWarnings("unused")
public class CoreData {

    private static CoreData data;
    private String prefix;
    private Map<String, Integer> configs;
    private Runnable runnableConfig;
    private String updateConfig = "";
    private boolean updatesAllowed = false;
    private boolean debug = false;
    private boolean ignoreErrors = false;
    private Runnable runnableAlter;
    private Runnable runnableCreate;
    private Map<String, List<Object>> databaseData;
    private boolean dataTags = false;
    private boolean isStarted = false;
    private boolean sql;
    private String sqlHost;
    private String sqlPort;
    private String sqlDatabase;
    private String sqlUser;
    private String sqlPass;
    private String tablePrefix;
    private List<String> permissions = new ArrayList<>();
    private List<String> languages = new ArrayList<>();

    /**
     * Gets the instance of the DataHandler.
     *
     * @return The DataHandler instance.
     */
    public static @Nonnull CoreData getData() {
        if (data == null) {
            data = new CoreData();
        }
        return data;
    }

    /**
     * Checks if the plugin as fully loaded.
     *
     * @return If the plugin is fully loaded.
     */
    public boolean isStarted() {
        return this.isStarted;
    }

    /**
     * Sets the plugin as fully loaded.
     *
     * @param bool - If the plugin has fully loaded.
     */
    public void setStarted(final boolean bool) {
        this.isStarted = bool;
    }

    /**
     * Checks if NBTData is enabled for the current server version.
     *
     * @return If NBTData is enabled.
     */
    public boolean dataTagsEnabled() {
        return this.dataTags;
    }

    /**
     * Sets if data tags should be enabled.
     *
     * @param bool - If data tags should be enabled.
     */
    public void setDataTags(final boolean bool) {
        this.dataTags = bool;
    }

    /**
     * Checks if the plugin can check for updates.
     *
     * @return If update checking is enabled.
     */
    public boolean checkForUpdates() {
        return this.updatesAllowed;
    }

    /**
     * Sets the plugin to check for updates.
     *
     * @param bool - If update checking should be enabled.
     */
    public void setCheckforUpdates(final boolean bool) {
        this.updatesAllowed = bool;
    }

    /**
     * Checks if Debugging is enabled.
     *
     * @return If Debugging is enabled.
     */
    public boolean debugEnabled() {
        return this.debug;
    }

    /**
     * Sets the debugging status.
     *
     * @param bool - If debugging should be enabled.
     */
    public void setDebug(final boolean bool) {
        this.debug = bool;
    }

    /**
     * Checks if Ignore Errors is enabled.
     *
     * @return If Ignore Errors is enabled.
     */
    public boolean ignoreErrors() {
        return !this.ignoreErrors;
    }

    /**
     * Sets the Ignore Errors status.
     *
     * @param bool - If Ignore Errors should be enabled.
     */
    public void setIgnoreErrors(final boolean bool) {
        this.ignoreErrors = bool;
    }

    /**
     * The Prefix of the Plugin that created this core.
     *
     * @return The Plugin Prefix.
     */
    public @Nonnull String getPluginPrefix() {
        return this.prefix;
    }

    /**
     * Sets the visual display Prefix for the plugin.
     *
     * @param prefix - The visual Prefix to be set.
     */
    public void setPluginPrefix(final @Nonnull String prefix) {
        this.prefix = prefix;

    }

    /**
     * Gets the AlterTables Runnable for the Database.
     *
     * @return The runnable for the AlterTables.
     */
    public @Nonnull Runnable getAlterTables() {
        return this.runnableAlter;
    }

    /**
     * Sets the AlterTables Runnable for the Database.
     *
     * @param runnableAlter - The runnable for the AlterTables.
     */
    public void setAlterTables(final @Nonnull Runnable runnableAlter) {
        this.runnableAlter = runnableAlter;

    }

    /**
     * Gets the CreateTables Runnable for the Database.
     *
     * @return The runnable for the CreateTables.
     */
    public @Nonnull Runnable getCreateTables() {
        return this.runnableCreate;
    }

    /**
     * Sets the CreateTables Runnable for the Database.
     *
     * @param runnableCreate - The runnable for the CreateTables.
     */
    public void setCreateTables(final @Nonnull Runnable runnableCreate) {
        this.runnableCreate = runnableCreate;

    }

    /**
     * Gets the UpdateConfig Runnable for the Database.
     *
     * @return The runnable for the UpdateConfig.
     */
    public @Nonnull Runnable runUpdateConfig() {
        return this.runnableConfig;
    }

    /**
     * Gets the Config Name for the UpdateConfig Runnable for the Database.
     *
     * @return The Config Name for the UpdateConfig.
     */
    public @Nonnull String getUpdateConfig() {
        return this.updateConfig;
    }

    /**
     * Sets the UpdateConfig Runnable for the Database.
     * Sets the Config Name of the UpdateConfig Runnable for the Database.
     *
     * @param runnableConfig - The runnable for the UpdateConfig.
     * @param updateConfig   - The Config Name for the UpdateConfig.
     */
    public void setUpdateConfig(final @Nonnull Runnable runnableConfig, final @Nonnull String updateConfig) {
        this.runnableConfig = runnableConfig;
        this.updateConfig = updateConfig;

    }

    /**
     * Gets the full dataset for the SQL Database.
     *
     * @return The full dataset for the SQL Database.
     */
    public @Nonnull Map<String, List<Object>> getDatabaseData() {
        return this.databaseData;
    }

    /**
     * Sets the dataset for the SQL Database.
     *
     * @param databaseData - The dataset for the SQL Database.
     */
    public void setDatabaseData(final @Nonnull Map<String, List<Object>> databaseData) {
        this.databaseData = databaseData;
    }

    /**
     * Gets the Map of available configs and their version numbers.
     *
     * @return The Map of available configs.
     */
    public @Nonnull Map<String, Integer> getConfigs() {
        return this.configs;
    }

    /**
     * Sets the Map of available configs and their version numbers.
     *
     * @param configs - The Map of available configs.
     */
    public void setConfig(final @Nonnull Map<String, Integer> configs) {
        this.configs = configs;

    }

    /**
     * Gets the list of Languages specified for the plugin language file.
     *
     * @return The list of Languages for the plugin.
     */
    public @Nonnull List<String> getLanguages() {
        return this.languages;
    }

    /**
     * Sets the list of Languages specified for the plugin language file.
     *
     * @param languages - The list of Languages for the plugin.
     */
    public void setLanguages(final @Nonnull List<String> languages) {
        this.languages = languages;
    }

    /**
     * Gets the list of Permissions specified for the plugin.
     *
     * @return The list of Permissions for the plugin.
     */
    public @Nonnull List<String> getPermissions() {
        return this.permissions;
    }

    /**
     * Sets the list of Permissions specified for the plugin.
     *
     * @param permissions - The list of Permissions for the plugin.
     */
    public void setPermissions(final @Nonnull List<String> permissions) {
        this.permissions = permissions;
    }

    /**
     * Checks if the remote MySQL database is enabled.
     *
     * @return If the remote MySQL database is enabled.
     */
    public boolean sqlEnabled() {
        return this.sql;
    }

    /**
     * Sets the remote MySQL database should be enabled.
     *
     * @param bool - If the remote MySQL database should be enabled.
     */
    public void setSQL(final boolean bool) {
        this.sql = bool;
    }

    /**
     * Gets the database table prefix.
     *
     * @return The database table prefix.
     */
    public @Nonnull String getTablePrefix() {
        return this.tablePrefix != null && !this.tablePrefix.isEmpty() && !this.tablePrefix.equalsIgnoreCase("NULL") ? this.tablePrefix : "";
    }

    /**
     * Sets the database table prefix.
     *
     * @param str - The database table prefix.
     */
    public void setTablePrefix(final @Nullable String str) {
        this.tablePrefix = str;
    }

    /**
     * Gets the SQL Host for the Database
     *
     * @return The SQL Host for the Database.
     */
    public @Nonnull String getSQLHost() {
        return this.sqlHost;
    }

    /**
     * Sets the SQL Host for the Database
     *
     * @param str - The SQL Host for the Database.
     */
    public void setSQLHost(final @Nullable String str) {
        this.sqlHost = str;
    }

    /**
     * Gets the SQL Port for the Database
     *
     * @return The SQL Port for the Database.
     */
    public @Nonnull String getSQLPort() {
        return this.sqlPort;
    }

    /**
     * Sets the SQL Port for the Database
     *
     * @param str - The SQL Port for the Database.
     */
    public void setSQLPort(final @Nullable String str) {
        this.sqlPort = str;
    }

    /**
     * Gets the SQL Database for the Database
     *
     * @return The SQL Database for the Database.
     */
    public @Nonnull String getSQLDatabase() {
        return this.sqlDatabase;
    }

    /**
     * Sets the SQL Database for the Database
     *
     * @param str - The SQL Database for the Database.
     */
    public void setSQLDatabase(final @Nullable String str) {
        this.sqlDatabase = str;
    }

    /**
     * Gets the SQL User for the Database
     *
     * @return The SQL User for the Database.
     */
    public @Nonnull String getSQLUser() {
        return this.sqlUser;
    }

    /**
     * Sets the SQL User for the Database
     *
     * @param str - The SQL User for the Database.
     */
    public void setSQLUser(final @Nullable String str) {
        this.sqlUser = str;
    }

    /**
     * Gets the SQL Pass for the Database
     *
     * @return The SQL Pass for the Database.
     */
    public @Nonnull String getSQLPass() {
        return this.sqlPass;
    }

    /**
     * Sets the SQL Pass for the Database
     *
     * @param str - The SQL Pass for the Database.
     */
    public void setSQLPass(final @Nullable String str) {
        this.sqlPass = str;
    }

    /**
     * Gets the list of official ChaosCore plugins.
     * These are plugins developed by @RockinChaos.
     *
     * @return The ArrayList of official ChaosCore plugins.
     */
    public @Nonnull List<String> getOfficialPlugins() {
        return Arrays.asList("ItemJoin", "FakeCreative");
    }

    /**
     * Refreshes the DataHandler cached instance.
     */
    public void refresh() {
        ReflectionUtils.refresh();
        data = new CoreData();
    }
}