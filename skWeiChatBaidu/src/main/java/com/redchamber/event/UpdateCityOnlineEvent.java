package com.redchamber.event;

public class UpdateCityOnlineEvent {

    public String cityName;
    public String onlineFirst;

    public UpdateCityOnlineEvent(String cityName, String onlineFirst) {
        this.cityName = cityName;
        this.onlineFirst = onlineFirst;
    }
}
