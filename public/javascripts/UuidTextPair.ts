import {Line} from "./Fragment";
import {SvgDrawer} from "./SvgDrawer";

export class UuidTextPair {

    uuid: string;
    text: string;
    destDocUrl: string;
    destDocTitle: string;
    svgY: number = 0;
    oneLineCharNum: number = Math.ceil(SvgDrawer.ONE_LINE_CHAR * SvgDrawer.DETAIL_LINE_RATE);

    lines: Line[] = [];
    constructor(uuid: string, text: string, destDocUrl: string, destDocTitle: string){
        this.uuid = uuid;
        this.text = text;
        this.destDocUrl = destDocUrl;
        this.destDocTitle = destDocTitle;
        this.setLine();
    }

    setLine(): void{
        this.lines = [];

        for(let i = 0 ; i * this.oneLineCharNum < this.text.length ; i++){
            this.lines.push(new Line(this.text.substr(i * this.oneLineCharNum ,this.oneLineCharNum)));
        }
        this.lines.push(new Line(""));
        for(let i = 0 ; i * this.oneLineCharNum < this.destDocTitle.length ; i++){
            this.lines.push(new Line(this.destDocTitle.substr(i * this.oneLineCharNum ,this.oneLineCharNum)));
        }
        /*this.lines.push(new Line(""));
        for(let i = 0 ; i * this.oneLineCharNum < this.destDocUrl.length ; i++){
            this.lines.push(new Line(this.destDocUrl.substr(i * this.oneLineCharNum ,this.oneLineCharNum)));
        }*/
    }
}