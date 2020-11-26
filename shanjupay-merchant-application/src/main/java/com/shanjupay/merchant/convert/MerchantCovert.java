package com.shanjupay.merchant.convert;


import com.shanjupay.merchant.api.dto.MerchantDTO;
import com.shanjupay.merchant.vo.MerchantRegisterVO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface MerchantCovert {

    // 转换类实例
    MerchantCovert INSTANCE = Mappers.getMapper(MerchantCovert.class);

    // MerchantRegisterVO转MerchantDTO
    MerchantDTO vo2dto(MerchantRegisterVO merchantRegisterVO);
    // MerchantDTO转MerchantRegisterVO
    MerchantRegisterVO dto2vo(MerchantDTO merchantDTO);

    public static void main(String[] args) {
        MerchantRegisterVO merchantRegisterVO = new MerchantRegisterVO();
        merchantRegisterVO.setMobile("123456");
        merchantRegisterVO.setUsername("wx");
        merchantRegisterVO.setPassword("123321");
        MerchantDTO merchantDTO = MerchantCovert.INSTANCE.vo2dto(merchantRegisterVO);
        System.out.println(merchantDTO);
        MerchantRegisterVO merchantRegisterVO1 = MerchantCovert.INSTANCE.dto2vo(merchantDTO);
        System.out.println(merchantRegisterVO1);
    }

}
