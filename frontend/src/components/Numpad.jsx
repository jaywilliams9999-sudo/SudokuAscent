import React from 'react';

const Numpad = ({ onNumberClick, onErase }) => {
  const nums = [1, 2, 3, 4, 5, 6, 7, 8, 9];
  
  return (
    <div className="glass-panel" style={{ padding: '1.5rem', marginTop: '2rem', display: 'flex', flexWrap: 'wrap', gap: '10px', justifyContent: 'center', width: '100%', maxWidth: '500px' }}>
      {nums.map((num) => (
        <button 
          key={num}
          onClick={() => onNumberClick(num)}
          style={{
            width: '15%', minWidth: '45px', height: '55px', 
            borderRadius: '12px', border: '1px solid var(--glass-border)',
            background: 'var(--cell-bg)', color: 'white', fontSize: '1.5rem',
            cursor: 'pointer', transition: 'all 0.2s ease'
          }}
          onMouseOver={(e) => e.target.style.background = 'var(--cell-hover)'}
          onMouseOut={(e) => e.target.style.background = 'var(--cell-bg)'}
        >
          {num}
        </button>
      ))}
      <button 
        onClick={onErase}
        style={{
          width: 'calc(100% - 14px)', height: '50px', 
          borderRadius: '12px', border: '1px solid rgba(147, 51, 234, 0.4)',
          background: 'rgba(147, 51, 234, 0.1)', color: '#c084fc', fontSize: '1.2rem',
          cursor: 'pointer', marginTop: '5px', transition: 'all 0.2s ease'
        }}
        onMouseOver={(e) => e.target.style.background = 'rgba(147, 51, 234, 0.2)'}
        onMouseOut={(e) => e.target.style.background = 'rgba(147, 51, 234, 0.1)'}
      >
        Erase Selected Cell
      </button>
    </div>
  );
};

export default Numpad;
