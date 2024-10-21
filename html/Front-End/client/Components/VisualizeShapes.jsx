import React, { useEffect, useRef } from 'react';

const VisualizeShapes = ({ shapesData, scaleFactor = 100 }) => {
    // Reference to the canvas element
    const canvasRef = useRef(null);

    // Get the window's width to scale the canvas size
    const windowWidth = window.innerWidth;

    // useEffect hook is used to handle the drawing logic when the component mounts or when shapesData or scaleFactor changes
    useEffect(() => {
        // Get the canvas and context for drawing
        const canvas = canvasRef.current;
        const ctx = canvas.getContext('2d');

        // Set canvas width to 80% of the window width
        canvas.width = windowWidth * 0.8;

        // Flip the y-axis to mimic a coordinate system where (0, 0) is at the bottom-left of the canvas
        ctx.transform(1, 0, 0, -1, 0, canvas.height);

        // Clear the canvas before drawing to avoid overlapping previous drawings
        ctx.clearRect(0, 0, canvas.width, canvas.height);

        // Outline the entire canvas for visual clarity
        ctx.strokeRect(0, 0, canvas.width, canvas.height);

        // Function to draw shapes by iterating over the shapesData
        const drawShapes = () => {
            shapesData.forEach((shape) => {
                // Check the shape type and call the appropriate function to draw it
                if (shape.type === 'Line2D') {
                    drawLine(ctx, shape, scaleFactor); // Draw a line
                } else if (shape.type === 'Arc2D') {
                    drawArc(ctx, shape, scaleFactor); // Draw an arc
                } else if (shape.type === 'circle') {
                    drawCircle(ctx, shape, scaleFactor); // Draw a circle
                }
            });
        };

        // Function to draw a Line2D shape
        const drawLine = (ctx, shape, scaleFactor) => {
            ctx.beginPath();
            // Move to the start coordinates of the line, scaled by scaleFactor
            ctx.moveTo(shape.startX * scaleFactor, shape.startY * scaleFactor);
            // Draw the line to the end coordinates
            ctx.lineTo(shape.endX * scaleFactor, shape.endY * scaleFactor);
            ctx.strokeStyle = 'black'; // Set line color to black
            ctx.lineWidth = 2; // Set line width
            ctx.stroke(); // Render the line
        };

        // Function to draw an Arc2D shape
        const drawArc = (ctx, shape, scaleFactor) => {
            ctx.beginPath();
            // Draw the arc using the center point, radius, and start/end angles
            ctx.arc(
                shape.centerX * scaleFactor, // Center X coordinate, scaled
                shape.centerY * scaleFactor, // Center Y coordinate, scaled
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
        const drawCircle = (ctx, shape, scaleFactor) => {
            ctx.beginPath();
            // Draw the circle using the center point and radius
            ctx.arc(
                shape.centerX * scaleFactor, // Center X coordinate, scaled
                shape.centerY * scaleFactor, // Center Y coordinate, scaled
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
    }, [shapesData, scaleFactor]); // Re-run the effect if shapesData or scaleFactor changes

    return (
        <div id="container">
            {/* Render a canvas element and attach the reference to canvasRef */}
            <canvas ref={canvasRef} width={window.innerWidth} height={window.innerHeight} />
        </div>
    );
};

export default VisualizeShapes;
