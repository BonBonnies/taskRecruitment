package com.example.taskRecruitment;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

public class GitHubServiceTest {

    private GitHubService githubService;
    private RestTemplate restTemplate;

    @BeforeEach
    public void setUp() {
        restTemplate = Mockito.mock(RestTemplate.class);
        githubService = new GitHubService(restTemplate);
    }

    @Test
    public void shouldReturnRepositoriesWithBranches() {
        ResponseEntity<GitHubService.GitHubRepo[]> repoResponse =
                new ResponseEntity<>(new GitHubService.GitHubRepo[]{}, HttpStatus.OK);

        ResponseEntity<GitHubService.GitHubBranch[]> branchResponse =
                new ResponseEntity<>(new GitHubService.GitHubBranch[]{}, HttpStatus.OK);

        when(restTemplate.getForEntity(contains("/users/testuser/repos"), eq(GitHubService.GitHubRepo[].class)))
                .thenReturn(repoResponse);

        when(restTemplate.getForEntity(contains("/branches"), eq(GitHubService.GitHubBranch[].class)))
                .thenReturn(branchResponse);

        List<RepositoryResponse> result = githubService.getUserRepositories("testuser");

        assertNotNull(result);
        verify(restTemplate, times(1)).getForEntity(contains("/users/testuser/repos"),
                eq(GitHubService.GitHubRepo[].class));
    }

    @Test
    public void shouldThrow404WhenUserNotFound() {
        when(restTemplate.getForEntity(anyString(), eq(GitHubService.GitHubRepo[].class)))
                .thenThrow(HttpClientErrorException.create(HttpStatus.NOT_FOUND, "Not Found", null, null, null));

        assertThrows(HttpClientErrorException.class, () -> githubService.getUserRepositories("non-existing-user"));
    }
}