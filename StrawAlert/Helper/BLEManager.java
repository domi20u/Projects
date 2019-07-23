package de.tum.mw.ftm.praktikum.androidapp_strohhalm.Helper;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.widget.Toast;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.Random;
import java.util.UUID;

import de.tum.mw.ftm.praktikum.androidapp_strohhalm.PopUps.PopUpList;
import de.tum.mw.ftm.praktikum.androidapp_strohhalm.R;
import de.tum.mw.ftm.praktikum.androidapp_strohhalm.db.sqlite.data.DataDrink;
import de.tum.mw.ftm.praktikum.androidapp_strohhalm.db.sqlite.data.DataPatient;

/**
 * Created by bg on 22.01.2017.
 * BLE Scanning
 */

public class BLEManager {

    private Context context;
    private FragmentActivity currentActivity;
    private BluetoothAdapter MyBluetoothAdapter;
    public boolean forWriting;
    public boolean forSingleWriting;
    public boolean forDrinkReset;
    private Snackbar snackbar_scanning;
    private Snackbar snackbar_connected;

    //Parameters for BLE output
    private String string_write_notification;
    private String characteristic_value;
    private String characteristic_string_output;
    private String characteristic_string_output_debug;
    public String connect_next;
    private int count_characteristics_current;
    private int count_characteristics_current_write;
    private int count_characteristics_current_reset;
    private static long SCAN_PERIOD = 5000; //stops scanning after 5 seconds
    private Random random;

    //Parameters for Bluetooth
    private boolean mScanning;
    private Handler MyHandler;
    public BluetoothGatt MyBluetoothGatt;
    private ArrayList<BluetoothDevice> MyListDevices;
    private ArrayList<BluetoothDevice> MyListStrawDevices;
    private ArrayList<BluetoothGattService> MyListServices;
    private ArrayList<BluetoothGattCharacteristic> MyListCharacteristics;
    private ArrayList<String> MyListStrawCharacteristicsStrings;
    private DataPatient dataPatientStraw;
    private DataPatient dataPatientNew;
    private DataDrink dataDrinkStraw;
    private DataDrink dataDrinkNew;

    //additional straw characteristics
    private int batteryLevelStraw;
    public final int pulseCountReset = 0;
    public final int numberDrinkEventsReset = 0;
    public final int drunkVolumeReset = 0;
    public final float pulseMultiplierReset = 10.00f;
    public int pulseCountStraw;
    public int numberDrinkEventsStraw;
    public float pulseMultiplierStraw;

    private NoticeBLEListener MyNoticeBLEListener;
    public interface NoticeBLEListener{
        void onBLEScanAndReadFinished(DataPatient dataPatientStraw, DataDrink dataDrinkStraw, int batteryLevel);
        void onBLEWriteFinished(DataPatient dataPatientStraw);
    }

    public BLEManager(Context context, FragmentActivity currentActivity){
        this.context = context;
        this.currentActivity = currentActivity;
        this.forWriting = false;
        this.forSingleWriting = false;
        this.forDrinkReset = false;
        MyNoticeBLEListener = (NoticeBLEListener) currentActivity;

        MyListDevices = new ArrayList<>();
        MyListStrawDevices = new ArrayList<>();
        MyListServices = new ArrayList<>();
        MyListCharacteristics = new ArrayList<>();
        MyHandler = new Handler();
        dataPatientStraw = new DataPatient();
        dataDrinkStraw = new DataDrink();
        random = new Random();

        MyListStrawCharacteristicsStrings = new ArrayList<>();
        MyListStrawCharacteristicsStrings.add(context.getString(R.string.strawCharacteristic_DrunkVolume_name));
        MyListStrawCharacteristicsStrings.add(context.getString(R.string.strawCharacteristic_PulseCount_name));
        MyListStrawCharacteristicsStrings.add(context.getString(R.string.strawCharacteristic_NumberDrinkEvents_name));
        MyListStrawCharacteristicsStrings.add(context.getString(R.string.strawCharacteristic_PulseMultiplier_name));
        MyListStrawCharacteristicsStrings.add(context.getString(R.string.strawCharacteristic_FirstName_name));
        MyListStrawCharacteristicsStrings.add(context.getString(R.string.strawCharacteristic_LastName_name));
        MyListStrawCharacteristicsStrings.add(context.getString(R.string.strawCharacteristic_UserId_name));
        MyListStrawCharacteristicsStrings.add(context.getString(R.string.strawCharacteristic_Gender_name));
        MyListStrawCharacteristicsStrings.add(context.getString(R.string.strawCharacteristic_BatteryLevel_name));

    }

