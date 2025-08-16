package gr.rtfm.sql2rest.service;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import org.springframework.stereotype.Service;

import gr.rtfm.sql2rest.model.SQLRequest;

@Service
public class SQLValidationService {
    
    private static final List<String> DANGEROUS_KEYWORDS = Arrays.asList(
        "DROP", "DELETE FROM", "TRUNCATE", "ALTER", 
        "CREATE", "GRANT", "REVOKE", "EXECUTE", "EXEC"
    );
    
    private static final Pattern COMMENT_PATTERN = Pattern.compile("--.*|/\\*.*?\\*/", Pattern.DOTALL);
    
    /**
     * Validates an SQL request to ensure it doesn't contain dangerous operations
     * @param request The SQL request to validate
     * @param allowDML Whether to allow DML operations (UPDATE, DELETE)
     * @return true if the request is safe, false otherwise
     */
    public boolean isValidRequest(SQLRequest request, boolean allowDML) {
        String sql = request.getSql().toUpperCase();
        
        // Remove comments to prevent comment-based SQL injection
        sql = COMMENT_PATTERN.matcher(sql).replaceAll("");
        
        // Check for dangerous keywords
        for (String keyword : DANGEROUS_KEYWORDS) {
            if (sql.contains(keyword)) {
                return false;
            }
        }
        
        // For SELECT endpoints, ensure only SELECT statements are allowed
        if (!allowDML && !(sql.trim().startsWith("SELECT"))) {
            return false;
        }
        
        return true;
    }
}
