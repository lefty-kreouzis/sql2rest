package gr.rtfm.sql2rest.model;

public class SQLParameter {

    String name;
    Object value;

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public Object getValue() {
        return value;
    }
    public void setValue(Object value) {
        this.value = value;
    }

}
