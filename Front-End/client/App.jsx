import React from "react";
import "../public/styles.css";
import FileUploadForm from "./Components/UploadForm.jsx";
import { IconLucideBookOpenText } from "./Components/BookIcon";

export default function App() {
  return (
    <div className="container">
      <img
        src="https://www.mathias-die.com/wp-content/uploads/2018/07/MDC-Logo-copy.png"
        alt="MDC Logo"
        className="centered-image"
      />
      <h1>Upload a File</h1>
      <FileUploadForm />

      {/* User's Manual Button */}
      <button className="manual-button" onClick={() => window.open("/path-to-manual", "_blank")}>
        <IconLucideBookOpenText />
      </button>
    </div>
  );
}
