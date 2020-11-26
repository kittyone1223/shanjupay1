package com.shanjupay.merchant.convert;


import com.shanjupay.merchant.api.dto.MerchantDTO;
import com.shanjupay.merchant.entity.Merchant;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.ArrayList;
import java.util.List;

@Mapper
public interface MerchantCovert {
    MerchantCovert INSTANCE = Mappers.getMapper(MerchantCovert.class);

    MerchantDTO entity2dto(Merchant entity);

    Merchant dto2entity(MerchantDTO dto);

    List<Merchant> dtoList2entity(List<MerchantDTO> merchantDTOS);

    public static void main(String[] args) {
        MerchantDTO merchantDTO1 = new MerchantDTO();
        merchantDTO1.setMobile("123");
        MerchantDTO merchantDTO2 = new MerchantDTO();
        merchantDTO2.setMobile("123");
        MerchantDTO merchantDTO3= new MerchantDTO();
        merchantDTO3.setMobile("123");
        List list = new ArrayList<MerchantDTO>();
        list.add(merchantDTO1);
        list.add(merchantDTO2);
        list.add(merchantDTO3);

        List list1 = MerchantCovert.INSTANCE.dtoList2entity(list);



    }

}
