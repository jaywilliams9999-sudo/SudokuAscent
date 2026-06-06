import React, { useState, useEffect } from 'react';

const SudokuGrid = ({ grid, onCellChange, initialGrid, selectedCell, onCellSelect, isWon, wrongCells = new Set() }) => {

  const handleCellClick = (r, c) => {
    if (isWon) return;
    if (initialGrid[r][c] !== 0) return;
    onCellSelect({ r, c });
  };

  useEffect(() => {
    const handleKeyDown = (e) => {
      if (isWon) return;
      if (!selectedCell) return;
      if (e.key > '0' && e.key <= '9') {
        onCellChange(selectedCell.r, selectedCell.c, parseInt(e.key));
      } else if (e.key === 'Backspace' || e.key === 'Delete') {
        onCellChange(selectedCell.r, selectedCell.c, 0);
      }
    };
    window.addEventListener('keydown', handleKeyDown);
    return () => window.removeEventListener('keydown', handleKeyDown);
  }, [selectedCell, onCellChange, isWon]);

  return (
    <div
      className={`sudoku-grid-container${isWon ? ' grid-won' : ''}`}
      style={{
        display: 'grid',
        gridTemplateColumns: 'repeat(9, 1fr)',
        gap: '2px',
        background: 'var(--glass-border)',
        padding: '4px',
        borderRadius: '8px',
        width: '100%',
        minWidth: '400px',
        maxWidth: '500px',
        aspectRatio: '1',
        transition: 'box-shadow 0.6s ease',
        boxShadow: isWon ? '0 0 40px rgba(74, 222, 128, 0.35)' : 'none'
      }}
    >
      {grid.map((row, r) => (
        row.map((val, c) => {
          const isPrefilled = initialGrid[r][c] !== 0;
          const isSelected = selectedCell?.r === r && selectedCell?.c === c;
          const isWrong = wrongCells.has(`${r}-${c}`);
          // Cascade delay: sweep left-to-right, top-to-bottom
          const cascadeDelay = (r * 9 + c) * 25; // 25ms between each cell

          let cellStyle = {
            background: isWon
              ? undefined  // handled by animation
              : isWrong
                ? 'rgba(239, 68, 68, 0.25)'
                : isSelected
                  ? 'var(--cell-active)'
                  : isPrefilled
                    ? 'var(--cell-prefilled)'
                    : 'var(--cell-bg)',
            display: 'flex', alignItems: 'center', justifyContent: 'center',
            fontSize: '1.5rem', fontWeight: isPrefilled ? '800' : '600',
            cursor: isWon ? 'default' : (isPrefilled ? 'default' : 'pointer'),
            userSelect: 'none',
            color: isWon ? '#fff' : (isWrong ? '#fca5a5' : (isPrefilled ? '#e2e8f0' : (isSelected ? '#818cf8' : 'white'))),
            boxShadow: isSelected && !isWon ? 'inset 0 0 10px rgba(79, 70, 229, 0.5)' : 'none',
            borderRight: (c % 3 === 2 && c !== 8) ? '3px solid rgba(255, 255, 255, 0.4)' : 'none',
            borderBottom: (r % 3 === 2 && r !== 8) ? '3px solid rgba(255, 255, 255, 0.4)' : 'none',
            transition: 'all 0.2s ease',
            animationDelay: isWon ? `${cascadeDelay}ms` : undefined,
          };

          return (
            <div
              key={`${r}-${c}`}
              className={isWon ? 'cell-won' : ''}
              onClick={() => handleCellClick(r, c)}
              style={cellStyle}
              onMouseOver={(e) => { if(!isPrefilled && !isSelected && !isWon && !isWrong) e.target.style.background = 'var(--cell-hover)'; }}
              onMouseOut={(e) => { if(!isPrefilled && !isSelected && !isWon && !isWrong) e.target.style.background = 'var(--cell-bg)'; }}
            >
              {val === 0 ? '' : val}
            </div>
          );
        })
      ))}
    </div>
  );
};

export default SudokuGrid;