    public boolean BLEPreparation() {
        //check for BLE capability of the device
        if (!context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(context.getApplicationContext(), R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
            return false;
        }
        else{
            //gets the Bluetooth Adapter
            //Adapter is necessary for all communication over BT and BLE
            GetBTAdapter();

            //check for BLE is enabled on the device
            EnableBT();

            return MyBluetoothAdapter != null &&
                    MyBluetoothAdapter.isEnabled() &&
                    ActivityCompat.checkSelfPermission(context.getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(context.getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;

        }
    }

    private void GetBTAdapter(){
        if (MyBluetoothAdapter == null) {
            final BluetoothManager mBluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
            MyBluetoothAdapter = mBluetoothManager.getAdapter();
        }
    }

    private void EnableBT(){
        if (MyBluetoothAdapter == null || !MyBluetoothAdapter.isEnabled()){
            Intent enableBTIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            //locally-defined integer (which must be greater than 0) that the system passes back to you in your onActivityResult(int, int, android.content.Intent) implementation as the requestCode parameter
            int REQUEST_ENABLE_BT = 48756;
            currentActivity.startActivityForResult(enableBTIntent, REQUEST_ENABLE_BT);
        }
        if (ActivityCompat.checkSelfPermission(context.getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(context.getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            Log.d("DevicePairing", "-- Location permissions granted");
            //Toast.makeText(getApplicationContext(), "Location permissions granted", Toast.LENGTH_SHORT).show();
        }
        else{
            Log.d("DevicePairing", "-- Location permissions request");
            Toast.makeText(context.getApplicationContext(), "Location permissions request", Toast.LENGTH_SHORT).show();
            ActivityCompat.requestPermissions(currentActivity, new String[] {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
        }
    }

    private boolean StartBLEScan(BluetoothAdapter.LeScanCallback mLeScanCallback){
        //Toast.makeText(context.getApplicationContext(), currentActivity.getComponentName().getClassName(), Toast.LENGTH_SHORT).show();
        if (currentActivity.getComponentName().getClassName().contains("MainActivity"))
            snackbar_scanning = Snackbar.make(currentActivity.findViewById(R.id.clayout), R.string.ble_scanning, Snackbar.LENGTH_INDEFINITE);
        else if (currentActivity.getComponentName().getClassName().contains("PatientRegistrationActivity"))
            snackbar_scanning = Snackbar.make(currentActivity.getCurrentFocus(), R.string.ble_scanning, Snackbar.LENGTH_INDEFINITE);
        snackbar_scanning.show();
        MyBluetoothAdapter.startLeScan(mLeScanCallback);
        return true;
    }

    private boolean StopBLEScan(BluetoothAdapter.LeScanCallback mLeScanCallback){
        //Toast.makeText(context.getApplicationContext(), R.string.stopping_ble_scan, Toast.LENGTH_SHORT).show();
        snackbar_scanning.dismiss();
        MyBluetoothAdapter.stopLeScan(mLeScanCallback);
        return false;
    }

    private int EvaluateGattStatus(String function, int status){
        switch (status){
            case (BluetoothGatt.GATT_SUCCESS): Log.d("DevicePairing", "-- " + function + ": GATT_SUCCESS"); return BluetoothGatt.GATT_SUCCESS;
            case (BluetoothGatt.GATT_FAILURE): Log.d("DevicePairing", "-- " + function + ": GATT_FAILURE"); return BluetoothGatt.GATT_FAILURE;
            case (BluetoothGatt.GATT_INSUFFICIENT_AUTHENTICATION): Log.d("DevicePairing", "-- " + function + ": GATT_INSUFFICIENT_AUTHENTICATION"); return BluetoothGatt.GATT_INSUFFICIENT_AUTHENTICATION;
            case (BluetoothGatt.GATT_INSUFFICIENT_ENCRYPTION): Log.d("DevicePairing", "-- " + function + ": GATT_INSUFFICIENT_ENCRYPTION"); return BluetoothGatt.GATT_INSUFFICIENT_ENCRYPTION;
            case (BluetoothGatt.GATT_INVALID_ATTRIBUTE_LENGTH): Log.d("DevicePairing", "-- " + function + ": GATT_INVALID_ATTRIBUTE_LENGTH"); return BluetoothGatt.GATT_INVALID_ATTRIBUTE_LENGTH;
            case (BluetoothGatt.GATT_INVALID_OFFSET): Log.d("DevicePairing", "-- " + function + ": GATT_INVALID_OFFSET"); return BluetoothGatt.GATT_INVALID_OFFSET;
            case (BluetoothGatt.GATT_READ_NOT_PERMITTED): Log.d("DevicePairing", "-- " + function + ": GATT_READ_NOT_PERMITTED"); return BluetoothGatt.GATT_READ_NOT_PERMITTED;
            case (BluetoothGatt.GATT_REQUEST_NOT_SUPPORTED): Log.d("DevicePairing", "-- " + function + ": GATT_REQUEST_NOT_SUPPORTED"); return BluetoothGatt.GATT_REQUEST_NOT_SUPPORTED;
            case (BluetoothGatt.GATT_WRITE_NOT_PERMITTED): Log.d("DevicePairing", "-- " + function + ": GATT_WRITE_NOT_PERMITTED"); return BluetoothGatt.GATT_WRITE_NOT_PERMITTED;
            default: Log.d("DevicePairing", "-- " + function + ": Status unknown"); return BluetoothGatt.GATT_FAILURE;
        }
    }

    public void ScanForBLEDevices(final boolean enable) {
        if (enable) {
            MyListDevices.clear();
            MyListStrawDevices.clear();

            //stops after a pre-defined scan period
            MyHandler.postDelayed(
                    new Runnable() {
                        @Override
                        public void run() {
                            mScanning = StopBLEScan(mLeScanCallback);
                            AskForDeviceForConnecting();

                        }
                    }, SCAN_PERIOD
            );

            mScanning = StartBLEScan(mLeScanCallback);
            //mBluetoothLeScanner.startScan(mLeScanCallback); //for API Level >=21

        }else {
            mScanning = StopBLEScan(mLeScanCallback);
        }

    }

    //LeScanCallbeck returns the found devices
    private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(final BluetoothDevice bluetoothDevice, int rssi, byte[] scanRecord) {
            currentActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    //add all found devices
                    if (!MyListDevices.contains(bluetoothDevice)) {
                        Log.d("DevicePairing", "----- Device found (" + bluetoothDevice.getAddress() + " : " + bluetoothDevice.getName() + ") and saved in MyListDevices.");
                        MyListDevices.add(bluetoothDevice);
                        //add only straw devices
                        if (bluetoothDevice.getName() != null) {
                            if (bluetoothDevice.getName().equals(context.getString(R.string.ble_getName_straw))) {
                                MyListStrawDevices.add(bluetoothDevice);
                            }
                        }
                    }
                }
            });
        }
    };

    public void AskForDeviceForConnecting(){
        //only Straw Devices in MyListStrawDevices
        //all found Devices are saved in MyListDevices
        if (MyListStrawDevices.size() == 0) {
            Toast.makeText(currentActivity.getApplicationContext(), R.string.ble_no_devices_found, Toast.LENGTH_SHORT).show();
        }
        else if (MyListStrawDevices.size() == 1) {
            if (MyBluetoothGatt == null){
                ConnectToGatt(MyListStrawDevices.get(0).getAddress());
                connect_next = null;
            }
            else
                if (MyBluetoothGatt.getDevice().getAddress().equals(MyListStrawDevices.get(0).getAddress()))
                    MyBluetoothGatt.discoverServices();
            else {
                    connect_next = MyListStrawDevices.get(0).getAddress();
                    DisconnectFromGatt();
                }
        }
        else if (MyListStrawDevices.size() > 0) {
            String[] itemsStrings = new String[MyListStrawDevices.size()];
            String[] returnStrings = new String[MyListStrawDevices.size()];
            for (int i = 0; i < MyListStrawDevices.size(); i++) {
                itemsStrings[i] = MyListStrawDevices.get(i).getAddress() + " : " + MyListStrawDevices.get(i).getName();
                returnStrings[i] = MyListStrawDevices.get(i).getAddress();
            }

            PopUpList dialog = new PopUpList();
            dialog.setItemsAndReturnValues(itemsStrings, returnStrings);
            dialog.setDialogType(context.getString(R.string.dialogType_ChooseDevice));
            dialog.setDialogTitle(context.getString(R.string.dialogType_ChooseDevice_Title));
            dialog.show(currentActivity.getSupportFragmentManager(), context.getString(R.string.dialogType_ChooseDevice));
        }
    }

    public void AskForCharacteristicForWriting(){
        String[] itemsStrings = new String[MyListStrawCharacteristicsStrings.size()];
        String[] returnStrings = new String[MyListStrawCharacteristicsStrings.size()];
        for (int i = 0; i < MyListStrawCharacteristicsStrings.size(); i++){
            itemsStrings[i] = MyListStrawCharacteristicsStrings.get(i);
            returnStrings[i] = MyListStrawCharacteristicsStrings.get(i);
        }

        PopUpList dialog = new PopUpList();
        dialog.setItemsAndReturnValues(itemsStrings, returnStrings);
        dialog.setDialogType(context.getString(R.string.dialogType_ChooseValue));
        dialog.setDialogTitle(context.getString(R.string.dialogType_ChooseValue_Title));
        dialog.show(currentActivity.getSupportFragmentManager(), context.getString(R.string.dialogType_ChooseValue));
    }

    public void AskForValueForWriting(String characteristicForWriting){

    }

    public void ConnectToGatt(String address){
        connect_next = null;
        for (BluetoothDevice device : MyListStrawDevices){
            if (device.getAddress().equals(address)){

                //stops after a pre-defined scan period
                MyHandler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                DisconnectFromGatt();
                            }
                        }, 5000);

                //Toast.makeText(currentActivity.getApplicationContext(), context.getString(R.string.ble_connecting), Toast.LENGTH_SHORT).show();
                MyBluetoothGatt = device.connectGatt(context.getApplicationContext(), false, mGattCallback);
            }
        }
    }

