import * as d3 from "d3";
import {Axis, Document, LinkSvgData} from "./Document";
import {CurationMap} from "./CurationMap";
import $ from "jquery";



export class SvgDrawer{

    static CHAR_SIZE = 16;
    static PADDING = 20;
    static FRAG_MARGIN = 2;//行
    static BOX_MARGIN = 1;//行
    static SVG_WIDTH = window.innerWidth;
    static ONE_LINE_CHAR = Math.round((SvgDrawer.SVG_WIDTH - SvgDrawer.PADDING) / 2.5 / SvgDrawer.CHAR_SIZE);
    static LINE_SPACE_RATE = 1.5;
    static DETAIL_LINE_RATE = 0.5;
    static HEIGHT_MARGIN = 500;

    drawMainSvg(cMap: CurationMap, hubNum: number): void{

        cMap.init();
        const treeData: Document = cMap.documents[hubNum];

        const svgWidth = SvgDrawer.SVG_WIDTH;
        const svgHeight = treeData.getSvgHeight();

        $("#matomeDocUrl").html("<a target=\"_blank\" href=\""+treeData.url+"\">" + treeData.title + "</a>");

        d3.select("svg").remove();

        const svg = d3.select("body")
            .append("svg")
            .attr("width", svgWidth)
            .attr("height", svgHeight);

// #drop-shadow　という ID のフィルタを定義
        const filter = svg.append("defs")
            .append("filter")
            .attr("id", "drop-shadow")
            .attr("height", "130%");

// 元となるSVG要素をぼかして影を作る
        const feGaussianBlur = filter.append("feGaussianBlur")
            .attr("in", "SourceAlpha")
            .attr("stdDeviation", 3)
            .attr("result", "blur");

// 2つの入力画像 (元のSVG要素と影) を重ねて表示
        filter.append("feBlend")
            .attr("in", "SourceGraphic")
            .attr("in2", "blurOut")
            .attr("mode", "normal");


        const matomeBoxSvgData = treeData.getMatomeBoxSvgData();
        const matomeTextSvgData = treeData.getMatomeTextSvgData();
        const detailBoxSvgData = treeData.getDetailBoxSvgData();
        const detailTextSvgData = treeData.getDetailTextSvgData();

        const matomeFragsSvgG = svg.append('g')
            .attr("class", "matomeFrags");
        const detailFragsSvgG = svg.append('g')
            .attr("class", "detailFrags");
        const linksSvgG = svg.append('g')
            .attr("class", "fragLinks");


        matomeBoxSvgData.forEach(matomeBox =>{

            const simpleMatomeFragSvgG = matomeFragsSvgG.append("g")
                .attr("id", matomeBox[2]);


            const fragMatomeBox: [number, number, string][] = [];
            fragMatomeBox.push(matomeBox);

            simpleMatomeFragSvgG.selectAll("matomebox")
                .data(fragMatomeBox)
                .enter()
                .append("rect")
                .attr("class", d => "matomeBoxes")
                .attr("x", SvgDrawer.PADDING / 2)
                .attr("y", d => d[1] )//svgY
                .attr("rx", 10)
                .attr("ry", 10)
                .attr("width", SvgDrawer.ONE_LINE_CHAR * SvgDrawer.CHAR_SIZE + SvgDrawer.PADDING / 2)
                .attr("height", d => d[0] )//boxHeight
                .style("filter", "url(#drop-shadow)");
            const fragMatomeText: [string, number, string][] = [];
            matomeTextSvgData.forEach(matomeText => {
                if (matomeText[2] == matomeBox[2]) {
                    fragMatomeText.push(matomeText)
                }
            });
            simpleMatomeFragSvgG.selectAll("matometext")
                .data(fragMatomeText)
                .enter()
                .append("text")
                .attr("class", d => "matomeTexts")
                .text(d => d[0])//text
                .attr("x", SvgDrawer.PADDING)
                .attr("y", d => d[1])//svgY
                .attr("font-size", SvgDrawer.CHAR_SIZE + "px");

        });

        detailBoxSvgData.forEach(detailBox => {

            const simpleDetailFragSvgG = detailFragsSvgG.append("g")
                .attr("id", detailBox[2])
                .attr("class", cMap.getHitsRankFromUuid(detailBox[2]));

            const fragDetailBox: [number, number, string][] = [];
            fragDetailBox.push(detailBox);

            simpleDetailFragSvgG.selectAll("detailbox")
                .data(fragDetailBox)
                .enter()
                .append("rect")
                .attr("class", "detailBoxes")
                .attr("x", svgWidth - SvgDrawer.ONE_LINE_CHAR * SvgDrawer.CHAR_SIZE * SvgDrawer.DETAIL_LINE_RATE - SvgDrawer.PADDING * 2)
                .attr("y", d => d[1] )//svgY
                .attr("width", SvgDrawer.ONE_LINE_CHAR * SvgDrawer.CHAR_SIZE * SvgDrawer.DETAIL_LINE_RATE + SvgDrawer.PADDING / 2)
                .attr("height", d => d[0] )//boxHeight
                .attr("stroke", d => d3.interpolateRainbow(( d[1] * 2 + d[0] ) / 2 / svgHeight))
                .attr("fill", d => d3.interpolateRainbow(( d[1] * 2 + d[0] ) / 2 / svgHeight));

            const fragDetailText: [string, number, string][] = [];
            detailTextSvgData.forEach(detailText => {
                if (detailText[2] == detailBox[2]) {
                    fragDetailText.push(detailText)
                }
            });
            simpleDetailFragSvgG.selectAll("detailtext")
                .data(fragDetailText)
                .enter()
                .append("text")
                .attr("class", "detailTexts")
                .text(d => d[0])//text
                .attr("x", svgWidth - SvgDrawer.ONE_LINE_CHAR * SvgDrawer.CHAR_SIZE * SvgDrawer.DETAIL_LINE_RATE - SvgDrawer.PADDING * 2)
                .attr("y", d => d[1])//svgY
                .attr("font-size", SvgDrawer.CHAR_SIZE+"px");
        });


        const lineFunction = d3.line()
            .x(d => d[0])
            .y(d => d[1])
            .curve(d3.curveBasis);

        const linkSvgData = treeData.getLinkSvgData();
        let i = 0;
        linkSvgData.forEach(link =>{
            linksSvgG.append("path")
                .datum(link.getObject(i))
                .attr("class", "links")
                .attr("d", lineFunction)
                .attr("stroke", d3.interpolateRainbow(link.linkAxises[link.linkAxises.length - 1].y / svgHeight));

            i++;
        });

        const me: SvgDrawer = this;
        $(".matomeFrags").children().on({
            "click" :function () {
                const cl : string = $(this).attr("id") as string;

                const destuuids = cMap.getLinkUuidFromUuid(cl);
                destuuids.forEach( du =>{
                    $.ajax("getdestfrag",
                        {
                            type:"GET",
                            data:{
                                frag : cl,
                                destdoc : du
                            },
                            async : false
                        })
                        .done(function (data) {
                            cMap.setTextOfUuidTextPairFromUuid(du, data);
                        })
                        .fail(function (data) {
                            console.log(data)
                        });
                });


                me.drawMatomeClickSvg(cl, cMap, hubNum, svg);

            },
            "mouseenter" :function () {
                $(this).children(".matomeBoxes").css("fill", "darkgray");

            },
            "mouseleave" :function () {
                $(this).children(".matomeBoxes").css("fill", "white");
            }
        });

        $(".detailFrags").children().on({
            "click" :function () {
                const cl : string = $(this).attr("class") as string;
                $("select").val(cl);
                const num: number = parseInt(cl);

                me.drawMainSvg(cMap,num);
                $(window).scrollTop(0);
            }
        })

    }

