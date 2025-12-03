import { useState, useEffect } from 'react'
import { useAuth } from '../contexts/AuthContext'
import { getCreators, createCreator } from '../api/creators'

export default function CreatorsPage() {
    const { user } = useAuth()
    const [creators, setCreators] = useState([])
    const [loading, setLoading] = useState(true)
    const [showForm, setShowForm] = useState(false)
    const [form, setForm] = useState({ name: '', channelName: '', channelUrl: '', note: '' })
    const [submitting, setSubmitting] = useState(false)

    const fetchCreators = async () => {
        try {
            const res = await getCreators(user.id)
            setCreators(res.data)
        } catch (err) {
            console.error('Failed to fetch creators:', err)
        } finally {
            setLoading(false)
        }
    }

    useEffect(() => {
        fetchCreators()
    }, [user.id])

    const handleSubmit = async (e) => {
        e.preventDefault()
        setSubmitting(true)
        try {
            await createCreator({ ...form, advertiserId: user.id })
            setForm({ name: '', channelName: '', channelUrl: '', note: '' })
            setShowForm(false)
            fetchCreators()
        } catch (err) {
            console.error('Failed to create creator:', err)
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
                <h1 className="text-2xl font-bold">크리에이터</h1>
                <button
                    onClick={() => setShowForm(!showForm)}
                    className="px-4 py-2 bg-blue-600 text-white rounded hover:bg-blue-700"
                >
                    {showForm ? '취소' : '새 크리에이터'}
                </button>
            </div>

            {showForm && (
                <form onSubmit={handleSubmit} className="bg-white p-6 rounded-lg shadow space-y-4">
                    <div>
                        <label className="block text-sm font-medium text-gray-700 mb-1">이름 *</label>
                        <input
                            type="text"
                            value={form.name}
                            onChange={(e) => setForm({ ...form, name: e.target.value })}
                            required
                            className="w-full px-3 py-2 border border-gray-300 rounded focus:outline-none focus:ring-2 focus:ring-blue-500"
                        />
                    </div>
                    <div>
                        <label className="block text-sm font-medium text-gray-700 mb-1">채널명 *</label>
                        <input
                            type="text"
                            value={form.channelName}
                            onChange={(e) => setForm({ ...form, channelName: e.target.value })}
                            required
                            className="w-full px-3 py-2 border border-gray-300 rounded focus:outline-none focus:ring-2 focus:ring-blue-500"
                        />
                    </div>
                    <div>
                        <label className="block text-sm font-medium text-gray-700 mb-1">채널 URL *</label>
                        <input
                            type="url"
                            value={form.channelUrl}
                            onChange={(e) => setForm({ ...form, channelUrl: e.target.value })}
                            required
                            placeholder="https://youtube.com/@channel"
                            className="w-full px-3 py-2 border border-gray-300 rounded focus:outline-none focus:ring-2 focus:ring-blue-500"
                        />
                    </div>
                    <div>
                        <label className="block text-sm font-medium text-gray-700 mb-1">메모</label>
                        <textarea
                            value={form.note}
                            onChange={(e) => setForm({ ...form, note: e.target.value })}
                            className="w-full px-3 py-2 border border-gray-300 rounded focus:outline-none focus:ring-2 focus:ring-blue-500"
                            rows={2}
                        />
                    </div>
                    <button
                        type="submit"
                        disabled={submitting}
                        className="px-4 py-2 bg-blue-600 text-white rounded hover:bg-blue-700 disabled:opacity-50"
                    >
                        {submitting ? '생성 중...' : '크리에이터 등록'}
                    </button>
                </form>
            )}

            {creators.length === 0 ? (
                <div className="bg-white p-6 rounded-lg shadow text-center text-gray-500">
                    등록된 크리에이터가 없습니다.
                </div>
            ) : (
                <div className="bg-white rounded-lg shadow overflow-hidden">
                    <table className="w-full">
                        <thead className="bg-gray-50">
                        <tr>
                            <th className="px-4 py-3 text-left text-sm font-medium text-gray-700">이름</th>
                            <th className="px-4 py-3 text-left text-sm font-medium text-gray-700">채널명</th>
                            <th className="px-4 py-3 text-left text-sm font-medium text-gray-700">채널 URL</th>
                            <th className="px-4 py-3 text-left text-sm font-medium text-gray-700">메모</th>
                        </tr>
                        </thead>
                        <tbody className="divide-y divide-gray-200">
                        {creators.map((c) => (
                            <tr key={c.id} className="hover:bg-gray-50">
                                <td className="px-4 py-3">{c.name}</td>
                                <td className="px-4 py-3">{c.channelName}</td>
                                <td className="px-4 py-3 text-sm text-gray-600 truncate max-w-xs">
                                    <a href={c.channelUrl} target="_blank" rel="noreferrer" className="text-blue-600 hover:underline">
                                        {c.channelUrl}
                                    </a>
                                </td>
                                <td className="px-4 py-3 text-sm text-gray-600">{c.note || '-'}</td>
                            </tr>
                        ))}
                        </tbody>
                    </table>
                </div>
            )}
        </div>
    )
}