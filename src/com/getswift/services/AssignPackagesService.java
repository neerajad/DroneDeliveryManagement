package com.getswift.services;

import java.util.ArrayList;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.json.JSONArray;
import org.json.JSONObject;

import com.getswift.beans.DroneBean;
import com.getswift.beans.PackageBean;
import com.getswift.delegate.AssignPackagesDelegate;

/**
 * This is the service class for RESTFULws call from the JSP.
 * @author neera
 */

@Path("/assignPackage")
public class AssignPackagesService {

	/**
	 * This method makes the delegate call where the actual logic of assigning a drone to a package resides.
	 * @param data
	 * @return
	 */
	@POST
	@Path("/assignPackagesToDrones")
	@Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
	public Response assignPackagesToDrones(String data) {
		
		ArrayList<DroneBean> droneBeanArr = new ArrayList<DroneBean>();
		ArrayList<PackageBean> packageeBeanArr = new ArrayList<PackageBean>();
		JSONObject finalJSON = new JSONObject();
		AssignPackagesDelegate assignPackagesDelegate = new AssignPackagesDelegate();
		
		try {
			// Fetch Drones and Packages array from the data
			JSONObject jsonObject = new JSONObject(data);
			JSONArray droneArr = new JSONArray((String) jsonObject.get("drones"));
			JSONArray packagesArr = new JSONArray((String) jsonObject.get("packages"));
			
			// Call delegate to set values from JSON to drone beans
			droneBeanArr = assignPackagesDelegate.getDroneDetailsArr(droneArr);
			// Call delegate to set values from JSON to package beans
			packageeBeanArr = assignPackagesDelegate.getPackageDetailsArr(packagesArr);
			
			// Call delegate to assign a drone to each package
			finalJSON = assignPackagesDelegate.assignNewPackages(droneBeanArr, packageeBeanArr);
		} catch (Exception e) {
			System.out.println("Exception is " + e.getMessage());
		}
		 return Response.status(200).entity(finalJSON.toString()).build();
	}
}


