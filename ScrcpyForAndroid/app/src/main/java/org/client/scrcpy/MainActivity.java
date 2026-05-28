package org.client.scrcpy;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ActivityInfo;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.Surface;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListPopupWindow;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toast;

import org.client.scrcpy.utils.AdbHelper;
import org.client.scrcpy.utils.HttpRequest;
import org.client.scrcpy.utils.PreUtils;
import org.client.scrcpy.utils.Progress;
import org.client.scrcpy.utils.ThreadUtils;
import org.client.scrcpy.utils.Util;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;


public class MainActivity extends Activity implements Scrcpy.ServiceCallbacks, SensorEventListener {

    public final static String START_REMOTE = "start_remote_headless";

    private boolean headlessMode = false;
    private int screenWidth;
    private int screenHeight;
    private boolean landscape = false;
    private boolean first_time = true;
    private boolean result_of_Rotation = false;
    private boolean serviceBound = false;
    private boolean resumeScrcpy = false;
    SensorManager sensorManager;
    private SendCommands sendCommands;
    private int videoBitrate;
    private int delayControl;
    private Context context;
    private String serverAdr = null;
    private SurfaceView surfaceView;
    private Surface surface;
    private Scrcpy scrcpy;
    private long timestamp = 0;

    private LinearLayout linearLayout;

    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            scrcpy = ((Scrcpy.MyServiceBinder) iBinder).getService();
            scrcpy.setServiceCallbacks(MainActivity.this);
            serviceBound = true;
            if (first_time) {
                if (!Progress.isShowing()) {
                    Progress.showDialog(MainActivity.this, getString(R.string.please_wait));
                }
                scrcpy.start(surface, Scrcpy.LOCAL_IP + ":" + Scrcpy.LOCAL_FORWART_PORT,
                        screenHeight, screenWidth, delayControl);
                ThreadUtils.workPost(() -> {
                    boolean success = AdbHelper.executeWithTimeout(() -> {
                        while (!scrcpy.check_socket_connection()) {
                            try {
                                Thread.sleep(10);
                            } catch (InterruptedException e) {
                                break;
                            }
                        }
                    }, SendCommands.WAIT_TIME, TimeUnit.MILLISECONDS);
                    ThreadUtils.post(() -> {
                        Progress.closeDialog();
                        if (!success) {
                            if (serviceBound) {
                                showMainView();
                            }
                            Toast.makeText(context, "Connection Timed out 2", Toast.LENGTH_SHORT).show();
                        } else {
                            first_time = false;
                            set_display_nd_touch();
                            connectSuccessExt();
                        }
                    });
                });
            } else {
                scrcpy.setParms(surface, screenWidth, screenHeight);
                set_display_nd_touch();
                connectSuccessExt();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            serviceBound = false;
        }
    };

    private void showMainView() {
        showMainView(false);
    }

    private void showMainView(boolean userDisconnect) {
        if (scrcpy != null) {
            scrcpy.StopService();
        }
        try {
            unbindService(serviceConnection);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (surface != null) {
            surface = null;
        }
        if (surfaceView != null) {
            surfaceView = null;
        }
        serviceBound = false;
        scrcpy_main();

        if (scrcpy != null) {
            scrcpy = null;
        }
        connectExitExt(userDisconnect);
    }

    @SuppressLint("SourceLockedOrientationActivity")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.context = this;
        if (savedInstanceState != null) {
            first_time = savedInstanceState.getBoolean("first_time");
            landscape = savedInstanceState.getBoolean("landscape");
            headlessMode = savedInstanceState.getBoolean("headlessMode");
            resumeScrcpy = savedInstanceState.getBoolean("resumeScrcpy");
            screenHeight = savedInstanceState.getInt("screenHeight");
            screenWidth = savedInstanceState.getInt("screenWidth");
        }
        landscape = getApplication().getResources().getConfiguration().orientation
                != Configuration.ORIENTATION_PORTRAIT;
        if (first_time) {
            scrcpy_main();
        } else {
            Log.e("Scrcpy: ", "from onCreate");
            start_screen_copy_magic();
        }
        sensorManager = (SensorManager) this.getSystemService(SENSOR_SERVICE);
        Sensor proximity;
        proximity = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
        sensorManager.registerListener(this, proximity, SensorManager.SENSOR_DELAY_NORMAL);

        if (savedInstanceState == null || !savedInstanceState.getBoolean("from_save_instance", false)) {
            if (getIntent() != null && getIntent().getExtras() != null) {
                headlessMode = getIntent().getExtras().getBoolean(START_REMOTE, headlessMode);
            }
        }
        if (headlessMode && first_time) {
            getAttributes();
            connectScrcpyServer(PreUtils.get(this, Constant.CONTROL_REMOTE_ADDR, ""));
        }
        if (headlessMode) {
            View scrollView = findViewById(R.id.main_scroll_view);
            if (scrollView != null) {
                scrollView.setVisibility(View.INVISIBLE);
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("from_save_instance", true);
        outState.putBoolean("first_time", first_time);
        outState.putBoolean("landscape", landscape);
        outState.putBoolean("headlessMode", headlessMode);
        outState.putInt("screenHeight", screenHeight);
        outState.putInt("screenWidth", screenWidth);
    }

    @SuppressLint("SourceLockedOrientationActivity")
    public void scrcpy_main() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            this.getWindow().setStatusBarColor(getColor(R.color.status_bar));
        } else {
            this.getWindow().setStatusBarColor(getResources().getColor(R.color.status_bar));
        }
        final View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(View.VISIBLE);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        landscape = false;
        setContentView(R.layout.activity_main);
        final Button startButton = findViewById(R.id.button_start);

        sendCommands = new SendCommands();

        startButton.setOnClickListener(v -> {
            getAttributes();
            connectScrcpyServer(serverAdr);
        });

        Button settingsButton = findViewById(R.id.button_settings);
        settingsButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
            startActivity(intent);
        });

        get_saved_preferences();

        EditText editText = findViewById(R.id.editText_server_host);

        findViewById(R.id.history_list).setOnClickListener(v -> {
            editText.clearFocus();
            showListPopulWindow(editText);
        });

        if (headlessMode) {
            View scrollView = findViewById(R.id.main_scroll_view);
            if (scrollView != null) {
                scrollView.setVisibility(View.INVISIBLE);
            }
        }
    }

    private void showListPopulWindow(EditText mEditText) {
        String[] list = getHistoryList();
        if (list.length == 0) {
            list = new String[]{"127.0.0.1"};
        }
        final ListPopupWindow listPopupWindow;
        listPopupWindow = new ListPopupWindow(this);
        listPopupWindow.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, list));
        listPopupWindow.setAnchorView(mEditText);
        listPopupWindow.setModal(true);
        listPopupWindow.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

        String[] finalList = list;
        listPopupWindow.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                mEditText.setText(finalList[i]);
                listPopupWindow.dismiss();
            }
        });
        listPopupWindow.show();
    }

    public void get_saved_preferences() {
        final EditText editTextServerHost = findViewById(R.id.editText_server_host);
        final Switch aSwitch0 = findViewById(R.id.switch0);
        final Switch aSwitch1 = findViewById(R.id.switch1);
        String historySpServerAdr = PreUtils.get(context, Constant.CONTROL_REMOTE_ADDR, "");
        if (TextUtils.isEmpty(historySpServerAdr)) {
            String[] historyList = getHistoryList();
            if (historyList.length > 0) {
                editTextServerHost.setText(historyList[0]);
            }
        } else {
            editTextServerHost.setText(historySpServerAdr);
        }
        aSwitch0.setChecked(PreUtils.get(context, Constant.CONTROL_NO, false));
        aSwitch1.setChecked(PreUtils.get(context, Constant.CONTROL_NAV, false));
        setSpinner(R.array.options_resolution_values, R.id.spinner_video_resolution, Constant.PREFERENCE_SPINNER_RESOLUTION);
        setSpinner(R.array.options_bitrate_keys, R.id.spinner_video_bitrate, Constant.PREFERENCE_SPINNER_BITRATE);
        setSpinner(R.array.options_delay_keys, R.id.delay_control_spinner, Constant.PREFERENCE_SPINNER_DELAY);
        if (aSwitch0.isChecked()) {
            aSwitch1.setClickable(false);
            aSwitch1.setTextColor(Color.GRAY);
        }

        aSwitch0.setOnClickListener(v -> {
            if (aSwitch0.isChecked()) {
                aSwitch1.setClickable(false);
                aSwitch1.setTextColor(Color.GRAY);
            } else {
                aSwitch1.setClickable(true);
                aSwitch1.setTextColor(Color.WHITE);
            }
        });
    }

    @SuppressLint("ClickableViewAccessibility")
    public void set_display_nd_touch() {
        DisplayMetrics metrics = new DisplayMetrics();
        if (ViewConfiguration.get(context).hasPermanentMenuKey()) {
            getWindowManager().getDefaultDisplay().getMetrics(metrics);
        } else {
            final Display display = getWindowManager().getDefaultDisplay();
            display.getRealMetrics(metrics);
        }

        float this_dev_height = linearLayout.getHeight();
        float this_dev_width = linearLayout.getWidth();
        if (PreUtils.get(context, Constant.CONTROL_NAV, false) &&
                !PreUtils.get(context, Constant.CONTROL_NO, false)) {
            if (landscape) {
                this_dev_width = this_dev_width - 96;
            } else {
                this_dev_height = this_dev_height - 96;
            }
        }
        int[] rem_res = scrcpy.get_remote_device_resolution();
        int remote_device_height = rem_res[1];
        int remote_device_width = rem_res[0];
        float remote_device_aspect_ratio = (float) remote_device_height / remote_device_width;

        if (!landscape) {
            float this_device_aspect_ratio = this_dev_height / this_dev_width;
            if (remote_device_aspect_ratio > this_device_aspect_ratio) {
                float wantWidth = this_dev_height / remote_device_aspect_ratio;
                int padding = (int) (this_dev_width - wantWidth) / 2;
                linearLayout.setPadding(padding, 0, padding, 0);
            } else if (remote_device_aspect_ratio < this_device_aspect_ratio) {
                linearLayout.setPadding(0, (int) (((this_device_aspect_ratio - remote_device_aspect_ratio) * this_dev_width)), 0, 0);
            }
        } else {
            float this_device_aspect_ratio = this_dev_width / this_dev_height;
            if (remote_device_aspect_ratio > this_device_aspect_ratio) {
                float wantHeight = this_dev_width / remote_device_aspect_ratio;
                int padding = (int) (this_dev_height - wantHeight) / 2;
                linearLayout.setPadding(0, padding, 0, padding);
            } else if (remote_device_aspect_ratio < this_device_aspect_ratio) {
                linearLayout.setPadding(((int) (((this_device_aspect_ratio - remote_device_aspect_ratio) * this_dev_height)) / 2), 0, ((int) (((this_device_aspect_ratio - remote_device_aspect_ratio) * this_dev_height)) / 2), 0);
            }
        }
        if (!PreUtils.get(context, Constant.CONTROL_NO, false)) {
            surfaceView.setOnTouchListener((view, event) -> scrcpy.touchevent(event, landscape, surfaceView.getWidth(), surfaceView.getHeight()));
        }

        if (PreUtils.get(context, Constant.CONTROL_NAV, false) &&
                !PreUtils.get(context, Constant.CONTROL_NO, false)) {
            final View backButton = findViewById(R.id.back_button);
            final View homeButton = findViewById(R.id.home_button);
            final View appswitchButton = findViewById(R.id.appswitch_button);

            if (backButton != null) {
                backButton.setOnClickListener(v -> scrcpy.sendKeyevent(KeyEvent.KEYCODE_BACK));
            }
            if (homeButton != null) {
                homeButton.setOnClickListener(v -> scrcpy.sendKeyevent(KeyEvent.KEYCODE_HOME));
            }
            if (appswitchButton != null) {
                appswitchButton.setOnClickListener(v -> scrcpy.sendKeyevent(KeyEvent.KEYCODE_APP_SWITCH));
            }
        }
    }

    private void setSpinner(final int textArrayOptionResId, final int textViewResId, final String preferenceId) {
        final Spinner spinner = findViewById(textViewResId);
        ArrayAdapter<CharSequence> arrayAdapter = ArrayAdapter.createFromResource(this, textArrayOptionResId, android.R.layout.simple_spinner_item);
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(arrayAdapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                PreUtils.put(context, preferenceId, position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                PreUtils.put(context, preferenceId, 0);
            }
        });
        int selection = PreUtils.get(context, preferenceId, 0);
        if (selection < arrayAdapter.getCount()) {
            spinner.setSelection(selection);
        } else {
            spinner.setSelection(0);
        }
    }

    private void getAttributes() {
        final EditText editTextServerHost = findViewById(R.id.editText_server_host);
        serverAdr = editTextServerHost.getText().toString();
        if (!TextUtils.isEmpty(serverAdr)) {
            serverAdr = serverAdr.trim();
        }
        if (!TextUtils.isEmpty(serverAdr)) {
            PreUtils.put(context, Constant.CONTROL_REMOTE_ADDR, serverAdr);
        }
        final Spinner videoResolutionSpinner = findViewById(R.id.spinner_video_resolution);
        final Spinner videoBitrateSpinner = findViewById(R.id.spinner_video_bitrate);
        final Spinner delayControlSpinner = findViewById(R.id.delay_control_spinner);
        final Switch a_Switch0 = findViewById(R.id.switch0);
        boolean no_control = a_Switch0.isChecked();
        final Switch a_Switch1 = findViewById(R.id.switch1);
        boolean nav = a_Switch1.isChecked();
        PreUtils.put(context, Constant.CONTROL_NO, no_control);
        PreUtils.put(context, Constant.CONTROL_NAV, nav);

        final String[] videoResolutions = getResources().getStringArray(R.array.options_resolution_values)[videoResolutionSpinner.getSelectedItemPosition()].split("x");
        screenHeight = Integer.parseInt(videoResolutions[0]);
        screenWidth = Integer.parseInt(videoResolutions[1]);
        videoBitrate = getResources().getIntArray(R.array.options_bitrate_values)[videoBitrateSpinner.getSelectedItemPosition()];
        delayControl = getResources().getIntArray(R.array.options_delay_values)[delayControlSpinner.getSelectedItemPosition()];
    }

    private String[] getHistoryList() {
        String historyList = PreUtils.get(context, Constant.HISTORY_LIST_KEY, "");
        if (TextUtils.isEmpty(historyList)) {
            return new String[]{};
        }
        try {
            JSONArray historyJson = new JSONArray(historyList);
            String[] retList = new String[historyJson.length()];
            for (int i = 0; i < historyJson.length(); i++) {
                retList[i] = historyJson.get(i).toString();
            }
            return retList;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new String[]{};
    }

    private boolean saveHistory(String device) {
        if (headlessMode) {
            return false;
        }
        JSONArray historyJson = new JSONArray();
        String[] historyList = getHistoryList();
        if (historyList.length == 0) {
            historyJson.put(device);
        } else {
            try {
                historyJson.put(0, device);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            int count = Math.min(historyList.length, 30);
            for (int i = 0; i < count; i++) {
                if (!historyList[i].equals(device)) {
                    historyJson.put(historyList[i]);
                }
            }
        }
        try {
            return PreUtils.put(context, Constant.HISTORY_LIST_KEY, historyJson.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private void swapDimensions() {
        int temp = screenHeight;
        screenHeight = screenWidth;
        screenWidth = temp;
    }

    @SuppressLint("ClickableViewAccessibility")
    private void start_screen_copy_magic() {
        setContentView(R.layout.surface);
        final View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        surfaceView = findViewById(R.id.decoder_surface);
        surface = surfaceView.getHolder().getSurface();
        final LinearLayout nav_bar = findViewById(R.id.nav_button_bar);
        if (PreUtils.get(context, Constant.CONTROL_NAV, false) &&
                !PreUtils.get(context, Constant.CONTROL_NO, false)) {
            nav_bar.setVisibility(LinearLayout.VISIBLE);
        } else {
            nav_bar.setVisibility(LinearLayout.GONE);
        }
        linearLayout = findViewById(R.id.container1);
        start_Scrcpy_service();
    }

    private void start_Scrcpy_service() {
        Intent intent = new Intent(this, Scrcpy.class);
        startService(intent);
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    @SuppressLint("SourceLockedOrientationActivity")
    @Override
    public void loadNewRotation() {
        if (first_time) {
            first_time = false;
        }
        try {
            unbindService(serviceConnection);
        } catch (Exception e) {
            e.printStackTrace();
        }
        serviceBound = false;
        result_of_Rotation = true;
        landscape = !landscape;
        swapDimensions();
        if (landscape) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
        }
    }

    @Override
    public void errorDisconnect() {
        Dialog.displayDialog(this, getString(R.string.disconnect),
                getString(R.string.disconnect_ask), () -> {
                    if (serviceBound) {
                        showMainView();
                        first_time = true;
                    } else {
                        MainActivity.this.finish();
                    }
                }, false);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (resumeScrcpy) {
            showMainView(true);
            first_time = true;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d("Scrcpy", "onStart: " + serviceBound);
        if (resumeScrcpy) {
            if (!serviceBound) {
                resumeScrcpy = false;
                connectScrcpyServer(PreUtils.get(context, Constant.CONTROL_REMOTE_ADDR, ""));
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d("Scrcpy", "onPause: " + serviceBound);
        if (serviceBound && scrcpy != null) {
            scrcpy.pause();
            resumeScrcpy = true;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!first_time && !result_of_Rotation) {
            final View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
            if (serviceBound) {
                linearLayout = findViewById(R.id.container1);
                scrcpy.resume();
            }
        }
        if (resumeScrcpy && !result_of_Rotation && scrcpy != null) {
            scrcpy.resume();
        }
        resumeScrcpy = false;
        result_of_Rotation = false;
    }

    @Override
    public void onBackPressed() {
        if (timestamp == 0) {
            if (serviceBound) {
                timestamp = SystemClock.uptimeMillis();
                Toast.makeText(context, "Press again to exit", Toast.LENGTH_SHORT).show();
            } else {
                finish();
            }
        } else {
            long now = SystemClock.uptimeMillis();
            if (now < timestamp + 1000) {
                timestamp = 0;
                if (serviceBound) {
                    showMainView(true);
                    first_time = true;
                } else {
                    finish();
                }
            }
            timestamp = 0;
        }
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if (sensorEvent.sensor.getType() == Sensor.TYPE_PROXIMITY) {
            if (sensorEvent.values[0] == 0) {
                if (serviceBound) {
                }
            } else {
                if (serviceBound) {
                }
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
    }

    private void connectScrcpyServer(String serverAdr) {
        if (!TextUtils.isEmpty(serverAdr)) {
            saveHistory(serverAdr);
            String[] serverInfo = Util.getServerHostAndPort(serverAdr);
            String serverHost = serverInfo[0];
            int serverPort = Integer.parseInt(serverInfo[1]);
            int localForwardPort = Scrcpy.LOCAL_FORWART_PORT;

            ScrcpyOptions options = buildScrcpyOptions();

            Progress.showDialog(MainActivity.this, getString(R.string.please_wait));
            ThreadUtils.workPost(() -> {
                AdbHelper.writeAssetsJarServer(App.mContext);
                SendCommands.CmdStatus sendStatus = sendCommands.executeAdbCommands(context, serverHost,
                        serverPort,
                        localForwardPort,
                        Scrcpy.LOCAL_IP,
                        videoBitrate, Math.max(screenHeight, screenWidth), options);
                if (sendStatus == SendCommands.CmdStatus.SUCCESS) {
                    ThreadUtils.post(() -> {
                        if (!MainActivity.this.isFinishing()) {
                            Log.e("Scrcpy: ", "from startButton");
                            start_screen_copy_magic();
                        }
                    });
                } else {
                    ThreadUtils.post(Progress::closeDialog);
                    Toast.makeText(context, "Network OR ADB connection failed", Toast.LENGTH_SHORT).show();
                    connectExitExt();
                }
            });
        } else {
            Toast.makeText(context, "Server Address Empty", Toast.LENGTH_SHORT).show();
            connectExitExt();
        }
    }

    private ScrcpyOptions buildScrcpyOptions() {
        ScrcpyOptions options = new ScrcpyOptions();

        String[] videoCodecs = getResources().getStringArray(R.array.options_video_codec);
        int codecIndex = PreUtils.get(context, Constant.PREF_VIDEO_CODEC, 0);
        if (codecIndex >= 0 && codecIndex < videoCodecs.length) {
            options.videoCodec = videoCodecs[codecIndex];
        }

        String[] fpsValues = getResources().getStringArray(R.array.options_fps_values);
        int fpsIndex = PreUtils.get(context, Constant.PREF_VIDEO_FPS, 0);
        if (fpsIndex >= 0 && fpsIndex < fpsValues.length) {
            options.videoFps = Integer.parseInt(fpsValues[fpsIndex]);
        }

        String[] audioCodecs = getResources().getStringArray(R.array.options_audio_codec);
        int audioCodecIndex = PreUtils.get(context, Constant.PREF_AUDIO_CODEC, 0);
        if (audioCodecIndex >= 0 && audioCodecIndex < audioCodecs.length) {
            options.audioCodec = audioCodecs[audioCodecIndex];
        }

        String[] audioBitrates = getResources().getStringArray(R.array.options_audio_bitrate);
        int audioBitrateIndex = PreUtils.get(context, Constant.PREF_AUDIO_BITRATE, 0);
        if (audioBitrateIndex >= 0 && audioBitrateIndex < audioBitrates.length) {
            String bitrateStr = audioBitrates[audioBitrateIndex].split(" ")[0];
            options.audioBitrate = Integer.parseInt(bitrateStr) * 1000;
        }

        String[] displays = getResources().getStringArray(R.array.options_display);
        int displayIndex = PreUtils.get(context, Constant.PREF_DISPLAY, 0);
        if (displayIndex > 0 && displayIndex < displays.length) {
            options.displayId = Integer.parseInt(displays[displayIndex]);
        }

        String[] rotations = getResources().getStringArray(R.array.options_rotation);
        int rotationIndex = PreUtils.get(context, Constant.PREF_ROTATION, 0);
        if (rotationIndex >= 0 && rotationIndex < rotations.length - 1) {
            options.rotation = Integer.parseInt(rotations[rotationIndex]);
        }

        options.showTouches = PreUtils.get(context, Constant.PREF_SHOW_TOUCHES, false);
        options.stayAwake = PreUtils.get(context, Constant.PREF_STAY_AWAKE, false);
        options.noVideo = PreUtils.get(context, Constant.PREF_NO_VIDEO, false);
        options.noAudio = !PreUtils.get(context, Constant.PREF_AUDIO_ENABLE, true);
        options.turnScreenOff = PreUtils.get(context, Constant.PREF_TURN_SCREEN_OFF, false);
        options.turnScreenOffOnClose = PreUtils.get(context, Constant.PREF_TURN_SCREEN_OFF_ON_CLOSE, false);
        options.customArgs = PreUtils.get(context, Constant.PREF_CUSTOM_ARGS, "");

        if (PreUtils.get(context, Constant.PREF_RECORD_ENABLE, false)) {
            String recordPath = PreUtils.get(context, Constant.PREF_RECORD_PATH, "/sdcard/scrcpy/");
            String timestamp = String.valueOf(System.currentTimeMillis());
            String[] recordFormats = getResources().getStringArray(R.array.options_record_format);
            int formatIndex = PreUtils.get(context, Constant.PREF_RECORD_FORMAT, 0);
            String ext = "mkv";
            if (formatIndex >= 0 && formatIndex < recordFormats.length) {
                ext = recordFormats[formatIndex];
            }
            options.recordPath = recordPath + "scrcpy_" + timestamp + "." + ext;
        }

        return options;
    }

    protected void connectSuccessExt() {
        Dialog.closeDialogs();
    }

    protected void connectExitExt() {
        this.connectExitExt(false);
    }

    protected void connectExitExt(boolean userDisconnect) {
        if (!userDisconnect) {
        }
        if (headlessMode && !resumeScrcpy && !result_of_Rotation) {
            if (!userDisconnect) {
                Dialog.displayDialog(this, getString(R.string.connect_faild),
                        getString(R.string.connect_faild_ask), () -> {
                            connectScrcpyServer(PreUtils.get(context, Constant.CONTROL_REMOTE_ADDR, ""));
                        }, () -> {
                            finishAndRemoveTask();
                        });
            } else {
                finishAndRemoveTask();
            }
        }
    }
}