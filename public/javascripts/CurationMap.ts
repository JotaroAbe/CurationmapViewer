import {Document} from "./Document";
import {UuidTextPair} from "./UuidTextPair";

export class CurationMap {

    documents: Document[];

    constructor(documents: Document[]){
        this.documents = documents;
        this.setLinkUuidTexts();
        this.calcDetailSvgY();
    }

    setLinkUuidTexts(){
        this.documents.forEach(doc =>{
            if(doc.linkUuidTexts.length == 0) {
                doc.fragments.forEach(frag => {
                    frag.links.forEach(link => {
                        if(!doc.hasFragTextInLinkUuidTexts(link.uuid)){
                            doc.linkUuidTexts.push(new UuidTextPair(link.uuid, this.getTextFromUuid(link.uuid), this.getDocUrl(link.destDocNum)));
                        }
                    })
                });
            }
        })
    }

    getTextFromUuid(uuid: string): string{
        let ret: string = "";
        this.documents.forEach(doc =>{
            if(doc.uuid == uuid){
                ret = doc.getDocText();
            }
            doc.fragments.forEach(frag =>{
                if(frag.uuid == uuid){
                    ret = frag.text;
                }
            })
        });
        return ret;
    }

    calcDetailSvgY(): void{
        this.documents.forEach(doc =>{
            doc.calcDetailSvgY();
        })
    }

    getDocUrl(docNum: number): string{
        let ret = "";
        this.documents.forEach(doc=>{
            if(doc.docNum == docNum){
                ret = doc.url;
            }
        });
        return ret;
    }
}