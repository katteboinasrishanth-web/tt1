import { Link, useLocation } from 'react-router-dom'
import { isLoggedIn, getRole, getUser, clearAuth } from '../api.js'
import { useNavigate } from 'react-router-dom'

export default function Navbar() {
  const navigate = useNavigate()
  const location = useLocation()
  const loggedIn = isLoggedIn()
  const role = getRole()
  const user = getUser()

  const handleLogout = () => {
    clearAuth()
    navigate('/')
  }

  const link = (to, label) => (
    <Link to={to} className={location.pathname === to ? 'nav-link active' : 'nav-link'}>
      {label}
    </Link>
  )

  return (
    <header className="navbar">
      <Link to="/" className="brand">
        🍽️ <b>Dine</b> <span>Reservations</span>
      </Link>
      <nav className="nav-links">
        {link('/', 'Home')}
        {role === 'ADMIN' && link('/admin', 'Admin Dashboard')}
        {loggedIn && role === 'CUSTOMER' && link('/my-reservations', 'My Reservations')}
        {!loggedIn && link('/login', 'Login')}
        {!loggedIn && link('/register', 'Register')}
        {loggedIn && (
          <button className="btn btn-outline btn-sm" onClick={handleLogout}>
            Logout {user?.name ? `(${user.name})` : ''}
          </button>
        )}
      </nav>
    </header>
  )
}