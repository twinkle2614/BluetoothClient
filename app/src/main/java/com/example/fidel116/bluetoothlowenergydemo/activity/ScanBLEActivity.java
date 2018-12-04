package com.example.fidel116.bluetoothlowenergydemo.activity;

import android.annotation.TargetApi;
import android.app.ListActivity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.fidel116.bluetoothlowenergydemo.R;
import com.example.fidel116.bluetoothlowenergydemo.model.SendBleDataModel;

import java.util.ArrayList;
import java.util.List;

@TargetApi(21)
public class ScanBLEActivity extends AppCompatActivity {

    private Button btn_scan;
    private ListView scan_list;
    private LeDeviceListAdapter mLeDeviceListAdapter;
    private BluetoothAdapter mBluetoothAdapter;
    private boolean mScanning;
    private Handler mHandler;


    private static final int REQUEST_ENABLE_BT = 1;
    // Stops scanning after 10 seconds.
    private static final long SCAN_PERIOD = 10000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_ble);

        //initialization of components...
        btn_scan = (Button) findViewById(R.id.btn_scan);
        scan_list = (ListView) findViewById(R.id.scan_list);
        SendBleDataModel sendBleDataModel = new SendBleDataModel("eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJ0d2lua2xlLnRoYWtrYXJAZmlkZWxpdHNlcnZpY2VzLmNvbSIsInNjb3BlcyI6WyJDVVNUT01FUl9VU0VSIl0sInVzZXJJZCI6Ijg1MDFlYjYwLWQwNDAtMTFlOC1hOTE2LWNiZWIzMTU1YjdlZSIsImZpcnN0TmFtZSI6InR3aW5rbGUiLCJsYXN0TmFtZSI6InRoYWtrYXIiLCJlbmFibGVkIjp0cnVlLCJpc1B1YmxpYyI6ZmFsc2UsInRlbmFudElkIjoiNTI1M2E0ZDAtN2ExNy0xMWU4LWIyMzYtZGRiMWE4OTQ0MzJiIiwiY3VzdG9tZXJJZCI6ImNjMDcxODUwLTgwMzEtMTFlOC04NzMxLTZmZjBlMmQ2YTdkNiIsImlzcyI6InRoaW5nc2JvYXJkLmlvIiwiaWF0IjoxNTQyOTcyMzAyLCJleHAiOjE1NDI5NzMyMDJ9.zVi-GX3_-mIed2ShzYIyEq19C2mYu3bCS0_oY-IPpJEZ-xCa8EfcvMuw24r3JidzWCOUhPwGyYS9_NGGqKNQpw",
                "8501eb60-d040-11e8-a916-cbeb3155b7ee");

        sendBleDataModel.setToken("eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJ0d2lua2xlLnRoYWtrYXJAZmlkZWxpdHNlcnZpY2VzLmNvbSIsInNjb3BlcyI6WyJDVVNUT01FUl9VU0VSIl0sInVzZXJJZCI6Ijg1MDFlYjYwLWQwNDAtMTFlOC1hOTE2LWNiZWIzMTU1YjdlZSIsImZpcnN0TmFtZSI6InR3aW5rbGUiLCJsYXN0TmFtZSI6InRoYWtrYXIiLCJlbmFibGVkIjp0cnVlLCJpc1B1YmxpYyI6ZmFsc2UsInRlbmFudElkIjoiNTI1M2E0ZDAtN2ExNy0xMWU4LWIyMzYtZGRiMWE4OTQ0MzJiIiwiY3VzdG9tZXJJZCI6ImNjMDcxODUwLTgwMzEtMTFlOC04NzMxLTZmZjBlMmQ2YTdkNiIsImlzcyI6InRoaW5nc2JvYXJkLmlvIiwiaWF0IjoxNTQyOTcyMzAyLCJleHAiOjE1NDI5NzMyMDJ9.zVi-GX3_-mIed2ShzYIyEq19C2mYu3bCS0_oY-IPpJEZ-xCa8EfcvMuw24r3JidzWCOUhPwGyYS9_NGGqKNQpw");
        sendBleDataModel.setUserId("8501eb60-d040-11e8-a916-cbeb3155b7ee");
        //Handler initialization
        mHandler = new Handler();

        //Check for ble supported hardware...
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
            finish();
        }

        //Check for bluetooth support... And initiate bluetooth service...
        final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, R.string.error_bluetooth_not_supported, Toast.LENGTH_SHORT).show();
            finish();
            return;

        }

        //scan button click listener...
        btn_scan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                scanLeDevice(true);
            }
        });

        //On item click listener...
        scan_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final BluetoothDevice device = mLeDeviceListAdapter.getDevice(position);
                if (device == null) return;
                final Intent intent = new Intent(getApplicationContext(), DeviceControlActivity.class);
                intent.putExtra(DeviceControlActivity.EXTRAS_DEVICE_NAME, device.getName());
                intent.putExtra(DeviceControlActivity.EXTRAS_DEVICE_ADDRESS, device.getAddress());
                if (mScanning) {
                    mBluetoothAdapter.stopLeScan(mLeScanCallback);
                    mScanning = false;
                }
                startActivity(intent);
            }
        });
    }
    @Override
    protected void onResume() {
        super.onResume();

        // Ensures Bluetooth is enabled on the device.  If Bluetooth is not currently enabled,
        // fire an intent to display a dialog asking the user to grant permission to enable it.
        if (!mBluetoothAdapter.isEnabled()) {
            if (!mBluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            }
        }
        // Initializes list view adapter.
        mLeDeviceListAdapter = new LeDeviceListAdapter();
        scan_list.setAdapter(mLeDeviceListAdapter);
        scanLeDevice(true);
    }

    private void scanLeDevice(final boolean enable) {
        if (enable) {
            // Stops scanning after a pre-defined scan period.
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mScanning = false;
                    mBluetoothAdapter.stopLeScan(mLeScanCallback);
                    invalidateOptionsMenu();
                }
            }, SCAN_PERIOD);

            mScanning = true;
            mBluetoothAdapter.startLeScan(mLeScanCallback);
        } else {
            mScanning = false;
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
        }
        invalidateOptionsMenu();
    }

    // Adapter for holding devices found through scanning.
    private class LeDeviceListAdapter extends BaseAdapter {
        private ArrayList<BluetoothDevice> mLeDevices;
        private ArrayList<Integer> rssi ;
        private LayoutInflater mInflator;

        public LeDeviceListAdapter() {
            super();
            mLeDevices = new ArrayList<BluetoothDevice>();
            rssi = new ArrayList();
            mInflator = ScanBLEActivity.this.getLayoutInflater();
        }

        public void addDevice(BluetoothDevice device) {
            if(!mLeDevices.contains(device)) {
                mLeDevices.add(device);
            }
        }
        public void addRssi(int rssi) {
            if(!this.rssi.contains(rssi)) {
                this.rssi.add(rssi);
            }
        }

        public BluetoothDevice getDevice(int position) {
            return mLeDevices.get(position);
        }

        public void clear() {
            mLeDevices.clear();
        }

        @Override
        public int getCount() {
            return mLeDevices.size();
        }

        @Override
        public Object getItem(int i) {
            return mLeDevices.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            ScanBLEActivity.ViewHolder viewHolder;
            // General ListView optimization code.
            if (view == null) {
                view = mInflator.inflate(R.layout.listitem_device, null);
                viewHolder = new ScanBLEActivity.ViewHolder();
                viewHolder.deviceAddress = (TextView) view.findViewById(R.id.device_address);
                viewHolder.deviceName = (TextView) view.findViewById(R.id.device_name);
                viewHolder.deviceRssi = (TextView) view.findViewById(R.id.device_rssi);
                view.setTag(viewHolder);
            } else {
                viewHolder = (ScanBLEActivity.ViewHolder) view.getTag();
            }

            BluetoothDevice device = mLeDevices.get(i);
            final String deviceName = device.getName();
            if (deviceName != null && deviceName.length() > 0)
                viewHolder.deviceName.setText(deviceName);
            else
                viewHolder.deviceName.setText(R.string.unknown_device);
            viewHolder.deviceAddress.setText(device.getAddress());

            int rssi = this.rssi.get(i);
            viewHolder.deviceRssi.setText(String.valueOf(rssi));

            return view;
        }
    }

    // Device scan callback.
    private BluetoothAdapter.LeScanCallback mLeScanCallback =
            new BluetoothAdapter.LeScanCallback() {

                @Override
                public void onLeScan(final BluetoothDevice device, final int rssi, byte[] scanRecord) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mLeDeviceListAdapter.addDevice(device);
                            mLeDeviceListAdapter.addRssi(rssi);
                            mLeDeviceListAdapter.notifyDataSetChanged();
                        }
                    });
                }
            };

    static class ViewHolder {
        TextView deviceName;
        TextView deviceAddress;
        TextView deviceRssi;
    }
}
