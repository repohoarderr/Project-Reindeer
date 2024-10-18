import React, { useState } from "react";
import "../public/styles.css";
import FileUploadForm from "./Components/UploadForm.jsx";
import DisplayResults from "./Components/DisplayResults.jsx";
import ManualFeatureSelection from "./Components/ManualFeatureSelection.jsx";
import VisualizeShapes from "./Components/VisualizeShapes.jsx";

export default function App() {
  const [uploadResults, setUploadResults] = useState("");

  //UPDATE
  const [isPanelVisible, setPanelVisible] = useState(false); //Panel visibility state


  // Callback to handle when upload is complete
  const handleUploadComplete = (results) => {
    setUploadResults(results); // Set the results received from the backend
  };

  //UPDATE
  const togglePanel = () => {
    setPanelVisible(!isPanelVisible); //Toggle panel visibility
  };

  return (
    <div className={`page-layout ${isPanelVisible ? "panel-open" : ""}`}>
      {/*Button to toggle the panel*/}
      <button className="panel-toggle-button" onClick={togglePanel}>
        <i className="fas fa-shapes"></i>  {/*Using Font Awesome shapes icon*/}
      </button>


      {/*Main content container*/}
      <div className="main-content">
        <img
          src="https://www.mathias-die.com/wp-content/uploads/2018/07/MDC-Logo-copy.png"
          alt="MDC Logo"
          className="centered-image"
        />
        <h1>Upload a File</h1>
        <FileUploadForm onUploadComplete={handleUploadComplete} />
        <DisplayResults results={uploadResults} />
        <ManualFeatureSelection />

        {uploadResults && (
          <VisualizeShapes shapesData={JSON.parse(uploadResults)} />
        )}
      </div>

      {/*Shape Selection Panel*/}
      <div className={`shape-panel ${isPanelVisible ? "show" : "hide"}`}>
        <h2>Select a Shape</h2>
        <div className="shape-selection">
          <div className="mock-shape circle"></div>
          <div className="mock-shape square"></div>
          <div className="mock-shape triangle"></div>
        </div>
      </div>
    </div>
  );
}
