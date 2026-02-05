package com.example.yoyo_data.common.document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class HotNewsDetail {
    private int index;
    private String title;
    private String hot;
    private String hotParam;
    private String discussVideoCount;
    private String imageUrl;
    private List<String> imageUrls;
    private String label;
    private String url;
    private String mobileUrl;
}