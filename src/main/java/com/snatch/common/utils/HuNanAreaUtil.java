package com.snatch.common.utils;

public class HuNanAreaUtil {

    /**
     * 根据县级单位得到市级单位
     */
    public static String area(String val) {
        String areaName = val;
        if(areaName.indexOf("长沙县")!=-1 || areaName.indexOf("浏阳")!=-1 || areaName.indexOf("望城")!=-1 || areaName.indexOf("宁乡")!=-1 || areaName.indexOf("湘江新区")!=-1 ||
                areaName.indexOf("岳麓区")!=-1 || areaName.indexOf("芙蓉区")!=-1 || areaName.indexOf("天心区")!=-1 || areaName.indexOf("开福区")!=-1 || areaName.indexOf("雨花区")!=-1) {
            areaName="长沙" + areaName;
        }
        if(areaName.indexOf("醴陵")!=-1 || areaName.indexOf("株洲")!=-1 || areaName.indexOf("炎陵")!=-1 || areaName.indexOf("茶陵")!=-1
                || areaName.indexOf("攸县")!=-1 || areaName.indexOf("天元区")!=-1 || areaName.indexOf("荷塘区")!=-1 || areaName.indexOf("芦淞区")!=-1 || areaName.indexOf("石峰区")!=-1) {
            areaName="株洲" + areaName;
        }
        if(areaName.indexOf("湘乡")!=-1 || areaName.indexOf("韶山")!=-1 || areaName.indexOf("湘潭县")!=-1 || areaName.indexOf("岳塘区")!=-1 || areaName.indexOf("雨湖区")!=-1) {
            areaName="湘潭" + areaName;
        }
        if(areaName.indexOf("南岳")!=-1 || areaName.indexOf("耒阳")!=-1 || areaName.indexOf("常宁")!=-1 || areaName.indexOf("衡阳县")!=-1 ||
                areaName.indexOf("衡东")!=-1 || areaName.indexOf("衡山")!=-1|| areaName.indexOf("衡南")!=-1 || areaName.indexOf("祁东")!=-1
                || areaName.indexOf("雁峰区")!=-1 || areaName.indexOf("珠晖区")!=-1 || areaName.indexOf("石鼓区")!=-1 || areaName.indexOf("蒸湘区")!=-1 || areaName.indexOf("南岳区")!=-1) {
            areaName="衡阳" + areaName;
        }
        if(areaName.indexOf("武冈")!=-1 || areaName.indexOf("邵东")!=-1 || areaName.indexOf("洞口")!=-1 || areaName.indexOf("新邵")!=-1 ||
                areaName.indexOf("绥宁")!=-1 || areaName.indexOf("新宁")!=-1 || areaName.indexOf("邵阳县")!=-1 || areaName.indexOf("隆回")!=-1
                || areaName.indexOf("城步")!=-1 || areaName.indexOf("双清区")!=-1 || areaName.indexOf("大祥区")!=-1 || areaName.indexOf("北塔区")!=-1) {
            areaName="邵阳" + areaName;
        }
        if(areaName.indexOf("临湘")!=-1 || areaName.indexOf("汨罗")!=-1 || areaName.indexOf("岳阳县")!=-1 || areaName.indexOf("湘阴")!=-1 ||
                areaName.indexOf("平江")!=-1 || areaName.indexOf("华容")!=-1 || areaName.indexOf("岳阳楼区")!=-1 || areaName.indexOf("君山区")!=-1 || areaName.indexOf("云溪区")!=-1) {
            areaName="岳阳" + areaName;
        }
        if(areaName.indexOf("津市")!=-1 || areaName.equals("澧县") || areaName.indexOf("临澧")!=-1 || areaName.indexOf("桃源县")!=-1 ||
                areaName.indexOf("汉寿县")!=-1 || areaName.indexOf("安乡")!=-1 || areaName.indexOf("石门")!=-1 || areaName.indexOf("武陵区")!=-1 || areaName.indexOf("鼎城区")!=-1) {
            areaName="常德" + areaName;
        }
        if(areaName.indexOf("武陵源")!=-1 || areaName.indexOf("慈利")!=-1 || areaName.indexOf("桑植")!=-1 || areaName.indexOf("永定区")!=-1 || areaName.indexOf("武陵源区")!=-1) {
            areaName="张家界" + areaName;
        }
        if(areaName.indexOf("沅江")!=-1 || areaName.indexOf("桃江")!=-1 || areaName.equals("南县") || areaName.indexOf("安化")!=-1
                || areaName.indexOf("赫山区")!=-1 || areaName.indexOf("资阳区")!=-1) {
            areaName="益阳" + areaName;
        }
        if(areaName.indexOf("资兴")!=-1 || areaName.indexOf("宜章")!=-1 || areaName.indexOf("汝城")!=-1 || areaName.indexOf("安仁")!=-1 || areaName.indexOf("嘉禾")!=-1
                || areaName.indexOf("临武")!=-1 || areaName.indexOf("桂东")!=-1 || areaName.indexOf("永兴")!=-1 || areaName.indexOf("桂阳")!=-1
                || areaName.indexOf("北湖区")!=-1 || areaName.indexOf("苏仙区")!=-1) {
            areaName="郴州" + areaName;
        }
        if(areaName.indexOf("祁阳")!=-1 || areaName.indexOf("蓝山")!=-1 || areaName.indexOf("宁远")!=-1 || areaName.indexOf("新田")!=-1 || areaName.indexOf("东安")!=-1
                || areaName.indexOf("江永")!=-1 || areaName.equals("道县") || areaName.indexOf("双牌")!=-1 || areaName.indexOf("江华")!=-1
                || areaName.indexOf("冷水滩区")!=-1 || areaName.indexOf("零陵区")!=-1) {
            areaName="永州" + areaName;
        }
        if(areaName.indexOf("洪江")!=-1 || areaName.indexOf("会同")!=-1 || areaName.indexOf("沅陵")!=-1 || areaName.indexOf("辰溪")!=-1 || areaName.indexOf("溆浦")!=-1
                || areaName.indexOf("中方")!=-1 || areaName.indexOf("新晃")!=-1 || areaName.indexOf("芷江")!=-1 || areaName.indexOf("通道")!=-1
                || areaName.indexOf("靖州")!=-1 || areaName.indexOf("麻阳")!=-1 || areaName.indexOf("鹤城区")!=-1) {
            areaName="怀化" + areaName;
        }
        if(areaName.indexOf("冷水江")!=-1 || areaName.indexOf("涟源")!=-1 || areaName.indexOf("新化")!=-1 || areaName.indexOf("双峰")!=-1
                || areaName.indexOf("娄星区")!=-1) {
            areaName="娄底" + areaName;
        }
        if(areaName.indexOf("吉首")!=-1 || areaName.indexOf("古丈")!=-1 || areaName.indexOf("龙山")!=-1 || areaName.indexOf("永顺")!=-1 || areaName.indexOf("凤凰")!=-1
                || areaName.indexOf("泸溪")!=-1 || areaName.indexOf("保靖")!=-1 || areaName.indexOf("花垣")!=-1) {
            areaName="湘西土家族苗族自治州" + areaName;
        }
        if(areaName.indexOf("湘西")!=-1) {
            areaName="湘西土家族苗族自治州";
        }
        return areaName;
    }


