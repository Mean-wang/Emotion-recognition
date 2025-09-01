package Model;

import android.media.MediaRecorder;
import android.os.Environment;
import java.io.File;
import java.io.IOException;
import okhttp3.*;//okhttp要在built.gradle.kts文件下implementation("com.squareup.okhttp3:okhttp:5.1.0")
public class AudioRecorder {
    private MediaRecorder mediaRecorder;
    private String currentFilePath;

    public void startRecording() throws IOException {
        String fileName = "audio_" + System.currentTimeMillis() + ".mp4";
        File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC);
        File outputFile = new File(storageDir, fileName);
        currentFilePath = outputFile.getAbsolutePath();

        mediaRecorder = new MediaRecorder();
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        mediaRecorder.setOutputFile(currentFilePath);
        mediaRecorder.prepare();
        mediaRecorder.start();
    }

    public void stopRecording() {
        if (mediaRecorder != null) {
            mediaRecorder.stop();
            mediaRecorder.release();
            mediaRecorder = null;
        }
    }

    public void uploadAudioToServer(String serverUrl, String userId, UploadCallback callback) {
        File audioFile = new File(currentFilePath);
        if (!audioFile.exists()) {
            callback.onFailure("Audio file not found");
            return;
        }

        OkHttpClient client = new OkHttpClient();

        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("userId", userId)
                .addFormDataPart("audio", audioFile.getName(),
                        RequestBody.create(MediaType.parse("audio/mp4"), audioFile))
                .build();

        Request request = new Request.Builder()
                .url(serverUrl)
                .post(requestBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.onFailure(e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    callback.onSuccess(response.body().string());
                } else {
                    callback.onFailure("Server error: " + response.code());
                }
            }
        });
    }

    public interface UploadCallback {
        void onSuccess(String response);
        void onFailure(String error);
    }
}

