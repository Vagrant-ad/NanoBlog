package com.vagrant.nanoblog.service;

        import java.util.List;
        import java.util.Map;

        import com.sun.javafx.collections.MappingChange;
        import com.vagrant.nanoblog.pojo.Article;

public interface DashboardService {
    // 获取核心统计数据
    Map<String, Integer> getDashboardStat();

    // 获取近期文章列表
    List<Article> getRecentArticles(Integer limit);

    // 获取近 7 日访问趋势
    Map<String, Object> getVisitTrend();

}