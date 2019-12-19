
import {SvgDrawer} from "./SvgDrawer";
import {CurationMap} from "./CurationMap";
import $ from "jquery";

$("#alpha_range").on({
    "change" :function () {
        const v : string = $("#alpha_range").val() as string;
        $("#alpha").text("alpha = " + v);
    }

});

$("#beta_range").on({
    "change" :function () {
        const v : string = $("#beta_range").val() as string;
        $("#beta").text("beta = " + v);
    }

});

$("#reloadButton").on({
   "click" : function () {

       const a = $("#alpha_range").val() as number;
       const b = $("#beta_range").val() as number;

       getMap(a, b);
   }
});

const query : string = $("#query").text();

getMap(0.6, 0.6);



//以下関数


function getMap(alpha : number, beta : number) {
    $.ajax("getmap",
        {
            type : "GET",
            data: {
                query : query,
                alpha : alpha,
                beta : beta,
                merge : $("#isMergeCheck").prop("checked"),
                gensplitlink : $("#isGenSplitLink").prop("checked")
            }
        })
        .done(
            function(data) {
                console.log(data);
                const cMap = new CurationMap(data);
                draw(cMap);

            }
        )
        .fail(
            function (data) {
                console.log(data);
            }
        );

}


function draw(cMap : CurationMap) {
    //SVG描画
    const svgDrawer = new SvgDrawer();
    svgDrawer.drawMainSvg(cMap, 0);

    let i: number = 1;
    $("select").empty();
    cMap.documents.forEach(doc=>{
        const op = $("select").append("<option value="+(i - 1)+">"+i+ ":" +doc.title+"</option>").eq( i - 1 );
        op.on("change", e=>svgDrawer.drawMainSvg(cMap,op.val() as number));
        i++;
    });
}







