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

    const doubleToPriceStr = (num) =>{
        return num !== undefined && !isNaN(num) ? `$${num.toFixed(2)}` : 'N/A'
    }

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
        if (num === undefined || num === null || isNaN(num)) {
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

        } else {
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
            .reduce((acc, shape) => {
                const price = calculatePrice(shape);
                // Create a data object for the shape
                const type = shape.type;
                const centerX = round(shape.centerX);
                const centerY = round(shape.centerY);
                const area = round(shape.area);
                const circumference = round(shape.circumference);
                const radius = round(shape.radius);
                const multipleRadius = shape.multipleRadius !== undefined ? shape.multipleRadius.toString() : 'N/A';
                const priceStr = doubleToPriceStr(price);
                const perimeter = round(shape.perimeter);

                const shapeData = {

                    key: `${type}-${area}-${radius}-${circumference}-${multipleRadius}-${perimeter}`,
                    //build info for an individual shape
                    data: {
                        type: type,
                        centerX: centerX,
                        centerY: centerY,
                        area: area,
                        circumference: circumference,
                        radius: radius,
                        multipleRadius: multipleRadius,
                        unitPrice: price,
                        unitPriceStr:priceStr,
                        perimeter: perimeter
                    },
                };

                //build info for an object which holds a group of similar shapes
                let groupNode = acc[shapeData.key];
                if (!groupNode) {
                   groupNode = acc[shapeData.key] = {
                        key: shapeData.key,
                        data: {
                            type: type,
                            area: area,
                            circumference: circumference,
                            radius: radius,
                            multipleRadius: multipleRadius,
                            perimeter: perimeter,

                            unitPrice: price,
                            unitPriceStr: doubleToPriceStr(price),
                            totalPrice: 0,
                            totalPriceStr: `$0.00`,
                            count:0
                        },
                        children: []
                    };
                }

                groupNode.children.push(shapeData);
                groupNode.data.count++;
                groupNode.data.totalPrice = groupNode.data.unitPrice * groupNode.data.count;
                groupNode.data.totalPriceStr = doubleToPriceStr(groupNode.data.totalPrice)
                return acc;
            }, {});

        // return Object.values(Object.values(shapeGroups).reduce((acc, shapeGroup) => {
        //     const groupKey = shapeGroup.children[0].data.type;
        //     if(!acc[groupKey]){
        //         acc[groupKey] = {
        //             key:groupKey,
        //             data:shapeGroup.data,
        //             count: shapeGroup.children.length,
        //             children: []
        //         }
        //     }
        //     acc[groupKey].children.push(shapeGroup);
        //     return acc;
        // }, {}));

        return Object.values(shapeGroups);
    };

    const treeTableData = shapesToNodes();

    //Calculate total price from all shapes in treeTableData
    const totalPrice = treeTableData.reduce((sum, group) => {
        return sum + group.children.reduce((groupSum, node) => {
            const price = node.data.unitPrice || 0;
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
                        <Column field="type" header="Type" expander ></Column>
                        <Column field="count" header="#" ></Column>
                        <Column field="multipleRadius" header="Multiple Radius" ></Column>
                        <Column field="perimeter" header="Perimeter" sortable></Column>
                        <Column field="unitPriceStr" header="Unit Price" sortable></Column>
                        <Column field="totalPriceStr" header="Total Price" sortable></Column>
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