package com.example.taskRecruitment;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class BranchResponse {
    private String name;
    private String lastCommitSha;
}