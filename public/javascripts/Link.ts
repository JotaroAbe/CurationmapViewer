
export class Link{
    //destText : string;
    destDocNum: number;
    weight: number;
    uuid: string;
    constructor(destDocNum: number, weight: number, uuid: string){
        this.destDocNum = destDocNum;
        this.weight = weight;
        this.uuid = uuid;
    }
}