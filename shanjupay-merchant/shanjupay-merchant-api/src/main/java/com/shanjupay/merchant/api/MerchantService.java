package com.shanjupay.merchant.api;

import com.shanjupay.common.domain.BusinessException;
import com.shanjupay.common.domain.PageVO;
import com.shanjupay.merchant.api.dto.MerchantDTO;
import com.shanjupay.merchant.api.dto.StaffDTO;
import com.shanjupay.merchant.api.dto.StoreDTO;

/**
 * Created by Administrator.
 */
public interface MerchantService {

    //根据 id查询商户
    public MerchantDTO queryMerchantById(Long id);


    /**
     * 根据租户id来查询商户信息
     * @param tenantId
     * @return
     */
    MerchantDTO queryMerchantByTenantId(Long tenantId) throws BusinessException;


    /**
     *   //注册用户
     * @param merchantDTO
     * @return
     * @throws BusinessException
     */
    public MerchantDTO createMerchant(MerchantDTO merchantDTO) throws BusinessException;

    public void applyMerchant(Long merchantId,MerchantDTO merchantDTO) throws BusinessException;

    /**
     * 在商户下新增门店
     * @param storeDTO
     * @return
     * @throws BusinessException
     */
    public StoreDTO createStore(StoreDTO storeDTO) throws BusinessException;


    /**
     * 商户新增员工
     * @param staffDTO
     * @return
     * @throws BusinessException
     */
    StaffDTO createStaff(StaffDTO staffDTO) throws BusinessException;


    /**
     * 为门店设置管理员  将员工和门店绑定
     * @param storeId
     * @param staffId
     * @throws BusinessException
     */
    void bindStaffToStore(Long storeId,Long staffId) throws BusinessException;


    /**
     *  分页查询商户下面的门店
     * @param storeDTO
     * @param pageNo
     * @param pageSize
     * @return
     */
    PageVO<StoreDTO> queryStoreByPage(StoreDTO storeDTO,Integer pageNo,Integer pageSize);


    /**
     * 查询某个门店是否属于某个商户
     * @param storeId
     * @param merchantId
     * @return
     * @throws BusinessException
     */
    Boolean queryStoreInMercahnt(Long storeId,Long merchantId) throws BusinessException;

}
