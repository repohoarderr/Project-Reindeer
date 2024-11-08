// Import React and the useState hook to manage state.
import React, { useState } from "react";

// Import external CSS file for styling.
import "../public/styles.css";

// Import all React components used in the app.
import FileUploadForm from "./Components/UploadForm.jsx";
import DisplayResults from "./Components/DisplayResults.jsx";
import ManualFeatureSelection from "./Components/ManualFeatureSelection.jsx";
import VisualizeShapes from "./Components/VisualizeShapes.jsx";
import LogConsole from "./services/GroupShapes.js"

export default function App() {
    // Define a state variable to store the results of the file upload.
    const [uploadResults, setUploadResults] = useState("");

    // Function to handle the completion of the file upload.
    // It accepts the results returned from the backend and updates the state.
    const handleUploadComplete = (results) => {
        setUploadResults(results);
    };

    return (
        <div className="container">

            {/* Display a company logo at the top of the page. */}
            <img
                src="https://www.mathias-die.com/wp-content/uploads/2018/07/MDC-Logo-copy.png"
                alt="MDC Logo"
                className="centered-image"
            />

            <h1>Upload a File</h1>

            {/* Render the file upload form.
                The 'onUploadComplete' prop specifies the callback function to be executed when a file upload finishes. */}
            <FileUploadForm onUploadComplete={handleUploadComplete} />

            {/* Display the results from the file upload.
                This component reads the 'uploadResults' state and displays it to the user. */}
            <DisplayResults results={uploadResults} />

            {/* Render the manual feature selection component, if the user wants to manually select features after uploading a file. */}
            <ManualFeatureSelection />

            {/* Render the VisualizeShapes class iff uploadResults has data */}
            {uploadResults && (
                // Parse the upload results (assuming they're JSON) and pass them as 'shapesData' to the VisualizeShapes component.
                <VisualizeShapes shapesData={JSON.parse(uploadResults)} />
            )}

            {uploadResults && (
                <LogConsole results={uploadResults} />
            )}
        </div>
    );
}
