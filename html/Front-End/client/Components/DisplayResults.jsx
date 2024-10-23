import React from "react";

import {TreeTable} from 'primereact/treetable';
import {Column} from 'primereact/column';

import 'primereact/resources/themes/saga-blue/theme.css';  // Choose a theme
import 'primereact/resources/primereact.min.css';

/**
 * DisplayResults component renders the results received from the backend.
 * It either shows the file upload results or displays a table with default column headers if no results are present.
 *
 * @param {Object} props - The props passed to the component.
 * @param {string} props.results - The results data to display. If no results are available, a message or table is shown.
 */
export default function DisplayResults({results}) {
    const round = (num) => parseFloat(num.toFixed(4));

    const shapesToNodes = () => {
        if (!results) return [];

        return Object.values(JSON.parse(results)
            .map((shape, index) => {
                return {
                    key: index.toString(),
                    data: {
                        type: shape.type,
                        centerX: shape.centerX ? round(shape.centerX) : '',
                        centerY: shape.centerY ? round(shape.centerY) : '',
                        area: shape.area ? round(shape.area) : '',
                        circumference: shape.circumference ? round(shape.circumference) : '',
                        radius: shape.radius ? round(shape.radius) : ''
                    },
                };
            })
            .filter(node => node !== null) // Remove null values
            .reduce((acc, node) => {
                const shape = node.data;
                // Generate a unique key for the group
                const key = `${shape.type}`;

                // If the group doesn't exist in the accumulator, create it
                if (!acc[key]) {
                    acc[key] = {
                        key: `${shape.type}-${shape.area}-${shape.radius}-${shape.circumference}`,
                        data: {
                            type: shape.type
                        },
                        children: []
                    };
                }

                // Add the shape to the group's children
                acc[key].children.push({
                    key: `${key}-${acc[key].children.length}`,
                    data: {
                        type: shape.type,
                        centerX: shape.centerX ? round(shape.centerX) : 'N/A',
                        centerY: shape.centerY ? round(shape.centerY) : 'N/A',
                        area: shape.area ? round(shape.area) : 'N/A',
                        radius: shape.radius ? round(shape.radius) : 'N/A',
                        circumference: shape.circumference ? round(shape.circumference) : 'N/A'
                    },
                });
                return acc; // Return the updated accumulator
            }, {})); // Start with an empty object as the accumulator
    };

    const treeTableData = shapesToNodes();
    console.log("Transformed Data:", treeTableData);


    return (
        <div className="results">
            {/* Conditionally render the results or a default message/table based on whether 'results' has data */}
            {results ? (
                <div>
                    {/* If results are available, display them inside a <pre> tag to preserve formatting */}
                    <h2>File Upload Results:</h2>
                    <pre>{results}</pre> {/* Using <pre> tag for formatting the results output (e.g., JSON or text) */}
                    <TreeTable value={treeTableData} columnResizeMode={"expand"} tableStyle={{minWidth: '50rem'}}>
                        <Column field="type" header="Type" expander></Column>
                        <Column field="centerX" header="Center X"></Column>
                        <Column field="centerY" header="Center Y"></Column>
                        <Column field="area" header="Area"></Column>
                        <Column field="circumference" header="Circumference"></Column>
                        <Column field="radius" header="Radius"></Column>
                    </TreeTable>
                </div>
            ) : (
                <div>
                    {/* If no results are available, show a message indicating this */}
                    <h2>No Results Available</h2>

                    {/*/!* Display a TreeTable with placeholder columns for "Name", "Size", and "Type" when no data is present *!/*/}
                    {/*<TreeTable tableStyle={{ minWidth: '50rem' }}> /!* Setting a minimum width for the table *!/*/}
                    {/*    /!* Define the columns for the TreeTable *!/*/}
                    {/*    <Column field="name" header="Name" expander></Column> /!* Column for file or folder name with expander *!/*/}
                    {/*    <Column field="size" header="Size"></Column> /!* Column for file size *!/*/}
                    {/*    <Column field="type" header="Type"></Column> /!* Column for file type *!/*/}
                    {/*</TreeTable>*/}
                </div>
            )}
        </div>
    );
}

