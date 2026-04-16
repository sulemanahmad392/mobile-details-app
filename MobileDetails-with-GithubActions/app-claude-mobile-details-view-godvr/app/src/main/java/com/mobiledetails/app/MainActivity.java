package com.mobiledetails.app;

import android.Manifest;
import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.StatFs;
import android.telephony.TelephonyManager;
import android.text.format.Formatter;
import android.util.DisplayMetrics;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private static final int PERM_REQ = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Request runtime permission for READ_PHONE_STATE (API 23+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.READ_PHONE_STATE)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_PHONE_STATE}, PERM_REQ);
                return; // will reload in onRequestPermissionsResult
            }
        }
        loadDetails();
    }

    @Override
    public void onRequestPermissionsResult(int req, String[] perms, int[] results) {
        super.onRequestPermissionsResult(req, perms, results);
        loadDetails(); // reload regardless – some info available without permission
    }

    // -------------------------------------------------------------------------
    // Build the full detail list and bind to RecyclerView
    // -------------------------------------------------------------------------
    private void loadDetails() {
        List<Object> items = new ArrayList<>();

        addDeviceInfo(items);
        addSystemInfo(items);
        addDisplayInfo(items);
        addMemoryInfo(items);
        addStorageInfo(items);
        addBatteryInfo(items);
        addNetworkInfo(items);
        addCameraInfo(items);
        addSimInfo(items);

        RecyclerView rv = findViewById(R.id.recyclerView);
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(new DetailAdapter(items));
    }

    // -------------------------------------------------------------------------
    // 1. Device / Hardware
    // -------------------------------------------------------------------------
    private void addDeviceInfo(List<Object> items) {
        items.add("Device Info");
        items.add(new DeviceDetail("Brand",        cap(Build.BRAND),        "device"));
        items.add(new DeviceDetail("Manufacturer", cap(Build.MANUFACTURER), "device"));
        items.add(new DeviceDetail("Model",        Build.MODEL,             "device"));
        items.add(new DeviceDetail("Device",       Build.DEVICE,            "device"));
        items.add(new DeviceDetail("Product",      Build.PRODUCT,           "device"));
        items.add(new DeviceDetail("Hardware",     Build.HARDWARE,          "device"));
        items.add(new DeviceDetail("Board",        Build.BOARD,             "device"));
    }

    // -------------------------------------------------------------------------
    // 2. Android / System
    // -------------------------------------------------------------------------
    private void addSystemInfo(List<Object> items) {
        items.add("System Info");
        items.add(new DeviceDetail("Android Version", Build.VERSION.RELEASE,              "system"));
        items.add(new DeviceDetail("API Level",       String.valueOf(Build.VERSION.SDK_INT), "system"));
        items.add(new DeviceDetail("Security Patch",
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                        ? Build.VERSION.SECURITY_PATCH : "N/A", "system"));
        items.add(new DeviceDetail("Build ID",        Build.ID,                           "system"));
        items.add(new DeviceDetail("Build Type",      Build.TYPE,                         "system"));
        items.add(new DeviceDetail("Fingerprint",     shortenFingerprint(Build.FINGERPRINT), "system"));
        items.add(new DeviceDetail("CPU ABI",         Build.SUPPORTED_ABIS[0],            "system"));
        items.add(new DeviceDetail("CPU Cores",       String.valueOf(Runtime.getRuntime().availableProcessors()), "system"));
    }

    // -------------------------------------------------------------------------
    // 3. Display
    // -------------------------------------------------------------------------
    private void addDisplayInfo(List<Object> items) {
        items.add("Display Info");
        DisplayMetrics dm = getResources().getDisplayMetrics();
        items.add(new DeviceDetail("Resolution",
                dm.widthPixels + " x " + dm.heightPixels + " px", "display"));
        items.add(new DeviceDetail("Density",
                dm.density + "x  (" + dm.densityDpi + " dpi)", "display"));
        items.add(new DeviceDetail("Screen Width",
                String.format(Locale.US, "%.1f dp", dm.widthPixels / dm.density), "display"));
        items.add(new DeviceDetail("Screen Height",
                String.format(Locale.US, "%.1f dp", dm.heightPixels / dm.density), "display"));
        items.add(new DeviceDetail("Font Scale",
                String.valueOf(dm.scaledDensity / dm.density), "display"));
    }

    // -------------------------------------------------------------------------
    // 4. Memory / RAM
    // -------------------------------------------------------------------------
    private void addMemoryInfo(List<Object> items) {
        items.add("Memory (RAM)");
        ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
        am.getMemoryInfo(mi);

        items.add(new DeviceDetail("Total RAM",
                formatBytes(mi.totalMem), "memory"));
        items.add(new DeviceDetail("Available RAM",
                formatBytes(mi.availMem), "memory"));
        items.add(new DeviceDetail("Used RAM",
                formatBytes(mi.totalMem - mi.availMem), "memory"));
        items.add(new DeviceDetail("Low Memory",
                mi.lowMemory ? "Yes" : "No", "memory"));
        items.add(new DeviceDetail("Low Memory Threshold",
                formatBytes(mi.threshold), "memory"));
    }

    // -------------------------------------------------------------------------
    // 5. Storage
    // -------------------------------------------------------------------------
    private void addStorageInfo(List<Object> items) {
        items.add("Storage Info");

        StatFs internal = new StatFs(Environment.getDataDirectory().getPath());
        long intTotal = internal.getTotalBytes();
        long intFree  = internal.getAvailableBytes();

        items.add(new DeviceDetail("Internal Total", formatBytes(intTotal), "storage"));
        items.add(new DeviceDetail("Internal Free",  formatBytes(intFree),  "storage"));
        items.add(new DeviceDetail("Internal Used",  formatBytes(intTotal - intFree), "storage"));

        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            StatFs ext = new StatFs(Environment.getExternalStorageDirectory().getPath());
            long extTotal = ext.getTotalBytes();
            long extFree  = ext.getAvailableBytes();
            items.add(new DeviceDetail("External Total", formatBytes(extTotal), "storage"));
            items.add(new DeviceDetail("External Free",  formatBytes(extFree),  "storage"));
        } else {
            items.add(new DeviceDetail("External Storage", "Not available", "storage"));
        }
    }

    // -------------------------------------------------------------------------
    // 6. Battery
    // -------------------------------------------------------------------------
    private void addBatteryInfo(List<Object> items) {
        items.add("Battery Info");
        IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent battery = registerReceiver(null, ifilter);
        if (battery == null) return;

        int level   = battery.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale   = battery.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
        int status  = battery.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
        int health  = battery.getIntExtra(BatteryManager.EXTRA_HEALTH, -1);
        int plug    = battery.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
        int temp    = battery.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, -1);
        int voltage = battery.getIntExtra(BatteryManager.EXTRA_VOLTAGE, -1);
        String tech = battery.getStringExtra(BatteryManager.EXTRA_TECHNOLOGY);

        float pct = (scale > 0) ? (level * 100f / scale) : -1;

        items.add(new DeviceDetail("Level",       (pct >= 0 ? String.format(Locale.US, "%.0f%%", pct) : "Unknown"), "battery"));
        items.add(new DeviceDetail("Status",      batteryStatus(status),  "battery"));
        items.add(new DeviceDetail("Health",      batteryHealth(health),  "battery"));
        items.add(new DeviceDetail("Power Source",chargeSource(plug),     "battery"));
        items.add(new DeviceDetail("Temperature", (temp > 0 ? temp / 10.0 + " °C" : "Unknown"), "battery"));
        items.add(new DeviceDetail("Voltage",     (voltage > 0 ? voltage + " mV" : "Unknown"), "battery"));
        items.add(new DeviceDetail("Technology",  (tech != null ? tech : "Unknown"), "battery"));
    }

    // -------------------------------------------------------------------------
    // 7. Network
    // -------------------------------------------------------------------------
    @SuppressWarnings("deprecation")
    private void addNetworkInfo(List<Object> items) {
        items.add("Network Info");
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo active = cm.getActiveNetworkInfo();

        if (active != null && active.isConnected()) {
            items.add(new DeviceDetail("Connected",    "Yes",                       "network"));
            items.add(new DeviceDetail("Network Type", active.getTypeName(),        "network"));
            items.add(new DeviceDetail("Subtype",      active.getSubtypeName(),     "network"));
            items.add(new DeviceDetail("State",        active.getState().toString(),"network"));
        } else {
            items.add(new DeviceDetail("Connected", "No", "network"));
        }

        // Wi-Fi specific
        WifiManager wm = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (wm != null && wm.isWifiEnabled()) {
            WifiInfo wi = wm.getConnectionInfo();
            String ssid = wi.getSSID();
            items.add(new DeviceDetail("Wi-Fi SSID",    ssid,                                      "network"));
            items.add(new DeviceDetail("Wi-Fi IP",      Formatter.formatIpAddress(wi.getIpAddress()), "network"));
            items.add(new DeviceDetail("Wi-Fi Signal",  wi.getRssi() + " dBm",                    "network"));
            items.add(new DeviceDetail("Wi-Fi Speed",   wi.getLinkSpeed() + " Mbps",               "network"));
        }
    }

    // -------------------------------------------------------------------------
    // 8. Camera
    // -------------------------------------------------------------------------
    private void addCameraInfo(List<Object> items) {
        items.add("Camera Info");
        CameraManager cm = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
            String[] ids = cm.getCameraIdList();
            items.add(new DeviceDetail("Total Cameras", String.valueOf(ids.length), "camera"));
            for (String id : ids) {
                CameraCharacteristics ch = cm.getCameraCharacteristics(id);
                Integer facing = ch.get(CameraCharacteristics.LENS_FACING);
                String side = (facing != null && facing == CameraCharacteristics.LENS_FACING_FRONT)
                        ? "Front" : "Back";
                float[] focal = ch.get(CameraCharacteristics.LENS_INFO_AVAILABLE_FOCAL_LENGTHS);
                String focalStr = (focal != null && focal.length > 0)
                        ? String.format(Locale.US, "%.1f mm", focal[0]) : "N/A";

                int[] pixelModes = new int[]{};
                android.util.Size[] sizes = null;
                try {
                    android.hardware.camera2.params.StreamConfigurationMap map =
                            ch.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
                    if (map != null) {
                        sizes = map.getOutputSizes(android.graphics.ImageFormat.JPEG);
                    }
                } catch (Exception ignored) {}

                String maxRes = "N/A";
                if (sizes != null && sizes.length > 0) {
                    android.util.Size max = sizes[0];
                    for (android.util.Size s : sizes) {
                        if (s.getWidth() * s.getHeight() > max.getWidth() * max.getHeight()) {
                            max = s;
                        }
                    }
                    long mp = (long) max.getWidth() * max.getHeight();
                    maxRes = String.format(Locale.US, "%d x %d (~%.1f MP)",
                            max.getWidth(), max.getHeight(), mp / 1_000_000.0);
                }

                items.add(new DeviceDetail("Camera " + id + " (" + side + ") Focal", focalStr,  "camera"));
                items.add(new DeviceDetail("Camera " + id + " (" + side + ") Max Res", maxRes,   "camera"));
            }
        } catch (Exception e) {
            items.add(new DeviceDetail("Camera Info", "Unavailable", "camera"));
        }
    }

    // -------------------------------------------------------------------------
    // 9. SIM / Telephony
    // -------------------------------------------------------------------------
    private void addSimInfo(List<Object> items) {
        items.add("SIM / Telephony");
        TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        if (tm == null) {
            items.add(new DeviceDetail("SIM", "Not available", "sim"));
            return;
        }

        items.add(new DeviceDetail("SIM State",       simState(tm.getSimState()),     "sim"));
        items.add(new DeviceDetail("Operator Name",   safeStr(tm.getNetworkOperatorName()), "sim"));
        items.add(new DeviceDetail("Operator Code",   safeStr(tm.getNetworkOperator()), "sim"));
        items.add(new DeviceDetail("SIM Country",     safeStr(tm.getSimCountryIso()).toUpperCase(), "sim"));
        items.add(new DeviceDetail("Network Country", safeStr(tm.getNetworkCountryIso()).toUpperCase(), "sim"));
        items.add(new DeviceDetail("Phone Type",      phoneType(tm.getPhoneType()), "sim"));
        items.add(new DeviceDetail("Network Type",    networkType(tm.getNetworkType()), "sim"));
        items.add(new DeviceDetail("Dual SIM",
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ? "Check Settings" : "N/A", "sim"));
    }

    // =========================================================================
    // Helpers
    // =========================================================================
    private String formatBytes(long bytes) {
        if (bytes <= 0) return "0 B";
        String[] units = {"B", "KB", "MB", "GB", "TB"};
        int idx = (int) (Math.log(bytes) / Math.log(1024));
        idx = Math.min(idx, units.length - 1);
        return String.format(Locale.US, "%.2f %s", bytes / Math.pow(1024, idx), units[idx]);
    }

    private String cap(String s) {
        if (s == null || s.isEmpty()) return "";
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }

    private String safeStr(String s) {
        return (s != null && !s.isEmpty()) ? s : "N/A";
    }

    private String shortenFingerprint(String fp) {
        if (fp == null) return "N/A";
        return fp.length() > 40 ? fp.substring(0, 40) + "…" : fp;
    }

    private String batteryStatus(int s) {
        switch (s) {
            case BatteryManager.BATTERY_STATUS_CHARGING:     return "Charging";
            case BatteryManager.BATTERY_STATUS_DISCHARGING:  return "Discharging";
            case BatteryManager.BATTERY_STATUS_FULL:         return "Full";
            case BatteryManager.BATTERY_STATUS_NOT_CHARGING: return "Not Charging";
            default: return "Unknown";
        }
    }

    private String batteryHealth(int h) {
        switch (h) {
            case BatteryManager.BATTERY_HEALTH_GOOD:          return "Good";
            case BatteryManager.BATTERY_HEALTH_OVERHEAT:      return "Overheat";
            case BatteryManager.BATTERY_HEALTH_DEAD:          return "Dead";
            case BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE:  return "Over Voltage";
            case BatteryManager.BATTERY_HEALTH_COLD:          return "Cold";
            default: return "Unknown";
        }
    }

    private String chargeSource(int p) {
        switch (p) {
            case BatteryManager.BATTERY_PLUGGED_AC:       return "AC Charger";
            case BatteryManager.BATTERY_PLUGGED_USB:      return "USB";
            case BatteryManager.BATTERY_PLUGGED_WIRELESS: return "Wireless";
            default: return "Battery";
        }
    }

    private String simState(int s) {
        switch (s) {
            case TelephonyManager.SIM_STATE_ABSENT:      return "Absent";
            case TelephonyManager.SIM_STATE_READY:       return "Ready";
            case TelephonyManager.SIM_STATE_PIN_REQUIRED:return "PIN Required";
            case TelephonyManager.SIM_STATE_PUK_REQUIRED:return "PUK Required";
            case TelephonyManager.SIM_STATE_NETWORK_LOCKED: return "Network Locked";
            default: return "Unknown";
        }
    }

    private String phoneType(int t) {
        switch (t) {
            case TelephonyManager.PHONE_TYPE_GSM:  return "GSM";
            case TelephonyManager.PHONE_TYPE_CDMA: return "CDMA";
            case TelephonyManager.PHONE_TYPE_SIP:  return "SIP";
            default: return "None";
        }
    }

    @SuppressWarnings("deprecation")
    private String networkType(int t) {
        switch (t) {
            case TelephonyManager.NETWORK_TYPE_GPRS:    return "GPRS (2G)";
            case TelephonyManager.NETWORK_TYPE_EDGE:    return "EDGE (2G)";
            case TelephonyManager.NETWORK_TYPE_CDMA:    return "CDMA (2G)";
            case TelephonyManager.NETWORK_TYPE_1xRTT:   return "1xRTT (2G)";
            case TelephonyManager.NETWORK_TYPE_UMTS:    return "UMTS (3G)";
            case TelephonyManager.NETWORK_TYPE_EVDO_0:  return "EVDO 0 (3G)";
            case TelephonyManager.NETWORK_TYPE_EVDO_A:  return "EVDO A (3G)";
            case TelephonyManager.NETWORK_TYPE_EVDO_B:  return "EVDO B (3G)";
            case TelephonyManager.NETWORK_TYPE_HSDPA:   return "HSDPA (3G)";
            case TelephonyManager.NETWORK_TYPE_HSUPA:   return "HSUPA (3G)";
            case TelephonyManager.NETWORK_TYPE_HSPA:    return "HSPA (3G)";
            case TelephonyManager.NETWORK_TYPE_HSPAP:   return "HSPA+ (3G)";
            case TelephonyManager.NETWORK_TYPE_LTE:     return "LTE (4G)";
            case TelephonyManager.NETWORK_TYPE_NR:      return "NR (5G)";
            default: return "Unknown";
        }
    }
}
