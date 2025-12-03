import client from './client'

export const login = (email, password) =>
    client.post('/auth/login', { email, password })

export const signup = (email, password, name) =>
    client.post('/auth/signup', { email, password, name })