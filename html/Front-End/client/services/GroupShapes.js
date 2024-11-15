export default function LogConsole({results}) {

    if (!results) return [];

    let obj = Object.values(JSON.parse(results)
        .map((object) => {
            return object.table;
        }))
        .map((shape, index) => {
            return {
                key: index.toString(),
                data: {
                    type: shape.type,
                    area: (shape.area),
                    circumference: (shape.circumference),
                    radius: (shape.radius),
                    multipleRadius: shape.multipleRadius !== undefined ? shape.multipleRadius : ""
                },
            };
        })
        .filter(node => node !== null) // Remove null values
        .reduce((accumulator, node) => {
            // console.log(node.data)
            let shape = node.data
            const key = `${shape.type}-${shape.area}-${shape.radius}-${shape.circumference}-${shape.multipleRadius}`;

            if (!accumulator[key]) {
                accumulator[key] = {
                    key: key,
                    data: {type: shape.type},
                    children: []
                };
            }
            accumulator[key].children.push({
                key: `${key}-${accumulator[key].children.length}`,
                data: {
                    type: shape.type,

                    //we have already rounded, so we don't need to round again
                    centerX: shape.centerX !== "" ? shape.centerX : 'N/A',
                    centerY: shape.centerY !== "" ? shape.centerY : 'N/A',
                    area: shape.area !== "" ? shape.area : 'N/A',
                    radius: shape.radius !== "" ? shape.radius : 'N/A',
                    circumference: shape.circumference !== "" ? shape.circumference : 'N/A',
                    multipleRadius: shape.multipleRadius !== "" ? shape.multipleRadius.toString() : 'N/A'
                },
            });
            console.log(accumulator)
            // return accumulator; // Return the updated accumulator
        });
    console.log(obj)
}
//
//
//     let matchArr = []
//     obj.forEach((ele) => {
//         if(!ele.area)
//             return
//         else{
//             obj.forEach((ele1) => {
//                 if(ele1 !== ele && ele1.area === ele.area){
//                     matchArr.push(ele)
//                     matchArr.push(ele1)
//                     console.log(ele1.area + "\n" + ele.area)
//                     console.warn("SAME")
//                 }
//             })
//         }
//     })
// }
//
