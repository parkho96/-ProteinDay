package com.shop.dto;

import com.shop.constant.ItemSellStatus;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter @Setter
public class ItemSearchDto  {
    // 상품 등록일 all 1d 1w 1m 6m
    private String searchDateType;
    private ItemSellStatus searchSellStatus;

    // 검색대상 itemNm createdBy
    private String searchBy;
    private String searchQuery="";

}
