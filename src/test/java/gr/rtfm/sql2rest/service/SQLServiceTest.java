package gr.rtfm.sql2rest.service;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import java.util.*;
import javax.sql.DataSource;

import org.checkerframework.checker.units.qual.s;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import gr.rtfm.sql2rest.model.SQLRequest;
import gr.rtfm.sql2rest.utils.SQLUtils;








public class SQLServiceTest {

    @InjectMocks
    private SQLService sqlService;

    @Mock
    private SQLUtils sqlUtils;

    @Mock
    private DataSource dataSource;

    @Mock
    private NamedParameterJdbcTemplate template;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        //when(new NamedParameterJdbcTemplate(dataSource)).thenReturn(template);
    }

    @Test
    public void testExecuteSQL() {
        SQLRequest request = new SQLRequest();
        request.setSql("SELECT * FROM users");
        request.setParameters(new LinkedList<>());

        sqlService.setDataSource(dataSource);
        sqlService.setSqlUtils(sqlUtils);
        sqlService.setTemplate(template);

        List<Map<String, Object>> expectedResult = new ArrayList<>();
        when(sqlUtils.executeSQL(any(NamedParameterJdbcTemplate.class), eq(request.getSql()), eq(request.getParameters())))
            .thenReturn(expectedResult);

        List<Map<String, Object>> result = sqlService.executeSQL(request);

        assertEquals(expectedResult, result);
        verify(sqlUtils, times(1)).executeSQL(any(NamedParameterJdbcTemplate.class), eq(request.getSql()), eq(request.getParameters()));
    }
}