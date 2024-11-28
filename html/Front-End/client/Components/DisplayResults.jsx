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

        let shapesTotal = 0;
        let laborTotal = 0;

        // Calculate the total price for each shape and update the price breakdown
        const shapeGroups = JSON.parse(results)
            .map((object) => object.table)
            .reduce((acc, shape, index) => {
                const key = `${shape.type}-${index}`;
                const isKissCut = kissCutSelections[key] || false;
                const perimeter = shape.perimeter || 0;
                const perimeterOver20 = perimeter > 20;

                const totalPrice = calculateTotalPrice(shape, isKissCut, perimeterOver20);

                shapesTotal += totalPrice - calculateManHourPrice(shape);
                laborTotal += calculateManHourPrice(shape);

                const shapeData = {
                    key,
                    data: {
                        type: shape.type,
                        quantity: shape.quantity || 1,
                        kissCut: (
                            <input
                                type="checkbox"
                                checked={kissCutSelections[key] || false}
                                onChange={(e) => onKissCutChange(key, e.target.checked)}
                            />

                        ),
                        perimeterOver20: `${perimeterOver20 ? "true" : "false"} (${perimeter.toFixed(2)}")`,
                        price: `$${totalPrice.toFixed(2)}`,
                    },
                    meta: { key, index },
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

        // Update state with the new data
        setTreeTableData(Object.values(shapeGroups));
        setPriceBreakdown({ shapes: shapesTotal, labor: laborTotal });
    }, [results, kissCutSelections]);

    // Callback function to handle the kiss cut selection change
    const handleKissCutChange = (key, isChecked) => {
        setKissCutSelections((prev) => ({ ...prev, [key]: isChecked }));
    };

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
                    <TreeTable value={treeTableData} columnResizeMode="expand" tableStyle={{ minWidth: '50rem' }}>
                        <Column field="type" header="Type" expander />
                        <Column field="quantity" header="Quantity" />
                        <Column field="kissCut" header="Kiss-Cut" />
                        <Column field="perimeterOver20" header="Perimeter Over 20&quot;" />
                        <Column field="price" header="Price" />
                    </TreeTable>

                    {/* Dropdown for price breakdown */}
                    <div className={`scrollHere ${highlightClass}`}>
                        <div className="dropdown-container">
                            <label>Total Price: ${totalPrice.toFixed(2)}</label>
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
