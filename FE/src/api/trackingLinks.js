import client from './client'

export const getTrackingLinks = (campaignId) =>
    client.get('/tracking-links', { params: { campaignId } })

export const createTrackingLink = (data) =>
    client.post('/tracking-links', data)