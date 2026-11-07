package com.aicomic.search.service;

import com.aicomic.common.domain.dto.PageDTO;
import com.aicomic.search.domain.query.ComicPageQuery;
import com.aicomic.search.domain.vo.ComicVO;

import java.util.List;

public interface ISearchService {

    List<ComicVO> queryCourseByCateId(Long cateLv2Id);

    List<ComicVO> queryBestTopN();

    List<ComicVO> queryNewTopN();

    List<ComicVO> queryFreeTopN();

    PageDTO<ComicVO> queryCoursesForPortal(ComicPageQuery query);

    List<Long> queryCoursesIdByName(String keyword);
}
