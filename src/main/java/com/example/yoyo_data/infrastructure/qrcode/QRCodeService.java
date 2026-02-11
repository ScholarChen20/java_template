package com.example.yoyo_data.infrastructure.qrcode;

import com.example.yoyo_data.infrastructure.config.properties.MinIOProperties;
import io.minio.MinioClient;
import io.minio.PutObjectOptions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.UUID;

/**
 * 二维码生成服务
 * 使用第三方API生成二维码并上传到MinIO
 *
 * @author YoYo Data Team
 * @version 1.0
 * @since 2026-02-10
 */
@Slf4j
@Service
public class QRCodeService {

    @Autowired
    private MinioClient minioClient;

    @Autowired
    private MinIOProperties minIOProperties;

    @Autowired
    private RestTemplate restTemplate;

    /**
     * 二维码生成API地址
     * 参数说明：
     * - text: 二维码内容
     * - size: 二维码尺寸（像素）
     * - margin: 二维码边距（像素）
     * - level: 纠错级别（L=7%, M=15%, Q=25%, H=30%）
     */
    private static final String QR_CODE_API = "https://api.qrtool.cn/";

    /**
     * 二维码解码API地址
     */
    private static final String QR_CODE_DECODE_API = "https://v2.xxapi.cn/api/deqrcode";

    /**
     * MinIO中二维码文件的存储目录
     */
    private static final String QR_CODE_BUCKET_PATH = "qrcodes/orders/";

    /**
     * 生成订单二维码并上传到MinIO
     *
     * @param orderNo 订单号（作为二维码内容）
     * @return 二维码在MinIO中的URL
     */
    public String generateOrderQRCode(String orderNo) {
        try {
            log.info("【二维码生成】开始生成订单二维码: orderNo={}", orderNo);

            // 1. 调用第三方API生成二维码
            byte[] qrCodeBytes = generateQRCodeImage(orderNo);

            if (qrCodeBytes == null || qrCodeBytes.length == 0) {
                log.error("【二维码生成失败】API返回空数据: orderNo={}", orderNo);
                return null;
            }

            log.info("【二维码生成成功】大小={}字节, orderNo={}", qrCodeBytes.length, orderNo);

            // 2. 上传二维码到MinIO
            String qrCodeUrl = uploadQRCodeToMinIO(orderNo, qrCodeBytes);

            log.info("【二维码上传成功】orderNo={}, url={}", orderNo, qrCodeUrl);

            return qrCodeUrl;

        } catch (Exception e) {
            log.error("【二维码生成异常】orderNo={}, error={}", orderNo, e.getMessage(), e);
            return null;
        }
    }

    /**
     * 调用第三方API生成二维码图片
     *
     * @param content 二维码内容
     * @return 二维码图片字节数组
     */
    private byte[] generateQRCodeImage(String content) {
        try {
            // 构建请求URL
            String apiUrl = String.format("%s?text=%s&size=%d&margin=%d&level=%s",
                    QR_CODE_API,
                    content,  // 二维码内容：订单号
                    500,      // 尺寸：500x500像素
                    20,       // 边距：20像素
                    "H"       // 纠错级别：H（最高，30%）
            );

            log.debug("【二维码API】请求URL: {}", apiUrl);

            // 调用API获取二维码图片
            byte[] qrCodeBytes = restTemplate.getForObject(apiUrl, byte[].class);

            return qrCodeBytes;

        } catch (Exception e) {
            log.error("【二维码API调用失败】content={}, error={}", content, e.getMessage(), e);
            return null;
        }
    }

    /**
     * 上传二维码到MinIO
     *
     * @param orderNo 订单号
     * @param qrCodeBytes 二维码图片字节数组
     * @return MinIO中的文件URL
     */
    private String uploadQRCodeToMinIO(String orderNo, byte[] qrCodeBytes) {
        try {
            // 1. 生成文件名
            String fileName = String.format("%s%s_%s.png",
                    QR_CODE_BUCKET_PATH,
                    orderNo,
                    UUID.randomUUID().toString().substring(0, 8)
            );

            // 2. 上传到MinIO
            String bucketName = minIOProperties.getBucketName();

            try (InputStream inputStream = new ByteArrayInputStream(qrCodeBytes)) {
                PutObjectOptions options = new PutObjectOptions(qrCodeBytes.length, -1);
                options.setContentType("image/png");

                minioClient.putObject(
                        bucketName,
                        fileName,
                        inputStream,
                        options
                );
            }

            // 3. 生成永久访问URL（不使用预签名URL，直接返回对象路径）
            // 格式：http://minio-endpoint/bucket-name/object-name
            String fileUrl = String.format("%s/%s/%s",
                    minIOProperties.getEndpoint(),
                    bucketName,
                    fileName
            );

            log.debug("【MinIO上传】bucketName={}, fileName={}, fileUrl={}",
                    bucketName, fileName, fileUrl);

            return fileUrl;

        } catch (Exception e) {
            log.error("【MinIO上传失败】orderNo={}, error={}", orderNo, e.getMessage(), e);
            return null;
        }
    }

    /**
     * 解码二维码（从URL获取二维码内容）
     *
     * @param qrCodeUrl 二维码图片URL
     * @return 二维码内容
     */
    public String decodeQRCode(String qrCodeUrl) {
        try {
            log.info("【二维码解码】开始解码: url={}", qrCodeUrl);

            // 构建请求URL
            String apiUrl = String.format("%s?url=%s", QR_CODE_DECODE_API, qrCodeUrl);

            // 调用API解码
            String response = restTemplate.getForObject(apiUrl, String.class);

            log.info("【二维码解码成功】url={}, content={}", qrCodeUrl, response);

            return response;

        } catch (Exception e) {
            log.error("【二维码解码失败】url={}, error={}", qrCodeUrl, e.getMessage(), e);
            return null;
        }
    }

    /**
     * 验证订单二维码是否有效
     *
     * @param qrCodeUrl 二维码URL
     * @param expectedOrderNo 期望的订单号
     * @return 是否有效
     */
    public boolean validateOrderQRCode(String qrCodeUrl, String expectedOrderNo) {
        try {
            String decodedContent = decodeQRCode(qrCodeUrl);

            if (decodedContent == null || decodedContent.isEmpty()) {
                log.warn("【二维码验证】解码失败: url={}", qrCodeUrl);
                return false;
            }

            boolean isValid = expectedOrderNo.equals(decodedContent);

            log.info("【二维码验证】url={}, expectedOrderNo={}, decodedContent={}, isValid={}",
                    qrCodeUrl, expectedOrderNo, decodedContent, isValid);

            return isValid;

        } catch (Exception e) {
            log.error("【二维码验证异常】url={}, error={}", qrCodeUrl, e.getMessage(), e);
            return false;
        }
    }
}
