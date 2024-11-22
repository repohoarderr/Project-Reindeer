import React, {useEffect, useState} from "react";
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
    const [highlightClass, setHighlightClass] = useState("");

    // useEffect hook to handle the animation for the total price display
    useEffect(() => {
        if (results) {
            const delayTimer = setTimeout(() => {
                setHighlightClass("highlight"); // Add class to trigger animation after 2 seconds
                const animationTimer = setTimeout(() => setHighlightClass(""), 2000); // Remove class after animation
                return () => clearTimeout(animationTimer); // Cleanup for animation timeout
            }, 1500); // 1.5-second delay before animation starts

            return () => clearTimeout(delayTimer); // Cleanup for delay timer
        }
    }, [results]);

    // Round a number to 4 decimal places
    const round = (num) => {
        if(num === undefined || num === null || isNaN(num)){
            return "";
        }
        return parseFloat(num.toFixed(4));
    }

    // Extract the size thresholds and prices from the settings.json file. All changes in the settings file will be reflected here.
    const prices = settings.prices
    /**
     * Calculate the price of the shape based on its type and size.
     *
     * There is probably a better way of doing this.
     * @param shape - The shape object to calculate the price for.
     * @returns {*} - The price of the shape.
     */
    const calculatePrice = (shape) => {
        let basePrice;
        if (shape.type === "freehand") {

        }
        else {
            basePrice = prices[`${shape.type}`]
        }
        return basePrice;
    };

    // Function to transform the results JSON into a format suitable for the TreeTable component
    const shapesToNodes = () => {
        if (!results) return [];

        // Parse the results JSON and extract the shape data
        const shapeGroups = JSON.parse(results)
            .map((object) => object.table)
            .reduce((acc, shape, index) => {
                const price = calculatePrice(shape);
                // Create a data object for the shape
                const shapeData = {
                    key: `${shape.type}-${index}`,
                    data: {
                        type: shape.type,
                        centerX: round(shape.centerX),
                        centerY: round(shape.centerY),
                        area: round(shape.area),
                        circumference: round(shape.circumference),
                        radius: round(shape.radius),
                        multipleRadius: shape.multipleRadius !== undefined ? shape.multipleRadius.toString() : 'N/A',
                        price: price !== undefined ? `$${price.toFixed(2)}` : 'N/A',
                        perimeter:round(shape.perimeter)
                    },
                };

                // Group shapes by type
                if (!acc[shape.type]) {
                    acc[shape.type] = {
                        key: shape.type,
                        data: { type: shape.type },
                        children: []
                    };
                }

                acc[shape.type].children.push(shapeData);
                return acc;
            }, {});

        return Object.values(shapeGroups);
    };

    const treeTableData = shapesToNodes();

    // Calculate total price from all shapes in treeTableData
    const totalPrice = treeTableData.reduce((sum, group) => {
        return sum + group.children.reduce((groupSum, node) => {
            const price = parseFloat(node.data.price.replace('$', '')) || 0;
            return groupSum + price;
        }, 0);
    }, 0);

    return (
        <div className="results">
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
                        <Column field="perimeter" header="Perimeter"></Column>
                        <Column field="price" header="Price"></Column>
                    </TreeTable>

                    {/* Display total price */}
                    <div className={`scrollHere ${highlightClass}`}>
                        Total Price: ${totalPrice.toFixed(2)}
                    </div>
                </div>
            ) : (
                <div>
                    {/* If no results are available, show a message indicating this */}
                    <h2>No Results Available</h2>
                </div>
            )}
        </div>
    );
}