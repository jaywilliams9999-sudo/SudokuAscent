package com.sudoku.backend.service;

import com.sudoku.backend.dto.IntelligenceResponse;
import com.sudoku.backend.dto.SudokuGridRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class IntelligenceServiceClient {

    @Value("${intelligence.service.url}")
    private String intelligenceServiceUrl;

    private final RestTemplate restTemplate;

    public IntelligenceServiceClient() {
        this.restTemplate = new RestTemplate();
    }

    public IntelligenceResponse getPuzzleAnalysis(SudokuGridRequest gridRequest) {
        String endpoint = intelligenceServiceUrl + "/predict";
        
        try {
            ResponseEntity<IntelligenceResponse> response = restTemplate.postForEntity(
                    endpoint,
                    gridRequest,
                    IntelligenceResponse.class
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return response.getBody();
            }
            throw new RuntimeException("Intelligence service returned empty or failure status.");
            
        } catch (Exception e) {
            System.err.println("Failed to contact Intelligence Service at " + endpoint);
            e.printStackTrace();
            throw new RuntimeException("Failed to analyze puzzle. Is the Python service running?", e);
        }
    }
}
