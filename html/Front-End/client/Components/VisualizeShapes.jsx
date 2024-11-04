import React, {useEffect, useRef} from 'react';

function findMaxY(shapesData) {
    return 100; //todo: fix later
}

function findMaxX(shapesData) {
    return shapesData
        .map((object) =>{
            return object.drawing;
        })
        .reduce((a, b) =>{ //find the largest value between all x values (start and end) of all drawing lines
            let maxFromA = Math.max(a.reduce((c, d) => c.startX > d.startX ? c : d).startX,
                a.reduce((c, d) => c.endX > d.endY ? c : d).endX);

            let maxFromB = Math.max(b.reduce((c, d) => c.startX > d.startX ? c : d).startX,
                b.reduce((c, d) => c.endX > d.endY ? c : d).endX);

            return maxFromA > maxFromB ? a : b;
        })
}

const VisualizeShapes = ({shapesData}) => {
    // Reference to the canvas element
    const canvasRef = useRef(null);

    // Get the window's width to scale the canvas size
    const windowWidth = window.innerWidth;

    let scaleFactor = 100;

    function calculateScaleFactor(shapesData) {
        let minX = Infinity;
        let maxX = 0;
        let minY = Infinity;
        let maxY = 0;
        shapesData
            .map((object) =>{
                return object.table;
            })
            .forEach((shape) => {
                //approximate min and max x using centerX, same thing for y
                minX = Math.min(minX, shape.centerX);
                minY = Math.min(minY, shape.centerY);
                maxX = Math.max(maxX, shape.centerX);
                maxY = Math.max(maxY, shape.centerY);
            })
        console.log("Max X", maxX);
        console.log("Max Y", maxY);
        console.log("Min X", minX);
        console.log("Min Y", minY);
    }

    // useEffect hook is used to handle the drawing logic when the component mounts or when shapesData or scaleFactor changes
    useEffect(() => {
            // Get the canvas and context for drawing
            const canvas = canvasRef.current;
            const ctx = canvas.getContext('2d');

            // Clear the canvas before drawing to avoid overlapping previous drawings
            ctx.clearRect(0, 0, canvas.width, canvas.height);

            scaleFactor =  Math.abs(600 / Math.max(findMaxX(shapesData), findMaxY(shapesData)));

            canvas.height = findMaxY(shapesData) * 2 * scaleFactor;
            canvas.width = findMaxX(shapesData)* 2 * scaleFactor;

            //flip y-axis, need to apply transforms after changing canvas width and height
            ctx.setTransform(1, 0, 0, -1, 0, canvas.height);


            console.log("Canvas width: ", canvas.width);
            console.log("Canvas height: ", canvas.height);
            console.log("-----------------------------------");

            // Outline the entire canvas for visual clarity
            ctx.strokeRect(0, 0, canvas.width, canvas.height);

            // Function to draw shapes by iterating over the shapesData
            const drawShapes = () => {
                shapesData
                    .map((object) => {
                        return object.drawing;
                    })
                    .forEach((drawArr) => {
                        drawArr.forEach((shape) => {
                            // Check the shape type and call the appropriate function to draw it
                            if (shape.type.toLowerCase() === 'Line2D'.toLowerCase()) {
                                drawLine(ctx, shape, scaleFactor); // Draw a line
                            } else if (shape.type.toLowerCase() === 'Arc2D'.toLowerCase()) {
                                drawArc(ctx, shape, scaleFactor); // Draw an arc
                            } else if (shape.type.toLowerCase() === 'circle'.toLowerCase() ||
                                shape.type.toLowerCase() === 'punch'.toLowerCase()) {
                                drawCircle(ctx, shape, scaleFactor); // Draw a circle
                            }
                        })
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

            calculateScaleFactor(shapesData);
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