// const exampleTreeData = [
//     {
//         key: '0',
//         data: {
//             type: 'Arc2D',
//             centerX: ' ',
//             centerY: '',
//             area: ' ',
//             radius: ' ',
//             circumference: ' '
//         },
//         children: [
//             {
//                 key: 'Arc2D-0',
//                 data: {
//                     type: 'Arc2D',
//                     centerX: 11.2651,
//                     centerY: 0.755,
//                     area: ' ',
//                     radius: 0.9375,
//                     circumference: ' '
//                 }
//             },
//             {
//                 key: 'Arc2D-1',
//                 data: {
//                     type: 'Arc2D',
//                     centerX: 6.0147,
//                     centerY: 0.755,
//                     area: ' ',
//                     radius: 0.9375,
//                     circumference: ' '
//                 }
//             },
//             {
//                 key: 'Arc2D-2',
//                 data: {
//                     type: 'Arc2D',
//                     centerX: 6.0004,
//                     centerY: 4.2,
//                     area: ' ',
//                     radius: 0.9375,
//                     circumference: ' '
//                 }
//             },
//             {
//                 key: 'Arc2D-3',
//                 data: {
//                     type: 'Arc2D',
//                     centerX: 0.75,
//                     centerY: 4.2,
//                     area: ' ',
//                     radius: 0.9375,
//                     circumference: ' '
//                 }
//             },
//             {
//                 key: 'Arc2D-4',
//                 data: {
//                     type: 'Arc2D',
//                     centerX: 8.6399,
//                     centerY: 3.1078,
//                     area: ' ',
//                     radius: 2.3088,
//                     circumference: ' '
//                 }
//             },
//             {
//                 key: 'Arc2D-5',
//                 data: {
//                     type: 'Arc2D',
//                     centerX: 3.3752,
//                     centerY: 1.8472,
//                     area: ' ',
//                     radius: 2.3088,
//                     circumference: ' '
//                 }
//             }
//         ]
//     },
//     {
//         key: '1',
//         data: {
//             type: 'Line2D',
//             centerX: ' ',
//             centerY: '',
//             area: ' ',
//             radius: ' ',
//             circumference: ' '
//         },
//         children: [
//             {
//                 key: 'Line2D-0',
//                 data: {
//                     type: 'Line2D',
//                     centerX: '',
//                     centerY: '',
//                     area: ' ',
//                     radius: ' ',
//                     circumference: ' '
//                 }
//             },
//             {
//                 key: 'Line2D-1',
//                 data: {
//                     type: 'Line2D',
//                     centerX: '',
//                     centerY: '',
//                     area: ' ',
//                     radius: ' ',
//                     circumference: ' '
//                 }
//             },
//             {
//                 key: 'Line2D-2',
//                 data: {
//                     type: 'Line2D',
//                     centerX: '',
//                     centerY: '',
//                     area: ' ',
//                     radius: ' ',
//                     circumference: ' '
//                 }
//             },
//             {
//                 key: 'Line2D-3',
//                 data: {
//                     type: 'Line2D',
//                     centerX: '',
//                     centerY: '',
//                     area: ' ',
//                     radius: ' ',
//                     circumference: ' '
//                 }
//             },
//             {
//                 key: 'Line2D-4',
//                 data: {
//                     type: 'Line2D',
//                     centerX: '',
//                     centerY: '',
//                     area: ' ',
//                     radius: ' ',
//                     circumference: ' '
//                 }
//             },
//             {
//                 key: 'Line2D-5',
//                 data: {
//                     type: 'Line2D',
//                     centerX: '',
//                     centerY: '',
//                     area: ' ',
//                     radius: ' ',
//                     circumference: ' '
//                 }
//             }
//         ]
//     }
// ];
