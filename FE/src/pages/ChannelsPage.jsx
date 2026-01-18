import { useState, useEffect } from 'react'
import { useAuth } from '../contexts/AuthContext'
import { getChannels, createChannel, updateChannel, deleteChannel } from '../api/channels'
import { uploadFile } from '../api/uploads'

export default function ChannelsPage() {
    const { user } = useAuth()
    const [channels, setChannels] = useState([])
    const [loading, setLoading] = useState(true)
    const [showForm, setShowForm] = useState(false)
    const [editingChannel, setEditingChannel] = useState(null)
    const [form, setForm] = useState({ platform: '', placement: '', note: '', iconUrl: '' })
    const [submitting, setSubmitting] = useState(false)
    const [uploading, setUploading] = useState(false)

    const fetchChannels = async () => {
        try {
            const res = await getChannels(user.id)
            setChannels(res.data)
        } catch (err) {
            console.error('Failed to fetch channels:', err)
        } finally {
            setLoading(false)
        }
    }

    useEffect(() => {
        fetchChannels()
    }, [user.id])

    const resetForm = () => {
        setForm({ platform: '', placement: '', note: '', iconUrl: '' })
        setEditingChannel(null)
        setShowForm(false)
    }

    const handleEdit = (channel) => {
        setEditingChannel(channel)
        setForm({
            platform: channel.platform,
            placement: channel.placement,
            note: channel.note || '',
            iconUrl: channel.iconUrl || '',
        })
        setShowForm(true)
    }

    const handleDelete = async (channel) => {
        if (!confirm(`"${channel.platform} > ${channel.placement}" 채널을 삭제하시겠습니까?`)) return

        try {
            await deleteChannel(channel.id, user.id)
            fetchChannels()
        } catch (err) {
            console.error('Failed to delete channel:', err)
            alert('삭제에 실패했습니다.')
        }
    }

    const handleIconUpload = async (e) => {
        const file = e.target.files?.[0]
        if (!file) return

        setUploading(true)
        try {
            const result = await uploadFile(file)
            setForm({ ...form, iconUrl: result.url })
        } catch (err) {
            console.error('Failed to upload icon:', err)
            alert('아이콘 업로드에 실패했습니다.')
        } finally {
            setUploading(false)
        }
    }

    const handleSubmit = async (e) => {
        e.preventDefault()
        setSubmitting(true)
        try {
            if (editingChannel) {
                await updateChannel(editingChannel.id, { ...form, advertiserId: user.id })
            } else {
                await createChannel({ ...form, advertiserId: user.id })
            }
            resetForm()
            fetchChannels()
        } catch (err) {
            console.error('Failed to save channel:', err)
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
                <h1 className="text-2xl font-bold">채널</h1>
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
                    {showForm ? '취소' : '새 채널'}
                </button>
            </div>

            {showForm && (
                <form onSubmit={handleSubmit} className="bg-white p-6 rounded-lg shadow space-y-4">
                    <h2 className="text-lg font-semibold">
                        {editingChannel ? '채널 수정' : '새 채널 등록'}
                    </h2>
                    <div className="grid grid-cols-2 gap-4">
                        <div>
                            <label className="block text-sm font-medium text-gray-700 mb-1">플랫폼 *</label>
                            <input
                                type="text"
                                value={form.platform}
                                onChange={(e) => setForm({...form, platform: e.target.value})}
                                required
                                placeholder="예: YouTube, Instagram, TikTok"
                                className="w-full px-3 py-2 border border-gray-300 rounded focus:outline-none focus:ring-2 focus:ring-blue-500"
                            />
                        </div>
                        <div>
                            <label className="block text-sm font-medium text-gray-700 mb-1">게시 위치 *</label>
                            <input
                                type="text"
                                value={form.placement}
                                onChange={(e) => setForm({...form, placement: e.target.value})}
                                required
                                placeholder="예: 영상 설명란, 스토리, 프로필 링크"
                                className="w-full px-3 py-2 border border-gray-300 rounded focus:outline-none focus:ring-2 focus:ring-blue-500"
                            />
                        </div>
                    </div>

                    {/*<div>*/}
                    {/*    <label className="block text-sm font-medium text-gray-700 mb-1">아이콘</label>*/}
                    {/*    <div className="flex items-center gap-4">*/}
                    {/*        {form.iconUrl && (*/}
                    {/*            <img src={form.iconUrl} alt="icon" className="w-10 h-10 rounded object-cover" />*/}
                    {/*        )}*/}
                    {/*        <input*/}
                    {/*            type="file"*/}
                    {/*            accept="image/*"*/}
                    {/*            onChange={handleIconUpload}*/}
                    {/*            disabled={uploading}*/}
                    {/*            className="text-sm"*/}
                    {/*        />*/}
                    {/*        {uploading && <span className="text-sm text-gray-500">업로드 중...</span>}*/}
                    {/*    </div>*/}
                    {/*</div>*/}
                    <div>
                        <label className="block text-sm font-medium text-gray-700 mb-1">아이콘</label>
                        <div className="flex items-center gap-4">
                            {form.iconUrl ? (
                                <img src={form.iconUrl} alt="icon"
                                     className="w-10 h-10 rounded object-contain bg-gray-50"/>
                            ) : (
                                <div
                                    className="w-10 h-10 rounded bg-gray-200 flex items-center justify-center text-gray-400 text-xs">
                                    없음
                                </div>
                            )}
                            <label
                                className="px-3 py-1.5 bg-gray-100 text-gray-700 text-sm rounded cursor-pointer hover:bg-gray-200">
                                파일 선택
                                <input
                                    type="file"
                                    accept="image/*"
                                    onChange={handleIconUpload}
                                    disabled={uploading}
                                    className="hidden"
                                />
                            </label>
                            {uploading && <span className="text-sm text-gray-500">업로드 중...</span>}
                            {form.iconUrl && (
                                <button
                                    type="button"
                                    onClick={() => setForm({...form, iconUrl: ''})}
                                    className="text-sm text-red-500 hover:underline"
                                >
                                    삭제
                                </button>
                            )}
                        </div>
                    </div>
                    <div>
                        <label className="block text-sm font-medium text-gray-700 mb-1">메모</label>
                        <textarea
                            value={form.note}
                            onChange={(e) => setForm({...form, note: e.target.value})}
                            className="w-full px-3 py-2 border border-gray-300 rounded focus:outline-none focus:ring-2 focus:ring-blue-500"
                            rows={2}
                        />
                    </div>
                    <button
                        type="submit"
                        disabled={submitting}
                        className="px-4 py-2 bg-blue-600 text-white rounded hover:bg-blue-700 disabled:opacity-50"
                    >
                        {submitting ? '저장 중...' : (editingChannel ? '수정' : '등록')}
                    </button>
                </form>
            )}

            {channels.length === 0 ? (
                <div className="bg-white p-6 rounded-lg shadow text-center text-gray-500">
                    등록된 채널이 없습니다.
                </div>
            ) : (
                <div className="bg-white rounded-lg shadow overflow-hidden">
                    <table className="w-full">
                        <thead className="bg-gray-50">
                        <tr>
                            <th className="px-4 py-3 text-left text-sm font-medium text-gray-700">아이콘</th>
                            <th className="px-4 py-3 text-left text-sm font-medium text-gray-700">플랫폼</th>
                            <th className="px-4 py-3 text-left text-sm font-medium text-gray-700">게시 위치</th>
                            <th className="px-4 py-3 text-left text-sm font-medium text-gray-700">메모</th>
                            <th className="px-4 py-3 text-left text-sm font-medium text-gray-700">액션</th>
                        </tr>
                        </thead>
                        <tbody className="divide-y divide-gray-200">
                        {channels.map((c) => (
                            <tr key={c.id} className="hover:bg-gray-50">
                                {/*<td className="px-4 py-3">*/}
                                {/*    {c.iconUrl ? (*/}
                                {/*        <img src={c.iconUrl} alt="" className="w-8 h-8 rounded object-cover" />*/}
                                {/*    ) : (*/}
                                {/*        <div className="w-8 h-8 rounded bg-gray-200"></div>*/}
                                {/*    )}*/}
                                {/*</td>*/}
                                <td className="px-4 py-3">
                                    {c.iconUrl ? (
                                        <img src={c.iconUrl} alt=""
                                             className="w-10 h-10 rounded object-contain bg-gray-50"/>
                                    ) : (
                                        <div className="w-10 h-10 rounded bg-gray-200"></div>
                                    )}
                                </td>
                                <td className="px-4 py-3 font-medium">{c.platform}</td>
                                <td className="px-4 py-3">{c.placement}</td>
                                <td className="px-4 py-3 text-sm text-gray-600">{c.note || '-'}</td>
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
                            </tr>
                        ))}
                        </tbody>
                    </table>
                </div>
            )}
        </div>
    )
}