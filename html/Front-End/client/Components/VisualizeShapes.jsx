import React, {useEffect, useRef} from 'react';

function findMaxY(shapesData) {
    let drawData = shapesData
        .map((object) => {
            return object.drawing;
        })

    let maxY = 0;
    for (let i = 0; i < drawData.length; ++i) {
        let arr = drawData[i];
        for (let k = 0; k < arr.length; k++) {
            let shape = arr[k];
            if (shape.startY) maxY = Math.max(maxY, shape.startY);
            if (shape.endY) maxY = Math.max(maxY, shape.endY);
            if (shape.centerY) maxY = Math.max(maxY, shape.centerY);
            if (shape.maxY) maxY = Math.max(maxY, shape.maxY);
        }
    }
    return maxY;
}

function findMaxX(shapesData) {
    let drawData = shapesData
        .map((object) => {
            return object.drawing;
        })

    let maxX = 0;
    for (let i = 0; i < drawData.length; ++i) {
        let arr = drawData[i];
        for (let k = 0; k < arr.length; k++) {
            let shape = arr[k];
            if (shape.startX) maxX = Math.max(maxX, shape.startX);
            if (shape.endX) maxX = Math.max(maxX, shape.endX);
            if (shape.centerX) maxX = Math.max(maxX, shape.centerX);
            if (shape.maxX) maxX = Math.max(maxX, shape.maxX);
        }
    }
    return maxX;
}

function findMinY(shapesData) {
    let drawData = shapesData
        .map((object) => {
            return object.drawing;
        })

    let minY = 0;
    for (let i = 0; i < drawData.length; ++i) {
        let arr = drawData[i];
        for (let k = 0; k < arr.length; k++) {
            let shape = arr[k];
            if (shape.startY) minY = Math.min(minY, shape.startY);
            if (shape.endY) minY = Math.min(minY, shape.endY);
            if (shape.centerY) minY = Math.min(minY, shape.centerY);
            if (shape.minY) minY = Math.min(minY, shape.minY);
        }
    }
    return minY;
}

function findMinX(shapesData) {
    let drawData = shapesData
        .map((object) => {
            return object.drawing;
        })

    let minX = 0;
    for (let i = 0; i < drawData.length; ++i) {
        let arr = drawData[i];
        for (let k = 0; k < arr.length; k++) {
            let shape = arr[k];
            if (shape.startX) minX = Math.min(minX, shape.startX);
            if (shape.endX) minX = Math.min(minX, shape.endX);
            if (shape.centerX) minX = Math.min(minX, shape.centerX);
            if (shape.minX) minX = Math.min(minX, shape.minX);
        }
    }
    return minX;
}

