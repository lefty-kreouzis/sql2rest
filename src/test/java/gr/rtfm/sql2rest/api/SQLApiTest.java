package gr.rtfm.sql2rest.api;

import gr.rtfm.sql2rest.model.SQLRequest;
import gr.rtfm.sql2rest.service.SQLService;
import gr.rtfm.sql2rest.api.SQLApi;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;






public class SQLApiTest {

    private MockMvc mockMvc;

    @Mock
    private SQLService sqlService;

    @InjectMocks
    private SQLApi sqlApi;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(sqlApi).build();
    }

    @Test
    public void testExecuteSQL() throws Exception {
        SQLRequest request = new SQLRequest();
        List<Map<String, Object>> response = Collections.singletonList(Collections.singletonMap("key", "value"));

        when(sqlService.executeSQL(request)).thenReturn(response);

        mockMvc.perform(post("/select")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"sql\":\"SELECT * FROM table\"}"))
                .andExpect(status().isOk())
                .andExpect(content().json("[{\"key\":\"value\"}]"));
    }
}