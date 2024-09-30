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
      {/* Display the results if they exist, or show a message otherwise */}
      {results ? (
        <div>
          <h2>File Upload Results:</h2>
          <pre>{results}</pre> {/* Use <pre> for formatting */}
        </div>
      ) : (
        <h2>No Results Available</h2>
      )}
    </div>
  );
}
