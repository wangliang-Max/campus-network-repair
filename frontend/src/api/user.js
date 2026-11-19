import request from '@/utils/request'

export const pageUsers = (params) => request.get('/users', { params })
export const getTechnicians = () => request.get('/users/technicians')
export const setUserEnabled = (id, enabled) => request.put(`/users/${id}/enabled`, { enabled })
