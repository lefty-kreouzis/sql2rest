package gr.rtfm.sql2rest.service;

import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import gr.rtfm.sql2rest.model.SQLRequest;
import gr.rtfm.sql2rest.utils.SQLUtils;

@Service
public class SQLService {

    @Autowired
    SQLUtils sqlUtils; 

    @Autowired
    DataSource dataSource;

    public List<Map<String, Object>> executeSQL(SQLRequest request) {
        NamedParameterJdbcTemplate template = new NamedParameterJdbcTemplate(dataSource);
        return sqlUtils.executeSQL(template, request.getSql(), request.getParameters());
    }
}
