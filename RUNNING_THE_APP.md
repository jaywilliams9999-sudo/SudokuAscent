# Running Sudoku Ascent

This guide provides the steps required to start the full Sudoku Ascent application. You will need to open three separate terminal windows to run all services concurrently.

## 1. Running the Intelligence Service

The intelligence service is built with Python and FastAPI. It relies on a local virtual environment.

Open your first terminal window and navigate to the project root directory.

Activate the virtual environment:
Use CTRL+SHIFT+P to select interpreter and select the venv. If you have not created it yet, create it and install dependences from requirements.txt

Navigate to the intelligence service directory:
cd intelligence-service

Start the FastAPI application:
uvicorn app.app:app --reload

## 2. Running the Backend

The backend is a Java Spring Boot application managed by Maven.

Open a second terminal window and literally just run SudokuApplication.java

## 3. Running the Frontend

The frontend is a React application built with Vite.

Open a third terminal window and navigate to the project root directory.

Navigate to the frontend directory:
cd frontend

Install the dependencies if you have not already done so:
npm install

Start the development server:
npm run dev

Once all three services are running and report that they have started successfully, you can access the frontend application through the URL provided in the frontend terminal output.
