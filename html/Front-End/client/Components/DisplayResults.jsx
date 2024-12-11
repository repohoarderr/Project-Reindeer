import React, { useEffect, useState } from "react";
import { TreeTable } from 'primereact/treetable';
import { Column } from 'primereact/column';
import calculatePrice from '../services/calculatePrices';

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
    const [treeTableData, setTreeTableData] = useState([]);
    const [totalPrice, setTotalPrice] = useState(0);
    const [highlightClass, setHighlightClass] = useState("");
    const [option, setOption] = useState("MDC +PLUS");

    //useEffect hook to handle the animation for the total price display
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

    // Calculate nodes and price breakdown whenever results or kissCutSelections change
    useEffect(() => {
        if (!results) return;

        const parsedResults = JSON.parse(results).map(obj => obj.table);

        const filteredResults = parsedResults.filter(shape =>
            shape.class !== "freehand" && shape.class !== "punch"
        );

        const pricing = calculatePrice(filteredResults);

        setTotalPrice(pricing.total);

        // Calculate the total price for each shape and update the price breakdown
        const shapeGroups = parsedResults.reduce((acc, shape, index) => {
            const key = `${shape.type}-${index}`;
            const isKissCut = kissCutSelections[key] || false;
            const perimeter = shape.perimeter || 0;
            const perimeterOver20 = perimeter > 20;

            // Match the shape by class to get pricing details
            const shapePriceDetails = pricing.detailedPrices.find(detail => detail.shape.class === shape.class) || {};
            const shapePrice = Math.round(shapePriceDetails.total || 0); // Round to nearest integer

            const shapeData = {
                key,
                data: {
                    type: shape.type,
                    quantity: shape.quantity || 1,
                    kissCut: (
                        <input
                            type="checkbox"
                            checked={isKissCut}
                            onChange={(e) => onKissCutChange(key, e.target.checked)}
                        />
                    ),
                    perimeterOver20: `${perimeterOver20 ? "true" : "false"} (${perimeter.toFixed(2)}")`,
                    price: `$${shapePrice.toFixed(2)}`, // Ensure two decimal places
                },
                meta: { key, index },
            };

            // Group shapes by type
            if (!acc[shape.type]) {
                acc[shape.type] = {
                    key: shape.type,
                    data: { type: shape.type },
                    children: [],
                };
            }

            acc[shape.type].children.push(shapeData);
            return acc;
        }, {});

        // Update state with the new data
        setTreeTableData(Object.values(shapeGroups));
    }, [results, kissCutSelections]);

    // Render the component with the results table and price breakdown dropdown
    return (
        <div className="results">
            <div className="radio-buttons">
                <label>
                    <input
                        type="radio"
                        value="MDC +PLUS"
                        checked={option === "MDC +PLUS"}
                        onChange={() => setOption("MDC +PLUS")}
                    />
                    MDC +PLUS
                </label>
                <br />
                <label id="grey">
                    <input
                        type="radio"
                        value="MDC Standard"
                        disabled
                        onChange={() => setOption("MDC Standard")}
                    />
                    MDC Standard
                </label>
            </div>

            {results ? (
                <div>
                    <TreeTable value={treeTableData} columnResizeMode="expand" tableStyle={{ minWidth: '50rem' }}>
                        <Column field="type" header="Type" expander />
                        <Column field="quantity" header="Quantity" />
                        <Column field="kissCut" header="Kiss-Cut" />
                        <Column field="perimeterOver20" header='Perimeter Over 20"' />
                        <Column field="price" header="Price" />
                    </TreeTable>

                    <div className={`scrollHere ${highlightClass}`}>
                        <div>
                            <label>Total Price: ${Math.round(totalPrice).toFixed(2)}</label>
                            <br/>
                            <label>Option Used: {option}</label>
                        </div>
                    </div>
                </div>
            ) : (
                <h2 id="NoResults">No Results Available</h2>
            )}
        </div>
    );
}
