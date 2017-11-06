package com.londonappbrewery.climapm;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONObject;

import java.nio.ByteBuffer;

import cz.msebera.android.httpclient.Header;


public class WeatherController extends AppCompatActivity {
    private static final String TAG = "WeatherController";

    BluetoothAdapter mBluetoothAdapter;

    // Constants:
    final String WEATHER_URL = "http://api.openweathermap.org/data/2.5/weather";
    // App ID to use OpenWeather data
    final String APP_ID = "e72ca729af228beabd5d20e3b7749713";
    // Time between location updates (5000 milliseconds or 5 seconds)
    final long MIN_TIME = 5000;
    // Distance between location updates (1000m or 1km)
    final float MIN_DISTANCE = 1000;

    // TODO: Set LOCATION_PROVIDER here:
    String LOCATION_PROVIDER = LocationManager.NETWORK_PROVIDER;


    // Member Variables:
    TextView mCityLabel;
    ImageView mWeatherImage;
    TextView mTemperatureLabel;
    TextView mDescription;
    TextView mHumidity;
    TextView mWindSpeed;
    TextView mSunRise;
    TextView mSunSet;

    // TODO: Declare a LocationManager and a LocationListener here:
    LocationManager mLocationManager;
    LocationListener mLocationListener;

    //TODO: Declare Light Variables here
    private SeekBar seekBar;
    private int progressValue = 0;
    private int color = 0;

    private static final int REQUEST_CODE = 123;

