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
package me.RockinChaos.core.utils.sql;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import me.RockinChaos.core.Core;
import me.RockinChaos.core.utils.SchedulerUtils;
import me.RockinChaos.core.utils.ServerUtils;

public class SQL {
	
	private Map < String, List<Object> > databaseData = new HashMap < String, List<Object> >();
	
	private static SQL data;
	
   /**
    * Creates a new SQLData instance.
    * 
    */
	public SQL() {
		Database.kill(); {
			this.createTables();
			ServerUtils.logDebug("{SQL} Database Connected."); 
		}
	}
	
   /**
    * Forces the SQLData to load.
    * 
    */
	public void load() {
		this.loadData();	
	}
	
   /**
    * Removes tables from the database.
    * 
    */
	public void purgeDatabase() {
		SchedulerUtils.runSingleAsync(() -> {
			for (String table: Core.getCore().getData().getDatabaseData().keySet()) {
				synchronized("CC_SQL") {
					if (Database.getDatabase() != null && Database.getDatabase().tableExists(Core.getCore().getData().getTablePrefix() + table)) {
						Database.getDatabase().executeStatement("DROP TABLE IF EXISTS "  + Core.getCore().getData().getTablePrefix() + table);
					}
				} 
			} { 
				this.databaseData.clear(); {
					this.createTables(); 
				}
			}
		});
	}
	
   /**
    * Saves the table data for the specified Object.
    * 
    * @param object - The Object data being saved.
    */
	public void saveData(final Object object) {
		if (object != null) { 
			try {
				final String tableName = (String) object.getClass().getMethod("getTableName").invoke(object);
				final String tableHeaders = (String) object.getClass().getMethod("getTableHeaders").invoke(object);
				final String tableInserts = (String) object.getClass().getMethod("getInsertValues").invoke(object);
				if (Core.getCore().getPlugin().isEnabled()) {
					SchedulerUtils.runSingleAsync(() -> {
						synchronized("CC_SQL") {
							Database.getDatabase().executeStatement("INSERT INTO " + Core.getCore().getData().getTablePrefix() + tableName + " (" + tableHeaders + ") VALUES (" + tableInserts + ")");
						}
					});
				} else {
					synchronized("CC_SQL") {
						Database.getDatabase().executeStatement("INSERT INTO " + Core.getCore().getData().getTablePrefix() + tableName + " (" + tableHeaders + ") VALUES (" + tableInserts + ")");
					}
				}
				if (this.databaseData.get(tableName) != null) {
					final List <Object> h1 = this.databaseData.get(tableName);
					h1.add(object);
					this.databaseData.put(tableName, h1);
				} else {
					final List <Object> h1 = new ArrayList<Object>();
					h1.add(object);
					this.databaseData.put(tableName, h1);
				}
			} catch (Exception e) {
				ServerUtils.sendSevereTrace(e);
			}
		}
	}
	
   /**
    * Removes the table data for the specified Object.
    * 
    * @param object - The Object being accessed.
    */
	public void removeData(final Object object) {
		if (object != null) { 
			try {
				final String tableName = (String) object.getClass().getMethod("getTableName").invoke(object);
				if (this.databaseData.get(tableName) != null && !this.databaseData.get(tableName).isEmpty()) {
					final Iterator<Object> dataSet = this.databaseData.get(tableName).iterator();
					while (dataSet.hasNext()) {
						final Object dataObject = dataSet.next();
						final String dataSetName = (String) dataObject.getClass().getMethod("getTableName").invoke(dataObject);
						final String tableRemoval = (String) dataObject.getClass().getMethod("getTableRemoval").invoke(dataObject);
						final String tableRemovals = (String) dataObject.getClass().getMethod("getRemovalValues").invoke(dataObject);
						if (dataObject != null && tableName.equals(dataSetName)) {
							final Boolean equalsDate = (Boolean) object.getClass().getMethod("equalsData", Object.class, Object.class).invoke(object, object, dataObject);
							if (equalsDate) {
								if (Core.getCore().getPlugin().isEnabled()) {
									SchedulerUtils.runSingleAsync(() -> {
										synchronized("CC_SQL") {
											Database.getDatabase().executeStatement("DELETE FROM " + Core.getCore().getData().getTablePrefix() + dataSetName + " WHERE (" + tableRemoval + ") = (" + tableRemovals + ")");
										}
									});
								} else {
									synchronized("CC_SQL") {
										Database.getDatabase().executeStatement("DELETE FROM " + Core.getCore().getData().getTablePrefix() + dataSetName + " WHERE (" + tableRemoval + ") = (" + tableRemovals + ")");
									}
								}
								dataSet.remove();
							}
						}
					}
				}
			} catch (Exception e) {
				ServerUtils.sendSevereTrace(e);
			}
		}
	}
	
