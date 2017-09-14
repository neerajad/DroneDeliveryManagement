package com.getswift.delegate;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import org.json.JSONArray;
import org.json.JSONObject;

import com.getswift.beans.DroneBean;
import com.getswift.beans.PackageBean;

/**
 * This class implements the actual logic to allocate drones to packages
 * @author neera
 *
 */
public class AssignPackagesDelegate {

	private static final int EARTH_RADIUS = 6371; // Approximate Earth radius in KM
	private static final int DRONE_SPEED = 50; // Speed at which drone flies is given as 50km/hr
	private static final double DEPO_LATITUDE = -37.8124895; // Latitude of 303 Collins Street, Melbourne, VIC 3000
	private static final double DEPO_LONGITUDE = 144.9564305; // Longitude of 303 Collins Street, Melbourne, VIC 3000
	
	
	/**
	 * Iterate over the JSON array and set the bean object
	 * @param droneArr
	 * @return
	 */
	public ArrayList<DroneBean> getDroneDetailsArr(JSONArray droneArr) {
		DroneBean droneBean;
		ArrayList<DroneBean> droneBeanArr = new ArrayList<DroneBean>();
		
		for (int i = 0; i < droneArr.length(); i++) {
			droneBean = new DroneBean();
			droneBean.setDroneId(Integer.parseInt(((JSONObject)droneArr.get(i)).get("droneId").toString()));
			JSONObject locationObject = (JSONObject)((JSONObject)droneArr.get(i)).get("location");
			droneBean.setLatitude(Double.parseDouble(locationObject.get("latitude").toString()));
			droneBean.setLongitude(Double.parseDouble(locationObject.get("longitude").toString()));
			
			JSONArray packagesArr = (JSONArray)((JSONObject)droneArr.get(i)).get("packages");
			// Call method to iterate over package array and set package bean
			ArrayList<PackageBean> packageBeanArr = getPackageDetailsArr(packagesArr);
			
			droneBean.setPackageBean(packageBeanArr);
			droneBeanArr.add(droneBean);
		}
		return droneBeanArr;
	}
	
	/**
	 * This method iterates over the package JSON and sets the package beans and populate the array
	 * @param packagesArr
	 * @return
	 */
	public ArrayList<PackageBean> getPackageDetailsArr(JSONArray packagesArr) {
		ArrayList<PackageBean> packageBeanArr = new ArrayList<PackageBean>();
		
			for (int j = 0; j < packagesArr.length(); j++) {
				PackageBean packageBean = new PackageBean();
				JSONObject destinationObject = (JSONObject)(((JSONObject)packagesArr.get(j)).get("destination"));
				packageBean.setLatitude(Double.parseDouble(destinationObject.get("latitude").toString()));
				packageBean.setLongitude(Double.parseDouble(destinationObject.get("longitude").toString()));
				
				packageBean.setPackageId(Integer.parseInt(((JSONObject)packagesArr.get(j)).get("packageId").toString()));
				packageBean.setDeadline(Integer.parseInt(((JSONObject)packagesArr.get(j)).get("deadline").toString()));
				
				packageBeanArr.add(packageBean);
			}
		return packageBeanArr;
	}
	
