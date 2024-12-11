/**
 * Imports all the prices from the settings.json file, then calculates the prices for each shape and returns the total price.
 *
 * This file will be changed to more accurately match the original pricing software.
 */
import settings from './settings.json';

export const calculateBasePrice = (shape) => {
    const basePrice = settings.prices[shape.type] || 0;
    return basePrice;
};

export const calculateKissCutPrice = (isKissCut) => {
    return isKissCut ? settings.kissCutPrice || 20 : 0;
};

export const calculateManHourPrice = (shape) => {
    const manHours = settings.manHours[shape.type] || 0.1; // Default to 0.1 if not specified
    const rate = settings.rate || 120; // Default rate
    return manHours * rate;
};

export const calculatePerimeterPrice = (perimeterOver20) => {
    return perimeterOver20 ? settings.perimeterOver20Price || 30 : 0;
};

export const calculateTotalPrice = (shape, isKissCut, perimeterOver20) => {
    const basePrice = calculateBasePrice(shape);
    const kissCutPrice = calculateKissCutPrice(isKissCut);
    const manHourPrice = calculateManHourPrice(shape);
    const perimeterPrice = calculatePerimeterPrice(perimeterOver20);

    return basePrice + kissCutPrice + manHourPrice + perimeterPrice;
};
