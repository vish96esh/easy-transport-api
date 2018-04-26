package com.minor.proj;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import org.json.JSONException;

import org.json.JSONArray;
import org.json.JSONObject;

public class Route2 {
	
	int adj[][][]={
			{{0,0},{4733,1},{0,0},{0,0},{0,0},{0,0},{0,0},{4701,2}},
			{{4733,13},{0,0},{4750,8},{0,0},{0,0},{0,0},{0,0},{0,0}},
			{{0,0},{4750,26},{0,0},{4799,22},{0,0},{4706,20},{4766,23},{0,0}},
			{{0,0},{0,0},{4799,16},{0,0},{4832,11},{4760,14},{0,0},{4754,23}},
			{{0,0},{0,0},{0,0},{4832,8},{0,0},{0,0},{0,0},{0,0}},
			{{0,0},{0,0},{4706,16},{4760,18},{0,0},{0,0},{0,0},{0,0}},
			{{0,0},{0,0},{4766,6},{0,0},{0,0},{0,0},{0,0},{0,0}},
			{{4701,3},{0,0},{0,0},{4754,1},{0,0},{0,0},{0,0},{0,0}}
	};
	
	Type3 route_array[];
	int top;
	
	int R=6371;
	
	public JSONObject getMetroRoute(double lat1, double lon1, double lat2, double lon2) throws SQLException, ClassNotFoundException, JSONException
	{
		Type1 arr[];
		arr=calc(lat1,lon1,-1);//source
		int stop_id1=arr[0].id;
		double dist1=arr[0].dist;
		arr=calc(lat2, lon2,-1);//destination
		int stop_id2=arr[0].id;
		double dist2=arr[0].dist;
		JSONObject obj=new JSONObject();
		Type4 x=getData(stop_id1,dist1,stop_id2,dist2,1);
		
		obj.put("routes", x.arr);
		obj.put("results", x.arr.length());
		JSONObject obj1=new JSONObject();
		obj1.put("price", x.price);
		obj1.put("route_no", x.route_no);
		obj.put("min_price", obj1);
		return obj;
	}
	