const VisualizeShapes = ({shapesData}) => {
    // Reference to the canvas element
    const canvasRef = useRef(null);

    let scaleFactor = 100;

    // useEffect hook is used to handle the drawing logic when the component mounts or when shapesData or scaleFactor changes
    useEffect(() => {
            // Get the canvas and context for drawing
            const canvas = canvasRef.current;
            const ctx = canvas.getContext('2d');

            // Clear the canvas before drawing to avoid overlapping previous drawings
            ctx.clearRect(0, 0, canvas.width, canvas.height);

            let padding = 0.1;
            let minX = findMinX(shapesData) - padding;
            let minY = findMinY(shapesData) - padding;

            let maxX = findMaxX(shapesData) + padding;
            let maxY = findMaxY(shapesData) + padding;

            const scaleFactor = Math.abs(600 / Math.max(maxX - minX, maxY - minY));

            canvas.width = (maxX - minX) * scaleFactor;
            canvas.height = (maxY - minY) * scaleFactor;

            // Center the canvas within the container by setting padding and margins
            canvas.style.display = 'block';
            canvas.style.margin = '0 auto';

            //flip y-axis, need to apply transforms after changing canvas width and height
            ctx.setTransform(1, 0, 0, -1, 0, canvas.height);

            // Outline the entire canvas with a dashed line for visual clarity
            ctx.setLineDash([5, 10]);
            ctx.strokeRect(0, 0, canvas.width, canvas.height);

            ctx.setLineDash([]);//draw as a solid line again

            // Function to draw shapes by iterating over the shapesData
            const drawShapes = () => {
                shapesData
                    .map((object) => {
                        return object.drawing;
                    })
                    .forEach((drawArr) => {
                        let xOffset = Math.abs(Math.min(0, minX));
                        let yOffset = Math.abs(Math.min(0, minY));
                        drawArr.forEach((shape) => {
                            // Check the shape type and call the appropriate function to draw it
                            if (shape.type.toLowerCase() === 'Line2D'.toLowerCase()) {
                                drawLine(ctx, shape, scaleFactor, xOffset, yOffset); // Draw a line
                            } else if (shape.type.toLowerCase() === 'Arc2D'.toLowerCase()) {
                                drawArc(ctx, shape, scaleFactor, xOffset, yOffset); // Draw an arc
                            } else if (shape.type.toLowerCase() === 'circle'.toLowerCase() ||
                                shape.type.toLowerCase() === 'punch'.toLowerCase()) {
                                drawCircle(ctx, shape, scaleFactor, xOffset, yOffset); // Draw a circle
                            }
                        })
                    });
            };

            // Function to draw a Line2D shape
            const drawLine = (ctx, shape, scaleFactor, xOffset, yOffset) => {
                ctx.beginPath();
                // Move to the start coordinates of the line, scaled by scaleFactor
                ctx.moveTo((shape.startX + xOffset) * scaleFactor, (shape.startY + yOffset) * scaleFactor);
                // Draw the line to the end coordinates
                ctx.lineTo((shape.endX + xOffset) * scaleFactor, (shape.endY + yOffset) * scaleFactor);
                ctx.strokeStyle = 'black'; // Set line color to black
                ctx.lineWidth = 2; // Set line width
                ctx.stroke(); // Render the line
            };

            // Function to draw an Arc2D shape
            const drawArc = (ctx, shape, scaleFactor, xOffset, yOffset) => {
                ctx.beginPath();
                // Draw the arc using the center point, radius, and start/end angles
                ctx.arc(
                    (shape.centerX + xOffset) * scaleFactor, // Center X coordinate, scaled
                    (shape.centerY + yOffset) * scaleFactor, // Center Y coordinate, scaled
                    shape.radius * scaleFactor,  // Arc radius, scaled
                    -(shape.angle),              // Start angle, converted from degrees to radians and flipped
                    -(shape.rotation),           // End angle, converted and flipped
                    true                         // Draw the arc in a counter-clockwise direction
                );
                ctx.strokeStyle = 'red'; // Set arc color to red
                ctx.lineWidth = 2; // Set line width
                ctx.stroke(); // Render the arc
            };

            // Function to draw a Circle shape
            const drawCircle = (ctx, shape, scaleFactor, xOffset, yOffset) => {
                ctx.beginPath();
                // Draw the circle using the center point and radius
                ctx.arc(
                    (shape.centerX + xOffset) * scaleFactor, // Center X coordinate, scaled
                    (shape.centerY + yOffset) * scaleFactor, // Center Y coordinate, scaled
                    shape.radius * scaleFactor,  // Circle radius, scaled
                    0,                           // Start angle (0 for a full circle)
                    2 * Math.PI                  // End angle (2π radians for a full circle)
                );
                ctx.strokeStyle = 'blue'; // Set circle color to blue
                ctx.lineWidth = 2; // Set line width
                ctx.stroke(); // Render the circle
            };

            // Call the function to draw all the shapes on the canvas
            drawShapes();
        },
        [shapesData, scaleFactor]); // Re-run the effect if shapesData or scaleFactor changes

    return (
        <div id="container">
            {/* Render a canvas element and attach the reference to canvasRef */}
            <canvas ref={canvasRef} width={window.innerWidth} height={window.innerHeight}/>
        </div>
    );
};

export default VisualizeShapes;
