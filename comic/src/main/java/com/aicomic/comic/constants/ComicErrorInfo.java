package com.aicomic.comic.constants;

/**
 * @since 2022/7/14 13:46
 * @version 1.0.0
 **/
public class ComicErrorInfo {

    //错误信息
    public class Msg {
        public static final String CATEGORY_PARENT_NOT_FOUND = "漫剧分类父分类没有找到";
        public static final String CATEGORY_CREATE_ON_THIRD = "三级分类下不能再创建子分类";
        public static final String CATEGORY_SAME_NAME = "分类名称不能重复！";
        public static final String CATEGORY_NOT_FOUND = "该分类未找到";
        public static final String CATEGORY_HAVE_CHILD = "该漫剧有子分类，不能删除";
        public static final String CATEGORY_ADD_NAME_NOT_NULL = "分类名称不能为空！";
        public static final String CATEGORY_ADD_NAME_SIZE = "名称不能超过15个字符";
        public static final String CATEGORY_ADD_INDEX_MAX_MIN = "分类序号格式错误，请重新输入！";
        public static final String CATEGORY_ADD_INDEX_NOT_NULL = "分类序号不能为空！";
        public static final String CATEGORY_ADD_OVER_THIRD_LEVEL = "漫剧分类不支持三级以上的分类";
        public static final String CATEGORY_ID_NOT_NULL = "未选中漫剧分类";
        public static final String CATEGORY_DISABLE_ENABLE_STATUS_ENUM = "只有禁用或启用两种状态";
        public static final String CATEGORY_UPDATE_NAME_NOT_NULL = "分类名称不能为空！";
        public static final String CATEGORY_UPDATE_NAME_SIZE = "名称不能超过15个字符";
        public static final String CATEGORY_UPDATE_INDEX_MAX_MIN = "分类序号格式错误，请重新输入！";
        public static final String CATEGORY_UPDATE_INDEX_NOT_NULL = "分类序号不能为空！";
        public static final String CATEGORY_DELETE_HAVE_TAG = "该分类下含有题目，无法删除";
        public static final String CATEGORY_DELETE_HAVE_COMIC = "该分类下含有漫剧，无法删除";
        public static final String CATEGORY_QUERY_ID_NULL = "查询目录信息，id为空";
        public static final String CATEGORY_DELETE_FAILD = "漫剧分类删除失败";
        public static final String CATEGORY_ENABLE_CANNOT = "当前上级分类为禁用状态，无法启用";

        public static final String TAG_NAME_EXEISTS = "该题目已存在";
        public static final String TAG_NO_DELETE_BY_USED = "当前题目已被引用，无法删除";
        public static final String TAG_SAVE_CATEGORY_INCOMPLETE = "漫剧分类不完整";

