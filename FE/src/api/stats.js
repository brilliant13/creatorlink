import client from './client'

export const getCampaignStats = (advertiserId) =>
    client.get('/stats/campaigns', { params: { advertiserId } })

export const getCreatorStats = (advertiserId) =>
    client.get('/stats/creators', { params: { advertiserId } })