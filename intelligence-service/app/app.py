from fastapi import FastAPI, HTTPException
from pydantic import BaseModel
import numpy as np
import joblib
import os
import sys

#ensure feature_extractor can be found
sys.path.append(os.path.abspath(os.path.join(os.path.dirname(__file__), '..')))
from feature_extractor import extract_all_features

app = FastAPI(title="Sudoku Ascent Intelligence Service")

#load the trained model
base_dir = os.path.dirname(os.path.abspath(__file__))
MODEL_PATH = os.path.abspath(os.path.join(base_dir, "..", "models", "model_new.pkl"))
if os.path.exists(MODEL_PATH):
    model = joblib.load(MODEL_PATH)
else:
    model = None
    print("Warning: Model not found. Run the training script first you goofball.")

#define expected input format
class SudokuBoard(BaseModel):
    grid: list[list[int]] #9x9 nested list

@app.post("/predict")
async def predict_difficulty(data: SudokuBoard):
    if model is None:
        raise HTTPException(status_code=500, detail="Model not loaded on server.")
    try:
        # 3. Process the incoming grid
        board_array = np.array(data.grid)
        
        if board_array.shape != (9, 9):
            raise ValueError("Grid must be exactly 9x9.")

        # 4. Extract Features (Same logic used in training!)
        features_dict = extract_all_features(board_array)
        
        # Convert dictionary to a flat list of values for the model
        # IMPORTANT: The order of features must match your training script!
        feature_vector = [list(features_dict.values())]

        # 5. Get the Prediction
        prediction = model.predict(feature_vector)
        difficulty_score = float(prediction[0])

        # 6. Return JSON to Java
        return {
            "difficulty_score": round(difficulty_score, 4),
            "tactics_found": features_dict,
            "status": "success"
        }

    except Exception as e:
        raise HTTPException(status_code=400, detail=str(e))

@app.get("/health")
def health_check():
    return {"status": "online", "model_loaded": model is not None}
