import React, { useState } from 'react'
import { registerUser, loginUser } from '../api'

export default function Auth({ onLogin }) {
  const [isLogin, setIsLogin] = useState(true)
  const [formData, setFormData] = useState({
    username: '',
    password: '',
    fullName: '',
    requestedRole: 'STUDENT'
  })
  const [message, setMessage] = useState('')
  const [loading, setLoading] = useState(false)
  const [debugInfo, setDebugInfo] = useState('')

  const testBackendConnection = async () => {
    setDebugInfo('Backend bağlantısı test ediliyor...')

    try {
      // Önce health endpoint'ini deneyelim (backend 8080 portunda)
      const response = await fetch('http://localhost:8080/api/health', {
        method: 'GET',
        headers: {
          'Accept': 'application/json',
          'Content-Type': 'application/json'
        },
        mode: 'cors'
      })

      if (response.ok) {
        const data = await response.json()
        setDebugInfo(`✅ Backend çalışıyor: ${data.message}`)
        return true
      }

      // Health endpoint yoksa courses'u deneyelim
      const coursesResponse = await fetch('http://localhost:8080/api/courses', {
        method: 'GET',
        headers: { 'Accept': 'application/json' },
        mode: 'cors'
      })

      if (coursesResponse.ok || coursesResponse.status === 200) {
        setDebugInfo(`✅ Backend çalışıyor (Courses API: ${coursesResponse.status})`)
        return true
      }

      setDebugInfo(`⚠️ Backend yanıt verdi ama status: ${response.status}`)
      return false

    } catch (error) {
      setDebugInfo(`❌ Backend bağlantı hatası: ${error.message}`)
      console.error('Backend connection test failed:', error)
      return false
    }
  }

  const handleSubmit = async (e) => {
    e.preventDefault()
    setLoading(true)
    setMessage('')
    setDebugInfo('')

    // Backend bağlantı testi
    const backendWorking = await testBackendConnection()
    if (!backendWorking) {
      setMessage('Backend sunucusuna bağlanılamıyor. Lütfen backend\'in çalıştığından emin olun.')
      setLoading(false)
      return
    }

    try {
      if (isLogin) {
        if (!formData.username || !formData.password) {
          setMessage('Kullanıcı adı ve şifre gereklidir')
          setLoading(false)
          return
        }

        setDebugInfo('Giriş isteği gönderiliyor...')
        const result = await loginUser({
          username: formData.username.trim(),
          password: formData.password.trim()
        })

        setDebugInfo(`API yanıtı alındı: ${JSON.stringify(result)}`)

        if (result.error) {
          setMessage(result.error)
        } else if (result.user) {
          setMessage('Giriş başarılı!')
          onLogin(result.user)
        } else {
          setMessage('Giriş başarısız - beklenmeyen yanıt')
          console.log('Login response:', result)
        }
      } else {
        if (!formData.username || !formData.password || !formData.fullName) {
          setMessage('Tüm alanları doldurmak gereklidir')
          setLoading(false)
          return
        }

        setDebugInfo('Kayıt isteği gönderiliyor...')
        const result = await registerUser({
          username: formData.username.trim(),
          password: formData.password.trim(),
          fullName: formData.fullName.trim(),
          requestedRole: formData.requestedRole
        })

        setDebugInfo(`API yanıtı alındı: ${JSON.stringify(result)}`)

        if (result.error) {
          setMessage(result.error)
        } else {
          setMessage('Kayıt başarılı! Admin onayını bekleyin. Şimdi giriş yapabilirsiniz.')
          setIsLogin(true)
          setFormData({
            username: '',
            password: '',
            fullName: '',
            requestedRole: 'STUDENT'
          })
        }
      }
    } catch (error) {
      console.error('Form submit error:', error)
      setMessage('Beklenmeyen bir hata oluştu: ' + error.message)
      setDebugInfo(`Hata detayı: ${error.stack}`)
    } finally {
      setLoading(false)
    }
  }

  const handleChange = (e) => {
    setFormData({
      ...formData,
      [e.target.name]: e.target.value
    })
  }

  return (
    <div className="auth-container">
      <div className="auth-form">
        <h2>{isLogin ? 'Giriş Yap' : 'Kayıt Ol'}</h2>

        {/* Debug Info */}
        {debugInfo && (
          <div style={{
            background: '#f8f9fa',
            padding: '10px',
            borderRadius: '4px',
            marginBottom: '1rem',
            fontSize: '12px',
            color: '#666'
          }}>
            <strong>Debug:</strong> {debugInfo}
          </div>
        )}

        <form onSubmit={handleSubmit}>
          <div className="form-group">
            <label>Kullanıcı Adı:</label>
            <input
              type="text"
              name="username"
              value={formData.username}
              onChange={(e) => setFormData({...formData, [e.target.name]: e.target.value})}
              required
            />
          </div>

          <div className="form-group">
            <label>Şifre:</label>
            <input
              type="password"
              name="password"
              value={formData.password}
              onChange={(e) => setFormData({...formData, [e.target.name]: e.target.value})}
              required
            />
          </div>

          {!isLogin && (
            <>
              <div className="form-group">
                <label>Ad Soyad:</label>
                <input
                  type="text"
                  name="fullName"
                  value={formData.fullName}
                  onChange={(e) => setFormData({...formData, [e.target.name]: e.target.value})}
                  required
                />
              </div>

              <div className="form-group">
                <label>Rol Talebi:</label>
                <select
                  name="requestedRole"
                  value={formData.requestedRole}
                  onChange={(e) => setFormData({...formData, [e.target.name]: e.target.value})}
                >
                  <option value="STUDENT">Öğrenci</option>
                  <option value="TEACHER">Öğretmen</option>
                </select>
              </div>
            </>
          )}

          <button type="submit" disabled={loading}>
            {loading ? 'İşleniyor...' : (isLogin ? 'Giriş Yap' : 'Kayıt Ol')}
          </button>
        </form>

        <p>
          {isLogin ? 'Hesabınız yok mu? ' : 'Zaten hesabınız var mı? '}
          <button
            type="button"
            className="link-button"
            onClick={() => {
              setIsLogin(!isLogin)
              setMessage('')
              setDebugInfo('')
              setFormData({
                username: '',
                password: '',
                fullName: '',
                requestedRole: 'STUDENT'
              })
            }}
          >
            {isLogin ? 'Kayıt Ol' : 'Giriş Yap'}
          </button>
        </p>

        {message && (
          <div className={`message ${message.includes('başarılı') ? 'success' : 'error'}`}>
            {message}
          </div>
        )}
      </div>
    </div>
  )
}
