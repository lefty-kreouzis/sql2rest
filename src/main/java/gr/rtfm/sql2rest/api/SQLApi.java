package gr.rtfm.sql2rest.api;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import gr.rtfm.sql2rest.model.SQLRequest;
import gr.rtfm.sql2rest.service.SQLService;
import gr.rtfm.sql2rest.service.SQLValidationService;
import gr.rtfm.sql2rest.utils.AESUtils;



@RestController
public class SQLApi {


    private org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(this.getClass());
    


    @Autowired
    SQLService sqlService;

    @Autowired
    AESUtils aesUtils;
    
    @Autowired
    SQLValidationService validationService;

    public void setSqlService(SQLService sqlService) {
        this.sqlService = sqlService;
    }

    /**
     * Execute a select SQL statement. The SQL statement is in the SQLRequest.sql field.
     * @param request The SQLRequest object containing the SQL statement and optionally, its parameters
     * @return The result set as a list of maps, where each row is a map and each column is a key in the map.
     */
    @PostMapping("/select")
    public ResponseEntity<?> executeSQL(@RequestBody SQLRequest request) 
    {
        log.info("Executing SQL: {}", request.getSql());
        
        // Validate the SQL query (only allow SELECT statements)
        if (!validationService.isValidRequest(request, false)) {
            log.warn("Invalid SQL request rejected: {}", request.getSql());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body("Invalid SQL request. Only SELECT statements are allowed.");
        }
        
        List<Map<String, Object>> result = sqlService.executeSQL(request);
        log.info("Returned {} rows", result.size());
        return ResponseEntity.ok(result);
    }

    /**
     * Execute an update SQL statement. The SQL statement is in the SQLRequest.sql field.
     * @param request The SQLRequest object containing the SQL statement and optionally, its parameters
     * @return The number of rows updated
     */
    @PostMapping("/update")
    public ResponseEntity<?> update(@RequestBody SQLRequest request) 
    {
        log.info("Executing SQL: {}", request.getSql());
        
        // Validate the SQL query (allow UPDATE statements)
        if (!validationService.isValidRequest(request, true)) {
            log.warn("Invalid SQL request rejected: {}", request.getSql());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body("Invalid SQL request. Only safe DML operations are allowed.");
        }
        
        int nRows = sqlService.update(request);
        log.info("Updated {} rows", nRows);
        return ResponseEntity.ok(nRows);
    }

    /**
     * Execute a delete SQL statement. The SQL statement is in the SQLRequest.sql field.
     * @param request The SQLRequest object containing the SQL statement and optionally, its parameters
     * @return The number of rows deleted
     */
    @PostMapping("/delete")
    public ResponseEntity<?> delete(@RequestBody SQLRequest request)
    {
        log.info("Executing SQL: {}", request.getSql());
        
        // Validate the SQL query (allow DELETE statements)
        if (!validationService.isValidRequest(request, true)) {
            log.warn("Invalid SQL request rejected: {}", request.getSql());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body("Invalid SQL request. Only safe DML operations are allowed.");
        }
        
        int nRows = sqlService.delete(request);
        log.info("Deleted {} rows", nRows);
        return ResponseEntity.ok(nRows);
    }

    @GetMapping("/encrypt")
    public String encryptPassword(@RequestParam(value = "passwd") String passwd, 
        @RequestParam(value = "key", required = false) String key) {
        String encrypted;
        if (StringUtils.isEmpty(key)) {
            log.info("Encrypting password with default key");
            encrypted = aesUtils.encrypt(passwd);
        } else {
            encrypted = aesUtils.encrypt(passwd, key);
        }
        return encrypted;
    }
    

    
    @GetMapping("/decrypt")
    public String decryptPassword(@RequestParam(value = "passwd") String passwd, 
    @RequestParam(value = "key", required = false) String key) {
        String decrypted;
        if (StringUtils.isEmpty(key)) {
            log.info("Decrypting password with default key");
            decrypted = aesUtils.decrypt(passwd);
        } else {
            decrypted = aesUtils.decrypt(passwd, key);
        }
        return decrypted;
    }
}
