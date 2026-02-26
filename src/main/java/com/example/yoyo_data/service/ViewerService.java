package com.example.yoyo_data.service;

import com.example.yoyo_data.common.Result;
import com.example.yoyo_data.common.dto.request.ContactRequest;
import com.example.yoyo_data.common.dto.request.UpdateViewerRequest;
import com.example.yoyo_data.common.vo.ViewerVO;

import java.util.List;

/**
 * 观演人服务接口
 */
public interface ViewerService {

    Result<List<ViewerVO>> getViewerList(String token);

    Result<ViewerVO> addViewer(ContactRequest req, String token);

    Result<String> updateViewer(Long viewerId, UpdateViewerRequest req, String token);

    Result<String> deleteViewer(Long viewerId, String token);

    Result<String> setDefaultViewer(Long viewerId, String token);
}
