package com.example.taskRecruitment;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;

import java.util.List;

@RestController
@RequestMapping("/api")
public class GitHubController {

    private final GitHubService gitHubService;

    @Autowired
    public GitHubController(GitHubService gitHubService) {
        this.gitHubService = gitHubService;
    }

    @GetMapping("/repositories/{username}")
    public ResponseEntity<List<RepositoryResponse>> getUserRepositories(@PathVariable String username) {
        List<RepositoryResponse> repositories = gitHubService.getUserRepositories(username);
        return ResponseEntity.ok(repositories);
    }

    @ExceptionHandler(HttpClientErrorException.class)
    public ResponseEntity<ErrorResponse> handleHttpClientError(HttpClientErrorException ex) {
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setStatus(ex.getStatusCode().value());

        if (ex.getStatusCode() == HttpStatus.NOT_FOUND) {
            errorResponse.setMessage("User not found");
        } else {
            errorResponse.setMessage(ex.getMessage());
        }
        return new ResponseEntity<>(errorResponse, ex.getStatusCode());
    }
}