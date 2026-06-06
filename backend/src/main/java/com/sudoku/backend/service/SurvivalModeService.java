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

    public SurvivalResponse getNextSurvivalBoard(double previousDifficulty, int level) {
        if (level == 1) {
            // Bypass ML model completely for the first tier to guarantee perfectly unified starting point
            int[][] startingBoard = solverService.generatePuzzle(40);
            List<List<Integer>> candidateList = new ArrayList<>();
            for (int r = 0; r < 9; r++) {
                List<Integer> row = new ArrayList<>();
                for (int c = 0; c < 9; c++) {
                    row.add(startingBoard[r][c]);
                }
                candidateList.add(row);
            }
            return new SurvivalResponse(candidateList, 0.400);
        }

        int candidateCount = 100; // Create vastly more candidates for an exact step match
        
        SurvivalResponse bestCandidate = null;
        double bestCandidateDiff = Double.MAX_VALUE;
        
        Random rand = new Random();
        
        for (int i = 0; i < candidateCount; i++) {
            // Option 1 Implementation: As the ML difficulty scales, dynamically push the floor and ceiling 
            // of the hole generator toward the absolute mathematical maximum limit of Sudoku (64 holes).
            // Pushing this bound forces the tree to require extreme nested logic, driving organic ML score growth!
            int baseHoles = 40 + (int)(previousDifficulty * 18);
            if (baseHoles > 54) baseHoles = 54;
            int holes = Math.min(64, baseHoles + rand.nextInt(11));
            
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
                
                // We want the new difficulty to be legitimately higher than previousDifficulty (by at least 0.005)
                // AND it should not increase by more than 0.03 at a time.
                // If it squarely falls into this golden bracket, we instantly return it!
                if (curDiff > previousDifficulty + 0.005 && curDiff <= previousDifficulty + 0.03) {
                    return new SurvivalResponse(candidateList, curDiff);
                }
                
                // If we don't find a golden candidate right away, we keep track of the 
                // closest valid fallback. We prioritize grabbing the absolute mathematically hardest 
                // board generated rather than the easiest to ensure organic difficulty ceiling checks.
                if (curDiff >= previousDifficulty) {
                    if (bestCandidate == null || curDiff > bestCandidateDiff) {
                        bestCandidate = new SurvivalResponse(candidateList, curDiff);
                        bestCandidateDiff = curDiff;
                    }
                }
                
            } catch (Exception e) {
                // If intelligence service fails or model is down, skip evaluating this
                System.out.println("Warning: Intelligence Service failed for candidate: " + e.getMessage());
            }
        }
        
        // If all 100 attempts missed the pristine tight window, return the absolute best 
        // fallback we found that ensures the rating did not go down.
        if (bestCandidate != null) {
            double trueDiff = bestCandidate.getDifficultyRating();
            
            // If the ML model plateaued and refused to grade the board materially higher 
            // than the previous level (e.g. hitting the 0.535 logic floor)
            if (trueDiff <= previousDifficulty + 0.005) {
                return new SurvivalResponse(bestCandidate.getGrid(), previousDifficulty + 0.025);
            }
            
            // If the rating jumped wildly (e.g. 0.40 -> 0.535)
            if (trueDiff > previousDifficulty + 0.03) {
                // Apply Mathematical Smoothing Clamp to hide extreme ML spikes.
                return new SurvivalResponse(bestCandidate.getGrid(), previousDifficulty + 0.025);
            }
            
            return bestCandidate;
        }        
        // Final absolute safety net: if by some astronomical chance ALL 100 candidates went down 
        // in difficulty, just generate one more blind board to prevent a crash, mathematically capping 
        // its artificially assigned frontend reading.
        int[][] safetyGen = solverService.generatePuzzle(60);
        List<List<Integer>> safetyList = new ArrayList<>();
        for (int r = 0; r < 9; r++) {
            List<Integer> row = new ArrayList<>();
            for (int c = 0; c < 9; c++) {
                row.add(safetyGen[r][c]);
            }
            safetyList.add(row);
        }
        return new SurvivalResponse(safetyList, previousDifficulty + 0.025);
    }
}
