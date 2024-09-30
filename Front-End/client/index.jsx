// Import react base and dom root utilities
import React from "react";
import { createRoot } from "react-dom/client";

// Import bootstrap for CSS bundling
import "bootstrap/dist/css/bootstrap.min.css";

// Root app component
import App from "./App.jsx";

// Once page is loaded, create react root and render the app
document.addEventListener("DOMContentLoaded", initApp);
function initApp() {
  const appRoot = createRoot(document.querySelector("#root"));
  appRoot.render(<App />);
}
