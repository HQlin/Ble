package com.example.lin.http;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

public class HttpProtocol {
	/**
	 * ÷¥––HttpPost–≠“È
	 * @param uri
	 * @param params
	 * @return
	 */
	public static String gotoHttpPost(String uri,List params){
		String result = null;		
		HttpPost httpPost = new HttpPost(uri);

		try {
			httpPost.setEntity(new UrlEncodedFormEntity(params,HTTP.UTF_8));
			HttpResponse httpResponse = new DefaultHttpClient().execute(httpPost);
			
			if (httpResponse.getStatusLine().getStatusCode() == 200){
				result = EntityUtils.toString(httpResponse.getEntity());
				System.out.println(result);			
			}
		} catch(Exception e) {
			e.printStackTrace();
		}	
		return result;	
	}
}
