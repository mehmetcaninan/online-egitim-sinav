import React, { useState, useEffect } from 'react'

export default function BackendStatus() {
  const [status, setStatus] = useState('checking')
  const [details, setDetails] = useState('')

  useEffect(() => {
    checkBackendStatus()
  }, [])

  const checkBackendStatus = async () => {
    setStatus('checking')
    setDetails('Backend bağlantısı kontrol ediliyor...')

    try {
      // Önce yeni health endpoint'ini test et
      const healthResponse = await fetch('http://localhost:8080/api/health', {
        method: 'GET',
        headers: { 'Accept': 'application/json' },
        mode: 'cors'
      })

      if (healthResponse.ok) {
        try {
          const data = await healthResponse.json()
          setStatus('connected')
          setDetails(`✅ Backend aktif: ${data.message} - Port: ${data.port}`)
          return
        } catch (e) {
          setStatus('connected')
          setDetails('✅ Backend aktif (Health endpoint erişilebilir)')
          return
        }
      }

      // Eğer health çalışmazsa courses endpoint'ini dene
      const coursesResponse = await fetch('http://localhost:8080/api/courses', {
        method: 'GET',
        headers: { 'Accept': 'application/json' },
        mode: 'cors'
      })

      if (coursesResponse.ok || coursesResponse.status === 401 || coursesResponse.status === 403) {
        setStatus('connected')
        setDetails(`✅ Backend çalışıyor (${coursesResponse.status}) - API erişilebilir`)
        return
      }

      setStatus('error')
      setDetails(`⚠️ Backend yanıt verdi ama beklenmeyen status: ${coursesResponse.status}`)

    } catch (error) {
      console.error('Backend connection test failed:', error)
      setStatus('disconnected')

      if (error.message.includes('Load failed') || error.message.includes('Failed to fetch')) {
        setDetails('❌ Backend bağlantı hatası - Sunucu erişilemiyor')
      } else {
        setDetails(`❌ Bağlantı hatası: ${error.message}`)
      }
    }
  }

  const getStatusColor = () => {
    switch (status) {
      case 'connected': return '#28a745'
      case 'disconnected': return '#dc3545'
      case 'error': return '#ffc107'
      default: return '#6c757d'
    }
  }

  const getStatusText = () => {
    switch (status) {
      case 'connected': return '✅ Bağlı'
      case 'disconnected': return '❌ Bağlantı Yok'
      case 'error': return '⚠️ Hata'
      default: return '🔄 Kontrol Ediliyor...'
    }
  }

  return (
    <div style={{
      position: 'fixed',
      top: '10px',
      right: '10px',
      background: 'white',
      padding: '8px 12px',
      border: `2px solid ${getStatusColor()}`,
      borderRadius: '8px',
      fontSize: '11px',
      maxWidth: '200px', // Daha küçük yap
      zIndex: 1000,
      boxShadow: '0 2px 10px rgba(0,0,0,0.1)',
      cursor: 'pointer'
    }}
    onClick={() => {
      // Tıklandığında detayları göster/gizle
      const detailsElement = document.getElementById('backend-status-details')
      if (detailsElement) {
        detailsElement.style.display = detailsElement.style.display === 'none' ? 'block' : 'none'
      }
    }}
    >
      <div style={{ fontWeight: 'bold', color: getStatusColor() }}>
        Backend: {getStatusText()}
      </div>
      <div id="backend-status-details" style={{
        marginTop: '5px',
        color: '#666',
        display: 'none', // Başlangıçta gizli
        fontSize: '10px'
      }}>
        {details}
      </div>

      {/* Minimize/Close buttons */}
      <div style={{
        position: 'absolute',
        top: '2px',
        right: '2px',
        display: 'flex',
        gap: '2px'
      }}>
        <button
          onClick={(e) => {
            e.stopPropagation()
            checkBackendStatus()
          }}
          style={{
            background: 'none',
            border: 'none',
            cursor: 'pointer',
            fontSize: '10px',
            padding: '0 2px',
            color: getStatusColor()
          }}
          title="Yenile"
        >
          ↻
        </button>
        <button
          onClick={(e) => {
            e.stopPropagation()
            e.target.closest('[data-backend-status]').style.display = 'none'
          }}
          style={{
            background: 'none',
            border: 'none',
            cursor: 'pointer',
            fontSize: '10px',
            padding: '0 2px',
            color: '#999'
          }}
          title="Gizle"
        >
          ×
        </button>
      </div>
    </div>
  )
}
