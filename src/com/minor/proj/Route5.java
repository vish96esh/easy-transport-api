package com.minor.proj;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

import org.json.JSONArray;
import org.json.JSONObject;

public class Route5 {

	public JSONObject getWalkData(double lat1, double lon1, double lat2, double lon2, int start) throws Exception
	{
		JSONObject obj=new JSONObject();
		String add1=lat1+","+lon1;
		String add2=lat2+","+lon2;
		double dist=parse2(add1,add2,2);
		double time=parse2(add1,add2,1);
		obj.put("from", "");
		obj.put("from_lat", lat1);
		obj.put("from_long", lon1);
		obj.put("from_distance", 0);
		obj.put("to", "");
		obj.put("to_lat", lat2);
		obj.put("to_long", lon2);
		obj.put("to_distance", 0);
		obj.put("distance", dist);
		obj.put("time", time);
		obj.put("path_no", start);
		obj.put("no_of_stops", 0);
		obj.put("type", "walk");
		obj.put("bus", "");
		return obj;
	}
	
	public double parse2(String add1, String add2, int ty) throws Exception
	{
		URL url = new URL("https://maps.googleapis.com/maps/api/distancematrix/json?origins="+ URLEncoder.encode(add1+"", "UTF-8")+"&destinations="+URLEncoder.encode(add2+"", "UTF-8")+"&mode=walking&key=AIzaSyCuLxUIqG22o6heZ6qe0N2n2rUYrs_UY0Q");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.connect();
        int rs=conn.getResponseCode();
        String line, outputString = "";
        if(rs == 200)
        {
        	BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        	while ((line = reader.readLine()) != null) {
        		outputString += line+"\n";
        	}
        }
	    JSONObject jsonObj = new JSONObject(outputString);
	    JSONArray arr=jsonObj.getJSONArray("rows").getJSONObject(0).getJSONArray("elements");
	    double cnt=0;
	    if(!arr.getJSONObject(0).getString("status").equals("OK"))
        {
            cnt=-1;
            return cnt;
        }
	    if(ty==1)
	    {
	    	cnt=arr.getJSONObject(0).getJSONObject("duration").getDouble("value");
	    }
	    else if(ty==2){
	    	cnt=arr.getJSONObject(0).getJSONObject("distance").getDouble("value")/1000;
	    }
	    return cnt;
	}
	
}
