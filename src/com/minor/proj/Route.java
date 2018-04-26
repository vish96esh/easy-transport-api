package com.minor.proj;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class Route {
	public static void main(String[] args) throws ClassNotFoundException, SQLException
	{
		int arr[]=new int[10];
		arr=calc(28.652781,77.192144,0.2);//karol bagh
		int i,id,id1;
		Class.forName("com.mysql.jdbc.Driver");
		Connection con=DriverManager.getConnection("jdbc:mysql://localhost:3306/minor","root","vishesh");
		Statement stmt=con.createStatement();
		Statement stmt1=con.createStatement();
		Statement stmt2=con.createStatement();
		ResultSet rs,rs1;
		String query="drop table if exists temp";
		stmt.executeUpdate(query);
		query="create table temp (stop_id int, bus_id int, flag int)";
		stmt.executeUpdate(query);
		for(i=0;i<arr.length;i++)
		{
			query="select bus_id from route where stop_id="+arr[i];
			rs=stmt.executeQuery(query);
			while(rs.next())
			{
				id=rs.getInt("bus_id");
				query="select stop_id from route where bus_id="+id;
				rs1=stmt1.executeQuery(query);
				while(rs1.next())
				{
					id1=rs1.getInt("stop_id");
					if(id1==arr[i])
						query="insert into temp(stop_id,bus_id,flag) values ('"+id1+"','0','0')";
					query="insert into temp(stop_id,bus_id,flag) values ('"+id1+"','"+id+"','"+arr[i]+"')";
					stmt2.executeUpdate(query);
				}
			}
		}
		arr=calc(28.6502,77.3027,0.2);//anand vihar
		ResultSet rs2;
		for(i=0;i<arr.length;i++)
		{
			query="select bus_id from route where stop_id="+arr[i];
			rs=stmt.executeQuery(query);
			while(rs.next())
			{
				id=rs.getInt("bus_id");
				query="select stop_id from route where bus_id="+id;
				rs1=stmt1.executeQuery(query);
				while(rs1.next())
				{
					id1=rs1.getInt("stop_id");
					query="select count(*) as n from temp where stop_id="+id1;
					rs2=stmt2.executeQuery(query);
					rs2.next();
					int len=rs2.getInt("n");
					if(len==0)
						continue;
					query="select * from temp where stop_id="+id1;
					rs2=stmt2.executeQuery(query);
					while(rs2.next())
					{
						int stop_id=rs2.getInt("stop_id");
						int bus_id=rs2.getInt("bus_id");
						int flag=rs2.getInt("flag");
						if(bus_id==0)
							System.out.println("Source:\nStop_id="+arr[i]+"\tBus_id="+id+"\nDest:\nStop_id="+stop_id);
						else
							System.out.println("Source:\nStop_id="+arr[i]+"\tBus_id="+id+"\tTo Stop_id="+stop_id+"\nDest:\nFrom Stop_id="+stop_id+"\tBus_id="+bus_id+"\tStop_id="+flag);
					}
					
				}
						
			}
		}
	}
	
	static int R=6371;
	public static  double  deg2rad(Double deg) {
		return deg * (Math.PI/180);}
   
  public static int[] calc(double lat1,double lon1,double v) throws SQLException
  {
	  Connection con;
	  Statement stmt1;
	  ResultSet rs1;
	  int arr[]=new int[50];
	  double dLon,dLat,d;
	  int i=0;
	  try
	  {  
		  Class.forName("com.mysql.jdbc.Driver");  
		  con=DriverManager.getConnection("jdbc:mysql://localhost:3306/minor","root","vishesh");
		  stmt1=con.createStatement();
		  String sql1="select * from stops";	                    
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
	        	if(d<=v)
	        	{
	        		arr[i++]=rs1.getInt("id");
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