	public Type4 getData(int stop_id1, double dist1, int stop_id2, double dist2, int start) throws SQLException, ClassNotFoundException, JSONException {
		Type4 x=new Type4();
		JSONArray arr1=new JSONArray();
		Class.forName("com.mysql.jdbc.Driver");
		Connection con=DriverManager.getConnection("jdbc:mysql://localhost:3306/minor","root","vishesh");
		Statement stmt1=con.createStatement();
		Statement stmt2=con.createStatement();
		String query="select bus_id,stop_no from route where stop_id="+stop_id1;
		ResultSet rs=stmt1.executeQuery(query);
		query="select bus_id,stop_no from route where stop_id="+stop_id2;
		ResultSet rs1=stmt2.executeQuery(query);
		int i,id1=0,id2=0,cnt=1;
		
		int min_no_of_stops=9999, flag=0, flag_change=0;
		while(rs.next())
		{
			switch(rs.getInt("bus_id"))
			{
				case 624:id1=0;
					break;
				case 627:id1=1;
					break;
				case 629:id1=2;
					break;
				case 630:id1=3;
					break;
				case 631:id1=4;
					break;
				case 632:id1=5;
					break;
				case 633:id1=6;
					break;
				case 634:id1=7;
					break;
			}
			while(rs1.next())
			{
				switch(rs1.getInt("bus_id"))
				{
					case 624:id2=0;
						break;
					case 627:id2=1;
						break;
					case 629:id2=2;
						break;
					case 630:id2=3;
						break;
					case 631:id2=4;
						break;
					case 632:id2=5;
						break;
					case 633:id2=6;
						break;
					case 634:id2=7;
						break;
				}
				Type3 route=new Type3();
				route_array=new Type3[10];
				top=-1;
				traverse(id1,rs.getInt("stop_no"),stop_id1,id2,rs1.getInt("stop_no"),stop_id2,route,0,1);
				int total_no_of_stops, total_time;
				int j,to,no,bus_id=0;
				for(i=0;i<=top;i++)
				{
					total_no_of_stops=0;
					JSONObject obj1=new JSONObject();
					JSONArray arr2=new JSONArray();
					obj1.put("route_no", cnt++);
					for(j=0;j<=route_array[i].top;j++)
					{
						if(j==route_array[i].top)
						{
							to=stop_id2;
							no=route_array[i].last_stop;
							
						}
						else
						{
							to=route_array[i].from[j+1];
							no=route_array[i].no_of_stops[j+1];
						}
						JSONObject obj2=new JSONObject();
						//System.out.println("From:"+route_array[i].from[j]);
						//System.out.println("To:"+to);
						//System.out.println("Bus:"+route_array[i].bus[j]);
						//System.out.println("No. of stops:"+no);
						//System.out.println();
						rs=stmt1.executeQuery("select * from stops where id="+route_array[i].from[j]);
						rs.next();
						obj2.put("from", rs.getString("name"));
						obj2.put("from_lat", rs.getDouble("latitude"));
						obj2.put("from_long", rs.getDouble("longitude"));
						if(route_array[i].from[j]==stop_id1)
							obj2.put("from_distance", dist1);
						else
							obj2.put("from_distance", -1);
						rs=stmt1.executeQuery("select * from stops where id="+to);
						rs.next();
						obj2.put("to", rs.getString("name"));
						obj2.put("to_lat", rs.getDouble("latitude"));
						obj2.put("to_long", rs.getDouble("longitude"));
						if(to==stop_id2)
							obj2.put("to_distance", dist2);
						else
							obj2.put("to_distance", -1);
						switch(route_array[i].bus[j])
						{
							case 0:bus_id=624;
								break;
							case 1:bus_id=627;
								break;
							case 2:bus_id=629;
								break;
							case 3:bus_id=630;
								break;
							case 4:bus_id=631;
								break;
							case 5:bus_id=632;
								break;
							case 6:bus_id=633;
								break;
							case 7:bus_id=634;
								break;
						}
						rs=stmt1.executeQuery("select * from bus where id="+bus_id);
						rs.next();
						obj2.put("distance", 0);
						obj2.put("time", 0);
						obj2.put("bus", rs.getString("bus_no"));
						obj2.put("no_of_stops", no);
						total_no_of_stops+=no;
						
						obj2.put("type", "metro");
						obj2.put("path_no", j+start);
						arr2.put(j, obj2);
					}
					obj1.put("path", arr2);
					if(total_no_of_stops<min_no_of_stops)
					{
						min_no_of_stops=total_no_of_stops;
						flag_change=route_array[i].top;
						flag=cnt-1;
					}
					total_time=calc_time(total_no_of_stops,route_array[i].top);
					obj1.put("total_no_of_stops", total_no_of_stops);
					obj1.put("total_time", total_time);
					obj1.put("total_dist", 0);
					obj1.put("price", 0);
					//System.out.println();
					//System.out.println("======================");
					//System.out.println();
					arr1.put(i,obj1);
				}

			}
		}
		int price=calc_price(min_no_of_stops,flag_change);
		x.arr=arr1;
		x.price=price;
		x.route_no=flag;
		
		return x;
	}
	
	public int calc_price(int no, int change)
	{
		int x=Math.floorDiv(no, 2);
		int price=x*2 + change +8;
		return price;
	}
	
	public int calc_time(int no, int change)
	{
		int time=(no*2)+change;
		return time;
	}
	
	public void traverse(int bus_id1, int stop_no1, int stop_id1, int bus_id2, int stop_no2, int stop_id2, Type3 route, int n, int no)
	{
		if(no>4)
			return;
		if(route.flag[bus_id1]==1)
			return;
		route.insert(stop_id1,bus_id1,n);
		route.flag[bus_id1]=1;
		if(bus_id1==bus_id2)
		{
			route.last_stop=Math.abs(stop_no1-stop_no2);
			route_array[++top]=new Type3(route);
			route.top--;
			route.flag[bus_id1]=0;
			return;
		}
		int i;
		for(i=0;i<8;i++)
		{
			if(adj[bus_id1][i][0]==0)
				continue;
			traverse(i,adj[i][bus_id1][1],adj[bus_id1][i][0],bus_id2,stop_no2,stop_id2,route,Math.abs(stop_no1-adj[bus_id1][i][1]),no+1);
			
		}
		route.top--;
		route.flag[bus_id1]=0;
	}
	
	public double  deg2rad(Double deg) {
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
		String str=(v==-1)?" where type='m'":"where type='b'";
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
}
