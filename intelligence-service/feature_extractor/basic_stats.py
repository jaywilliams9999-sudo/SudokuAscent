import numpy as np

board_total = 81

def get_stats(board_array):
    """Returns a dictionary of simple numerical features."""
    flat = board_array.flatten()
    
    return {
        "clue_count":
            int(np.count_nonzero(flat)),
        "empty_cells":
            int(board_total - np.count_nonzero(flat)),
        "symmetry":
            float(_calculate_symmetry(board_array))
    }

def _calculate_symmetry(board):
    """Helper function to calculate 180 degree rotational symmetry. Internal function."""

    #rotate 180 degrees
    rotated = np.rot90(board, 2)

    #check where cells are filled in both rotated and pre rotated
    #boolean mask used
    matches = (board != 0) == (rotated != 0)
    return np.sum(matches) / board_total




