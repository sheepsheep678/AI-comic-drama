package com.aicomic.watch.service;

import com.aicomic.watch.domain.vo.SignResultVO;

public interface ISignRecordService {
    SignResultVO addSignRecords();

    Byte[] querySignRecords();

}
