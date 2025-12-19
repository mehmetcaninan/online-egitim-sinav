import React, { useState, useEffect } from 'react'
import Auth from './components/Auth'
import StudentDashboard from './components/StudentDashboard'
import TeacherDashboard from './components/TeacherDashboard'
import Admin from './components/Admin'
import { getCurrentUser, logout } from './api'

export default function App() {
  const [currentUser, setCurrentUser] = useState(null)
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    checkAuthStatus()
  }, [])

  async function checkAuthStatus() {
    try {
      const user = await getCurrentUser()
      setCurrentUser(user)
    } catch (error) {
      console.error('Auth check failed:', error)
    } finally {
      setLoading(false)
    }
  }

  const handleLogin = (user) => {
    setCurrentUser(user)
  }

  const handleLogout = async () => {
    await logout()
    setCurrentUser(null)
  }

  const renderUserDashboard = () => {
    if (!currentUser) return null

    switch (currentUser.role) {
      case 'ADMIN':
        return <Admin />
      case 'TEACHER':
        return <TeacherDashboard user={currentUser} />
      case 'STUDENT':
        return <StudentDashboard user={currentUser} />
      default:
        return <div>Rol tanımlanmamış. Lütfen yöneticiye başvurun.</div>
    }
  }

  if (loading) {
    return (
      <div>
        <div className="loading">Yükleniyor...</div>
      </div>
    )
  }

  if (!currentUser) {
    return (
      <div>
        <Auth onLogin={handleLogin} />
      </div>
    )
  }

  return (
    <div className="app">

      <header>
        <div className="header-content">
          <h1>Online Eğitim Sistemi</h1>
          <div className="user-info">
            <span>Hoş geldiniz, <strong>{currentUser.fullName}</strong></span>
            <span className={`role-badge role-${currentUser.role.toLowerCase()}`}>
              {currentUser.role === 'ADMIN' ? 'Admin' :
               currentUser.role === 'TEACHER' ? 'Öğretmen' : 'Öğrenci'}
            </span>
            <button onClick={handleLogout} className="logout-btn">
              Çıkış Yap
            </button>
          </div>
        </div>
      </header>

      <main>
        {renderUserDashboard()}
      </main>
    </div>
  )
}
