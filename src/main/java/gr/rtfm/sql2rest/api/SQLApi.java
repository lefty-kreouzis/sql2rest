package gr.rtfm.sql2rest.api;

import org.springframework.web.bind.annotation.RestController;

import gr.rtfm.sql2rest.model.SQLRequest;
import gr.rtfm.sql2rest.service.SQLService;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@RestController
public class SQLApi {

    @Autowired
    SQLService sqlService;

    public void setSqlService(SQLService sqlService) {
        this.sqlService = sqlService;
    }

    @PostMapping("/select")
    public List<Map<String, Object>> executeSQL(@RequestBody SQLRequest request) 
    {
         return sqlService.executeSQL(request);
    }
}
