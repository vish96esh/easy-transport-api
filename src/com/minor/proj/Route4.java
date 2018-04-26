package com.minor.proj;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONArray;

public class Route4 {
	
	int R=6371;
	
	public JSONObject getMixedData(double lat1, double lon1, double lat2, double lon2) throws Exception, SQLException, ClassNotFoundException, JSONException
	{
		Type1 arr[];
		arr=calc(lat1,lon1,-1);//source
		int stop_id1=arr[0].id;
		double dist1=arr[0].dist;
		arr=calc(lat2, lon2,-1);//destination
		int stop_id2=arr[0].id;
		double dist2=arr[0].dist;
		Type4 x=new Type4();
		Route2 r2=new Route2();
		Route5 r5=new Route5();
		Route3 r3=new Route3();
		Route1 r1=new Route1();
		JSONObject obj=new JSONObject();
		if(dist1<0.5)
		{
			x=r2.getData(stop_id1, dist1, stop_id2, dist2, 2);
			JSONArray arr_path, temp1;
			JSONArray arr_route=x.arr;
			double wlat2, wlon2;
			double total_dist=0.0, total_time=0.0;
			int total_price=0;
			String wto;
			wlat2=arr_route.getJSONObject(0).getJSONArray("path").getJSONObject(0).getDouble("from_lat");
			wlon2=arr_route.getJSONObject(0).getJSONArray("path").getJSONObject(0).getDouble("from_long");
			wto=arr_route.getJSONObject(0).getJSONArray("path").getJSONObject(0).getString("from");
			temp1=new JSONArray();
			JSONObject temp_obj=r5.getWalkData(lat1, lon1, wlat2, wlon2, 1);
			total_dist+=temp_obj.getDouble("distance");
			total_time+=temp_obj.getDouble("time");
			temp_obj.remove("to");
			temp_obj.put("to", wto);
			temp1.put(temp_obj);
			int i,j,k,ctr=1,count=0;
			double temp_lat1=0.0, temp_lon1=0.0;
			String temp_str="";
			double temp_time, temp_dist;
			JSONArray final_arr=new JSONArray();
			for(i=0;i<arr_route.length();i++)
			{
				JSONArray temp=new JSONArray();
				temp=concatArray(temp,temp1);
				temp_time=total_time;
				temp_dist=total_dist;
				arr_path=arr_route.getJSONObject(i).getJSONArray("path");
				for(j=0;j<arr_path.length();j++)
				{
					temp.put(arr_path.getJSONObject(j));
					if(j==arr_path.length()-1)
					{
						temp_lat1=arr_path.getJSONObject(j).getDouble("to_lat");
						temp_lon1=arr_path.getJSONObject(j).getDouble("to_long");
						temp_str=arr_path.getJSONObject(j).getString("to");
					}
				}
				count=j;
				temp_time+=arr_route.getJSONObject(i).getDouble("total_time");
				total_price=x.price;
				if(dist2<0.5)
				{
					JSONObject walk_obj=r5.getWalkData(temp_lat1, temp_lon1, lat2, lon2, 2+j);
					walk_obj.remove("from");
					walk_obj.put("from", temp_str);
					temp_time+=walk_obj.getDouble("time");
					temp_dist+=walk_obj.getDouble("distance");
					temp.put(walk_obj);
					JSONObject final_obj1=new JSONObject();
					final_obj1.put("path", temp);
					final_obj1.put("total_no_of_stops", 0);
					final_obj1.put("total_time", temp_time);
					final_obj1.put("total_dist", temp_dist);
					final_obj1.put("price", total_price);
					final_obj1.put("route_no", ctr++);
					final_arr.put(final_obj1);
				}
				else if(dist2<2.0)
				{
					JSONObject auto=r3.getAutoRoute(temp_lat1, temp_lon1, lat2, lon2, 2+j);
					JSONObject auto_obj=auto.getJSONArray("routes").getJSONObject(0).getJSONArray("path").getJSONObject(0);
					auto_obj.remove("from");
					auto_obj.put("from", temp_str);
					temp_time+=auto_obj.getDouble("time");
					temp_dist+=auto_obj.getDouble("distance");
					total_price+=auto.getJSONArray("routes").getJSONObject(0).getInt("price");
					temp.put(auto_obj);
					JSONObject final_obj1=new JSONObject();
					final_obj1.put("path", temp);
					final_obj1.put("total_no_of_stops", 0);
					final_obj1.put("total_time", temp_time);
					final_obj1.put("total_dist", temp_dist);
					final_obj1.put("price", total_price);
					final_obj1.put("route_no", ctr++);
					final_arr.put(final_obj1);
				}
				else
				{
					Type4 x1=r1.getData(temp_lat1, temp_lon1, lat2, lon2, j+4);
					JSONArray bus_route_arr=x1.arr;
					double temp_flat=0.0, temp_flong=0.0;
					String temp_fstr="";
					double temp_dist1=0.0, temp_time1=0.0;
					int temp_price;
					for(k=0;k<bus_route_arr.length();k++)
					{
						JSONArray temp2=new JSONArray();
						temp2=concatArray(temp2,temp);
						temp_dist1=temp_dist;
						temp_time1=temp_time;
						JSONArray bus_path=bus_route_arr.getJSONObject(k).getJSONArray("path");
						temp_flat=bus_path.getJSONObject(0).getDouble("from_lat");
						temp_flong=bus_path.getJSONObject(0).getDouble("from_long");
						temp_fstr=bus_path.getJSONObject(0).getString("from");
						JSONObject walk_obj=r5.getWalkData(temp_lat1, temp_lon1, temp_flat, temp_flong, count+3);
						walk_obj.remove("from");
						walk_obj.put("from", temp_str);
						walk_obj.remove("to");
						walk_obj.put("to", temp_fstr);
						temp_dist1+=walk_obj.getDouble("distance");
						temp_time1+=walk_obj.getDouble("time");
						temp2.put(walk_obj);
						for(j=0;j<bus_path.length();j++)
						{
							temp2.put(bus_path.getJSONObject(j));
							if(j==bus_path.length()-1)
							{
								temp_flat=bus_path.getJSONObject(j).getDouble("to_lat");
								temp_flong=bus_path.getJSONObject(j).getDouble("to_long");
								temp_fstr=bus_path.getJSONObject(j).getString("to");
							}
						}
						temp_price=total_price+bus_route_arr.getJSONObject(k).getInt("price");
						temp_dist1+=bus_route_arr.getJSONObject(k).getDouble("total_dist");
						temp_time1+=bus_route_arr.getJSONObject(k).getDouble("total_time");
						walk_obj=r5.getWalkData(temp_flat, temp_flong, lat2, lon2, count+4+j);
						temp_dist1+=walk_obj.getDouble("distance");
						temp_time1+=walk_obj.getDouble("time");
						walk_obj.remove("from");
						walk_obj.put("from", temp_fstr);
						temp2.put(walk_obj);
						JSONObject final_obj1=new JSONObject();
						final_obj1.put("path", temp2);
						final_obj1.put("total_no_of_stops", 0);
						final_obj1.put("total_time", temp_time1);
						final_obj1.put("total_dist", temp_dist1);
						final_obj1.put("price", temp_price);
						final_obj1.put("route_no", ctr++);
						final_arr.put(final_obj1);
					}
				}
			}
			JSONObject final_obj=new JSONObject();
			final_obj.put("routes", final_arr);
			final_obj.put("results", ctr-1);
			JSONObject min_price_obj=new JSONObject();
			min_price_obj.put("route_no", 0);
			min_price_obj.put("price", 0);
			final_obj.put("min_price", min_price_obj);
			return final_obj;
		}
		else if(dist1<2.0)
		{
			x=r2.getData(stop_id1, dist1, stop_id2, dist2, 2);
			
			JSONArray arr_path, temp1;
			JSONArray arr_route=x.arr;
			
			double wlat2, wlon2;
			double total_dist=0.0, total_time=0.0;
			int total_price=0;
			String wto;
			wlat2=arr_route.getJSONObject(0).getJSONArray("path").getJSONObject(0).getDouble("from_lat");
			wlon2=arr_route.getJSONObject(0).getJSONArray("path").getJSONObject(0).getDouble("from_long");
			wto=arr_route.getJSONObject(0).getJSONArray("path").getJSONObject(0).getString("from");
			temp1=new JSONArray();
			//System.out.println(wlat2 + "," + wlon2);
			JSONObject temp_obj=r3.getAutoRoute(lat1, lon1, wlat2, wlon2, 1).getJSONArray("routes").getJSONObject(0);
			//System.out.println(temp_obj.toString());
			total_dist+=temp_obj.getJSONArray("path").getJSONObject(0).getDouble("distance");
			total_time+=temp_obj.getJSONArray("path").getJSONObject(0).getDouble("time");
			total_price+=temp_obj.getInt("price");
			temp_obj.getJSONArray("path").getJSONObject(0).remove("to");
			temp_obj.getJSONArray("path").getJSONObject(0).put("to", wto);
			temp1.put(temp_obj.getJSONArray("path").getJSONObject(0));
			int i,j,k,ctr=1,count=0;
			double temp_lat1=0.0, temp_lon1=0.0;
			String temp_str="";
			double temp_time, temp_dist;
			int temp_price;
			JSONArray final_arr=new JSONArray();
			
			for(i=0;i<arr_route.length();i++)
			{
				JSONArray temp=new JSONArray();
				temp=concatArray(temp1,temp);
				temp_time=total_time;
				temp_dist=total_dist;
				temp_price=total_price;
				arr_path=arr_route.getJSONObject(i).getJSONArray("path");
				for(j=0;j<arr_path.length();j++)
				{
					temp.put(arr_path.getJSONObject(j));
					if(j==arr_path.length()-1)
					{
						temp_lat1=arr_path.getJSONObject(j).getDouble("to_lat");
						temp_lon1=arr_path.getJSONObject(j).getDouble("to_long");
						temp_str=arr_path.getJSONObject(j).getString("to");
					}
				}
				count=j;
				temp_time+=arr_route.getJSONObject(i).getDouble("total_time");
				temp_price+=x.price;
				if(dist2<0.5)
				{
					JSONObject walk_obj=r5.getWalkData(temp_lat1, temp_lon1, lat2, lon2, 2+j);
					walk_obj.remove("from");
					walk_obj.put("from", temp_str);
					temp_time+=walk_obj.getDouble("time");
					temp_dist+=walk_obj.getDouble("distance");
					temp.put(walk_obj);
					JSONObject final_obj1=new JSONObject();
					final_obj1.put("path", temp);
					final_obj1.put("total_no_of_stops", 0);
					final_obj1.put("total_time", temp_time);
					final_obj1.put("total_dist", temp_dist);
					final_obj1.put("price", temp_price);
					final_obj1.put("route_no", ctr++);
					final_arr.put(final_obj1);
				}
				else if(dist2<2.0)
				{
					JSONObject auto=r3.getAutoRoute(temp_lat1, temp_lon1, lat2, lon2, 2+j);
					JSONObject auto_obj=auto.getJSONArray("routes").getJSONObject(0).getJSONArray("path").getJSONObject(0);
					auto_obj.remove("from");
					auto_obj.put("from", temp_str);
					temp_time+=auto_obj.getDouble("time");
					temp_dist+=auto_obj.getDouble("distance");
					temp_price+=auto.getJSONArray("routes").getJSONObject(0).getInt("price");
					temp.put(auto_obj);
					JSONObject final_obj1=new JSONObject();
					final_obj1.put("path", temp);
					final_obj1.put("total_no_of_stops", 0);
					final_obj1.put("total_time", temp_time);
					final_obj1.put("total_dist", temp_dist);
					final_obj1.put("price", temp_price);
					final_obj1.put("route_no", ctr++);
					final_arr.put(final_obj1);
				}
				else
				{
					Type4 x1=r1.getData(temp_lat1, temp_lon1, lat2, lon2, j+3);
					JSONArray bus_route_arr=x1.arr;
					//System.out.println(bus_route_arr.toString());
					double temp_flat=0.0, temp_flong=0.0;
					String temp_fstr="";
					double temp_dist1=0.0, temp_time1=0.0;
					int temp_price1;
					for(k=0;k<bus_route_arr.length();k++)
					{
						JSONArray temp2=new JSONArray();
						temp2=concatArray(temp,temp2);
						temp_dist1=temp_dist;
						temp_time1=temp_time;
						JSONArray bus_path=bus_route_arr.getJSONObject(k).getJSONArray("path");
						temp_flat=bus_path.getJSONObject(0).getDouble("from_lat");
						temp_flong=bus_path.getJSONObject(0).getDouble("from_long");
						temp_fstr=bus_path.getJSONObject(0).getString("from");
						JSONObject walk_obj=r5.getWalkData(temp_lat1, temp_lon1, temp_flat, temp_flong, count+2);
						walk_obj.remove("from");
						walk_obj.put("from", temp_str);
						walk_obj.remove("to");
						walk_obj.put("to", temp_fstr);
						temp_dist1+=walk_obj.getDouble("distance");
						temp_time1+=walk_obj.getDouble("time");
						temp2.put(walk_obj);
						for(j=0;j<bus_path.length();j++)
						{
							temp2.put(bus_path.getJSONObject(j));
							if(j==bus_path.length()-1)
							{
								temp_flat=bus_path.getJSONObject(j).getDouble("to_lat");
								temp_flong=bus_path.getJSONObject(j).getDouble("to_long");
								temp_fstr=bus_path.getJSONObject(j).getString("to");
							}
						}
						temp_price1=temp_price+bus_route_arr.getJSONObject(k).getInt("price");
						temp_dist1+=bus_route_arr.getJSONObject(k).getDouble("total_dist");
						temp_time1+=bus_route_arr.getJSONObject(k).getDouble("total_time");
						walk_obj=r5.getWalkData(temp_flat, temp_flong, lat2, lon2, count+4+j);
						temp_dist1+=walk_obj.getDouble("distance");
						temp_time1+=walk_obj.getDouble("time");
						walk_obj.remove("from");
						walk_obj.put("from", temp_fstr);
						temp2.put(walk_obj);
						JSONObject final_obj1=new JSONObject();
						final_obj1.put("path", temp2);
						final_obj1.put("total_no_of_stops", 0);
						final_obj1.put("total_time", temp_time1);
						final_obj1.put("total_dist", temp_dist1);
						final_obj1.put("price", temp_price1);
						final_obj1.put("route_no", ctr++);
						final_arr.put(final_obj1);
					}
				}
			}
			JSONObject final_obj=new JSONObject();
			final_obj.put("routes", final_arr);
			final_obj.put("results", ctr-1);
			JSONObject min_price_obj=new JSONObject();
			min_price_obj.put("route_no", 0);
			min_price_obj.put("price", 0);
			final_obj.put("min_price", min_price_obj);
			return final_obj;
		}
		else
		{
			if(dist2>2.0)
			{
				obj=r1.getBusRoute(lat1, lon1, lat2, lon2);
				return obj;
			}
			Class.forName("com.mysql.jdbc.Driver");  
			Connection con=DriverManager.getConnection("jdbc:mysql://localhost:3306/minor","root","vishesh");
			Statement stmt=con.createStatement();
			ResultSet rs=stmt.executeQuery("select latitude,longitude,name from stops where id="+stop_id1);
			rs.next();
			double wlat2=rs.getDouble("latitude");
			double wlon2=rs.getDouble("longitude");
			Type4 x1=r1.getData(lat1, lon1, wlat2, wlon2, 2);
			JSONArray bus_route_arr=x1.arr;
			int i,j,k,flag;
			JSONArray temp=new JSONArray();
			double temp_lat=0.0, temp_long=0.0;
			String temp_to="";
			double from_lat=0.0, from_long=0.0;
			String from="";
			int total_no_of_stops=0, total_price=0;
			double total_time=0.0, total_dist=0.0;
			int ctr=1;
			JSONArray route=new JSONArray();
			for(i=0;i<bus_route_arr.length();i++)
			{
				total_no_of_stops=0;
				total_dist=0.0;
				total_price=0;
				total_time=0.0;
				JSONObject obj1=bus_route_arr.getJSONObject(i);
				JSONArray path_arr=obj1.getJSONArray("path");
				total_no_of_stops+=obj1.getInt("total_no_of_stops");
				total_dist+=obj1.getDouble("total_dist");
				total_time+=obj1.getDouble("total_time");
				total_price+=obj1.getInt("price");
				from_lat=path_arr.getJSONObject(0).getDouble("from_lat");
				from_long=path_arr.getJSONObject(0).getDouble("from_long");
				from=path_arr.getJSONObject(0).getString("from");
				JSONObject walk_obj=r5.getWalkData(lat1, lon1, from_lat, from_long, 1);
				walk_obj.remove("to");
				walk_obj.put("to", from);
				total_dist+=walk_obj.getDouble("distance");
				total_time+=walk_obj.getDouble("time");
				temp.put(walk_obj);
				for(j=0;j<path_arr.length();j++)
				{
					temp.put(path_arr.getJSONObject(j));
					if(j==path_arr.length()-1)
					{
						temp_lat=path_arr.getJSONObject(j).getDouble("to_lat");
						temp_long=path_arr.getJSONObject(j).getDouble("to_long");
						temp_to=path_arr.getJSONObject(j).getString("to");
					}
				}
				walk_obj=r5.getWalkData(temp_lat, temp_long, wlat2, wlon2, j+2);
				walk_obj.remove("from");
				walk_obj.put("from", temp_to);
				walk_obj.remove("to");
				walk_obj.put("to", rs.getString("name"));
				total_time+=walk_obj.getDouble("time");
				total_dist+=walk_obj.getDouble("distance");
				temp.put(walk_obj);
				Type4 metro_x=r2.getData(stop_id1, 0, stop_id2, 0, j+3);
				flag=j+3;
				JSONArray metro_route_arr=metro_x.arr;
				total_price+=metro_x.price;
				int temp_price, temp_no_of_stops;
				double temp_time, temp_dist;
				for(k=0;k<metro_route_arr.length();k++)
				{
					JSONArray temp1=new JSONArray();
					temp1=concatArray(temp1,temp);
					temp_price=total_price;
					temp_no_of_stops=total_no_of_stops;
					temp_time=total_time;
					temp_dist=total_dist;
					JSONObject metro_obj=metro_route_arr.getJSONObject(k);
					temp_no_of_stops+=metro_obj.getInt("total_no_of_stops");
					temp_time+=metro_obj.getDouble("total_time");
					JSONArray metro_path_arr=metro_obj.getJSONArray("path");
					for(j=0;j<metro_path_arr.length();j++)
					{
						temp1.put(metro_path_arr.getJSONObject(j));
						if(j==metro_path_arr.length()-1)
						{
							temp_lat=metro_path_arr.getJSONObject(j).getDouble("to_lat");
							temp_long=metro_path_arr.getJSONObject(j).getDouble("to_long");
							temp_to=metro_path_arr.getJSONObject(j).getString("to");
						}
					}
					if(dist2<0.5)
					{
						JSONObject walk=r5.getWalkData(temp_lat, temp_long, lat2, lon2, j+3+flag);
						walk.remove("from");
						walk.put("from", temp_to);
						temp_time+=walk.getDouble("time");
						temp_dist+=walk.getDouble("distance");
						temp1.put(walk);
					}
					else if(dist2<2.0)
					{
						JSONObject auto=r3.getAutoRoute(temp_lat, temp_long, lat2, lon2, j+3+flag);
						JSONObject auto_obj=auto.getJSONArray("routes").getJSONObject(0).getJSONArray("path").getJSONObject(0);
						temp_dist+=auto_obj.getDouble("distance");
						temp_time+=auto_obj.getDouble("time");
						temp_price+=auto.getJSONArray("routes").getJSONObject(0).getInt("price");
						auto_obj.remove("from");
						auto_obj.put("from", temp_to);
						temp1.put(auto_obj);
					}
					JSONObject final_obj1=new JSONObject();
					final_obj1.put("path", temp);
					final_obj1.put("total_no_of_stops", temp_no_of_stops);
					final_obj1.put("total_time", temp_time);
					final_obj1.put("total_dist", temp_dist);
					final_obj1.put("price", temp_price);
					final_obj1.put("route_no", ctr++);
					route.put(final_obj1);
				}
			}
			JSONObject final_obj=new JSONObject();
			final_obj.put("results", ctr-1);
			final_obj.put("routes", route);
			JSONObject min_price_obj=new JSONObject();
			min_price_obj.put("route_no", 0);
			min_price_obj.put("price", 0);
			final_obj.put("min_price", min_price_obj);
			return final_obj;
		}
	}
	
