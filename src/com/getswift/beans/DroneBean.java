package com.getswift.beans;

import java.util.ArrayList;

public class DroneBean {
	
	private int droneId;
	private Double latitude;
	private Double longitude;
	ArrayList<PackageBean> packageBean = new ArrayList<PackageBean>();
	
	public int getDroneId() {
		return droneId;
	}
	public void setDroneId(int droneId) {
		this.droneId = droneId;
	}
	public Double getLatitude() {
		return latitude;
	}
	public void setLatitude(Double latitude) {
		this.latitude = latitude;
	}
	public Double getLongitude() {
		return longitude;
	}
	public void setLongitude(Double longitude) {
		this.longitude = longitude;
	}
	public ArrayList<PackageBean> getPackageBean() {
		return packageBean;
	}
	public void setPackageBean(ArrayList<PackageBean> packageBean) {
		this.packageBean = packageBean;
	}
}
