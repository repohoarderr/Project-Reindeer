// Express as our HTTP server
import Express from "express";

// Make an express application
const app = new Express();

// Import the router
import dataRouter from "./router.js";

// Static file server to serve index.html
app.use(Express.static("public"));

app.use("/data", dataRouter);

// Start server listening on port 5000
app.listen(5678, () => {
  console.log("Listening on port 5678");
});
