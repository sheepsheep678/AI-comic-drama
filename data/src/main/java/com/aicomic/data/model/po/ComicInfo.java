package com.aicomic.data.model.po;

import lombok.Data;

import java.io.Serializable;

/**
 * @ClassName ComicInfo
 * @Author 
 * @Date 2022/10/10 19:33
 * @Version
 **/
@Data
public class ComicInfo implements Serializable {
    private String category;
    private String name;
    private Integer newStuNum;
    private Double orderAmount;
}
