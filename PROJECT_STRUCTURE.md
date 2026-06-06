# Sudoku Ascent: File Directory & Architecture Manifest

This document outlines the purpose and functionality of every major file and directory within the Sudoku Ascent project, categorizing them by microservice and structural domains.

---

## 1. Project Root (Documentation & Tooling)

*   **`README.md`**: The primary introduction file detailing what the project is, basic features, requirements, and quick-start instructions.
*   **`RUNNING_THE_APP.md`**: Provides the explicit terminal commands to asynchronously launch all three required servers (Frontend, Backend, Intelligence Service) across multiple terminal instances.
*   **`TEAM_GUIDE.md`**: A developer-focused document primarily explaining the implementation of Git LFS (Large File Storage) rules for managing the 500+ MB training datasets and how to handle branch resetting.
*   **`PROJECT_OVERVIEW.md`**: An executive presentation guide detailing the project's macro architecture—specifically outlining the AI difficulty scaling and the implementation of Donald Knuth's Algorithm X (Dancing Links) to solve generation bottlenecks.
*   **`generate_presentation.py`**: A Python script utilizing `python-pptx` to programmatically build the slide deck outlining the project's architecture, generating `SudokuAscent_Presentation.pptx`.
*   **`SudokuAscent_Presentation.pptx`**: The compiled PowerPoint slide deck created by the generator script.
*   **`system_architecture_logic.png`**: A static image file utilized for architectural documentation and diagrams.
*   **`fetch_architect_image.py`**: A helper script utilized to download or parse graphical assets like architecture diagrams.
*   **`.gitignore` & `.gitattributes`**: Tell Git what files to ignore (like `node_modules` or `.env`) and configure Git LFS to strictly handle all `*.csv` files, preventing repository bloat.

---

## 2. Backend API (Java & Spring Boot)
*Located in `/backend`*

The backend enforces game rules, generates Sudoku boards using highly optimized matrix traversal, and communicates between the frontend and the machine-learning service.

*   **`pom.xml`**: The Maven configuration file. Defines project metadata, dependencies (like `spring-boot-starter-web` and `lombok`), and the Java 17 compile targets.
*   **`src/main/resources/application.yml`**: Application properties defining the backend port (`8080`) and the connection URL map for the intelligence microservice (`http://localhost:8000`).
*   **`src/main/java/com/sudoku/backend/SudokuApplication.java`**: The Spring Boot initialization class and primary entry point for launching the backend server context.

### Controllers
*   **`controller/SudokuController.java`**: The REST API surface. Defines frontend-accessible endpoints such as `/api/sudoku/analyze`, `/api/sudoku/solve`, `/api/sudoku/survival/next`, and `/api/sudoku/static/{levelId}`.

### DTOs (Data Transfer Objects)
*(Found in `dto/`)*
These files shape the JSON payloads moving between microservices and the client browser:
*   **`FrontendResponse.java`**: Standardizes payloads sent back to the React client.
*   **`IntelligenceResponse.java`**: Maps the structure of the JSON received from the Python ML service (capturing the difficulty rating).
*   **`SudokuGridRequest.java`**, **`SurvivalRequest.java`**, **`SurvivalResponse.java`**: Formats specific POST payloads regarding grid states and candidate board scaling parameters.

### Services (Core Business Logic)
*(Found in `service/`)*
*   **`SudokuSolverService.java`**: The algorithmic heavyweight. Employs "Algorithm X" via "Dancing Links" (DLX). Re-frames a 9x9 board into an exact-cover matrix utilizing linked node manipulation to generate uniquely solvable Sudoku pathways in sub-milliseconds.
*   **`IntelligenceServiceClient.java`**: Spring Boot REST component responsible for executing HTTP `POST` requests to the external Python FastAPI application for board difficulty scoring.
*   **`StaticLevelService.java`**: Evaluates and routes requested static-level generation loops (Easy, Medium, Hard).
*   **`SurvivalModeService.java`**: Orchestrates the core progression logic for Survival Mode. Capable of rapidly generating up to 100 candidates through the `SudokuSolverService`, batching them to the intelligence service, and returning the board that fits the exact fractional difficulty target.

