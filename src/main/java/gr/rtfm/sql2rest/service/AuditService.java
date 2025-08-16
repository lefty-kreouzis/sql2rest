package gr.rtfm.sql2rest.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import gr.rtfm.sql2rest.model.SQLRequest;

@Service
public class AuditService {
    
    private org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger("SQL2REST_AUDIT");
    
    /**
     * Log an SQL request for audit purposes
     * 
     * @param username The authenticated username
     * @param clientIp The client IP address
     * @param operation The operation type (SELECT, UPDATE, DELETE)
     * @param request The SQL request being executed
     * @param rowsAffected Number of rows affected (for updates/deletes)
     */
    public void logSqlOperation(String username, String clientIp, 
                                String operation, SQLRequest request, Integer rowsAffected) {
        StringBuilder auditMsg = new StringBuilder();
        auditMsg.append("TIME=").append(LocalDateTime.now()).append(" | ");
        auditMsg.append("USER=").append(username).append(" | ");
        auditMsg.append("IP=").append(clientIp).append(" | ");
        auditMsg.append("OPERATION=").append(operation).append(" | ");
        auditMsg.append("SQL=").append(request.getSql()).append(" | ");
        
        if (rowsAffected != null) {
            auditMsg.append("ROWS_AFFECTED=").append(rowsAffected);
        }
        
        log.info(auditMsg.toString());
    }
}
