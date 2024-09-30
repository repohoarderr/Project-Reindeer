import React from "react";
import "../public/styles.css";
import FileUploadForm from "./Components/UploadForm.jsx";

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
    </div>
  );
}
