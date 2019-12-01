import {Link} from "./Link";
import {Fragment} from "./Fragment";
import {Document} from "./Document";
import {SvgDrawer} from "./SvgDrawer";
import {CurationMap} from "./CurationMap";
import $ from "jquery";
import { v4 as uuid } from 'uuid';

const query : string = $("#query").text();

$.ajax("getmap",
    {
        type : "GET",
        data: {
            query : query,
            alpha : 0.6,
            beta : 0.6
        }
    })
    .done(
        function(data) {
            console.log(data);
            const cMap = convertJson2CMap(data);
            draw(cMap);
        }
    )
    .fail(
        function (data) {
            console.log(data);
        }
    );

function convertJson2CMap(data: any): CurationMap{
    data.documents.sort(function (a: any, b: any) {
            return (a.hub > b.hub) ? -1 : 1;
        }

    );

    const docID2UuidMap: { [key: number]: string; } = {};

    let i;
    for(i = 0 ; i < data.documents.length ; i++){
        console.log(data.documents[i].docNum);
        docID2UuidMap[data.documents[i].docNum] = uuid();
    }
    console.log(docID2UuidMap);

//CMap作成
    const docs: Document[] = [];
    for(const doc of data.documents) {
        const frags: Fragment[] = [];
        for (const frag of doc.fragments) {
            const links: Link[] = [];
            for (const link of frag.links) {
                links.push(new Link(link.destDocNum,link.weight, docID2UuidMap[link.destDocNum]));
            }
            frags.push(new Fragment(frag.text, links, uuid()));
        }
        docs.push(new Document(doc.url,doc.title, doc.docNum, frags, docID2UuidMap[doc.docNum]));
    }

    return new CurationMap(docs);
}

function draw(cMap : CurationMap) {
    //SVG描画

    const svgDrawer = new SvgDrawer();
    svgDrawer.drawMainSvg(cMap, 0);

    let i: number = 1;
    cMap.documents.forEach(doc=>{
        const op = $("select").append("<option value="+(i - 1)+">"+i+ ":" +doc.title+"</option>").eq( i - 1 );
        op.on("change", e=>svgDrawer.drawMainSvg(cMap,op.val() as number));
        i++;
    });
}







