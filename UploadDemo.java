package com.qcloud;

public class UploadDemo {
         public static void main(String[] args) {
                String secretId = "你的Secret ID";
                String secretKey = "你的Secret Key";
                String videoPath = "./Wildlife.wmv";
                String coverPath = "./Wildlife-cover.png";
                // 仅上传视频
                //VodApi.upload(secretId, secretKey, videoPath);
                // 同时上传视频和封面
                VodApi.upload(secretId, secretKey, videoPath, coverPath);
         }
}
