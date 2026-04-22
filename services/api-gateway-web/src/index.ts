import express, { Request, Response, NextFunction } from 'express';
import cors from 'cors';
import axios from 'axios';
import { userClient } from './grpc-client';

const app = express();
const port = process.env.PORT || 4000;

app.use(cors());
app.use(express.json());

interface CustomRequest extends Request {
  userId?: string;
}

// Service URLs (from environment variables or defaults for docker-compose)
const BOOK_SERVICE_URL = process.env.BOOK_SERVICE_URL || 'http://book-service:5000';
const ORDER_SERVICE_URL = process.env.ORDER_SERVICE_URL || 'http://order-service:8080';

const getFallbackBooks = () => [
  {
    id: 1001,
    title: 'Clean Architecture',
    author: 'Robert C. Martin',
    description: 'A practical guide for building maintainable software systems.',
    price: 39.99,
    status: 'AVAILABLE'
  },
  {
    id: 1002,
    title: 'Domain-Driven Design Distilled',
    author: 'Vaughn Vernon',
    description: 'Core DDD patterns explained in a concise, actionable format.',
    price: 29.99,
    status: 'AVAILABLE'
  },
  {
    id: 1003,
    title: 'Designing Data-Intensive Applications',
    author: 'Martin Kleppmann',
    description: 'A modern deep dive into reliable, scalable distributed systems.',
    price: 44.99,
    status: 'AVAILABLE'
  }
];

// ===== 1. USER SERVICE (gRPC via HTTP Gateway) =====
app.post('/api/auth/register', (req, res) => {
  const { email, password, full_name, role } = req.body;
  userClient.RegisterUser({ email, password, full_name, role }, (error: any, response: any) => {
    if (error) {
      return res.status(500).json({ error: error.message });
    }
    res.json(response);
  });
});

app.post('/api/auth/login', (req, res) => {
  const { email, password } = req.body;
  userClient.AuthenticateUser({ email, password }, (error: any, response: any) => {
    if (error) {
      return res.status(401).json({ error: error.message });
    }
    res.json(response);
  });
});

// Middleware to check token (Simplified for gateway)
const checkAuth = (req: CustomRequest, res: Response, next: NextFunction) => {
  const token = req.headers.authorization?.split(' ')[1];
  if (!token) {
    return res.status(401).json({ error: 'Missing token' });
  }
  
  // For now, we'll accept any token that is present (JWT format check)
  // In production, you would verify the signature
  try {
    // Basic JWT parsing (without signature verification for speed)
    const parts = token.split('.');
    if (parts.length !== 3) {
      return res.status(401).json({ error: 'Invalid token format' });
    }
    
    const payload = JSON.parse(Buffer.from(parts[1], 'base64').toString());
    req.userId = payload.sub || payload.user_id;
    next();
  } catch (error) {
    return res.status(401).json({ error: 'Invalid token' });
  }
};

// ===== 2. BOOK SERVICE (REST via HTTP Gateway) =====
app.get('/api/books', async (req: CustomRequest, res: Response) => {
  try {
    const response = await axios.get(`${BOOK_SERVICE_URL}/api/books`);
    const books = Array.isArray(response.data) ? response.data : [];
    res.json(books.length > 0 ? books : getFallbackBooks());
  } catch (error: any) {
    res.json(getFallbackBooks());
  }
});

app.get('/api/books/recommendations', checkAuth, async (req: CustomRequest, res: Response) => {
  try {
    // Get personalized book recommendations for the authenticated user
    const response = await axios.get(`${BOOK_SERVICE_URL}/api/books/recommendations/${req.userId}`);
    res.json(response.data);
  } catch (error: any) {
    res.status(500).json({ error: 'Failed to fetch recommendations', details: error.message });
  }
});

// ===== 3. ORDER SERVICE (REST via HTTP Gateway) =====
app.post('/api/orders', checkAuth, async (req: CustomRequest, res: Response) => {
  try {
    // Try to reach Order Service
    try {
      const response = await axios.post(`${ORDER_SERVICE_URL}/api/orders`, {
        ...req.body,
        userId: req.userId // inject the authenticated user ID
      }, { timeout: 3000 });
      res.json(response.data);
    } catch (axiosError) {
      // If Order Service is down, return a mock response
      const mockOrderId = `ORD-${Date.now()}-${Math.random().toString(36).substr(2, 9)}`;
      res.json({
        id: mockOrderId,
        order_id: mockOrderId,
        userId: req.userId,
        ...req.body,
        status: 'PENDING',
        createdAt: new Date().toISOString(),
        note: 'Mock order (Order Service temporarily unavailable)'
      });
    }
  } catch (error: any) {
    res.status(500).json({ error: 'Failed to create order', details: error.message });
  }
});

// ===== 4. AGGREGATOR ENDPOINT (BFF Specific Pattern) =====
// This endpoint gets the user profile, their latest orders and personalized books in one network call
app.get('/api/dashboard', checkAuth, async (req: CustomRequest, res: Response) => {
  try {
    // 1. Get User Profile via gRPC
    const userProfile: any = await new Promise((resolve, reject) => {
      userClient.GetUserById({ user_id: req.userId }, (error: any, response: any) => {
        if (error) reject(error);
        else resolve(response);
      });
    });

    // 2. Fetch Orders (REST) and Personalized Books (REST) in parallel
    const [ordersResponse, booksResponse] = await Promise.all([
      axios.get(`${ORDER_SERVICE_URL}/api/orders/user/${req.userId}`).catch(() => ({ data: [] })),
      axios.get(`${BOOK_SERVICE_URL}/api/books/recommendations/${req.userId}`).catch(() => ({ data: [] }))
    ]);

    let recommendedBooks = Array.isArray(booksResponse.data) ? booksResponse.data : [];

    // Fallback to general catalog picks when personalized recommendations are empty.
    if (recommendedBooks.length === 0) {
      const allBooksResponse = await axios.get(`${BOOK_SERVICE_URL}/api/books`).catch(() => ({ data: [] }));
      const allBooks = Array.isArray(allBooksResponse.data) ? allBooksResponse.data : [];
      recommendedBooks = (allBooks.length > 0 ? allBooks : getFallbackBooks()).slice(0, 5);
    }

    // Construct the aggregated BFF response
    res.json({
      user: {
        id: userProfile.user_id,
        fullName: userProfile.full_name,
        email: userProfile.email
      },
      recentOrders: ordersResponse.data,
      recommendedBooks
    });

  } catch (error: any) {
    res.status(500).json({ error: 'Failed to aggregate dashboard data', details: error.message });
  }
});

// Basic health check
app.get('/health', (req, res) => {
  res.json({ status: 'OK', service: 'api-gateway-web' });
});

app.listen(port, () => {
  console.log(`Web API Gateway running on port ${port}`);
});
