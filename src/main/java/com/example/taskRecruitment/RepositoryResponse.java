package com.example.taskRecruitment;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class RepositoryResponse {
    private String name;
    private String ownerLogin;
    private List<BranchResponse> branches;
}