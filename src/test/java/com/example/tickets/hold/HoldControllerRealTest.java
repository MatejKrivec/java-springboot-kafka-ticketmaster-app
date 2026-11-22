package com.example.tickets.hold;

import com.example.tickets.TestcontainersConfig;
import com.example.tickets.event.EventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Whole flow: HTTP -> controller -> service -> repositories -> test Postgres
@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfig.class)
class HoldControllerRealTest {

    @Autowired MockMvc mvc;
    @Autowired EventRepository events;
    @Autowired JdbcTemplate jdbc;

    @BeforeEach
    void resetDatabase() {
        jdbc.update("DELETE FROM holds");
        jdbc.update("UPDATE events SET tickets_left = 10 WHERE id = 1");
    }

    // Hold -> 200 + JSON, one ticket fewer in the database
    @Test
    void hold_returns_200_and_json() throws Exception {
        mvc.perform(post("/events/1/hold"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantity").value(1));

        assertThat(events.ticketsLeft(1L)).isEqualTo(9);
    }

    // No tickets left -> 409
    @Test
    void sold_out_returns_409() throws Exception {
        jdbc.update("UPDATE events SET tickets_left = 0 WHERE id = 1");

        mvc.perform(post("/events/1/hold"))
                .andExpect(status().isConflict());
    }
}
