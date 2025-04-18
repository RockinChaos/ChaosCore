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

import me.RockinChaos.core.Core;
import me.RockinChaos.core.CoreData;
import me.RockinChaos.core.utils.ReflectionUtils;
import me.RockinChaos.core.utils.ServerUtils;
import me.RockinChaos.core.utils.StringUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings("unused")
public class Database extends Controller {

    private static Database data;

    /**
     * Creates a new instance of SQL Connections.
     *
     * @param databaseName - The name of the database.
     */
    public Database(final @Nonnull String databaseName) {
        this.dataFolder = databaseName;
    }

    /**
     * Closes the active database connections and destroys existing Singletons.
     */
    public static void kill() {
        if (data != null) {
            data.closeConnection(true);
            data = null;
        }
    }

    /**
     * Gets the instance of the Database.
     *
     * @return The Database instance.
     */
    public static @Nonnull Database getDatabase() {
        if (data == null || !data.dataFolder.equalsIgnoreCase("database")) {
            data = new Database("database");
            try {
                data.getConnection();
            } catch (Exception e) {
                ServerUtils.logSevere("{SQL} [1] Failed to open database connection.");
                ServerUtils.sendDebugTrace(e);
            }
        }
        return data;
    }

    /**
     * Gets the instance of the Database.
     *
     * @param baseName - The database being fetched.
     * @return The Database instance.
     */
    public static @Nonnull Database getDatabase(final @Nonnull String baseName) {
        if (data == null || !data.dataFolder.equalsIgnoreCase(baseName)) {
            data = new Database(baseName);
            try {
                data.getConnection();
            } catch (Exception e) {
                ServerUtils.logSevere("{SQL} [2] Failed to open database connection.");
                ServerUtils.sendDebugTrace(e);
            }
        }
        return data;
    }

    /**
     * Executes a specified SQL statement.
     *
     * @param statement - the statement to be executed.
     */
    public void executeStatement(final @Nonnull String statement) {
        Connection conn = null;
        Statement ps = null;
        try {
            final Object[] executed = executeStatement(statement, false);
            conn = (Connection) executed[0];
            ps = (Statement) executed[1];
        } catch (Exception e) {
            ServerUtils.logSevere("{SQL} [1] Failed to execute database statement.");
            if (conn != null) {
                try {
                    ServerUtils.logSevere("{SQL} [1] Database Status: Open: " + !this.isClosed(conn) + "! Writable: " + !conn.isReadOnly() + "!");
                } catch (Exception e2) {
                    ServerUtils.logSevere("{SQL} [1] Failed to determine the Database Status.");
                }
            }
            ServerUtils.logSevere("{SQL} [1] Statement: " + statement);
            ServerUtils.sendSevereTrace(e);
        } finally {
            this.close(ps, null, conn, false);
        }
    }

    /**
     * Queries the specified row and the specified statement for a specific value.
     *
     * @param statement - the statement to be executed.
     * @param row       - the row being queried.
     * @return The result in as an object.
     */
    public @Nullable Object queryValue(final @Nonnull String statement, final @Nonnull String row) {
        Connection conn = null;
        Statement ps = null;
        ResultSet rs = null;
        Object returnValue = null;
        try {
            final Object[] executed = executeStatement(statement, true);
            conn = (Connection) executed[0];
            ps = (Statement) executed[1];
            rs = (ResultSet) executed[2];
            if (rs.next()) {
                returnValue = rs.getObject(row);
            }
        } catch (Exception e) {
            ServerUtils.logSevere("{SQL} [2] Failed to execute database statement.");
            if (conn != null) {
                try {
                    ServerUtils.logSevere("{SQL} [2] Database Status: Open: " + !this.isClosed(conn) + "! Writable: " + !conn.isReadOnly() + "!");
                } catch (Exception e2) {
                    ServerUtils.logSevere("{SQL} [2] Failed to determine the Database Status.");
                }
            }
            ServerUtils.logSevere("{SQL} [2] Statement: " + statement);
            ServerUtils.sendSevereTrace(e);
        } finally {
            this.close(ps, rs, conn, false);
        }
        return returnValue;
    }

