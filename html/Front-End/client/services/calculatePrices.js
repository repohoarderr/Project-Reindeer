import settings from './settings.json';

const shopRate = settings.prices.ShopRate;
const baseCost = settings.options["MDC +PLUS"];

let runCost = 0, featureCost = 0;

/**
 * Calculate the price for the given shapes
 *
 * Calls all helper functions to calculate the price for each shape
 *
 * @param shapes - The shapes to calculate the price for
 * @returns {{detailedPrices: *[], total: number, featureCost: number, perimeterCost: number, setup: number, base: number}}
 */
export default function calculatePrice(shapes) {
    const shapeQuantities = calculateShapeQuantities(shapes);

    let setupTotal = 0, totalFeatureCost = 0, perimeterCost = 0;
    const detailedPrices = [];
    const processedClasses = new Set();

    shapes.forEach((shape) => {
        if (processedClasses.has(shape.class)) {
            return; // Skip already processed classes
        }

        const quantity = shapeQuantities[shape.class] || 1;
        processedClasses.add(shape.class);
        console.log(shape.class + ", " + quantity);
        setEfficiencySlope(quantity); // This sets the dynamic discount

        const currentSetupCost = getSetupCost(shape.class) * setupDiscount(quantity);
        const currentRunCost = getRunCost(shape.class) * quantity;
        const currentPerimeterCost = (shape.perimeter || 0) * 0.65 * quantity;

        const shapeFeatureCost = featureCost + currentRunCost; // Includes dynamic efficiency
        const shapeTotal = currentSetupCost + shapeFeatureCost + currentPerimeterCost;

        setupTotal += currentSetupCost;
        totalFeatureCost += shapeFeatureCost;
        perimeterCost += currentPerimeterCost;

        detailedPrices.push({
            shape: { class: shape.class, quantity },
            setupCost: currentSetupCost,
            runCost: currentRunCost,
            perimeterCost: currentPerimeterCost,
            total: shapeTotal,
        });
    });

    const total = baseCost + setupTotal + totalFeatureCost + perimeterCost;

    return {
        base: baseCost,
        setup: setupTotal,
        featureCost: totalFeatureCost,
        perimeterCost,
        total,
        detailedPrices,
    };
}

function calculateShapeQuantities(shapes) {
    const quantities = {};

    shapes.forEach((shape) => {
        if (!quantities[shape.class]) {
            quantities[shape.class] = 0;
        }
        quantities[shape.class]++;
    });

    return quantities;
}

function setEfficiencySlope(quantity) {
    let costSub1 = runCost;
    let minCost = costSub1 * 0.25;
    featureCost = 0;

    for (let J = 1; J <= quantity; J++) {
        if (costSub1 > minCost) {
            featureCost += runCost;

            let effSlope = Math.sqrt(16 - Math.pow(0.052915 * J, 2)) - 3.02;

            costSub1 *= effSlope; // Efficiency slope factor
        } else {
            featureCost += minCost;
        }
    }
}


function setupDiscount(quantity) {
    if (quantity >= 7) return 0.25;

    switch (quantity) {
        case 1: return 1;
        case 2: return 0.92;
        case 3: return 0.86;
        case 4: return 0.76;
        case 5: return 0.63;
        case 6: return 0.46;
        default: return;
    }
}

function getRunCost(itemClass) {
    switch(itemClass) {
        case "F1A": return shopRate * 0.04 * 4;
        case "F1B": return shopRate * 0.042 * 4;
        case "F1C": return shopRate * 0.025 * 6;
        case "F17": return shopRate * 0.04 * 8;
        default: return;
    }
}

function getSetupCost(itemClass) {
    switch(itemClass) {
        case "F1A": return 0.22 * shopRate;
        case "F1B": return 0.3 * shopRate;
        case "F1C": return 0.32 * shopRate;
        case "F17": return 0.58 * shopRate;
        default: return;
    }
}

// TODO Future implementations:
//kisscut = setup * 1.15, runcost * 1.15
//multirad = runcost * 1.1, setup * .7 * # of radii
// perimeterCost = inch * $0.65
