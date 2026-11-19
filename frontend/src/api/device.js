import request from '@/utils/request'

export const pageDevices = (params) => request.get('/devices', { params })
export const getRooms = () => request.get('/devices/rooms')
export const getDevice = (id) => request.get(`/devices/${id}`)
export const createDevice = (data) => request.post('/devices', data)
export const updateDevice = (id, data) => request.put(`/devices/${id}`, data)
export const updateDeviceStatus = (id, status) => request.put(`/devices/${id}/status`, { status })
export const deleteDevice = (id) => request.delete(`/devices/${id}`)
