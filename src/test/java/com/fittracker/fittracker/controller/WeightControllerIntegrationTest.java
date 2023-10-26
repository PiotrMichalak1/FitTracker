package com.fittracker.fittracker.controller;

import com.fittracker.fittracker.entity.Weight;
import com.fittracker.fittracker.repository.WeightRepository;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.net.URI;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Testcontainers
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
public class WeightControllerIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    WeightRepository weightRepository;

    private final static String ENDPOINT = "/api/v1/weight";

    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16");

    @BeforeAll
    static void setUp() {
        postgres.start();
    }

    @BeforeEach
    void beforeSetUp() {
        weightRepository.deleteAll();
    }

    @Nested
    class Post {

        @Test
        void givenValidPostRequest_shouldSaveAnReturnWeight() throws Exception {
            assertThat(weightRepository.findAll()).hasSize(0);

            mockMvc.perform(post(URI.create(ENDPOINT))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"date\":\"2023-08-03\",\"value\":13.2}"))
                    .andExpect(status().isCreated())
                    .andExpect(content().string("{\"date\":\"2023-08-03\",\"value\":13.2}"));

            var expected = new Weight(LocalDate.of(2023, 8, 3), 13.2);

            var result = weightRepository.findAll();
            assertThat(result).hasSize(1);
            assertThat(result.iterator().next()).
                    usingRecursiveComparison().ignoringFields("id").isEqualTo(expected);

        }

        @Test
        void givenValidPostRequestWithExistingDate_shouldReturnError() throws Exception {
            assertThat(weightRepository.findAll()).hasSize(0);
            weightRepository.save(new Weight(LocalDate.of(2023, 8, 3), 13.2));
            mockMvc.perform(post(URI.create(ENDPOINT))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"date\":\"2023-08-03\",\"value\":13.2}"))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().string("{\"message\":\"Weight already exists for date: 2023-08-03\"}"));

            assertThat(weightRepository.findAll()).hasSize(1);
        }
    }

    @Nested
    class Get {

        @Test
        void givenValidGetRequestWithExistingDate_shouldReturnWeight() throws Exception {
            weightRepository.save(new Weight(LocalDate.of(2023, 8, 3), 13.2));
            assertThat(weightRepository.findAll()).hasSize(1);

            mockMvc.perform(get(URI.create(ENDPOINT + "?date=2023-08-03")))
                    .andExpect(status().isOk())
                    .andExpect(content().string("{\"date\":\"2023-08-03\",\"value\":13.2}"));
        }

        @Test
        void givenValidGetRequestWithNonExistingDate_shouldReturnError() throws Exception {
            assertThat(weightRepository.findAll()).hasSize(0);

            mockMvc.perform(get(URI.create(ENDPOINT + "?date=2023-08-03")))
                    .andExpect(status().isNotFound())
                    .andExpect(content().string("{\"message\":\"Weight not found for date: 2023-08-03\"}"));
        }
    }
}
