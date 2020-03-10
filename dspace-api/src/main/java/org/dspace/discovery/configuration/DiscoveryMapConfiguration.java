package org.dspace.discovery.configuration;

public class DiscoveryMapConfiguration extends DiscoveryViewConfiguration {
	
	private String latitude;
	private String longitude;
	private String latitudelongitude;
	
	public String getLatitude() {
		return latitude;
	}
	public void setLatitude(String latitude) {
		this.latitude = latitude;
	}
	public String getLongitude() {
		return longitude;
	}
	public void setLongitude(String longitude) {
		this.longitude = longitude;
	}
	public String getLatitudelongitude() {
		return latitudelongitude;
	}
	public void setLatitudelongitude(String latitudelongitude) {
		this.latitudelongitude = latitudelongitude;
	}

}
