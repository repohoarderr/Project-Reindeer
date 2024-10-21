import React from "react";
import { TreeTable } from 'primereact/treetable';
import { Column } from 'primereact/column';

/**
 * DisplayResults component renders the results received from the backend.
 * It either shows the file upload results or displays a table with default column headers if no results are present.
 *
 * @param {Object} props - The props passed to the component.
 * @param {string} props.results - The results data to display. If no results are available, a message or table is shown.
 */
export default function DisplayResults({ results }) {
    return (
        <div className="results">
            {/* Conditionally render the results or a default message/table based on whether 'results' has data */}
            {results ? (
                <div>
                    {/* If results are available, display them inside a <pre> tag to preserve formatting */}
                    <h2>File Upload Results:</h2>
                    <pre>{results}</pre> {/* Using <pre> tag for formatting the results output (e.g., JSON or text) */}
                </div>
            ) : (
                <div>
                    {/* If no results are available, show a message indicating this */}
                    <h2>No Results Available</h2>

                    {/* Display a TreeTable with placeholder columns for "Name", "Size", and "Type" when no data is present */}
                    <TreeTable tableStyle={{ minWidth: '50rem' }}> {/* Setting a minimum width for the table */}
                        {/* Define the columns for the TreeTable */}
                        <Column field="name" header="Name" expander></Column> {/* Column for file or folder name with expander */}
                        <Column field="size" header="Size"></Column> {/* Column for file size */}
                        <Column field="type" header="Type"></Column> {/* Column for file type */}
                    </TreeTable>
                </div>
            )}
        </div>
    );
}
