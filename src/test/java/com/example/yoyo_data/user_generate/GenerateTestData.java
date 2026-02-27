package com.example.yoyo_data.user_generate;

import com.alibaba.fastjson.JSON;
import com.example.yoyo_data.common.dto.request.LoginRequest;
import com.example.yoyo_data.common.dto.request.QuickGrabRequest;
import com.example.yoyo_data.common.dto.request.RegisterRequest;
import com.example.yoyo_data.common.dto.response.LoginResponse;
import com.example.yoyo_data.common.Result;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;
import cn.hutool.core.util.RandomUtil;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 模拟用户生成
 * 生成用户数据、token数据、抢票请求数据
 */
public class GenerateTestData {

    private static final String BASE_URL = "http://localhost:8080/api";
    private static final String USERS_FILE = "D:\\JavaPro\\yoyo_data\\src\\main\\resources\\jmeter-test\\test_user.txt";
    private static final String TOKENS_FILE = "D:\\JavaPro\\yoyo_data\\src\\main\\resources\\jmeter-test\\test_tokens.txt";
    private static final String REQUESTS_FILE = "D:\\JavaPro\\yoyo_data\\src\\main\\resources\\jmeter-test\\test_requests.txt";
    
    // 线程池配置
    private static final int CORE_POOL_SIZE = 50;
    private static final int MAX_POOL_SIZE = 100;
    private static final long KEEP_ALIVE_TIME = 60;
    private static final int BATCH_SIZE = 100;
    
    // HTTP客户端连接池
    private static final PoolingHttpClientConnectionManager connectionManager;
    private static final CloseableHttpClient httpClient;
    
    static {
        // 初始化HTTP连接池
        connectionManager = new PoolingHttpClientConnectionManager();
        connectionManager.setMaxTotal(200);
        connectionManager.setDefaultMaxPerRoute(100);
        httpClient = HttpClients.custom()
                .setConnectionManager(connectionManager)
                .build();
    }

    public static void main(String[] args) throws Exception {
        long startTime = System.currentTimeMillis();
        
        // 创建目录
        java.io.File directory = new java.io.File("D:\\JavaPro\\yoyo_data\\src\\main\\resources\\jmeter-test");
        if (!directory.exists()) {
            directory.mkdirs();
        }
        
        // 生成测试用户
        generateTestUsers(5000);
        // 登录获取token
        loginUsers();
        // 生成抢票请求数据
        generateGrabRequests();
        
        long endTime = System.currentTimeMillis();
        System.out.println("测试数据生成完成！耗时: " + (endTime - startTime) / 1000 + "秒");
        
        // 关闭HTTP客户端
        httpClient.close();
        connectionManager.shutdown();
    }

