package com.aicomic.user.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.aicomic.api.client.comic.ComicClient;
import com.aicomic.api.dto.comic.SubNumAndComicNumDTO;
import com.aicomic.common.domain.dto.PageDTO;
import com.aicomic.common.utils.BeanUtils;
import com.aicomic.user.domain.po.UserDetail;
import com.aicomic.user.domain.query.UserPageQuery;
import com.aicomic.user.domain.vo.CreatorPageVO;
import com.aicomic.common.enums.UserType;
import com.aicomic.user.service.ICreatorService;
import com.aicomic.user.service.IUserDetailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * <p>
 * 创作者详情表 服务实现类
 * </p>
 *
 * @since 2022-07-12
 */
@Service
public class CreatorServiceImpl implements ICreatorService {
    @Autowired
    private IUserDetailService detailService;
    @Autowired
    private ComicClient courseClient;

    @Override
    public PageDTO<CreatorPageVO> queryTeacherPage(UserPageQuery pageQuery) {
        // 1.分页参数
        Page<UserDetail> page = detailService.queryUserDetailByPage(pageQuery, UserType.CREATOR);
        // 2.处理返回值
        List<UserDetail> records = page.getRecords();
        // 2.1.查询创作者的试题数量、漫剧数量
        List<Long> ids = records.stream().map(UserDetail::getId).collect(Collectors.toList());
        List<SubNumAndComicNumDTO> countDTOs = courseClient.infoByTeacherIds(ids);
        Map<Long, SubNumAndComicNumDTO> map = countDTOs.stream()
                .collect(Collectors.toMap(SubNumAndComicNumDTO::getTeacherId, s -> s));
        // 2.2.数据转换
        List<CreatorPageVO> list = new ArrayList<>(records.size());
        for (UserDetail record : records) {
            CreatorPageVO creatorPageVO = BeanUtils.toBean(record, CreatorPageVO.class);
            SubNumAndComicNumDTO sc = map.get(creatorPageVO.getId());
            creatorPageVO.setCourseAmount(sc.getCourseNum());
            creatorPageVO.setQuizQuestionAmount(sc.getSubjectNum());
            list.add(creatorPageVO);
        }
        return new PageDTO<>(page.getTotal(), page.getPages(), list);
    }
}
