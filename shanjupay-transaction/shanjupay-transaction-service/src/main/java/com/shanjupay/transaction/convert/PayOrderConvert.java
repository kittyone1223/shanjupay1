package com.shanjupay.transaction.convert;

import com.shanjupay.transaction.api.dto.OrderResultDTO;
import com.shanjupay.transaction.api.dto.PayOrderDTO;
import com.shanjupay.transaction.entity.PayOrder;
import com.shanjupay.transaction.vo.OrderConfirmVO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface PayOrderConvert {

    PayOrderConvert INSTANCE = Mappers.getMapper(PayOrderConvert.class);

    OrderResultDTO request2dto(PayOrderDTO payOrderDTO);

    PayOrderDTO dto2request(OrderResultDTO OrderResult);

    //OrderResultDTO entity2dto(PayOrder entity);
    PayOrderDTO entity2dto(PayOrder payOrder);


    // vo 转dto
    @Mapping(target = "totalAmount", ignore = true)
    PayOrderDTO vo2dto(OrderConfirmVO orderConfirmVO);

    PayOrder dto2entity(PayOrderDTO payOrderDTO);
    //PayOrderDTO payorderentity2dto(PayOrder payOrder);


    PayOrder dto2entity(OrderResultDTO dto);

}
