import express from 'express';
import bodyParser from 'body-parser';

const app = express();
app.use(bodyParser.json());

// In-memory storage for testing
let users: Record<string, any> = {};

// Create user
app.post('/api/users', (req, res) => {
  const { name, email, password } = req.body;
  const id = Date.now().toString();
  users[id] = { id, name, email, password, status: 'pending' };
  res.status(201).json({ id, name, email, status: 'pending' });
});

// Activate user
app.post('/api/users/:id/activate', (req, res) => {
  const { id } = req.params;
  const { activationCode } = req.body;
  
  if (!users[id]) {
    return res.status(404).json({ message: 'User not found' });
  }
  
  if (activationCode === '123456') {
    users[id].status = 'active';
    return res.status(200).json({ message: 'User activated successfully' });
  }
  
  res.status(400).json({ message: 'Invalid activation code' });
});

// Get user
app.get('/api/users/:id', (req, res) => {
  const { id } = req.params;
  const user = users[id];
  
  if (!user) {
    return res.status(404).json({ message: 'User not found' });
  }
  
  res.status(200).json(user);
});

// Update user
app.put('/api/users/:id', (req, res) => {
  const { id } = req.params;
  const { name, email, password } = req.body;
  
  if (!users[id]) {
    return res.status(404).json({ message: 'User not found' });
  }
  
  users[id] = { ...users[id], name, email, password };
  res.status(200).json(users[id]);
});

// Delete user
app.delete('/api/users/:id', (req, res) => {
  const { id } = req.params;
  
  if (!users[id]) {
    return res.status(404).json({ message: 'User not found' });
  }
  
  delete users[id];
  res.status(204).send();
});

const PORT = process.env.PORT || 3000;
app.listen(PORT, () => {
  console.log(`Mock API server running on port ${PORT}`);
}); 