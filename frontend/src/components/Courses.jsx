import React, { useState } from 'react'
import { createCourse } from '../api'

export default function Courses({ courses = [], onCreated }) {
  const [title, setTitle] = useState('')
  const [description, setDescription] = useState('')

  async function submit(e) {
    e.preventDefault()
    if (!title) return alert('Title required')
    await createCourse({ title, description })
    setTitle('')
    setDescription('')
    if (onCreated) onCreated()
  }

  return (
    <div>
      <h2>Courses</h2>
      <ul>
        {courses.map(c => (
          <li key={c.id}>{c.title} {c.teacher ? `- ${c.teacher.fullName || c.teacher.username}` : ''}</li>
        ))}
      </ul>

      <h3>Create Course</h3>
      <form onSubmit={submit}>
        <div>
          <input placeholder="Title" value={title} onChange={e => setTitle(e.target.value)} />
        </div>
        <div>
          <textarea placeholder="Description" value={description} onChange={e => setDescription(e.target.value)} />
        </div>
        <button type="submit">Create</button>
      </form>
    </div>
  )
}

