//录音工作与上传分开
package Model;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.media.MediaRecorder;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;

/**
 * 后台录音服务，使用前台服务保证在后台持续运行
 * 功能：
 * 1. 定时检查是否需要开始录音
 * 2. 录音开始/停止控制
 * 3. 录音文件管理
 * 4. 通过通知栏显示状态
 */
public class RecordingService extends Service {

    // 录音相关变量
    private MediaRecorder mediaRecorder; // 媒体录制器实例
    private String currentFilePath;     // 当前录音文件路径
    private PowerManager.WakeLock wakeLock; // 唤醒锁，防止CPU休眠

    // 通知相关常量
    private static final int NOTIFICATION_ID = 123; // 通知ID
    private static final String CHANNEL_ID = "recording_channel"; // 通知渠道ID

    // 定时任务处理
    private Handler handler = new Handler(); // 用于定时任务的Handler
    private Runnable recordingRunnable;      // 定时执行的任务

    // 录音参数配置
    private static final int RECORD_DURATION = 30 * 60 * 1000; // 每次录音时长30分钟
    private static final int CHECK_INTERVAL = 5 * 60 * 1000;   // 每5分钟检查一次

    /**
     * 服务创建时调用，初始化资源
     */
    @Override
    public void onCreate() {
        super.onCreate();

        // 创建通知渠道（Android 8.0+要求）
        createNotificationChannel();

        // 启动前台服务，必须显示通知
        startForeground(NOTIFICATION_ID, buildNotification("录音服务运行中"));

        // 获取唤醒锁，防止录音过程中CPU休眠
        acquireWakeLock();

        Log.d("RecordingService", "录音服务已创建");
    }

    /**
     * 服务启动时调用，开始定时录音任务
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // 开始定时录音任务
        startScheduledRecording();

        // 返回START_STICKY表示服务被系统杀掉后会自动重启
        return START_STICKY;
    }

    private void startScheduledRecording() {
        recordingRunnable = new Runnable() {
            @Override
            public void run() {
                // 获取当前时间
                Calendar now = Calendar.getInstance();
                int hour = now.get(Calendar.HOUR_OF_DAY); // 当前小时(0-23)
                int minute = now.get(Calendar.MINUTE);    // 当前分钟

                Log.d("RecordingService", "检查录音时间，当前时间: " + hour + ":" + minute);

                // 检查是否在指定的录音时间段（上午9点或下午3点）
                    startRecording();

                    // 30分钟后停止录音
                    handler.postDelayed(() -> {
                        stopRecording();
                        Log.d("RecordingService", "定时录音结束");
                    }, RECORD_DURATION);
                }

                // 每5分钟检查一次（避免精确时间错过）
                handler.postDelayed(this, CHECK_INTERVAL);
            }
        };

        // 立即开始检查
        handler.post(recordingRunnable);
    }

    /**
     * 开始录音
     */
    private void startRecording() {
        try {
            // 创建录音文件
            String fileName = "recording_" + System.currentTimeMillis() + ".mp3";
            File outputDir = getExternalFilesDir(Environment.DIRECTORY_MUSIC);
            File outputFile = new File(outputDir, fileName);
            currentFilePath = outputFile.getAbsolutePath();

            Log.d("RecordingService", "录音文件路径: " + currentFilePath);

            // 初始化MediaRecorder
            mediaRecorder = new MediaRecorder();
            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC); // 设置音频源为麦克风
            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4); // 输出格式为MP4
            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC); // 音频编码为AAC
            mediaRecorder.setOutputFile(currentFilePath); // 设置输出文件路径

            // 准备并开始录音
            mediaRecorder.prepare();
            mediaRecorder.start();

            // 更新通知显示录音状态
            updateNotification("正在录音...");

            Log.d("RecordingService", "录音已开始");

        } catch (IOException e) {
            Log.e("RecordingService", "录音初始化失败", e);
            updateNotification("录音初始化失败");
        } catch (IllegalStateException e) {
            Log.e("RecordingService", "录音状态异常", e);
            updateNotification("录音状态异常");
        }
    }

    /**
     * 停止录音
     */
    private void stopRecording() {
        if (mediaRecorder != null) {
            try {
                // 停止并释放MediaRecorder
                mediaRecorder.stop();
                mediaRecorder.release();
                mediaRecorder = null;

                Log.d("RecordingService", "录音已停止");

                // 上传录音文件
                uploadRecording(currentFilePath);

                // 更新通知
                updateNotification("录音完成，等待下次任务");

            } catch (IllegalStateException e) {
                Log.e("RecordingService", "停止录音失败", e);
                updateNotification("停止录音失败");
            }
        }
    }

    /**
     * 上传录音文件（需自行实现）
     * @param filePath 录音文件路径
     */
    private void uploadRecording(String filePath) {
        // TODO: 实现文件上传逻辑
        // 可以使用OkHttp、Retrofit等网络库
        Log.d("RecordingService", "开始上传录音文件: " + filePath);
    }

    /**
     * 创建通知渠道（Android 8.0+要求）
     */
    private void createNotificationChannel() {
        // Android 8.0及以上版本需要创建通知渠道
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "录音服务通知",
                    NotificationManager.IMPORTANCE_LOW // 低重要性，不会发出声音
            );
            channel.setDescription("后台录音服务的状态通知");

            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);

            Log.d("RecordingService", "通知渠道已创建");
        }
    }

    /**
     * 构建通知
     * @param text 通知内容
     * @return 构建好的Notification对象
     */
    private Notification buildNotification(String text) {
        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("后台录音服务") // 通知标题
                .setContentText(text)       // 通知内容
                .setPriority(NotificationCompat.PRIORITY_LOW) // 低优先级
                .build();
    }

    /**
     * 更新通知内容
     * @param text 新的通知内容
     */
    private void updateNotification(String text) {
        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        manager.notify(NOTIFICATION_ID, buildNotification(text));
    }

    /**
     * 获取唤醒锁，防止录音过程中CPU休眠
     */
    private void acquireWakeLock() {
        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(
                PowerManager.PARTIAL_WAKE_LOCK, // 部分唤醒锁，保持CPU运行但屏幕可关闭
                "RecordingService::WakeLock"    // 唤醒锁标签
        );
        wakeLock.acquire(RECORD_DURATION); // 获取唤醒锁，持续时间与录音时长相同
        Log.d("RecordingService", "唤醒锁已获取");
    }

    /**
     * 释放唤醒锁
     */
    private void releaseWakeLock() {
        if (wakeLock != null && wakeLock.isHeld()) {
            wakeLock.release();
            wakeLock = null;
            Log.d("RecordingService", "唤醒锁已释放");
        }
    }

    /**
     * 服务销毁时清理资源
     */
    @Override
    public void onDestroy() {
        super.onDestroy();

        // 停止所有定时任务
        if (handler != null && recordingRunnable != null) {
            handler.removeCallbacks(recordingRunnable);
        }

        // 停止录音
        stopRecording();

        // 释放唤醒锁
        releaseWakeLock();

        Log.d("RecordingService", "录音服务已销毁");
    }

    /**
     * 绑定服务时调用（本例不需要绑定服务）
     */
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
