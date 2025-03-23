package gr.rtfm.sql2rest.api;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import gr.rtfm.sql2rest.model.SQLRequest;
import gr.rtfm.sql2rest.service.SQLService;
import gr.rtfm.sql2rest.utils.AESUtils;



@RestController
public class SQLApi {


    private org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(this.getClass());
    


    @Autowired
    SQLService sqlService;

    @Autowired
    AESUtils aesUtils;

    public void setSqlService(SQLService sqlService) {
        this.sqlService = sqlService;
    }

    /**
     * Execute a select SQL statement. The SQL statement is in the SQLRequest.sql field.
     * @param request The SQLRequest object containing the SQL statement and optionally, its parameters
     * @return The result set as a list of maps, where each row is a map and each column is a key in the map.
     */
    @PostMapping("/select")
    public List<Map<String, Object>> executeSQL(@RequestBody SQLRequest request) 
    {
        log.info("Executing SQL: {}", request.getSql());
        List<Map<String, Object>> result = sqlService.executeSQL(request);
        log.info("Returned {} rows",result.size());
        return result;
    }

    /**
     * Execute an update SQL statement. The SQL statement is in the SQLRequest.sql field.
     * @param request The SQLRequest object containing the SQL statement and optionally, its parameters
     * @return The number of rows updated
     */
    @PostMapping("/update")
    public int update(@RequestBody SQLRequest request) 
    {
        log.info("Executing SQL: {}", request.getSql());
        int nRows = sqlService.update(request);
        log.info("Updated {} rows", nRows);
        return nRows;
    }

    /**
     * Execute a delete SQL statement. The SQL statement is in the SQLRequest.sql field.
     * @param request The SQLRequest object containing the SQL statement and optionally, its parameters
     * @return The number of rows deleted
     */
    @PostMapping("/delete")
    public int delete(@RequestBody SQLRequest request)
    {
        log.info("Executing SQL: {}", request.getSql());
        int nRows = sqlService.delete(request);
        log.info("Deleted {} rows", nRows);
        return nRows;
    }

    @GetMapping("/encrypt")
    public String encryptPassword(@RequestParam(value = "passwd") String passwd, 
        @RequestParam(value = "key", required = false) String key) {
        String encrypted;
        if ( StringUtils.isEmpty(key) )
        {
            log.info("Encrypting password with default key");
            encrypted = aesUtils.encrypt(passwd);
        }
        else
        {
            encrypted = aesUtils.encrypt(passwd, key);
        }
        return encrypted;
    }
    

    
    @GetMapping("/dencrypt")
    public String dencryptPassword(@RequestParam(value = "passwd") String passwd, 
    @RequestParam(value = "key", required = false) String key) {
        String encrypted;
        if ( StringUtils.isEmpty(key) )
        {
            log.info("Decrypting password with default key");
            encrypted = aesUtils.decrypt(passwd);
        }
        else
        {
            encrypted = aesUtils.decrypt(passwd, key);
        }
        return encrypted;
    }
}