    public void DisconnectFromGatt(){
        Log.d("DevicePairing", "-- DisconnectFromGatt()");
        if (MyBluetoothGatt != null)
            MyBluetoothGatt.disconnect();
    }

    public void CloseFromGatt(){
        Log.d("DevicePairing", "-- CloseFromGatt()");
        MyBluetoothGatt.close();
        MyBluetoothGatt = null;
    }

    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {

            EvaluateGattStatus("onConnectionStateChange(STATUS)", status);
            final String address = gatt.getDevice().getAddress();

            switch (newState){
                case (BluetoothProfile.STATE_CONNECTED):

                    currentActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            //Toast.makeText(currentActivity, String.format(context.getString(R.string.ble_connected_with), address), Toast.LENGTH_SHORT).show();
                        }
                    });

                    Log.d("DevicePairing", "-- onConnectionStateChange(STATE): STATE_CONNECTED");
                    Log.d("DevicePairing", "-- DiscoverServices()");
                    if (currentActivity.getComponentName().getClassName().contains("MainActivity"))
                        snackbar_connected = Snackbar.make(currentActivity.findViewById(R.id.clayout), String.format(context.getString(R.string.ble_connected_with), address), Snackbar.LENGTH_INDEFINITE);
                    else if (currentActivity.getComponentName().getClassName().contains("PatientRegistrationActivity"))
                        snackbar_connected = Snackbar.make(currentActivity.getCurrentFocus(), String.format(context.getString(R.string.ble_connected_with), address), Snackbar.LENGTH_INDEFINITE);

                    snackbar_connected.show();
                    MyBluetoothGatt.discoverServices();
                    break;
                case (BluetoothProfile.STATE_CONNECTING): Log.d("DevicePairing", "-- onConnectionStateChange(STATE): STATE_CONNECTING"); break;
                case (BluetoothProfile.STATE_DISCONNECTED):
                    Log.d("DevicePairing", "-- onConnectionStateChange(STATE): STATE_DISCONNECTED");
                    snackbar_connected.dismiss();

                    currentActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            //Toast.makeText(currentActivity, context.getString(R.string.ble_disconnected), Toast.LENGTH_SHORT).show();
                        }
                    });

                    if (connect_next != null) {
                        DisconnectFromGatt();
                        ConnectToGatt(connect_next);
                    }
                    else{
                        CloseFromGatt();
                    }
                    break;
                case (BluetoothProfile.STATE_DISCONNECTING) : Log.d("DevicePairing", "-- onConnectionStateChange(STATE): STATE_DISCONNECTING"); break;
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (EvaluateGattStatus("onServicesDiscovered", status) == BluetoothGatt.GATT_SUCCESS) {
                if (forWriting) {
                    WriteCharacteristics(dataPatientNew);
                    forWriting = false;
                }
                else {
                    dataPatientStraw = new DataPatient();
                    dataDrinkStraw = new DataDrink();
                    dataPatientStraw.setP_mac(gatt.getDevice().getAddress());
                    dataPatientStraw.setP_enabled(true);
                    //dataDrinkStraw.setDevent_timestamp(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.GERMANY).format(new Date()));
                    dataDrinkStraw.setDevent_timestamp(GregorianCalendar.getInstance().getTime());
                    ListServices();
                }
            }
        }

        @Override
        // Result of a characteristic read operation
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         final BluetoothGattCharacteristic characteristic,
                                         int status) {
            //EvaluateGattStatus("onCharacteristicRead", status);
            ListCharacteristic(characteristic);
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            if (EvaluateGattStatus("onCharacteristicWrite", status) == BluetoothGatt.GATT_SUCCESS){
                //string_write_notification = "Written successfully";
                Log.d("DevicePairing", "---- " + characteristic.getUuid().toString() + " : " + characteristic.getStringValue(0) + " -> Value written");
                //currentActivity.runOnUiThread(new Runnable() { @Override public void run() { Toast.makeText(currentActivity, string_write_notification, Toast.LENGTH_SHORT).show(); } });
                if (characteristic.getUuid().toString().equals(context.getString(R.string.strawCharacteristic_UserId_uuid))) {
                    String value = characteristic.getStringValue(0);
                    String value2;
                    if (value.contains("*"))
                        value2 = value.substring(0, value.indexOf("*"));
                    else
                        value2 = value;
                    dataPatientNew.setP_key(value2);
                    dataPatientNew.setP_mac(gatt.getDevice().getAddress());
                }
                if (forDrinkReset) WriteResetData();
                else if (forSingleWriting) forSingleWriting = false;
                else { WriteCharacteristic(); }
            }

        }
    };

    private void ListServices(){
        Log.d("DevicePairing", "-- ListServices");

        MyListServices.clear();
        MyListCharacteristics.clear();

        for ( BluetoothGattService mGattService : MyBluetoothGatt.getServices()){
            Log.d("DevicePairing", "---- Service " + mGattService.getUuid());

            MyListServices.add(mGattService);

            for (BluetoothGattCharacteristic mGattCharacteristic : mGattService.getCharacteristics()){
                Log.d("DevicePairing", "------ Chara " + mGattCharacteristic.getUuid() + " (" +
                        mGattCharacteristic.getDescriptors().size() +" Descriptors, " +
                        mGattCharacteristic.getPermissions() + " Permissions, " +
                        mGattCharacteristic.getProperties() + " PropertiesBitMask)");

                MyListCharacteristics.add(mGattCharacteristic);

            }
        }

        Log.d("DevicePairing", "---- " + MyListServices.size() + " Services and " + MyListCharacteristics.size() + " Characteristics (current " + count_characteristics_current + ", sum " + MyListCharacteristics.size() + ")");
        Log.d("DevicePairing", "-- ReadCharacteristics()");
        MyBluetoothGatt.readCharacteristic(MyListCharacteristics.get(count_characteristics_current));

    }

    private void ListCharacteristic(BluetoothGattCharacteristic characteristic){

        characteristic_string_output = " ";
        characteristic_string_output_debug = " ";

        if (characteristic.getUuid().toString().equals(context.getString(R.string.strawCharacteristic_DrunkVolume_uuid))){
            int value = characteristic.getIntValue(GetFormatId(context.getString(R.string.strawCharacteristic_DrunkVolume_format)), 0);
            dataDrinkStraw.setDevent_volumen((double) value);
            characteristic_value = String.valueOf(value);
            characteristic_string_output = characteristic_string_output.concat(context.getString(R.string.strawCharacteristic_DrunkVolume_name) + " : " + characteristic_value);
            characteristic_string_output_debug = characteristic_string_output_debug.concat(characteristic.getUuid().toString() + " : " + context.getString(R.string.strawCharacteristic_DrunkVolume_name) + " : " + characteristic_value + " (" + context.getString(R.string.strawCharacteristic_DrunkVolume_format) + ", " + characteristic.getValue().length + " Bytes) : ");
        }
        else if (characteristic.getUuid().toString().equals(context.getString(R.string.strawCharacteristic_PulseCount_uuid))){
            int value = characteristic.getIntValue(GetFormatId(context.getString(R.string.strawCharacteristic_PulseCount_format)), 0);
            pulseCountStraw = value;
            characteristic_value = String.valueOf(value);
            characteristic_string_output = characteristic_string_output.concat(context.getString(R.string.strawCharacteristic_PulseCount_name) + " : " + characteristic_value);
            characteristic_string_output_debug = characteristic_string_output_debug.concat(characteristic.getUuid().toString() + " : " + context.getString(R.string.strawCharacteristic_PulseCount_name) + " : " + characteristic_value + " (" + context.getString(R.string.strawCharacteristic_PulseCount_format) + ", " + characteristic.getValue().length + " Bytes) : ");
        }
        else if (characteristic.getUuid().toString().equals(context.getString(R.string.strawCharacteristic_NumberDrinkEvents_uuid))){
            int value = characteristic.getIntValue(GetFormatId(context.getString(R.string.strawCharacteristic_NumberDrinkEvents_format)), 0);
            numberDrinkEventsStraw = value;
            characteristic_value = String.valueOf(value);
            characteristic_string_output = characteristic_string_output.concat(context.getString(R.string.strawCharacteristic_NumberDrinkEvents_name) + " : " + characteristic_value);
            characteristic_string_output_debug = characteristic_string_output_debug.concat(characteristic.getUuid().toString() + " : " + context.getString(R.string.strawCharacteristic_NumberDrinkEvents_name) + " : " + characteristic_value + " (" + context.getString(R.string.strawCharacteristic_NumberDrinkEvents_format) + ", " + characteristic.getValue().length + " Bytes) : ");
        }
        else if (characteristic.getUuid().toString().equals(context.getString(R.string.strawCharacteristic_PulseMultiplier_uuid))){
            float value = ByteBuffer.wrap(characteristic.getValue()).getFloat();
            pulseMultiplierStraw = value;
            characteristic_value = String.valueOf(value);
            characteristic_string_output = characteristic_string_output.concat(context.getString(R.string.strawCharacteristic_PulseMultiplier_name) + " : " + characteristic_value);
            characteristic_string_output_debug = characteristic_string_output_debug.concat(characteristic.getUuid().toString() + " : " + context.getString(R.string.strawCharacteristic_PulseMultiplier_name) + " : " + characteristic_value + " (" + context.getString(R.string.strawCharacteristic_PulseMultiplier_format) + ", " + characteristic.getValue().length + " Bytes) : ");
        }
        else if (characteristic.getUuid().toString().equals(context.getString(R.string.strawCharacteristic_FirstName_uuid))){
            String value = characteristic.getStringValue(0);
            String value2;
            if (value.contains("*"))
                value2 = value.substring(0, value.indexOf("*"));
            else
                value2 = value;
            dataPatientStraw.setP_firstname(value2);
            characteristic_value = value2;
            characteristic_string_output = characteristic_string_output.concat(context.getString(R.string.strawCharacteristic_FirstName_name) + " : " + characteristic_value);
            characteristic_string_output_debug = characteristic_string_output_debug.concat(characteristic.getUuid().toString() + " : " + context.getString(R.string.strawCharacteristic_FirstName_name) + " : " + characteristic_value + " (" + context.getString(R.string.strawCharacteristic_FirstName_format) + ", " + characteristic.getValue().length + " Bytes) : ");
        }
        else if (characteristic.getUuid().toString().equals(context.getString(R.string.strawCharacteristic_LastName_uuid))){
            String value = characteristic.getStringValue(0);
            String value2;
            if (value.contains("*"))
                value2 = value.substring(0, value.indexOf("*"));
            else
                value2 = value;
            dataPatientStraw.setP_lastname(value2);
            characteristic_value = value2;
            characteristic_string_output = characteristic_string_output.concat(context.getString(R.string.strawCharacteristic_LastName_name) + " : " + characteristic_value);
            characteristic_string_output_debug = characteristic_string_output_debug.concat(characteristic.getUuid().toString() + " : " + context.getString(R.string.strawCharacteristic_LastName_name) + " : " + characteristic_value + " (" + context.getString(R.string.strawCharacteristic_LastName_format) + ", " + characteristic.getValue().length + " Bytes) : ");
        }
        else if (characteristic.getUuid().toString().equals(context.getString(R.string.strawCharacteristic_UserId_uuid))){
            //int value = characteristic.getIntValue(GetFormatId(context.getString(R.string.strawCharacteristic_UserId_format)), 0);
            String value = characteristic.getStringValue(0);
            String value2;
            if (value.contains("*"))
                value2 = value.substring(0, value.indexOf("*"));
            else
                value2 = value;
            dataDrinkStraw.setDevent_p_key(value2);
            dataPatientStraw.setP_key(value2);
            dataPatientStraw.setP_birthdate(value2.substring(0, value2.indexOf(" ")).concat(" 00:00:00.00"));
            characteristic_value = value;
            characteristic_string_output = characteristic_string_output.concat(context.getString(R.string.strawCharacteristic_UserId_name) + " : " + characteristic_value);
            characteristic_string_output_debug = characteristic_string_output_debug.concat(characteristic.getUuid().toString() + " : " + context.getString(R.string.strawCharacteristic_UserId_name) + " : " + characteristic_value + " (" + context.getString(R.string.strawCharacteristic_UserId_format) + ", " + characteristic.getValue().length + " Bytes) : ");
        }
        else if (characteristic.getUuid().toString().equals(context.getString(R.string.strawCharacteristic_Gender_uuid))){
            char value = (char) characteristic.getValue()[0];
            if (value == 'm') { dataPatientStraw.setP_sex(true); }
            else { dataPatientStraw.setP_sex(false); }
            characteristic_value = String.valueOf(value);
            characteristic_string_output = characteristic_string_output.concat(context.getString(R.string.strawCharacteristic_Gender_name) + " : " + characteristic_value);
            characteristic_string_output_debug = characteristic_string_output_debug.concat(characteristic.getUuid().toString() + " : " + context.getString(R.string.strawCharacteristic_Gender_name) + " : " + characteristic_value + " (" + context.getString(R.string.strawCharacteristic_Gender_format) + ", " + characteristic.getValue().length + " Bytes) : ");
        }
        else if (characteristic.getUuid().toString().equals(context.getString(R.string.strawCharacteristic_BatteryLevel_uuid))){
            int value = characteristic.getIntValue(GetFormatId(context.getString(R.string.strawCharacteristic_BatteryLevel_format)), 0);
            batteryLevelStraw = value;
            characteristic_value = String.valueOf(value);
            characteristic_string_output = characteristic_string_output.concat(context.getString(R.string.strawCharacteristic_BatteryLevel_name) + " : " + characteristic_value);
            characteristic_string_output_debug = characteristic_string_output_debug.concat(characteristic.getUuid().toString() + " : " + context.getString(R.string.strawCharacteristic_BatteryLevel_name) + " : " + characteristic_value + " (" + context.getString(R.string.strawCharacteristic_BatteryLevel_format) + ", " + characteristic.getValue().length + " Bytes) : ");
        }
        else{
            characteristic_value = characteristic.getStringValue(0);
            characteristic_string_output = characteristic_string_output.concat(characteristic.getUuid().toString() + " : " + characteristic_value);
            characteristic_string_output_debug = characteristic_string_output_debug.concat(characteristic.getUuid().toString() + " : Unknown : " + characteristic_value + " (" + characteristic.getValue().length + " Bytes) : ");
        }

        //Show permissions and properties
        if ( (characteristic.getProperties() & BluetoothGattCharacteristic.PROPERTY_READ) != 0)
            characteristic_string_output_debug = characteristic_string_output_debug.concat("PROPERTY_READ, ");
        if ( (characteristic.getProperties() & BluetoothGattCharacteristic.PROPERTY_WRITE) != 0)
            characteristic_string_output_debug = characteristic_string_output_debug.concat("PROPERTY_WRITE, ");
        if ( (characteristic.getProperties() & BluetoothGattCharacteristic.PERMISSION_READ) != 0)
            characteristic_string_output_debug = characteristic_string_output_debug.concat("PERMISSION_READ, ");
        if ( (characteristic.getProperties() & BluetoothGattCharacteristic.PERMISSION_READ_ENCRYPTED) != 0)
            characteristic_string_output_debug = characteristic_string_output_debug.concat("PERMISSION_READ_ENCRYPTED, ");
        if ( (characteristic.getProperties() & BluetoothGattCharacteristic.PERMISSION_READ_ENCRYPTED_MITM) != 0)
            characteristic_string_output_debug = characteristic_string_output_debug.concat("PERMISSION_READ_ENCRYPTED_MITM, ");
        if ( (characteristic.getProperties() & BluetoothGattCharacteristic.PERMISSION_WRITE) != 0)
            characteristic_string_output_debug = characteristic_string_output_debug.concat("PERMISSION_WRITE, ");
        if ( (characteristic.getProperties() & BluetoothGattCharacteristic.PERMISSION_WRITE_ENCRYPTED) != 0)
            characteristic_string_output_debug = characteristic_string_output_debug.concat("PERMISSION_WRITE_ENCRYPTED, ");
        if ( (characteristic.getProperties() & BluetoothGattCharacteristic.PERMISSION_READ_ENCRYPTED_MITM) != 0)
            characteristic_string_output_debug = characteristic_string_output_debug.concat("PERMISSION_READ_ENCRYPTED_MITM, ");
        if ( (characteristic.getProperties() & BluetoothGattCharacteristic.PERMISSION_WRITE_SIGNED) != 0)
            characteristic_string_output_debug = characteristic_string_output_debug.concat("PERMISSION_WRITE_SIGNED, ");
        if ( (characteristic.getProperties() & BluetoothGattCharacteristic.PERMISSION_WRITE_SIGNED_MITM) != 0)
            characteristic_string_output_debug = characteristic_string_output_debug.concat("PERMISSION_WRITE_SIGNED_MITM, ");

        Log.d("DevicePairing", "--(" + count_characteristics_current + ")-- " + characteristic_string_output_debug);

        count_characteristics_current++;

        if (count_characteristics_current < MyListCharacteristics.size()) {

            if (count_characteristics_current == 4) {
                count_characteristics_current++;
            }

            //for some time writing wasn't functional (I got the GOT_REQUEST_NOT_SUPPORTED on the onCharacteristicWrite() callback)
            //i tried this, this way worked
            //afterwards the other way through the button worked again
            /*if (characteristic.getUuid().toString().equals(uuidCharacteristic_DrunkVolume.toString())){
                int value = random.nextInt(30);
                characteristic.setValue(value, BluetoothGattCharacteristic.FORMAT_UINT16, 0);
                Log.d("DevicePairing", "---- Writing to " + characteristic.getUuid() + " : DrunkVolume : " + value + " (FORMAT_UINT16)");
                MyBluetoothGatt.writeCharacteristic(characteristic);
            }
            else*/
            MyBluetoothGatt.readCharacteristic(MyListCharacteristics.get(count_characteristics_current));

        }
        else {
            Log.d("DevicePairing", "---- All Characteristics read");
            count_characteristics_current = 0;

            /*currentActivity.runOnUiThread(new Runnable() { @Override public void run() {
                Toast.makeText(currentActivity, dataPatientStraw.toString(), Toast.LENGTH_SHORT).show();
                Toast.makeText(currentActivity, dataDrinkStraw.toString(), Toast.LENGTH_SHORT).show();
            } });*/

            dataDrinkStraw.setDevent_volumen((dataDrinkStraw.getDevent_volumen()==0)?pulseCountStraw*pulseMultiplierStraw:dataDrinkStraw.getDevent_volumen());
            if (dataPatientStraw.getP_birthdate() != null) {
                Log.d("DevicePairing", "-- dataPatientStraw: " + dataPatientStraw.toString());
                Log.d("DevicePairing", "-- dataDrinkStraw" + dataDrinkStraw.toString());
            }
            else{
                Log.d("DevicePairing", "-- Error: Wrong date format for birthday.");
            }

            ResetDrinkData();

        }

    }

    private int GetFormatId(String characteristicFormat){
        switch (characteristicFormat){
            case "FORMAT_UINT8": return BluetoothGattCharacteristic.FORMAT_UINT8;
            case "FORMAT_UINT16": return BluetoothGattCharacteristic.FORMAT_UINT16;
            case "FORMAT_UINT32": return BluetoothGattCharacteristic.FORMAT_UINT32;
            case "FORMAT_FLOAT": return BluetoothGattCharacteristic.FORMAT_FLOAT;
            default: return 0;
        }
    }

    public void WriteCharacteristics(DataPatient dataPatientNew){
        if (MyBluetoothGatt == null){
            this.dataPatientNew = dataPatientNew;
            ScanForBLEDevices(true);
            return;
        }
        count_characteristics_current_write = 4;

        Log.d("DevicePairing", "-- WriteCharacteristic");
        WriteCharacteristic();

    }

    public void WriteCharacteristic(){

        String characteristicNameString = MyListStrawCharacteristicsStrings.get(count_characteristics_current_write);

        if (count_characteristics_current_write < MyListStrawCharacteristicsStrings.size()-1){
            WriteCharacteristic(characteristicNameString);
            count_characteristics_current_write++;
        }
        else{
            Log.d("DevicePairing", "-- All Characteristics written");
            count_characteristics_current_write = 4;
            forWriting = false;
            currentActivity.runOnUiThread(new Runnable() { @Override public void run() { Toast.makeText(currentActivity, R.string.ble_written_successful, Toast.LENGTH_SHORT).show(); } });
            //notify called Activity
            MyNoticeBLEListener.onBLEWriteFinished(dataPatientNew);
            DisconnectFromGatt();
            //CloseFromGatt();
        }
    }

    public void WriteCharacteristic(String characteristicNameString) {

        BluetoothGattCharacteristic characteristic;

        if (characteristicNameString.equals(context.getString(R.string.strawCharacteristic_DrunkVolume_name))){
            //int value = random.nextInt(30);
            int value = drunkVolumeReset;

            characteristic = MyBluetoothGatt.getService(UUID.fromString(context.getString(R.string.strawService_VolumeData_uuid))).getCharacteristic(UUID.fromString(context.getString(R.string.strawCharacteristic_DrunkVolume_uuid)));
            characteristic.setValue(value, GetFormatId(context.getString(R.string.strawCharacteristic_DrunkVolume_format)), 0);

            string_write_notification = "Writing to " + characteristic.getUuid() + " : " + context.getString(R.string.strawCharacteristic_DrunkVolume_name) + " : " + value + " (" + context.getString(R.string.strawCharacteristic_DrunkVolume_format) + ")";
            Log.d("DevicePairing", "---- " + string_write_notification);
            //currentActivity.runOnUiThread(new Runnable() { @Override public void run() { Toast.makeText(currentActivity, string_write_notification, Toast.LENGTH_SHORT).show(); } });

            MyBluetoothGatt.writeCharacteristic(characteristic);
        }
        else if (characteristicNameString.equals(context.getString(R.string.strawCharacteristic_PulseCount_name))){
            //int value = random.nextInt(30);
            int value = pulseCountReset;

            characteristic = MyBluetoothGatt.getService(UUID.fromString(context.getString(R.string.strawService_VolumeData_uuid))).getCharacteristic(UUID.fromString(context.getString(R.string.strawCharacteristic_PulseCount_uuid)));
            characteristic.setValue(value, GetFormatId(context.getString(R.string.strawCharacteristic_PulseCount_format)), 0);

            string_write_notification = "Writing to " + characteristic.getUuid() + " : " + context.getString(R.string.strawCharacteristic_PulseCount_name) + " : " + value + " (" + context.getString(R.string.strawCharacteristic_PulseCount_format) + ")";
            Log.d("DevicePairing", "---- " + string_write_notification);
            //currentActivity.runOnUiThread(new Runnable() { @Override public void run() { Toast.makeText(currentActivity, string_write_notification, Toast.LENGTH_SHORT).show(); } });

            MyBluetoothGatt.writeCharacteristic(characteristic);
        }
        else if (characteristicNameString.equals(context.getString(R.string.strawCharacteristic_NumberDrinkEvents_name))){
            //int value = random.nextInt(30);
            int value = numberDrinkEventsReset;

            characteristic = MyBluetoothGatt.getService(UUID.fromString(context.getString(R.string.strawService_VolumeData_uuid))).getCharacteristic(UUID.fromString(context.getString(R.string.strawCharacteristic_NumberDrinkEvents_uuid)));
            characteristic.setValue(value, GetFormatId(context.getString(R.string.strawCharacteristic_PulseCount_format)), 0);

            string_write_notification = "Writing to " + characteristic.getUuid() + " : " + context.getString(R.string.strawCharacteristic_NumberDrinkEvents_name) + " : " + value + " (" + context.getString(R.string.strawCharacteristic_PulseCount_format) + ")";
            Log.d("DevicePairing", "---- " + string_write_notification);
            //currentActivity.runOnUiThread(new Runnable() { @Override public void run() { Toast.makeText(currentActivity, string_write_notification, Toast.LENGTH_SHORT).show(); } });

            MyBluetoothGatt.writeCharacteristic(characteristic);
        }
        else if (characteristicNameString.equals(context.getString(R.string.strawCharacteristic_PulseMultiplier_name))){
            //float value = random.nextFloat();
            float value = pulseMultiplierReset;

            characteristic = MyBluetoothGatt.getService(UUID.fromString(context.getString(R.string.strawService_VolumeData_uuid))).getCharacteristic(UUID.fromString(context.getString(R.string.strawCharacteristic_PulseMultiplier_uuid)));
            byte[] valueBytes = ByteBuffer.allocate(4).putFloat(value).array();
            characteristic.setValue(valueBytes);

            string_write_notification = "Writing to " + characteristic.getUuid() + " : " + context.getString(R.string.strawCharacteristic_PulseMultiplier_name) + " : " + value + " (" + context.getString(R.string.strawCharacteristic_PulseMultiplier_format) + ")";
            Log.d("DevicePairing", "---- " + string_write_notification);
            //currentActivity.runOnUiThread(new Runnable() { @Override public void run() { Toast.makeText(currentActivity, string_write_notification, Toast.LENGTH_SHORT).show(); } });

            MyBluetoothGatt.writeCharacteristic(characteristic);
        }
        else if (characteristicNameString.equals(context.getString(R.string.strawCharacteristic_FirstName_name))){
            //String value = "martin";
            String value = dataPatientNew.getP_firstname();

            characteristic = MyBluetoothGatt.getService(UUID.fromString(context.getString(R.string.strawService_UserData_uuid))).getCharacteristic(UUID.fromString(context.getString(R.string.strawCharacteristic_FirstName_uuid)));
            //for (int k = value.length(); k < 22; k++){
                value = value.concat(String.valueOf("*"));
            //}
            //byte[] valueBytes = new byte[value.length()];
            //System.arraycopy(Charset.forName("UTF-8").encode(value).array(), 0, valueBytes, 0, value.length());
            try {
                characteristic.setValue(value.getBytes("UTF-8"));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

            string_write_notification = "Writing to " + characteristic.getUuid() + " : " + context.getString(R.string.strawCharacteristic_FirstName_name) + " : " + value + " (" + context.getString(R.string.strawCharacteristic_FirstName_format) + ")";
            Log.d("DevicePairing", "---- " + string_write_notification);
            //currentActivity.runOnUiThread(new Runnable() { @Override public void run() { Toast.makeText(currentActivity, string_write_notification, Toast.LENGTH_SHORT).show(); } });

            MyBluetoothGatt.writeCharacteristic(characteristic);
        }
        else if (characteristicNameString.equals(context.getString(R.string.strawCharacteristic_LastName_name))){
            //String value = "Lustig";
            String value = dataPatientNew.getP_lastname();

            characteristic = MyBluetoothGatt.getService(UUID.fromString(context.getString(R.string.strawService_UserData_uuid))).getCharacteristic(UUID.fromString(context.getString(R.string.strawCharacteristic_LastName_uuid)));
            //for (int k = value.length(); k < 22; k++){
                value = value.concat(String.valueOf("*"));
            //}
            //byte[] valueBytes = new byte[value.length()];
            //System.arraycopy(Charset.forName("UTF-8").encode(value).array(), 0, valueBytes, 0, value.length());
            try {
                characteristic.setValue(value.getBytes("UTF-8"));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

            string_write_notification = "Writing to " + characteristic.getUuid() + " : " + context.getString(R.string.strawCharacteristic_LastName_name) + " : " + value + " (" + context.getString(R.string.strawCharacteristic_LastName_format) + ")";
            Log.d("DevicePairing", "---- " + string_write_notification);
            //currentActivity.runOnUiThread(new Runnable() { @Override public void run() { Toast.makeText(currentActivity, string_write_notification, Toast.LENGTH_SHORT).show(); } });

            MyBluetoothGatt.writeCharacteristic(characteristic);
        }
        else if (characteristicNameString.equals(context.getString(R.string.strawCharacteristic_UserId_name))){
            //int value = random.nextInt(1000);
            //characteristic.setValue(value, GetFormatId(context.getString(R.string.strawCharacteristic_UserId_format)), 0);
            //String value = dataPatientNew.getP_key();
            String value = dataPatientNew.getP_birthdateString();

            //SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.GERMANY);
            //String valueAdd = df.format(GregorianCalendar.getInstance().getTime());
            String valueAdd = String.valueOf(GregorianCalendar.getInstance().getTimeInMillis());

            value = value.substring(0, value.indexOf(" ")).concat(" ").concat(valueAdd).substring(0, 19);

            characteristic = MyBluetoothGatt.getService(UUID.fromString(context.getString(R.string.strawService_UserData_uuid))).getCharacteristic(UUID.fromString(context.getString(R.string.strawCharacteristic_UserId_uuid)));
            //int idLength = (value.length() < 22) ? value.length() : 22;
            //for (int k = value.length(); k < 22; k++){
                value = value.concat(String.valueOf("*"));
            //}
            //byte[] valueBytes = new byte[22];
            //System.arraycopy(Charset.forName("UTF-8").encode(value).array(), 0, valueBytes, 0, 22);
            try {
                characteristic.setValue(value.getBytes("UTF-8"));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

            string_write_notification = "Writing to " + characteristic.getUuid() + " : " + context.getString(R.string.strawCharacteristic_UserId_name) + " : " + value + " (" + context.getString(R.string.strawCharacteristic_UserId_format) + ")";
            Log.d("DevicePairing", "---- " + string_write_notification);
            //currentActivity.runOnUiThread(new Runnable() { @Override public void run() { Toast.makeText(currentActivity, string_write_notification, Toast.LENGTH_SHORT).show(); } });

            MyBluetoothGatt.writeCharacteristic(characteristic);
        }
        else if (characteristicNameString.equals(context.getString(R.string.strawCharacteristic_Gender_name))){
            //char value = 'm';
            char value;
            value = dataPatientNew.isP_sex() ? 'm' : 'f';

            characteristic = MyBluetoothGatt.getService(UUID.fromString(context.getString(R.string.strawService_UserData_uuid))).getCharacteristic(UUID.fromString(context.getString(R.string.strawCharacteristic_Gender_uuid)));
            byte[] value2 = {(byte) value};
            characteristic.setValue(value2);

            string_write_notification = "Writing to " + characteristic.getUuid() + " : " + context.getString(R.string.strawCharacteristic_Gender_name) + " : " + value + " (" + context.getString(R.string.strawCharacteristic_Gender_format) + ")";
            Log.d("DevicePairing", "---- " + string_write_notification);
            //currentActivity.runOnUiThread(new Runnable() { @Override public void run() { Toast.makeText(currentActivity, string_write_notification, Toast.LENGTH_SHORT).show(); } });

            MyBluetoothGatt.writeCharacteristic(characteristic);
        }
    }

    public void ResetDrinkData(){

        if (MyBluetoothGatt == null){
            ScanForBLEDevices(true);
            return;
        }

        forDrinkReset = true;
        count_characteristics_current_reset = 0;

        Log.d("DevicePairing", "-- ResetDrinkData");
        WriteResetData();
    }

    private void WriteResetData(){
        String characteristicNameString = MyListStrawCharacteristicsStrings.get(count_characteristics_current_reset);

        if (count_characteristics_current_reset < 4){
            Log.d("DevicePairing", "-- count current reset = " + count_characteristics_current_reset);
            WriteCharacteristic(characteristicNameString);
            count_characteristics_current_reset++;
        }
        else{
            Log.d("DevicePairing", "-- Deleting Drink Data finished");
            count_characteristics_current_reset = 0;
            forDrinkReset = false;
            currentActivity.runOnUiThread(new Runnable() { @Override public void run() { Toast.makeText(currentActivity, R.string.ble_drink_event_deleted, Toast.LENGTH_SHORT).show(); } });
            //notify called Activity
            MyNoticeBLEListener.onBLEScanAndReadFinished(dataPatientStraw, dataDrinkStraw, batteryLevelStraw);
            //MyNoticeBLEListener.onBLEWriteFinished(dataPatientNew);
            DisconnectFromGatt();
            //CloseFromGatt();
        }

    }

    public void ResetAllStrawData(){
        DataPatient dataPatientReset = new DataPatient("00:00:00:00:00:00", "firstname", "lastname", "0000-00-00 00:00:00.00", false, false, "key");
        forWriting = true;
        WriteCharacteristics(dataPatientReset);
    }

}
