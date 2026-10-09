import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import api from '../api.js'

export default function Register() {
  const navigate = useNavigate()
  const [form, setForm] = useState({ name: '', email: '', password: '', phone: '' })
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)

  const set = (k) => (e) => setForm((f) => ({ ...f, [k]: e.target.value }))

  const submit = async (e) => {
    e.preventDefault()
    setError('')
    setLoading(true)
    try {
      await api.registerAndStore(form)
      navigate('/')
    } catch (err) {
      setError(err.message)
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="page narrow">
      <div className="card">
        <h2>Create an account</h2>
        {error && <div className="alert error">{error}</div>}
        <form onSubmit={submit}>
          <label>
            Full name
            <input value={form.name} onChange={set('name')} required autoFocus />
          </label>
          <label>
            Email
            <input type="email" value={form.email} onChange={set('email')} placeholder="you@example.com" required />
          </label>
          <label>
            Password
            <input type="password" value={form.password} onChange={set('password')} minLength={6} required />
          </label>
          <label>
            Phone
            <input value={form.phone} onChange={set('phone')} placeholder="Optional" />
          </label>
          <button className="btn btn-primary full" disabled={loading}>
            {loading ? 'Creating account…' : 'Register'}
          </button>
        </form>
        <p className="muted center">
          Already have an account? <Link to="/login">Login here</Link>
        </p>
      </div>
    </div>
  )
}