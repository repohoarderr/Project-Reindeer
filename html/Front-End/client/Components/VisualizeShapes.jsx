import React from 'react';
import { Stage, Layer, Line, Arc, Circle } from 'react-konva';

const VisualizeShapes = ({ shapesData, scaleFactor = 100 }) => {
    const renderShapes = () => {
        return shapesData.map((shape, index) => {
            if (shape.type === 'Line2D') {
                return (
                    <Line
                        key={index}
                        points={[
                            shape.startX * scaleFactor,
                            shape.startY * scaleFactor,
                            shape.endX * scaleFactor,
                            shape.endY * scaleFactor
                        ]}
                        stroke="black"
                        strokeWidth={2}
                    />
                );
            } else if (shape.type === 'Arc2D') {
                return (
                    <Arc
                        key={index}
                        x={shape.startX * scaleFactor}
                        y={shape.startY * scaleFactor}
                        angle={-shape.angle}
                        innerRadius={shape.radius * scaleFactor}
                        outerRadius={shape.radius * scaleFactor}
                        rotation={shape.rotation}
                        stroke="red"
                        strokeWidth={2}
                    />
                );
            } else if (shape.type === 'circle') {
                // Drawing a circle with scaled inches to pixels
                return (
                    <Circle
                        key={index}
                        x={shape.centerX * scaleFactor}  // Scale the centerX position
                        y={shape.centerY * scaleFactor}  // Scale the centerY position
                        radius={shape.radius * scaleFactor}  // Scale the radius
                        stroke="blue"  // Circle outline color
                        strokeWidth={2}
                    />
                );
            }
            return null;
        });
    };

    return (
        <Stage width={window.innerWidth} height={window.innerHeight}>
            <Layer>
                {renderShapes()}
            </Layer>
        </Stage>
    );
};

export default VisualizeShapes;
