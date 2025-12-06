import { useState, useEffect } from 'react'
import { Link } from 'react-router-dom'
import { useAuth } from '../contexts/AuthContext'
import { getCampaigns, createCampaign, updateCampaign, deleteCampaign } from '../api/campaigns'

export default function CampaignsPage() {
    const { user } = useAuth()
    const [campaigns, setCampaigns] = useState([])
    const [loading, setLoading] = useState(true)
    const [showForm, setShowForm] = useState(false)
    const [editingCampaign, setEditingCampaign] = useState(null)
    const [form, setForm] = useState({ name: '', description: '', landingUrl: '', startDate: '', endDate: '' })
    const [submitting, setSubmitting] = useState(false)

    const fetchCampaigns = async () => {
        try {
            const res = await getCampaigns(user.id)
            setCampaigns(res.data)
        } catch (err) {
            console.error('Failed to fetch campaigns:', err)
        } finally {
            setLoading(false)
        }
    }

    useEffect(() => {
        fetchCampaigns()
    }, [user.id])

    const resetForm = () => {
        setForm({ name: '', description: '', landingUrl: '', startDate: '', endDate: '' })
        setEditingCampaign(null)
        setShowForm(false)
    }

    const handleEdit = (campaign) => {
        setEditingCampaign(campaign)
        setForm({
            name: campaign.name,
            description: campaign.description || '',
            landingUrl: campaign.landingUrl,
            startDate: campaign.startDate || '',
            endDate: campaign.endDate || '',
        })
        setShowForm(true)
    }

    const handleDelete = async (campaign) => {
        if (!confirm(`"${campaign.name}" 캠페인을 삭제하시겠습니까?`)) return

        try {
            await deleteCampaign(campaign.id, user.id)
            fetchCampaigns()
        } catch (err) {
            console.error('Failed to delete campaign:', err)
            alert('삭제에 실패했습니다.')
        }
    }

    const handleSubmit = async (e) => {
        e.preventDefault()
        setSubmitting(true)
        try {
            if (editingCampaign) {
                await updateCampaign(editingCampaign.id, { ...form, advertiserId: user.id })
            } else {
                await createCampaign({ ...form, advertiserId: user.id })
            }
            resetForm()
            fetchCampaigns()
        } catch (err) {
            console.error('Failed to save campaign:', err)
        } finally {
            setSubmitting(false)
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

    if (loading) {
        return <div className="text-gray-500">로딩 중...</div>
    }

    const activeCampaigns = campaigns.filter(c => c.state === 'UPCOMING' || c.state === 'RUNNING')
    const endedCampaigns = campaigns.filter(c => c.state === 'ENDED')

    const CampaignTable = ({ campaigns, showActions = true }) => (
        <div className="bg-white rounded-lg shadow overflow-hidden">
            <table className="w-full">
                <thead className="bg-gray-50">
                <tr>
                    <th className="px-4 py-3 text-left text-sm font-medium text-gray-700">캠페인명</th>
                    <th className="px-4 py-3 text-left text-sm font-medium text-gray-700">상태</th>
                    <th className="px-4 py-3 text-left text-sm font-medium text-gray-700">기간</th>
                    <th className="px-4 py-3 text-left text-sm font-medium text-gray-700">랜딩 URL</th>
                    {showActions && <th className="px-4 py-3 text-left text-sm font-medium text-gray-700">액션</th>}
                </tr>
                </thead>
                <tbody className="divide-y divide-gray-200">
                {campaigns.map((c) => {
                    const stateLabel = getStateLabel(c.state)
                    return (
                        <tr key={c.id} className="hover:bg-gray-50">
                            <td className="px-4 py-3">
                                <Link to={`/campaigns/${c.id}`} className="text-blue-600 hover:underline">
                                    {c.name}
                                </Link>
                            </td>
                            <td className="px-4 py-3">
                  <span className={`px-2 py-1 text-xs rounded-full ${stateLabel.color}`}>
                    {stateLabel.text}
                  </span>
                            </td>
                            <td className="px-4 py-3 text-sm text-gray-600">
                                {c.startDate || '-'} ~ {c.endDate || '-'}
                            </td>
                            <td className="px-4 py-3 text-sm text-gray-600 truncate max-w-xs">
                                {c.landingUrl}
                            </td>
                            {showActions && (
                                <td className="px-4 py-3">
                                    <button
                                        onClick={() => handleEdit(c)}
                                        className="text-blue-600 hover:underline text-sm mr-3"
                                    >
                                        수정
                                    </button>
                                    <button
                                        onClick={() => handleDelete(c)}
                                        className="text-red-600 hover:underline text-sm"
                                    >
                                        삭제
                                    </button>
                                </td>
                            )}
                        </tr>
                    )
                })}
                </tbody>
            </table>
        </div>
    )

    return (
        <div className="space-y-6">
            <div className="flex justify-between items-center">
                <h1 className="text-2xl font-bold">캠페인</h1>
                <button
                    onClick={() => {
                        if (showForm) {
                            resetForm()
                        } else {
                            setShowForm(true)
                        }
                    }}
                    className="px-4 py-2 bg-blue-600 text-white rounded hover:bg-blue-700"
                >
                    {showForm ? '취소' : '새 캠페인'}
                </button>
            </div>

            {showForm && (
                <form onSubmit={handleSubmit} className="bg-white p-6 rounded-lg shadow space-y-4">
                    <h2 className="text-lg font-semibold">
                        {editingCampaign ? '캠페인 수정' : '새 캠페인 생성'}
                    </h2>
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
                            placeholder="https://example.com"
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
                        {submitting ? '저장 중...' : (editingCampaign ? '수정' : '생성')}
                    </button>
                </form>
            )}

            <div className="space-y-6">
                <div>
                    <h2 className="text-lg font-semibold mb-3">진행 중 / 시작 예정</h2>
                    {activeCampaigns.length === 0 ? (
                        <div className="bg-white p-6 rounded-lg shadow text-center text-gray-500">
                            진행 중인 캠페인이 없습니다.
                        </div>
                    ) : (
                        <CampaignTable campaigns={activeCampaigns} />
                    )}
                </div>

                {endedCampaigns.length > 0 && (
                    <div>
                        <h2 className="text-lg font-semibold mb-3">종료된 캠페인</h2>
                        <CampaignTable campaigns={endedCampaigns} />
                    </div>
                )}
            </div>
        </div>
    )
}
