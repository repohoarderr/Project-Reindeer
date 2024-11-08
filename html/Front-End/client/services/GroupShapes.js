export default function LogConsole({results}) {

    if (!results) return [];

    let obj = Object.values(JSON.parse(results)
        .map((object) => {
            return object.table;
        }))


    let matchArr = []
    obj.forEach((ele) => {
        if(!ele.area)
            return
        else{
            obj.forEach((ele1) => {
                if(ele1 !== ele && ele1.area === ele.area){
                    matchArr.push(ele)
                    matchArr.push(ele1)
                    console.log(ele1.area + "\n" + ele.area)
                    console.warn("SAME")
                }
            })
        }
    })
}

