package com.byf.map;

import com.alibaba.fastjson.JSONObject;
import com.byf.entity.UseTypeInfo;
import com.byf.kafka.KafkaEvent;
import com.byf.log.ScanProductLog;
import com.byf.util.HbaseUtils;
import com.byf.util.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.util.Collector;

import java.util.HashMap;
import java.util.Map;

public class UseTypeMap implements FlatMapFunction<KafkaEvent, UseTypeInfo>  {

    @Override
    public void flatMap(KafkaEvent kafkaEvent, Collector<UseTypeInfo> collector) throws Exception {
            String data = kafkaEvent.getWord();
            ScanProductLog scanProductLog = JSONObject.parseObject(data,ScanProductLog.class);
            int userid = scanProductLog.getUserId();
            int usetype = scanProductLog.getUserType();////终端类型：0、pc端；1、移动端；2、小程序端
            String usetypename = usetype == 0?"pc端":usetype == 1?"移动端":"小程序端";
            String tablename = "userflaginfo";
            String rowkey = userid+"";
            String famliyname = "userbehavior";
            String colum = "usetypelist";//运营
            String mapdata = HbaseUtils.getdata(tablename,rowkey,famliyname,colum);
            Map<String,Long> map = new HashMap<String,Long>();
            if(StringUtils.isNotBlank(mapdata)){
                map = JSONObject.parseObject(mapdata,Map.class);
            }
            //获取之前的终端偏好
            String maxpreusetype = MapUtils.getMaxMap(map);

            long preusetype = map.get(usetypename)==null?0L:map.get(usetypename);
            map.put(usetypename,preusetype+1);
            String finalstring = JSONObject.toJSONString(map);
            HbaseUtils.putdata(tablename,rowkey,famliyname,colum,finalstring);

            String maxusetype = MapUtils.getMaxMap(map);
            if(StringUtils.isNotBlank(maxusetype)&&!maxpreusetype.equals(maxusetype)){
                UseTypeInfo useTypeInfo = new UseTypeInfo();
                useTypeInfo.setUsetype(maxpreusetype);
                useTypeInfo.setCount(-1L);
                useTypeInfo.setGroupbyfield("==usetypeinfo=="+maxpreusetype);
                collector.collect(useTypeInfo);
            }

            UseTypeInfo useTypeInfo = new UseTypeInfo();
            useTypeInfo.setUsetype(maxusetype);
            useTypeInfo.setCount(1L);
            useTypeInfo.setGroupbyfield("==usetypeinfo=="+maxusetype);
            collector.collect(useTypeInfo);
            colum = "usetype";
            HbaseUtils.putdata(tablename,rowkey,famliyname,colum,maxusetype);

    }

}
