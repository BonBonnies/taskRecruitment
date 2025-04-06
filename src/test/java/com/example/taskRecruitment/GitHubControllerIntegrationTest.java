package com.example.taskRecruitment;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.HttpClientErrorException;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.hamcrest.Matchers.is;

@WebMvcTest(GitHubController.class)
@ContextConfiguration(classes = {GitHubControllerIntegrationTest.TestConfig.class, ApplicationConfig.class})
public class GitHubControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private GitHubService githubService;

    @Configuration
    @Import(GitHubController.class)
    static class TestConfig {
        @Bean
        public GitHubService githubService() {
            return Mockito.mock(GitHubService.class);
        }
    }

    @BeforeEach
    public void setUp() {
        Mockito.reset(githubService);
    }

    @Test
    public void shouldReturnRepositories() throws Exception {
        List<BranchResponse> branches = Arrays.asList(
                new BranchResponse("main", "abc123"),
                new BranchResponse("develop", "def456")
        );

        List<RepositoryResponse> repositories = Collections.singletonList(
                new RepositoryResponse("repo1", "login", branches)
        );

        when(githubService.getUserRepositories("login")).thenReturn(repositories);

        // When & Then
        mockMvc.perform(get("/api/repositories/login")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name", is("repo1")))
                .andExpect(jsonPath("$[0].ownerLogin", is("login")))
                .andExpect(jsonPath("$[0].branches", hasSize(2)))
                .andExpect(jsonPath("$[0].branches[*].name", containsInAnyOrder("main", "develop")))
                .andExpect(jsonPath("$[0].branches[*].lastCommitSha", containsInAnyOrder("abc123", "def456")));
    }

    @Test
    public void shouldReturn404ForNonExistingUser() throws Exception {
        // Given
        when(githubService.getUserRepositories(eq("non-existing-user")))
                .thenThrow(HttpClientErrorException.NotFound.create(
                        HttpStatus.NOT_FOUND, "Not Found", null, null, null));

        // When & Then
        mockMvc.perform(get("/api/repositories/non-existing-user")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.message", is("User not found")));
    }

    @Test
    public void shouldReturnEmptyListWhenUserHasNoRepos() throws Exception {
        // Given
        when(githubService.getUserRepositories("user-with-no-repos"))
                .thenReturn(Collections.emptyList());

        // When & Then
        mockMvc.perform(get("/api/repositories/user-with-no-repos")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(0)));
    }
}