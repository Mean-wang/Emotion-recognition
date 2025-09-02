//上传工具，将资源上传
package Model;

import androidx.annotation.NonNull;

import java.io.File;
import java.io.IOException;

import okhttp3.*;

public class UploadResource {
    private String currentFilePath;
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
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                callback.onFailure(e.getMessage());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
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