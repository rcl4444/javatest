package cn.zhijian.passport.domain.commandhandlers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.params.CoreConnectionPNames;
import org.axonframework.commandhandling.CommandHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.zhijian.passport.commands.SendSMSCommand;
import cn.zhijian.passport.commands.ValidateSMSCodeCommand;
import cn.zhijian.passport.ssl.MySSLSocketFactory;

public class SMSCommandHandler {
	private static Logger logger = LoggerFactory.getLogger(SMSCommandHandler.class);
	private static String YbgUrl = "https://api.leancloud.cn/1.1";
	private static String SendVerificationCode = "/requestSmsCode";
	private static String ValidateCode = "/verifySmsCode";
	
	public SMSCommandHandler() {
	}

	@CommandHandler
	public boolean SendVerificationCode(SendSMSCommand cmd) {
		try {
			String msg = "{\"mobilePhoneNumber\": \"" + cmd.getMobile() + "\"}";
			return Post(YbgUrl + SendVerificationCode, msg);
		} catch (Exception e) {
			return false;
		}
	}
	
	@CommandHandler
	public boolean ValidateCode(ValidateSMSCodeCommand cmd)
	{
		String Url  = YbgUrl+ValidateCode+ "/"+ cmd.getCode() + "?mobilePhoneNumber="+cmd.getMobile();
		try {
			return Post(Url, "");
		} catch (Exception e) {
			return false;
		}
	}

	public boolean Post(String url, String data) {
		HttpPost post = null;
		try {
			HttpClient httpClient = MySSLSocketFactory.getNewHttpClient();
			// 设置超时时间
			httpClient.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 2000);
			httpClient.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, 2000);
			post = new HttpPost(url);
			// 构造消息头
			post.setHeader("Content-type", "application/json; charset=utf-8");
			post.setHeader("X-LC-Id", "m42u0c4ahfmhanylrmrxu3kj2wje4c3lam1uj00dbc4yn3lq");
			post.setHeader("X-LC-Key", "9qbyhlj3er1akh2jmgvs0xhb0isv6t35meg7v8ai512lj2hh");
			post.setEntity(new ByteArrayEntity(data.getBytes()));
			HttpResponse response = httpClient.execute(post);
			// 检验返回码
			int statusCode = response.getStatusLine().getStatusCode();
			if (statusCode != HttpStatus.SC_OK) {
				logger.info("请求出错: " + statusCode);
				return false;
			} 
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		} finally {
			if (post != null) {
				try {
					// post.releaseConnection();
					Thread.sleep(500);
				} catch (InterruptedException e) {
					e.printStackTrace();
					return false;
				}
			}
		}
		return true;
	}
	
	public static String convertStreamToString(InputStream is) {      
        /*  
          * To convert the InputStream to String we use the BufferedReader.readLine()  
          * method. We iterate until the BufferedReader return null which means  
          * there's no more data to read. Each line will appended to a StringBuilder  
          * and returned as String.  
          */     
         BufferedReader reader = new BufferedReader(new InputStreamReader(is));      
         StringBuilder sb = new StringBuilder();      
     
         String line = null;      
        try {      
            while ((line = reader.readLine()) != null) {      
                 sb.append(line + "\n");      
             }      
         } catch (IOException e) {      
             e.printStackTrace();      
         } finally {      
            try {      
                 is.close();      
             } catch (IOException e) {      
                 e.printStackTrace();      
             }      
         }      
     
        return sb.toString();      
     }
}
