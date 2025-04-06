package com.example.taskRecruitment;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class GitHubService {

    private final RestTemplate restTemplate;

    @Autowired
    public GitHubService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public List<RepositoryResponse> getUserRepositories(String username) {
        String url = "https://api.github.com/users/" + username + "/repos";
        ResponseEntity<GitHubRepo[]> response = restTemplate.getForEntity(url, GitHubRepo[].class);
        GitHubRepo[] repos = response.getBody();
        if (repos == null) {
            return new ArrayList<>();
        }

        return Arrays.stream(repos)
                .filter(repo -> !repo.isFork())
                .map(repo -> new RepositoryResponse(
                        repo.getName(),
                        repo.getOwner().getLogin(),
                        getBranches(username, repo.getName())
                ))
                .collect(Collectors.toList());
    }

    private List<BranchResponse> getBranches(String username, String repositoryName) {
        String url = "https://api.github.com/repos/" + username + "/" + repositoryName + "/branches";
        ResponseEntity<GitHubBranch[]> response = restTemplate.getForEntity(url, GitHubBranch[].class);
        GitHubBranch[] branches = response.getBody();
        if (branches == null) {
            return new ArrayList<>();
        }

        return Arrays.stream(branches)
                .map(branch -> new BranchResponse(branch.getName(), branch.getCommit().getSha()))
                .collect(Collectors.toList());
    }

    @Getter
    static class GitHubRepo {
        private String name;
        private boolean fork;
        private Owner owner;
    }

    @Getter
    private static class Owner {
        private String login;
    }

    @Getter
    static class GitHubBranch {
        private String name;
        private Commit commit;
    }

    @Getter
    private static class Commit {
        private String sha;
    }
}