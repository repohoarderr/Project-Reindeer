import Express from "express";

const app = Express();

// Middleware to serve static files from the "public" directory
app.use(Express.static("public"));

// If there are any other API endpoints, define them here (if necessary)

// Start server on port 5678
app.listen(5678, () => {
  console.log("Server listening on port 5678");
});
