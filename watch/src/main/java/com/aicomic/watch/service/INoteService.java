package com.aicomic.watch.service;

import com.aicomic.common.domain.dto.PageDTO;
import com.aicomic.watch.domain.dto.NoteFormDTO;
import com.aicomic.watch.domain.po.Note;
import com.baomidou.mybatisplus.extension.service.IService;
import com.aicomic.watch.domain.query.NoteAdminPageQuery;
import com.aicomic.watch.domain.query.NotePageQuery;
import com.aicomic.watch.domain.vo.NoteAdminDetailVO;
import com.aicomic.watch.domain.vo.NoteAdminVO;
import com.aicomic.watch.domain.vo.NoteVO;

/**
 * <p>
 *  服务类
 * </p>
 *
 */
public interface INoteService extends IService<Note> {

    void saveNote(NoteFormDTO noteDTO);

    void gatherNote(Long id);

    void removeGatherNote(Long id);

    void updateNote(NoteFormDTO noteDTO);

    PageDTO<NoteVO> queryNotePage(NotePageQuery query);

    PageDTO<NoteAdminVO> queryNotePageForAdmin(NoteAdminPageQuery query);

    NoteAdminDetailVO queryNoteDetailForAdmin(Long id);

    void hiddenNote(Long id, boolean hidden);

    void removeMyNote(Long id);
}
