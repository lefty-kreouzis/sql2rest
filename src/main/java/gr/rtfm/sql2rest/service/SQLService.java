package gr.rtfm.sql2rest.service;

import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import gr.rtfm.sql2rest.model.SQLRequest;
import gr.rtfm.sql2rest.utils.SQLUtils;
import jakarta.annotation.PostConstruct;

@Service
public class SQLService {

    private final org.apache.commons.logging.Log log = org.apache.commons.logging.LogFactory.getLog(this.getClass());

    @Autowired
    SQLUtils sqlUtils; 

    public void setSqlUtils(SQLUtils sqlUtils) {
        this.sqlUtils = sqlUtils;
    }

    @Autowired
    DataSource dataSource;


    NamedParameterJdbcTemplate template;
    public void setTemplate(NamedParameterJdbcTemplate template) {
        this.template = template;
    }

    @PostConstruct
    public void init() {
        template = new NamedParameterJdbcTemplate(dataSource);
    }

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public List<Map<String, Object>> executeSQL(SQLRequest request) {

        log.debug("Executing SQL: " + request.getSql());
        List<Map<String, Object>> result = sqlUtils.executeSQL(template, request.getSql(), request.getParameters());
        log.debug("Returned " + result.size() + " rows");
        return result;
    }


}
