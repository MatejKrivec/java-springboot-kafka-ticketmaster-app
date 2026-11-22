package com.example.tickets.hold;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.OffsetDateTime;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Real HoldController, fake HoldService, fake HTTP (MockMvc)
@WebMvcTest(HoldController.class)
class HoldControllerMockTest {

    @Autowired MockMvc mvc;
    @MockitoBean HoldService service;

    // Hold -> 200 + JSON
    @Test
    void hold_returns_200_and_json() throws Exception {
        when(service.hold(1L, 1)).thenReturn(new Hold(7, 1, OffsetDateTime.now().plusMinutes(5)));

        mvc.perform(post("/events/1/hold"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(7));
    }

    // Sold out -> 409
    @Test
    void sold_out_returns_409() throws Exception {
        when(service.hold(1L, 1)).thenThrow(new SoldOutException());

        mvc.perform(post("/events/1/hold"))
                .andExpect(status().isConflict());
    }
}
