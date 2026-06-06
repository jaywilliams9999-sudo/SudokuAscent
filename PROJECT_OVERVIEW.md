# Sudoku Ascent: Project Overview & Presentation Guide

## 1. Executive Summary

**Sudoku Ascent** is a full-stack, modular web application that modernizes traditional Sudoku by integrating a responsive "glassmorphism" user interface with a machine learning-driven intelligence layer. Its flagship feature is **Survival Mode**, where players are fed an infinite stream of progressively harder puzzles. 

This project solves two primary challenges in modern puzzle applications:
1. **Dynamic Difficulty Scaling:** Instead of static "Easy/Medium/Hard" tiers, an AI model automatically predicts and scales puzzle difficulty via a strictly controlled decimal curve.
2. **Generation Bottleneck:** To instantly generate unique, single-solution puzzles in real-time without freezing the server, the backend employs Donald Knuth's **Algorithm X** via the **Dancing Links (DLX)** technique, reducing solver/generator processing times to sub-milliseconds.

---

## 2. Core Features & User Experience

* **Survival Mode:** An infinite loop of Sudoku puzzles that scales difficulty mathematically. The AI evaluates candidate boards and selects the optimal puzzle to ensure a linear, tailored difficulty increase without regression.
* **Intelligent Puzzle Analysis & Inference:** The backend communicates with a dedicated python microservice that evaluates board mechanics using a machine learning model trained on a 3-million puzzle dataset.
* **"Check Mistakes":** Provides instant, matrix-validated feedback to check for player errors.
* **Premium User Interface:** Built with React, the frontend focuses on reactive UX. It features locking win animations (e.g., 2.8s cascading green validations), asynchronous processing screens, and structural glassmorphism to look modern and engaging.

---

## 3. System Architecture

The application adopts a decoupled, microservices architecture to manage varying workloads efficiently:

### The Frontend (User Interface)
* **Stack:** React, Vite, modern CSS styling.
* **Role:** Delivers a premium, real-time visual interface. Handles grid state, asynchronous API polling, board animations, and player interaction.

### The Backend (Core Pipeline)
* **Stack:** Java, Spring Boot, Maven.
* **Role:** Operates as the central hub. It enforces the strict mathematical rules of the game, controls the Survival Mode progression logic (generating batches of puzzle candidates), and handles user sessions. 
* **Key Engine:** Replaces naive array backtracking with the hyper-efficient **Dancing Links** algorithm to rapidly serve valid boards.

### The Intelligence Service (AI Layer)
* **Stack:** Python, FastAPI, scikit-learn.
* **Role:** Receives raw boards from the Java backend, extracts statistical and structural features, and runs them through a trained ML model (`model.pkl`) to assign a precise difficulty score.

---

## 4. Technical Deep Dive: The Algorithm (For Your Presentation)

When presenting the technical merits of this project, focus on how the team solved the **Generation Bottleneck**. 

### The Problem
Creating a Sudoku board with *exactly one* unique solution usually freezes a server. Standard brute-force nested arrays cause massive thread congestion and high latency.

### The Solution: Algorithm X & Dancing Links (DLX)
The Java backend completely re-frames standard Sudoku into a mathematical **Exact Cover** problem. 
* It transforms the 9x9 Sudoku constraints strictly into a sparse **324-column binary matrix**. 
* Instead of copying arrays as the program tests solutions, every '1' in the matrix acts as a spatial **Node** linked to its 4 neighbors (Up, Down, Left, Right). 
* When the solver places a number, it **physically un-links the pointers** to "hide" constraints temporarily (O(1) time complexity). If a guess is wrong, it simply re-links the pointers to reverse the operation.
* **Result:** No array duplication. Solve times drop to a fraction of a millisecond.

---

## 5. Development & Data Management (Git LFS)

Training the intelligence service required massive datasets (e.g., `sudoku-3m.csv` which is over 500 MB). To manage this within a standard Git repository, the team utilized **Git Large File Storage (LFS)** directly into their pipeline. All `.csv` files are stored as managed pointers to bypass standard platform storage limits, making collaborative training seamless across machines.

---

## 6. How to Demo the Application

The application requires three concurrent terminals to boot fully. 

1. **Start the Intelligence Service:**
   * Navigate to `/intelligence-service`
   * Activate the python virtual environment.
   * Run: `uvicorn app.app:app --reload`
2. **Start the Core Backend:**
   * Navigate to `/backend`
   * Run `SudokuApplication.java` via your Java IDE or Maven.
3. **Start the Web Frontend:**
   * Navigate to `/frontend`
   * Run: `npm run dev`

Access the system via the `localhost` URL provided in the React/Vite terminal output.
