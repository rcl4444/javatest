package cn.zhijian.pay.util;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import cn.zhijian.pay.sdk.WXPayUtil;
import lombok.Data;

@Data
public class HttpUtil {
	
	final HttpServletRequest request;
	final HttpServletResponse response; 
	final String resXml;
	
	public HttpUtil (HttpServletRequest request, HttpServletResponse response , String resXml) 
	{
		this.request = request;
		this.response = response;
		this.resXml = resXml;
	}
	
	public Map<String, String> Request(HttpServletRequest request) throws Exception {
		
		InputStream inputStream ;  
        StringBuffer sb = new StringBuffer();  
        inputStream = request.getInputStream();  
        String s ;  
        BufferedReader in = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));  
        while ((s = in.readLine()) != null){  
            sb.append(s);  
        }  
        in.close();  
        inputStream.close();  
        
        //解析xml成map  
        Map<String, String> m = new HashMap<String, String>();  
        m = WXPayUtil.xmlToMap(sb.toString());  
        return m;
	}
	
	public void Response(String resXml, HttpServletResponse response) throws Exception{
		
        BufferedOutputStream out = new BufferedOutputStream(  
                response.getOutputStream());  
        out.write(resXml.getBytes());  
        out.flush();  
        out.close();
	}
}
