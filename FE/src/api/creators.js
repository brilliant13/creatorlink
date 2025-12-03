import client from './client'

export const getCreators = (advertiserId) =>
    client.get('/creators', { params: { advertiserId } })

export const getCreator = (id, advertiserId) =>
    client.get(`/creators/${id}`, { params: { advertiserId } })

export const createCreator = (data) =>
    client.post('/creators', data)