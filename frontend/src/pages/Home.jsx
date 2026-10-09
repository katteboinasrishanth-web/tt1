import { useState } from 'react'
import { Link } from 'react-router-dom'
import api, { isLoggedIn } from '../api.js'
import { OCCASIONS, occasionOf, tablesLabel } from '../constants.js'

const TIME_SLOTS = ['12:00', '13:00', '14:00', '18:00', '19:00', '20:00', '21:00', '22:00']

const today = () => new Date().toISOString().split('T')[0]

export default function Home() {
  const [date, setDate] = useState(today())
  const [time, setTime] = useState('19:00')
  const [people, setPeople] = useState(2)
  const [occasion, setOccasion] = useState('NONE')
  const [note, setNote] = useState('')
  const [searching, setSearching] = useState(false)
  const [suggestions, setSuggestions] = useState([])
  const [message, setMessage] = useState({ type: '', text: '' })
  const [selected, setSelected] = useState(null)
  const [booking, setBooking] = useState(false)
  const [confirmed, setConfirmed] = useState(null)

  const notify = (type, text) => {
    setMessage({ type, text })
    setTimeout(() => setMessage({ type: '', text: '' }), 6000)
  }

  const search = async (e) => {
    e.preventDefault()
    setSearching(true)
    setConfirmed(null)
    setSelected(null)
    setMessage({ type: '', text: '' })
    try {
      const data = await api.availableCombos({ date, time, numberOfPeople: people, occasion })
      setSuggestions(data)
      if (!data.length) notify('error', 'No tables available for this slot. Try another time.')
    } catch (err) {
      notify('error', err.message)
    } finally {
      setSearching(false)
    }
  }

  const book = async () => {
    setBooking(true)
    setMessage({ type: '', text: '' })
    try {
      const created = await api.createReservation({
        tableIds: selected.tableIds,
        date,
        time,
        numberOfPeople: people,
        occasion,
        occasionNote: note.trim() || undefined
      })
      setConfirmed(created)
      setSelected(null)
      setSuggestions([])
      notify('success', 'Reservation confirmed! Check "My Reservations" for details.')
    } catch (err) {
      notify('error', err.message)
      // Somebody else grabbed the tables first - refresh the suggestions.
      if (err.message && err.message.toLowerCase().includes('booked')) {
        try {
          setSuggestions(await api.availableCombos({ date, time, numberOfPeople: people, occasion }))
        } catch {
          /* keep the original error */
        }
      }
    } finally {
      setBooking(false)
    }
  }

  return (
    <div className="page">
      <section className="hero">
        <h1>
          Reserve your table at <span className="accent">tt1</span>
        </h1>
        <p className="muted">
          Multi-table combos for large groups, smart best-fit seating and occasion mode with
          automatic setup buffers.
        </p>
      </section>

      {message.text && <div className={`alert ${message.type}`}>{message.text}</div>}

      {confirmed && (
        <div className="card success-card">
          <h3>Reservation Confirmed</h3>
          <p>
            Booking #{confirmed.reservationId} · {tablesLabel(confirmed)}
            {confirmed.totalSeats ? ` (total seats ${confirmed.totalSeats})` : ''}
          </p>
          <p>
            {confirmed.reservationDate} at {confirmed.reservationTime} · {confirmed.numberOfPeople}{' '}
            people
          </p>
          {confirmed.occasion && confirmed.occasion !== 'NONE' && (
            <p className="muted">
              Occasion: {confirmed.occasionLabel}
              {confirmed.setupBufferMinutes ? ` · setup buffer ${confirmed.setupBufferMinutes} min` : ''}
              {confirmed.prepReadyTime ? ` · prep ready ${confirmed.prepReadyTime}` : ''}
            </p>
          )}
          <Link className="btn btn-primary" to="/my-reservations">View My Reservations</Link>
        </div>
      )}

      <form className="card search-form" onSubmit={search}>
        <label>
          Date
          <input type="date" value={date} min={today()} onChange={(e) => setDate(e.target.value)} required />
        </label>
        <label>
          Time
          <select value={time} onChange={(e) => setTime(e.target.value)} required>
            {TIME_SLOTS.map((slot) => (
              <option key={slot} value={slot}>{slot}</option>
            ))}
          </select>
        </label>
        <label>
          Number of people
          <input
            type="number"
            min="1"
            max="20"
            value={people}
            onChange={(e) => setPeople(e.target.value)}
            required
          />
        </label>
        <button className="btn btn-primary" disabled={searching}>
          {searching ? 'Finding best fit…' : 'Find Tables & Combos'}
        </button>
      </form>

      <div className="card occasion-card">
        <h3>
          {occasionOf(occasion).icon} Occasion mode
        </h3>
        <p className="muted">
          Celebrations add a setup buffer, so the table is prepared before your arrival time.
        </p>
        <div className="chips">
          {OCCASIONS.map((o) => (
            <button
              key={o.value}
              type="button"
              className={`chip ${occasion === o.value ? 'active' : ''}`}
              onClick={() => setOccasion(o.value)}
            >
              {o.icon} {o.label}
              <small>{o.buffer ? `+${o.buffer} min setup` : 'no buffer'}</small>
            </button>
          ))}
        </div>
        {occasion !== 'NONE' && (
          <label className="occasion-note">
            Note (optional)
            <input
              value={note}
              onChange={(e) => setNote(e.target.value)}
              placeholder="Cake, name, requests..."
              maxLength={200}
            />
          </label>
        )}
      </div>

      {suggestions.length > 0 && !selected && (
        <section>
          <h2 className="section-title">Best fit for {people} people · {date} at {time}</h2>
          <div className="table-grid">
            {suggestions.map((s, idx) => {
              const numbers = s.tableNumbers || (s.tableNumber ? [s.tableNumber] : [])
              const label = s.label || (numbers.length > 1 ? `Tables ${numbers.join(' + ')}` : `Table ${numbers[0]}`)
              return (
                <div key={`${s.kind}-${idx}`} className={`table-card ${s.kind === 'COMBO' ? 'is-combo' : ''}`}>
                  <div className="table-card-head">
                    <span className="table-icon">{s.kind === 'COMBO' ? '🔗' : '🪑'}</span>
                    <b>{label}</b>
                    {s.kind === 'COMBO' && <span className="combo-badge">COMBO</span>}
                  </div>
                  <p className="muted">
                    Seats: {s.seats} · wasted {s.wastedSeats}
                  </p>
                  {s.zone && <span className="zone-tag">{s.zone}</span>}
                  {s.reason && <p className="suggestion-reason">{s.reason}</p>}
                  {s.prepReadyTime && (
                    <p className="prep-line">
                      Prep ready {s.prepReadyTime}
                      {s.setupBufferMinutes ? ` · buffer ${s.setupBufferMinutes} min` : ''}
                    </p>
                  )}
                  {isLoggedIn() ? (
                    <button className="btn btn-primary btn-sm" onClick={() => setSelected(s)}>
                      Book this option
                    </button>
                  ) : (
                    <Link className="btn btn-outline btn-sm" to="/login">Log in to book</Link>
                  )}
                </div>
              )
            })}
          </div>
        </section>
      )}

      {selected && (
        <div className="card book-card">
          <div className="book-row">
            <div>
              <h3>Confirm booking</h3>
              <p>
                <b>{selected.label}</b> · {date} at {time} · {people} people
              </p>
              {selected.prepReadyTime && (
                <p className="muted">
                  Prep ready {selected.prepReadyTime}
                  {selected.setupBufferMinutes
                    ? ` (setup buffer ${selected.setupBufferMinutes} min)`
                    : ''}
                </p>
              )}
              {occasion !== 'NONE' && (
                <p className="muted">
                  Occasion: {occasionOf(occasion).label}
                  {note.trim() && ` · ${note.trim()}`}
                </p>
              )}
              {!isLoggedIn() && (
                <p className="error-text">
                  You must be logged in. <Link to="/login">Log in</Link> first.
                </p>
              )}
            </div>
          </div>
          <div className="row">
            <button
              className="btn btn-primary"
              onClick={book}
              disabled={!isLoggedIn() || booking}
            >
              {booking ? 'Confirming…' : 'Confirm Reservation'}
            </button>
            <button className="btn btn-outline" onClick={() => setSelected(null)} disabled={booking}>
              Cancel
            </button>
          </div>
        </div>
      )}
    </div>
  )
}