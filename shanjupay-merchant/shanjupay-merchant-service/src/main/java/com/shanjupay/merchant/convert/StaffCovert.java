package com.shanjupay.merchant.convert;

import com.shanjupay.merchant.api.dto.StaffDTO;
import com.shanjupay.merchant.entity.Staff;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface StaffCovert {

    StaffCovert INSTANCE = Mappers.getMapper(StaffCovert.class);

    Staff dto2entity(StaffDTO staffDTO);

    StaffDTO entity2dto(Staff staff);

}
