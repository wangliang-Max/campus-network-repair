export function formatTime(value) {
  if (!value) return '--'
  return String(value).replace('T', ' ').slice(0, 19)
}

export function formatDate(value) {
  if (!value) return '--'
  return String(value).slice(0, 10)
}
