import { createContext, useContext, useState } from 'react'

const AuthContext = createContext(null)

export function AuthProvider({ children }) {
    const [user, setUser] = useState(() => {
        const saved = sessionStorage.getItem('user')
        return saved ? JSON.parse(saved) : null
    })

    const saveUser = (userData) => {
        setUser(userData)
        sessionStorage.setItem('user', JSON.stringify(userData))
    }

    const logout = () => {
        setUser(null)
        sessionStorage.removeItem('user')
    }

    return (
        <AuthContext.Provider value={{ user, saveUser, logout }}>
            {children}
        </AuthContext.Provider>
    )
}

export function useAuth() {
    const context = useContext(AuthContext)
    if (!context) {
        throw new Error('useAuth must be used within AuthProvider')
    }
    return context
}