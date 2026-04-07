package com.sudoku.backend.dto;

import lombok.Data;
import java.util.Map;

@Data
public class IntelligenceResponse {
    private Double difficulty_score;
    private Map<String, Integer> tactics_found;
    private String status;
}
