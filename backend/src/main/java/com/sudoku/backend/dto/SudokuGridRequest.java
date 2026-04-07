package com.sudoku.backend.dto;

import lombok.Data;
import java.util.List;

@Data
public class SudokuGridRequest {
    private List<List<Integer>> grid;
}
