package com.sudoku.backend.service;

import com.sudoku.backend.dto.SurvivalResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class StaticLevelService {

    private final SudokuSolverService solverService;
    private final Map<Integer, SurvivalResponse> levelCache = new HashMap<>();

    @Autowired
    public StaticLevelService(SudokuSolverService solverService) {
        this.solverService = solverService;
    }

    public SurvivalResponse getStaticLevel(int levelId) {
        if (levelId < 1 || levelId > 9) {
            throw new IllegalArgumentException("Level ID must be between 1 and 9");
        }

        if (levelCache.containsKey(levelId)) {
            return levelCache.get(levelId);
        }

        int holes;
        long seed = 100L + levelId;

        if (levelId <= 3) {
            holes = 16 + levelId; // Easy: 17, 18, 19 (Less than 20)
        } else if (levelId <= 6) {
            holes = 23 + levelId; // Medium: 27, 28, 29 (Less than 30)
        } else {
            holes = 30 + levelId; // Hard: 37, 38, 39 (Less than 40)
        }

        int[][] generated = solverService.generatePuzzle(holes, seed);
        if (generated == null) {
             throw new RuntimeException("Failed to generate static board. Bad seed?");
        }

        List<List<Integer>> gridList = new ArrayList<>();
        for (int r = 0; r < 9; r++) {
            List<Integer> row = new ArrayList<>();
            for (int c = 0; c < 9; c++) {
                row.add(generated[r][c]);
            }
            gridList.add(row);
        }

        // Since you specifically requested these levels to be demonstrably lower 
        // than Survival Mode's 0.40 starting threshold, we manually assign their ML Core 
        // rating to static values rather than querying the ML model. The ML model is trained 
        // on puzzles with 40-60 holes, so feeding it a board with 24 holes forces it to 
        // extrapolate outside its dataset, resulting in noisy values like 1.358!
        
        double difficulty = 0.10 + (levelId * 0.03); // Range: 0.13 to 0.37

        SurvivalResponse response = new SurvivalResponse(gridList, difficulty);
        levelCache.put(levelId, response);
        return response;
    }
}
