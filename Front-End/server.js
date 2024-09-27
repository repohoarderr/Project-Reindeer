// Express as our HTTP server
import Express from "express";
import WebSocket from "ws";

// Make an express application
const app = new Express();

// Static file server to serve index.html
app.use(Express.static("."));

// Start server listening on port 5000
app.listen(5000, () => {
  console.log("Listening on port 5000");
});
