const express = require('express');
const cors = require('cors');
const path = require('path'); 
const { Pool } = require('pg');

const app = express();
app.use(cors());
app.use(express.json());

// Serve static files from the "public" directory
app.use(express.static(path.join(__dirname, 'public')));

// Set up PostgreSQL connection pool with SSL disabled
const pool = new Pool({
  user: 'root',
  host: '0.0.0.0',
  database: 'bookly',
  port: 26257, 
  ssl: false // Disable SSL
});

// Endpoint to get all query tunings
app.get('/query-tunings', async (req, res) => {
  try {
    const result = await pool.query('SELECT * FROM tunings');
    res.json(result.rows);
  } catch (err) {
    console.error(err);
    res.status(500).send('Server error');
  }
});

// Endpoint to add a new query tuning
app.post('/query-tunings', async (req, res) => {
    const { query, execution_time, intervention } = req.body;
    try {
      // Check console to verify received data
      console.log('Received:', req.body);
  
      await pool.query(
        'INSERT INTO tunings (query, execution_time, intervention) VALUES ($1, $2, $3)',
        [query, execution_time, intervention]
      );
      res.send('Tuning added');
    } catch (err) {
      console.error(err);
      res.status(500).send('Server error');
    }
  });  

// Start the server and listen on all interfaces (0.0.0.0) and port 3000
const PORT = process.env.PORT || 3000;
const HOST = '0.0.0.0';  // Listen on all network interfaces
app.listen(PORT, HOST, () => {
  console.log(`Server running on http://${HOST}:${PORT}`);
});
