import React, { useState, useRef } from "react";

export default function ManualFeatureSelection ({ onShapeSelect }){
  const [selectedShapes, setSelectedShapes] = useState([]);

  //Ensure the state and toggle function are correctly initialized
  //const [isVisible, setIsVisible] = useState(false);
  //const [isManualVisible, setIsManualVisible] = useState(false); // State for user's manual
  const [isShapePanelVisible, setIsShapePanelVisible] = useState(false);
  const [isManualVisible, setIsManualVisible] = useState(false);

  const [panelPosition, setPanelPosition] = useState({ x: 100, y: 100 }); // Initial panel position
  const [dragging, setDragging] = useState(false); // State for dragging
  const [offset, setOffset] = useState({ x: 0, y: 0 }); // Offset to handle smooth dragging
  //State to keep track of the active manual section
  const [activeSection, setActiveSection] = useState("introduction");

  const manualButtonRef = useRef(null); // Reference to the manual button

//news-------------------------------
  // Function to set manual panel position dynamically based on the button position
  const toggleManual = () => {
    if (!isManualVisible && manualButtonRef.current) {
      const buttonRect = manualButtonRef.current.getBoundingClientRect();
      setPanelPosition({
        x: buttonRect.left - 500,
        y: buttonRect.top - 600,
      });
    }
    setIsManualVisible(!isManualVisible);
  };
//news-------------------------------


  // Toggle functions
  const toggleShapePanel = () => setIsShapePanelVisible(!isShapePanelVisible);
  //const toggleManual = () => setIsManualVisible(!isManualVisible);

  //Function to set the active manual section
  const changeSection = (section) => { setActiveSection(section); };

  //Function to toggle panel visibility
  //const togglePanel = () => { setIsVisible(!isVisible); };

  //Function to toggle the user's manual visibility
  //const toggleManual = () => { setIsManualVisible(!isManualVisible);};

  // Function to handle drag move
  const handleMouseDown = (e) => {
    // Only start dragging if clicked within the panel (e.g., on header)
    setDragging(true);
    setOffset({
      x: e.clientX,
      y: e.clientY,
    });
  };

// Function to handle drag move
  const handleMouseMove = (e) => {
    if (dragging) {
      const deltaX = e.clientX - offset.x;
      const deltaY = e.clientY - offset.y;

      const panelWidth = 300; // Adjust as needed
      const panelHeight = 400; // Adjust as needed

      const boundedX = Math.max(
          0,
          Math.min(window.innerWidth - panelWidth, panelPosition.x + deltaX)
      );
      const boundedY = Math.max(
          0,
          Math.min(window.innerHeight - panelHeight, panelPosition.y + deltaY)
      );

      setPanelPosition({
        x: boundedX,
        y: boundedY,
      });

      setOffset({
        x: e.clientX,
        y: e.clientY,
      });
    }
  };

  // Handle Drag End
  const handleMouseUp = () => setDragging(false);

  // Attach event listeners for dragging
  React.useEffect(() => {
    if (dragging) {
      window.addEventListener("mousemove", handleMouseMove);
      window.addEventListener("mouseup", handleMouseUp);
    } else {
      window.removeEventListener("mousemove", handleMouseMove);
      window.removeEventListener("mouseup", handleMouseUp);
    }
    return () => {
      window.removeEventListener("mousemove", handleMouseMove);
      window.removeEventListener("mouseup", handleMouseUp);
    };
  }, [dragging]);

  // Function to handle shape selection on button click
  const handleShapeSelect = (shapeType) => {
    setSelectedShapes((prevShapes) => {
      const isSelected = prevShapes.includes(shapeType);
      const newShapes = isSelected
          ? prevShapes.filter((shape) => shape !== shapeType)
          : [...prevShapes, shapeType];

      if (onShapeSelect) {
        onShapeSelect(newShapes);
      }

      return newShapes;
    });
  };

  //Button click handler
  const selectFeature = (event) => {
    const shapeType = event.target.innerText;
    console.log(`Button clicked: ${shapeType}`);
    handleShapeSelect(shapeType); // Directly call handleShapeSelect with shapeType
  };

  return (
      <div className="fade-out-panel-container">

        {/* Toggle shape panel */}
        <button className="toggle-button" onClick={() => setIsShapePanelVisible(!isShapePanelVisible)}>
          <i className="fas fa-shapes"></i>
        </button>

        {/*/!* Manual book icon *!/*/}
        {/*<button className="manual-button" onClick={toggleManual}>*/}
        {/*  <i className="fas fa-book"></i>*/}
        {/*</button>*/}

        {/* Feature buttons with click event */}
        {/* Shape Panel */}
        {isShapePanelVisible && (
            <div className="fade-out-panel">
              <button className="feature-button" onClick={() => handleShapeSelect("F1A")}>
                <img src="/icons/F1A.png" alt="1A thumbnail" className="thumbnail"/>
              </button>
              <button className="feature-button" onClick={() => handleShapeSelect("F1B")}>
                <img src="/icons/F1B.png" alt="1B thumbnail" className="thumbnail"/>
              </button>
              <button className="feature-button" onClick={() => handleShapeSelect("F1C")}>
                <img src="/icons/F1C.png" alt="1C thumbnail" className="thumbnail"/>
              </button>
              <button className="feature-button" onClick={() => handleShapeSelect("F2A")}>
                <img src="/icons/F2A.png" alt="2A thumbnail" className="thumbnail"/>
              </button>
              <button className="feature-button" onClick={() => handleShapeSelect("F3")}>
                <img src="/icons/F3.png" alt="3 thumbnail" className="thumbnail"/>
              </button>
              <button className="feature-button" onClick={() => handleShapeSelect("F4")}>
                <img src="/icons/F4.png" alt="4 thumbnail" className="thumbnail"/>
              </button>
              <button className="feature-button" onClick={() => handleShapeSelect("F6")}>
                <img src="/icons/F6.png" alt="6 thumbnail" className="thumbnail"/>
              </button>

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
        )}


        {/*/!* User's manual book icon at the bottom-right corner *!/*/}
        {/*<button className="manual-button" onClick={togglePanel}>*/}
        {/*  <i className="fas fa-book"></i>*/}
        {/*</button>*/}

        {/* Manual book icon */}
        <button className="manual-button" ref={manualButtonRef} onClick={toggleManual}>
          <i className="fas fa-book"></i>
        </button>

        {/* Manual Panel */}
        {isManualVisible && (
            <div
                className="manual-dialog-modern"
                style={{
                  position: "absolute",
                  top: `${panelPosition.y}px`,
                  left: `${panelPosition.x}px`,
                  cursor: dragging ? "grabbing" : "grab",
                }}
                onMouseDown={handleMouseDown}
            >
              {/*Close*/}
              <button className="close-button" onClick={toggleManual}>
                &times;
              </button>
              <div className="manual-sidebar">
                <h3>User Manual</h3>
                <ul>
                  <li onClick={() => setActiveSection("introduction")}>Introduction</li>
                  <li onClick={() => setActiveSection("getting-started")}>Getting Started</li>
                  <li onClick={() => setActiveSection("features")}>Features</li>
                  <li onClick={() => setActiveSection("guide")}>Step-by-Step Guide</li>
                  <li onClick={() => setActiveSection("troubleshooting")}>Troubleshooting</li>
                  <li onClick={() => setActiveSection("faqs")}>FAQs</li>
                  <li onClick={() => setActiveSection("support")}>Contact Support</li>
                </ul>
              </div>

              <div className="manual-content">
                {activeSection === "introduction" && (
                    <div>
                      <h2>Introduction</h2>
                      <p>Welcome to the File Upload and Shape Visualization App!</p>
                      <p>This app allows users to upload .dxf files, view structured results, visualize shapes, choose
                        your own shapes and calculate prices.</p>
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

                    <div className="info-section">
                      <div className="info-item">
                        <div className="icon-circle">
                          <i class="fa-solid fa-phone"></i> {/* Phone Icon */}
                        </div>
                        <div className="info-text">
                          <h4>Customer Support</h4>
                          <p>(800) 899-3437</p>
                        </div>
                      </div>

                      <div className="info-item">
                        <div className="icon-circle">
                          <i class="fa-solid fa-location-dot"></i> {/* Location Icon */}
                        </div>
                        <div className="info-text">
                          <h4>Our Location</h4>
                          <p>391 Malden Street</p>
                          <p>South St. Paul, MN 55075</p>
                        </div>
                      </div>
                    </div>
                )}
              </div>
            </div>
        )}
      </div>
  );
}