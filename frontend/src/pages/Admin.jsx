import { useState, useEffect } from 'react'
import api from '../api.js'
import { ZONES, occasionOf, tablesLabel } from '../constants.js'

const STATUSES = ['AVAILABLE', 'RESERVED', 'MAINTENANCE']
const RES_BADGE = {
  PENDING: 'badge pending',
  CONFIRMED: 'badge confirmed',
  CANCELLED: 'badge cancelled',
  COMPLETED: 'badge completed'
}

const today = () => new Date().toISOString().split('T')[0]

export default function Admin() {
  const [tab, setTab] = useState('tables')
  const [message, setMessage] = useState({ type: '', text: '' })

  const show = (type, text) => {
    setMessage({ type, text })
    setTimeout(() => setMessage({ type: '', text: '' }), 5000)
  }

  return (
    <div className="page">
      <h1 className="page-title">Admin Dashboard</h1>
      {message.text && <div className={`alert ${message.type}`}>{message.text}</div>}
      <div className="tabs">
        <button className={`tab ${tab === 'tables' ? 'active' : ''}`} onClick={() => setTab('tables')}>
          Manage Tables
        </button>
        <button className={`tab ${tab === 'reservations' ? 'active' : ''}`} onClick={() => setTab('reservations')}>
          Reservations
        </button>
        <button className={`tab ${tab === 'prep' ? 'active' : ''}`} onClick={() => setTab('prep')}>
          Prep Board
        </button>
      </div>
      {tab === 'tables' && <TablesTab notify={show} />}
      {tab === 'reservations' && <ReservationsTab notify={show} />}
      {tab === 'prep' && <PrepBoardTab notify={show} />}
    </div>
  )
}

// ---------------- Manage Tables ----------------

function TablesTab({ notify }) {
  const [tables, setTables] = useState([])
  const [loading, setLoading] = useState(true)
  const [editingId, setEditingId] = useState(null)
  const [form, setForm] = useState({ tableNumber: '', capacity: 2, status: 'AVAILABLE', zone: 'MAIN' })
  const [saving, setSaving] = useState(false)

  const load = async () => {
    setLoading(true)
    try {
      setTables(await api.adminTables())
    } catch (e) {
      notify('error', e.message)
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    load()
  }, [])

  const startAdd = () => {
    setEditingId(null)
    setForm({ tableNumber: '', capacity: 2, status: 'AVAILABLE', zone: 'MAIN' })
  }

  const startEdit = (t) => {
    setEditingId(t.tableId)
    setForm({
      tableNumber: t.tableNumber,
      capacity: t.capacity,
      status: t.status,
      zone: t.zone || 'MAIN'
    })
  }

  const save = async (e) => {
    e.preventDefault()
    setSaving(true)
    try {
      if (editingId) {
        await api.adminUpdateTable(editingId, form)
        notify('success', `Table ${form.tableNumber} updated.`)
      } else {
        await api.adminAddTable(form)
        notify('success', `Table ${form.tableNumber} added.`)
      }
      setEditingId(null)
      await load()
    } catch (err) {
      notify('error', err.message)
    } finally {
      setSaving(false)
    }
  }

  const remove = async (t) => {
    if (!confirm(`Delete table ${t.tableNumber}?`)) return
    try {
      await api.adminDeleteTable(t.tableId)
      notify('success', `Table ${t.tableNumber} deleted.`)
      await load()
    } catch (err) {
      notify('error', err.message)
    }
  }

  if (loading) return <p className="muted">Loading tables…</p>

  return (
    <section>
      {(editingId !== null || !tables.length) ? (
        <form className="card search-form" onSubmit={save}>
          <h3>{editingId ? `Edit Table ${form.tableNumber}` : 'Add new table'}</h3>
          <label>
            Table number
            <input value={form.tableNumber} onChange={(e) => setForm({ ...form, tableNumber: e.target.value })} required />
          </label>
          <label>
            Capacity
            <input type="number" min={1} value={form.capacity} onChange={(e) => setForm({ ...form, capacity: e.target.value })} required />
          </label>
          <label>
            Status
            <select value={form.status} onChange={(e) => setForm({ ...form, status: e.target.value })}>
              {STATUSES.map((s) => (
                <option key={s} value={s}>{s}</option>
              ))}
            </select>
          </label>
          <label>
            Zone (tables of one zone can be joined)
            <select value={form.zone} onChange={(e) => setForm({ ...form, zone: e.target.value })}>
              {ZONES.map((z) => (
                <option key={z} value={z}>{z}</option>
              ))}
            </select>
          </label>
          <div className="row">
            <button className="btn btn-primary" disabled={saving}>
              {saving ? 'Saving…' : 'Save'}
            </button>
            {editingId !== null && (
              <button className="btn btn-outline" onClick={() => { setEditingId(null); startAdd(); }} type="button">
                Cancel
              </button>
            )}
          </div>
        </form>
      ) : null}

      <div className="row spread" style={{ margin: '16px 0' }}>
        <h2 className="section-title">All tables ({tables.length})</h2>
        <button className="btn btn-primary btn-sm" onClick={startAdd}>+ Add table</button>
      </div>

      <div className="table-grid">
        {tables.map((t) => (
          <div className="table-card" key={t.tableId}>
            <div className="table-card-head">
              <span className="table-icon">🪑</span>
              <b>Table {t.tableNumber}</b>
              <span className="zone-tag">{t.zone || 'MAIN'}</span>
            </div>
            <p className="muted">Capacity: {t.capacity} seats</p>
            <p>
              Status: <span className={`badge ${t.status === 'AVAILABLE' ? 'confirmed' : 'pending'}`}>{t.status}</span>
            </p>
            <div className="row">
              <button className="btn btn-outline btn-sm" onClick={() => startEdit(t)}>Edit</button>
              <button className="btn btn-danger btn-sm" onClick={() => remove(t)}>Delete</button>
            </div>
          </div>
        ))}
        {!tables.length && <p className="muted">No tables yet. Add one above.</p>}
      </div>
    </section>
  )
}

