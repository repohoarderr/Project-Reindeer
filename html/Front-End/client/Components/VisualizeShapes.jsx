import React, { useEffect, useRef } from 'react';

const VisualizeShapes = ({ shapesData, scaleFactor = 100 }) => {
    const canvasRef = useRef(null);

    useEffect(() => {
        const canvas = canvasRef.current;
        const ctx = canvas.getContext('2d');

        // Clear the canvas
        ctx.clearRect(0, 0, canvas.width, canvas.height);

        // Function to draw all shapes
        const drawShapes = () => {
            var test = true
            shapesData.forEach((shape) => {
                if (shape.type === 'Line2D') {
                    drawLine(ctx, shape, scaleFactor);
                } else if (shape.type === 'Arc2D' && test) {
                    test = false;
                    console.log("rotation: " + shape.rotation)
                    console.log("angle: " + shape.angle)
                    drawArc(ctx, shape, scaleFactor);
                } else if (shape.type === 'circle') {
                    drawCircle(ctx, shape, scaleFactor);
                }
            });
        };

        // Helper functions to draw different shapes
        const drawLine = (ctx, shape, scaleFactor) => {
            ctx.beginPath();
            ctx.moveTo(shape.startX * scaleFactor, shape.startY * scaleFactor);
            ctx.lineTo(shape.endX * scaleFactor, shape.endY * scaleFactor);
            ctx.strokeStyle = 'black';
            ctx.lineWidth = 2;
            ctx.stroke();
        };

        const drawArc = (ctx, shape, scaleFactor) => {
            // const startAngle = (shape.rotation);
            // const endAngle = (shape.angle);

            ctx.beginPath();
            ctx.arc(
                shape.centerX * scaleFactor,
                shape.centerY * scaleFactor,
                shape.radius * scaleFactor,
                // Start point of the arc in radians
                shape.rotation,
                // End of the drawn arc in radians
                shape.angle,
                true
            );
            ctx.strokeStyle = 'red';
            ctx.lineWidth = 2;
            ctx.stroke();
        };


        const drawCircle = (ctx, shape, scaleFactor) => {
            ctx.beginPath();
            ctx.arc(
                shape.centerX * scaleFactor,
                shape.centerY * scaleFactor,
                shape.radius * scaleFactor,
                0,
                2 * Math.PI
            );
            ctx.strokeStyle = 'blue';
            ctx.lineWidth = 2;
            ctx.stroke();
        };

        // Draw the shapes
        drawShapes();
    }, [shapesData, scaleFactor]);

    return (
        // <div className="visualize-container">
        //     <canvas ref={canvasRef} width={800} height={600} />
        // </div>
        <canvas ref={canvasRef} width={window.innerWidth} height={window.innerHeight} />
    );
};

export default VisualizeShapes;


// import React from 'react';
// import { Stage, Layer, Line, Arc, Circle } from 'react-konva';
//
//
// const VisualizeShapes = ({ shapesData, scaleFactor = 100 }) => {
//     var switched = 0;
//     const renderShapes = () => {
//         return shapesData.map((shape, index) => {
//             if (shape.type === 'Line2D') {
//                 return (
//                     <Line
//                         key={index}
//                         points={[
//                             shape.startX * scaleFactor,
//                             shape.startY * scaleFactor,
//                             shape.endX * scaleFactor,
//                             shape.endY * scaleFactor
//                         ]}
//                         stroke="black"
//                         strokeWidth={2}
//                     />
//                 );
//             } else if (shape.type === 'Arc2D' && switched<4) {
//                 // shape.rotation = (-shape.rotation + 90)%360;
//                 // shape.angle = -shape.angle
//                 switched++;
//                 console.log("angle: " + (-shape.rotation+360)%360);
//                 console.log("rotation: " + (shape.rotation-shape.angle));
//                 return (
//                     <Arc
//                         key={index}
//                         x={shape.centerX * scaleFactor}
//                         y={shape.centerY * scaleFactor}
//                         // Renders the rotation, not the angle
//                         rotation={(-shape.rotation+360)%360}
//                         angle={shape.rotation}
//                         innerRadius={shape.radius * scaleFactor}
//                         outerRadius={shape.radius * scaleFactor}
//                         stroke="red"
//                         strokeWidth={2}
//                     />
//                 );
//             } else if (shape.type === 'circle') {
//                 // Drawing a circle with scaled inches to pixels
//                 return (
//                     <Circle
//                         key={index}
//                         x={shape.centerX * scaleFactor}  // Scale the centerX position
//                         y={shape.centerY * scaleFactor}  // Scale the centerY position
//                         radius={shape.radius * scaleFactor}  // Scale the radius
//                         stroke="blue"  // Circle outline color
//                         strokeWidth={2}
//                     />
//                 );
//             }
//             return null;
//         });
//     };
//
//     return (
//         <Stage width={window.innerWidth} height={window.innerHeight}>
//             <Layer>
//                 {renderShapes()}
//             </Layer>
//         </Stage>
//     );
// };
//
// export default VisualizeShapes;
