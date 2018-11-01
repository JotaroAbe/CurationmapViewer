import {Link} from "./Link";
import {Fragment} from "./Fragment";
import {Document} from "./Document";
import {SvgDrawer} from "./SvgDrawer";
import {CurationMap} from "./CurationMap";
import $ from "jquery";
//JSONパース
const jsonDoc: any = document.getElementById("jsontext");
let jsonText :string;
if(jsonDoc != null){
    jsonText = jsonDoc.textContent;
}else{
    jsonText = "{}";
}

const map: any = JSON.parse(jsonText);

//並べ替え
map.documents.sort(function (a: any, b: any) {
        return (a.hub > b.hub) ? -1 : 1;
    }

);

//CMap作成
const docs: Document[] = [];
for(const doc of map.documents) {
    const frags: Fragment[] = [];
    for (const frag of doc.fragments) {
        const links: Link[] = [];
        for (const link of frag.links) {
            links.push(new Link(link.destDocNum, link.uuid));
        }
        frags.push(new Fragment(frag.text, links, frag.uuid));
    }
    docs.push(new Document(doc.url, doc.docNum, frags, doc.uuid));
}

const cMap: CurationMap = new CurationMap(docs);

//SVG描画

const svgDrawer = new SvgDrawer();
svgDrawer.drawMainSvg(cMap, 0);

let i: number = 1;
cMap.documents.forEach(doc=>{
    const op = $("select").append("<option value="+(i - 1)+">"+i+ ":" +doc.getDocText().substr(0, 20)+"</option>").eq( i - 1 );
    op.on("change", e=>svgDrawer.drawMainSvg(cMap,op.val() as number));
    i++;
});




