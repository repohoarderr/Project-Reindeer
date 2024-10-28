import React, { useRef, useState } from "react";
import apiService from "../services/apiService";

/**
 * UploadForm component allows the user to select a .dxf file, upload it, and remove the file selection.
 * It handles file upload and provides feedback on the status of the upload.
 *
 * @param {function} onUploadComplete - A callback function passed from the parent component,
 *                                      invoked when the file upload is successfully completed.
 */
export default function UploadForm({ onUploadComplete }) {
  // useRef is used to access the file input element to read the selected file
  const fileInputRef = useRef(null);

  // useState hook to manage the status of the file upload (e.g., successful or failed)
  const [uploadStatus, setUploadStatus] = useState("");

  /**
   * Handles the file upload process when the user submits the form.
   * It prevents the default form behavior, retrieves the selected file, and sends it via the API service.
   * If no file is selected, it provides feedback to the user.
   *
   * @param {Event} event - The event object generated when the form is submitted.
   */
  const handleUpload = async (event) => {
    event.preventDefault(); // Prevent the default form submission behavior

    // Access the file selected in the file input field
    const file = fileInputRef.current.files[0];

    // If no file is selected, set an error message and exit the function
    if (!file) {
      setUploadStatus("No file selected");
      return;
    }

    try {
      // Use the apiService to handle the file upload, passing the selected file
      const result = await apiService.uploadFile(file);

      // If successful, update the status to inform the user and invoke the callback to notify the parent component
      setUploadStatus('Upload successful');
      onUploadComplete(result); // Pass the upload result to the parent component (App.js)
    } catch (error) {
      // If there is an error during the upload process, display an error message
      setUploadStatus(`Upload failed: ${error.message}`);
    }
  };

  /**
   * Resets the file input and clears the current upload status when the user clicks the "Remove File" button.
   */
  const handleRemove = () => {
    fileInputRef.current.value = ""; // Clear the value of the file input field (remove the file selection)
    setUploadStatus(""); // Reset the upload status message
  };

  return (
      <form id="uploadForm" onSubmit={handleUpload}>
        {/* Input field to allow users to select a file. It only accepts .dxf files. */}
        <input
            type="file"
            id="fileInput"
            name="file"
            ref={fileInputRef} // Reference to the file input element
            required
            accept=".dxf" // Restrict file types to .dxf
        />

        {/* Buttons to upload the selected file or remove the selection */}
        <div className="button-group">
          <button type="submit">Upload</button> {/* Submit button to trigger file upload */}
          <button type="button" className="remove-button" onClick={handleRemove}>
            Remove File {/* Button to clear the selected file */}
          </button>
        </div>

        {/* Display the upload status to provide feedback to the user */}
        {uploadStatus && <p>{uploadStatus}</p>}
      </form>
  );
}
