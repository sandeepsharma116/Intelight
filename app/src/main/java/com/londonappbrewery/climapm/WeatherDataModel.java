package com.londonappbrewery.climapm;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;

import static android.content.ContentValues.TAG;

public class WeatherDataModel {

    // TODO: Declare the member variables here
    private String mTemperature;
    private String mCity;
    private String mIconName;
    private String mDescription;
    private int mCondition;
    private String mMinTemp;
    private String mMaxTemp;
    private String mHumidity;
    private String mWindSpeed;
    private String mSunSet;
    private String mSunRise;


    // TODO: Create a WeatherDataModel from a JSON:
    public static WeatherDataModel fromJson(JSONObject jsonObject) {

        try {
            WeatherDataModel weatherData = new WeatherDataModel();
            weatherData.mCity = jsonObject.getString("name");
            weatherData.mCondition = jsonObject.getJSONArray("weather").getJSONObject(0).getInt("id");
            weatherData.mDescription = jsonObject.getJSONArray("weather").getJSONObject(0).getString("description");
            weatherData.mIconName = updateWeatherIcon(weatherData.mCondition);


            double tempResult = jsonObject.getJSONObject("main").getDouble("temp") - 273.15;
            int roundedValue = (int) Math.rint(tempResult);
            weatherData.mTemperature = Integer.toString(roundedValue);

            int humiResult = jsonObject.getJSONObject("main").getInt("humidity");
            weatherData.mHumidity = Integer.toString(humiResult);

            tempResult = jsonObject.getJSONObject("main").getDouble("temp_min") - 273.15;
            roundedValue = (int) Math.rint(tempResult);
            weatherData.mMinTemp = Integer.toString(roundedValue);

            tempResult = jsonObject.getJSONObject("main").getDouble("temp_max") - 273.15;
            roundedValue = (int) Math.rint(tempResult);
            weatherData.mMaxTemp = Integer.toString(roundedValue);

            double windSpeed = jsonObject.getJSONObject("wind").getDouble("speed")*1.6;
            roundedValue = (int) Math.rint(windSpeed);
            weatherData.mWindSpeed = Integer.toString(roundedValue);

            int sunRise = jsonObject.getJSONObject("sys").getInt("sunrise");
            String temp = new SimpleDateFormat("h:mm a").format(new Date(sunRise * 1000L+19800));
            weatherData.mSunRise = temp;

            int sunSet = jsonObject.getJSONObject("sys").getInt("sunset");
            temp = new SimpleDateFormat("h:mm a").format(new Date(sunSet * 1000L+19800));
            weatherData.mSunSet = temp;

            Log.d(TAG, "fromJson: Sunset: " + temp);

            return weatherData;
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }


    // TODO: Uncomment to this to get the weather image name from the condition:
    private static String updateWeatherIcon(int condition) {

        if (condition >= 0 && condition < 300) {
            return "tstorm1";
        } else if (condition >= 300 && condition < 500) {
            return "light_rain";
        } else if (condition >= 500 && condition < 600) {
            return "shower3";
        } else if (condition >= 600 && condition <= 700) {
            return "snow4";
        } else if (condition >= 701 && condition <= 771) {
            return "fog";
        } else if (condition >= 772 && condition < 800) {
            return "tstorm3";
        } else if (condition == 800) {
            return "sunny";
        } else if (condition >= 801 && condition <= 804) {
            return "cloudy2";
        } else if (condition >= 900 && condition <= 902) {
            return "tstorm3";
        } else if (condition == 903) {
            return "snow5";
        } else if (condition == 904) {
            return "sunny";
        } else if (condition >= 905 && condition <= 1000) {
            return "tstorm3";
        }
        return "dunno";
    }

    // TODO: Create getter methods for temperature, city, and icon name:


    public String getTemperature() {
        return mTemperature + "°";
    }

    public String getCity() {
        return mCity;
    }

    public String getIconName() {
        return mIconName;
    }

    public String getDescription() {
        return mDescription;
    }


    public String getMinTemp() {
        return mMinTemp;
    }

    public String getMaxTemp() {
        return mMaxTemp;
    }

    public String getHumidity() {
        return mHumidity;
    }

    public String getWindSpeed() {
        return mWindSpeed;
    }

    public String getSunSet() {
        return mSunSet;
    }

    public String getSunRise() {
        return mSunRise;
    }
}
