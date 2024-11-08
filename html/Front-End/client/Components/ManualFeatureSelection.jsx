import React, { useState } from "react";

export default function ManualFeatureSelection() {
  //Ensure the state and toggle function are correctly initialized
  const [isVisible, setIsVisible] = useState(false);
  const [isManualVisible, setIsManualVisible] = useState(false); // State for user's manual

  //State to keep track of the active manual section
  const [activeSection, setActiveSection] = useState("introduction");

    //Function to set the active manual section
    const changeSection = (section) => {
      setActiveSection(section);
    };

    //Function to toggle panel visibility
    const togglePanel = () => {
        setIsVisible(!isVisible);
    };

    //Function to toggle the user's manual visibility
    const toggleManual = () => {
        setIsManualVisible(!isManualVisible);
    };


  //Button click handler
  const selectFeature = (event) => {
    switch (event.target.innerText) {
      case "1A":
        console.log("Good job clicking the first button!!!");
        break;
      case "1B":
        break;
      case "1C":
        break;
      case "2A":
        break;
      case "3":
        break;
      case "4":
        break;
      case "6":
        break;
      case "7":
        break;
      case "8":
        break;
      case "9":
        break;
      case "11":
        break;
      case "12":
        break;
      case "13":
        break;
      case "14":
        break;
      case "15":
        break;
      case "17":
        break;
      case "S1":
        break;
      case "S2":
        break;
      case "16":
        break;
      default:
        console.log("what did you click???"); // TODO: Add error handling logic here.
    }
  };

  return (
    <div className="fade-out-panel-container">

      {/* Toggle button placed at the top-right corner */}
      <button className="toggle-button" onClick={togglePanel}>
      <i className="fas fa-shapes"></i> {/* Use the Shapes icon */}
      </button>

      <div className={`fade-out-panel ${isVisible ? "visible" : "hidden"}`}>

        {/* Feature buttons */}
        <button className="feature-button">
        <img src="/icons/Grp1.png" alt="1A thumbnail" className="thumbnail" />
        </button>

        <button className="feature-button">
        <img src="/icons/Grp2A.png" alt="1B thumbnail" className="thumbnail" />
        </button>

        <button className="feature-button">
        <img src="/icons/Grp3.png" alt="1C thumbnail" className="thumbnail" />
        </button>

        <button className="feature-button" onClick={selectFeature}>2A</button>
        <button className="feature-button" onClick={selectFeature}>3</button>
        <button className="feature-button" onClick={selectFeature}>4</button>
        <button className="feature-button" onClick={selectFeature}>6</button>
        <button className="feature-button" onClick={selectFeature}>7</button>
        <button className="feature-button" onClick={selectFeature}>8</button>
        <button className="feature-button" onClick={selectFeature}>9</button>
        <button className="feature-button" onClick={selectFeature}>11</button>
        <button className="feature-button" onClick={selectFeature}>12</button>
        <button className="feature-button" onClick={selectFeature}>13</button>
        <button className="feature-button" onClick={selectFeature}>14</button>
        <button className="feature-button" onClick={selectFeature}>15</button>
        <button className="feature-button" onClick={selectFeature}>17</button>
        <button className="feature-button" onClick={selectFeature}>S1</button>
        <button className="feature-button" onClick={selectFeature}>S2</button>
        <button className="feature-button" onClick={selectFeature}>16</button>
      </div>

      {/* User's manual book icon at the bottom-right corner */}
      <button className="manual-button" onClick={toggleManual}>
        <i className="fas fa-book"></i>
      </button>

      {/* User manual with sidebar navigation and content sections */}
      {isManualVisible && (
        <div className="manual-dialog-modern">

          {/* Close button in the top-right corner */}
          <button className="close-button" onClick={() => setIsManualVisible(false)}>
            &times;
          </button>

          <div className="manual-sidebar">
            <h3>User Manual</h3>
            <ul>
              <li onClick={() => changeSection("introduction")}>Introduction</li>
              <li onClick={() => changeSection("getting-started")}>Getting Started</li>
              <li onClick={() => changeSection("features")}>Features</li>
              <li onClick={() => changeSection("guide")}>Step-by-Step Guide</li>
              <li onClick={() => changeSection("troubleshooting")}>Troubleshooting</li>
              <li onClick={() => changeSection("faqs")}>FAQs</li>
              <li onClick={() => changeSection("support")}>Contact Support</li>
            </ul>
          </div>
          <div className="manual-content">
            {activeSection === "introduction" && (
              <div>
                <h2>Introduction</h2>
                <p>Welcome to the File Upload and Shape Visualization App! This app allows users to upload .dxf files, view structured results, and visualize shapes.</p>
              </div>
            )}
            {activeSection === "getting-started" && (
              <div>
                <h2>Getting Started</h2>
                <p><b>System Requirements</b>: Web browser with JavaScript enabled.</p>
                <p><b>Accessing the App</b>: Open the app URL in your web browser.</p>
              </div>
            )}
            {activeSection === "features" && (
              <div>
                <h2>Features</h2>
                <h3>Uploading a File</h3>
                <p>Click on the "Upload" button to select and upload a .dxf file.</p>
                <h3>Viewing Upload Results</h3>
                <p>After uploading, view results in the tree table format under "Results."</p>
                <h3>Selecting Features</h3>
                <p>Open the panel by clicking the Shapes icon and choose from various features.</p>
                <h3>Visualizing Shapes</h3>
                <p>Shapes from the file will display on the canvas after upload.</p>
              </div>
            )}
            {activeSection === "guide" && (
              <div>
                <h2>Step-by-Step Guide</h2>
                <h3>Step 1: Uploading a File</h3>
                <p>Select a .dxf file and click "Upload." Look for a confirmation message.</p>
                <h3>Step 2: Viewing Results</h3>
                <p>Results will display in a structured format if the upload is successful.</p>
                <h3>Step 3: Using the Feature Selection Panel</h3>
                <p>Click the Shapes icon, select desired features, and click again to close.</p>
                <h3>Step 4: Visualizing Shapes</h3>
                <p>The canvas will display shapes extracted from your .dxf file.</p>
              </div>
            )}
            {activeSection === "troubleshooting" && (
              <div>
                <h2>Troubleshooting</h2>
                <p>If the file upload fails, ensure it’s a .dxf file and retry.</p>
              </div>
            )}
            {activeSection === "faqs" && (
              <div>
                <h2>FAQs</h2>
                <p><b>What file types are supported?</b> Only .dxf files.</p>
                <p><b>How can I remove a file?</b> Click the "Remove File" button.</p>
              </div>
            )}
            {activeSection === "support" && (
              <div>
                <h2>Contact Support</h2>
                <p>For help, contact</p>
              </div>
            )}
          </div>  
        </div>
      )}  
    </div>
  );
}