    /**
     * Queries a row for a specified list of values.
     *
     * @param statement - the statement to be executed.
     * @param row       - the row being queried.
     * @return The result in as a listed object.
     */
    public @Nonnull List<Object> queryRow(final @Nonnull String statement, final @Nonnull String row) {
        final List<Object> objects = new ArrayList<>();
        Connection conn = null;
        Statement ps = null;
        ResultSet rs = null;
        try {
            final Object[] executed = executeStatement(statement, true);
            conn = (Connection) executed[0];
            ps = (Statement) executed[1];
            rs = (ResultSet) executed[2];
            while (rs.next()) {
                objects.add(rs.getObject(row));
            }
        } catch (Exception e) {
            ServerUtils.logSevere("{SQL} [3] Failed to execute database statement.");
            if (conn != null) {
                try {
                    ServerUtils.logSevere("{SQL} [3] Database Status: Open: " + !this.isClosed(conn) + "! Writable: " + !conn.isReadOnly() + "!");
                } catch (Exception e2) {
                    ServerUtils.logSevere("{SQL} [3] Failed to determine the Database Status.");
                }
            }
            ServerUtils.logSevere("{SQL} [3] Statement: " + statement);
            ServerUtils.sendSevereTrace(e);
        } finally {
            this.close(ps, rs, conn, false);
        }
        return objects;
    }

    /**
     * Queries a list of rows for their specified statements for a specific list of multiple values.
     *
     * @param statement - the statement to be executed.
     * @param rows      - the list of rows being queried.
     * @return The result in as a listed list of strings.
     */
    public @Nonnull List<HashMap<String, String>> queryTableData(final @Nonnull String statement, final @Nonnull String rows) {
        final List<HashMap<String, String>> existingData = new ArrayList<>();
        Connection conn = null;
        Statement ps = null;
        ResultSet rs = null;
        try {
            final Object[] executed = executeStatement(statement, true);
            conn = (Connection) executed[0];
            ps = (Statement) executed[1];
            rs = (ResultSet) executed[2];
            while (rs.next()) {
                final HashMap<String, String> columnData = new HashMap<>();
                for (final String singleRow : rows.split(", ")) {
                    if (!this.isClosed(rs) && !this.isClosed(conn)) {
                        columnData.put(singleRow, rs.getString(singleRow));
                    }
                }
                existingData.add(columnData);
            }
        } catch (Exception e) {
            ServerUtils.logSevere("{SQL} [4] Failed to execute database statement.");
            if (conn != null) {
                try {
                    ServerUtils.logSevere("{SQL} [4] Database Status: Open: " + !this.isClosed(conn) + "! Writable: " + !conn.isReadOnly() + "!");
                } catch (Exception e2) {
                    ServerUtils.logSevere("{SQL} [4] Failed to determine the Database Status.");
                }
            }
            ServerUtils.logSevere("{SQL} [4] Statement: " + statement);
            ServerUtils.sendSevereTrace(e);
        } finally {
            this.close(ps, rs, conn, false);
        }
        return existingData;
    }

    /**
     * Queries multiple rows for a specific value.
     *
     * @param statement - the statement to be executed.
     * @param row       - the list of rows being queried.
     * @return The result in as a HashMap.
     */
    public @Nonnull Map<String, List<Object>> queryMultipleRows(final @Nonnull String statement, final @Nonnull String... row) {
        final List<Object> objects = new ArrayList<>();
        final Map<String, List<Object>> map = new HashMap<>();
        Connection conn = null;
        Statement ps = null;
        ResultSet rs = null;
        try {
            final Object[] executed = executeStatement(statement, true);
            conn = (Connection) executed[0];
            ps = (Statement) executed[1];
            rs = (ResultSet) executed[2];
            while (rs.next()) {
                for (final String singleRow : row) {
                    objects.add(rs.getObject(singleRow));
                }
                for (final String singleRow : row) {
                    map.put(singleRow, objects);
                }
            }
        } catch (Exception e) {
            ServerUtils.logSevere("{SQL} [5] Failed to execute database statement.");
            if (conn != null) {
                try {
                    ServerUtils.logSevere("{SQL} [5] Database Status: Open: " + !this.isClosed(conn) + "! Writable: " + !conn.isReadOnly() + "!");
                } catch (Exception e2) {
                    ServerUtils.logSevere("{SQL} [5] Failed to determine the Database Status.");
                }
            }
            ServerUtils.logSevere("{SQL} [5] Statement: " + statement);
            ServerUtils.sendSevereTrace(e);
        } finally {
            this.close(ps, rs, conn, false);
        }
        return map;
    }