	/**
	 * This method iterates over the drones and packages and assigns the appropriate drone to a package
	 * @param droneBeanArr
	 * @param packageeBeanArr
	 * @return
	 */
	public JSONObject assignNewPackages(ArrayList<DroneBean> droneBeanArr, ArrayList<PackageBean> packageeBeanArr) {
		JSONArray assignmentJsonArr = new JSONArray();
		JSONArray unAssignedJsonArr = new JSONArray();
		JSONObject finalJSON = new JSONObject();
		ArrayList<Integer> excludeDroneIds = new ArrayList<Integer>();
		
		TimeZone gmtTimeZone = TimeZone.getTimeZone("GMT");
		Calendar cal = Calendar.getInstance(gmtTimeZone);
		
		for (int i = 0; i< packageeBeanArr.size(); i++) {
			// Get one package details
			PackageBean packageBean = packageeBeanArr.get(i);
			Double newLati = packageBean.getLatitude();
			Double newLong = packageBean.getLongitude();
			Double shortestDelTime = 0.0;
			int selDroneId = 0;
			
			JSONObject assignedJson = new JSONObject();
			
			// Convert UNIX time stamp to java date
			Date packageDeadline = Date.from(Instant.ofEpochSecond(packageBean.getDeadline()));
			
			// calculate distance between current destination and depo
			Double timeToNewDest = calculateDistance(DEPO_LATITUDE, DEPO_LONGITUDE, newLati, newLong);
			
			// Iterate through Drone list and find best match for this package
			for (int  j = 0; j < droneBeanArr.size(); j++) {
				// get first drone
				DroneBean droneBean = droneBeanArr.get(j);
				Double totalDelTime = 0.0;
				
				// Proceed only if this drone is not allocated with a package in the current cycle. Else go to next drone
				if (!excludeDroneIds.contains(droneBean.getDroneId())) {
				
					// If the drone already has a package assigned, find the time left to deliver the current package
					if ((droneBean.getPackageBean().size() > 0)) {
						PackageBean currPackage = droneBean.getPackageBean().get(0);
						
						// calculate the remaining distance to be covered by the drone to deliver the current package
						Double remainingTime = calculateDistance(droneBean.getLatitude(), droneBean.getLongitude(), currPackage.getLatitude(), currPackage.getLongitude());
						
						// calculate distance between current destination and depo
						Double timeToDepo = calculateDistance(currPackage.getLatitude(), currPackage.getLongitude(), -37.8124895, 144.9564305);
						
						//total time for this drone to finish this assignment, come back to depo and deliver the new package 
						totalDelTime = remainingTime + timeToDepo + timeToNewDest;
					} else {
						// If the drone doesn't have a current package, then only time to fly from depo to destination needs to be considered
						totalDelTime = timeToNewDest;
					}
					if (totalDelTime < shortestDelTime || j == 0.0) {
						shortestDelTime = totalDelTime;
						selDroneId = droneBean.getDroneId();
					}
				}
			}
			// Now check if selected drone with minimum delivery time can deliver it within package deadline
			
			// Add time required by drone to current time
			int shorestDelSec = (int) Double.parseDouble((shortestDelTime * 60 * 60) + "");
			cal.add(Calendar.SECOND, shorestDelSec);

			// Check if the time by which this drone can deliver the package is before package deadline
			if (cal.getTime().before(packageDeadline)) {
				// If delivery time is before deadline map this drone to the package
				assignedJson.put("droneId", selDroneId);
				assignedJson.put("packageId", packageBean.getPackageId());
				assignmentJsonArr.put(assignedJson);
				
				// Add this drone to a list of droneIds to be excluded for mapping to other packages since its already allocated
				excludeDroneIds.add(selDroneId);
			} else {
				// If the package could not be allocated to any drone, add it to array of unassigned package Id's
				unAssignedJsonArr.put(packageBean.getPackageId());
			}
		}
		
		// Create a final JSON object to send to UI for display
		finalJSON.put("assignments", assignmentJsonArr);
		finalJSON.put("unassignedPackageIds", unAssignedJsonArr);
		return finalJSON;
	}
	
	/**
	 * This method uses haversine formula to find distance between 2 points, if their latitudes and longitudes are given
	 * @param startLat
	 * @param startLong
	 * @param endLat
	 * @param endLong
	 * @return time in hrs
	 */
	public static double calculateDistance(double startLat, double startLong, double endLat, double endLong) {
		double dLat = Math.toRadians((endLat - startLat));
		double dLong = Math.toRadians((endLong - startLong));

		startLat = Math.toRadians(startLat);
		endLat = Math.toRadians(endLat);

		// a is haversine(distance/radius)
		double a = haversin(dLat) + Math.cos(startLat) * Math.cos(endLat) * haversin(dLong);
		// get inverse haversine to get distance/radius
		double dOverR = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

		double distance = EARTH_RADIUS * dOverR;
		double time = distance/DRONE_SPEED;
		return time;
	}

	public static double haversin(double val) {
		return Math.pow(Math.sin(val / 2), 2);
	}
}
