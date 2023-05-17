package com.shop.entity;

import com.fasterxml.jackson.databind.ser.Serializers;
import com.shop.constant.ItemSellStatus;
import com.shop.dto.ItemFormDto;
import com.shop.exception.OutOfStockException;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name="item")
@Getter
@Setter
@ToString
public class Item extends BaseEntity {
    @Id
    @Column(name = "item_id")
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id; //상품 코드

    @Column(nullable = false, length = 50)
    private String itemNm; //상품명

    @Column(name="price",nullable = false)
    private int price; //가격

    @Column(nullable = false)
    private int stockNumber; //재고수량

    @Lob
    @Column(nullable = false)
    private String itemDetail; //상품 상세 설명

    @Enumerated(EnumType.STRING)
    private ItemSellStatus itemSellStatus; //상품 판매 상태

    @OneToMany(mappedBy = "item", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ItemImg> itemImg;
    public void updateItem(ItemFormDto itemFormDto)
    {
        this.itemNm = itemFormDto.getItemNm();
        this.price = itemFormDto.getPrice();
        this.stockNumber = itemFormDto.getStockNumber();
        this.itemDetail = itemFormDto.getItemDetail();
        this.itemSellStatus = itemFormDto.getItemSellStatus();
    }
    // 재고 변경에 대한 것
    // 엔티티 내에서 하는 이유가 어짜피 감소한 값을 업데이트 해도 되지만 
    // 여기서 하면 공용으로 있기에 구현 과정이 편해짐 예외처리는 OutOfStockException에서 처리할 예정
    public void removeStock(int sellNumber)
    {
        int restStock = this.stockNumber - sellNumber;
//        int restStock = stockNumber - sellNumber; this 꼭써야하나?

        // 이거 내가 개인적으로 넣은 것
        if(restStock==0)
        {
            this.itemSellStatus = ItemSellStatus.SOLD_OUT;
        }else if(restStock<0)
        {
            throw new OutOfStockException("상품의 재고가 부족합니다.(현재 재고 수량: " + this.stockNumber+ ")");
        }
        this.stockNumber = restStock;
    }


    public void addStock(int cancelNumber)
    {
        // 이거 내가 개인적으로 넣은 것
        if(this.itemSellStatus == ItemSellStatus.SOLD_OUT)
        {
            this.itemSellStatus = ItemSellStatus.SELL;
        }

        this.stockNumber += cancelNumber;
    }
}