    /**
     * Checks if the column exists in the database.
     *
     * @param statement - the statement to be executed.
     * @return If the column exists.
     */
    public boolean columnExists(final @Nonnull String statement) {
        Connection conn = null;
        Statement ps = null;
        ResultSet rs = null;
        boolean columnExists = false;
        try {
            final Object[] executed = executeStatement(statement, true);
            conn = (Connection) executed[0];
            ps = (Statement) executed[1];
            rs = (ResultSet) executed[2];
            columnExists = true;
        } catch (Exception e) {
            if (!(StringUtils.containsIgnoreCase(e.getCause().getMessage(), "no such column") || StringUtils.containsIgnoreCase(e.getCause().getMessage(), "unknown column") || StringUtils.containsIgnoreCase(e.getCause().getMessage(), "unknown error"))) {
                ServerUtils.logSevere("{SQL} [6] Failed to execute database statement.");
                if (conn != null) {
                    try {
                        ServerUtils.logSevere("{SQL} [6] Database Status: Open: " + !this.isClosed(conn) + "! Writable: " + !conn.isReadOnly() + "!");
                    } catch (Exception e2) {
                        ServerUtils.logSevere("{SQL} [6] Failed to determine the Database Status.");
                    }
                }
                ServerUtils.logSevere("{SQL} [6] Statement: " + statement);
                ServerUtils.sendSevereTrace(e);
            }
        } finally {
            this.close(ps, rs, conn, false);
        }
        return columnExists;
    }

    /**
     * Checks if the table exists in the database.
     *
     * @param tableName - the name of the table.
     * @return If the table exists.
     */
    public boolean tableExists(final @Nonnull String tableName) {
        boolean tExists = false;
        Connection conn = null;
        ResultSet rs = null;
        try {
            conn = this.getConnection();
            rs = conn.getMetaData().getTables(null, null, tableName, null);
            while (rs.next()) {
                if (!this.isClosed(rs) && !this.isClosed(conn)) {
                    final String tName = rs.getString("TABLE_NAME");
                    if (tName != null && tName.equals(tableName)) {
                        tExists = true;
                        break;
                    }
                }
            }
        } catch (Exception e) {
            ServerUtils.logSevere("{SQL} [9] Failed to check if the table " + tableName + " exists.");
            ServerUtils.sendDebugTrace(e);
        } finally {
            this.close(null, rs, conn, false);
        }
        return tExists;
    }

    /**
     * Checks if the specific data set exists in the database.
     *
     * @param statement - the statement to be executed.
     * @return If the data exists.
     */
    public boolean dataExists(final @Nonnull String statement) {
        Connection conn = null;
        Statement ps = null;
        ResultSet rs = null;
        boolean dataExists = false;
        try {
            final Object[] executed = executeStatement(statement, true);
            conn = (Connection) executed[0];
            ps = (Statement) executed[1];
            rs = (ResultSet) executed[2];
            if (rs != null && !rs.isBeforeFirst()) {
                ServerUtils.logDebug("{SQL} Result set is empty.");
            } else {
                ServerUtils.logDebug("{SQL} Result set is not empty.");
                dataExists = true;
            }
        } catch (Exception e) {
            ServerUtils.logSevere("{SQL} Could not read from the " + data.dataFolder + ".db file, some " + Core.getCore().getPlugin().getName() + " features have been disabled!");
            ServerUtils.sendSevereTrace(e);
        } finally {
            this.close(ps, rs, conn, false);
        }
        return dataExists;
    }

