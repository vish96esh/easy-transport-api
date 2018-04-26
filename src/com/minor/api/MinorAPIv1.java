package com.minor.api;

/*import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.sql.ResultSet;
*/
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.json.JSONObject;

import com.minor.proj.Route1;
import com.minor.proj.Route2;
import com.minor.proj.Route3;
import com.minor.proj.Route4;
//import com.minor.proj.Type1;

@Path("/v1")
public class MinorAPIv1 {
	
	@Path("/bus")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getBusData(@QueryParam("slat") String s_lat, @QueryParam("slong") String s_lon, @QueryParam("dlat") String d_lat, @QueryParam("dlong") String d_lon)
	{
		double lat1=Double.parseDouble(s_lat);
		double lon1=Double.parseDouble(s_lon);
		double lat2=Double.parseDouble(d_lat);
		double lon2=Double.parseDouble(d_lon);
		//System.out.println(lat1+" "+lon1+" "+lat2+" "+lon2);
		Route1 r=new Route1();
		Response s;
		try
		{
			JSONObject res=r.getBusRoute(lat1, lon1, lat2, lon2);
			//System.out.println("res="+res);
			s= Response.status(200).entity(res.toString()).build();
		}
		catch(Exception e)
		{
			s= Response.status(500).entity("\"Error\":\""+e.toString()+"\"").build();
		}
		return s;
	}
	
	@Path("/metro")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getMetroData(@QueryParam("slat") String s_lat, @QueryParam("slong") String s_lon, @QueryParam("dlat") String d_lat, @QueryParam("dlong") String d_lon)
	{
		double lat1=Double.parseDouble(s_lat);
		double lon1=Double.parseDouble(s_lon);
		double lat2=Double.parseDouble(d_lat);
		double lon2=Double.parseDouble(d_lon);
		//System.out.println(lat1+" "+lon1+" "+lat2+" "+lon2);
		Route2 r=new Route2();
		Response s;
		try
		{
			JSONObject res=r.getMetroRoute(lat1, lon1, lat2, lon2);
			//System.out.println("res="+res);
			s= Response.status(200).entity(res.toString()).build();
		}
		catch(Exception e)
		{
			s= Response.status(500).entity("\"Error\":\""+e.toString()+"\"").build();
		}
		return s;
	}
	
	@Path("/auto")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getAutoData(@QueryParam("slat") String s_lat, @QueryParam("slong") String s_lon, @QueryParam("dlat") String d_lat, @QueryParam("dlong") String d_lon)
	{
		double lat1=Double.parseDouble(s_lat);
		double lon1=Double.parseDouble(s_lon);
		double lat2=Double.parseDouble(d_lat);
		double lon2=Double.parseDouble(d_lon);
		Route3 r=new Route3();
		Response s;
		try {
			JSONObject res=r.getAutoRoute(lat1, lon1, lat2, lon2, 1);
			s= Response.status(200).entity(res.toString()).build();
		}
		catch(Exception e)
		{
			s= Response.status(500).entity("\"Error\":\""+e.toString()+"\"").build();
		}
		return s;
	}
	
	@Path("/mixed")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getMixedData(@QueryParam("slat") String s_lat, @QueryParam("slong") String s_lon, @QueryParam("dlat") String d_lat, @QueryParam("dlong") String d_lon)
	{
		double lat1=Double.parseDouble(s_lat);
		double lon1=Double.parseDouble(s_lon);
		double lat2=Double.parseDouble(d_lat);
		double lon2=Double.parseDouble(d_lon);
		Route4 r=new Route4();
		Response s;
		try {
			JSONObject res=r.getMixedData(lat1, lon1, lat2, lon2);
			s= Response.status(200).entity(res.toString()).build();
		}
		catch(Exception e)
		{
			s= Response.status(500).entity("\"Error\":\""+e.toString()+"\"").build();
		}
		return s;
	}
}
