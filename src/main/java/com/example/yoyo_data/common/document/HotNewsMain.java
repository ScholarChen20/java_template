package com.example.yoyo_data.common.document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import com.example.yoyo_data.common.document.HotNewsDetail;
import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Document(collection = "hot_news_main")
public class HotNewsMain {
    @Id
    private String id;
    private boolean success;
    private String title;
    private String subtitle;
    private String updateTime;
    private List<HotNewsDetail> data;
    private LocalDateTime createdAt;
    private String type; // 如：douyinhot
}