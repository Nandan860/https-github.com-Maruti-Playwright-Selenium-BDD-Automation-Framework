package com.framework.db;

import com.framework.config.ConfigManager;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;
import java.util.*;

/**
 * Thin wrapper around HikariCP for PostgreSQL.
 *
 * Separate pools for "source" (legacy) and "target" (new) DBs —
 * useful for ETL/migration validation.
 *
 * Usage:
 *   DBClient source = DBClient.source();
 *   List<Map<String,Object>> rows = source.query("SELECT * FROM accounts WHERE id=?", id);
 */
public class DBClient {

    private static final Logger log = LogManager.getLogger(DBClient.class);

    private static volatile DBClient SOURCE_INSTANCE;
    private static volatile DBClient TARGET_INSTANCE;

    private final HikariDataSource ds;

    private DBClient(String prefix) {
        HikariConfig cfg = new HikariConfig();
        cfg.setJdbcUrl(ConfigManager.get(prefix + ".db.url"));
        cfg.setUsername(ConfigManager.get(prefix + ".db.user"));
        cfg.setPassword(ConfigManager.get(prefix + ".db.password"));
        cfg.setDriverClassName("org.postgresql.Driver");
        cfg.setMaximumPoolSize(ConfigManager.getInt(prefix + ".db.pool.size", 5));
        cfg.setMinimumIdle(2);
        cfg.setConnectionTimeout(30_000);
        cfg.setIdleTimeout(600_000);
        cfg.setMaxLifetime(1_800_000);
        cfg.setPoolName(prefix.toUpperCase() + "-pool");
        this.ds = new HikariDataSource(cfg);
        log.info("DB pool '{}' initialised → {}", cfg.getPoolName(), cfg.getJdbcUrl());
    }

    public static DBClient source() {
        if (SOURCE_INSTANCE == null) {
            synchronized (DBClient.class) {
                if (SOURCE_INSTANCE == null) SOURCE_INSTANCE = new DBClient("source");
            }
        }
        return SOURCE_INSTANCE;
    }

    public static DBClient target() {
        if (TARGET_INSTANCE == null) {
            synchronized (DBClient.class) {
                if (TARGET_INSTANCE == null) TARGET_INSTANCE = new DBClient("target");
            }
        }
        return TARGET_INSTANCE;
    }

    // ── Query helpers ───────────────────────────────────────────────────────

    /**
     * Run a SELECT and return rows as List<Map<columnName, value>>.
     */
    public List<Map<String, Object>> query(String sql, Object... params) {
        List<Map<String, Object>> results = new ArrayList<>();
        try (Connection conn = ds.getConnection();
             PreparedStatement ps = buildStatement(conn, sql, params);
             ResultSet rs = ps.executeQuery()) {

            ResultSetMetaData meta = rs.getMetaData();
            int cols = meta.getColumnCount();
            while (rs.next()) {
                Map<String, Object> row = new LinkedHashMap<>();
                for (int i = 1; i <= cols; i++) {
                    row.put(meta.getColumnName(i).toLowerCase(), rs.getObject(i));
                }
                results.add(row);
            }
        } catch (SQLException e) {
            throw new RuntimeException("DB query failed: " + sql, e);
        }
        return results;
    }

    /**
     * Get a single scalar value (e.g. COUNT, SUM).
     */
    public Object queryScalar(String sql, Object... params) {
        List<Map<String, Object>> rows = query(sql, params);
        if (rows.isEmpty()) return null;
        return rows.get(0).values().iterator().next();
    }

    /**
     * Run INSERT / UPDATE / DELETE. Returns affected row count.
     */
    public int execute(String sql, Object... params) {
        try (Connection conn = ds.getConnection();
             PreparedStatement ps = buildStatement(conn, sql, params)) {
            return ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("DB execute failed: " + sql, e);
        }
    }

