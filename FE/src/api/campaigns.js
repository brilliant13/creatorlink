import client from './client'

export const getCampaigns = (advertiserId) =>
    client.get('/campaigns', { params: { advertiserId } })

export const getCampaign = (id, advertiserId) =>
    client.get(`/campaigns/${id}`, { params: { advertiserId } })

export const createCampaign = (data) =>
    client.post('/campaigns', data)

export const updateCampaign = (id, data) =>
    client.put(`/campaigns/${id}`, data)

export const deleteCampaign = (id, advertiserId) =>
    client.delete(`/campaigns/${id}`, { params: { advertiserId } })
