import { useState, useEffect, useCallback } from 'react'
import { useAuth } from '../contexts/AuthContext'
import { getCampaignStats, getCreatorStats } from '../api/stats'
import { getCampaigns } from '../api/campaigns'
import { getCreators } from '../api/creators'
import { BarChart, Bar, XAxis, YAxis, Tooltip, ResponsiveContainer, Cell } from 'recharts'

export default function DashboardPage() {
    const { user } = useAuth()
    const [campaignStats, setCampaignStats] = useState([])
    const [creatorStats, setCreatorStats] = useState([])
    const [campaigns, setCampaigns] = useState([])
    const [creatorCount, setCreatorCount] = useState(0)
    const [loading, setLoading] = useState(true)
    const [lastUpdated, setLastUpdated] = useState(null)

    const fetchStats = useCallback(async () => {
        try {
            const [campaignStatsRes, creatorStatsRes, campaignsRes, creatorsRes] = await Promise.all([
                getCampaignStats(user.id),
                getCreatorStats(user.id),
                getCampaigns(user.id),
                getCreators(user.id),
            ])
            setCampaignStats(campaignStatsRes.data)
            setCreatorStats(creatorStatsRes.data)
            setCampaigns(campaignsRes.data)
            setCreatorCount(creatorsRes.data.length)
            setLastUpdated(new Date())
        } catch (err) {
            console.error('Failed to fetch stats:', err)
        } finally {
            setLoading(false)
        }
    }, [user.id])

    useEffect(() => {
        fetchStats()
        const interval = setInterval(fetchStats, 30000)
        return () => clearInterval(interval)
    }, [fetchStats])

    if (loading) {
        return <div className="text-gray-500">로딩 중...</div>
    }

    const totalClicks = campaignStats.reduce((sum, c) => sum + c.totalClicks, 0)
    const activeCampaigns = campaigns.filter(c => c.state === 'RUNNING' || c.state === 'UPCOMING')
    const endedCampaigns = campaigns.filter(c => c.state === 'ENDED')

    // 캠페인 차트 데이터 (상태 정보 포함)
    const campaignChartData = campaignStats.map(stat => {
        const campaign = campaigns.find(c => c.id === stat.campaignId)
        return {
            name: stat.campaignName.length > 10 ? stat.campaignName.slice(0, 10) + '...' : stat.campaignName,
            fullName: stat.campaignName,
            clicks: stat.totalClicks,
            state: campaign?.state || 'UNKNOWN',
        }
    })

    // 크리에이터 차트 데이터
    const creatorChartData = creatorStats.map((stat, index) => ({
        name: stat.creatorName.length > 8 ? stat.creatorName.slice(0, 8) + '...' : stat.creatorName,
        fullName: stat.creatorName,
        clicks: stat.totalClicks,
        isTop: index === 0 && stat.totalClicks > 0,
    }))

    const getStateColor = (state) => {
        switch (state) {
            case 'RUNNING': return '#22c55e'
            case 'UPCOMING': return '#eab308'
            case 'ENDED': return '#9ca3af'
            default: return '#3b82f6'
        }
    }

    const getStateLabel = (state) => {
        switch (state) {
            case 'UPCOMING': return { text: '시작 예정', color: 'bg-yellow-100 text-yellow-800' }
            case 'RUNNING': return { text: '진행 중', color: 'bg-green-100 text-green-800' }
            case 'ENDED': return { text: '종료', color: 'bg-gray-100 text-gray-800' }
            default: return { text: state, color: 'bg-gray-100 text-gray-800' }
        }
    }

    const CustomTooltip = ({ active, payload }) => {
        if (active && payload && payload.length) {
            const data = payload[0].payload
            return (
                <div className="bg-white p-2 border border-gray-200 rounded shadow text-sm">
                    <p className="font-medium">{data.fullName}</p>
                    <p className="text-blue-600">{data.clicks.toLocaleString()} 클릭</p>
                </div>
            )
        }
        return null
    }

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

            {/* 요약 카드 */}
            <div className="grid grid-cols-4 gap-4">
                <div className="bg-white p-6 rounded-lg shadow">
                    <p className="text-sm text-gray-500">총 클릭 수</p>
                    <p className="text-3xl font-bold text-blue-600">{totalClicks.toLocaleString()}</p>
                </div>
                <div className="bg-white p-6 rounded-lg shadow">
                    <p className="text-sm text-gray-500">진행 중 캠페인</p>
                    <p className="text-3xl font-bold text-green-600">{activeCampaigns.length}</p>
                    {endedCampaigns.length > 0 && (
                        <p className="text-xs text-gray-400 mt-1">종료: {endedCampaigns.length}개</p>
                    )}
                </div>
                <div className="bg-white p-6 rounded-lg shadow">
                    <p className="text-sm text-gray-500">크리에이터 수</p>
                    <p className="text-3xl font-bold">{creatorCount}</p>
                </div>
                <div className="bg-white p-6 rounded-lg shadow">
                    <p className="text-sm text-gray-500">Top 크리에이터</p>
                    {creatorStats.length > 0 && creatorStats[0].totalClicks > 0 ? (
                        <>
                            <p className="text-xl font-bold text-purple-600 truncate">{creatorStats[0].creatorName}</p>
                            <p className="text-xs text-gray-400 mt-1">{creatorStats[0].totalClicks.toLocaleString()} 클릭</p>
                        </>
                    ) : (
                        <p className="text-gray-400 text-sm">데이터 없음</p>
                    )}
                </div>
            </div>

            {/* 차트 섹션 */}
            <div className="grid grid-cols-2 gap-6">
                <div className="bg-white p-6 rounded-lg shadow">
                    <div className="flex justify-between items-center mb-4">
                        <h2 className="text-lg font-semibold">캠페인별 클릭</h2>
                        <div className="flex gap-2 text-xs">
              <span className="flex items-center gap-1">
                <span className="w-2 h-2 rounded-full bg-green-500"></span> 진행 중
              </span>
                            <span className="flex items-center gap-1">
                <span className="w-2 h-2 rounded-full bg-yellow-500"></span> 예정
              </span>
                            <span className="flex items-center gap-1">
                <span className="w-2 h-2 rounded-full bg-gray-400"></span> 종료
              </span>
                        </div>
                    </div>
                    {campaignChartData.length === 0 ? (
                        <p className="text-gray-500 text-sm">데이터가 없습니다.</p>
                    ) : (
                        <ResponsiveContainer width="100%" height={250}>
                            <BarChart data={campaignChartData} layout="vertical" margin={{ left: 20, right: 20 }}>
                                <XAxis type="number" />
                                <YAxis type="category" dataKey="name" width={80} tick={{ fontSize: 12 }} />
                                <Tooltip content={<CustomTooltip />} />
                                <Bar dataKey="clicks" radius={[0, 4, 4, 0]}>
                                    {campaignChartData.map((entry, index) => (
                                        <Cell key={`cell-${index}`} fill={getStateColor(entry.state)} />
                                    ))}
                                </Bar>
                            </BarChart>
                        </ResponsiveContainer>
                    )}
                </div>

                <div className="bg-white p-6 rounded-lg shadow">
                    <h2 className="text-lg font-semibold mb-4">크리에이터별 클릭</h2>
                    {creatorChartData.length === 0 ? (
                        <p className="text-gray-500 text-sm">데이터가 없습니다.</p>
                    ) : (
                        <ResponsiveContainer width="100%" height={250}>
                            <BarChart data={creatorChartData} layout="vertical" margin={{ left: 20, right: 20 }}>
                                <XAxis type="number" />
                                <YAxis type="category" dataKey="name" width={80} tick={{ fontSize: 12 }} />
                                <Tooltip content={<CustomTooltip />} />
                                <Bar dataKey="clicks" radius={[0, 4, 4, 0]}>
                                    {creatorChartData.map((entry, index) => (
                                        <Cell key={`cell-${index}`} fill={entry.isTop ? '#8b5cf6' : '#3b82f6'} />
                                    ))}
                                </Bar>
                            </BarChart>
                        </ResponsiveContainer>
                    )}
                </div>
            </div>

            {/* 캠페인 상세 리스트 */}
            <div className="grid grid-cols-2 gap-6">
                <div className="bg-white p-6 rounded-lg shadow">
                    <h2 className="text-lg font-semibold mb-4">캠페인 상세</h2>
                    {campaignStats.length === 0 ? (
                        <p className="text-gray-500 text-sm">데이터가 없습니다.</p>
                    ) : (
                        <ul className="space-y-3">
                            {campaignStats.map((stat) => {
                                const campaign = campaigns.find(c => c.id === stat.campaignId)
                                const stateLabel = getStateLabel(campaign?.state)
                                return (
                                    <li key={stat.campaignId} className="flex justify-between items-center">
                                        <div className="flex items-center gap-2">
                      <span className={`px-2 py-0.5 text-xs rounded-full ${stateLabel.color}`}>
                        {stateLabel.text}
                      </span>
                                            <span className="truncate max-w-[150px]">{stat.campaignName}</span>
                                        </div>
                                        <span className="font-medium">{stat.totalClicks.toLocaleString()}</span>
                                    </li>
                                )
                            })}
                        </ul>
                    )}
                </div>

                <div className="bg-white p-6 rounded-lg shadow">
                    <h2 className="text-lg font-semibold mb-4">크리에이터 상세</h2>
                    {creatorStats.length === 0 ? (
                        <p className="text-gray-500 text-sm">데이터가 없습니다.</p>
                    ) : (
                        <ul className="space-y-3">
                            {creatorStats.map((stat, index) => (
                                <li key={stat.creatorId} className="flex justify-between items-center">
                                    <div className="flex items-center gap-2">
                                        {index === 0 && stat.totalClicks > 0 && (
                                            <span className="px-2 py-0.5 text-xs rounded-full bg-purple-100 text-purple-800">
                        TOP
                      </span>
                                        )}
                                        <span className={index === 0 && stat.totalClicks > 0 ? 'font-medium' : ''}>
                      {stat.creatorName}
                    </span>
                                    </div>
                                    <span className="font-medium">{stat.totalClicks.toLocaleString()}</span>
                                </li>
                            ))}
                        </ul>
                    )}
                </div>
            </div>
        </div>
    )
}