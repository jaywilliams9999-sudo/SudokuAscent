package com.sudoku.backend.service;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

@Service
public class SudokuSolverService {

    private static final int SIZE = 9;
    private static final int MAX_K = 324;
    private ColumnNode root;
    private List<Node> solutionPaths;
    private int[][] solvedGrid;
    private boolean isSolved;
    private int solutionCount;

    class Node {
        Node left, right, up, down;
        ColumnNode column;
        int rowIndex; // Identifies the Sudoku candidate

        public Node() {
            left = right = up = down = this;
        }

        public Node(ColumnNode c, int rowIndex) {
            this();
            this.column = c;
            this.rowIndex = rowIndex;
        }
    }

    class ColumnNode extends Node {
        int size;
        int name;

        public ColumnNode(int name) {
            super();
            this.size = 0;
            this.name = name;
            this.column = this;
        }

        void cover() {
            right.left = left;
            left.right = right;
            for (Node i = down; i != this; i = i.down) {
                for (Node j = i.right; j != i; j = j.right) {
                    j.down.up = j.up;
                    j.up.down = j.down;
                    j.column.size--;
                }
            }
        }

        void uncover() {
            for (Node i = up; i != this; i = i.up) {
                for (Node j = i.left; j != i; j = j.left) {
                    j.column.size++;
                    j.down.up = j;
                    j.up.down = j;
                }
            }
            right.left = this;
            left.right = this;
        }
    }

    /**
     * Attempts to solve the provided 9x9 Sudoku grid.
     * 
     * @param grid The initial 9x9 grid where 0 denotes an empty cell.
     * @return A solved 9x9 grid, or null if unsolveable.
     */
    public int[][] solve(int[][] grid) {
        if (grid == null || grid.length != 9 || grid[0].length != 9) return null;
        
        isSolved = false;
        solvedGrid = new int[SIZE][SIZE];
        solutionPaths = new ArrayList<>();
        
        // Build Exact Cover Matrix
        int[][] exactCoverMatrix = buildExactCoverMatrix();
        
        // Build Dancing Links Toroidal structure
        buildDancingLinks(exactCoverMatrix);
        
        // Pre-cover columns based on the given grid hints
        for (int r = 0; r < SIZE; r++) {
            for (int c = 0; c < SIZE; c++) {
                if (grid[r][c] != 0) {
                    int num = grid[r][c] - 1;
                    int rowIndex = (r * SIZE * SIZE) + (c * SIZE) + num;
                    
                    // We need to trace down columns and find the node for this rowIndex
                    // However, we didn't store row mappings easily. 
                    // Let's implement a simpler pre-cover strategy: 
                    // During DLX conversion, we can just cover right away. 
                    // Actually, a better way is to solve the puzzle purely by adding only the 
                    // candidates that are valid. If a cell has a value, only 1 candidate row is added.
                }
            }
        }
        
        // Let's rebuild grid mapping natively to allow skipping invalid rows
        root = new ColumnNode(0);
        buildDancingLinksCustomGrid(grid);

        search(0, false, false);
        
        if (isSolved) {
            return solvedGrid;
        }
        return null; // Unsolvable
    }

    private void buildDancingLinksCustomGrid(int[][] grid) {
        // Create 324 ColumnNodes
        ColumnNode cur = root;
        List<ColumnNode> columns = new ArrayList<>();
        for (int i = 0; i < MAX_K; i++) {
            ColumnNode col = new ColumnNode(i);
            columns.add(col);
            cur.right = col;
            col.left = cur;
            cur = col;
        }
        cur.right = root;
        root.left = cur;

        // Iterate through all 729 possibilities
        for (int r = 0; r < SIZE; r++) {
            for (int c = 0; c < SIZE; c++) {
                for (int num = 0; num < SIZE; num++) {
                    
                    // If the cell is populated, and this 'num' isn't what's populated, skip this candidate
                    if (grid[r][c] != 0 && grid[r][c] != (num + 1)) {
                        continue;
                    }
                    
                    int rowIndex = (r * SIZE * SIZE) + (c * SIZE) + num;
                    
                    int cellConstraint = r * SIZE + c;
                    int rowConstraint = 81 + r * SIZE + num;
                    int colConstraint = 162 + c * SIZE + num;
                    int boxConstraint = 243 + ((r / 3) * 3 + (c / 3)) * SIZE + num;
                    
                    int[] cols = {cellConstraint, rowConstraint, colConstraint, boxConstraint};
                    
                    Node prev = null;
                    Node first = null;
                    
                    for (int colIdx : cols) {
                        ColumnNode colNode = columns.get(colIdx);
                        Node newNode = new Node(colNode, rowIndex);
                        if (prev == null) {
                            prev = newNode;
                            first = newNode;
                        } else {
                            newNode.left = prev;
                            newNode.right = prev.right;
                            prev.right.left = newNode;
                            prev.right = newNode;
                            prev = newNode;
                        }
                        
                        newNode.down = colNode;
                        newNode.up = colNode.up;
                        colNode.up.down = newNode;
                        colNode.up = newNode;
                        
                        colNode.size++;
                    }
                }
            }
        }
    }

