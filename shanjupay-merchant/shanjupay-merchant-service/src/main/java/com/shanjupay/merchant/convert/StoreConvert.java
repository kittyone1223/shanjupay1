package com.shanjupay.merchant.convert;

import com.shanjupay.merchant.api.dto.StoreDTO;
import com.shanjupay.merchant.entity.Store;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;


@Mapper
public interface StoreConvert {
    StoreConvert INSTANCE = Mappers.getMapper(StoreConvert.class);

    Store dto2entity(StoreDTO storeDTO);
    StoreDTO entity2dto(Store store);
    List<Store> dto2entity(List<StoreDTO> storeDTOList);

    List<StoreDTO> entitylist2dtolist(List<Store> storeList);
}
