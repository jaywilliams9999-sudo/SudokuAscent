import React from 'react';
import { BrowserRouter as Router, Routes, Route, Link } from 'react-router-dom';
import Home from './pages/Home';
import SurvivalMode from './pages/SurvivalMode';

import PlayLevel from './pages/PlayLevel';
import LevelSelect from './pages/LevelSelect';

function App() {
  return (
    <Router>
      <div className="container" style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '2rem' }}>
        <Link to="/" style={{ textDecoration: 'none' }}>
          <h1 className="title-glow" style={{ fontSize: '2rem', fontWeight: 800 }}>SudokuAscent</h1>
        </Link>
      </div>

      <div className="container">
        <Routes>
          <Route path="/" element={<Home />} />
          <Route path="/survival" element={<SurvivalMode />} />
          <Route path="/levels" element={<LevelSelect />} />
          <Route path="/levels/:id" element={<PlayLevel />} />
        </Routes>
      </div>
    </Router>
  );
}

export default App;
