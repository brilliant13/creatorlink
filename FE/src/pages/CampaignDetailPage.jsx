import { useState, useEffect } from 'react'
import { useParams } from 'react-router-dom'
import { useAuth } from '../contexts/AuthContext'
import { getCampaign } from '../api/campaigns'
import { getTrackingLinks, createTrackingLink } from '../api/trackingLinks'
import { getCreators } from '../api/creators'

export default function CampaignDetailPage() {
    const { id } = useParams()
    const { user } = useAuth()
    const [campaign, setCampaign] = useState(null)
    const [links, setLinks] = useState([])
    const [creators, setCreators] = useState([])
    const [loading, setLoading] = useState(true)
    const [selectedCreator, setSelectedCreator] = useState('')
    const [submitting, setSubmitting] = useState(false)

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

    const copyToClipboard = (slug) => {
        navigator.clipboard.writeText(`${window.location.origin}/t/${slug}`)
    }

    if (loading) {
        return <div className="text-gray-500">로딩 중...</div>
    }

    if (!campaign) {
        return <div className="text-gray-500">캠페인을 찾을 수 없습니다.</div>
    }

    const linkedCreatorIds = links.map((l) => l.creatorId)
    const availableCreators = creators.filter((c) => !linkedCreatorIds.includes(c.id))

    return (
        <div className="space-y-6">
            <div>
                <h1 className="text-2xl font-bold">{campaign.name}</h1>
                {campaign.description && <p className="text-gray-600 mt-1">{campaign.description}</p>}
            </div>

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