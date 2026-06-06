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

    public int[][] solve(int[][] grid) {
        return new DLXEngine().solve(grid);
    }

    public int[][] generatePuzzle(int holes) {
        return new DLXEngine().generatePuzzle(holes, new Random());
    }

    public int[][] generatePuzzle(int holes, long seed) {
        return new DLXEngine().generatePuzzle(holes, new Random(seed));
    }

    private class DLXEngine {
        private ColumnNode root;
        private List<Node> solutionPaths;
        private int[][] solvedGrid;
        private boolean isSolved;
        private int solutionCount;

        class Node {
            Node left, right, up, down;
            ColumnNode column;
            int rowIndex;

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
         * Solves a given 9x9 Sudoku grid.
         * This method initializes the Dancing Links structures, builds the exact cover matrix,
         * and triggers the recursive search algorithm.
         */
        public int[][] solve(int[][] grid) {
            if (grid == null || grid.length != 9 || grid[0].length != 9)
                return null;

            isSolved = false;
            solvedGrid = new int[SIZE][SIZE];
            solutionPaths = new ArrayList<>();

            root = new ColumnNode(0);
            buildDancingLinksCustomGrid(grid);

            search(0, false, false, null);

            if (isSolved) {
                return solvedGrid;
            }
            return null;
        }

        /**
         * Converts the 9x9 Sudoku grid into a sparse 324-column exact cover matrix.
         * The matrix contains 4 types of constraints: Cell, Row, Column, and Box constraints.
         * Each possible number placement creates a row of linked nodes.
         */
        private void buildDancingLinksCustomGrid(int[][] grid) {
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

            for (int r = 0; r < SIZE; r++) {
                for (int c = 0; c < SIZE; c++) {
                    for (int num = 0; num < SIZE; num++) {

                        if (grid[r][c] != 0 && grid[r][c] != (num + 1)) {
                            continue;
                        }

                        int rowIndex = (r * SIZE * SIZE) + (c * SIZE) + num;

                        int cellConstraint = r * SIZE + c;
                        int rowConstraint = 81 + r * SIZE + num;
                        int colConstraint = 162 + c * SIZE + num;
                        int boxConstraint = 243 + ((r / 3) * 3 + (c / 3)) * SIZE + num;

                        int[] cols = { cellConstraint, rowConstraint, colConstraint, boxConstraint };

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

        /**
         * The core recursive backtracking mechanism of Algorithm X.
         * It selects a column with the fewest options, covers it (removes it from the matrix),
         * tries out each possible row, and proceeds deeper. 
         * If a dead end is reached, it uncovers the paths and tries the next option.
         */
        private void search(int k, boolean randomize, boolean countAll, Random rand) {
            if (isSolved && !countAll)
                return;
            if (countAll && solutionCount > 1)
                return;

            if (root.right == root) {
                solutionCount++;
                if (!countAll) {
                    isSolved = true;
                    mapSolutionPathsToGrid();
                }
                return;
            }

            ColumnNode c = chooseColumn();
            c.cover();

            List<Node> rows = new ArrayList<>();
            for (Node r = c.down; r != c; r = r.down) {
                rows.add(r);
            }

            if (randomize) {
                if (rand != null)
                    Collections.shuffle(rows, rand);
                else
                    Collections.shuffle(rows, new Random());
            }

            for (Node r : rows) {
                solutionPaths.add(r);
                for (Node j = r.right; j != r; j = j.right) {
                    j.column.cover();
                }

                search(k + 1, randomize, countAll, rand);

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

        /**
         * Selects the column node with the lowest size (fewest possible candidates).
         * This heuristic drastically reduces the number of branches the search has to explore.
         */
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

        /**
         * Once the exact cover matrix is solved, this reads the selected Node rows
         * and translates their indices back into numbers on a 9x9 Sudoku grid.
         */
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

        /**
         * Generates a completely new Sudoku puzzle.
         * It first builds a full valid board randomly, then iteratively removes numbers (holes)
         * while checking that the board still only has exactly one valid solution.
         */
        public int[][] generatePuzzle(int holes, Random rand) {
            isSolved = false;
            solvedGrid = new int[SIZE][SIZE];
            solutionPaths = new ArrayList<>();
            root = new ColumnNode(0);

            buildDancingLinksCustomGrid(new int[9][9]);
            search(0, true, false, rand);

            if (!isSolved)
                return null;

            int[][] puzzle = new int[9][9];
            for (int r = 0; r < 9; r++) {
                System.arraycopy(solvedGrid[r], 0, puzzle[r], 0, 9);
            }

            // Create a shuffled list of all 81 coordinates
            List<int[]> coords = new ArrayList<>();
            for (int r = 0; r < 9; r++) {
                for (int c = 0; c < 9; c++) {
                    coords.add(new int[] { r, c });
                }
            }
            Collections.shuffle(coords, rand != null ? rand : new Random());

            int removed = 0;
            // Iterate over all coordinates exactly once
            for (int[] coord : coords) {
                if (removed >= holes) {
                    break;
                }

                int r = coord[0];
                int c = coord[1];

                int backup = puzzle[r][c];
                puzzle[r][c] = 0;

                // Test validity on a completely fresh DLXEngine branch
                if (!new DLXEngine().hasUniqueSolution(puzzle)) {
                    puzzle[r][c] = backup;
                } else {
                    removed++;
                }
            }

            return puzzle;
        }

        /**
         * Helper method for generation. It runs the solver on a board, but instead of stopping
         * at the first solution, it searches all branches. It fails and returns false if a second
         * valid solution is found, ensuring puzzle integrity.
         */
        public boolean hasUniqueSolution(int[][] grid) {
            solutionCount = 0;
            solutionPaths = new ArrayList<>();
            root = new ColumnNode(0);
            buildDancingLinksCustomGrid(grid);
            search(0, false, true, null);
            return solutionCount == 1;
        }
    }
}
