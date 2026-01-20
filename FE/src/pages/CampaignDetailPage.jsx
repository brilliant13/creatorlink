
import { useState, useEffect } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import { useAuth } from '../contexts/AuthContext'
import { getCampaign, updateCampaign, deleteCampaign } from '../api/campaigns'
import { getTrackingLinks, createTrackingLink, deleteTrackingLink } from '../api/trackingLinks'
import { getCreators } from '../api/creators'
import { getChannels } from '../api/channels'
import { getCampaignKpi, getCombinationStats, getChannelRanking } from '../api/stats'
import { BarChart, Bar, XAxis, YAxis, Tooltip, ResponsiveContainer, Cell } from 'recharts'

export default function CampaignDetailPage() {
    const { id } = useParams()
    const navigate = useNavigate()
    const { user } = useAuth()

    // 기본 데이터
    const [campaign, setCampaign] = useState(null)
    const [links, setLinks] = useState([])
    const [creators, setCreators] = useState([])
    const [channels, setChannels] = useState([])
    const [loading, setLoading] = useState(true)

    // 폼 상태
    const [selectedCreator, setSelectedCreator] = useState('')
    const [selectedChannel, setSelectedChannel] = useState('')
    const [submitting, setSubmitting] = useState(false)
    const [editing, setEditing] = useState(false)
    const [form, setForm] = useState({ name: '', description: '', landingUrl: '', startDate: '', endDate: '' })

    // 탭 상태
    const [activeTab, setActiveTab] = useState('links')

    // 탭2, 탭3 데이터
    const [kpi, setKpi] = useState(null)
    const [combinations, setCombinations] = useState([])
    const [channelRanking, setChannelRanking] = useState([])
    const [statsLoading, setStatsLoading] = useState(false)

    // 기간 필터
    const [dateRange, setDateRange] = useState('all') // 'all', '7days', 'custom'
    const [customFrom, setCustomFrom] = useState('')
    const [customTo, setCustomTo] = useState('')

    const fetchData = async () => {
        try {
            const [campaignRes, linksRes, creatorsRes, channelsRes] = await Promise.all([
                getCampaign(id, user.id),
                getTrackingLinks(id),
                getCreators(user.id),
                getChannels(user.id),
            ])
            setCampaign(campaignRes.data)
            setLinks(linksRes.data)
            setCreators(creatorsRes.data)
            setChannels(channelsRes.data)
            setForm({
                name: campaignRes.data.name,
                description: campaignRes.data.description || '',
                landingUrl: campaignRes.data.landingUrl,
                startDate: campaignRes.data.startDate || '',
                endDate: campaignRes.data.endDate || '',
            })
        } catch (err) {
            console.error('Failed to fetch data:', err)
        } finally {
            setLoading(false)
        }
    }

    const getDateParams = () => {
        const today = new Date()
        const formatDate = (d) => d.toISOString().split('T')[0]

        if (dateRange === '7days') {
            const from = new Date(today)
            from.setDate(from.getDate() - 6)
            return { from: formatDate(from), to: formatDate(today) }
        } else if (dateRange === 'custom' && customFrom && customTo) {
            return { from: customFrom, to: customTo }
        } else {
            // 'all' - 캠페인 전체 기간
            return {
                from: campaign?.startDate || '2020-01-01',
                to: campaign?.endDate || formatDate(today)
            }
        }
    }

    const fetchStatsData = async () => {
        if (!campaign) return
        setStatsLoading(true)
        const { from, to } = getDateParams()

        try {
            const [kpiRes, combinationsRes, rankingRes] = await Promise.all([
                getCampaignKpi(id, user.id, from, to),
                getCombinationStats(id, user.id, from, to),
                getChannelRanking(id, user.id, from, to, 10),
            ])
            setKpi(kpiRes.data)
            setCombinations(combinationsRes.data)
            setChannelRanking(rankingRes.data)
        } catch (err) {
            console.error('Failed to fetch stats:', err)
        } finally {
            setStatsLoading(false)
        }
    }

    useEffect(() => {
        fetchData()
    }, [id, user.id])

    useEffect(() => {
        if ((activeTab === 'performance' || activeTab === 'ranking') && campaign) {
            fetchStatsData()
        }
    }, [activeTab, campaign, dateRange, customFrom, customTo])

    const handleCreateLink = async () => {
        if (!selectedCreator || !selectedChannel) return
        setSubmitting(true)
        try {
            await createTrackingLink({
                campaignId: Number(id),
                creatorId: Number(selectedCreator),
                channelId: Number(selectedChannel),
                advertiserId: user.id,
            })
            setSelectedCreator('')
            setSelectedChannel('')
            fetchData()
        } catch (err) {
            console.error('Failed to create tracking link:', err)
        } finally {
            setSubmitting(false)
        }
    }

    const handleDeleteLink = async (linkId, creatorName) => {
        if (!confirm(`"${creatorName}" 크리에이터의 트래킹 링크를 삭제하시겠습니까?`)) return
        try {
            await deleteTrackingLink(linkId)
            fetchData()
        } catch (err) {
            console.error('Failed to delete tracking link:', err)
            alert('삭제에 실패했습니다.')
        }
    }

    const handleUpdate = async (e) => {
        e.preventDefault()
        setSubmitting(true)
        try {
            await updateCampaign(id, { ...form, advertiserId: user.id })
            setEditing(false)
            fetchData()
        } catch (err) {
            console.error('Failed to update campaign:', err)
        } finally {
            setSubmitting(false)
        }
    }

    const handleDelete = async () => {
        if (!confirm(`"${campaign.name}" 캠페인을 삭제하시겠습니까?`)) return
        try {
            await deleteCampaign(id, user.id)
            navigate('/campaigns')
        } catch (err) {
            console.error('Failed to delete campaign:', err)
            alert('삭제에 실패했습니다.')
        }
    }

    const copyToClipboard = (slug) => {
        navigator.clipboard.writeText(`${window.location.origin}/t/${slug}`)
    }

    const getStateLabel = (state) => {
        switch (state) {
            case 'UPCOMING': return { text: '시작 예정', color: 'bg-yellow-100 text-yellow-800' }
            case 'RUNNING': return { text: '진행 중', color: 'bg-green-100 text-green-800' }
            case 'ENDED': return { text: '종료', color: 'bg-gray-100 text-gray-800' }
            default: return { text: state, color: 'bg-gray-100 text-gray-800' }
        }
    }

    if (loading) {
        return <div className="text-gray-500">로딩 중...</div>
    }

    if (!campaign) {
        return <div className="text-gray-500">캠페인을 찾을 수 없습니다.</div>
    }

    const stateLabel = getStateLabel(campaign.state)
    const topCombination = combinations.length > 0 ? combinations[0] : null

    return (
        <div className="space-y-6">
            {/* 캠페인 헤더 */}
            <div className="flex justify-between items-start">
                <div>
                    <div className="flex items-center gap-3">
                        <h1 className="text-2xl font-bold">{campaign.name}</h1>
                        <span className={`px-2 py-1 text-xs rounded-full ${stateLabel.color}`}>
              {stateLabel.text}
            </span>
                    </div>
                    {campaign.description && <p className="text-gray-600 mt-1">{campaign.description}</p>}
                    <p className="text-sm text-gray-500 mt-1">
                        {campaign.startDate || '-'} ~ {campaign.endDate || '-'}
                    </p>
                </div>
                <div className="flex gap-2">
                    <button
                        onClick={() => setEditing(!editing)}
                        className="px-3 py-1 text-sm bg-gray-100 text-gray-700 rounded hover:bg-gray-200"
                    >
                        {editing ? '취소' : '수정'}
                    </button>
                    <button
                        onClick={handleDelete}
                        className="px-3 py-1 text-sm bg-red-100 text-red-700 rounded hover:bg-red-200"
                    >
                        삭제
                    </button>
                </div>
            </div>

            {/* 캠페인 수정 폼 */}
            {editing && (
                <form onSubmit={handleUpdate} className="bg-white p-6 rounded-lg shadow space-y-4">
                    <h2 className="text-lg font-semibold">캠페인 수정</h2>
                    <div>
                        <label className="block text-sm font-medium text-gray-700 mb-1">캠페인명 *</label>
                        <input
                            type="text"
                            value={form.name}
                            onChange={(e) => setForm({ ...form, name: e.target.value })}
                            required
                            className="w-full px-3 py-2 border border-gray-300 rounded focus:outline-none focus:ring-2 focus:ring-blue-500"
                        />
                    </div>
                    <div>
                        <label className="block text-sm font-medium text-gray-700 mb-1">설명</label>
                        <textarea
                            value={form.description}
                            onChange={(e) => setForm({ ...form, description: e.target.value })}
                            className="w-full px-3 py-2 border border-gray-300 rounded focus:outline-none focus:ring-2 focus:ring-blue-500"
                            rows={2}
                        />
                    </div>
                    <div>
                        <label className="block text-sm font-medium text-gray-700 mb-1">랜딩 URL *</label>
                        <input
                            type="url"
                            value={form.landingUrl}
                            onChange={(e) => setForm({ ...form, landingUrl: e.target.value })}
                            required
                            className="w-full px-3 py-2 border border-gray-300 rounded focus:outline-none focus:ring-2 focus:ring-blue-500"
                        />
                    </div>
                    <div className="grid grid-cols-2 gap-4">
                        <div>
                            <label className="block text-sm font-medium text-gray-700 mb-1">시작일</label>
                            <input
                                type="date"
                                value={form.startDate}
                                onChange={(e) => setForm({ ...form, startDate: e.target.value })}
                                className="w-full px-3 py-2 border border-gray-300 rounded focus:outline-none focus:ring-2 focus:ring-blue-500"
                            />
                        </div>
                        <div>
                            <label className="block text-sm font-medium text-gray-700 mb-1">종료일</label>
                            <input
                                type="date"
                                value={form.endDate}
                                onChange={(e) => setForm({ ...form, endDate: e.target.value })}
                                className="w-full px-3 py-2 border border-gray-300 rounded focus:outline-none focus:ring-2 focus:ring-blue-500"
                            />
                        </div>
                    </div>
                    <button
                        type="submit"
                        disabled={submitting}
                        className="px-4 py-2 bg-blue-600 text-white rounded hover:bg-blue-700 disabled:opacity-50"
                    >
                        {submitting ? '저장 중...' : '저장'}
                    </button>
                </form>
            )}

            {/* 탭 네비게이션 */}
            <div className="border-b border-gray-200">
                <nav className="flex gap-4">
                    <button
                        onClick={() => setActiveTab('links')}
                        className={`py-2 px-1 border-b-2 font-medium text-sm ${
                            activeTab === 'links'
                                ? 'border-blue-500 text-blue-600'
                                : 'border-transparent text-gray-500 hover:text-gray-700'
                        }`}
                    >
                        링크 관리
                    </button>
                    <button
                        onClick={() => setActiveTab('performance')}
                        className={`py-2 px-1 border-b-2 font-medium text-sm ${
                            activeTab === 'performance'
                                ? 'border-blue-500 text-blue-600'
                                : 'border-transparent text-gray-500 hover:text-gray-700'
                        }`}
                    >
                        성과 비교
                    </button>
                    <button
                        onClick={() => setActiveTab('ranking')}
                        className={`py-2 px-1 border-b-2 font-medium text-sm ${
                            activeTab === 'ranking'
                                ? 'border-blue-500 text-blue-600'
                                : 'border-transparent text-gray-500 hover:text-gray-700'
                        }`}
                    >
                        채널 랭킹
                    </button>
                </nav>
            </div>

            {/* 탭1: 링크 관리 */}
            {activeTab === 'links' && (
                <>
                    <div className="bg-white p-6 rounded-lg shadow">
                        <h2 className="text-lg font-semibold mb-4">트래킹 링크 생성</h2>
                        {creators.length === 0 || channels.length === 0 ? (
                            <p className="text-gray-500 text-sm">
                                {creators.length === 0 && '크리에이터를 먼저 등록해주세요. '}
                                {channels.length === 0 && '채널을 먼저 등록해주세요.'}
                            </p>
                        ) : (
                            <div className="space-y-3">
                                <div className="grid grid-cols-2 gap-3">
                                    <select
                                        value={selectedCreator}
                                        onChange={(e) => setSelectedCreator(e.target.value)}
                                        className="px-3 py-2 border border-gray-300 rounded focus:outline-none focus:ring-2 focus:ring-blue-500"
                                    >
                                        <option value="">크리에이터 선택</option>
                                        {creators.map((c) => (
                                            <option key={c.id} value={c.id}>{c.name} ({c.channelName})</option>
                                        ))}
                                    </select>
                                    <select
                                        value={selectedChannel}
                                        onChange={(e) => setSelectedChannel(e.target.value)}
                                        className="px-3 py-2 border border-gray-300 rounded focus:outline-none focus:ring-2 focus:ring-blue-500"
                                    >
                                        <option value="">채널 선택</option>
                                        {channels.map((c) => (
                                            <option key={c.id} value={c.id}>{c.platform} &gt; {c.placement}</option>
                                        ))}
                                    </select>
                                </div>
                                <button
                                    onClick={handleCreateLink}
                                    disabled={!selectedCreator || !selectedChannel || submitting}
                                    className="px-4 py-2 bg-blue-600 text-white rounded hover:bg-blue-700 disabled:opacity-50"
                                >
                                    {submitting ? '생성 중...' : '링크 생성'}
                                </button>
                            </div>
                        )}
                    </div>

                    <div className="bg-white p-6 rounded-lg shadow">
                        <h2 className="text-lg font-semibold mb-4">발급된 트래킹 링크</h2>
                        {links.length === 0 ? (
                            <p className="text-gray-500 text-sm">발급된 링크가 없습니다.</p>
                        ) : (
                            <table className="w-full">
                                <thead className="bg-gray-50">
                                <tr>
                                    <th className="px-4 py-3 text-left text-sm font-medium text-gray-700">크리에이터</th>
                                    <th className="px-4 py-3 text-left text-sm font-medium text-gray-700">채널</th>
                                    <th className="px-4 py-3 text-left text-sm font-medium text-gray-700">트래킹 링크</th>
                                    <th className="px-4 py-3 text-left text-sm font-medium text-gray-700">액션</th>
                                </tr>
                                </thead>
                                <tbody className="divide-y divide-gray-200">
                                {links.map((link) => {
                                    const creator = creators.find((c) => c.id === link.creatorId)
                                    const creatorName = creator?.name || `ID: ${link.creatorId}`
                                    const channel = channels.find((c) => c.id === link.channelId)
                                    return (
                                        <tr key={link.id}>
                                            <td className="px-4 py-3">{creatorName}</td>
                                            <td className="px-4 py-3">
                                                <div className="flex items-center gap-2">
                                                    {channel?.iconUrl ? (
                                                        <img src={channel.iconUrl} alt="" className="w-8 h-8 rounded object-contain bg-gray-50"/>
                                                    ) : (
                                                        <div className="w-8 h-8 rounded bg-gray-200"></div>
                                                    )}
                                                    <span className="text-sm text-gray-600">
                              {link.channelDisplay || (channel ? `${channel.platform} > ${channel.placement}` : '-')}
                            </span>
                                                </div>
                                            </td>
                                            <td className="px-4 py-3 text-sm font-mono text-gray-600">
                                                /t/{link.slug}
                                            </td>
                                            <td className="px-4 py-3">
                                                <button
                                                    onClick={() => copyToClipboard(link.slug)}
                                                    className="text-blue-600 hover:underline text-sm mr-3"
                                                >
                                                    복사
                                                </button>
                                                <button
                                                    onClick={() => handleDeleteLink(link.id, creatorName)}
                                                    className="text-red-600 hover:underline text-sm"
                                                >
                                                    삭제
                                                </button>
                                            </td>
                                        </tr>
                                    )
                                })}
                                </tbody>
                            </table>
                        )}
                    </div>
                </>
            )}

            {/* 탭2: 성과 비교 */}
            {activeTab === 'performance' && (
                <>
                    {/* 기간 필터 */}
                    <div className="bg-white p-4 rounded-lg shadow flex items-center gap-4">
                        <span className="text-sm font-medium text-gray-700">기간:</span>
                        <select
                            value={dateRange}
                            onChange={(e) => setDateRange(e.target.value)}
                            className="px-3 py-1.5 border border-gray-300 rounded text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
                        >
                            <option value="all">전체 기간</option>
                            <option value="7days">최근 7일</option>
                            <option value="custom">직접 선택</option>
                        </select>
                        {dateRange === 'custom' && (
                            <>
                                <input
                                    type="date"
                                    value={customFrom}
                                    onChange={(e) => setCustomFrom(e.target.value)}
                                    className="px-3 py-1.5 border border-gray-300 rounded text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
                                />
                                <span className="text-gray-500">~</span>
                                <input
                                    type="date"
                                    value={customTo}
                                    onChange={(e) => setCustomTo(e.target.value)}
                                    className="px-3 py-1.5 border border-gray-300 rounded text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
                                />
                            </>
                        )}
                    </div>

                    {statsLoading ? (
                        <div className="text-gray-500">로딩 중...</div>
                    ) : (
                        <>
                            {/* KPI 카드 */}
                            <div className="grid grid-cols-4 gap-4">
                                <div className="bg-white p-5 rounded-lg shadow">
                                    <p className="text-sm text-gray-500">기간 내 클릭</p>
                                    <p className="text-2xl font-bold text-blue-600">{kpi?.rangeClicks?.toLocaleString() || 0}</p>
                                </div>
                                <div className="bg-white p-5 rounded-lg shadow">
                                    <p className="text-sm text-gray-500">오늘 클릭</p>
                                    {campaign.state === 'ENDED' ? (
                                        <p className="text-gray-400 text-sm">캠페인 종료</p>
                                    ) : (
                                        <p className="text-2xl font-bold text-green-600">{kpi?.todayClicks?.toLocaleString() || 0}</p>
                                    )}
                                    {/*<p className="text-2xl font-bold text-green-600">{kpi?.todayClicks?.toLocaleString() || 0}</p>*/}
                                </div>
                                <div className="bg-white p-5 rounded-lg shadow">
                                    <p className="text-sm text-gray-500">활성 링크</p>
                                    <p className="text-2xl font-bold">{kpi?.activeLinks || 0}</p>
                                </div>
                                <div className="bg-white p-5 rounded-lg shadow">
                                    <p className="text-sm text-gray-500">Top 조합</p>
                                    {topCombination && topCombination.rangeClicks > 0 ? (
                                        <>
                                            <p className="text-lg font-bold text-purple-600 truncate">
                                                {topCombination.creatorName}
                                            </p>
                                            <p className="text-xs text-gray-400">{topCombination.channelDisplayName} · {topCombination.rangeClicks}회</p>
                                        </>
                                    ) : (
                                        <p className="text-gray-400 text-sm">데이터 없음</p>
                                    )}
                                </div>
                            </div>

                            {/* 조합별 성과 테이블 */}
                            <div className="bg-white p-6 rounded-lg shadow">
                                <h2 className="text-lg font-semibold mb-4">크리에이터 × 채널 성과</h2>
                                {combinations.length === 0 ? (
                                    <p className="text-gray-500 text-sm">데이터가 없습니다.</p>
                                ) : (
                                    <table className="w-full">
                                        <thead className="bg-gray-50">
                                        <tr>
                                            <th className="px-4 py-3 text-left text-sm font-medium text-gray-700">크리에이터</th>
                                            <th className="px-4 py-3 text-left text-sm font-medium text-gray-700">채널</th>
                                            <th className="px-4 py-3 text-right text-sm font-medium text-gray-700">오늘</th>
                                            <th className="px-4 py-3 text-right text-sm font-medium text-gray-700">기간 내</th>
                                            <th className="px-4 py-3 text-right text-sm font-medium text-gray-700">전체</th>
                                        </tr>
                                        </thead>
                                        <tbody className="divide-y divide-gray-200">
                                        {combinations.map((row, idx) => {
                                            const channel = channels.find(c => c.id === row.channelId)
                                            return (
                                                <tr key={`${row.creatorId}-${row.channelId}`} className={idx === 0 && row.rangeClicks > 0 ? 'bg-purple-50' : ''}>
                                                    <td className="px-4 py-3">
                                                        <div className="flex items-center gap-2">
                                                            {idx === 0 && row.rangeClicks > 0 && (
                                                                <span className="px-1.5 py-0.5 text-xs rounded bg-purple-100 text-purple-800">TOP</span>
                                                            )}
                                                            {row.creatorName}
                                                        </div>
                                                    </td>
                                                    <td className="px-4 py-3">
                                                        <div className="flex items-center gap-2">
                                                            {channel?.iconUrl ? (
                                                                <img src={channel.iconUrl} alt="" className="w-6 h-6 rounded object-contain bg-gray-50"/>
                                                            ) : (
                                                                <div className="w-6 h-6 rounded bg-gray-200"></div>
                                                            )}
                                                            <span className="text-sm text-gray-600">{row.channelDisplayName}</span>
                                                        </div>
                                                    </td>
                                                    <td className="px-4 py-3 text-right font-medium">
                                                        {row.todayClicks > 0 ? (
                                                            <span className="text-green-600">{row.todayClicks.toLocaleString()}</span>
                                                        ) : (
                                                            <span className="text-gray-400">0</span>
                                                        )}
                                                    </td>
                                                    <td className="px-4 py-3 text-right font-medium">{row.rangeClicks.toLocaleString()}</td>
                                                    <td className="px-4 py-3 text-right text-gray-500">{row.totalClicks.toLocaleString()}</td>
                                                </tr>
                                            )
                                        })}
                                        </tbody>
                                    </table>
                                )}
                            </div>
                        </>
                    )}
                </>
            )}

            {/* 탭3: 채널 랭킹 */}
            {activeTab === 'ranking' && (
                <>
                    {/* 기간 필터 */}
                    <div className="bg-white p-4 rounded-lg shadow flex items-center gap-4">
                        <span className="text-sm font-medium text-gray-700">기간:</span>
                        <select
                            value={dateRange}
                            onChange={(e) => setDateRange(e.target.value)}
                            className="px-3 py-1.5 border border-gray-300 rounded text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
                        >
                            <option value="all">전체 기간</option>
                            <option value="7days">최근 7일</option>
                            <option value="custom">직접 선택</option>
                        </select>
                        {dateRange === 'custom' && (
                            <>
                                <input
                                    type="date"
                                    value={customFrom}
                                    onChange={(e) => setCustomFrom(e.target.value)}
                                    className="px-3 py-1.5 border border-gray-300 rounded text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
                                />
                                <span className="text-gray-500">~</span>
                                <input
                                    type="date"
                                    value={customTo}
                                    onChange={(e) => setCustomTo(e.target.value)}
                                    className="px-3 py-1.5 border border-gray-300 rounded text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
                                />
                            </>
                        )}
                    </div>

                    {statsLoading ? (
                        <div className="text-gray-500">로딩 중...</div>
                    ) : (
                        <div className="bg-white p-6 rounded-lg shadow">
                            <h2 className="text-lg font-semibold mb-4">채널 Top 10</h2>
                            {channelRanking.length === 0 ? (
                                <p className="text-gray-500 text-sm">클릭 데이터가 있는 채널이 없습니다.</p>
                            ) : (
                                <ResponsiveContainer width="100%" height={300}>
                                    <BarChart data={channelRanking} layout="vertical" margin={{ left: 20, right: 30 }}>
                                        <XAxis type="number" />
                                        <YAxis
                                            type="category"
                                            dataKey="channelDisplayName"
                                            width={150}
                                            tick={{ fontSize: 12 }}
                                        />
                                        <Tooltip
                                            formatter={(value) => [`${value.toLocaleString()} 클릭`, '클릭 수']}
                                        />
                                        <Bar dataKey="clicks" radius={[0, 4, 4, 0]}>
                                            {channelRanking.map((entry, index) => (
                                                <Cell
                                                    key={`cell-${index}`}
                                                    fill={index === 0 ? '#8b5cf6' : index < 3 ? '#3b82f6' : '#93c5fd'}
                                                />
                                            ))}
                                        </Bar>
                                    </BarChart>
                                </ResponsiveContainer>
                            )}
                        </div>
                    )}
                </>
            )}
        </div>
    )
}