    /**
     * Attempts to execute a SQL statement with retry logic for failed connections.
     *
     * @param statement - the SQL statement to be executed.
     * @param isQuery   - whether the statement is a query (select) or update.
     * @return An array containing the Connection, Statement, and ResultSet (if applicable).
     */
    private Object[] executeStatement(final @Nonnull String statement, final boolean isQuery, final boolean...retry) {
        Connection conn = null;
        Statement ps = null;
        ResultSet rs = null;
        try {
            conn = this.getConnection(retry.length > 0 && retry[0]);
            ps = conn.createStatement();
            if (isQuery) {
                rs = ps.executeQuery(statement);
            } else {
                ps.executeUpdate(statement);
            }
            return new Object[] { conn, ps, rs };
        } catch (SQLException e) {
            if (StringUtils.containsIgnoreCase(e.getMessage(), "SQLNonTransientConnectionException") || StringUtils.containsIgnoreCase(e.getMessage(), "CommunicationsException") || StringUtils.containsIgnoreCase(e.getMessage(), "The database has been closed") || StringUtils.containsIgnoreCase(e.getMessage(), "Communications link failure")) {
                ServerUtils.logDebug("{SQL} Failed to execute statement: " + statement);
                ServerUtils.logDebug("{SQL} Attempting to restart database connection and retry...");
                if (retry.length == 0) {
                    this.close(ps, rs, conn, true);
                    return executeStatement(statement, isQuery, true);
                } else {
                    ServerUtils.logSevere("{SQL} [Retry] An attempt was made to restart the connection but failed, this is likely a connection issue!");
                    throw new IllegalStateException("{SQL} [Retry] Failed to execute statement: " + statement);
                }
            } else {
                throw new IllegalStateException("{SQL} Failed to execute statement: " + statement, e);
            }
        }
    }

    /**
     * Closes the active database connection.
     */
    public void closeConnection(final boolean force) {
        this.close(null, null, this.connection, force);
    }
}

/**
 * Handles the current Controller instance.
 * Controls all database connection information.
 */
abstract class Controller {
    protected Connection connection;
    protected String dataFolder;

    /**
     * Gets the proper SQL connection.
     *
     * @return The SQL connection.
     */
    protected @Nonnull Connection getConnection(final boolean...force) throws SQLException {
        synchronized ("CC_SQL") {
            if (this.isClosed(this.connection) || (force.length > 0 && force[0])) {
                ServerUtils.logDebug("{SQL} Connection was detected as being closed, initializing... isClosed: " + this.connection + " " + (this.connection == null || this.connection.isClosed()) + " forced: " + (force.length > 0 && force[0]));
                if (Core.getCore().getData().sqlEnabled()) {
                    ServerUtils.logDebug("{SQL} Detected MySQL configuration, setting up!");
                    try {
                        final CoreData data = Core.getCore().getData();
                        final String database = jdbc(data, true);
                        Class<?> driverClass;
                        try {
                            driverClass = Class.forName("com.mysql.cj.jdbc.Driver");
                        } catch (ClassNotFoundException e) {
                            driverClass = Class.forName("com.mysql.jdbc.Driver");
                        }
                        final Driver driver = (Driver) driverClass.getDeclaredConstructor().newInstance();
                        ServerUtils.logInfo("Loading SQL driver: " + driver.getMajorVersion() + "." + driver.getMinorVersion() + " (" + driverClass.getName() + ")");
                        long start = System.nanoTime();
                        try {
                            this.connection = DriverManager.getConnection(database, data.getSQLUser(), data.getSQLPass());
                            try (Statement statement = this.connection.createStatement()) {
                                statement.executeUpdate("SET NAMES 'utf8'");
                            }
                        } catch (Exception e) {
                            if (StringUtils.containsIgnoreCase(e.getMessage(), "unknown database")) {
                                Statement ps = null;
                                try {
                                    final String newDatabase = jdbc(data, false);
                                    this.connection = DriverManager.getConnection(newDatabase, data.getSQLUser(), data.getSQLPass());
                                    ps = this.connection.createStatement();
                                    ps.executeUpdate("CREATE DATABASE IF NOT EXISTS " + data.getSQLDatabase() + ";");
                                } catch (Exception e2) {
                                    ServerUtils.logSevere("{SQL} [1] Failed to create the database, please manually create the database defined in your config.yml Database settings.");
                                    ServerUtils.sendSevereTrace(e);
                                } finally {
                                    this.close(ps, null, this.connection, true);
                                }
                                return this.getConnection();
                            }
                        }
                        long end = System.nanoTime();
                        double durationMs = (end - start) / 1_000_000.0;
                        ServerUtils.logInfo(String.format("Connected to MySQL database successfully (%.1f ms).", durationMs));
                    } catch (Exception e) {
                        ServerUtils.logSevere("{SQL} Unable to connect to the defined MySQL database, check your settings.");
                        ServerUtils.sendSevereTrace(e);
                    }
                } else {
                    try {
                        ServerUtils.logDebug("{SQL} Detected SQLite configuration, setting up!");
                        final File dataFolder = new File(Core.getCore().getPlugin().getDataFolder(), this.dataFolder + ".db");
                        {
                            final String database = "jdbc:sqlite:" + dataFolder.getAbsolutePath();
                            ReflectionUtils.getCanonicalClass("org.sqlite.JDBC");
                            this.connection = DriverManager.getConnection(database);
                        }
                    } catch (Exception e) {
                        ServerUtils.logSevere("{SQL} SQLite exception on initialize.");
                        ServerUtils.sendSevereTrace(e);
                    }
                }
            }
            return this.connection;
        }
    }

