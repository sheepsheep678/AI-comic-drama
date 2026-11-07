package com.aicomic.comic.utils;

import com.aicomic.common.utils.CollUtils;
import com.aicomic.common.utils.ObjectUtils;
import com.aicomic.common.utils.ReflectUtils;
import com.aicomic.common.utils.StringUtils;
import com.aicomic.comic.domain.po.Tag;

import java.util.ArrayList;
import java.util.List;

/**
 * 题目工具类
 *
 * @ClassName TagUtils
 * @Author 
 * @Date 2022/7/15 17:13
 * @Version
 **/
public class TagUtils {

    /**
     * 将选项列表中的选项设置到题目中
     *
     * @param tag 题目
     * @param options 选项
     */
    public static void setOptions(Tag tag, List<String> options) {
        if (CollUtils.isNotEmpty(options)) {
            for (int count = 0; count < options.size(); count++) {
                ReflectUtils.setFieldValue(tag, "option" + (count + 1), options.get(count));
            }
        }
    }

    /**
     * 从题目中获取选项
     *
     * @param tag 题目
     * @return 选项
     */
    public static List<String> getOptions(Tag tag) {
        List<String> options = new ArrayList<>();
        for (int count = 1; count <= 10; count++) {
            Object option = ReflectUtils.getFieldValue(tag, "option" + count);
            if (ObjectUtils.isEmpty(option) || StringUtils.isEmpty((String)option)) {
                return options;
            }
            options.add((String) option);
        }
        return options;
    }
}
