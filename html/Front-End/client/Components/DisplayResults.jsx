import React from "react";
import { TreeTable } from 'primereact/treetable';
import { Column } from 'primereact/column';

/**
 * DisplayResults component renders the results received from the backend
 *
 * @param {Object} props - The props passed to the component
 * @param {string} props.results - The results data to display
 */
export default function DisplayResults({ results }) {
    const round = (num) => parseFloat(num.toFixed(4));

    const shapesToNodes = () => {
        if (!results) return [];

        return JSON.parse(results)
            .map((shape, index) => {
                if (shape.type === 'Line2D' || shape.type === 'Arc2D' || shape.type === 'circle') {
                    return {
                        key: index.toString(),
                        data: {
                            type: shape.type,
                            centerX: shape.centerX ? round(shape.centerX) : '',
                            centerY: shape.centerY ? round(shape.centerY) : '',
                            area: shape.area ? round(shape.area): '',
                            circumference: shape.circumference ? round(shape.circumference) : '',
                            radius: shape.radius ? round(shape.radius) : ''
                        },
                    };
                }
                return null;
            })
            .filter(node => node !== null);// Remove null values

    };

    const treeTableData = shapesToNodes();
    console.log("Transformed Data:", treeTableData);

    return (
        <div className="results">
            {results ? (
                <div>
                    <h2>File Upload Results:</h2>
                    <TreeTable value={treeTableData} tableStyle={{ minWidth: '50rem' }}>
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
                    <h2>No Results Available</h2>
                </div>
            )}
        </div>
    );
}
