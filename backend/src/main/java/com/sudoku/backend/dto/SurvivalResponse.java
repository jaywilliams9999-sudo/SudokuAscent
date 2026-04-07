package com.sudoku.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SurvivalResponse {
    private List<List<Integer>> grid;
    private double difficultyRating;
}
