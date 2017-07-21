package com.qcloud;
import java.util.TreeMap;

import com.qcloud.QcloudApiModuleCenter;
import com.qcloud.Module.Vod;
import com.qcloud.Utilities.Json.JSONObject;

import com.qcloud.cos.*;
import com.qcloud.cos.common_utils.CommonFileUtils;
import com.qcloud.cos.meta.FileAuthority;
import com.qcloud.cos.meta.InsertOnly;
import com.qcloud.cos.request.CreateFolderRequest;
import com.qcloud.cos.request.DelFileRequest;
import com.qcloud.cos.request.DelFolderRequest;
import com.qcloud.cos.request.GetFileLocalRequest;
import com.qcloud.cos.request.ListFolderRequest;
import com.qcloud.cos.request.MoveFileRequest;
import com.qcloud.cos.request.StatFileRequest;
import com.qcloud.cos.request.StatFolderRequest;
import com.qcloud.cos.request.UpdateFileRequest;
import com.qcloud.cos.request.UpdateFolderRequest;
import com.qcloud.cos.request.UploadFileRequest;
import com.qcloud.cos.sign.Credentials;

public class VodApi {
        public static void upload(String secretId, String secretKey, String videoPath) {
            upload(secretId, secretKey, videoPath, null);
        }

        public static void upload(String secretId, String secretKey, String videoPath, String coverPath) {
                TreeMap<String, Object> config = new TreeMap<String, Object>();
                config.put("SecretId", secretId);
                config.put("SecretKey", secretKey);
                config.put("RequestMethod", "GET");
                QcloudApiModuleCenter module = new QcloudApiModuleCenter(new Vod(),
                                config);

                // 第一步，发起上传
                TreeMap<String, Object> params = new TreeMap<String, Object>();
                String[] videoPathSplit = videoPath.split("\\.");
                params.put("videoType", videoPathSplit[videoPathSplit.length - 1]);
                if(coverPath != null) {
                        String[] coverPathSplit = coverPath.split("\\.");
                        params.put("coverType", coverPathSplit[coverPathSplit.length - 1]);
                }
                String result = null;
                try {
                        result = module.call("ApplyUpload", params);
                } catch (Exception e) {
                        System.out.println("error..." + e.getMessage());
                }

                JSONObject json_result = new JSONObject(result);
                System.out.print("ApplyUpload|recv:");
                System.out.println(json_result);

                String bucket = json_result.getString("storageBucket");
                String region = json_result.getString("storageRegion");
                String vodSessionKey = json_result.getString("vodSessionKey");
                String videoDst = json_result.getJSONObject("video").getString("storagePath");
                String coverDst = null;
                if(coverPath != null) {
                        coverDst = json_result.getJSONObject("cover").getString("storagePath");
                }

                // 第二步，上传文件到COS
                long appId = 10022853;
                ClientConfig clientConfig = new ClientConfig();
                clientConfig.setRegion(region);
                clientConfig.setSignExpired(24 * 3600);
                Credentials cred = new Credentials(appId, secretId, secretKey);
                COSClient cosClient = new COSClient(clientConfig, cred);

        	UploadFileRequest uploadFileRequest = new UploadFileRequest(bucket, videoDst, videoPath);
                uploadFileRequest.setInsertOnly(InsertOnly.OVER_WRITE);
                String uploadFileRet = cosClient.uploadFile(uploadFileRequest);
                System.out.print("upload video to cos|recv:");
                System.out.println(uploadFileRet);

                if(coverDst != null) {
        		uploadFileRequest = new UploadFileRequest(bucket, coverDst, coverPath);
                        uploadFileRequest.setInsertOnly(InsertOnly.OVER_WRITE);
                        uploadFileRet = cosClient.uploadFile(uploadFileRequest);
                        System.out.print("upload cover to cos|recv:");
                        System.out.println(uploadFileRet);
                }

                // 第三步，确认上传
                params = new TreeMap<String, Object>();
                params.put("vodSessionKey", vodSessionKey);
                result = null;
                try {
                        result = module.call("CommitUpload", params);
                } catch (Exception e) {
                        System.out.println("error..." + e.getMessage());
                }

                json_result = new JSONObject(result);
                System.out.print("CommitUpload|recv:");
                System.out.println(json_result);
        }
}
