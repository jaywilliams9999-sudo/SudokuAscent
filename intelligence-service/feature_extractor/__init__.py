from .basic_stats import get_stats
from .tactic_checker import check_tactics

def extract_all_features(board_array):
    """Aggregation function."""
    stats = get_stats(board_array)
    tactics = check_tactics(board_array)

    return {**stats, **tactics}

