import request from '@/utils/request'

export const pageOrders = (params) => request.get('/repair-orders', { params })
export const myOrders = () => request.get('/repair-orders/my')
export const getOrder = (id) => request.get(`/repair-orders/${id}`)
export const createOrder = (data) => request.post('/repair-orders', data)
export const acceptOrder = (id, data) => request.post(`/repair-orders/${id}/accept`, data)
export const startRepair = (id, data) => request.post(`/repair-orders/${id}/start-repair`, data)
export const finishOrder = (id, data) => request.post(`/repair-orders/${id}/finish`, data)
export const returnInfo = (id, data) => request.post(`/repair-orders/${id}/return-info`, data)
export const supplementOrder = (id, data) => request.post(`/repair-orders/${id}/supplement`, data)
export const confirmOrder = (id, data) => request.post(`/repair-orders/${id}/confirm`, data)
