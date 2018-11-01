import {Fragment} from "./Fragment";
import {SvgDrawer} from "./SvgDrawer";
import {UuidTextPair} from "./UuidTextPair";

export class Document{
    url : string;
    fragments : Fragment[];
    docNum: number;
    uuid: string;

    linkUuidTexts: UuidTextPair[] = [];

    constructor(url :string, docNum: number, frags : Fragment[], uuid: string){
        this.url = url;
        this.docNum = docNum;
        this.fragments = frags;
        this.uuid = uuid;
        this.setFragLine();
        this.calcMatomeSvgY();
    }

    setFragLine(): void{
        this.fragments.forEach(frag => {
            frag.setLine()
        })
    }

    getSvgHeight(): number{
        let matomeHeight = 0;
        this.fragments.forEach(frag => {
            matomeHeight += frag.lines.length + SvgDrawer.FRAG_MARGIN ;
        });

        let detailHeight = 0;
        this.linkUuidTexts.forEach(uuidText => {
            detailHeight += uuidText.lines.length + SvgDrawer.FRAG_MARGIN ;
        });

        let ret = 0;
        if(matomeHeight > detailHeight){
            ret = matomeHeight;
        }else {
            ret = detailHeight;
        }

        return ret * SvgDrawer.CHAR_SIZE + SvgDrawer.PADDING * 2;
    }

    calcMatomeSvgY(): void{
        let i = 0;
        this.fragments.forEach(frag => {
            frag.svgY = i * SvgDrawer.CHAR_SIZE + SvgDrawer.PADDING;
            frag.lines.forEach(line => {
                line.svgY = i * SvgDrawer.CHAR_SIZE + SvgDrawer.PADDING;
                i++;
            });
            i += SvgDrawer.FRAG_MARGIN;
        });
    }

    calcDetailSvgY(): void{
        let matomeLineNum = 0;
        this.fragments.forEach(frag => {
            matomeLineNum += frag.lines.length + SvgDrawer.FRAG_MARGIN;
        });
        let detailLineNum = 0;
        this.linkUuidTexts.forEach( uuidText =>{
            detailLineNum += uuidText.lines.length;
        });
        let detailMargin: number;
        if(matomeLineNum > detailLineNum){
            detailMargin = Math.floor((matomeLineNum - detailLineNum) / this.linkUuidTexts.length)
        }else{
            detailMargin = SvgDrawer.FRAG_MARGIN;
        }


        let i = 0;
        this.linkUuidTexts.forEach( uuidText =>{
            uuidText.svgY = i * SvgDrawer.CHAR_SIZE + SvgDrawer.PADDING;
            uuidText.lines.forEach(line => {
                line.svgY = i * SvgDrawer.CHAR_SIZE + SvgDrawer.PADDING;
                i++;
            });
            i += detailMargin;

        })
    }

    getMatomeTextSvgData(): [string, number, string][] {//text,y座標,uuidの配列
        const ret: [string, number, string][] = [];
        this.fragments.forEach(frag => {
            frag.lines.forEach(line => {
                ret.push([line.text, line.svgY, frag.uuid]);
            })
        });
        return ret;
    }

    getMatomeTextSvgDataFromUuid(uuid: string): [string, number][] {//text yの相対座標
        const ret: [string, number][] = [];
        this.fragments.forEach(frag => {
            if(uuid == frag.uuid) {
                let fragY: number = frag.svgY;
                frag.lines.forEach(line => {
                    ret.push([line.text, line.svgY - fragY]);
                })
            }
        });
        return ret;
    }

    getMatomeBoxSvgData(): [number, number, string][]{//height,y,uuidの配列
        const ret: [number,number, string][] = [];
        this.fragments.forEach(frag => {
            ret.push([(frag.lines.length + SvgDrawer.FRAG_MARGIN - SvgDrawer.BOX_MARGIN)* SvgDrawer.CHAR_SIZE, frag.svgY - SvgDrawer.PADDING, frag.uuid]);
        });
        return ret;
    }

    getDetailTextSvgData(): [string, number, string][] {//text,y座標,uuidの配列
        const ret: [string, number, string][] = [];
        this.linkUuidTexts.forEach(uuidText => {
            uuidText.lines.forEach(line => {
                ret.push([line.text, line.svgY, uuidText.uuid]);
            })
        });
        return ret;
    }

    getDetailBoxSvgData(): [number, number, string][]{
        const ret: [number, number, string][] = [];
        this.linkUuidTexts.forEach( uuidText => {
            ret.push([(uuidText.lines.length + SvgDrawer.FRAG_MARGIN - SvgDrawer.BOX_MARGIN)* SvgDrawer.CHAR_SIZE, uuidText.svgY - SvgDrawer.PADDING, uuidText.uuid]);
        });

        return ret;
    }

    getLinkSvgData(): LinkSvgData[]{
        const ret: LinkSvgData[] = [];
        this.fragments.forEach(frag =>{
            frag.links.forEach(link =>{
                //ret.push([frag.svgY + frag.lines.length * SvgDrawer.CHAR_SIZE / 2, this.getDestLinkBoxSvgY(link.uuid)]);

                const axises: Axis[] = [];

                const x1 = SvgDrawer.ONE_LINE_CHAR * SvgDrawer.CHAR_SIZE + SvgDrawer.PADDING;
                const y1 = frag.svgY + frag.lines.length * SvgDrawer.CHAR_SIZE / 2 - SvgDrawer.CHAR_SIZE / 2;
                const x2 = SvgDrawer.SVG_WIDTH- SvgDrawer.ONE_LINE_CHAR * SvgDrawer.CHAR_SIZE - SvgDrawer.PADDING * 2;
                const y2 = this.getDestLinkBoxSvgY(link.uuid);

                axises.push(new Axis(x1, y1));
                axises.push(new Axis((x1 * 1.5 + x2 * 0.5) / 2, y1));
                axises.push(new Axis((x1 * 0.5 + x2 * 1.5) / 2, y2));
                axises.push(new Axis(x2, y2));

                ret.push(new LinkSvgData(axises));
            })
        });


        return ret;
    }

    getDestLinkBoxSvgY(uuid: string): number{
        let ret: number = 0;
        this.linkUuidTexts.forEach(uuidText =>{
            if(uuidText.uuid == uuid){
                ret = uuidText.svgY + uuidText.lines.length * SvgDrawer.CHAR_SIZE / 2 - SvgDrawer.CHAR_SIZE / 2;
            }
        });
        return ret;
    }

    getDocText():string{
        let ret = "";
        this.fragments.forEach(frag =>{
            ret += frag.text;
        });
        return ret;
    }

    hasFragTextInLinkUuidTexts(uuid: string): boolean{
        let ret = false;
        this.linkUuidTexts.forEach(uuidText=> {
            if(uuidText.uuid == uuid){
                ret = true;
            }
        });
        return ret;
    }


}

export class LinkSvgData{
    linkAxises :Axis[];
    constructor(linkAxis : Axis[]){
        this.linkAxises = linkAxis;
    }
    getObject(i: number): [number, number][]{
        const ret: [number, number][] = [];

        this.linkAxises.forEach(axis => {
           ret.push([axis.x,  axis.y]);
        });

        return ret;
    }
}
export class Axis{
    x: number;
    y: number;
    constructor(x: number, y:number){
        this.x = x;
        this.y = y;
    }
}