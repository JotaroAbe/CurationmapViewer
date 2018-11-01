export class Link{
    //destText : string;
    destDocNum: number;
    uuid: string;
    constructor(destDocNum: number, uuid: string){
        this.destDocNum = destDocNum;
        this.uuid = uuid;
    }
}