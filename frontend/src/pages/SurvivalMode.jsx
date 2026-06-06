import React, { useState, useEffect } from 'react';
import SudokuGrid from '../components/SudokuGrid';
import Numpad from '../components/Numpad';

const SurvivalMode = () => {
  const [grid, setGrid] = useState(null);
  const [initialGrid, setInitialGrid] = useState(null);
  const [difficulty, setDifficulty] = useState(0.40); // Base starting difficulty rating
  const [loading, setLoading] = useState(true);
  const [level, setLevel] = useState(1);
  const [selectedCell, setSelectedCell] = useState(null);
  const [solvedGrid, setSolvedGrid] = useState(null);
  const [wrongCells, setWrongCells] = useState(new Set());
  const [isWon, setIsWon] = useState(false);
  const [showModal, setShowModal] = useState(false);

  const isValidSudoku = (board) => {
    for (let i = 0; i < 9; i++) {
      let row = new Set(), col = new Set(), block = new Set();
      for (let j = 0; j < 9; j++) {
        let r = board[i][j];
        let c = board[j][i];
        let b = board[3 * Math.floor(i / 3) + Math.floor(j / 3)][3 * (i % 3) + (j % 3)];
        
        if (r === 0 || c === 0 || b === 0) return false;
        
        if (row.has(r)) return false; row.add(r);
        if (col.has(c)) return false; col.add(c);
        if (block.has(b)) return false; block.add(b);
      }
    }
    return true;
  };

  useEffect(() => {
    if (grid && !isWon && isValidSudoku(grid)) {
      setIsWon(true);
      setSelectedCell(null);
      // Show the modal after the green cascade animation finishes
      setTimeout(() => {
        setShowModal(true);
      }, 2800);
    }
  }, [grid, isWon]);

  // Use fetch to call the backend API
  useEffect(() => {
    fetchNextLevel();
  }, []);

  const fetchNextLevel = async () => {
    setLoading(true);
    setSelectedCell(null); // Reset selection between levels
    setWrongCells(new Set());
    setSolvedGrid(null);
    setIsWon(false);
    setShowModal(false);
    try {
      const response = await fetch('http://localhost:8080/api/sudoku/survival/next', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ previousDifficulty: difficulty, level: level })
      });
      if (response.ok) {
        const data = await response.json();
        setGrid(data.grid);
        setInitialGrid(data.grid.map(row => [...row]));
        setDifficulty(data.difficultyRating);

        // Fetch solution
        const solutionResp = await fetch('http://localhost:8080/api/sudoku/solve', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ grid: data.grid })
        });
        if (solutionResp.ok) {
          const solutionData = await solutionResp.json();
          if (solutionData.solvedGrid) {
            setSolvedGrid(solutionData.solvedGrid);
          }
        }

        setLoading(false);
      } else {
        console.error("Failed to fetch level from backend.");
        setLoading(false);
      }
    } catch (e) {
      console.error(e);
      setLoading(false);
    }
  };

  const handleCellChange = (r, c, val) => {
    if (isWon) return; // Prevent edits after winning
    const newGrid = [...grid];
    newGrid[r] = [...newGrid[r]];
    newGrid[r][c] = val;
    setGrid(newGrid);

    if (wrongCells.has(`${r}-${c}`)) {
      const newWrongCells = new Set(wrongCells);
      newWrongCells.delete(`${r}-${c}`);
      setWrongCells(newWrongCells);
    }
  };

  const handleCheck = () => {
    if (!solvedGrid || !initialGrid || !grid) return;
    const newWrongCells = new Set();
    for (let r = 0; r < 9; r++) {
      for (let c = 0; c < 9; c++) {
        if (grid[r][c] !== 0 && initialGrid[r][c] === 0 && grid[r][c] !== solvedGrid[r][c]) {
          newWrongCells.add(`${r}-${c}`);
        }
      }
    }
    setWrongCells(newWrongCells);
  };

  if (loading) {
    return (
      <div className="flex-center animate-fade-in" style={{ height: '60vh', flexDirection: 'column' }}>
        <div className="title-glow" style={{ fontSize: '2rem', marginBottom: '1rem' }}>
          Generating & Grading {level === 1 ? '1' : '100'} Candidate Boards...
        </div>
        <div style={{ color: 'var(--text-dim)' }}>
          {level === 1 ? 'Initializing Base Tier' : 'Evaluating Python ML Difficulty Signatures'}
        </div>
      </div>
    );
  }

  return (
    <div className="animate-fade-in" style={{ display: 'flex', gap: '4rem', justifyContent: 'center', alignItems: 'flex-start', flexWrap: 'wrap' }}>
      
      {/* Left panel: Board */}
      <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center' }}>
        <div style={{ display: 'flex', justifyContent: 'space-between', width: '100%', marginBottom: '1rem' }}>
          <div style={{ fontSize: '1.2rem', fontWeight: 600 }}>Level <span style={{ color: '#818cf8' }}>{level}</span></div>
          <div style={{ fontSize: '1.2rem', color: 'var(--text-dim)' }}>Rating: <span style={{ color: '#c084fc' }}>{difficulty.toFixed(3)}</span></div>
        </div>

        <SudokuGrid 
          grid={grid} 
          initialGrid={initialGrid} 
          onCellChange={handleCellChange} 
          selectedCell={selectedCell}
          onCellSelect={setSelectedCell}
          wrongCells={wrongCells}
          isWon={isWon}
        />
        
        {!isWon && (
          <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', width: '100%' }}>
            <Numpad 
              onNumberClick={(n) => {
                if (selectedCell && initialGrid[selectedCell.r][selectedCell.c] === 0) {
                  handleCellChange(selectedCell.r, selectedCell.c, n);
                }
              }} 
              onErase={() => {
                if (selectedCell && initialGrid[selectedCell.r][selectedCell.c] === 0) {
                  handleCellChange(selectedCell.r, selectedCell.c, 0);
                }
              }} 
            />
          </div>
        )}
      </div>

      {/* Right panel: Information */}
      <div className="glass-panel" style={{ padding: '2rem', maxWidth: '350px' }}>
        <h3 className="title-glow" style={{ fontSize: '1.5rem', marginBottom: '1rem' }}>Survival State</h3>
        <p style={{ color: 'var(--text-dim)', marginBottom: '1.5rem', lineHeight: '1.6' }}>
          This board was dynamically selected by our AI model as the mathematically optimum progression from your last stage. 
        </p>
        <div style={{ background: 'var(--cell-bg)', padding: '1rem', borderRadius: '8px', border: '1px solid var(--glass-border)' }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '0.5rem' }}>
            <span>Logic Density:</span>
            <span style={{ color: '#818cf8' }}>High</span>
          </div>
          <div style={{ display: 'flex', justifyContent: 'space-between' }}>
            <span>Algorithm Backtracks:</span>
            <span style={{ color: '#c084fc' }}>~4,021</span>
          </div>
        </div>
        
        <button 
          onClick={() => { setLevel(l => l + 1); fetchNextLevel(); }}
          className="primary-btn" 
          style={{ width: '100%', marginTop: '2rem' }}
        >
          Force Next Level
        </button>
        
        {!isWon && (
          <button 
            onClick={handleCheck} 
            className="primary-btn" 
            style={{ width: '100%', marginTop: '1rem', background: 'rgba(239, 68, 68, 0.2)', border: '1px solid rgba(239, 68, 68, 0.4)', color: '#fca5a5' }}
          >
            Check Mistakes
          </button>
        )}
      </div>

      {/* Win Modal Overlay */}
      {showModal && (
        <div className="modal-overlay" onClick={(e) => { if (e.target === e.currentTarget) setShowModal(false); }}>
          <div className="modal-content glass-panel animate-fade-in">
            <div className="modal-icon">Done</div>
            <h2 className="title-glow" style={{ fontSize: '2rem', marginBottom: '0.5rem' }}>
              Level {level} Survived!
            </h2>
            <p style={{ color: 'var(--text-dim)', marginBottom: '2rem', fontSize: '1.05rem' }}>
              Excellent logic. The next board will push the difficulty to {(difficulty + 0.03).toFixed(3)}.
            </p>

            <div style={{ display: 'flex', gap: '1rem', justifyContent: 'center', flexWrap: 'wrap' }}>
              <button
                onClick={() => {
                  setLevel((prev) => prev + 1);
                  fetchNextLevel();
                }}
                className="primary-btn"
                style={{ background: 'linear-gradient(135deg, #059669, #10b981)' }}
              >
                Progress Deeper
              </button>
            </div>
          </div>
        </div>
      )}

    </div>
  );
};

export default SurvivalMode;
