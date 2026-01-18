import { Routes, Route, Navigate } from 'react-router-dom'
import { AuthProvider } from './contexts/AuthContext'
import ProtectedRoute from './components/ProtectedRoute'
import Layout from './components/Layout'
import LoginPage from './pages/LoginPage'
import SignupPage from './pages/SignupPage'
import DashboardPage from './pages/DashboardPage'
import CampaignsPage from './pages/CampaignsPage'
import CampaignDetailPage from './pages/CampaignDetailPage'
import CreatorsPage from './pages/CreatorsPage'
import ChannelsPage from './pages/ChannelsPage'

function App() {
    return (
        <AuthProvider>
            <Routes>
                <Route path="/login" element={<LoginPage />} />
                <Route path="/signup" element={<SignupPage />} />
                <Route path="/" element={<ProtectedRoute><Layout /></ProtectedRoute>}>
                    <Route index element={<Navigate to="/dashboard" replace />} />
                    <Route path="dashboard" element={<DashboardPage />} />
                    <Route path="campaigns" element={<CampaignsPage />} />
                    <Route path="campaigns/:id" element={<CampaignDetailPage />} />
                    <Route path="creators" element={<CreatorsPage />} />
                    <Route path="channels" element={<ChannelsPage />} />
                </Route>
            </Routes>
        </AuthProvider>
    )
}

export default App