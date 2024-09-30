import React, { useRef } from "react";

export default function FileUploadForm({ onUploadComplete }) {
  const fileInputRef = useRef(null);

  const handleUpload = async (event) => {
    event.preventDefault();

    alert("File upload functionality is currently disabled.");
  };

  const handleRemove = () => {
    fileInputRef.current.value = ""; // Clear the selected file
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
    </form>
  );
}
