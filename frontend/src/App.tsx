import React from 'react';
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import { AppProvider } from './context/AppContext'; 
import Dashboard from './components/Dashboard';
import EventTimelinePage from './pages/EventTimelinePage';
import Navbar from './components/Navbar';

function App() {
  return (
    <Router>
      <AppProvider>
        <div className="min-h-screen bg-gradient-to-br from-slate-900 via-purple-900 to-slate-900">
          <Navbar />
          <Routes>
            <Route path="/" element={<Dashboard />} />
            <Route path="/events" element={<EventTimelinePage />} />
          </Routes>
        </div>
      </AppProvider>
    </Router>
  );
}

export default App;
