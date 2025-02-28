package gr.rtfm.sql2rest.api;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import gr.rtfm.sql2rest.model.SQLRequest;
import gr.rtfm.sql2rest.service.SQLService;


@RestController
public class SQLApi {


    private org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(this.getClass());
    


    @Autowired
    SQLService sqlService;

    public void setSqlService(SQLService sqlService) {
        this.sqlService = sqlService;
    }

    @PostMapping("/select")
    public List<Map<String, Object>> executeSQL(@RequestBody SQLRequest request) 
    {
        log.info("Executing SQL: {}", request.getSql());
        List<Map<String, Object>> result = sqlService.executeSQL(request);
        log.info("Returned {} rows",result.size());
        return result;
    }

    @PostMapping("/update")
    public int update(@RequestBody SQLRequest request) 
    {
        log.info("Executing SQL: {}", request.getSql());
        int nRows = sqlService.update(request);
        log.info("Updated {} rows", nRows);
        return nRows;
    }
}
