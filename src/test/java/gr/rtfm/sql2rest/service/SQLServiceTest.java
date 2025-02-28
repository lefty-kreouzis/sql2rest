package gr.rtfm.sql2rest.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import gr.rtfm.sql2rest.model.SQLParameter;
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

    @Test
    public void testUpdate() {
        SQLRequest request = new SQLRequest();
        request.setSql("UPDATE users SET name = :name WHERE id = :id");
        List<SQLParameter> parameters = new LinkedList<>();
        parameters.add(new SQLParameter("name", "John Doe"));
        parameters.add(new SQLParameter("id", 1));
        request.setParameters(parameters);

        sqlService.setDataSource(dataSource);
        sqlService.setSqlUtils(sqlUtils);
        sqlService.setTemplate(template);

        when(sqlUtils.executeUpdateSQL(any(NamedParameterJdbcTemplate.class), eq(request.getSql()), eq(request.getParameters())))
            .thenReturn(1);

        int result = sqlService.update(request);

        assertEquals(1, result);
        verify(sqlUtils, times(1)).executeUpdateSQL(any(NamedParameterJdbcTemplate.class), eq(request.getSql()), eq(request.getParameters()));
    }
}