    /**
     * Constructs a MySQL JDBC connection URL using the provided {@link CoreData} configuration.
     *
     * @param data      The core configuration data containing SQL host, port, and database name.
     * @param database  If {@code true}, includes the database name in the JDBC URL; otherwise, constructs the URL without it.
     * @return A full JDBC connection string with MySQL connection parameters for performance and compatibility.
     */
    private String jdbc(final CoreData data, final boolean database) {
        String base = "jdbc:mysql://" + data.getSQLHost() + ":" + data.getSQLPort();
        if (database) base += "/" + data.getSQLDatabase();
        return base + "?useUnicode=true&characterEncoding=utf-8&connectTimeout=20000&socketTimeout=20000" + "&useSSL=false&allowPublicKeyRetrieval=true&useCursorFetch=true&useLocalSessionState=true" + "&rewriteBatchedStatements=true&maintainTimeStats=false";
    }

    /**
     * Checks if the Connection Object isClosed.
     *
     * @return If the Connection isClosed.
     */
    protected boolean isClosed(final @Nullable Statement object) {
        try {
            if (object == null || object.isClosed()) {
                return true;
            }
        } catch (AbstractMethodError | NoClassDefFoundError e) {
            return true;
        } catch (SQLException e) {
            ServerUtils.logSevere("{SQL} [11] Failed to check if the Statement connection is closed.");
            ServerUtils.sendDebugTrace(e);
            return true;
        }
        return false;
    }

    /**
     * Checks if the Connection Object isClosed.
     *
     * @return If the Connection isClosed.
     */
    protected boolean isClosed(final @Nonnull ResultSet object) {
        try {
            if (object.isClosed()) {
                return true;
            }
        } catch (AbstractMethodError | NoClassDefFoundError e) {
            return true;
        } catch (SQLException e) {
            ServerUtils.logSevere("{SQL} [11] Failed to check if the ResultSet connection is closed.");
            ServerUtils.sendDebugTrace(e);
            return true;
        }
        return false;
    }

    /**
     * Checks if the Connection Object isClosed.
     *
     * @return If the Connection isClosed.
     */
    protected boolean isClosed(final @Nullable Connection object) {
        try {
            if (object == null || object.isClosed()) {
                return true;
            }
        } catch (AbstractMethodError | NoClassDefFoundError e) {
            return true;
        } catch (SQLException e) {
            ServerUtils.logSevere("{SQL} [11] Failed to check if the Database connection is closed.");
            ServerUtils.sendDebugTrace(e);
            return true;
        }
        return false;
    }

    /**
     * Closes the specified connections.
     *
     * @param ps    - the PreparedStatement being closed.
     * @param rs    - the ResultSet being closed.
     * @param conn  - the Connection being closed.
     * @param force - If the connection should be forced to close.
     */
    protected void close(final @Nullable Statement ps, final @Nullable ResultSet rs, final @Nullable Connection conn, final boolean force) {
        try {
            if (ps != null && !this.isClosed(ps)) {
                ps.close();
            }
            if (rs != null && !this.isClosed(rs)) {
                rs.close();
            }
            if (conn != null && !this.isClosed(conn) && force) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    ServerUtils.logSevere("{SQL} [10] Failed to close database connection.");
                   ServerUtils.sendDebugTrace(e);
                }
            }
        } catch (SQLException e) {
            ServerUtils.logSevere("{SQL} [10] Failed to close database connection(s).");
            ServerUtils.sendDebugTrace(e);
        }
    }
}