package com.aicomic.comic.constants;

/**
 * @since 2022/7/14 13:44
 * @version 1.0.0 1.0
 **/
public class ComicConstants {

    public static final long CATEGORY_ROOT = 0;

    public class ComicStep {
        public static final int BASE_INFO = 1; //基本信息
        public static final int EPISODE = 2; //剧集目录
        public static final int MEDIA = 3; //视频
        public static final int TAG = 4; //标签
        public static final int CREATOR = 5; //创作者
    }

    //剧集类型
    public class EpisodeType{
        public static final int CHAPTER = 1; //章
        public static final int CLIP = 2; //片段
        public static final int PRATICE = 3; //练习或测试
    }

}
