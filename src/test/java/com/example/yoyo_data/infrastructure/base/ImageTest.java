package com.example.yoyo_data.infrastructure.base;

import okhttp3.*;
import org.json.JSONObject;
import java.util.concurrent.TimeUnit;

import java.io.*;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;
import java.net.URLEncoder;

public class ImageTest {

    /**
     * 需要添加依赖
     * <!-- https://mvnrepository.com/artifact/com.squareup.okhttp3/okhttp -->
     * <dependency>
     *     <groupId>com.squareup.okhttp3</groupId>
     *     <artifactId>okhttp</artifactId>
     *     <version>4.12.0</version>
     * </dependency>
     */

    public static final OkHttpClient HTTP_CLIENT = new OkHttpClient().newBuilder().readTimeout(300, TimeUnit.SECONDS).build();
    public static void main(String []args) throws IOException{
            MediaType mediaType = MediaType.parse("application/x-www-form-urlencoded");
            // image 可以通过 getFileContentAsBase64("C:\fakepath\1(1).png") 方法获取,如果Content-Type是application/x-www-form-urlencoded时,第二个参数传true
            RequestBody body = RequestBody.create(mediaType, "url=https%3A%2F%2Fts3.tc.mm.bing.net%2Fth%2Fid%2FOIP-C.R86aJ5pc3Fu1oGj9boecuAAAAA%3Frs%3D1%26pid%3DImgDetMain%26o%3D7%26rm%3D3");
            Request request = new Request.Builder()
                    .url("https://aip.baidubce.com/rest/2.0/image-classify/v2/advanced_general")
                    .method("POST", body)
                    .addHeader("Content-Type", "application/x-www-form-urlencoded")
                    .addHeader("Accept", "application/json")
                    .addHeader("Authorization", "Bearer bce-v3/ALTAK-jW0E3OBOJEoPQRchN4naV/504cea4dd653e79b479f32a1afe76ecedfc4fe4a")
                    .build();
            Response response = HTTP_CLIENT.newCall(request).execute();
            System.out.println(response.body().string());

        }

        /**
         * 获取文件base64编码
         *
         * @param path      文件路径
         * @param urlEncode 如果Content-Type是application/x-www-form-urlencoded时,传true
         * @return base64编码信息，不带文件头
         * @throws IOException IO异常
         */
        static String getFileContentAsBase64(String path, boolean urlEncode) throws IOException {
            byte[] b = Files.readAllBytes(Paths.get(path));
            String base64 = Base64.getEncoder().encodeToString(b);
            if (urlEncode) {
                base64 = URLEncoder.encode(base64, "utf-8");
            }
            return base64;
        }
}
