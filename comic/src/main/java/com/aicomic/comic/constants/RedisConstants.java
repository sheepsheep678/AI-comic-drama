package com.aicomic.comic.constants;

/**
 * @since 2022/7/17 17:20
 * @version 1.0.0
 **/
public class RedisConstants {

    //一级二级分类拥有的三级分类的数量
    public static final String REDIS_KEY_CATEGORY_THIRD_NUMBER = "CATEGORY:THIRD_NUMBER";

    public static class Formatter {
        public static final String STATISTICS_QUIZINFO = "COMIC:TAG:ANSWER_PROCESS_#{quizDetailInfoDTO.recordId}";
        public static final String STATISTICS_COMIC_NUM_CATE = "COMIC:COMIC_NUM_CATEGORY";
        public static final String CATEGORY_ID_LIST_HAVE_COMIC = "COMIC:CATEGORY_ID_WITH_COMIC";
    }
}
