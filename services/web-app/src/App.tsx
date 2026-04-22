import { useState, useEffect } from 'react'
import './App.css'
import axios from 'axios'

const API_GATEWAY = 'http://localhost:4000'

interface Book {
  id: number
  title: string
  author: string
  description: string
  price: number
  status: string
}

interface DashboardData {
  user: { id: string; fullName: string; email: string }
  recentOrders: any[]
  recommendedBooks: any[]
}

function App() {
  const [page, setPage] = useState<'login' | 'register' | 'catalog' | 'dashboard'>('login')
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [fullName, setFullName] = useState('')
  const [role, setRole] = useState('READER')
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)
  const [token, setToken] = useState(localStorage.getItem('accessToken') || '')
  const [books, setBooks] = useState<Book[]>([])
  const [dashboardData, setDashboardData] = useState<DashboardData | null>(null)
  const [booksLoading, setBooksLoading] = useState(false)
  const [dashboardLoading, setDashboardLoading] = useState(false)
  const [selectedBook, setSelectedBook] = useState<any>(null)
  const [quantity, setQuantity] = useState(1)
  const [orderLoading, setOrderLoading] = useState(false)


  const api = axios.create({
    baseURL: API_GATEWAY,
    headers: { 'Content-Type': 'application/json' }
  })

  api.interceptors.request.use(config => {
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  })

  const handleRegister = async (e: React.FormEvent) => {
    e.preventDefault()
    setLoading(true)
    setError('')
    try {
      await api.post('/api/auth/register', { email, password, full_name: fullName, role })
      alert('✅ Registration successful! Please login.')
      setPage('login')
      setEmail('')
      setPassword('')
      setFullName('')
    } catch (err: any) {
      setError(err.response?.data?.error || 'Registration failed')
    } finally {
      setLoading(false)
    }
  }

  const handleLogin = async (e: React.FormEvent) => {
    e.preventDefault()
    setLoading(true)
    setError('')
    try {
      const res = await api.post('/api/auth/login', { email, password })
      const newToken = res.data.access_token
      setToken(newToken)
      localStorage.setItem('accessToken', newToken)
      setPage('catalog')
      setEmail('')
      setPassword('')
    } catch (err: any) {
      setError(err.response?.data?.error || 'Login failed')
    } finally {
      setLoading(false)
    }
  }

  const loadBooks = async () => {
    setBooksLoading(true)
    try {
      const res = await api.get('/api/books')
      setBooks(Array.isArray(res.data) ? res.data : [])
    } catch (err) {
      setBooks([])
      setError('Failed to load books')
    } finally {
      setBooksLoading(false)
    }
  }

  const loadDashboard = async () => {
    setDashboardLoading(true)
    try {
      const res = await api.get('/api/dashboard')
      setDashboardData(res.data)
    } catch (err) {
      setDashboardData(null)
      setError('Failed to load dashboard')
    } finally {
      setDashboardLoading(false)
    }
  }

  const handleAddOrder = async () => {
    if (!selectedBook) return
    setOrderLoading(true)
    setError('')
    try {
      const res = await api.post('/api/orders', {
        bookId: selectedBook.id,
        bookTitle: selectedBook.title,
        quantity: quantity,
        price: selectedBook.price
      })
      alert(`✅ Order created successfully! Order ID: ${res.data.id || res.data.order_id || 'pending'}`)
      setSelectedBook(null)
      setQuantity(1)
      setPage('dashboard')
    } catch (err: any) {
      setError(err.response?.data?.error || 'Failed to create order')
    } finally {
      setOrderLoading(false)
    }
  }

  useEffect(() => {
    if (page === 'catalog') loadBooks()
  }, [page])

  useEffect(() => {
    if (page === 'dashboard') loadDashboard()
  }, [page])

  const isAuthenticated = !!token

  return (
    <div className="app">
      <nav className="navbar">
        <div className="logo">📖 Book Landing System</div>
        <div className="nav-links">
          {isAuthenticated ? (
            <>
              <button onClick={() => setPage('dashboard')} className={page === 'dashboard' ? 'active' : ''}>
                📊 Dashboard
              </button>
              <button onClick={() => setPage('catalog')} className={page === 'catalog' ? 'active' : ''}>
                📚 Books
              </button>
              <button onClick={() => {
                setToken('')
                localStorage.removeItem('accessToken')
                setPage('login')
              }}>
                🚪 Logout
              </button>
            </>
          ) : (
            <>
              <button onClick={() => setPage('login')} className={page === 'login' ? 'active' : ''}>
                🔐 Login
              </button>
              <button onClick={() => setPage('register')} className={page === 'register' ? 'active' : ''}>
                ✍️ Register
              </button>
            </>
          )}
        </div>
      </nav>

      <main className="main-content">
        {/* LOGIN PAGE */}
        {page === 'login' && (
          <div className="auth-container">
            <form onSubmit={handleLogin} className="auth-form">
              <h2>🔐 Login</h2>
              {error && <div className="error-alert">{error}</div>}
              <input
                type="email"
                placeholder="Email"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                required
                className="input-field"
              />
              <input
                type="password"
                placeholder="Password"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                required
                className="input-field"
              />
              <button type="submit" disabled={loading} className="btn-primary">
                {loading ? '⏳ Logging in...' : '✅ Login'}
              </button>
              <p className="toggle-link">
                Don't have an account? <a onClick={() => setPage('register')}>Register here</a>
              </p>
            </form>
          </div>
        )}

        {/* REGISTER PAGE */}
        {page === 'register' && (
          <div className="auth-container">
            <form onSubmit={handleRegister} className="auth-form">
              <h2>✍️ Create Account</h2>
              {error && <div className="error-alert">{error}</div>}
              <input
                type="text"
                placeholder="Full Name"
                value={fullName}
                onChange={(e) => setFullName(e.target.value)}
                required
                className="input-field"
              />
              <input
                type="email"
                placeholder="Email"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                required
                className="input-field"
              />
              <input
                type="password"
                placeholder="Password"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                required
                className="input-field"
              />
              <select value={role} onChange={(e) => setRole(e.target.value)} className="input-field">
                <option value="READER">👤 Reader</option>
                <option value="AUTHOR">✍️ Author</option>
                <option value="ADMIN">⚙️ Admin</option>
              </select>
              <button type="submit" disabled={loading} className="btn-primary">
                {loading ? '⏳ Creating...' : '✅ Register'}
              </button>
              <p className="toggle-link">
                Already have an account? <a onClick={() => setPage('login')}>Login here</a>
              </p>
            </form>
          </div>
        )}

        {/* CATALOG PAGE */}
        {page === 'catalog' && (
          <div className="catalog-container">
            <h1>📚 Book Catalog</h1>
            {error && <div className="error-alert">{error}</div>}
            
            {selectedBook && (
              <div style={{ 
                position: 'fixed', 
                top: 0, 
                left: 0, 
                right: 0, 
                bottom: 0, 
                backgroundColor: 'rgba(0,0,0,0.5)', 
                display: 'flex', 
                justifyContent: 'center', 
                alignItems: 'center',
                zIndex: 1000
              }}>
                <div style={{ 
                  backgroundColor: 'white', 
                  padding: '2rem', 
                  borderRadius: '12px',
                  maxWidth: '400px',
                  width: '90%'
                }}>
                  <h2>Create Order</h2>
                  <p><strong>Book:</strong> {selectedBook.title}</p>
                  <p><strong>Author:</strong> {selectedBook.author}</p>
                  <p><strong>Price:</strong> ${selectedBook.price.toFixed(2)}</p>
                  <div style={{ marginTop: '1rem' }}>
                    <label>Quantity: </label>
                    <input 
                      type="number" 
                      min="1" 
                      max="100"
                      value={quantity}
                      onChange={(e) => setQuantity(parseInt(e.target.value) || 1)}
                      className="input-field"
                      style={{ marginTop: '0.5rem' }}
                    />
                  </div>
                  <div style={{ marginTop: '1rem', display: 'flex', gap: '1rem' }}>
                    <button 
                      onClick={handleAddOrder} 
                      disabled={orderLoading}
                      className="btn-primary"
                      style={{ flex: 1 }}
                    >
                      {orderLoading ? '⏳ Processing...' : '✅ Confirm Order'}
                    </button>
                    <button 
                      onClick={() => { setSelectedBook(null); setQuantity(1) }}
                      style={{ 
                        flex: 1,
                        padding: '0.75rem',
                        backgroundColor: '#ccc',
                        border: 'none',
                        borderRadius: '6px',
                        cursor: 'pointer'
                      }}
                    >
                      ❌ Cancel
                    </button>
                  </div>
                </div>
              </div>
            )}
            
            <div className="books-grid">
              {booksLoading ? (
                <p>⏳ Loading books...</p>
              ) : books.length > 0 ? (
                books.map((book) => (
                  <div key={book.id} className="book-card">
                    <h3>{book.title}</h3>
                    <p className="author">by {book.author}</p>
                    <p className="description">{book.description}</p>
                    <div className="book-footer">
                      <span className="price">${book.price.toFixed(2)}</span>
                      <span className={`status ${book.status.toLowerCase()}`}>{book.status}</span>
                    </div>
                    <button 
                      className="btn-order"
                      onClick={() => setSelectedBook(book)}
                    >
                      🛒 Create Order
                    </button>
                  </div>
                ))
              ) : (
                <p>📭 No books available right now</p>
              )}
            </div>
          </div>
        )}

        {/* DASHBOARD PAGE */}
        {page === 'dashboard' && (
          <div className="dashboard-container">
            <h1>📊 Dashboard</h1>
            {error && <div className="error-alert">{error}</div>}
            {dashboardLoading && !dashboardData ? (
              <p>⏳ Loading dashboard...</p>
            ) : dashboardData ? (
              <>
                <section className="section">
                  <h2>👤 Profile</h2>
                  <p><strong>Name:</strong> {dashboardData.user.fullName}</p>
                  <p><strong>Email:</strong> {dashboardData.user.email}</p>
                </section>

                <section className="section">
                  <h2>📦 Recent Orders ({dashboardData.recentOrders.length})</h2>
                  {dashboardData.recentOrders.length > 0 ? (
                    <table className="orders-table">
                      <thead>
                        <tr>
                          <th>Order ID</th>
                          <th>Book</th>
                          <th>Quantity</th>
                          <th>Status</th>
                        </tr>
                      </thead>
                      <tbody>
                        {dashboardData.recentOrders.map((order) => (
                          <tr key={order.id}>
                            <td>{order.id || 'N/A'}</td>
                            <td>{order.bookTitle || 'N/A'}</td>
                            <td>{order.quantity || 1}</td>
                            <td><span className="status-badge">{order.status || 'PENDING'}</span></td>
                          </tr>
                        ))}
                      </tbody>
                    </table>
                  ) : (
                    <p>📭 No orders yet</p>
                  )}
                </section>

                <section className="section">
                  <h2>⭐ Recommended Books ({dashboardData.recommendedBooks.length})</h2>
                  <div className="books-carousel">
                    {dashboardData.recommendedBooks.length > 0 ? (
                      dashboardData.recommendedBooks.map((book) => (
                        <div key={book.id} className="carousel-item">
                          <h4>{book.title}</h4>
                          <p className="author">{book.author}</p>
                          <p className="price">${book.price}</p>
                        </div>
                      ))
                    ) : (
                      <p>📭 No recommendations yet. Create an order to get personalized suggestions.</p>
                    )}
                  </div>
                </section>
              </>
            ) : (
              <p>📭 No dashboard data available</p>
            )}
          </div>
        )}
      </main>
    </div>
  )
}

export default App