    public static String hunanArea(String val) {
        String areaName = val;
        if(areaName.indexOf("岳麓区")!=-1 || areaName.indexOf("芙蓉区")!=-1 || areaName.indexOf("天心区")!=-1
                || areaName.indexOf("开福区")!=-1 || areaName.indexOf("雨花区")!=-1) {
            areaName="长沙市";
        }
        if(areaName.indexOf("天元区")!=-1 || areaName.indexOf("荷塘区")!=-1 || areaName.indexOf("芦淞区")!=-1
                || areaName.indexOf("石峰区")!=-1) {
            areaName="株洲市";
        }
        if(areaName.indexOf("岳塘区")!=-1 || areaName.indexOf("雨湖区")!=-1 ) {
            areaName="湘潭市";
        }
        if(areaName.indexOf("南岳区")!=-1 || areaName.indexOf("雁峰区")!=-1 || areaName.indexOf("珠晖区")!=-1
                || areaName.indexOf("石鼓区")!=-1 || areaName.indexOf("蒸湘区")!=-1 || areaName.indexOf("南岳区")!=-1) {
            areaName="衡阳市";
        }
        if(areaName.indexOf("大祥区")!=-1 || areaName.indexOf("北塔区")!=-1 || areaName.indexOf("市直单位")!=-1) {
            areaName="邵阳市";
        }
        if(areaName.indexOf("岳阳楼区")!=-1 || areaName.indexOf("君山区")!=-1 || areaName.indexOf("云溪区")!=-1) {
            areaName="岳阳市";
        }
        if(areaName.indexOf("武陵区")!=-1 || areaName.indexOf("鼎城区")!=-1) {
            areaName="常德市";
        }
        if(areaName.indexOf("永定区")!=-1 || areaName.indexOf("武陵源区")!=-1) {
            areaName="张家界市";
        }
        if(areaName.indexOf("赫山区")!=-1 || areaName.indexOf("资阳区")!=-1) {
            areaName="益阳市";
        }
        if(areaName.indexOf("北湖区")!=-1 || areaName.indexOf("苏仙区")!=-1) {
            areaName="郴州市";
        }
        if(areaName.indexOf("冷水滩区")!=-1 || areaName.indexOf("零陵区")!=-1) {
            areaName="永州市";
        }
        if(areaName.indexOf("鹤城区")!=-1) {
            areaName="怀化市";
        }
        if(areaName.indexOf("冷水滩区")!=-1 || areaName.indexOf("娄星区")!=-1) {
            areaName="娄底市";
        }
        if(areaName.indexOf("湘西经济开发区")!=-1 || areaName.indexOf("州本级")!=-1) {
            areaName="吉首市";
        }
        return areaName;
    }
}
