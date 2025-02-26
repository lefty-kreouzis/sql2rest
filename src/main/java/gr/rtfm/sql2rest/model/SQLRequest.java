package gr.rtfm.sql2rest.model;

import java.util.List;

public class SQLRequest {

    String sql;

    List<SQLParameter> parameters;

    public String getSql() {
        return sql;
    }

    public void setSql(String sql) {
        this.sql = sql;
    }

    public List<SQLParameter> getParameters() {
        return parameters;
    }

    public void setParameters(List<SQLParameter> parameters) {
        this.parameters = parameters;
    }

    

}