---

## 3. Frontend Application (React & Vite)
*Located in `/frontend`*

The presentation layer built around reactive components, sleek glassmorphic UI, and asynchronous state management.

*   **`package.json` & `package-lock.json`**: NPM package manifest dictating installed libraries (React, Vite, react-router, TailwindCSS, etc.) and run scripts (`npm run dev`).
*   **`vite.config.js`**: The configuration for the Vite bundler optimizing hot-module reloading and compiling React JSX into optimized browser-native JS.
*   **`index.html`**: The root DOM element the React tree attaches to.
*   **`src/main.jsx` & `src/App.jsx`**: The application's core wrapper. Injects global CSS stylesheets and dictating application routing via `react-router-dom`.
*   **`src/index.css`**: Central stylesheet utilizing Tailwind CSS directives alongside custom animations, structural tokens, and glassmorphic UI variables.

### Pages (Navigation Routes)
*(Found in `src/pages/`)*
*   **`Home.jsx`**: Landing screen with animated title sequences and route selectors.
*   **`LevelSelect.jsx`**: The classical game menu permitting user selection between static board difficulty tiers.
*   **`PlayLevel.jsx`**: Core game scene mapping static game logic, time-tracking, and classic UI overlays.
*   **`SurvivalMode.jsx`**: The dynamic infinite-play page. Houses specialized loading visualizations as it waits for the complex API generation callbacks and iterates player level streaks dynamically.

### Components (Reusable UI)
*(Found in `src/components/`)*
*   **`SudokuGrid.jsx`**: The visual 9x9 matrix mapped into React state. Listens for user input mapping, highlights matching clusters conditionally, marks numerical errors functionally, and triggers complex CSS cascading animations upon completion.
*   **`Numpad.jsx`**: A scalable 1-9 button structure allowing pointer mapping (touch/mouse) to sync with internal state selections natively.

---

## 4. Intelligence Service (Machine Learning)
*Located in `/intelligence-service`*

The Python-powered AI inference microservice designed to convert naive matrices into feature sets and measure how difficult a human finds the solution path.

*   **`requirements.txt`**: Registry for the Python environment (pip) listing libraries like `fastapi`, `uvicorn`, `scikit-learn`, `numpy`, and `pandas`.

### API Layer
*   **`app/app.py`**: Maps the FastAPI REST structure, manages startup schemas, and acts as the model execution bridge. It accepts parsed 9x9 grids, extracts statistical features, invokes the loaded machine learning model (`model.pkl`), and returns the predicted difficulty index back to the backend.

### Feature Extraction
*(Found in `feature_extractor/`)*
A trained ML model needs statistical categories to measure context. These tools convert a 2D puzzle array into mathematical representations.
*   **`basic_stats.py`**: Extracts structural statistics (e.g., cell sparsity, clustering rates, numeric distribution variances).
*   **`tactic_checker.py`**: Evaluates the board's sparsity to generate broad logical difficulty flags (basic, advanced, extreme logic).

### Models & Training
*(Found in `models/` and `training/`)*
*   **`models/model.pkl` & `model_new.pkl`**: Pickled serialization binaries representing the fully "trained" machine learning pathways (such as Gradient Boosting Regressors) capable of spitting out accurate scale assessments in milliseconds. 
*   **`training/data_prep.py`**: Standardizes the initial massive dataset strings utilizing scikit-learn standard scalers converting strings like `'000000010...'` into explicit target matrices.
*   **`training/sudoku-3m.csv`**: A colossal ~511 MB dataset tracking raw boards over 3 million data points, natively skipped via Git LFS pointers to manage file sizes.
*   **`training/train_model`**: Script that automates running `sudoku-3m.csv` across regression trees, measuring validation error rates (Mean Absolute Error), to automatically output and save newly optimized `model.pkl` weights.