    /**
     * 生成测试用户
     */
    private static void generateTestUsers(int count) throws Exception {
        ExecutorService executorService = new ThreadPoolExecutor(
                CORE_POOL_SIZE,
                MAX_POOL_SIZE,
                KEEP_ALIVE_TIME,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(10000),
                new ThreadPoolExecutor.CallerRunsPolicy()
        );
        
        final AtomicInteger successCount = new AtomicInteger(0);
        
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(USERS_FILE))) {
            // 批量提交任务
            for (int i = 1; i <= count; i++) {
                final int index = i;
                executorService.submit(() -> {
                    try {
                        String username = "test_user_" + index;
                        String password = "123456**";
                        String email = "test_" + index + "@example.com";
                        String phone = "138" + String.format("%08d", index);
                        
                        RegisterRequest request = new RegisterRequest();
                        request.setUsername(username);
                        request.setPassword(password);
                        request.setEmail(email);
                        request.setPhone(phone);
                        
                        // 注册用户
                        boolean success = registerUser(request);
                        if (success) {
                            synchronized (writer) {
                                writer.write(username + "," + password + "\n");
                            }
                            int current = successCount.incrementAndGet();
                            if (current % 100 == 0) {
                                System.out.println("已生成 " + current + " 个用户");
                            }
                        }
                    } catch (Exception e) {
                        System.err.println("注册用户失败: " + e.getMessage());
                    }
                });
                
                // 每批次提交后稍作休息
                if (i % BATCH_SIZE == 0) {
                    Thread.sleep(100);
                }
            }
            
            // 等待所有任务完成
            executorService.shutdown();
            while (!executorService.awaitTermination(1, TimeUnit.MINUTES)) {
                System.out.println("等待用户注册完成... 当前进度: " + successCount.get() + "/" + count);
            }
            
            System.out.println("用户注册完成: " + successCount.get() + "/" + count);
        } finally {
            executorService.shutdownNow();
        }
    }

    /**
     * 注册用户
     */
    private static boolean registerUser(RegisterRequest request) throws Exception {
        HttpPost post = new HttpPost(BASE_URL + "/auth/register");
        post.setHeader("Content-Type", "application/json");
        
        String json = JSON.toJSONString(request);
        post.setEntity(new StringEntity(json));
        
        try (CloseableHttpResponse response = httpClient.execute(post)) {
            String result = EntityUtils.toString(response.getEntity());
            // 简单检查注册是否成功
            return result.contains("success");
        } finally {
            post.releaseConnection();
        }
    }

    /**
     * 登录用户获取token
     */
    private static void loginUsers() throws Exception {
        ExecutorService executorService = new ThreadPoolExecutor(
                CORE_POOL_SIZE,
                MAX_POOL_SIZE,
                KEEP_ALIVE_TIME,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(10000),
                new ThreadPoolExecutor.CallerRunsPolicy()
        );
        
        final AtomicInteger successCount = new AtomicInteger(0);
        final List<String> userLines = new ArrayList<>();
        
        // 先读取所有用户数据
        java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.FileReader(USERS_FILE));
        String line;
        while ((line = reader.readLine()) != null) {
            userLines.add(line);
        }
        reader.close();
        
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(TOKENS_FILE))) {
            // 批量提交登录任务
            for (String userLine : userLines) {
                final String lineCopy = userLine;
                executorService.submit(() -> {
                    try {
                        String[] parts = lineCopy.split(",");
                        if (parts.length != 2) return;
                        
                        String username = parts[0];
                        String password = parts[1];
                        
                        LoginRequest request = new LoginRequest();
                        request.setUsername(username);
                        request.setPassword(password);
                        
                        String token = loginUser(request);
                        if (token != null) {
                            synchronized (writer) {
                                writer.write(username + "," + token + "\n");
                            }
                            int current = successCount.incrementAndGet();
                            if (current % 100 == 0) {
                                System.out.println("已获取 " + current + " 个token");
                            }
                        }
                    } catch (Exception e) {
                        System.err.println("登录失败: " + e.getMessage());
                    }
                });
                
                // 每批次提交后稍作休息
                if (successCount.get() % BATCH_SIZE == 0) {
                    Thread.sleep(100);
                }
            }
            
            // 等待所有任务完成
            executorService.shutdown();
            while (!executorService.awaitTermination(1, TimeUnit.MINUTES)) {
                System.out.println("等待登录完成... 当前进度: " + successCount.get() + "/" + userLines.size());
            }
            
            System.out.println("登录完成: " + successCount.get() + "/" + userLines.size());
        } finally {
            executorService.shutdownNow();
        }
    }

    /**
     * 登录用户获取token
     */
    private static String loginUser(LoginRequest request) throws Exception {
        HttpPost post = new HttpPost(BASE_URL + "/auth/login");
        post.setHeader("Content-Type", "application/json");
        
        String json = JSON.toJSONString(request);
        post.setEntity(new StringEntity(json));
        
        try (CloseableHttpResponse response = httpClient.execute(post)) {
            String result = EntityUtils.toString(response.getEntity());
            Result<LoginResponse> loginResult = JSON.parseObject(result, Result.class);
            
            if (loginResult.getCode() == 200 && loginResult.getData() != null) {
                LoginResponse loginResponse = JSON.parseObject(JSON.toJSONString(loginResult.getData()), LoginResponse.class);
                return loginResponse.getToken();
            }
            return null;
        } finally {
            post.releaseConnection();
        }
    }

    /**
     * 生成抢票请求数据
     */
    private static void generateGrabRequests() throws Exception {
        ExecutorService executorService = new ThreadPoolExecutor(
                CORE_POOL_SIZE,
                MAX_POOL_SIZE,
                KEEP_ALIVE_TIME,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(10000),
                new ThreadPoolExecutor.CallerRunsPolicy()
        );
        
        final AtomicInteger count = new AtomicInteger(0);
        final List<String> requests = new ArrayList<>(10000);
        
        // 生成10000个抢票请求
        for (int i = 1; i <= 10000; i++) {
            final int index = i;
            executorService.submit(() -> {
                try {
                    QuickGrabRequest request = new QuickGrabRequest();
                    request.setShowEventId(1L); // 假设演出ID为1
                    
                    // 随机选择座位区域
                    String[] zones = {"VIP", "A", "B", "C"};
                    String zone = zones[RandomUtil.randomInt(0, zones.length)];
                    request.setSeatZone(zone);
                    
                    // 随机选择购票数量（1-4）
                    int seatCount = RandomUtil.randomInt(1, 5);
                    request.setSeatCount(seatCount);
                    
                    // 随机选择是否优先连座
                    request.setPreferContinuous(RandomUtil.randomBoolean());
                    
                    // 生成观影人列表
                    List<QuickGrabRequest.TicketUser> ticketUsers = new ArrayList<>();
                    for (int j = 0; j < seatCount; j++) {
                        QuickGrabRequest.TicketUser user = new QuickGrabRequest.TicketUser();
                        user.setContactName("测试用户" + index + "-" + (j + 1));
                        user.setContactPhone("138" + String.format("%08d", index * 10 + j));
                        user.setContactIdCard(generateIdCard());
                        ticketUsers.add(user);
                    }
                    request.setTicketUsers(ticketUsers);
                    
                    synchronized (requests) {
                        requests.add(JSON.toJSONString(request));
                    }
                    
                    int current = count.incrementAndGet();
                    if (current % 1000 == 0) {
                        System.out.println("已生成 " + current + " 个抢票请求");
                    }
                } catch (Exception e) {
                    System.err.println("生成抢票请求失败: " + e.getMessage());
                }
            });
        }
        
        // 等待所有任务完成
        executorService.shutdown();
        while (!executorService.awaitTermination(1, TimeUnit.MINUTES)) {
            System.out.println("等待抢票请求生成完成... 当前进度: " + count.get() + "/10000");
        }
        
        // 写入文件
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(REQUESTS_FILE))) {
            for (String request : requests) {
                writer.write(request + "\n");
            }
        }
        
        System.out.println("抢票请求生成完成: " + requests.size() + "/10000");
        
        executorService.shutdownNow();
    }

    /**
     * 生成随机身份证号
     */
    private static String generateIdCard() {
        // 简单生成身份证号，实际测试中可以使用更真实的生成方法
        String prefix = "1101011990";
        String middle = String.format("%04d", RandomUtil.randomInt(1000, 9999));
        String suffix = String.valueOf(RandomUtil.randomInt(0, 10));
        return prefix + middle + suffix;
    }
}