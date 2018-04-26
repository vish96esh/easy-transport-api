package com.minor.proj;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

import org.json.JSONArray;
import org.json.JSONObject;

public class Route3 {
	
	public JSONObject getAutoRoute(double lat1, double lon1, double lat2, double lon2, int start) throws Exception {
		String add1=lat1+","+lon1;
		String add2=lat2+","+lon2;
	
	    double time=parse2(add1,add2,1);
	    double dist=parse2(add1,add2,2);
	    double price=calc(dist);
	    JSONObject obj=new JSONObject();
	    JSONArray obj_arr=new JSONArray();
	    JSONObject res=new JSONObject();
	    JSONObject path=new JSONObject();
	    JSONArray arr=new JSONArray();
	    path.put("from_lat", lat1);
	    path.put("from_long", lon1);
	    path.put("to_lat", lat2);
	    path.put("to_long", lon2);
	    path.put("type", "auto");
	    path.put("time", time);
	    path.put("distance", dist);
	    path.put("from_distance", 0.0);
	    path.put("to_distance", 0.0);
	    path.put("to", "");
	    path.put("from", "");
	    path.put("path_no", start);
	    path.put("bus", "");
	    path.put("no_of_stops", 0);
	    arr.put(0,path);
	    res.put("path", arr);
	    res.put("route_no", 1);
	    res.put("total_time", time);
	    res.put("price", (int)price);
	    res.put("total_dist", dist);
	    res.put("total_no_of_stops",0);
	    obj_arr.put(0, res);
	    obj.put("routes", obj_arr);
	    JSONObject obj1=new JSONObject();
	    obj1.put("price", (int)price);
	    obj1.put("route_no", 1);
	    obj.put("min_price", obj1);
	    obj.put("results", 1);
	    return obj;
	}
	
	public double parse2(String add1, String add2, int ty) throws Exception
	{
		URL url = new URL("https://maps.googleapis.com/maps/api/distancematrix/json?origins="+ URLEncoder.encode(add1+"", "UTF-8")+"&destinations="+URLEncoder.encode(add2+"", "UTF-8")+"&mode=driving&key=AIzaSyCuLxUIqG22o6heZ6qe0N2n2rUYrs_UY0Q");
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
	    //System.out.println(jsonObj.toString());
	    JSONArray arr=jsonObj.getJSONArray("rows").getJSONObject(0).getJSONArray("elements");
	    //System.out.println(arr.toString());
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
	
	public double calc(double dist) {
		double price=25;
		dist-=1;
		if(dist>0)
			price+=dist*14;
		return price;
	}

}
