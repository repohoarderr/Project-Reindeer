import React from "react";

/**
 * DisplayResults component renders the results received from the backend
 *
 * @param {Object} props - The props passed to the component
 * @param {string} props.results - The results data to display
 */
export default function DisplayResults({ results }) {
  return (
    <div className="results">
      <div className="results">
        <h2>Results:</h2>
        {/* Display the actual results below the box, or show a message otherwise */}
        {results ? (
          <pre>{results}</pre> // Display the JSON result below the header
        ) : (
          <p>No Results Available</p>
        )}
      </div>
    </div>
  );
}
