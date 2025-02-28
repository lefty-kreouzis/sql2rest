/*
 * SLABus Code
 * (c) Copyright 2007,2008,2009,2010,2011 Parallon Systems
 */

package gr.rtfm.sql2rest.utils;

import java.io.IOException;
import java.io.Reader;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.Callable;

import javax.sql.DataSource;
import javax.sql.rowset.serial.SerialClob;
import javax.sql.rowset.serial.SerialException;

import org.springframework.dao.ConcurrencyFailureException;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.TransientDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.jdbc.support.rowset.SqlRowSetMetaData;
import org.springframework.stereotype.Service;

import gr.rtfm.sql2rest.model.SQLParameter;
import jakarta.annotation.PostConstruct;

@Service("sqlUtils")
public class SQLUtils {

	private org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(this.getClass());
	
	@PostConstruct
	public void init() {
		if (log.isInfoEnabled()) {
			log.info("SQLUtils(): set default Timezone");
		}
		TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
	}

	private static final int MAX_RETRIES = 3;
	private static final int RETRY_DELAY_SEC = 5;

	private <T> T executeWithRetry(Callable<T> callable) throws DataAccessException {
		int nRetries = 0;
		TransientDataAccessException lastException = null;

		while (nRetries < MAX_RETRIES) {
			try {
				return callable.call();
			} catch (TransientDataAccessException e) {
				if (log.isWarnEnabled()) {
					log.warn("TransientDataAccessException during execution", e);
				}
				lastException = e;
			} catch (Exception e) {
				if (log.isWarnEnabled()) {
					log.warn("Exception during execution", e);
				}
				throw new DataAccessException("Exception during execution", e) {
				};
			}
			nRetries++;
			try {
				Thread.sleep(RETRY_DELAY_SEC * 1000L);
			} catch (InterruptedException e) {
				if (log.isWarnEnabled()) {
					log.warn("InterruptedException while sleeping for retry", e);
				}
			}
		}

		throw new ConcurrencyFailureException("Failed to execute after retrying " + nRetries + " times", lastException);
	}

	public synchronized SqlRowSet query(JdbcTemplate template, String sql, Object... args) {
		if (log.isDebugEnabled()) {
			log.debug("Query:" + sql);
		}

		SqlRowSet rowSet = executeWithRetry(() -> {
			if (args == null) {
				return template.queryForRowSet(sql);
			} else {
				return template.queryForRowSet(sql, args);
			}
		});

		log.debug(sql + " => " + rowSet);
		return rowSet;
	}

	public synchronized SqlRowSet query(JdbcTemplate template, String sql) {
		if (log.isDebugEnabled()) {
			log.debug("Query:" + sql);
		}

		SqlRowSet rowSet = executeWithRetry(() -> template.queryForRowSet(sql));

		log.debug(sql + " => " + rowSet);
		return rowSet;
	}

	public Map<String, Object> argsToNamedParameters(Object... args) {
		log.debug("args=" + args);
		Map<String, Object> namedParameters = new HashMap<>();
		for (int i = 0; i < args.length; i += 2) {
			String argName = (String) args[i];
			Object value = args[i + 1];
			if (log.isDebugEnabled()) {
				log.debug("SQLUtils.argsToNamedParameters(): param=" + argName);
				log.debug("SQLUtils.argsToNamedParameters(): value=" + value + ", "
						+ (value != null ? value.getClass() : "NULL"));
			}
			namedParameters.put(argName, value);
		}
		log.debug("namedParameters=" + namedParameters);
		return namedParameters;
	}

	public synchronized SqlRowSet queryWithNamedArgs(NamedParameterJdbcTemplate template, String sql, Object... args) {
		if (log.isDebugEnabled()) {
			log.debug("SQLUtils.queryWithNamedArgs(): Query:" + sql);
		}
		Map<String, Object> namedParameters = argsToNamedParameters(args);
		return template.queryForRowSet(sql, namedParameters);

	}

	public synchronized <T> List<T> query(NamedParameterJdbcTemplate template, String sql, RowMapper<T> rowMapper,
			Class<T> returnType, Object... args) {
		if (log.isDebugEnabled()) {
			log.debug("SQLUtils.query(): Query:" + sql);
		}
		Map<String, Object> namedParameters = argsToNamedParameters(args);

		return executeWithRetry(() -> template.query(sql, namedParameters, rowMapper));
	}

	public synchronized <T> List<T> query(JdbcTemplate template, String sql, RowMapper<T> rowMapper,
			Class<T> returnType, Object... args) {
		if (log.isDebugEnabled()) {
			log.debug("SQLUtils.query(): sql=" + sql);
		}

		return executeWithRetry(() -> {
			return template.query(sql, rowMapper, args);
		});
	}