// ---------------- Reservations ----------------

function ReservationsTab({ notify }) {
  const [reservations, setReservations] = useState([])
  const [filterDate, setFilterDate] = useState('')
  const [loading, setLoading] = useState(true)
  const [busyId, setBusyId] = useState(null)

  const load = async (date) => {
    setLoading(true)
    try {
      setReservations(date ? await api.adminReservationsByDate(date) : await api.adminReservations())
    } catch (e) {
      notify('error', e.message)
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    load()
  }, [])

  const setStatus = async (id, status) => {
    setBusyId(id)
    try {
      await api.adminUpdateReservation(id, status)
      notify('success', `Reservation #${id} marked as ${status}.`)
      await load(filterDate || undefined)
    } catch (e) {
      notify('error', e.message)
    } finally {
      setBusyId(null)
    }
  }

  const filter = (e) => {
    e.preventDefault()
    load(filterDate || undefined)
  }

  if (loading) return <p className="muted">Loading reservations…</p>

  return (
    <section>
      <form className="card search-form" onSubmit={filter} style={{ marginBottom: 16 }}>
        <label>
          Filter by date
          <input
            type="date"
            value={filterDate}
            onChange={(e) => setFilterDate(e.target.value)}
            max={today()}
          />
        </label>
        <button className="btn btn-outline">Filter</button>
        {filterDate && (
          <button
            className="btn btn-outline"
            type="button"
            onClick={() => {
              setFilterDate('')
              load()
            }}
          >
            Clear
          </button>
        )}
      </form>

      <h2 className="section-title">All reservations ({reservations.length})</h2>

      {!reservations.length ? (
        <div className="card center-card">
          <p className="muted">No reservations found.</p>
        </div>
      ) : (
        <div className="res-list">
          {reservations.map((r) => (
            <div className="card res-item" key={r.reservationId}>
              <div className="res-head">
                <div>
                  <b>#{r.reservationId}</b> · {r.combo ? 'Combo' : 'Table'}
                  <b> {r.tableNumbers?.join(' + ') || r.tableNumber}</b>
                  {r.combo ? ` (total ${r.totalSeats} seats)` : ` (${r.capacity} seats)`}
                </div>
                <span className={RES_BADGE[r.status] || 'badge'}>{r.status}</span>
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
              <p className="muted">
                {r.customerName} · {r.customerEmail}
                {r.customerPhone ? ` · ${r.customerPhone}` : ''}
              </p>
              <div className="row">
                {r.status === 'PENDING' && (
                  <button
                    className="btn btn-primary btn-sm"
                    disabled={busyId === r.reservationId}
                    onClick={() => setStatus(r.reservationId, 'CONFIRMED')}
                  >
                    Confirm
                  </button>
                )}
                {r.status === 'CONFIRMED' && (
                  <button
                    className="btn btn-success btn-sm"
                    disabled={busyId === r.reservationId}
                    onClick={() => setStatus(r.reservationId, 'COMPLETED')}
                  >
                    Mark completed
                  </button>
                )}
                {(r.status === 'PENDING' || r.status === 'CONFIRMED') && (
                  <button
                    className="btn btn-danger btn-sm"
                    disabled={busyId === r.reservationId}
                    onClick={() => setStatus(r.reservationId, 'CANCELLED')}
                  >
                    Cancel
                  </button>
                )}
              </div>
            </div>
          ))}
        </div>
      )}
    </section>
  )
}