    drawMatomeClickSvg(uuid: string, cMap: CurationMap, hubNum: number, svg: any): void{

        const treeData = cMap.documents[hubNum];

        const svgG = svg.append('g')
            .attr("class", "matomeClicks");

        svgG.append("rect")
            .attr("class", "clickBgRect")
            .attr("x", 0)
            .attr("y", 0)
            .attr("width", SvgDrawer.SVG_WIDTH)
            .attr("height", treeData.getSvgHeight());

        $(".matomeClicks").on("click",function () {
            $(this).remove();
        });

        const matomeTextData: [string, number][] = treeData.getMatomeTextSvgDataFromUuid(uuid);

        const y: number = $(window).scrollTop() as number;
        const fragG = svgG.append('g')
            .attr("class", "matomeClickFrags");
        fragG.append("rect")
            .attr("class", "clickBoxes")
            .attr("x", SvgDrawer.PADDING / 2)
            .attr("y", y)
            .attr("rx", 10)
            .attr("ry", 10)
            .attr("width", SvgDrawer.ONE_LINE_CHAR * SvgDrawer.CHAR_SIZE + SvgDrawer.PADDING / 2)
            .attr("height", (matomeTextData.length + SvgDrawer.FRAG_MARGIN - SvgDrawer.BOX_MARGIN) * SvgDrawer.CHAR_SIZE * SvgDrawer.LINE_SPACE_RATE)
            .style("filter", "url(#drop-shadow)");
        const matomeLinkSvgY = y + ((matomeTextData.length + SvgDrawer.FRAG_MARGIN - SvgDrawer.BOX_MARGIN) * SvgDrawer.CHAR_SIZE ) / 2;

        fragG.selectAll("clickmatometext")
            .data(matomeTextData)
            .enter()
            .append("text")
            .attr("class", "clickTexts")
            .text((d:[string, number]) => d[0])//text
            .attr("x", SvgDrawer.PADDING)
            .attr("y", (d:[string, number])  => y + d[1] + SvgDrawer.CHAR_SIZE)//svgY
            .attr("font-size", SvgDrawer.CHAR_SIZE + "px");

        const detailFragGs = svgG.append('g')
            .attr("class", "detailClickFrags");

        const detailBoxSvgData = treeData.getDetailBoxSvGDataFromFragUuid(uuid);
        const detailTextSvgData = treeData.getDetailTextSvgDataFromFragUuid(uuid);

        const detailLinkSvgYs: number[] = [];

        detailBoxSvgData.forEach(boxData=>{
            const detailFragG = detailFragGs.append('g')
                .attr("class", "simpleDetailClickFrags")
                .attr("class", cMap.getHitsRankFromUuid(boxData[2]));

            detailFragG.append("rect")
                .attr("class", "clickBoxes")
                .attr("x", SvgDrawer.SVG_WIDTH - SvgDrawer.ONE_LINE_CHAR * SvgDrawer.CHAR_SIZE - SvgDrawer.PADDING * 2)
                .attr("y", y + boxData[1])
                .attr("rx", 10)
                .attr("ry", 10)
                .attr("width", SvgDrawer.ONE_LINE_CHAR * SvgDrawer.CHAR_SIZE + SvgDrawer.PADDING / 2)
                .attr("height", boxData[0])
                .style("filter", "url(#drop-shadow)");

            const simpleDetailTextSvgData: [string,number][] =[];
            detailTextSvgData.forEach(textData=>{
                if(boxData[2] == textData[2]){
                    simpleDetailTextSvgData.push([textData[0], textData[1]]);
                }
            });

            detailFragG.selectAll("clickdetailtext")
                .data(simpleDetailTextSvgData)
                .enter()
                .append("text")
                .attr("class", "clickTexts")
                .text((d:[string, number]) => d[0])//text
                .attr("x", SvgDrawer.SVG_WIDTH - SvgDrawer.ONE_LINE_CHAR * SvgDrawer.CHAR_SIZE - SvgDrawer.PADDING * 2)
                .attr("y", (d:[string, number])  => y + d[1] + SvgDrawer.CHAR_SIZE)//svgY
                .attr("font-size", SvgDrawer.CHAR_SIZE + "px");

            detailLinkSvgYs.push(y + boxData[1] + boxData[0] / 2);
        });

        const me: SvgDrawer = this;
        $(".detailClickFrags").children().on({
            "click" :function () {
                const cl : string = $(this).attr("class") as string;
                $("select").val(cl);
                const num: number = parseInt(cl);
                me.drawMainSvg(cMap,num);
                $(window).scrollTop(0);
            }
        });

        const linkSvgData: LinkSvgData[] =[];

        detailLinkSvgYs.forEach(destSvgY =>{

            const axises: Axis[] = [];

            const x1 = SvgDrawer.ONE_LINE_CHAR * SvgDrawer.CHAR_SIZE + SvgDrawer.PADDING;
            const y1 = matomeLinkSvgY;
            const x2 = SvgDrawer.SVG_WIDTH- SvgDrawer.ONE_LINE_CHAR * SvgDrawer.CHAR_SIZE - SvgDrawer.PADDING * 2;
            const y2 = destSvgY;

            axises.push(new Axis(x1, y1));
            axises.push(new Axis((x1 * 1.5 + x2 * 0.5) / 2, y1));
            axises.push(new Axis((x1 * 0.5 + x2 * 1.5) / 2, y2));
            axises.push(new Axis(x2, y2));

            linkSvgData.push(new LinkSvgData(axises));
        });


        const linkSvgG = svgG.append('g')
            .attr("class", "clickLinks");

        const lineFunction = d3.line()
            .x(d => d[0])
            .y(d => d[1])
            .curve(d3.curveBasis);

        let i = 0;
        linkSvgData.forEach(link =>{
            linkSvgG.append("path")
                .datum(link.getObject(i))
                .attr("class", "clickLinks")
                .attr("d", lineFunction);

            i++;
        });
    }
}