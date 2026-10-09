import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import api from '../api.js'

export default function Login() {
  const navigate = useNavigate()
  const [mode, setMode] = useState('customer')
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [username, setUsername] = useState('')
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)

  const submit = async (e) => {
    e.preventDefault()
    setError('')
    setLoading(true)
    try {
      if (mode === 'admin') {
        await api.adminLoginAndStore({ username, password })
        navigate('/admin')
      } else {
        await api.loginAndStore({ email, password })
        navigate('/')
      }
    } catch (err) {
      setError(err.message)
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="page narrow">
      <div className="card">
        <div className="tabs">
          <button
            className={`tab ${mode === 'customer' ? 'active' : ''}`}
            onClick={() => setMode('customer')}
          >
            Customer Login
          </button>
          <button
            className={`tab ${mode === 'admin' ? 'active' : ''}`}
            onClick={() => setMode('admin')}
          >
            Admin Login
          </button>
        </div>

        <h2>{mode === 'admin' ? 'Admin login' : 'Customer login'}</h2>

        {mode === 'admin' && (
          <p className="muted">Demo admin: <code>sri</code> / <code>sri123</code></p>
        )}

        {error && <div className="alert error">{error}</div>}

        <form onSubmit={submit}>
          {mode === 'customer' ? (
            <label>
              Email
              <input
                type="email"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                placeholder="you@example.com"
                required
              />
            </label>
          ) : (
            <label>
              Username
              <input
                value={username}
                onChange={(e) => setUsername(e.target.value)}
                placeholder="sri"
                required
              />
            </label>
          )}
          <label>
            Password
            <input
              type="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              required
            />
          </label>
          <button className="btn btn-primary full" disabled={loading}>
            {loading ? 'Logging in…' : 'Login'}
          </button>
        </form>

        {mode === 'customer' && (
          <p className="muted center">
            Don't have an account? <Link to="/register">Register here</Link>
          </p>
        )}
      </div>
    </div>
  )
}