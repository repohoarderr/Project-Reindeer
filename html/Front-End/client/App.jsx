import React, { useState } from "react";
import "../public/styles.css";
import FileUploadForm from "./Components/UploadForm.jsx";
import DisplayResults from "./Components/DisplayResults.jsx";

export default function App() {
  const [uploadResults, setUploadResults] = useState("");

  // Callback to handle when upload is complete
  const handleUploadComplete = (results) => {
    setUploadResults(results); // Set the results received from the backend
  };

  return (
    <div className="container">
      <img
        src="https://www.mathias-die.com/wp-content/uploads/2018/07/MDC-Logo-copy.png"
        alt="MDC Logo"
        className="centered-image"
      />
      <h1>Upload a File</h1>
      <FileUploadForm onUploadComplete={handleUploadComplete} />
      <DisplayResults results={uploadResults} /> {/* Display results here */}
    </div>
  );
}
