import axios from 'axios'

export const uploadFile = async (file) => {
    const formData = new FormData()
    formData.append('file', file)

    const res = await axios.post('/api/uploads', formData, {
        headers: {
            'Content-Type': 'multipart/form-data',
        },
    })
    return res.data
}