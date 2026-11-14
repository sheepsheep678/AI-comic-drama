package com.aicomic.unlock.strategy.scope;

import com.aicomic.api.client.comic.ComicClient;
import com.aicomic.api.dto.comic.ComicSimpleInfoDTO;
import com.aicomic.common.exceptions.BizIllegalException;
import com.aicomic.common.utils.CollUtils;
import com.aicomic.unlock.constants.ScopeType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Component(ScopeType.COMIC_HANDLER_NAME)
public class ComicScopeNameHandler implements ScopeNameHandler {

    private final ComicClient courseClient;

    @Override
    public List<String> getNameByIds(List<Long> scopeIds) {
        List<ComicSimpleInfoDTO> infos = courseClient.getSimpleInfoList(scopeIds);
        if (CollUtils.isEmpty(infos)) {
            throw new BizIllegalException("漫剧信息不存在");
        }
        return infos.stream().map(ComicSimpleInfoDTO::getName).collect(Collectors.toList());
    }
}
