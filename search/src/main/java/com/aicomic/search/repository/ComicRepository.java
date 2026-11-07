package com.aicomic.search.repository;

import com.aicomic.search.domain.po.Comic;

import java.util.List;
import java.util.Optional;

public interface ComicRepository{
    String INDEX_NAME = "comic";
    String DEFAULT_QUERY_NAME = "name";
    String CATEGORY_ID_LV1 = "categoryIdLv1";
    String CATEGORY_ID_LV2 = "categoryIdLv2";
    String CATEGORY_ID_LV3 = "categoryIdLv3";
    String PUBLISH_TIME = "publishTime";
    String FREE = "free";
    String STATUS = "status";
    String TYPE = "type";
    String UPDATE_TIME = "updateTime";
    String SOLD = "sold";

    /**
     * <h1>更新sold的脚本</h1>
     * <pre>
     * {
     *   "script": {
     *     "lang": "painless",
     *     "source": "ctx._source.sold += params.count"
     *   }
     * }
     * </pre>
     */
    String INCREMENT_SOLD_SCRIPT_ID = "increment_sold";
    String INCREMENT_SOLD_SCRIPT_PARAM = "count";

    void save(Comic course);

    void deleteById(Long courseId);

    Optional<Comic> findById(Long courseId);

    void updateById(Long courseId, Object ... docs);

    void increment(Long courseId, String field, int amount);

    void incrementSold(List<Long> courseIds, int amount);

    void saveAll(List<Comic> list);

    void deleteByIds(List<Long> courseIds);
}