	public Type1[] calc(double lat1,double lon1,double v) throws SQLException, Exception
	{
		Connection con;
		Statement stmt1;
		ResultSet rs1;
		Type1 arr[]=new Type1[50];
		arr[0]=new Type1();
		double dLon,dLat,d;
		String str=(v==-1)?" where type='m'":" where type='b'";
		int i=0;
		try
		{  
			Class.forName("com.mysql.jdbc.Driver");  
			con=DriverManager.getConnection("jdbc:mysql://localhost:3306/minor","root","vishesh");
			stmt1=con.createStatement();
			String sql1="select * from stops"+str;	                    
			rs1=stmt1.executeQuery(sql1);
			while(rs1.next())
			{
				dLat = deg2rad(rs1.getDouble("latitude")-lat1); 
				if(dLat<0)
					dLat=dLat*-1;
         		dLon = deg2rad(lon1-rs1.getDouble("longitude")); 
	            if(dLon<0)
	                dLon=dLon*-1;
	            double a = Math.sin(dLat/2) * Math.sin(dLat/2) +Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(rs1.getDouble("latitude"))) *Math.sin(dLon/2) * Math.sin(dLon/2); 
	            double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a)); 
	            d = R * c; // Distance in km
				/*dLat=rs1.getDouble("latitude");
				dLon=rs1.getDouble("longitude");
         		String add1=lat1+","+lon1;
         		String add2=dLat+","+dLon;
         		d=parse2(add1, add2, 2);*/
	            if(v==-1)
	            {
	            	if(arr[0].id==0)
	            	{
	            		arr[0].id=rs1.getInt("id");
	            		arr[0].dist=d;
	            	}
	            	else if(d<arr[0].dist)
	            	{
	            		arr[0].id=rs1.getInt("id");
	            		arr[0].dist=d;
	            	}
	            }
	            else if(d<=v)
	            {
	            	arr[i]=new Type1();
	            	arr[i].id=rs1.getInt("id");
	            	arr[i].dist=d;
	            	i++;
    			}
	    	}
        }           
        catch(NullPointerException ee)
        {
        	System.out.println("NUll pointer");
        }
		catch(SQLException s)
		{
			System.out.println("connection error");
		}
		catch(ClassNotFoundException e)
		{
			System.out.println(e);
		}
        return arr;
   	}
	
	public JSONArray concatArray(JSONArray arr1, JSONArray arr2) throws JSONException {
	    JSONArray result = new JSONArray();
	    for (int i = 0; i < arr1.length(); i++) {
	        result.put(arr1.get(i));
	    }
	    for (int i = 0; i < arr2.length(); i++) {
	        result.put(arr2.get(i));
	    }
	    return result;
	}
	
	public double  deg2rad(Double deg) {
		return deg * (Math.PI/180);
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
