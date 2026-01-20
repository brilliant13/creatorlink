import client from './client'

export const getCampaignStats = (advertiserId) =>
    client.get('/stats/campaigns', { params: { advertiserId } })

export const getCreatorStats = (advertiserId) =>
    client.get('/stats/creators', { params: { advertiserId } })

export const getTodayStats = (advertiserId) =>
    client.get('/stats/today', { params: { advertiserId } })

// 캠페인 상세 - KPI
export const getCampaignKpi = (campaignId, advertiserId, from, to) =>
    client.get(`/stats/campaigns/${campaignId}/kpi`, {
        params: { advertiserId, from, to }
    })

// 캠페인 상세 - 조합별 성과
export const getCombinationStats = (campaignId, advertiserId, from, to) =>
    client.get(`/stats/campaigns/${campaignId}/combinations`, {
        params: { advertiserId, from, to }
    })

// 캠페인 상세 - 채널 랭킹
export const getChannelRanking = (campaignId, advertiserId, from, to, limit = 10) =>
    client.get(`/stats/campaigns/${campaignId}/channels/ranking`, {
        params: { advertiserId, from, to, limit }
    })