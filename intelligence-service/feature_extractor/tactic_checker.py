from sudoku import Sudoku
import numpy as np

def check_tactics(board_array):
    """Analyzes te board to identify specific required strategies. Returns a dictionary of boolean style flags (0 or 1)."""
    
    #prepare board for library
    board_list = board_array.tolist()
    for r in range(9):
        for c in range(9):
            if board_list[r][c] == 0:
                board_list[r][c] = None

    puzzle = Sudoku(3, 3, board=board_list)

    #get the library's difficulty report
    #note: different libraries provide different levels of detail
    #the library we are using is a good baseline 
    #but for specific tactics we look at the complexity of the solution path
    # py-sudoku's .difficulty() is a generation method. To evaluate difficulty under this library, 
    # we calculate the proportion of empty cells, which correlates with its difficulty index (0 to 1).
    empty_cells = sum(row.count(None) for row in board_list)
    lib_diff = empty_cells / 81.0

    #create a tactic profile
    #map the library's float difficulty to specific thresholds that represent when certain tactics usually appear
    features = {
        "is_solvable": 1 if puzzle.solve() else 0,
        "requires_basic_logic": 1 if lib_diff > 0.1 else 0,
        "requires_advanced_logic": 1 if lib_diff > 0.5 else 0,
        "requires_extreme_logic": 1 if lib_diff > 0.8 else 0,
        "raw_lib_rating": lib_diff if lib_diff else 0
    }

    return features