	public synchronized <T> List<T> query(JdbcTemplate template, String sql, RowMapper<T> rowMapper,
			Class<T> returnType) {
		if (log.isDebugEnabled()) {
			log.debug("SQLUtils.query(): sql=" + sql);
		}

		return executeWithRetry(() -> template.query(sql, rowMapper));
	}

	public List<Map<String, Object>> queryForRowMaps(NamedParameterJdbcTemplate template, String sql, Object... args) {
		Map<String, Object> namedParameters = argsToNamedParameters(args);

		if (log.isDebugEnabled()) {
			log.debug("queryForRowMaps(): template=" + template + " namedParams=" + namedParameters);
		}

		List<Map<String, Object>> rows = template.queryForList(sql, namedParameters);
		return rows;
	}

	public synchronized int update(JdbcTemplate template, String sql, Object... args) {
		if (log.isDebugEnabled()) {
			log.debug("SQLUtils.update(): sql=" + sql);
		}

		return executeWithRetry(() -> {
			DataSource dataSource = template.getDataSource();
			dataSource.getConnection().setAutoCommit(true);

			int nrows;
			if (args == null) {
				nrows = template.update(sql);
			} else {
				nrows = template.update(sql, args);
			}
			dataSource.getConnection().commit();
			return nrows;
		});
	}

	public synchronized int update(JdbcTemplate template, String sql) {
		if (log.isDebugEnabled()) {
			log.debug("SQLUtils.update(): sql=" + sql);
		}

		return executeWithRetry(() -> {
			DataSource dataSource = template.getDataSource();
			dataSource.getConnection().setAutoCommit(true);

			int nrows = template.update(sql);
			dataSource.getConnection().commit();
			return nrows;
		});
	}

	public void dumpResults(org.apache.commons.logging.Log log, SqlRowSet rs) {
		if (log.isDebugEnabled()) {
			SqlRowSetMetaData metaData = rs.getMetaData();
			for (int c = 1; c < metaData.getColumnCount(); c++) {
				log.debug("SQLUtils.dumpResults():" + metaData.getColumnName(c) + ":" + rs.getObject(c));
			}
		}
	}

	public void dumpResults(org.apache.commons.logging.Log log, ResultSet rs) {
		if (log.isTraceEnabled()) {
			log.trace("SQLUtils.dumpResults():" + resultToString(rs));
		}
	}

	public String resultToString(ResultSet rs) {
		StringBuilder sb = new StringBuilder();

		sb.append("[");
		try {
			ResultSetMetaData metaData = rs.getMetaData();
			for (int c = 1; c < metaData.getColumnCount(); c++) {
				sb.append(metaData.getColumnName(c) + ":" + rs.getObject(c) + ", ");
			}
		} catch (SQLException e) {
			if (log.isErrorEnabled()) {
				log.error("SQLUtils.dumpResults()", e);
			}
		}
		sb.append("]");
		return sb.toString();
	}

	public String clobToString(SerialClob clob) {
		Reader is;
		try {
			is = clob.getCharacterStream();
			StringBuilder sb = new StringBuilder();
			int length = (int) clob.length();

			if (length > 0) {
				char[] buffer = new char[length];
				try {
					while (is.read(buffer) != -1)
						sb.append(buffer);

					return new String(sb);
				} catch (IOException e) {
					if (log.isErrorEnabled()) {
						log.error("clobToString(): error", e);
					}
				}
			} else {
				return "";
			}
		} catch (SerialException e1) {
			if (log.isErrorEnabled()) {
				log.error("clobToString(): error", e1);
			}
		}

		return null;
	}

	public Map<String, Object> argsToNamedParameters(List<SQLParameter> parameters) {
		Map<String, Object> namedParameters = new HashMap<>();
		for (SQLParameter param : parameters) {
			String argName = param.getName();
			Object value = param.getValue();
			if (log.isDebugEnabled()) {
				log.debug("SQLUtils.argsToNamedParameters(): param=" + argName);
				log.debug("SQLUtils.argsToNamedParameters(): value=" + value + ", "
						+ (value != null ? value.getClass() : "NULL"));
			}
			namedParameters.put(argName, value);
		}
		return namedParameters;
	}

	public List<Map<String, Object>> executeSQL(NamedParameterJdbcTemplate template, String sql,
			List<SQLParameter> parameters) {
		Map<String, Object> namedParameters = argsToNamedParameters(parameters);
		if (log.isDebugEnabled()) {
			log.debug("executeSQL(): sql=" + sql + " namedParams=" + namedParameters);
		}

		List<Map<String, Object>> rows = template.queryForList(sql, namedParameters);
		return rows;
	}

	public int executeUpdateSQL(NamedParameterJdbcTemplate template, String sql,
			List<SQLParameter> parameters) {
		Map<String, Object> namedParameters = argsToNamedParameters(parameters);
		if (log.isDebugEnabled()) {
			log.debug("executeSQL(): sql=" + sql + " namedParams=" + namedParameters);
		}

		int nRows = template.update(sql, namedParameters);
		return nRows;
	}
}
