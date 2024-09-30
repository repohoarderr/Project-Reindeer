import React from "react";
import { createRoot } from "react-dom/client";

// Import bootstrap for CSS bundling
import "bootstrap/dist/css/bootstrap.min.css";

// Root app component
import App from "./App.jsx";

// Ensure DOM content is fully loaded before rendering React app
document.addEventListener("DOMContentLoaded", () => {
  const rootElement = document.getElementById("root");

  if (rootElement) {
    const root = createRoot(rootElement);
    root.render(<App />);
  } else {
    console.error("Root element not found");
  }
});
