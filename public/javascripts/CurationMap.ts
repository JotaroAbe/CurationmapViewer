import {Document} from "./Document";
import {UuidTextPair} from "./UuidTextPair";
import {Fragment} from "./Fragment";
import {Link} from "./Link";

export class CurationMap {

    data :any;
    documents: Document[];

    constructor(data : any){

        this.data = data;
        this.documents = [];

    }

    init(){
        this.data.documents.sort(function (a: any, b: any) {
                return (a.hub > b.hub) ? -1 : 1;
            }

        );

        const docID2UuidMap: { [key: number]: string; } = {};

        let i;
        for(i = 0 ; i < this.data.documents.length ; i++){
            docID2UuidMap[this.data.documents[i].docNum] = this.data.documents[i].uuid;
        }

//CMap作成
        const docs: Document[] = [];
        for(const doc of this.data.documents) {
            const frags: Fragment[] = [];
            for (const frag of doc.fragments) {
                const links: Link[] = [];
                for (const link of frag.links) {
                    links.push(new Link(link.destDocNum,link.weight, docID2UuidMap[link.destDocNum]));
                }
                frags.push(new Fragment(frag.text, links, frag.uuid));
            }
            docs.push(new Document(doc.url,doc.title, doc.docNum, frags, docID2UuidMap[doc.docNum]));
        }

        this.documents = docs;

        this.setLinkUuidTexts();
        this.calcDetailSvgY();
    }



    setLinkUuidTexts(){
        this.documents.forEach(doc =>{
            if(doc.linkUuidTexts.length == 0) {
                doc.fragments.forEach(frag => {
                    frag.links.forEach(link => {
                        if(!doc.hasFragTextInLinkUuidTexts(link.uuid)){
                            doc.linkUuidTexts.push(new UuidTextPair(link.uuid,
                                (this.getHitsRankFromUuid(link.uuid) + 1).toString() + ".",
                                this.getDocUrl(link.destDocNum), this.getDocTitle(link.destDocNum)));
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

    getLinkUuidFromUuid(uuid: string): string[]{
        let ret: string[] = [];
        this.documents.forEach(doc =>{
            doc.fragments.forEach(frag =>{
                if(frag.uuid == uuid){
                    frag.links.forEach( link =>{
                            ret.push(link.uuid);
                        }
                    )
                }
            })
        });
        return ret;

    }

    setTextOfUuidTextPairFromUuid(uuid: string, text: string): void{
        this.documents.forEach( doc =>{
            doc.linkUuidTexts.forEach( lu=>{
                if(lu.uuid == uuid){
                    lu.text = text;
                    lu.setLine();

                }
            })
        })
    }

    getHitsRankFromUuid(uuid: string): number{
        let ret: number = -1;
        let i: number = 0;
        this.documents.forEach(doc =>{
            if(doc.uuid == uuid){
                ret = i;
            }
            doc.fragments.forEach(frag =>{
                if(frag.uuid == uuid){
                    ret = i
                }
            });
            i++;
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
    getDocTitle(docNum: number): string{
        let ret = "";
        this.documents.forEach(doc=>{
            if(doc.docNum == docNum){
                ret = doc.title;
            }
        });
        return ret;
    }
}