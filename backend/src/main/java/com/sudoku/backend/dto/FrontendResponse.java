package com.sudoku.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;
import java.util.Map;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FrontendResponse {
    private double difficultyRating;
    private Map<String, Integer> requiredLogicTactics;
    private boolean successfullyAnalyzed;
    private String message;
    private List<List<Integer>> solvedGrid;
}
