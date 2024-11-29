import React, { useEffect, useState } from "react";
import { TreeTable } from 'primereact/treetable';
import { Column } from 'primereact/column';
import { Dropdown } from 'primereact/dropdown';
import { calculateTotalPrice, calculateManHourPrice } from '../services/pricingEngine';

/**
 * DisplayResults component displays the results of the uploaded file, including the shapes, quantities, kiss-cut options, and prices.
 *
 * @param results - The results of the uploaded file, containing the shapes and their properties
 * @param kissCutSelections - Whether kiss-cut is selected for each shape
 * @param onKissCutChange - Callback function to handle the kiss-cut selection change
 * @returns {Element} - The component to display the results of the uploaded file
 */
export default function DisplayResults({ results, kissCutSelections, onKissCutChange }) {
    // State variables to manage the price breakdown, selected breakdown category, tree table data, and highlight class
    const [priceBreakdown, setPriceBreakdown] = useState({ shapes: 0, labor: 0 });
    const [selectedBreakdownCategory, setSelectedBreakdownCategory] = useState(null);
    const [treeTableData, setTreeTableData] = useState([]);
    const [highlightClass, setHighlightClass] = useState("");

    const doubleToPriceStr = (num) =>{
        return num !== undefined && !isNaN(num) ? `$${num.toFixed(2)}` : 'N/A'
    }

    // useEffect hook to handle the animation for the total price display
    useEffect(() => {
        if (results) {
            const delayTimer = setTimeout(() => {
                setHighlightClass("highlight"); // Add class to trigger animation
                const animationTimer = setTimeout(() => setHighlightClass(""), 2000);

                return () => clearTimeout(animationTimer); // Cleanup animation timer
            }, 1500);

            return () => clearTimeout(delayTimer); // Cleanup delay timer
        }
    }, [results]);

    // Round a number to 4 decimal places
    const round = (num) => {
        if (num === undefined || num === null || isNaN(num)) {
            return "";
        }
        return parseFloat(num.toFixed(4));
    }

    // Calculate nodes and price breakdown whenever results or kissCutSelections change
    useEffect(() => {
        if (!results) return;

        let shapesTotal = 0;
        let laborTotal = 0;

        // Calculate the total price for each shape and update the price breakdown
        const shapeGroups = JSON.parse(results)
            .map((object) => object.table)
            .reduce((acc, shape, index) => {
                // Create a data object for the shape
                const type = shape.type;
                const centerX = round(shape.centerX);
                const centerY = round(shape.centerY);
                const area = round(shape.area);
                const circumference = round(shape.circumference);
                const radius = round(shape.radius);
                const multipleRadius = shape.multipleRadius !== undefined ? shape.multipleRadius.toString() : 'N/A';

                const perimeter = round(shape.perimeter);
                const key = `${type}-${area}-${radius}-${circumference}-${multipleRadius}-${perimeter}`
                const kissKey = `${key}-${index}`;
                const isKissCut = kissCutSelections[key] || false;
                const perimeterOver20 =`${perimeter > 20 ? "true" : "false"} (${perimeter.toFixed(2)}")`;

                const totalPrice = calculateTotalPrice(shape, isKissCut, perimeterOver20);

                shapesTotal += totalPrice - calculateManHourPrice(shape);
                laborTotal += calculateManHourPrice(shape);

                const shapeData = {
                    key: key,
                    data: {
                        type: type,
                        centerX: centerX,
                        centerY: centerY,
                        area: area,
                        circumference: circumference,
                        radius: radius,
                        multipleRadius: multipleRadius,
                        perimeter: perimeter,
                        kissCut: (
                            <input
                                type="checkbox"
                                checked={kissCutSelections[kissKey] || false}
                                onChange={(e) => onKissCutChange(kissKey, e.target.checked)}
                            />

                        ),
                        perimeterOver20: perimeterOver20,
                        price: doubleToPriceStr(totalPrice),
                    },
                    meta: { key, index },
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
                            perimeterOver20: perimeterOver20,

                            count:0
                        },
                        children: []
                    };
                }

                groupNode.children.push(shapeData);
                groupNode.data.count++;
                return acc;
            }, {});

        // Update state with the new data
        setTreeTableData(Object.values(shapeGroups));
        setPriceBreakdown({ shapes: shapesTotal, labor: laborTotal });
    }, [results, kissCutSelections]);

    // TODO change later
    const totalPrice = priceBreakdown.shapes + priceBreakdown.labor;
    const priceBreakdownOptions = [
        { label: "Shapes", value: priceBreakdown.shapes },
        { label: "Labor", value: priceBreakdown.labor }
    ];

    // Render the component with the results table and price breakdown dropdown
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
                        <Column field="kissCut" header="Kiss-Cut" />
                        <Column field="perimeterOver20" header="Perimeter Over 20&quot;" sortable/>
                    </TreeTable>

                    {/* Dropdown for price breakdown */}
                    <div className={`scrollHere ${highlightClass}`}>
                        <div className="dropdown-container">
                            <label>Total Price: {doubleToPriceStr(totalPrice)}</label>
                            <Dropdown
                                value={selectedBreakdownCategory}
                                options={priceBreakdownOptions}
                                onChange={(e) => setSelectedBreakdownCategory(e.value)}
                                placeholder="Select Category"
                            />
                            {selectedBreakdownCategory !== null && (
                                <p>Category Cost: ${selectedBreakdownCategory.toFixed(2)}</p>
                            )}
                        </div>
                    </div>
                </div>
            ) : (
                <h2>No Results Available</h2>
            )}
        </div>
    );
}