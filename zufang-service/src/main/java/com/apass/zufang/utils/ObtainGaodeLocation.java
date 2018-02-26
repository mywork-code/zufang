package com.apass.zufang.utils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import com.apass.gfb.framework.utils.GsonUtils;
import com.apass.zufang.domain.common.CoordinateAddress;
import com.apass.zufang.domain.common.GaodeLocation;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;


public class ObtainGaodeLocation {
	
	private static final Logger logger = LoggerFactory.getLogger(ObtainGaodeLocation.class);
	
	/**
	 * key值
	 */
	@Value("${gaode.location.key}")
	private static String KEY;

	private static Pattern pattern = Pattern.compile("\"location\":\"(\\d+\\.\\d+),(\\d+\\.\\d+)\"");
 
	

	public static GaodeLocation addressToGPS(String address) {

		try {

			String url = String.format("http://restapi.amap.com/v3/geocode/geo?&s=rsv3&address=%s&key=%s", address,
					KEY);

			URL myURL = null;

			URLConnection httpsConn = null;

			try {

				myURL = new URL(url);

			} catch (MalformedURLException e) {

				e.printStackTrace();

			}

			InputStreamReader insr = null;

			BufferedReader br = null;

			httpsConn = (URLConnection) myURL.openConnection();// 不使用代理

			if (httpsConn != null) {

				insr = new InputStreamReader(httpsConn.getInputStream(), "UTF-8");

				br = new BufferedReader(insr);

				String data = "";

				String line = null;

				while ((line = br.readLine()) != null) {

					data += line;

				}
				
				GaodeLocation jsonGaode= GsonUtils.convertObj(data, GaodeLocation.class);
				
				return jsonGaode;
			}
		} catch (Exception e) {

			e.printStackTrace();

			return null;
		}

		return null;

	}
	public static CoordinateAddress getAdd(String log, String lat ){  
		
		CoordinateAddress result=new CoordinateAddress();
        //lat 小  log  大  
        //参数解释: 纬度,经度 type 001 (100代表道路，010代表POI，001代表门址，111可以同时显示前三项)  
        String urlString = "http://gc.ditu.aliyun.com/regeocoding?l="+lat+","+log+"&type=010";  
        String res = "";     
        try {     
            URL url = new URL(urlString);    
            java.net.HttpURLConnection conn = (java.net.HttpURLConnection)url.openConnection();    
            conn.setDoOutput(true);    
            conn.setRequestMethod("POST");    
            java.io.BufferedReader in = new java.io.BufferedReader(new java.io.InputStreamReader(conn.getInputStream(),"UTF-8"));    
            String line;    
           while ((line = in.readLine()) != null) {    
               res += line+"\n";    
         }    
            in.close();    
        } catch (Exception e) {    
            System.out.println("error in wapaction,and e is " + e.getMessage());    
        }   
    	JSONObject jsonObject = JSONObject.fromObject(res);  
        JSONArray jsonArray = JSONArray.fromObject(jsonObject.getString("addrList"));  
        JSONObject j_2 = JSONObject.fromObject(jsonArray.get(0));  
        int status = j_2.getInt("status");  
        if(status==1){
        	 result.setName(j_2.getString("name"));
        	 result.setId(j_2.getString("id"));
        	 result.setAdmCode(j_2.getString("admCode"));
        	 
        	 String allAdd = j_2.getString("admName");
        	 String arr[] = allAdd.split(",");  
        	 result.setProvince(arr[0]);
        	 result.setCity(arr[1]);
        	 result.setArea(arr[2]);
        	 result.setAddr(j_2.getString("addr"));
        	 result.setDistance(j_2.getDouble("distance"));
        	 
        }else{
        	 return null; 
        }
        return result;    
    } 
	
	
	

//	public static void main(String[] args) {
//		
//		GaodeLocation data = ObtainGaodeLocation.addressToGPS("上海市东方明珠广播电视塔有限公司");
//		
//		CoordinateAddress add = getAdd("121.499361", "31.240229");
//
//		System.out.println("经度,纬度:" + data.getGeocodes().get(0).getLocation());
//
//	}
	
	
}
