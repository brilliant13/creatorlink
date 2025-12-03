import { useState, useEffect } from 'react'
import { Link } from 'react-router-dom'
import { useAuth } from '../contexts/AuthContext'
import { getCampaigns, createCampaign } from '../api/campaigns'

export default function CampaignsPage() {
    const { user } = useAuth()
    const [campaigns, setCampaigns] = useState([])
    const [loading, setLoading] = useState(true)
    const [showForm, setShowForm] = useState(false)
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

    const handleSubmit = async (e) => {
        e.preventDefault()
        setSubmitting(true)
        try {
            await createCampaign({ ...form, advertiserId: user.id })
            setForm({ name: '', description: '', landingUrl: '', startDate: '', endDate: '' })
            setShowForm(false)
            fetchCampaigns()
        } catch (err) {
            console.error('Failed to create campaign:', err)
        } finally {
            setSubmitting(false)
        }
    }

    if (loading) {
        return <div className="text-gray-500">로딩 중...</div>
    }

    return (
        <div className="space-y-6">
            <div className="flex justify-between items-center">
                <h1 className="text-2xl font-bold">캠페인</h1>
                <button
                    onClick={() => setShowForm(!showForm)}
                    className="px-4 py-2 bg-blue-600 text-white rounded hover:bg-blue-700"
                >
                    {showForm ? '취소' : '새 캠페인'}
                </button>
            </div>

            {showForm && (
                <form onSubmit={handleSubmit} className="bg-white p-6 rounded-lg shadow space-y-4">
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
                        {submitting ? '생성 중...' : '캠페인 생성'}
                    </button>
                </form>
            )}

            {campaigns.length === 0 ? (
                <div className="bg-white p-6 rounded-lg shadow text-center text-gray-500">
                    등록된 캠페인이 없습니다.
                </div>
            ) : (
                <div className="bg-white rounded-lg shadow overflow-hidden">
                    <table className="w-full">
                        <thead className="bg-gray-50">
                        <tr>
                            <th className="px-4 py-3 text-left text-sm font-medium text-gray-700">캠페인명</th>
                            <th className="px-4 py-3 text-left text-sm font-medium text-gray-700">기간</th>
                            <th className="px-4 py-3 text-left text-sm font-medium text-gray-700">랜딩 URL</th>
                        </tr>
                        </thead>
                        <tbody className="divide-y divide-gray-200">
                        {campaigns.map((c) => (
                            <tr key={c.id} className="hover:bg-gray-50">
                                <td className="px-4 py-3">
                                    <Link to={`/campaigns/${c.id}`} className="text-blue-600 hover:underline">
                                        {c.name}
                                    </Link>
                                </td>
                                <td className="px-4 py-3 text-sm text-gray-600">
                                    {c.startDate || '-'} ~ {c.endDate || '-'}
                                </td>
                                <td className="px-4 py-3 text-sm text-gray-600 truncate max-w-xs">
                                    {c.landingUrl}
                                </td>
                            </tr>
                        ))}
                        </tbody>
                    </table>
                </div>
            )}
        </div>
    )
}