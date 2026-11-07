package com.aicomic.comic.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.aicomic.api.client.user.UserClient;
import com.aicomic.api.dto.user.UserDTO;
import com.aicomic.common.constants.ErrorInfo;
import com.aicomic.common.exceptions.DbException;
import com.aicomic.common.utils.BeanUtils;
import com.aicomic.common.utils.CollUtils;
import com.aicomic.comic.domain.po.ComicCreator;
import com.aicomic.comic.domain.vo.ComicCreatorVO;
import com.aicomic.comic.mapper.ComicCreatorMapper;
import com.aicomic.comic.service.IComicCreatorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * <p>
 * 漫剧创作者关系表草稿 服务实现类
 * </p>
 *
 * @since 2022-07-20
 */
@Service
public class ComicCreatorServiceImpl extends ServiceImpl<ComicCreatorMapper, ComicCreator> implements IComicCreatorService {

    @Autowired
    private UserClient userClient;

    @Override
    public List<ComicCreatorVO> queryTeachers(Long couserId) {
        //1.查询条件
        LambdaQueryWrapper<ComicCreator> queryWrapper =
                Wrappers.lambdaQuery(ComicCreator.class)
                        .eq(ComicCreator::getCourseId, couserId);
        //2.查询数据
        List<ComicCreator> comicCreators = baseMapper.selectList(queryWrapper);
        //2.1.数据判空
        if (CollUtils.isEmpty(comicCreators)) {
            return null;
        }
        //3.查询创作者信息
        List<UserDTO> creatorDetailDTOS =
                userClient.queryUserByIds(
                        comicCreators
                                .stream()
                                .map(ComicCreator::getTeacherId)
                                .collect(Collectors.toList()));
        //3.1.创作者id+创作者信息map
        Map<Long, UserDTO> creatorDetailDTOMap =
                creatorDetailDTOS
                        .stream()
                        .collect(Collectors.toMap(UserDTO::getId,
                                CreatorDetailDTO -> CreatorDetailDTO));
        //4.组装数据
        return BeanUtils.copyList(comicCreators, ComicCreatorVO.class,
                (comicCreator, comicCreatorVO) -> {
                    //4.1.创作者信息
                    UserDTO creatorDetailDTO = creatorDetailDTOMap.get(comicCreator.getTeacherId());
                    if (creatorDetailDTO != null) {
                        //4.2设置创作者形象照
                        comicCreatorVO.setIcon(creatorDetailDTO.getPhoto());
                        //4.3设置创作者姓名
                        comicCreatorVO.setName(creatorDetailDTO.getName());
                        //4.4.设置创作者简介
                        comicCreatorVO.setIntroduce(creatorDetailDTO.getIntro());
                        //4.5.设置创作者职业
                        comicCreatorVO.setJob(creatorDetailDTO.getJob());
                    }
                    //4.6.设置创作者id
                    comicCreatorVO.setId(comicCreator.getTeacherId());
                });
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = {DbException.class, Exception.class})
    public void deleteByCourseId(Long courserId) {
        //1.删除创作者-漫剧关系
        if (baseMapper.deleteByCourseId(courserId) <= 0) {
            throw new DbException(ErrorInfo.Msg.DB_DELETE_EXCEPTION);
        }
    }

    @Override
    public List<Long> getTeacherIdOfCourse(Long courseId) {
        //1.查询条件
        LambdaQueryWrapper<ComicCreator> queryWrapper =
                Wrappers.lambdaQuery(ComicCreator.class)
                        .eq(ComicCreator::getCourseId, courseId)
                        .orderByAsc(ComicCreator::getCIndex);
        //2.查询数据
        List<ComicCreator> comicCreators = baseMapper.selectList(queryWrapper);
        //3.组装数据
        return CollUtils.isEmpty(comicCreators) ?
                new ArrayList<>() : comicCreators
                .stream()
                .map(ComicCreator::getTeacherId)
                .collect(Collectors.toList());
    }

}
