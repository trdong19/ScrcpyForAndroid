package org.client.scrcpy;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toast;

import org.client.scrcpy.utils.PreUtils;

public class SettingsActivity extends Activity {

    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        this.context = this;

        initVideoSettings();
        initAudioSettings();
        initDeviceSettings();
        initInputSettings();
        initRecordSettings();

        Button saveButton = findViewById(R.id.btn_save_settings);
        saveButton.setOnClickListener(v -> {
            saveSettings();
            Toast.makeText(context, "Settings saved", Toast.LENGTH_SHORT).show();
            finish();
        });

        Button resetButton = findViewById(R.id.btn_reset_settings);
        resetButton.setOnClickListener(v -> {
            resetSettings();
            loadSettings();
            Toast.makeText(context, "Settings reset", Toast.LENGTH_SHORT).show();
        });
    }

    private void initVideoSettings() {
        Spinner resolutionSpinner = findViewById(R.id.spinner_video_resolution);
        ArrayAdapter<CharSequence> resolutionAdapter = ArrayAdapter.createFromResource(this,
                R.array.options_resolution_values, android.R.layout.simple_spinner_item);
        resolutionAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        resolutionSpinner.setAdapter(resolutionAdapter);

        Spinner bitrateSpinner = findViewById(R.id.spinner_video_bitrate);
        ArrayAdapter<CharSequence> bitrateAdapter = ArrayAdapter.createFromResource(this,
                R.array.options_bitrate_keys, android.R.layout.simple_spinner_item);
        bitrateAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        bitrateSpinner.setAdapter(bitrateAdapter);

        Spinner fpsSpinner = findViewById(R.id.spinner_fps);
        ArrayAdapter<CharSequence> fpsAdapter = ArrayAdapter.createFromResource(this,
                R.array.options_fps_values, android.R.layout.simple_spinner_item);
        fpsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        fpsSpinner.setAdapter(fpsAdapter);

        Spinner codecSpinner = findViewById(R.id.spinner_video_codec);
        ArrayAdapter<CharSequence> codecAdapter = ArrayAdapter.createFromResource(this,
                R.array.options_video_codec, android.R.layout.simple_spinner_item);
        codecAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        codecSpinner.setAdapter(codecAdapter);
    }

    private void initAudioSettings() {
        Spinner audioCodecSpinner = findViewById(R.id.spinner_audio_codec);
        ArrayAdapter<CharSequence> audioCodecAdapter = ArrayAdapter.createFromResource(this,
                R.array.options_audio_codec, android.R.layout.simple_spinner_item);
        audioCodecAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        audioCodecSpinner.setAdapter(audioCodecAdapter);

        Spinner audioBitrateSpinner = findViewById(R.id.spinner_audio_bitrate);
        ArrayAdapter<CharSequence> audioBitrateAdapter = ArrayAdapter.createFromResource(this,
                R.array.options_audio_bitrate, android.R.layout.simple_spinner_item);
        audioBitrateAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        audioBitrateSpinner.setAdapter(audioBitrateAdapter);
    }

    private void initDeviceSettings() {
        Spinner displaySpinner = findViewById(R.id.spinner_display);
        ArrayAdapter<CharSequence> displayAdapter = ArrayAdapter.createFromResource(this,
                R.array.options_display, android.R.layout.simple_spinner_item);
        displayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        displaySpinner.setAdapter(displayAdapter);

        Spinner rotationSpinner = findViewById(R.id.spinner_rotation);
        ArrayAdapter<CharSequence> rotationAdapter = ArrayAdapter.createFromResource(this,
                R.array.options_rotation, android.R.layout.simple_spinner_item);
        rotationAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        rotationSpinner.setAdapter(rotationAdapter);
    }

    private void initInputSettings() {
        Spinner keyboardInjectSpinner = findViewById(R.id.spinner_keyboard_inject);
        ArrayAdapter<CharSequence> keyboardAdapter = ArrayAdapter.createFromResource(this,
                R.array.options_keyboard_inject, android.R.layout.simple_spinner_item);
        keyboardAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        keyboardInjectSpinner.setAdapter(keyboardAdapter);
    }

    private void initRecordSettings() {
        Spinner recordFormatSpinner = findViewById(R.id.spinner_record_format);
        ArrayAdapter<CharSequence> recordFormatAdapter = ArrayAdapter.createFromResource(this,
                R.array.options_record_format, android.R.layout.simple_spinner_item);
        recordFormatAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        recordFormatSpinner.setAdapter(recordFormatAdapter);
    }

    private void saveSettings() {
        Spinner resolutionSpinner = findViewById(R.id.spinner_video_resolution);
        PreUtils.put(context, Constant.PREF_VIDEO_RESOLUTION, resolutionSpinner.getSelectedItemPosition());

        Spinner bitrateSpinner = findViewById(R.id.spinner_video_bitrate);
        PreUtils.put(context, Constant.PREF_VIDEO_BITRATE, bitrateSpinner.getSelectedItemPosition());

        Spinner fpsSpinner = findViewById(R.id.spinner_fps);
        PreUtils.put(context, Constant.PREF_VIDEO_FPS, fpsSpinner.getSelectedItemPosition());

        Spinner codecSpinner = findViewById(R.id.spinner_video_codec);
        PreUtils.put(context, Constant.PREF_VIDEO_CODEC, codecSpinner.getSelectedItemPosition());

        Switch noVideoSwitch = findViewById(R.id.switch_no_video);
        PreUtils.put(context, Constant.PREF_NO_VIDEO, noVideoSwitch.isChecked());

        Switch audioSwitch = findViewById(R.id.switch_audio);
        PreUtils.put(context, Constant.PREF_AUDIO_ENABLE, audioSwitch.isChecked());

        Spinner audioCodecSpinner = findViewById(R.id.spinner_audio_codec);
        PreUtils.put(context, Constant.PREF_AUDIO_CODEC, audioCodecSpinner.getSelectedItemPosition());

        Spinner audioBitrateSpinner = findViewById(R.id.spinner_audio_bitrate);
        PreUtils.put(context, Constant.PREF_AUDIO_BITRATE, audioBitrateSpinner.getSelectedItemPosition());

        Spinner displaySpinner = findViewById(R.id.spinner_display);
        PreUtils.put(context, Constant.PREF_DISPLAY, displaySpinner.getSelectedItemPosition());

        Spinner rotationSpinner = findViewById(R.id.spinner_rotation);
        PreUtils.put(context, Constant.PREF_ROTATION, rotationSpinner.getSelectedItemPosition());

        Switch showTouchesSwitch = findViewById(R.id.switch_show_touches);
        PreUtils.put(context, Constant.PREF_SHOW_TOUCHES, showTouchesSwitch.isChecked());

        Switch stayAwakeSwitch = findViewById(R.id.switch_stay_awake);
        PreUtils.put(context, Constant.PREF_STAY_AWAKE, stayAwakeSwitch.isChecked());

        Spinner keyboardInjectSpinner = findViewById(R.id.spinner_keyboard_inject);
        PreUtils.put(context, Constant.PREF_KEYBOARD_INJECT, keyboardInjectSpinner.getSelectedItemPosition());

        Switch controlSwitch = findViewById(R.id.switch_control);
        PreUtils.put(context, Constant.PREF_CONTROL_ENABLE, controlSwitch.isChecked());

        Switch recordSwitch = findViewById(R.id.switch_record);
        PreUtils.put(context, Constant.PREF_RECORD_ENABLE, recordSwitch.isChecked());

        Spinner recordFormatSpinner = findViewById(R.id.spinner_record_format);
        PreUtils.put(context, Constant.PREF_RECORD_FORMAT, recordFormatSpinner.getSelectedItemPosition());

        EditText recordPathEdit = findViewById(R.id.edit_record_path);
        PreUtils.put(context, Constant.PREF_RECORD_PATH, recordPathEdit.getText().toString());
    }

    private void loadSettings() {
        Spinner resolutionSpinner = findViewById(R.id.spinner_video_resolution);
        resolutionSpinner.setSelection(PreUtils.get(context, Constant.PREF_VIDEO_RESOLUTION, 0));

        Spinner bitrateSpinner = findViewById(R.id.spinner_video_bitrate);
        bitrateSpinner.setSelection(PreUtils.get(context, Constant.PREF_VIDEO_BITRATE, 0));

        Spinner fpsSpinner = findViewById(R.id.spinner_fps);
        fpsSpinner.setSelection(PreUtils.get(context, Constant.PREF_VIDEO_FPS, 0));

        Spinner codecSpinner = findViewById(R.id.spinner_video_codec);
        codecSpinner.setSelection(PreUtils.get(context, Constant.PREF_VIDEO_CODEC, 0));

        Switch noVideoSwitch = findViewById(R.id.switch_no_video);
        noVideoSwitch.setChecked(PreUtils.get(context, Constant.PREF_NO_VIDEO, false));

        Switch audioSwitch = findViewById(R.id.switch_audio);
        audioSwitch.setChecked(PreUtils.get(context, Constant.PREF_AUDIO_ENABLE, true));

        Spinner audioCodecSpinner = findViewById(R.id.spinner_audio_codec);
        audioCodecSpinner.setSelection(PreUtils.get(context, Constant.PREF_AUDIO_CODEC, 0));

        Spinner audioBitrateSpinner = findViewById(R.id.spinner_audio_bitrate);
        audioBitrateSpinner.setSelection(PreUtils.get(context, Constant.PREF_AUDIO_BITRATE, 0));

        Spinner displaySpinner = findViewById(R.id.spinner_display);
        displaySpinner.setSelection(PreUtils.get(context, Constant.PREF_DISPLAY, 0));

        Spinner rotationSpinner = findViewById(R.id.spinner_rotation);
        rotationSpinner.setSelection(PreUtils.get(context, Constant.PREF_ROTATION, 0));

        Switch showTouchesSwitch = findViewById(R.id.switch_show_touches);
        showTouchesSwitch.setChecked(PreUtils.get(context, Constant.PREF_SHOW_TOUCHES, false));

        Switch stayAwakeSwitch = findViewById(R.id.switch_stay_awake);
        stayAwakeSwitch.setChecked(PreUtils.get(context, Constant.PREF_STAY_AWAKE, false));

        Spinner keyboardInjectSpinner = findViewById(R.id.spinner_keyboard_inject);
        keyboardInjectSpinner.setSelection(PreUtils.get(context, Constant.PREF_KEYBOARD_INJECT, 0));

        Switch controlSwitch = findViewById(R.id.switch_control);
        controlSwitch.setChecked(PreUtils.get(context, Constant.PREF_CONTROL_ENABLE, true));

        Switch recordSwitch = findViewById(R.id.switch_record);
        recordSwitch.setChecked(PreUtils.get(context, Constant.PREF_RECORD_ENABLE, false));

        Spinner recordFormatSpinner = findViewById(R.id.spinner_record_format);
        recordFormatSpinner.setSelection(PreUtils.get(context, Constant.PREF_RECORD_FORMAT, 0));

        EditText recordPathEdit = findViewById(R.id.edit_record_path);
        recordPathEdit.setText(PreUtils.get(context, Constant.PREF_RECORD_PATH, "/sdcard/scrcpy/"));
    }

    private void resetSettings() {
        PreUtils.clearAll(context);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadSettings();
    }
}