import React, {useState} from "react";
import "../public/styles.css";
import FileUploadForm from "./Components/UploadForm.jsx";
import DisplayResults from "./Components/DisplayResults.jsx";
import ManualFeatureSelection from "./Components/ManualFeatureSelection.jsx";
import VisualizeShapes from "./Components/VisualizeShapes.jsx";

/**
 * This App component is the main component that renders the application.
 *
 * @returns {Element} - the main application component
 */
export default function App() {
    // State variables to manage the upload results and kiss cut selections
    const [uploadResults, setUploadResults] = useState("");
    const [kissCutSelections, setKissCutSelections] = useState({});

    // Callback function to handle the upload completion
    const handleUploadComplete = (results) => {
        setUploadResults(results);
        setKissCutSelections({});
    };

    // Callback function to handle the kiss cut selection change
    const handleKissCutChange = (key, isChecked) => {
        setKissCutSelections((prev) => ({...prev, [key]: isChecked}));
    };

    const round = (num) => {
        if (num === undefined || num === null || isNaN(num)) {
            return "";
        }
        return parseFloat(num.toFixed(4));
    }

    function calculateShapeKey(shape, index) {
        const type = shape.type;
        const area = round(shape.area);
        const circumference = round(shape.circumference);
        const radius = round(shape.radius);
        const multipleRadius = shape.multipleRadius !== undefined ? shape.multipleRadius.toString() : 'N/A';

        const perimeter = round(shape.perimeter);
        return `${type}-${area}-${radius}-${circumference}-${multipleRadius}-${perimeter}-${index}`;
    }

    // Render the main application component. Ties together all the components.
    return (
        <div className="container">
            {/* Display MDC logo at the top of the page. */}
            <img id={"logo"}
                src="https://www.mathias-die.com/wp-content/uploads/2018/07/MDC-Logo-copy.png"
                alt="MDC Logo"
                className="centered-image"
            />
            <h1>Upload a File</h1>
            <FileUploadForm onUploadComplete={handleUploadComplete}/>
            <DisplayResults
                results={uploadResults}
                kissCutSelections={kissCutSelections}
                onKissCutChange={handleKissCutChange}
            />
            <ManualFeatureSelection/>
            {uploadResults && (
                <VisualizeShapes
                    shapesData={JSON.parse(uploadResults).map((shape, index) => ({
                        ...shape,
                        key: calculateShapeKey(shape.table, index)
                    }))}
                    kissCutSelections={kissCutSelections}
                />
            )}
        </div>
    );
}
