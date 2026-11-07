package com.aicomic.comic.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.aicomic.api.client.user.UserClient;
import com.aicomic.api.dto.user.UserDTO;
import com.aicomic.common.constants.ErrorInfo;
import com.aicomic.common.exceptions.DbException;
import com.aicomic.common.utils.BeanUtils;
import com.aicomic.common.utils.CollUtils;
import com.aicomic.comic.constants.ComicConstants;
import com.aicomic.comic.domain.dto.ComicCreatorSaveDTO;
import com.aicomic.comic.domain.po.ComicCreator;
import com.aicomic.comic.domain.po.ComicCreatorDraft;
import com.aicomic.comic.domain.vo.ComicCreatorVO;
import com.aicomic.comic.mapper.ComicCreatorDraftMapper;
import com.aicomic.comic.service.IComicDraftService;
import com.aicomic.comic.service.IComicCreatorDraftService;
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
public class ComicCreatorDraftServiceImpl extends ServiceImpl<ComicCreatorDraftMapper, ComicCreatorDraft> implements IComicCreatorDraftService {

    @Autowired
    private IComicDraftService comicDraftService;

    @Autowired
    private IComicCreatorService comicCreatorService;

    @Autowired
    private UserClient userClient;

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void save(ComicCreatorSaveDTO comicCreatorSaveDTO) {

        //1.数据删除条件
        LambdaUpdateWrapper<ComicCreatorDraft> updateWrapper =
                Wrappers.lambdaUpdate(ComicCreatorDraft.class)
                        .eq(ComicCreatorDraft::getCourseId, comicCreatorSaveDTO.getId());
        //1.1.数据删除
        baseMapper.delete(updateWrapper);

        //2.组装即将插入的数据
        List<ComicCreatorDraft> comicCreatorDrafts =
                BeanUtils.copyList(comicCreatorSaveDTO.getCreators(),
                        ComicCreatorDraft.class, (creatorInfo, creatorDraft) -> {
                            //2.1.设置漫剧id
                            creatorDraft.setCourseId(comicCreatorSaveDTO.getId());
                            //2.2.设置创作者id
                            creatorDraft.setTeacherId(creatorInfo.getId());
                            //2.3.设置漫剧中创作者排序
                            creatorDraft.setCIndex(comicCreatorSaveDTO.getCreators().indexOf(creatorInfo));
                        });
        //3.批量插入漫剧的创作者信息
        saveBatch(comicCreatorDrafts);
        //4.更新漫剧填写进度
        comicDraftService.updateStep(comicCreatorSaveDTO.getId(), ComicConstants.ComicStep.CREATOR);
    }

    @Override
    public List<ComicCreatorVO> queryTeacherOfCourse(Long courseId, Boolean see) {
        if (see) {
            //1.查询漫剧创作者关系
            List<ComicCreatorVO> comicCreatorVOS = comicCreatorService.queryTeachers(courseId);
            //1.1.漫剧创作者关系判非空
            if (CollUtils.isNotEmpty(comicCreatorVOS)) {
                return comicCreatorVOS;
            }
            //2.查询草稿中的漫剧创作者关系
            comicCreatorVOS = queryTeachers(courseId);
            //3.组装数据
            return CollUtils.isEmpty(comicCreatorVOS) ? new ArrayList<>() : comicCreatorVOS;
        } else {
            //4.查询草稿中的漫剧创作者关系
            return queryTeachers(courseId);
        }
    }

    @Override
    @Transactional(rollbackFor = {DbException.class, Exception.class})
    public void copyToShelf(Long courseId, Boolean isFirstShelf) {
        //1.先将草稿中的数据查出来
        LambdaQueryWrapper<ComicCreatorDraft> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ComicCreatorDraft::getCourseId, courseId);
        List<ComicCreatorDraft> comicCreatorDrafts = baseMapper.selectList(queryWrapper);
        //2.删除架上的创作者数据，
        if (!isFirstShelf) {
            comicCreatorService.deleteByCourseId(courseId);
        }
        //3.将草稿上架
        List<ComicCreator> comicCreators = BeanUtils.copyList(comicCreatorDrafts, ComicCreator.class);
        comicCreatorService.saveOrUpdateBatch(comicCreators);
        //4.删除草稿
        if (baseMapper.deleteByCourseId(courseId) <= 0) {
            throw new DbException(ErrorInfo.Msg.DB_DELETE_EXCEPTION);
        }
    }

    private List<ComicCreatorVO> queryTeachers(Long couserId) {

        //1.查询条件
        LambdaQueryWrapper<ComicCreatorDraft> queryWrapper =
                Wrappers.lambdaQuery(ComicCreatorDraft.class)
                        .eq(ComicCreatorDraft::getCourseId, couserId);
        //1.1.查询数据
        List<ComicCreatorDraft> comicCreatorDrafts = baseMapper.selectList(queryWrapper);
        //1.2.数据判空
        if (CollUtils.isEmpty(comicCreatorDrafts)) {
            return new ArrayList<>();
        }

        // 2.查询创作者详细信息
        List<UserDTO> UserDTOS = userClient.queryUserByIds(
                comicCreatorDrafts.stream().map(ComicCreatorDraft::getTeacherId).collect(Collectors.toList()));
        // 3.组织为map
        Map<Long, UserDTO> UserDTOMap = UserDTOS.stream().collect(Collectors.toMap(UserDTO::getId, UserDTO -> UserDTO));

        //4.数据组装
        return BeanUtils.copyList(comicCreatorDrafts, ComicCreatorVO.class,
                (comicCreator, comicCreatorVO) -> {
            //4.1.创作者信息
                    UserDTO creatorDetailDTO = UserDTOMap.get(comicCreator.getTeacherId());
            if (creatorDetailDTO != null) {
                //4.2.设置创作者形象照
                comicCreatorVO.setIcon(creatorDetailDTO.getIcon());
                comicCreatorVO.setPhoto(creatorDetailDTO.getPhoto());
                //4.3.设置创作者姓名
                comicCreatorVO.setName(creatorDetailDTO.getName());
                //4.4.设置创作者介绍
                comicCreatorVO.setIntroduce(creatorDetailDTO.getIntro());
                //4.5.设置创作者职业
                comicCreatorVO.setJob(creatorDetailDTO.getJob());
            }
            //4.6.设置创作者id
            comicCreatorVO.setId(comicCreator.getTeacherId());
        });
    }

}
