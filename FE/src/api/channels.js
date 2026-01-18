import client from './client'

export const getChannels = (advertiserId) =>
    client.get('/channels', { params: { advertiserId } })

export const getChannel = (id, advertiserId) =>
    client.get(`/channels/${id}`, { params: { advertiserId } })

export const createChannel = (data) =>
    client.post('/channels', data)

export const updateChannel = (id, data) =>
    client.patch(`/channels/${id}`, data)

export const deleteChannel = (id, advertiserId) =>
    client.delete(`/channels/${id}`, { params: { advertiserId } })