   /**
    * Gets the table data for the specified Object.
    * 
    * @param object - The Object being accessed.
    * @return The found table data.
    */
	public Object getData(final Object object) {
		if (object != null) { 
			try {
				final String tableName = (String) object.getClass().getMethod("getTableName").invoke(object);
				if (this.databaseData.get(tableName) != null && !this.databaseData.get(tableName).isEmpty()) {
					final Iterator<Object> dataSet = this.databaseData.get(tableName).iterator();
					while (dataSet.hasNext()) {
						final Object dataObject = dataSet.next();
						final String dataSetName = (String) dataObject.getClass().getMethod("getTableName").invoke(dataObject);
						final Boolean equalsDate = (Boolean) object.getClass().getMethod("equalsData", Object.class, Object.class).invoke(object, object, dataObject);
						if (dataObject != null && dataSetName.equals(tableName) && equalsDate) {
							return dataObject;
						}
					}
				}
			} catch (Exception e) {
				ServerUtils.sendSevereTrace(e);
			}
		}
		return null;
	}
	
   /**
    * Gets the table data list for the specified Object.
    * 
    * @param object - The Object being accessed.
    * @return The found table data list.
    */
	public List<Object> getDataList(final Object object) {
		final List<Object> dataList = new ArrayList<Object>();
		if (object != null) {
			try {
				final String tableName = (String) object.getClass().getMethod("getTableName").invoke(object);
				if (this.databaseData.get(tableName) != null && !this.databaseData.get(tableName).isEmpty()) {
					final Iterator<Object> dataSet = this.databaseData.get(tableName).iterator();
					while (dataSet.hasNext()) {
						final Object dataObject = dataSet.next();
						final Boolean isTemporary = (Boolean) object.getClass().getMethod("isTemporary").invoke(object);
						final String dataSetName = (String) dataObject.getClass().getMethod("getTableName").invoke(dataObject);
						final Boolean equalsDate = (Boolean) object.getClass().getMethod("equalsData", Object.class, Object.class).invoke(object, object, dataObject);
						if (dataObject != null && dataSetName.equals(tableName) && (isTemporary || equalsDate)) {
							dataList.add(dataObject);
						}
					}
				}
			} catch (Exception e) {
				ServerUtils.sendSevereTrace(e);
			}
		}
		return dataList;
	}
	
	
   /**
    * Gets the Equal Data of the Object
    * 
    * @param object - The Object being accessed.
    * @return If the data is equal.
    */	
	public boolean hasDataSet(final Object object) {
		if (object != null) {
			try {
				final String tableName = (String) object.getClass().getMethod("getTableName").invoke(object);
				final Iterator<Object> dataSet = this.databaseData.get(tableName).iterator();
				while (dataSet.hasNext()) {
					final Object dataObject = dataSet.next();
					final String dataSetName = (String) dataObject.getClass().getMethod("getTableName").invoke(dataObject);
					final Boolean equalsDate = (Boolean) object.getClass().getMethod("equalsData", Object.class, Object.class).invoke(object, object, dataObject);
					if (dataSetName.equals(tableName) && equalsDate) {
						return true;
					}
				}
			} catch (Exception e) {
				ServerUtils.sendSevereTrace(e);
			}
		}
		return false;
	}
	
   /**
    * Loads all the database data into memory.
    * 
    */
	private void loadData() {
		final Map < String, List<Object> > databaseData = Core.getCore().getData().getDatabaseData();
		if (databaseData != null) {
			this.databaseData = databaseData;
		}
	}
	
   /**
    * Creates the missing database tables.
    * 
    */
	public void createTables() {
		Core.getCore().getData().getCreateTables().run(); {
	       Core.getCore().getData().getAlterTables().run(); 
		}
	}
	
   /**
    * Attempts to refresh the SQL instance.
    * 
    * @param checkSafety - If the SQL instance should be force renewed or attempted if safe.
    * @return If the refresh was successful.
    */
	public boolean refresh(final boolean checkSafety) {
		if (!checkSafety) {
			data = new SQL();
			return true;
		} else if ((!Core.getCore().getData().sqlEnabled() && Database.getDatabase().getConstant()) || (Core.getCore().getData().sqlEnabled() && !Database.getDatabase().getConstant())) {
			data = new SQL();
			return true;
		}
		return false;
	}
	
   /**
    * Gets the instance of the SQL.
    * 
    * @param regen - If the SQL should have a new instance created.
    * @return The SQL instance.
    */
    public static SQL getSQL() { 
        if (data == null) { data = new SQL(); }
        return data; 
    } 
}