package com.vagrant.nanoblog.service.impl;

        import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
        import com.vagrant.nanoblog.mapper.ArticleMapper;
        import com.vagrant.nanoblog.mapper.CommentMapper;
        import com.vagrant.nanoblog.mapper.UserMapper;
        import com.vagrant.nanoblog.pojo.Article;
        import com.vagrant.nanoblog.pojo.Comment;
        import com.vagrant.nanoblog.pojo.User;
        import com.vagrant.nanoblog.service.DashboardService;
        import org.springframework.beans.factory.annotation.Autowired;
        import org.springframework.stereotype.Service;

        import java.time.LocalDate;
        import java.time.LocalDateTime;
        import java.time.format.DateTimeFormatter;
        import java.util.*;

@Service
public class DashboardServiceImpl implements DashboardService {

    @Autowired
    private ArticleMapper articleMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private CommentMapper commentMapper;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Override
    public Map<String, Integer> getDashboardStat() {
        Map<String, Integer> statMap = new HashMap<>();

        // 统计文章总数
        QueryWrapper<Article> articleWrapper = new QueryWrapper<>();
        articleWrapper.eq("is_deleted", 0);
        Integer articleCount = articleMapper.selectCount(articleWrapper);
        statMap.put("articleCount", articleCount);

        // 统计用户总数
        QueryWrapper<User> userWrapper = new QueryWrapper<>();
        Integer userCount = userMapper.selectCount(userWrapper);
        statMap.put("userCount", userCount);

        // 统计评论总数
        QueryWrapper<Comment> commentWrapper = new QueryWrapper<>();
        Integer commentCount = commentMapper.selectCount(commentWrapper);
        statMap.put("commentCount", commentCount);

        // 统计总浏览量
        QueryWrapper<Article> viewWrapper = new QueryWrapper<>();
        viewWrapper.eq("is_deleted", 0);
        List<Article> allArticles = articleMapper.selectList(viewWrapper);
        Long totalViews = allArticles.stream()
                .mapToLong(Article::getViewCount)
                .sum();
        statMap.put("totalViews", Math.toIntExact(totalViews));

        return statMap;
    }

    @Override
    public List<Article> getRecentArticles(Integer limit) {
        QueryWrapper<Article> wrapper = new QueryWrapper<>();
        wrapper.eq("is_deleted", 0)
                .eq("status", 1)
                .orderByDesc("publish_time")
                .last("LIMIT " + limit);
        return articleMapper.selectList(wrapper);
    }

    @Override
    public Map<String, Object> getVisitTrend() {
        Map<String, Object> trendMap = new HashMap<>();
        List<Map<String, Object>> trendData = new ArrayList<>();

        // 获取近 7 天的日期
        for (int i = 6; i >= 0; i--) {
            LocalDate date = LocalDate.now().minusDays(i);
            String dateStr = date.format(DATE_FORMATTER);
            LocalDateTime startOfDay = date.atStartOfDay();
            LocalDateTime endOfDay = date.atTime(23, 59, 59);

            // 查询当天的文章浏览量
            QueryWrapper<Article> wrapper = new QueryWrapper<>();
            wrapper.eq("is_deleted", 0)
                    .between("publish_time", startOfDay, endOfDay);
            List<Article> articles = articleMapper.selectList(wrapper);

            // 计算当天的总浏览量
            long dailyViews = articles.stream()
                    .mapToLong(Article::getViewCount)
                    .sum();

            Map<String, Object> dayData = new HashMap<>();
            dayData.put("date", dateStr);
            dayData.put("views", dailyViews);
            trendData.add(dayData);
        }

        trendMap.put("trend", trendData);
        return trendMap;
    }
}