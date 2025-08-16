package com.example.page;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    // UI组件
    private Button recordButton;
    private Button stopButton;
    private Button playButton;
    private Button saveButton;
    private Button settingsButton;
    private TextView statusTextView;
    private TextView timerTextView;

    // 录音状态
    private boolean isRecording = false;
    private boolean isPlaying = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.recordactivity_main);

        // 初始化UI组件
        initializeViews();

        // 设置按钮点击监听器
        setupButtonListeners();
    }

    private void initializeViews() {
        recordButton = findViewById(R.id.recordButton);
        stopButton = findViewById(R.id.stopButton);
        playButton = findViewById(R.id.playButton);
        saveButton = findViewById(R.id.saveButton);
        settingsButton = findViewById(R.id.settingsButton);
        statusTextView = findViewById(R.id.statusTextView);
        timerTextView = findViewById(R.id.timerTextView);

        // 初始状态设置
        stopButton.setEnabled(false);
        playButton.setEnabled(false);
        saveButton.setEnabled(false);
    }

    private void setupButtonListeners() {
        // 录音按钮点击事件
        recordButton.setOnClickListener(v -> {
            if (!isRecording) {
                startRecording();
            } else {
                pauseRecording();
            }
        });

        // 停止按钮点击事件
        stopButton.setOnClickListener(v -> stopRecording());

        // 播放按钮点击事件
        playButton.setOnClickListener(v -> {
            if (!isPlaying) {
                startPlaying();
            } else {
                pausePlaying();
            }
        });

        // 保存按钮点击事件
        saveButton.setOnClickListener(v -> saveRecording());

        // 设置按钮点击事件
        settingsButton.setOnClickListener(v -> openSettings());
    }

    private void startRecording() {
        isRecording = true;
        recordButton.setText(R.string.pause_recording);
        stopButton.setEnabled(true);
        playButton.setEnabled(false);
        saveButton.setEnabled(false);
        statusTextView.setText(R.string.recording_status);

        // 这里可以添加实际的录音开始逻辑
        // startRecordingLogic();
    }

    private void pauseRecording() {
        isRecording = false;
        recordButton.setText(R.string.resume_recording);
        playButton.setEnabled(true);
        saveButton.setEnabled(true);
        statusTextView.setText(R.string.paused_status);

        // 这里可以添加实际的录音暂停逻辑
        // pauseRecordingLogic();
    }

    private void stopRecording() {
        isRecording = false;
        recordButton.setText(R.string.start_recording);
        stopButton.setEnabled(false);
        playButton.setEnabled(true);
        saveButton.setEnabled(true);
        statusTextView.setText(R.string.ready_status);

        // 这里可以添加实际的录音停止逻辑
        // stopRecordingLogic();
    }

    private void startPlaying() {
        isPlaying = true;
        playButton.setText(R.string.pause_playback);
        recordButton.setEnabled(false);
        statusTextView.setText(R.string.playing_status);

        // 这里可以添加实际的播放开始逻辑
        // startPlayingLogic();
    }

    private void pausePlaying() {
        isPlaying = false;
        playButton.setText(R.string.resume_playback);
        recordButton.setEnabled(true);
        statusTextView.setText(R.string.paused_status);

        // 这里可以添加实际的播放暂停逻辑
        // pausePlayingLogic();
    }

    private void saveRecording() {
        // 这里可以添加实际的保存逻辑
        // saveRecordingLogic();

        // 显示保存成功的提示
        statusTextView.setText(R.string.saved_status);
    }

    private void openSettings() {
        // 这里可以打开设置界面
        // openSettingsActivity();
    }

    // 更新计时器的方法，可以由外部调用
    public void updateTimer(String time) {
        runOnUiThread(() -> timerTextView.setText(time));
    }

    // 更新状态的方法，可以由外部调用
    public void updateStatus(String status) {
        runOnUiThread(() -> statusTextView.setText(status));
    }
}