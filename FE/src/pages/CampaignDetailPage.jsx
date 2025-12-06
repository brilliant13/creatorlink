import { useState, useEffect } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import { useAuth } from '../contexts/AuthContext'
import { getCampaign, updateCampaign, deleteCampaign } from '../api/campaigns'
import { getTrackingLinks, createTrackingLink } from '../api/trackingLinks'
import { getCreators } from '../api/creators'

export default function CampaignDetailPage() {
    const { id } = useParams()
    const navigate = useNavigate()
    const { user } = useAuth()
    const [campaign, setCampaign] = useState(null)
    const [links, setLinks] = useState([])
    const [creators, setCreators] = useState([])
    const [loading, setLoading] = useState(true)
    const [selectedCreator, setSelectedCreator] = useState('')
    const [submitting, setSubmitting] = useState(false)
    const [editing, setEditing] = useState(false)
    const [form, setForm] = useState({ name: '', description: '', landingUrl: '', startDate: '', endDate: '' })

    const fetchData = async () => {
        try {
            const [campaignRes, linksRes, creatorsRes] = await Promise.all([
                getCampaign(id, user.id),
                getTrackingLinks(id),
                getCreators(user.id),
            ])
            setCampaign(campaignRes.data)
            setLinks(linksRes.data)
            setCreators(creatorsRes.data)
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

    useEffect(() => {
        fetchData()
    }, [id, user.id])

    const handleCreateLink = async () => {
        if (!selectedCreator) return
        setSubmitting(true)
        try {
            await createTrackingLink({
                campaignId: Number(id),
                creatorId: Number(selectedCreator),
            })
            setSelectedCreator('')
            fetchData()
        } catch (err) {
            console.error('Failed to create tracking link:', err)
        } finally {
            setSubmitting(false)
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

    const linkedCreatorIds = links.map((l) => l.creatorId)
    const availableCreators = creators.filter((c) => !linkedCreatorIds.includes(c.id))
    const stateLabel = getStateLabel(campaign.state)

    return (
        <div className="space-y-6">
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

            <div className="bg-white p-6 rounded-lg shadow">
                <h2 className="text-lg font-semibold mb-4">트래킹 링크 생성</h2>
                {availableCreators.length === 0 ? (
                    <p className="text-gray-500 text-sm">모든 크리에이터에게 링크가 발급되었거나, 등록된 크리에이터가 없습니다.</p>
                ) : (
                    <div className="flex gap-2">
                        <select
                            value={selectedCreator}
                            onChange={(e) => setSelectedCreator(e.target.value)}
                            className="flex-1 px-3 py-2 border border-gray-300 rounded focus:outline-none focus:ring-2 focus:ring-blue-500"
                        >
                            <option value="">크리에이터 선택</option>
                            {availableCreators.map((c) => (
                                <option key={c.id} value={c.id}>{c.name} ({c.channelName})</option>
                            ))}
                        </select>
                        <button
                            onClick={handleCreateLink}
                            disabled={!selectedCreator || submitting}
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
                            <th className="px-4 py-3 text-left text-sm font-medium text-gray-700">트래킹 링크</th>
                            <th className="px-4 py-3 text-left text-sm font-medium text-gray-700">액션</th>
                        </tr>
                        </thead>
                        <tbody className="divide-y divide-gray-200">
                        {links.map((link) => {
                            const creator = creators.find((c) => c.id === link.creatorId)
                            return (
                                <tr key={link.id}>
                                    <td className="px-4 py-3">{creator?.name || `ID: ${link.creatorId}`}</td>
                                    <td className="px-4 py-3 text-sm font-mono text-gray-600">
                                        /t/{link.slug}
                                    </td>
                                    <td className="px-4 py-3">
                                        <button
                                            onClick={() => copyToClipboard(link.slug)}
                                            className="text-blue-600 hover:underline text-sm"
                                        >
                                            복사
                                        </button>
                                    </td>
                                </tr>
                            )
                        })}
                        </tbody>
                    </table>
                )}
            </div>
        </div>
    )
}
