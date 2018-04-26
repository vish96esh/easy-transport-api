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
import org.json.JSONObject;
import org.json.JSONException;
import org.json.JSONArray;

public class Route1 {
	
	static int R=6371;
	
	public JSONObject getBusRoute(double lat1, double lon1, double lat2, double lon2) throws Exception, SQLException, ClassNotFoundException, JSONException
	{
		
		
		JSONObject obj=new JSONObject();
		Type4 x=new Type4();
		x=getData(lat1, lon1, lat2, lon2, 1);
		obj.put("results", x.arr.length());
		obj.put("routes",x.arr);
		JSONObject obj1=new JSONObject();
		obj1.put("price", x.price);
		obj1.put("route_no", x.route_no);
		obj.put("min_price", obj1);
		return obj;
	}
	
	public Type4 getData(double lat1, double lon1, double lat2, double lon2, int start) throws Exception, SQLException, ClassNotFoundException, JSONException
	{
		Type1 arr[];
		//calculating all the bus stops in the radius of 0.5km of the latlong provided and stores it in arr
		arr=calc(lat2,lon2,0.5);//destination
		int i,id,id1,n1,count=0;
		Type1 dist[]=new Type1[50];
		Class.forName("com.mysql.jdbc.Driver");
		Connection con=DriverManager.getConnection("jdbc:mysql://localhost:3306/minor","root","vishesh");
		Statement stmt=con.createStatement();
		Statement stmt1=con.createStatement();
		ResultSet rs,rs1;
		String query="select count(*) as cnt from stops";
		rs=stmt.executeQuery(query);
		rs.next();
		int n=rs.getInt("cnt");
		query="select count(*) as cnt from bus";
		rs=stmt.executeQuery(query);
		rs.next();
		n1=rs.getInt("cnt");
		Stop stp[]=new Stop[n];
		int bus_flag[][]=new int[n1][3];
		int route[]=new int[n1];
		int top=-1;
		int stop_no;
		for(i=0;i<n;i++)
		{
			if(i<n1)
			{
				bus_flag[i][0]=-1;
				bus_flag[i][1]=-1;
				bus_flag[i][2]=-1;
			}
			stp[i]=new Stop();
		}
		for(i=0;i<arr.length;i++)
		{
			if(arr[i]==null)
				continue;
			dist[count]=new Type1();
			dist[count].id=arr[i].id;
			dist[count].dist=arr[i].dist;
			count++;
			stp[arr[i].id-1].flag=0; //indicating the destination bus stop
			stp[arr[i].id-1].bus_id=0;
			stp[arr[i].id-1].stop_no=0;
			query="select bus_id,stop_no from route where stop_id="+arr[i].id;
			//finding all the buses passing through the bus stop and the corresponding stop_no
			rs=stmt.executeQuery(query);
			while(rs.next())
			{
				id=rs.getInt("bus_id");
				stop_no=rs.getInt("stop_no");
				query="select stop_id,stop_no from route where bus_id="+id+" and stop_no<="+stop_no+" order by stop_no";
				//finding all the bus stops through which we can reach the destination bus stop by a given bus
				rs1=stmt1.executeQuery(query);
				while(rs1.next())
				{
					id1=rs1.getInt("stop_id");
					if(id1==arr[i].id)
						break;
					//if a stop is one of the destination bus stops
					if(stp[id1-1].flag==0)
						continue;
					//if a stop is already flagged
					if(stp[id1-1].flag!=-1)
					{
						//if no of stops between the stop and destination stop is greater then skip
						if(stop_no-rs1.getInt("stop_no")>=stp[id1-1].stop_no)
							continue;
					}
					//flagging the stop with the destination stop_id
					stp[id1-1].bus_id=id;
					stp[id1-1].flag=arr[i].id;
					stp[id1-1].stop_no=stop_no-rs1.getInt("stop_no");
				}
			}
		}
		
		arr=calc(lat1,lon1,0.5);//source
		for(i=0;i<arr.length;i++)
		{
			if(arr[i]==null)
				continue;
			dist[count]=new Type1();
			dist[count].id=arr[i].id;
			dist[count].dist=arr[i].dist;
			count++;
			query="select bus_id,stop_no from route where stop_id="+arr[i].id;
			//finding all the buses passing through the stop and the corresponding stop_no
			rs=stmt.executeQuery(query);
			while(rs.next())
			{
				id=rs.getInt("bus_id");
				stop_no=rs.getInt("stop_no");
				query="select stop_id,stop_no from route where bus_id="+id+" and stop_no>"+stop_no+" order by stop_no";
				rs1=stmt1.executeQuery(query);
				while(rs1.next())
				{
					id1=rs1.getInt("stop_id");
					if(stp[id1-1].flag!=-1)
					{
						if(bus_flag[id-1][0]==-1 && bus_flag[id-1][1]==-1)
						{
							bus_flag[id-1][0]=id1;
							bus_flag[id-1][1]=arr[i].id;
							bus_flag[id-1][2]=Math.abs(rs1.getInt("stop_no")-stop_no);
							route[++top]=id;
						}
						else
						{
							if(stp[id1-1].stop_no<stp[bus_flag[id-1][0]-1].stop_no)
							{
								bus_flag[id-1][0]=id1;
								bus_flag[id-1][2]=Math.abs(rs1.getInt("stop_no")-stop_no);
							}
							if(Math.abs(rs1.getInt("stop_no")-stop_no)<bus_flag[id-1][2])
							{
								bus_flag[id-1][1]=arr[i].id;
								bus_flag[id-1][2]=Math.abs(rs1.getInt("stop_no")-stop_no);
							}
						}
					}
				}
			}
		}
		
		JSONArray arr1=new JSONArray();
		int min_price=99999;
		int flag1=0;
		//System.out.println("\n"+top+"\n");
		for(int j=0;j<=top;j++)
		{
			double total_dist=0, total_time=0, dist1, time, price;
			String add1="", add2="";
			int total_price=0, total_no_of_stops=0;
			//System.out.println(route[j]);
			JSONObject obj2=new JSONObject();
			obj2.put("route_no", j+1);
			JSONArray arr2=new JSONArray();
			int ctr=0;
			//System.out.println(stp[bus_flag[route[j]-1][0]-1].flag);
			if(stp[bus_flag[route[j]-1][0]-1].flag==0)
			{
				//System.out.println("From:"+bus_flag[route[j]-1][1]+"\t To:"+bus_flag[route[j]-1][0]+"\t bus_id:"+route[j]+"\t No of stops:"+bus_flag[route[j]-1][2]+"\n\n");
				JSONObject path1=new JSONObject();
				path1.put("path_no", start);
				rs=stmt.executeQuery("select * from stops where id="+bus_flag[route[j]-1][1]);
				rs.next();
				path1.put("from", rs.getString("name"));
				path1.put("from_lat", rs.getDouble("latitude"));
				path1.put("from_long", rs.getDouble("longitude"));
				add1=rs.getDouble("latitude")+","+rs.getDouble("longitude");
				ctr=0;
				for(i=0;i<count;i++)
				{
					if(dist[i].id==bus_flag[route[j]-1][1])
					{
						ctr++;
						path1.put("from_distance",dist[i].dist);
						total_dist+=dist[i].dist;
						break;
					}
				}
				if(ctr==0)
					path1.put("from_distance", -1);
				rs=stmt.executeQuery("select * from stops where id="+bus_flag[route[j]-1][0]);
				rs.next();
				path1.put("to", rs.getString("name"));
				path1.put("to_lat", rs.getDouble("latitude"));
				path1.put("to_long", rs.getDouble("longitude"));
				add2=rs.getDouble("latitude")+","+rs.getDouble("longitude");
				ctr=0;
				for(i=0;i<count;i++)
				{
					if(dist[i].id==bus_flag[route[j]-1][1])
					{
						ctr++;
						path1.put("to_distance",dist[i].dist);
						total_dist+=dist[i].dist;
						break;
					}
				}
				if(ctr==0)
					path1.put("to_distance", -1);
				rs=stmt.executeQuery("select * from bus where id="+route[j]);
				rs.next();
				dist1=parse2(add1,add2,2);
				time=parse2(add1,add2,1);
				total_dist+=dist1;
				total_time+=time;
				path1.put("time", time);
				path1.put("distance", dist1);
				path1.put("bus", rs.getString("bus_no"));
				path1.put("type", "bus");
				path1.put("no_of_stops", bus_flag[route[j]-1][2]);
				total_no_of_stops+=bus_flag[route[j]-1][2];
				price=calc_price(dist1);
				total_price+=price;
				arr2.put(0,path1);
			}
			else
			{
				//System.out.println("From:"+bus_flag[route[j]-1][1]+"\t To:"+bus_flag[route[j]-1][0]+"\t bus_id:"+route[j]+"\t No of stops:"+bus_flag[route[j]-1][2]);
				JSONObject path1=new JSONObject();
				path1.put("path_no", start);
				rs=stmt.executeQuery("select * from stops where id="+bus_flag[route[j]-1][1]);
				rs.next();
				path1.put("from", rs.getString("name"));
				path1.put("from_lat", rs.getDouble("latitude"));
				path1.put("from_long", rs.getDouble("longitude"));
				add1=rs.getDouble("latitude")+","+rs.getDouble("longitude");
				ctr=0;
				for(i=0;i<count;i++)
				{
					if(dist[i].id==bus_flag[route[j]-1][1])
					{
						ctr++;
						path1.put("from_distance",dist[i].dist);
						total_dist+=dist[i].dist;
						break;
					}
				}
				if(ctr==0)
					path1.put("from_distance", -1);
				rs=stmt.executeQuery("select * from stops where id="+bus_flag[route[j]-1][0]);
				rs.next();
				path1.put("to", rs.getString("name"));
				path1.put("to_lat", rs.getDouble("latitude"));
				path1.put("to_long", rs.getDouble("longitude"));
				add2=rs.getDouble("latitude")+","+rs.getDouble("longitude");
				ctr=0;
				for(i=0;i<count;i++)
				{
					if(dist[i].id==bus_flag[route[j]-1][1])
					{
						ctr++;
						path1.put("to_distance",dist[i].dist);
						total_dist+=dist[i].dist;
						break;
					}
				}
				if(ctr==0)
					path1.put("to_distance", -1);
				rs=stmt.executeQuery("select * from bus where id="+route[j]);
				rs.next();
				dist1=parse2(add1,add2,2);
				time=parse2(add1,add2,1);
				total_dist+=dist1;
				total_time+=time;
				path1.put("time", time);
				path1.put("distance", dist1);
				path1.put("bus", rs.getString("bus_no"));
				path1.put("type", "bus");
				path1.put("no_of_stops", bus_flag[route[j]-1][2]);
				total_no_of_stops+=bus_flag[route[j]-1][2];
				price=calc_price(dist1);
				total_price+=price;
				arr2.put(0,path1);
				//System.out.println("Then, From:"+bus_flag[route[j]-1][0]+"\t To:"+stp[bus_flag[route[j]-1][0]-1].flag+"\t bus_id:"+stp[bus_flag[route[j]-1][0]-1].bus_id+"\t No of stops:"+stp[bus_flag[route[j]-1][0]-1].stop_no+"\n\n");
				path1=new JSONObject();
				path1.put("path_no", start+1);
				rs=stmt.executeQuery("select * from stops where id="+bus_flag[route[j]-1][0]);
				rs.next();
				path1.put("from", rs.getString("name"));
				path1.put("from_lat", rs.getDouble("latitude"));
				path1.put("from_long", rs.getDouble("longitude"));
				add1=rs.getDouble("latitude")+","+rs.getDouble("longitude");
				ctr=0;
				for(i=0;i<count;i++)
				{
					if(dist[i].id==bus_flag[route[j]-1][0])
					{
						ctr++;
						path1.put("from_distance",dist[i].dist);
						total_dist+=dist[i].dist;
						break;
					}
				}
				if(ctr==0)
					path1.put("from_distance", -1);
				rs=stmt.executeQuery("select * from stops where id="+stp[bus_flag[route[j]-1][0]-1].flag);
				rs.next();
				path1.put("to", rs.getString("name"));
				path1.put("to_lat", rs.getDouble("latitude"));
				path1.put("to_long", rs.getDouble("longitude"));
				add2=rs.getDouble("latitude")+","+rs.getDouble("longitude");
				ctr=0;
				for(i=0;i<count;i++)
				{
					if(dist[i].id==stp[bus_flag[route[j]-1][0]-1].flag)
					{
						ctr++;
						path1.put("to_distance",dist[i].dist);
						total_dist+=dist[i].dist;
						break;
					}
				}
				if(ctr==0)
					path1.put("to_distance", -1);
				rs=stmt.executeQuery("select * from bus where id="+stp[bus_flag[route[j]-1][0]-1].bus_id);
				rs.next();
				dist1=parse2(add1,add2,2);
				time=parse2(add1,add2,1);
				total_dist+=dist1;
				total_time+=time;
				path1.put("time", time);
				path1.put("distance", dist1);
				path1.put("bus", rs.getString("bus_no"));
				path1.put("type", "bus");
				path1.put("no_of_stops", stp[bus_flag[route[j]-1][0]-1].stop_no);
				total_no_of_stops+=stp[bus_flag[route[j]-1][0]-1].stop_no;
				price=calc_price(dist1);
				total_price+=price;
				arr2.put(1,path1);
			}	
			obj2.put("path", arr2);
			obj2.put("total_time", total_time);
			obj2.put("total_dist", total_dist);
			obj2.put("price", total_price);
			obj2.put("total_no_of_stops", total_no_of_stops);
			if(total_price<min_price)
			{
				min_price=total_price;
				flag1=j;
			}
			arr1.put(j,obj2);
		}
		Type4 x=new Type4();
		x.arr=arr1;
		x.price=min_price;
		x.route_no=flag1+1;
		return x;
	}
	
	public int calc_price(double dist) {
		int price=5*((int)(Math.ceil(dist/5)+1));
		if(price>15)
			price=15;
		
		return price;
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
	    //System.out.println(jsonObj);
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
	
	public double deg2rad(Double deg) {
		return deg * (Math.PI/180);
	}
	
	public Type1[] calc(double lat1,double lon1,double v) throws SQLException
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
				//System.out.println("test_calc1");
				if(dLat<0)
					dLat=dLat*-1;
         		dLon = deg2rad(lon1-rs1.getDouble("longitude"));
         		//System.out.println("test_calc2");
	            if(dLon<0)
	                dLon=dLon*-1;
	            double a = Math.sin(dLat/2) * Math.sin(dLat/2) +Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(rs1.getDouble("latitude"))) *Math.sin(dLon/2) * Math.sin(dLon/2); 
	            double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a)); 
	            d = R * c; // Distance in km
	            //System.out.println("test_calc3");
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
	            	//System.out.println("test_calc");
	            	arr[i]=new Type1();
	            	arr[i].id=rs1.getInt("id");
	            	arr[i].dist=d;
	            	//System.out.println(d);
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
}
