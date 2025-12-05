import React from 'react';
import { Link, NavLink } from 'react-router-dom';

export default function Navbar() {
  return (
    <nav className="navbar">
      <div className="container" style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
        <Link
          to="/"
          style={{
            fontSize: '1.5rem',
            fontWeight: 700,
            color: 'var(--text-primary)',
            display: 'flex',
            alignItems: 'center',
            gap: '0.5rem',
            textDecoration: 'none',
          }}
        >
          🏦 Banking System
        </Link>

        <div style={{ display: 'flex', gap: '0.5rem' }}>
          <NavLink
            to="/"
            className={({ isActive }) =>
              `nav-link${isActive ? ' active' : ''}`
            }
          >
            📊 Dashboard
          </NavLink>

          <NavLink
            to="/events"
            className={({ isActive }) =>
              `nav-link${isActive ? ' active' : ''}`
            }
          >
            📜 Events
          </NavLink>
        </div>

        <div style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>
          SAGA Pattern | Event Sourcing
        </div>
      </div>
    </nav>
  );
}
