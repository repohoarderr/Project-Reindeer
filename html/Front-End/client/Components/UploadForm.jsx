import React, { useRef, useState } from "react";
import apiService from "../services/apiService";

export default function UploadForm({ onUploadComplete }) {
  const fileInputRef = useRef(null);
  const [uploadStatus, setUploadStatus] = useState("");

  const handleUpload = async (event) => {
    event.preventDefault();

    const file = fileInputRef.current.files[0]; // Get the selected file

    if (!file) {
      setUploadStatus("No file selected");
      return;
    }

    try {
      // Upload file using apiService
      const result = await apiService.uploadFile(file);
      setUploadStatus('Upload successful'); // Set status message
      onUploadComplete(result); // Pass the result to the parent component
    } catch (error) {
      setUploadStatus(`Upload failed: ${error.message}`);
    }
  };

  const handleRemove = () => {
    fileInputRef.current.value = ""; // Clear the selected file
    setUploadStatus(""); // Reset the upload status
  };

  return (
    <form id="uploadForm" onSubmit={handleUpload}>
      <input
        type="file"
        id="fileInput"
        name="file"
        ref={fileInputRef}
        required
        accept=".dxf"
      />
      <div className="button-group">
        <button type="submit">Upload</button>
        <button type="button" className="remove-button" onClick={handleRemove}>
          Remove File
        </button>
      </div>
      {/* Display the current upload status */}
      {uploadStatus && <p>{uploadStatus}</p>}
    </form>
  );
}