    private static final int REQUEST_CONNECT_DEVICE_SECURE = 1;
    public EditText et3;
    Button b2, b30, b32;
    String mConnectedDeviceName;
    Context context;
    TextView tv1;
    String readMessage = "";
    final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {

            switch (msg.what) {
                case Constants.MESSAGE_STATE_CHANGE:
                    switch (msg.arg1) {
                        case BluetoothChatService.STATE_CONNECTED:
                            Toast.makeText(WeatherController.this, "Connected to " + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
                            break;

                    }
                    break;
                case Constants.MESSAGE_WRITE:
                    byte[] writeBuf = (byte[]) msg.obj;
                    // construct a string from the buffer
                    String writeMessage = new String(writeBuf);

                    break;
                case Constants.MESSAGE_READ:
                    byte[] readBuf = (byte[]) msg.obj;
                    // construct a string from the valid bytes in the buffer
                    readMessage = readMessage + new String(readBuf, 0, msg.arg1);
                    //Toast.makeText(context, readMessage, Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "handleMessage: Message Read from Bluetooth: " + readMessage);

                    break;
                case Constants.MESSAGE_DEVICE_NAME:
                    // save the connected device's name
                    mConnectedDeviceName = msg.getData().getString(Constants.DEVICE_NAME);

                    Toast.makeText(getApplicationContext(), mConnectedDeviceName, Toast.LENGTH_SHORT).show();

                    break;
                case Constants.MESSAGE_TOAST:

                    Toast.makeText(getApplicationContext(), msg.getData().getString(Constants.TOAST), Toast.LENGTH_SHORT).show();

                    break;
            }
        }
    };
    BluetoothChatService mChatService = new BluetoothChatService(this, mHandler);
    final Handler handler = new Handler() {


        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            String message = (String) msg.obj;
            mChatService.write(message.getBytes());

        }
    };


    /*
    this part of code is for identifying Bluetooth devices and connecting to the device which we want to connect.
    use this code as connect button

     */
    View.OnClickListener m = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            Log.d(TAG, "onClick: Checking Bluetooth adapter: " + mBluetoothAdapter);

            if (!mBluetoothAdapter.isEnabled()) {
                ensureDiscoverable();
            }
            Intent serverIntent = new Intent(WeatherController.this, DeviceListActivity.class);
            startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE_SECURE);

        }

    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.weather_controller_layout);
        EventBus.getDefault().register(this);

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if(mBluetoothAdapter==null){
            Log.e(TAG, "onCreate: Bluetooth no supported");
        }
        else {
            Log.d(TAG, "onCreate: Bluetooth adapter has been taken " + mBluetoothAdapter);
        }

        // Linking the elements in the layout to Java code
        mCityLabel = (TextView) findViewById(R.id.locationTV);
        mWeatherImage = (ImageView) findViewById(R.id.weatherSymbolIV);
        mTemperatureLabel = (TextView) findViewById(R.id.tempTV);
        mDescription = (TextView) findViewById(R.id.descriptionTV);
        mHumidity = (TextView) findViewById(R.id.humidity);
        mWindSpeed = (TextView) findViewById(R.id.Wind);
        mSunRise = (TextView) findViewById(R.id.sunrise);
        mSunSet = (TextView) findViewById(R.id.sunset);

        mWeatherImage.setOnClickListener(m);

        //TODO: Add SeekBar and Light Code here
        final ColorPaletteView colorPaletteView = new ColorPaletteView(this, Color.MAGENTA);
        LinearLayout linearLayout = (LinearLayout) findViewById(R.id.light_play_linearLayout);
        linearLayout.addView(colorPaletteView);
        seekBar = (SeekBar) findViewById(R.id.light_play_seekBar);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                progressValue = 8 - progress;
                sendRequest(color, progressValue);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        getWeatherForCurrentLocation();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    private void sendRequest(int color, int progressValue) {
        byte[] data = ByteBuffer.allocate(4).putInt(color).array();
        data[0] = 0x19;
        int div = getTwoPow(progressValue);
        data[1] = divSome(data[1], div);
        data[2] = divSome(data[2], div);
        data[3] = divSome(data[3], div);
        Log.d(TAG, "sendRequest: Color Value is: " + data[0] + " " + data[1] + " " + data[2] + " " + data[3]);
        mChatService.write(data);
    }

    private byte divSome(byte a, int div) {
        int val = a & 0xFF;
        val = val / div;
        return (byte) val;
    }


    private int getTwoPow(int a) {
        switch (a) {
            case 0:
                return 1;
            case 1:
                return 2;
            case 2:
                return 4;
            case 3:
                return 8;
            case 4:
                return 16;
            case 5:
                return 32;
            case 6:
                return 64;
            case 7:
                return 128;
            case 8:
                return 256;
            default:
                return 1;
        }
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void onColorEvent(Integer color) {
        this.color = color;
        sendRequest(color, progressValue);
    }

    // TODO: Add onResume() here:
    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: called and Getting data for whether location!");


        if (ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_CODE);

            return;
        }

    }


    // TODO: Add getWeatherForCurrentLocation() here:
    private void getWeatherForCurrentLocation() {
        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        mLocationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                Log.d(TAG, "onLocationChanged: ");

                String longitude = String.valueOf(location.getLongitude());
                String latitude = String.valueOf(location.getLatitude());

                Log.d(TAG, "onLocationChanged: Long: " + longitude);
                Log.d(TAG, "onLocationChanged: Lati: " + latitude);

                RequestParams params = new RequestParams();
                params.put("lat", latitude);
                params.put("lon", longitude);
                params.put("appid", APP_ID);
                letsDoSomeNetworking(params);
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
                Log.d(TAG, "onStatusChanged: ");

            }

            @Override
            public void onProviderEnabled(String provider) {
                Log.d(TAG, "onProviderEnabled: ");

            }

            @Override
            public void onProviderDisabled(String provider) {
                Log.d(TAG, "onProviderDisabled: ");

            }
        };

        if (ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_CODE);

            return;
        }
        mLocationManager.requestLocationUpdates(LOCATION_PROVIDER, MIN_TIME, MIN_DISTANCE, mLocationListener);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "onRequestPermissionsResult: Permission Granted!");
                getWeatherForCurrentLocation();
            } else {
                Log.d(TAG, "onRequestPermissionsResult: Permission Denied");
            }
        }

    }


    // TODO: Add letsDoSomeNetworking(RequestParams params) here:
    private void letsDoSomeNetworking(RequestParams params) {

        Log.d(TAG, "letsDoSomeNetworking: Called");
        AsyncHttpClient client = new AsyncHttpClient();

        client.get(WEATHER_URL, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                Log.d(TAG, "onSuccess: JSON: " + response.toString());
                WeatherDataModel weatherData = WeatherDataModel.fromJson(response);
                updateUI(weatherData);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                Log.e(TAG, "onFailure: " + statusCode + " Failed: " + throwable.toString());
                Toast.makeText(WeatherController.this, "Request Failed", Toast.LENGTH_SHORT).show();
            }
        });
    }


    // TODO: Add updateUI() here:
    private void updateUI(WeatherDataModel weather) {
        mTemperatureLabel.setText(weather.getTemperature());
        mCityLabel.setText(weather.getCity());
        int resourceID = getResources().getIdentifier(weather.getIconName(), "drawable", getPackageName());
        mWeatherImage.setImageResource(resourceID);
        mDescription.setText(weather.getDescription());
        mHumidity.setText(weather.getHumidity() + "%");
        mWindSpeed.setText(weather.getWindSpeed() + " Km/Hr");
        mSunRise.setText(weather.getSunRise());
        mSunSet.setText(weather.getSunSet());
    }


    // TODO: Add onPause() here:


    @Override
    protected void onPause() {
        super.onPause();
        if (mLocationManager != null) {
            mLocationManager.removeUpdates(mLocationListener);
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CONNECT_DEVICE_SECURE:
                // When DeviceListActivity returns with a device to connect
                if (resultCode == Activity.RESULT_OK) {
                    connectDevice(data, true);
                }
                break;

        }
    }


    private void ensureDiscoverable() {
        if (mBluetoothAdapter.getScanMode() !=
                BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
            Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
            startActivity(discoverableIntent);
        }
    }

    private void connectDevice(Intent data, boolean secure) {
        // Get the device MAC address
        String address = data.getExtras()
                .getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
        // Get the BluetoothDevice object
        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        // Attempt to connect to the device
        mChatService.connect(device, secure);
    }

}
