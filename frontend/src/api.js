const BASE = '/api'

function getToken() {
  return localStorage.getItem('tt1_token') || ''
}

function setAuth(token, role, user) {
  localStorage.setItem('tt1_token', token)
  localStorage.setItem('tt1_role', role)
  if (user) localStorage.setItem('tt1_user', JSON.stringify(user))
}

function clearAuth() {
  localStorage.removeItem('tt1_token')
  localStorage.removeItem('tt1_role')
  localStorage.removeItem('tt1_user')
}

export { clearAuth }

export function isLoggedIn() {
  return !!localStorage.getItem('tt1_token')
}

export function getRole() {
  return localStorage.getItem('tt1_role') || ''
}

export function getUser() {
  try {
    return JSON.parse(localStorage.getItem('tt1_user') || 'null')
  } catch {
    return null
  }
}

async function request(path, { method = 'GET', body, auth = true } = {}) {
  const headers = { 'Content-Type': 'application/json' }
  if (auth) {
    const token = getToken()
    if (token) headers.Authorization = `Bearer ${token}`
  }
  const res = await fetch(`${BASE}${path}`, {
    method,
    headers,
    body: body ? JSON.stringify(body) : undefined
  })
  const data = await res.json().catch(() => ({}))
  if (!res.ok) {
    if (res.status === 401) clearAuth()
    throw new Error(data.message || `Request failed (${res.status})`)
  }
  return data
}

export const api = {
  register: (body) => request('/auth/register', { method: 'POST', body, auth: false }),
  login: (body) => request('/auth/login', { method: 'POST', body, auth: false }),
  adminLogin: (body) => request('/admin/auth/login', { method: 'POST', body, auth: false }),
  logout: () => request('/auth/logout', { method: 'POST' }),

  // Combined re-login that also stores the returned token
  loginAndStore: async (body) => {
    const data = await api.login(body)
    setAuth(data.token, data.role, data.user)
    return data
  },
  registerAndStore: async (body) => {
    const data = await api.register(body)
    setAuth(data.token, data.role, data.user)
    return data
  },
  adminLoginAndStore: async (body) => {
    const data = await api.adminLogin(body)
    setAuth(data.token, data.role)
    return data
  },

  availableTables: (params) => {
    const q = new URLSearchParams(params).toString()
    return request(`/tables/available?${q}`, { auth: false })
  },
  availableCombos: (params) => {
    const q = new URLSearchParams(params).toString()
    return request(`/tables/combos?${q}`, { auth: false })
  },

  createReservation: (body) => request('/reservations', { method: 'POST', body }),
  myReservations: () => request('/reservations/mine'),
  cancelReservation: (id) => request(`/reservations/mine/${id}`, { method: 'DELETE' }),

  adminTables: () => request('/tables', { auth: false }),
  adminAddTable: (body) => request('/admin/tables', { method: 'POST', body }),
  adminUpdateTable: (id, body) => request(`/admin/tables/${id}`, { method: 'PUT', body }),
  adminDeleteTable: (id) => request(`/admin/tables/${id}`, { method: 'DELETE' }),
  adminReservations: () => request('/admin/reservations'),
  adminReservationsByDate: (date) => request(`/admin/reservations/date?date=${date}`),
  adminPrepBoard: (date) => request(`/admin/prep-board${date ? `?date=${date}` : ''}`),
  adminUpdateReservation: (id, status) =>
    request(`/admin/reservations/${id}/status`, { method: 'PUT', body: { status } })
}

export default api