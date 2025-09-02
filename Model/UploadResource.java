package Model;
import androidx.annotation.NonNull;
import java.io.File;
import java.io.IOException;
import okhttp3.*;
public class UploadResource {
    private String currentFilePath;
    public void uploadAudioToServer(String serverUrl, String userId, UploadCallback callback,String currentFilePath) {
        this.currentFilePath = currentFilePath;
        File audioFile = new File(currentFilePath);
        //检查文件时候存在
        if (!audioFile.exists()) {
            callback.onFailure("Audio file not found");
            return;
        }
        //创建OkHttp客户端
        OkHttpClient client = new OkHttpClient();
        //构建多部分表单请求体
        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("userId", userId)
                .addFormDataPart("audio", audioFile.getName(),
                        RequestBody.create(MediaType.parse("audio/mp4"), audioFile))
                .build();
        //构建请求对象
        Request request = new Request.Builder()
                .url(serverUrl)
                .post(requestBody)
                .build();
        //发起异步网络请求
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                callback.onFailure(e.getMessage());//接口实现callback处理成功逻辑
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    new File(currentFilePath).delete();//上传成功就将文件删除。
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