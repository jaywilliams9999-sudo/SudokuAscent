# SudokuAscent

SudokuAscent is a full-stack web application that uses machine learning to help users solve and improve at Sudoku puzzles. The platform analyzes puzzle difficulty, detects solving tactics, and provides intelligent hints powered by a trained AI model.

## Features

- **Intelligent Puzzle Analysis** - An ML-powered service that evaluates puzzle difficulty and recommends solving strategies based on a dataset of 3 million Sudoku puzzles.
- **Tactic Detection** - Automatically identifies applicable Sudoku techniques such as naked singles, hidden pairs, and other common strategies.
- **Feature Extraction** - Extracts statistical features from puzzles to feed into the prediction model.
- **Modern Web Interface** - A React-based frontend for interacting with puzzles in the browser.
- **Backend API** - A Java/Spring backend that handles game logic, user sessions, and communication between the frontend and the intelligence service.

## Project Structure

- **backend/** - Java/Spring REST API (Maven)
- **frontend/** - React single-page application
- **intelligence-service/** - Python ML service for puzzle analysis and inference
  - **app/** - Application entry point and inference logic
  - **feature_extractor/** - Statistical and tactic-based feature extraction
  - **models/** - Trained model artifacts
  - **training/** - Data preparation and model training scripts
- **docs/** - Architecture diagrams and project documentation

## Prerequisites

- Java 17 or later
- Node.js 18 or later
- Python 3.10 or later
- Git LFS (required for pulling the training dataset)

## Getting Started

```bash
# Install Git LFS (required before cloning)
git lfs install

# Clone the repository
git clone https://github.com/jaywilliams9999-sudo/SudokuAscent.git
cd SudokuAscent
```

Refer to [TEAM_GUIDE.md](TEAM_GUIDE.md) for detailed setup instructions, branching workflow, and team conventions.

## Git LFS

This repository uses Git Large File Storage (LFS) to manage large training datasets. All `*.csv` files are tracked by LFS. Make sure to run `git lfs install` before cloning to ensure the datasets are downloaded correctly.

## Team

See [TEAM_GUIDE.md](TEAM_GUIDE.md) for onboarding instructions and branch workflow.
