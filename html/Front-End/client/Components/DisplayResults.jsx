import React from "react";

import {TreeTable} from 'primereact/treetable';
import {Column} from 'primereact/column';

import 'primereact/resources/themes/saga-blue/theme.css';  // Choose a theme
import 'primereact/resources/primereact.min.css';
import settings from '../services/settings.json';

/**
 * DisplayResults component renders the results received from the backend.
 * It either shows the file upload results or displays a table with default column headers if no results are present.
 *
 * @param {Object} props - The props passed to the component.
 * @param {string} props.results - The results data to display. If no results are available, a message or table is shown.
 */
export default function DisplayResults({results}) {
    const round = (num) => {
        if(num === undefined || isNaN(num)){
            return "";
        }
        return parseFloat(num.toFixed(4));
    }

    const sizeThresholds = settings.sizeThresholds;
    const prices = settings.prices
    /**
     * Calculate the price of the shape based on its type and size.
     *
     * There is probably a better way of doing this.
     * @param shape - The shape object to calculate the price for.
     * @returns {*} - The price of the shape.
     */
    const calculatePrice = (shape) => {
        console.log(shape.type.concat(shape.area))
        let basePrice;
        if (shape.type === 'circle' || shape.type === 'punch') {
            // Get size (diameter)
            const size = 2*(shape.radius);

            // Find the price for the size
            let threshold = 'small';
            if (size >= sizeThresholds[`${shape.type}.medium.lower`] && size <= sizeThresholds[`${shape.type}.medium.upper`]) {
                threshold = 'medium';
            } else if (size >= sizeThresholds[`${shape.type}.large.lower`] && size <= sizeThresholds[`${shape.type}.large.upper`]) {
                threshold = 'large';
            }

            // set basePrise
            basePrice = prices[`${shape.type}.${threshold}`] || 0;
        }
        else if (shape.type === 'rectangle' || shape.type === 'roundRectangle') {
            let area = shape.area;
            let threshold = 'small';
            if (area >= sizeThresholds[`${shape.type}.medium.lower`] && area <= sizeThresholds[`${shape.type}.medium.upper`]) {
                threshold = 'medium';
            } else if (area >= sizeThresholds[`${shape.type}.large.lower`] && area <= sizeThresholds[`${shape.type}.large.upper`]) {
                threshold = 'large';
            }
            basePrice = prices[`${shape.type}.${threshold}`] || 0;
        }
        // This is commented out because the areas for these shapes is not defined.
        // else if (shape.type === 'roundTrapezoid' || shape.type === 'roundTriangle' || shape.type === 'oblong') {
        //     let area = shape.area;
        //     let threshold = 'small';
        //     if (area >= sizeThresholds[`${shape.type}.medium.lower`] && area <= sizeThresholds[`${shape.type}.medium.upper`]) {
        //         threshold = 'medium';
        //     } else if (area >= sizeThresholds[`${shape.type}.large.lower`] && area <= sizeThresholds[`${shape.type}.large.upper`]) {
        //         threshold = 'large';
        //     }
        //     basePrice = prices[`${shape.type}.${threshold}`] || 0;
        // }

        else {
            basePrice = 0
        }

        return basePrice;
    };


    const shapesToNodes = () => {
        if (!results) return [];

        return Object.values(JSON.parse(results)
            .map((object) => {
                return object.table;
            })
            .map((shape, index) => {
                const price = calculatePrice(shape)
                return {
                    key: index.toString(),
                    data: {
                        type: shape.type,
                        centerX: round(shape.centerX),
                        centerY: round(shape.centerY),
                        area: round(shape.area),
                        circumference: round(shape.circumference),
                        radius: round(shape.radius),
                        multipleRadius: shape.multipleRadius !== undefined ? shape.multipleRadius : "",
                        price: price !== undefined ? `$${price.toFixed(2)}` : 'N/A'
                }
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

                        //we have already rounded, so we don't need to round again
                        centerX: shape.centerX !=="" ? shape.centerX : 'N/A',
                        centerY: shape.centerY !=="" ? shape.centerY : 'N/A',
                        area: shape.area !=="" ? shape.area : 'N/A',
                        radius: shape.radius !=="" ? shape.radius : 'N/A',
                        circumference: shape.circumference !=="" ? shape.circumference : 'N/A',
                        multipleRadius: shape.multipleRadius !=="" ? shape.multipleRadius.toString() : 'N/A',
                        price: shape.price !== undefined ? shape.price: 'N/A'
                    },
                });
                return acc; // Return the updated accumulator
            }, {})); // Start with an empty object as the accumulator
    };

    const treeTableData = shapesToNodes();
    //console.log("Transformed Data:", treeTableData);

    // const totalPrice = treeTableData.reduce((sum, node) => {
    //     const price = parseFloat(node.data.price.replace('$', ''));
    //     return sum + price;
    // })

    return (
        <div className="results">
            {/* Conditionally render the results or a default message/table based on whether 'results' has data */}
            {results ? (
                <div>
                    {/* If results are available, display them inside a <pre> tag to preserve formatting */}
                    <h2>File Upload Results:</h2>
                    <pre>{results}</pre>
                    {/* Using <pre> tag for formatting the results output (e.g., JSON or text) */}
                    <TreeTable value={treeTableData} columnResizeMode={"expand"} tableStyle={{minWidth: '50rem'}}>
                        <Column field="type" header="Type" expander></Column>
                        <Column field="centerX" header="Center X"></Column>
                        <Column field="centerY" header="Center Y"></Column>
                        <Column field="area" header="Area"></Column>
                        <Column field="circumference" header="Circumference"></Column>
                        <Column field="radius" header="Radius"></Column>
                        <Column field="multipleRadius" header="Multiple Radius"></Column>
                        <Column field="price" header="Price"></Column>
                    </TreeTable>

                    {/*<div id="totalPrice"> Total Price: ${totalPrice.toFixed(2)}</div>*/}
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
