import { useState, useEffect } from 'react'
import { Link } from 'react-router-dom'
import api from '../api.js'

const STATUS_COLORS = {
  PENDING: 'badge pending',
  CONFIRMED: 'badge confirmed',
  CANCELLED: 'badge cancelled',
  COMPLETED: 'badge completed'
}

export default function MyReservations() {
  const [reservations, setReservations] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [busy, setBusy] = useState(null)

  const load = async () => {
    setLoading(true)
    setError('')
    try {
      setReservations(await api.myReservations())
    } catch (err) {
      setError(err.message)
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    load()
  }, [])

  const cancel = async (id) => {
    setBusy(id)
    try {
      await api.cancelReservation(id)
      await load()
    } catch (err) {
      setError(err.message)
    } finally {
      setBusy(null)
    }
  }

  if (loading) return <div className="page narrow"><p className="muted">Loading your reservations…</p></div>

  return (
    <div className="page">
      <h1 className="page-title">My Reservations</h1>
      {error && <div className="alert error">{error}</div>}

      {!reservations.length ? (
        <div className="card center-card">
          <p className="muted">You don't have any reservations yet.</p>
          <Link className="btn btn-primary" to="/">Book a table</Link>
        </div>
      ) : (
        <div className="res-list">
          {reservations.map((r) => (
            <div className="card res-item" key={r.reservationId}>
              <div className="res-head">
                <div>
                  <b>#{r.reservationId}</b> · {r.combo ? 'Combo' : 'Table'}
                  <b> {r.tableNumbers?.join(' + ') || r.tableNumber}</b>
                  {r.combo && <span className="muted"> (total {r.totalSeats} seats)</span>}
                  {!r.combo && <span className="muted"> ({r.capacity} seats)</span>}
                </div>
                <span className={STATUS_COLORS[r.status] || 'badge'}>{r.status}</span>
              </div>
              <p>
                {r.reservationDate} at {r.reservationTime} · {r.numberOfPeople} people
              </p>
              {r.occasion && r.occasion !== 'NONE' && (
                <p className="muted">
                  Occasion: {r.occasionLabel}
                  {r.occasionNote && ` · ${r.occasionNote}`}
                  {r.prepReadyTime && ` · prep ready ${r.prepReadyTime}`}
                </p>
              )}
              {(r.status === 'PENDING' || r.status === 'CONFIRMED') && (
                <button
                  className="btn btn-danger btn-sm"
                  disabled={busy === r.reservationId}
                  onClick={() => cancel(r.reservationId)}
                >
                  Cancel reservation
                </button>
              )}
            </div>
          ))}
        </div>
      )}
    </div>
  )
}