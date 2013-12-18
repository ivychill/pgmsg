package com.luyun.msg;

public class Checkin {
	enum OsType {
		ANDROID(0), IOS(1), WP(2), OTHER(9);

		private final int id;

		OsType(int id) {
			this.id = id;
		}

		public int getValue() {
			return id;
		}
	}

	public String getDeviceModel() {
		return deviceModel;
	}

	public void setDeviceModel(String deviceModel) {
		this.deviceModel = deviceModel;
	}

	public OsType getOsType() {
		return osType;
	}

	public void setOsType(OsType osType) {
		this.osType = osType;
	}

	public String getOsVersion() {
		return osVersion;
	}

	public void setOsVersion(String osVersion) {
		this.osVersion = osVersion;
	}

	public int getMajorRelease() {
		return majorRelease;
	}

	public void setMajorRelease(int majorRelease) {
		this.majorRelease = majorRelease;
	}

	public int getMinorRelease() {
		return minorRelease;
	}

	public void setMinorRelease(int minorRelease) {
		this.minorRelease = minorRelease;
	}

	public String getDownloadUrl() {
		return downloadUrl;
	}

	public void setDownloadUrl(String downloadUrl) {
		this.downloadUrl = downloadUrl;
	}

	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}

	String deviceModel; 
	OsType osType;
	String osVersion;
	int majorRelease;
	int minorRelease;
	String downloadUrl;
	String desc;
}
