import { NavLink, Outlet } from 'react-router-dom'
import { useAuth } from '../contexts/AuthContext'

export default function Layout() {
    const { user, logout } = useAuth()

    const linkClass = ({ isActive }) =>
        `block px-4 py-2 rounded ${isActive ? 'bg-blue-600 text-white' : 'text-gray-700 hover:bg-gray-100'}`

    return (
        <div className="flex h-screen bg-gray-50">
            <aside className="w-64 bg-white border-r border-gray-200 flex flex-col">
                <div className="p-4 border-b border-gray-200">
                    <h1 className="text-xl font-bold text-blue-600">CreatorLink</h1>
                </div>
                <nav className="flex-1 p-4 space-y-1">
                    <NavLink to="/dashboard" className={linkClass}>대시보드</NavLink>
                    <NavLink to="/campaigns" className={linkClass}>캠페인</NavLink>
                    <NavLink to="/creators" className={linkClass}>크리에이터</NavLink>
                    <NavLink to="/channels" className={linkClass}>채널</NavLink>
                </nav>
                <div className="p-4 border-t border-gray-200">
                    <p className="text-sm text-gray-600 mb-2">{user?.name}</p>
                    <button
                        onClick={logout}
                        className="text-sm text-red-600 hover:underline"
                    >
                        로그아웃
                    </button>
                </div>
            </aside>
            <main className="flex-1 overflow-auto p-6">
                <Outlet />
            </main>
        </div>
    )
}