    private int[][] buildExactCoverMatrix() {
       return null; // We use buildDancingLinksCustomGrid directly!
    }

    private void buildDancingLinks(int[][] matrix) {
        // We use buildDancingLinksCustomGrid directly!
    }

    private void search(int k, boolean randomize, boolean countAll) {
        if (isSolved && !countAll) return;
        if (countAll && solutionCount > 1) return;
        
        if (root.right == root) {
            solutionCount++;
            if (!countAll) {
                isSolved = true;
                mapSolutionPathsToGrid();
            }
            return;
        }
        
        // Choose column deterministically (shortest column to minimize branching)
        ColumnNode c = chooseColumn();
        c.cover();
        
        List<Node> rows = new ArrayList<>();
        for (Node r = c.down; r != c; r = r.down) {
            rows.add(r);
        }
        
        if (randomize) {
            Collections.shuffle(rows, new Random());
        }
        
        for (Node r : rows) {
            solutionPaths.add(r);
            for (Node j = r.right; j != r; j = j.right) {
                j.column.cover();
            }
            
            search(k + 1, randomize, countAll);
            
            solutionPaths.remove(solutionPaths.size() - 1);
            
            for (Node j = r.left; j != r; j = j.left) {
                j.column.uncover();
            }
            
            if (isSolved && !countAll) {
                c.uncover();
                return;
            }
            if (countAll && solutionCount > 1) {
                c.uncover();
                return;
            }
        }
        
        c.uncover();
    }

    private ColumnNode chooseColumn() {
        int minSize = Integer.MAX_VALUE;
        ColumnNode best = null;
        for (ColumnNode c = (ColumnNode) root.right; c != root; c = (ColumnNode) c.right) {
            if (c.size < minSize) {
                minSize = c.size;
                best = c;
            }
        }
        return best;
    }

    private void mapSolutionPathsToGrid() {
        for (Node node : solutionPaths) {
            int rowIndex = node.rowIndex;
            int num = (rowIndex % 9) + 1;
            rowIndex /= 9;
            int col = rowIndex % 9;
            rowIndex /= 9;
            int row = rowIndex % 9;
            solvedGrid[row][col] = num;
        }
    }

    public int[][] generatePuzzle(int holes) {
        isSolved = false;
        solvedGrid = new int[SIZE][SIZE];
        solutionPaths = new ArrayList<>();
        root = new ColumnNode(0);
        
        buildDancingLinksCustomGrid(new int[9][9]);
        search(0, true, false);
        
        if (!isSolved) return null;
        
        int[][] puzzle = new int[9][9];
        for (int r = 0; r < 9; r++) {
            System.arraycopy(solvedGrid[r], 0, puzzle[r], 0, 9);
        }
        
        Random rand = new Random();
        int removed = 0;
        int attempts = 0;
        while (removed < holes && attempts < 200) {
            attempts++;
            int r = rand.nextInt(9);
            int c = rand.nextInt(9);
            if (puzzle[r][c] != 0) {
                int backup = puzzle[r][c];
                puzzle[r][c] = 0;
                
                if (!hasUniqueSolution(puzzle)) {
                    puzzle[r][c] = backup;
                } else {
                    removed++;
                }
            }
        }
        
        return puzzle;
    }
    
    public boolean hasUniqueSolution(int[][] grid) {
        solutionCount = 0;
        solutionPaths = new ArrayList<>();
        root = new ColumnNode(0);
        buildDancingLinksCustomGrid(grid);
        search(0, false, true);
        return solutionCount == 1;
    }
}
