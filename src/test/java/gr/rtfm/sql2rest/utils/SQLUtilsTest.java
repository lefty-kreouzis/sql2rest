package gr.rtfm.sql2rest.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.dao.TransientDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;

public class SQLUtilsTest {


    @Mock
    private JdbcTemplate jdbcTemplate;

    @Mock
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @Mock
    private SqlRowSet sqlRowSet;

    @InjectMocks
    private SQLUtils sqlUtils;

    @Mock
    DataSource dataSource;

    @Mock
    Connection connection;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testQueryWithRetries() {
        String sql = "SELECT * FROM table";
        // when(log.isDebugEnabled()).thenReturn(true);

        when(jdbcTemplate.queryForRowSet(anyString(), any())).thenThrow(new TransientDataAccessException("Transient error") {
        }).thenReturn(sqlRowSet);
        when(jdbcTemplate.queryForRowSet(anyString())).thenThrow(new TransientDataAccessException("Transient error") {
        }).thenReturn(sqlRowSet);

        when(jdbcTemplate.getDataSource()).thenReturn(dataSource);

        SqlRowSet result = sqlUtils.query(jdbcTemplate, sql);

        // verify(log).debug("SQLUtils.query(): Query:" + sql);
        //verify(log, times(1)).warn(anyString(), any(TransientDataAccessException.class));
        assertNotNull(result);
    }

    @Test
    public void testQueryWithRetriesAndArgs() {
        String sql = "SELECT * FROM table";
        // when(log.isDebugEnabled()).thenReturn(true);

        when(jdbcTemplate.queryForRowSet(anyString(), any(Object[].class))).thenThrow(new TransientDataAccessException("Transient error") {
        }).thenReturn(sqlRowSet);
        when(jdbcTemplate.queryForRowSet(anyString())).thenThrow(new TransientDataAccessException("Transient error") {
        }).thenReturn(sqlRowSet);

        when(jdbcTemplate.getDataSource()).thenReturn(dataSource);

        SqlRowSet result = sqlUtils.query(jdbcTemplate, sql, "name", "value");

        // verify(log).debug("SQLUtils.query(): Query:" + sql);
        //verify(log, times(1)).warn(anyString(), any(TransientDataAccessException.class));
        assertNotNull(result);
    }

    @Test
    public void testQueryWithNamedArgs() {
        String sql = "SELECT * FROM table WHERE id = :id";
        Map<String, Object> namedParameters = new HashMap<>();
        namedParameters.put("id", 1);
        // when(log.isDebugEnabled()).thenReturn(true);
        when(namedParameterJdbcTemplate.queryForRowSet(sql, namedParameters)).thenReturn(sqlRowSet);

        SqlRowSet result = sqlUtils.queryWithNamedArgs(namedParameterJdbcTemplate, sql, "id", 1);

        //verify(log).debug("SQLUtils.queryWithNamedArgs(): Query:" + sql);
        assertNotNull(result);
    }

    @Test
    public void testArgsToNamedParameters() {
        Map<String, Object> namedParameters = sqlUtils.argsToNamedParameters("param1", "value1", "param2", "value2");

        assertEquals(2, namedParameters.size());
        assertEquals("value1", namedParameters.get("param1"));
        assertEquals("value2", namedParameters.get("param2"));
    }

    @Test
    public void testUpdateWithRetries() throws SQLException {
        String sql = "UPDATE table SET column = value";
        // when(log.isDebugEnabled()).thenReturn(true);
        when(jdbcTemplate.update(anyString())).thenThrow(new TransientDataAccessException("Transient error") {
        }).thenReturn(1);
        when(jdbcTemplate.getDataSource()).thenReturn(dataSource);
        when(dataSource.getConnection()).thenReturn(connection);

        int rowsAffected = sqlUtils.update(jdbcTemplate, sql);

        // verify(log).debug("SQLUtils.update(): sql=" + sql);
        // verify(log, times(1)).warn(anyString(), any(TransientDataAccessException.class));
        assertEquals(1, rowsAffected);
    }

    @Test
    public void testUpdateWithRetriesAndArgs() throws SQLException {
        String sql = "UPDATE table SET column = value";
        // when(log.isDebugEnabled()).thenReturn(true);
        when(jdbcTemplate.update(anyString(),any(Object[].class))).thenThrow(new TransientDataAccessException("Transient error") {
        }).thenReturn(1);
        when(jdbcTemplate.getDataSource()).thenReturn(dataSource);
        when(dataSource.getConnection()).thenReturn(connection);

        int rowsAffected = sqlUtils.update(jdbcTemplate, sql, "name", "value");

        // verify(log).debug("SQLUtils.update(): sql=" + sql);
        // verify(log, times(1)).warn(anyString(), any(TransientDataAccessException.class));
        assertEquals(1, rowsAffected);
    }

    @Test
    public void testQueryForRowMaps() {
        String sql = "SELECT * FROM table WHERE id = :id";
        Map<String, Object> namedParameters = new HashMap<>();
        namedParameters.put("id", 1);
        List<Map<String, Object>> expectedRows = List.of(Map.of("id", 1, "name", "test"));
        // when(log.isDebugEnabled()).thenReturn(true);
        when(namedParameterJdbcTemplate.queryForList(sql, namedParameters)).thenReturn(expectedRows);

        List<Map<String, Object>> result = sqlUtils.queryForRowMaps(namedParameterJdbcTemplate, sql, "id", 1);

        // verify(log)
        //         .debug("queryForRowMaps(): template=" + namedParameterJdbcTemplate + " namedParams=" + namedParameters);
        assertEquals(expectedRows, result);
    }

}