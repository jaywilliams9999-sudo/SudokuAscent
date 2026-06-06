import React from 'react';
import { Link } from 'react-router-dom';

const LevelSelect = () => {
  const levels = [
    { id: 1, difficulty: 'Easy' },
    { id: 2, difficulty: 'Easy' },
    { id: 3, difficulty: 'Easy' },
    { id: 4, difficulty: 'Medium' },
    { id: 5, difficulty: 'Medium' },
    { id: 6, difficulty: 'Medium' },
    { id: 7, difficulty: 'Hard' },
    { id: 8, difficulty: 'Hard' },
    { id: 9, difficulty: 'Hard' }
  ];

  return (
    <div className="animate-fade-in" style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', gap: '2rem' }}>
      <h2 className="title-glow" style={{ fontSize: '2.5rem' }}>Select Level</h2>
      
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(250px, 1fr))', gap: '2rem', width: '100%', maxWidth: '900px' }}>
        
        {['Easy', 'Medium', 'Hard'].map((diffCategory) => (
          <div key={diffCategory} className="glass-panel" style={{ padding: '2rem', display: 'flex', flexDirection: 'column', alignItems: 'center' }}>
            <h3 style={{ fontSize: '1.5rem', marginBottom: '1.5rem', color: diffCategory === 'Easy' ? '#4ade80' : diffCategory === 'Medium' ? '#facc15' : '#f87171' }}>
              {diffCategory}
            </h3>
            <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem', width: '100%' }}>
              {levels.filter(l => l.difficulty === diffCategory).map(level => (
                <Link key={level.id} to={`/levels/${level.id}`} style={{ textDecoration: 'none', width: '100%' }}>
                  <button className="primary-btn" style={{ width: '100%', padding: '1rem' }}>
                    Level {level.id}
                  </button>
                </Link>
              ))}
            </div>
          </div>
        ))}
        
      </div>
    </div>
  );
};

export default LevelSelect;