// ---------------- Prep board ----------------

function PrepBoardTab({ notify }) {
  const [date, setDate] = useState(today())
  const [board, setBoard] = useState(null)
  const [loading, setLoading] = useState(true)

  const load = async () => {
    setLoading(true)
    try {
      setBoard(await api.adminPrepBoard(date))
    } catch (e) {
      notify('error', e.message)
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    load()
  }, [date])

  const refresh = (e) => {
    e.preventDefault()
    load()
  }

  if (loading) return <p className="muted">Loading prep board…</p>

  return (
    <section>
      <form className="card search-form" onSubmit={refresh}>
        <label>
          Date
          <input type="date" value={date} onChange={(e) => setDate(e.target.value)} />
        </label>
        <button className="btn btn-outline">Refresh</button>
      </form>

      {board && (
        <div className="card" style={{ marginTop: 16 }}>
          <h3>{board.date} · Prep board</h3>
          <p className="muted">
            {board.taskCount} tasks · {board.occasionCount} occasions · {board.comboCount} combos ·
            dining window {board.diningMinutes} min
          </p>
        </div>
      )}

      {board?.tasks?.length ? (
        <div className="res-list" style={{ marginTop: 16 }}>
          {board.tasks.map((t) => (
            <div
              className={`card prep-item ${t.occasion !== 'NONE' ? 'has-occasion' : ''}`}
              key={t.reservationId}
            >
              <div className="res-head">
                <div>
                  <b>#{t.reservationId}</b> · {t.combo ? 'Combo' : 'Table'} {t.tableNumbers?.join(' + ')}
                  {t.combo ? ` (total ${t.totalSeats} seats)` : ''}
                </div>
                <span className={t.occasion !== 'NONE' ? 'occasion-chip' : 'badge confirmed'}>
                  {occasionOf(t.occasion).icon} {t.occasionLabel}
                </span>
              </div>
              <p>
                <span className="prep-time">{t.prepReadyTime}</span> ready for {t.time} ·{' '}
                {t.numberOfPeople} people
              </p>
              {t.occasionNote && <p className="prep-note">Note: {t.occasionNote}</p>}
              <p className="muted">
                {t.customerName}
                {t.customerPhone ? ` · ${t.customerPhone}` : ''}
                {` · ${tablesLabel({ tableNumbers: t.tableNumbers })}`}
              </p>
            </div>
          ))}
        </div>
      ) : (
        <div className="card center-card" style={{ marginTop: 16 }}>
          <p className="muted">No upcoming bookings for this date.</p>
        </div>
      )}
    </section>
  )
}