        public static final String COMIC_SAVE_FAILD = "漫剧基本信息保存失败";
        public static final String COMIC_SAVE_CATEGORY_NULL = "漫剧分类为空，请选择漫剧分类";
        public static final String COMIC_SAVE_CATEGORY_NOT_FOUND = "漫剧分类未找到";
        public static final String COMIC_CATEGORY_NOT_FOUND = "漫剧分类未找到";
        public static final String COMIC_SAVE_NAME_NULL = "漫剧名称为空，请填写漫剧名称";
        public static final String COMIC_SAVE_COVER_URL_NULL = "漫剧封面为空，请上传漫剧封面";
        public static final String COMIC_SAVE_FREE_NULL = "售卖模式为空，请选择售卖模式";
        public static final String COMIC_SAVE_PURCHASE_TIME_NULL = "漫剧周期为空，请设置漫剧周期";
        public static final String COMIC_SAVE_INTRODUCE_NULL = "漫剧介绍为空，请输入漫剧介绍";
        public static final String COMIC_SAVE_USE_PEOPLE_NULL = "适用人群为空，请输入适用人群";
        public static final String COMIC_SAVE_DETAIL_NULL = "漫剧详情为空，请输入漫剧详情";
        public static final String COMIC_SAVE_DURATION_NULL = "观看周期为空，请输入观看周期";
        public static final String COMIC_SAVE_PRICE_NULL = "漫剧价格为空，请输入漫剧价格";
        public static final String COMIC_SAVE_PRICE_NEGATIVE = "漫剧价格为正数，请输入合法的漫剧价格";
        public static final String COMIC_SAVE_PRICE_FREE = "免费漫剧没有价格，可以填0";
        public static final String COMIC_SAVE_PURCHASE_ILLEGAL = "漫剧下架时间不得早于当前时间";
        public static final String COMIC_SAVE_PURCHASE_ILLEGAL2 = "漫剧开始购买时间不得早于当前";
        public static final String COMIC_SAVE_NAME_EXISTS = "漫剧名称重复，请重新输入";
        public static final String COMIC_EPISODES_SAVE_NAME_NULL = "章名称为空，请输入章名称";
        public static final String COMIC_EPISODES_SAVE_NAME_SIZE = "章名称格格式错误，请重新输入";
        public static final String COMIC_EPISODES_SAVE_NAME_SIZE2 = "片段名称格格式错误，请重新输入";
        public static final String COMIC_EPISODES_SAVE_NAME_NULL2 = "片段名称为空，请输入片段名称";
        public static final String COMIC_EPISODES_SAVE_CHAPTER_WITHOUT_CLIP = "章里必须有片段";
        public static final String COMIC_EPISODES_SAVE_CHAPTER_NAME_DELETED = "已经上架的{}目录被删除了";
        public static final String COMIC_EPISODES_SAVE_CHAPTER_NAME_MOVE = "已经上架的《%s》目录被移动了";
        public static final String COMIC_EPISODES_SAVE_INEDX = "目录的章中有序号是重复的";
        public static final String COMIC_EPISODES_SAVE_INEDX_JUMP = "目录的章中有序号不连续";
        public static final String COMIC_EPISODES_SAVE_CHAPTER_INDEX_REPEAT = "章序号有重复的";
        public static final String COMIC_EPISODES_SAVE_CHAPTER_INDEX_INTERRUPTED = "章序号填写有中断";
        public static final String COMIC_EPISODE_NOT_EXISTS = "目录结构不存在";
        public static final String COMIC_MEDIA_SAVE_ILLEGAL = "请求参数不合法";
        public static final String COMIC_MEDIA_SAVE_SIZE_WRONG = "请检查所有的片段是否关联素材或题目";
        public static final String COMIC_MEDIA_SAVE_NO_EXECUTE = "素材当前不能保存";
        public static final String COMIC_MEDIA_SAVE_MEDIA_NULL = "部分章节未选择视频，请选择/上传视频";
        public static final String COMIC_MEDIA_SAVE_TRAILER_NULL = "有漫剧还没有选择是否支持试看";
        public static final String COMIC_TAG_SAVE_TAG_IDS_NULL = "阶段测试为空，请设置阶段测试题目";
        public static final String COMIC_TAG_SAVE_EPISODE_ID_NULL = "题目未指定练习id";
        public static final String COMIC_CREATOR_SAVE_COMIC_ID_NULL = "漫剧id不能为空";
        public static final String COMIC_CREATOR_SAVE_CREATORS_NULL = "请至少设置一名创作者";
//        public static final String COMIC_CREATOR_SAVE_CREATORS_NUM_MAX = "最多可设置5名漫剧创作者";
        public static final String COMIC_CREATOR_SAVE_CREATORS_NUM_MAX = "必须设置创作者1-到5人";
        public static final String COMIC_CREATOR_SAVE_CREATOR_SHOW = "创作者用户端显示不能为空";
        public static final String COMIC_CREATOR_SAVE_CREATOR_ID_NULL = "创作者id不能为空";

        public static final String COMIC_OPERATE_ID_NULL = "未选定漫剧";

        public static final String COMIC_UP_SHELF_INFO_INCOMPLETE = "漫剧信息未填写完，无法上架";
        public static final String COMIC_UP_SHELF_STATE_WRONG = "当前漫剧不能进行上架";
        public static final String COMIC_UP_SHELF_PURCHASE_ILLEGAL = "下架时间需晚于当前时间";
        public static final String COMIC_UP_SHELF_CLIP_WITHOUT_MEDIA = "片段《{}》未上传素材";
        public static final String COMIC_UP_SHELF_PRACTICE_WITHOUT_TAG = "练习《{}》未上传题目";
        public static final String COMIC_UP_SHELF_NOT_FOUND_COMIC = "未找到对应的漫剧";
        public static final String COMIC_UP_SHELF_AREADY = "漫剧已经上架，请勿重复操作";
        public static final String COMIC_DOWN_SHELF_FAILD = "当前漫剧不能下架";
        public static final String COMIC_CHECK_NOT_FOUND = "未查询到漫剧信息";
        public static final String COMIC_CHECK_NOT_EXISTS = "某些漫剧不存在或已经删除";
        public static final String COMIC_CHECK_DOWN_SHELF = "漫剧已经下架";
        public static final String COMIC_CHECK_FINISHED = "漫剧已经完结";
        public static final String COMIC_CHECK_NO_SALE = "漫剧还未开始销售";
    }
}
