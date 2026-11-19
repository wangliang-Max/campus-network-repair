import request from '@/utils/request'

export const getOverview = () => request.get('/stats/overview')
export const getTrend = (days = 7) => request.get('/stats/trend', { params: { days } })
