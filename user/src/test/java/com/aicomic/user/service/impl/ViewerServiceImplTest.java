package com.aicomic.user.service.impl;

import com.aicomic.user.domain.dto.ViewerFormDTO;
import com.aicomic.user.service.IViewerService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class ViewerServiceImplTest {

    @Autowired
    private IViewerService viewerService;

    @Test
    void saveStudent() {
        for (int i = 1; i < 20; i++) {
            ViewerFormDTO dto = new ViewerFormDTO();
            dto.setCellPhone((13898675600L + i) + "");
            dto.setPassword("123321");
            viewerService.saveStudent(dto);
        }
    }
}