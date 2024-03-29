package com.byf.map;


import com.byf.entity.EmaiInfo;
import com.byf.util.EmailUtils;
import com.byf.util.HbaseUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.flink.api.common.functions.MapFunction;

public class EmailMap implements MapFunction<String, EmaiInfo>{
    @Override
    public EmaiInfo map(String s) throws Exception {
        if(StringUtils.isBlank(s)){
            return null;
        }
        String[] userinfos = s.split(",");
        String userid = userinfos[0];
        String username = userinfos[1];
        String sex = userinfos[2];
        String telphone = userinfos[3];
        String email = userinfos[4];
        String age = userinfos[5];
        String registerTime = userinfos[6];
        String usetype = userinfos[7];//'终端类型：0、pc端；1、移动端；2、小程序端'

        String emailtype = EmailUtils.getEmailtypeBy(email);

        String tablename = "userflaginfo";
        String rowkey = userid;
        String famliyname = "baseinfo";
        String colum = "emailinfo";//运营商
        HbaseUtils.putdata(tablename,rowkey,famliyname,colum,emailtype);
        EmaiInfo emailInfo = new EmaiInfo();
        String groupfield = "emailInfo=="+emailtype;
        emailInfo.setEmailtype(emailtype);
        emailInfo.setCount(1L);
        emailInfo.setGroupfield(groupfield);
        return emailInfo;
    }
}
