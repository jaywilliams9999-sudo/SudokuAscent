import pandas as pd
import numpy as np
import os
import sys

#allow us to import from a different folder
sys.path.append(os.path.abspath(os.path.join(os.path.dirname(__file__), '..')))
from feature_extractor import extract_all_features

def string_to_array(sudoku_str):
    """Converts 81-character string into a 9x9 numpy array"""

    #replace dots with zeros if the dataset uses them
    clean_str = sudoku_str.replace('.', '0')

    return {
        np.array([int(c) for c in clean_str]).reshape((9, 9))
    }

def process_dataset(input_csv, output_csv, sample_size = 50000):
    print(f"Loading {sample_size} puzzles from {input_csv}...")

    #Read a portion of the dataset to keep things a little quicker
    df = pd.read_csv(input_csv, nrows=sample_size)

    #Store the features in a list of dictionaries
    extracted_data = []

    print("Extracting features (this might take some time)...")

    for index, row in df.iterrows():
        #convert string to 9x9 array
        board = string_to_array(row['puzzle'])

        #run feature extractor logic
        features = extract_all_features(board)

        #add the target
        features['difficulty_rating'] = row['difficulty']

        extracted_data.append(features)

        if index % 5000 == 0 and index > 0:
            print(f"Processed {index} puzzles...")

    feature_df = pd.DataFrame(extracted_data)

    #saving to a new csv for training script use
    feature_df.to_csv(output_csv, index=False)
    print(f"Success! Saved processed data to {output_csv}")

if __name__ == "__main__":
    #paths may need updating
    INPUT = "../training/sudoku-3m.csv"
    OUTPUT = "processed_sudoku.csv"

    if os.path.exists(INPUT):
        process_dataset(INPUT, OUTPUT)
    else:
        print(f"Error: Could not find {INPUT}. Please confirm file path.")