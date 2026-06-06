import React, { useState, useEffect } from 'react';
import { useParams, Link, useNavigate } from 'react-router-dom';
import SudokuGrid from '../components/SudokuGrid';
import Numpad from '../components/Numpad';

const PlayLevel = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const levelId = parseInt(id);

  const [grid, setGrid] = useState(null);
  const [initialGrid, setInitialGrid] = useState(null);
  const [difficulty, setDifficulty] = useState(0);
  const [loading, setLoading] = useState(true);
  const [selectedCell, setSelectedCell] = useState(null);
  const [isWon, setIsWon] = useState(false);
  const [showModal, setShowModal] = useState(false);
  const [solvedGrid, setSolvedGrid] = useState(null);
  const [wrongCells, setWrongCells] = useState(new Set());

  useEffect(() => {
    fetchLevel();
  }, [levelId]);

  const fetchLevel = async () => {
    setLoading(true);
    setSelectedCell(null);
    setIsWon(false);
    setShowModal(false);
    setWrongCells(new Set());
    setSolvedGrid(null);
    try {
      const response = await fetch(`http://localhost:8080/api/sudoku/static/${levelId}`);
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
        console.error("Failed to fetch static level from backend.");
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
    if (!solvedGrid) return;
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
      // Wait approximately 2.8 seconds to allow the win animation to finish
      setTimeout(() => {
        setShowModal(true);
      }, 2800);
    }
  }, [grid]);

  const isFinalLevel = levelId >= 9;

  if (loading) {
    return (
      <div className="flex-center animate-fade-in" style={{ height: '60vh', flexDirection: 'column' }}>
        <div className="title-glow" style={{ fontSize: '2rem', marginBottom: '1rem' }}>Loading Level {levelId}...</div>
        <div style={{ color: 'var(--text-dim)' }}>Retrieving from Server</div>
      </div>
    );
  }

  return (
    <div className="animate-fade-in" style={{ display: 'flex', gap: '4rem', justifyContent: 'center', alignItems: 'flex-start', flexWrap: 'wrap' }}>
      
      {/* Left panel: Board */}
      <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center' }}>
        <div style={{ display: 'flex', justifyContent: 'space-between', width: '100%', marginBottom: '1rem' }}>
          <div style={{ fontSize: '1.2rem', fontWeight: 600 }}>Level <span style={{ color: '#818cf8' }}>{levelId}</span></div>
          <div style={{ fontSize: '1.2rem', color: 'var(--text-dim)' }}>Rating: <span style={{ color: '#c084fc' }}>{difficulty.toFixed(3)}</span></div>
        </div>

        <SudokuGrid 
          grid={grid} 
          initialGrid={initialGrid} 
          onCellChange={handleCellChange} 
          selectedCell={selectedCell}
          onCellSelect={setSelectedCell}
          isWon={isWon}
          wrongCells={wrongCells}
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
            <button 
              onClick={handleCheck} 
              className="primary-btn" 
              style={{ marginTop: '1.5rem', background: 'rgba(239, 68, 68, 0.2)', border: '1px solid rgba(239, 68, 68, 0.4)', color: '#fca5a5' }}
            >
              Check Mistakes
            </button>
          </div>
        )}
      </div>

      {/* Win Modal Overlay */}
      {showModal && (
        <div className="modal-overlay" onClick={(e) => { if (e.target === e.currentTarget) setShowModal(false); }}>
          <div className="modal-content glass-panel animate-fade-in">
            <div className="modal-icon">Done</div>
            <h2 className="title-glow" style={{ fontSize: '2rem', marginBottom: '0.5rem' }}>
              Level {levelId} Complete!
            </h2>
            <p style={{ color: 'var(--text-dim)', marginBottom: '2rem', fontSize: '1.05rem' }}>
              {isFinalLevel
                ? 'You conquered the final level. Ready for the infinite climb?'
                : `You solved level ${levelId} with a difficulty rating of ${difficulty.toFixed(3)}.`
              }
            </p>

            <div style={{ display: 'flex', gap: '1rem', justifyContent: 'center', flexWrap: 'wrap' }}>
              <button
                onClick={() => navigate('/')}
                className="primary-btn"
                style={{ background: 'transparent', border: '1px solid rgba(255,255,255,0.2)', color: 'var(--text-dim)' }}
              >
                Main Menu
              </button>

              {isFinalLevel ? (
                <button
                  onClick={() => navigate('/survival')}
                  className="primary-btn"
                  style={{ background: 'linear-gradient(135deg, #059669, #10b981)' }}
                >
                  Enter Survival Mode
                </button>
              ) : (
                <button
                  onClick={() => navigate(`/levels/${levelId + 1}`)}
                  className="primary-btn"
                >
                  Next Level
                </button>
              )}
            </div>
          </div>
        </div>
      )}

    </div>
  );
};

export default PlayLevel;
