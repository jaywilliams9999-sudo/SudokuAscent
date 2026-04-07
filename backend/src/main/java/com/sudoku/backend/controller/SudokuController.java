package com.sudoku.backend.controller;

import com.sudoku.backend.dto.FrontendResponse;
import com.sudoku.backend.dto.IntelligenceResponse;
import com.sudoku.backend.dto.SudokuGridRequest;
import com.sudoku.backend.dto.SurvivalRequest;
import com.sudoku.backend.dto.SurvivalResponse;
import com.sudoku.backend.service.IntelligenceServiceClient;
import com.sudoku.backend.service.SudokuSolverService;
import com.sudoku.backend.service.SurvivalModeService;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/sudoku")
@CrossOrigin(origins = "*") // Allows React frontend to hit this controller
public class SudokuController {

    private final IntelligenceServiceClient intelligenceClient;
    private final SudokuSolverService solverService;
    private final SurvivalModeService survivalModeService;

    @Autowired
    public SudokuController(IntelligenceServiceClient intelligenceClient, SudokuSolverService solverService, SurvivalModeService survivalModeService) {
        this.intelligenceClient = intelligenceClient;
        this.solverService = solverService;
        this.survivalModeService = survivalModeService;
    }

    @PostMapping("/analyze")
    public ResponseEntity<FrontendResponse> analyzePuzzle(@RequestBody SudokuGridRequest request) {
        
        // 1. Basic Validation
        if (request.getGrid() == null || request.getGrid().size() != 9) {
            return ResponseEntity.badRequest().body(
                    FrontendResponse.builder()
                            .successfullyAnalyzed(false)
                            .message("Invalid grid format. Must be 9x9.")
                            .build()
            );
        }

        try {
            // 2. Call the Python AI Service
            IntelligenceResponse mlResponse = intelligenceClient.getPuzzleAnalysis(request);

            // 3. Map to Frontend format
            FrontendResponse finalResponse = FrontendResponse.builder()
                    .difficultyRating(mlResponse.getDifficulty_score())
                    .requiredLogicTactics(mlResponse.getTactics_found())
                    .successfullyAnalyzed(true)
                    .message("Successfully analyzed!")
                    .build();

            // 4. Return to React
            return ResponseEntity.ok(finalResponse);

        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    FrontendResponse.builder()
                            .successfullyAnalyzed(false)
                            .message(ex.getMessage())
                            .build()
            );
        }
    }

    @PostMapping("/solve")
    public ResponseEntity<FrontendResponse> solvePuzzle(@RequestBody SudokuGridRequest request) {
        if (request.getGrid() == null || request.getGrid().size() != 9) {
            return ResponseEntity.badRequest().body(
                    FrontendResponse.builder()
                            .successfullyAnalyzed(false)
                            .message("Invalid grid format. Must be 9x9.")
                            .build()
            );
        }

        try {
            int[][] intGrid = new int[9][9];
            for (int r = 0; r < 9; r++) {
                List<Integer> row = request.getGrid().get(r);
                for (int c = 0; c < 9; c++) {
                    intGrid[r][c] = row.get(c);
                }
            }

            int[][] solved = solverService.solve(intGrid);

            if (solved != null) {
                List<List<Integer>> solvedList = new ArrayList<>();
                for (int r = 0; r < 9; r++) {
                    List<Integer> row = new ArrayList<>();
                    for (int c = 0; c < 9; c++) {
                        row.add(solved[r][c]);
                    }
                    solvedList.add(row);
                }
                
                return ResponseEntity.ok(FrontendResponse.builder()
                        .successfullyAnalyzed(true)
                        .message("Successfully solved puzzle!")
                        .solvedGrid(solvedList)
                        .build());
            } else {
                return ResponseEntity.ok(FrontendResponse.builder()
                        .successfullyAnalyzed(false)
                        .message("Puzzle is unsolvable.")
                        .build());
            }

        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    FrontendResponse.builder()
                            .successfullyAnalyzed(false)
                            .message(ex.getMessage())
                            .build()
            );
        }
    }

    @PostMapping("/survival/next")
    public ResponseEntity<SurvivalResponse> getNextSurvivalBoard(@RequestBody SurvivalRequest request) {
        try {
            SurvivalResponse response = survivalModeService.getNextSurvivalBoard(request.getPreviousDifficulty());
            if (response != null) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            }
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
