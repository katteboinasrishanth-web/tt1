import { Routes, Route, Navigate } from 'react-router-dom'
import Navbar from './components/Navbar.jsx'
import Home from './pages/Home.jsx'
import Login from './pages/Login.jsx'
import Register from './pages/Register.jsx'
import MyReservations from './pages/MyReservations.jsx'
import Admin from './pages/Admin.jsx'
import { isLoggedIn, getRole } from './api.js'

function RequireAuth({ children }) {
  return isLoggedIn() ? children : <Navigate to="/login" replace />
}

function RequireAdmin({ children }) {
  return isLoggedIn() && getRole() === 'ADMIN' ? (
    children
  ) : (
    <Navigate to="/login" replace />
  )
}

export default function App() {
  return (
    <div className="app">
      <Navbar />
      <main className="content">
        <Routes>
          <Route path="/" element={<Home />} />
          <Route path="/login" element={<Login />} />
          <Route path="/register" element={<Register />} />
          <Route
            path="/my-reservations"
            element={
              <RequireAuth>
                <MyReservations />
              </RequireAuth>
            }
          />
          <Route
            path="/admin"
            element={
              <RequireAdmin>
                <Admin />
              </RequireAdmin>
            }
          />
          <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>
      </main>
      <footer className="footer">Dine · Restaurant Table Reservation System</footer>
    </div>
  )
}