package com.sudoku.backend.service;

import com.sudoku.backend.dto.IntelligenceResponse;
import com.sudoku.backend.dto.SudokuGridRequest;
import com.sudoku.backend.dto.SurvivalResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Service
public class SurvivalModeService {

    private final SudokuSolverService solverService;
    private final IntelligenceServiceClient intelligenceClient;

    @Autowired
    public SurvivalModeService(SudokuSolverService solverService, IntelligenceServiceClient intelligenceClient) {
        this.solverService = solverService;
        this.intelligenceClient = intelligenceClient;
    }

    public SurvivalResponse getNextSurvivalBoard(double previousDifficulty) {
        int candidateCount = 5;
        double targetDifficulty = previousDifficulty + 0.05; // Standard step scaling
        
        SurvivalResponse bestCandidate = null;
        double smallestDiff = Double.MAX_VALUE;
        
        Random rand = new Random();
        
        for (int i = 0; i < candidateCount; i++) {
            // Randomly dig holes between 40 and 60 (yielding 21-41 given clues)
            int holes = 40 + rand.nextInt(21);
            int[][] generated = solverService.generatePuzzle(holes);
            
            if (generated == null) continue; // Safety check
            
            // Format to generic DTO list map
            List<List<Integer>> candidateList = new ArrayList<>();
            for (int r = 0; r < 9; r++) {
                List<Integer> row = new ArrayList<>();
                for (int c = 0; c < 9; c++) {
                    row.add(generated[r][c]);
                }
                candidateList.add(row);
            }
            
            // Pipeline to Python ML Microservice
            try {
                SudokuGridRequest request = new SudokuGridRequest();
                request.setGrid(candidateList);
                IntelligenceResponse analysis = intelligenceClient.getPuzzleAnalysis(request);
                
                double curDiff = analysis.getDifficulty_score();
                double currentGap = Math.abs(curDiff - targetDifficulty);
                
                if (bestCandidate == null || currentGap < smallestDiff) {
                    smallestDiff = currentGap;
                    bestCandidate = new SurvivalResponse(candidateList, curDiff);
                }
            } catch (Exception e) {
                // If intelligence service fails or model is down, skip evaluating this
                System.out.println("Warning: Intelligence Service failed for candidate: " + e.getMessage());
            }
        }
        
        return bestCandidate;
    }
}
