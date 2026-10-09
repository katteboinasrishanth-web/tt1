export const OCCASIONS = [
  { value: 'NONE', label: 'Just dining', buffer: 0, icon: '🍽️' },
  { value: 'BIRTHDAY', label: 'Birthday / celebration', buffer: 15, icon: '🎂' },
  { value: 'ANNIVERSARY', label: 'Anniversary', buffer: 15, icon: '💐' },
  { value: 'DATE_NIGHT', label: 'Date night', buffer: 10, icon: '🕯️' },
  { value: 'BUSINESS', label: 'Business meal', buffer: 5, icon: '💼' },
  { value: 'FAMILY', label: 'Family get-together', buffer: 5, icon: '🍰' },
  { value: 'OTHER', label: 'Something else', buffer: 10, icon: '✨' }
]

export const ZONES = ['WINDOW', 'MAIN', 'PRIVATE', 'PATIO', 'TERRACE', 'BAR']

export function occasionOf(value) {
  return OCCASIONS.find((o) => o.value === value) || OCCASIONS[0]
}

/** "Table 5" for a single table, "Tables 5 + 8" for a combo booking. */
export function tablesLabel(reservation) {
  const numbers = reservation.tableNumbers
  if (Array.isArray(numbers) && numbers.length > 1) {
    return `Tables ${numbers.join(' + ')}`
  }
  const single = numbers && numbers.length ? numbers[0] : reservation.tableNumber
  return `Table ${single}`
}