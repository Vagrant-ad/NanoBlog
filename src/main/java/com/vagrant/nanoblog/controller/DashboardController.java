package com.vagrant.nanoblog.controller;

import com.vagrant.nanoblog.common.ResponseResult;
import com.vagrant.nanoblog.pojo.Article;
import com.vagrant.nanoblog.service.DashboardService;
import com.vagrant.nanoblog.service.ICommentService;
import com.vagrant.nanoblog.vo.CommentManageVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin/dashboard")
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;

    @Autowired
    private ICommentService commentService;

    // 获取核心统计数据
    @GetMapping("/stat")
    public ResponseResult<Map<String, Integer>> getDashboardStat() {
        Map<String, Integer> statMap = dashboardService.getDashboardStat();
        return ResponseResult.okResult(statMap);
    }

    // 获取近期文章列表
    @GetMapping("/recent/articles")
    public ResponseResult<List<Article>> getRecentArticles() {
        List<Article> articles = dashboardService.getRecentArticles(10);
        return ResponseResult.okResult(articles);
    }

    // 获取最近评论列表（用于仪表盘）
    @GetMapping("/recent/comments")
    public ResponseResult<List<CommentManageVO>> getRecentComments(
            @RequestParam(defaultValue = "5") Integer limit) {
        List<CommentManageVO> comments = commentService.getRecentComments(limit);
        return ResponseResult.okResult(comments);
    }

    // 获取近 7 日访问趋势

    @GetMapping("/visit/trend")
    public ResponseResult<Map<String, Object>> getVisitTrend() {
        Map<String, Object> trendMap = dashboardService.getVisitTrend();
        return ResponseResult.okResult(trendMap);
    }
}
