import React from 'react';
import { Link } from 'react-router-dom';

const Home = () => {
  return (
    <div className="animate-fade-in" style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', gap: '3rem', marginTop: '4rem' }}>
      <div style={{ textAlign: 'center' }}>
        <h2 className="title-glow" style={{ fontSize: '3.5rem', marginBottom: '1rem' }}>Elevate Your Mind</h2>
        <p style={{ color: 'var(--text-dim)', fontSize: '1.2rem', maxWidth: '600px' }}>
          Welcome to the next generation of Sudoku. Train against intelligence analytics or endure the infinite survival climb.
        </p>
      </div>

      <div style={{ display: 'flex', gap: '2rem', flexWrap: 'wrap', justifyContent: 'center' }}>
        <div className="glass-panel" style={{ padding: '2rem', width: '300px', textAlign: 'center', border: '1px solid rgba(147, 51, 234, 0.5)' }}>
          <h3 style={{ fontSize: '1.5rem', marginBottom: '1rem', color: '#c084fc' }}>Static Levels</h3>
          <p style={{ color: 'var(--text-dim)', marginBottom: '2rem' }}>Play 9 hand-crafted levels separated by distinct ML difficulty tiers.</p>
          <Link to="/levels">
            <button className="primary-btn" style={{ width: '100%' }}>Select Level</button>
          </Link>
        </div>

        <div className="glass-panel" style={{ padding: '2rem', width: '300px', textAlign: 'center', border: '1px solid rgba(147, 51, 234, 0.5)' }}>
          <h3 style={{ fontSize: '1.5rem', marginBottom: '1rem', color: '#c084fc' }}>Survival Mode</h3>
          <p style={{ color: 'var(--text-dim)', marginBottom: '2rem' }}>Generate an infinite progression of structurally sound puzzles dynamically scaling in difficulty.</p>
          <Link to="/survival">
            <button className="primary-btn" style={{ width: '100%' }}>Begin Descent</button>
          </Link>
        </div>
      </div>
    </div>
  );
};

export default Home;