    /**
     * Compare two tables (source vs target) on a given key column.
     * Returns a list of discrepancies.
     */
    public List<String> compareWith(DBClient other, String table, String keyColumn,
                                    String... columns) {
        List<String> issues = new ArrayList<>();
        String cols = columns.length == 0 ? "*" : String.join(",", columns);
        String sql = "SELECT " + keyColumn + "," + cols + " FROM " + table + " ORDER BY " + keyColumn;

        List<Map<String, Object>> sourceRows = this.query(sql);
        List<Map<String, Object>> targetRows = other.query(sql);

        Map<Object, Map<String, Object>> targetMap = new LinkedHashMap<>();
        for (Map<String, Object> row : targetRows) {
            targetMap.put(row.get(keyColumn), row);
        }

        for (Map<String, Object> sRow : sourceRows) {
            Object key = sRow.get(keyColumn);
            Map<String, Object> tRow = targetMap.get(key);
            if (tRow == null) {
                issues.add("MISSING in target: " + keyColumn + "=" + key);
                continue;
            }
            for (String col : sRow.keySet()) {
                Object sv = sRow.get(col), tv = tRow.get(col);
                if (!Objects.equals(sv, tv)) {
                    issues.add("MISMATCH [" + keyColumn + "=" + key + "] col=" + col
                               + " source=" + sv + " target=" + tv);
                }
            }
        }
        return issues;
    }

    /**
     * Chunked table comparison — memory-efficient for large tables.
     * Processes CHUNK_SIZE rows at a time to avoid loading millions of rows into memory.
     */
    public List<String> compareWithChunked(DBClient other, String table,
                                           String keyColumn, int chunkSize,
                                           String... columns) {
        List<String> issues = new ArrayList<>();
        String cols = columns.length == 0 ? "*" : keyColumn + "," + String.join(",", columns);

        long totalRows = ((Number) this.queryScalar("SELECT COUNT(*) FROM " + table)).longValue();
        log.info("Starting chunked comparison: table={} totalRows={} chunkSize={}", table, totalRows, chunkSize);

        for (long offset = 0; offset < totalRows; offset += chunkSize) {
            String sql = "SELECT " + cols + " FROM " + table
                    + " ORDER BY " + keyColumn
                    + " LIMIT " + chunkSize + " OFFSET " + offset;

            List<Map<String, Object>> sourceChunk = this.query(sql);
            List<Map<String, Object>> targetChunk = other.query(sql);

            Map<Object, Map<String, Object>> targetMap = new LinkedHashMap<>();
            for (Map<String, Object> row : targetChunk) {
                targetMap.put(row.get(keyColumn), row);
            }

            for (Map<String, Object> sRow : sourceChunk) {
                Object key = sRow.get(keyColumn);
                Map<String, Object> tRow = targetMap.get(key);
                if (tRow == null) {
                    issues.add("MISSING in target: " + keyColumn + "=" + key);
                    continue;
                }
                for (String col : sRow.keySet()) {
                    Object sv = sRow.get(col), tv = tRow.get(col);
                    if (!Objects.equals(String.valueOf(sv), String.valueOf(tv))) {
                        issues.add("MISMATCH [" + keyColumn + "=" + key + "] col=" + col
                                + " source=" + sv + " target=" + tv);
                    }
                }
            }
            log.info("  Compared chunk offset={} → issues so far: {}", offset, issues.size());
        }
        return issues;
    }

    /** Check if DB connection is healthy. */
    public boolean isHealthy() {
        try {
            Object result = queryScalar("SELECT 1");
            return result != null;
        } catch (Exception e) {
            log.error("DB health check failed: {}", e.getMessage());
            return false;
        }
    }

    /** Returns pool name for logging. */
    public String getPoolName() {
        return ds.getPoolName();
    }

    public void close() {
        if (ds != null && !ds.isClosed()) {
            log.info("Closing DB pool: {}", ds.getPoolName());
            ds.close();
        }
    }

    /** Called from Hooks.AfterAll — shuts down both pools cleanly. */
    public static void closeAll() {
        if (SOURCE_INSTANCE != null) { SOURCE_INSTANCE.close(); SOURCE_INSTANCE = null; }
        if (TARGET_INSTANCE != null) { TARGET_INSTANCE.close(); TARGET_INSTANCE = null; }
    }

    private PreparedStatement buildStatement(Connection conn, String sql, Object[] params) throws SQLException {
        PreparedStatement ps = conn.prepareStatement(sql);
        for (int i = 0; i < params.length; i++) ps.setObject(i + 1, params[i]);
        return ps;
    }
}
