import React, { useState } from 'react'
import { uploadResource } from '../api'

export default function Upload({ courses = [], onUploaded }) {
  const [courseId, setCourseId] = useState(courses.length ? courses[0].id : '')
  const [file, setFile] = useState(null)
  const [type, setType] = useState('MATERIAL')
  const [result, setResult] = useState(null)

  async function submit(e) {
    e.preventDefault()
    if (!file) return alert('Select file')
    const res = await uploadResource(courseId || null, file, type)
    setResult(res)
    if (onUploaded) onUploaded()
  }

  return (
    <div>
      <h2>Upload Resource</h2>
      <form onSubmit={submit}>
        <div>
          <label>Course</label>
          <select value={courseId} onChange={e => setCourseId(e.target.value)}>
            <option value="">(none)</option>
            {courses.map(c => <option key={c.id} value={c.id}>{c.title}</option>)}
          </select>
        </div>
        <div>
          <label>Type</label>
          <select value={type} onChange={e => setType(e.target.value)}>
            <option value="MATERIAL">Material</option>
            <option value="VIDEO">Video</option>
          </select>
        </div>
        <div>
          <input type="file" onChange={e => setFile(e.target.files[0])} />
        </div>
        <button type="submit">Upload</button>
      </form>

      {result && (
        <div style={{marginTop:10}}>
          <h4>Uploaded</h4>
          <pre>{JSON.stringify(result, null, 2)}</pre>
        </div>
      )}
    </div>
  )
}

