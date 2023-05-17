package com.shop.repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Wildcard;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.shop.constant.ItemSellStatus;
import com.shop.dto.ItemSearchDto;
import com.shop.dto.MainItemDto;
import com.shop.dto.QMainItemDto;
import com.shop.entity.Item;
import com.shop.entity.QItem;
import com.shop.entity.QItemImg;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.thymeleaf.util.StringUtils;

import javax.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.List;

public class ItemRepositoryCustomImpl implements ItemRepositoryCustom {

    private JPAQueryFactory queryFactory;

    public ItemRepositoryCustomImpl(EntityManager em) {
        this.queryFactory = new JPAQueryFactory(em);
    }


    // 여기서 오는 1d, 1w, 1m, 6m 값은 모두  ItemMng 에서 가져온다.
    private BooleanExpression regDateAfter(String searchDateType) {
        LocalDateTime dateTime = LocalDateTime.now();
        if (StringUtils.equals("all", searchDateType) || searchDateType == null) {
            return null;
        } else if (StringUtils.equals("1d", searchDateType)) {
            dateTime = dateTime.minusDays(1);
        } else if (StringUtils.equals("1w", searchDateType)) {
            dateTime = dateTime.minusWeeks(1);
        } else if (StringUtils.equals("1m", searchDateType)) {
            dateTime = dateTime.minusMonths(1);
        } else if (StringUtils.equals("6m", searchDateType)) {
            dateTime = dateTime.minusMonths(6);
        }
        return QItem.item.regTime.after(dateTime);
    }


    private BooleanExpression searchSellStatusEq(ItemSellStatus searchSellStatus) {
        return searchSellStatus == null ? null : QItem.item.itemSellStatus.eq(searchSellStatus);
    }

    private BooleanExpression searchByLike(String searchBy, String seachQuery) {
        if (StringUtils.equals("itemNm", searchBy)) {
            return QItem.item.itemNm.like("%" + seachQuery + "%");
        } else if (StringUtils.equals("createdBy", searchBy)) {
            return QItem.item.itemNm.like("%" + seachQuery + "%");
        }
        return null;
    }


    @Override
    public Page<Item> getAdminItemPage(ItemSearchDto itemSearchDto, Pageable pageable) {
        List<Item> content = queryFactory
                .selectFrom(QItem.item)
                .where(regDateAfter(itemSearchDto.getSearchDateType()),
                        searchSellStatusEq(itemSearchDto.getSearchSellStatus()),
                        searchByLike(itemSearchDto.getSearchBy(), itemSearchDto.getSearchQuery())
                ).orderBy(QItem.item.id.desc())
                .offset(pageable.getOffset()).limit(pageable.getPageSize())
                .fetch();
        // fetchOne은 하나만 가져오는 것이라서 예외처리에 대한 도움을 받을 수 있음
        // ex) 여러개가 들어오면 잘못된 것이기에 실행에 오류가 난다.
        long total = queryFactory.select(Wildcard.count).from(QItem.item)
                .where(regDateAfter(itemSearchDto.getSearchDateType()),
                        searchSellStatusEq(itemSearchDto.getSearchSellStatus()),
                        searchByLike(itemSearchDto.getSearchBy(), itemSearchDto.getSearchQuery())
                ).fetchOne();


        return new PageImpl<>(content, pageable, total);
    }


    private BooleanExpression itemNmLike(String searchQuery)
    {
        return StringUtils.isEmpty(searchQuery)?null:QItem.item.itemNm.like("%" + searchQuery + "%" );
    }
    @Override
    public Page<MainItemDto> getMainitemPage(ItemSearchDto itemSearchDto, Pageable pageable) {
        QItem item = QItem.item;
        QItemImg itemImg = QItemImg.itemImg;


        List<MainItemDto> content=queryFactory
                .select(
                        new QMainItemDto(
                                item.id,
                                item.itemNm,
                                item.itemDetail,
                                itemImg.imgUrl,
                                item.price)
                ).from(itemImg)
                .join(itemImg.item, item)
                .where(itemImg.repimgYn.eq("Y"))
                .where(itemNmLike(itemSearchDto.getSearchQuery()))
                .orderBy(item.id.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();
        long total = queryFactory
                .select(Wildcard.count)
                .from(itemImg)
                .join(itemImg.item,item)
                .where(itemImg.repimgYn.eq("Y"))
                .where(itemNmLike(itemSearchDto.getSearchQuery()))
                .fetchOne();


        return new PageImpl<>(content, pageable, total);
    }
}
