import Express from "express";
import router from "./router.js"; // Import the router

const app = Express();

// Middleware to serve static files
app.use(Express.static("public"));

// Use the defined router for API requests
app.use("/api", router);

// Start server on port 5678
app.listen(5678, () => {
  console.log("Server listening on port 5678");
});
