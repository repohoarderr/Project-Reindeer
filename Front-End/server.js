// Express as our HTTP server
import Express from "express";
import WebSocket from "ws";

// Make an express application
const app = new Express();

// Socket
function connectWebSocket() {
  const socket = new WebSocket("ws://localhost:8080");

  socket.on("open", () => {
    console.log("WebSocket connection established");
  });

  socket.on("error", (error) => {
    console.error("WebSocket error:", error);
    setTimeout(connectWebSocket, 5000); // Retry connection after 5 seconds
  });

  socket.on("close", () => {
    console.log("WebSocket connection closed, retrying...");
    setTimeout(connectWebSocket, 5000); // Retry connection after 5 seconds
  });
}

connectWebSocket();

// Static file server to serve index.html
app.use(Express.static("."));

// Start server listening on port 5000
app.listen(5000, () => {
  console.log("Listening on port 5000");
});
