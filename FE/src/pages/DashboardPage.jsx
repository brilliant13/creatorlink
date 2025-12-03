import { useState, useEffect, useCallback } from 'react'
import { useAuth } from '../contexts/AuthContext'
import { getCampaignStats, getCreatorStats } from '../api/stats'

export default function DashboardPage() {
    const { user } = useAuth()
    const [campaignStats, setCampaignStats] = useState([])
    const [creatorStats, setCreatorStats] = useState([])
    const [loading, setLoading] = useState(true)
    const [lastUpdated, setLastUpdated] = useState(null)

    const fetchStats = useCallback(async () => {
        try {
            const [campaignRes, creatorRes] = await Promise.all([
                getCampaignStats(user.id),
                getCreatorStats(user.id),
            ])
            setCampaignStats(campaignRes.data)
            setCreatorStats(creatorRes.data)
            setLastUpdated(new Date())
        } catch (err) {
            console.error('Failed to fetch stats:', err)
        } finally {
            setLoading(false)
        }
    }, [user.id])

    useEffect(() => {
        fetchStats()

        // 30초마다 자동 갱신
        const interval = setInterval(fetchStats, 300000)
        return () => clearInterval(interval)
    }, [fetchStats])

    if (loading) {
        return <div className="text-gray-500">로딩 중...</div>
    }

    const totalClicks = campaignStats.reduce((sum, c) => sum + c.totalClicks, 0)

    return (
        <div className="space-y-6">
            <div className="flex justify-between items-center">
                <h1 className="text-2xl font-bold">대시보드</h1>
                <div className="flex items-center gap-3">
                    {lastUpdated && (
                        <span className="text-sm text-gray-500">
              {lastUpdated.toLocaleTimeString()} 업데이트
            </span>
                    )}
                    <button
                        onClick={fetchStats}
                        className="px-3 py-1 text-sm bg-gray-100 text-gray-700 rounded hover:bg-gray-200"
                    >
                        새로고침
                    </button>
                </div>
            </div>

            <div className="grid grid-cols-3 gap-4">
                <div className="bg-white p-6 rounded-lg shadow">
                    <p className="text-sm text-gray-500">총 클릭 수</p>
                    <p className="text-3xl font-bold text-blue-600">{totalClicks.toLocaleString()}</p>
                </div>
                <div className="bg-white p-6 rounded-lg shadow">
                    <p className="text-sm text-gray-500">캠페인 수</p>
                    <p className="text-3xl font-bold">{campaignStats.length}</p>
                </div>
                <div className="bg-white p-6 rounded-lg shadow">
                    <p className="text-sm text-gray-500">크리에이터 수</p>
                    <p className="text-3xl font-bold">{creatorStats.length}</p>
                </div>
            </div>

            <div className="grid grid-cols-2 gap-6">
                <div className="bg-white p-6 rounded-lg shadow">
                    <h2 className="text-lg font-semibold mb-4">캠페인별 클릭</h2>
                    {campaignStats.length === 0 ? (
                        <p className="text-gray-500 text-sm">데이터가 없습니다.</p>
                    ) : (
                        <ul className="space-y-2">
                            {campaignStats.map((c) => (
                                <li key={c.campaignId} className="flex justify-between">
                                    <span>{c.campaignName}</span>
                                    <span className="font-medium">{c.totalClicks.toLocaleString()}</span>
                                </li>
                            ))}
                        </ul>
                    )}
                </div>
                <div className="bg-white p-6 rounded-lg shadow">
                    <h2 className="text-lg font-semibold mb-4">크리에이터별 클릭</h2>
                    {creatorStats.length === 0 ? (
                        <p className="text-gray-500 text-sm">데이터가 없습니다.</p>
                    ) : (
                        <ul className="space-y-2">
                            {creatorStats.map((c) => (
                                <li key={c.creatorId} className="flex justify-between">
                                    <span>{c.creatorName}</span>
                                    <span className="font-medium">{c.totalClicks.toLocaleString()}</span>
                                </li>
                            ))}
                        </ul>
                    )}
                </div>
            </div>
        </div>
    )
}