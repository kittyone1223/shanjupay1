package com.shanjupay.merchant.service;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shanjupay.common.domain.BusinessException;
import com.shanjupay.common.domain.CommonErrorCode;
import com.shanjupay.common.domain.ErrorCode;
import com.shanjupay.common.domain.PageVO;
import com.shanjupay.common.util.PhoneUtil;
import com.shanjupay.merchant.api.MerchantService;
import com.shanjupay.merchant.api.dto.MerchantDTO;
import com.shanjupay.merchant.api.dto.StaffDTO;
import com.shanjupay.merchant.api.dto.StoreDTO;
import com.shanjupay.merchant.convert.MerchantCovert;
import com.shanjupay.merchant.convert.StaffCovert;
import com.shanjupay.merchant.convert.StoreConvert;
import com.shanjupay.merchant.entity.Merchant;
import com.shanjupay.merchant.entity.Staff;
import com.shanjupay.merchant.entity.Store;
import com.shanjupay.merchant.entity.StoreStaff;
import com.shanjupay.merchant.mapper.MerchantMapper;
import com.shanjupay.merchant.mapper.StaffMapper;
import com.shanjupay.merchant.mapper.StoreMapper;
import com.shanjupay.merchant.mapper.StoreStaffMapper;
import com.shanjupay.user.api.TenantService;
import com.shanjupay.user.api.dto.tenant.CreateTenantRequestDTO;
import com.shanjupay.user.api.dto.tenant.TenantDTO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.dubbo.config.annotation.Reference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Created by Administrator.
 */
@org.apache.dubbo.config.annotation.Service
@Slf4j
public class MerchantServiceImpl implements MerchantService {

    @Autowired
    MerchantMapper merchantMapper;


    @Autowired
    private StaffMapper staffMapper;

    @Autowired
    private StoreMapper storeMapper;

    @Autowired
    private StoreStaffMapper storeStaffMapper;

    @Reference
    private TenantService tenantService;

    @Override
    public MerchantDTO queryMerchantById(Long id) {
        Merchant merchant = merchantMapper.selectById(id);
//        MerchantDTO merchantDTO = new MerchantDTO();
//        merchantDTO.setId(merchant.getId());
//        merchantDTO.setMerchantName(merchant.getMerchantName());


        //....
        return MerchantCovert.INSTANCE.entity2dto(merchant);
    }

    /**
     *  根据租户id来查询用户id
     * @param tenantId
     * @return
     * @throws BusinessException
     */
    @Override
    public MerchantDTO queryMerchantByTenantId(Long tenantId) throws BusinessException {

        LambdaQueryWrapper<Merchant> lambdaQueryWrapper = new LambdaQueryWrapper<Merchant>().eq(Merchant::getTenantId, tenantId);
        Merchant merchant = merchantMapper.selectOne(lambdaQueryWrapper);
        return MerchantCovert.INSTANCE.entity2dto(merchant);
    }


    /**
     * 创建商户  接收账号 密码 手机号 为了可扩展性使用merchantDto接受数据
     * 调用saas接口新增租户 用户 绑定租户 和用户的关系   初始化权限
     *
     * @param merchantDTO
     * @return
     */
    @Override
    public MerchantDTO createMerchant(MerchantDTO merchantDTO) {
//        Merchant merchant = new Merchant();
//        // 设置审核状态0‐未申请,1‐已申请待审核,2‐审核通过,3‐审核拒绝
//        merchant.setAuditStatus("0");
//        // 设置手机号
//        merchant.setMobile(merchantDTO.getMobile());
//        merchant.setUsername(merchantDTO.getUsername());
//        merchant.setPassword(merchantDTO.getPassword());


        // 1.校验
        if (merchantDTO == null) {
            throw new BusinessException(CommonErrorCode.E_100108);
        }
        // 手机号非空
        if (StringUtils.isEmpty(merchantDTO.getMobile())) {
            throw new BusinessException(CommonErrorCode.E_100112);
        }

        // 校验手机号的合法性
        if (!PhoneUtil.isMatches(merchantDTO.getMobile())) {
            throw new BusinessException(CommonErrorCode.E_100109);
        }


        // 商户注册时  密码也不能为空
        if (StringUtils.isBlank(merchantDTO.getPassword())) {
            throw new BusinessException(CommonErrorCode.E_100111);
        }

        // 手机号的唯一性
        QueryWrapper<Merchant> merchantQueryWrapper = new QueryWrapper<>();

        Integer count = merchantMapper.selectCount(merchantQueryWrapper.eq("mobile", merchantDTO.getMobile()));
        if (count > 0) {
            throw new BusinessException(CommonErrorCode.E_100113);
        }


        // 调用saas接口
        /**
         * 接口参数： 1、手机号 2、账号 3、密码 4、租户类型：shanju-merchant 5、默认套餐：shanju-merchant 6、租户名称，同账号名
         */

        CreateTenantRequestDTO createTenantRequestDTO = new CreateTenantRequestDTO();
        createTenantRequestDTO.setMobile(merchantDTO.getMobile());
        createTenantRequestDTO.setUsername(merchantDTO.getUsername());
        createTenantRequestDTO.setPassword(merchantDTO.getPassword());
        createTenantRequestDTO.setTenantTypeCode("shanju-merchant");   // 租户类型
        createTenantRequestDTO.setBundleCode("shanju-merchant");  // 套餐  根据套餐设置权限
        createTenantRequestDTO.setName(merchantDTO.getUsername());

        // 如果用户在sass已经存在  saas直接   返回此用户的信息   否则进行添加
        TenantDTO tenantAndAccount = tenantService.createTenantAndAccount(createTenantRequestDTO);

        // 获取租户的id
        if (tenantAndAccount==null||tenantAndAccount.getId()==null){
            throw new BusinessException(CommonErrorCode.E_200012);
        }

        // 该id为租户id
        Long id = tenantAndAccount.getId();


        /// 利用mapsturct

        Merchant merchant = MerchantCovert.INSTANCE.dto2entity(merchantDTO);
        merchant.setAuditStatus("0");

        // 在数据库添加时  将返回的租户id设置到商户表中的最后一个字段
        merchant.setTenantId(id);
        // 保存用户
        merchantMapper.insert(merchant);

        // 新增门店
        StoreDTO storeDTO = new StoreDTO();
        storeDTO.setStoreName("根门店");
        storeDTO.setMerchantId(merchant.getId()); // 商户id
        storeDTO.setStoreStatus(true);
        StoreDTO store = createStore(storeDTO);

        // 新增员工

        StaffDTO staffDTO = new StaffDTO();
        staffDTO.setMobile(merchantDTO.getMobile());  // 手机
        staffDTO.setUsername(merchantDTO.getUsername());  // 账号
        staffDTO.setStoreId(store.getId());  // 员工所属门店id
        staffDTO.setMerchantId(merchant.getId());  //商户id
        StaffDTO staff = createStaff(staffDTO);

        // 门店和员工  相互绑定

        // 设施管理员

        log.info("门店{}",store.getId());
        log.info("员工{}",staff.getId());
        System.out.println("-----------------------------"+store.getId()+"-------------------------");
        System.out.println("-----------------------------"+staff.getId()+"-------------------------");
        bindStaffToStore(store.getId(),staff.getId());



        return MerchantCovert.INSTANCE.entity2dto(merchant);
    }

    @Override
    @Transactional
    public void applyMerchant(Long merchantId, MerchantDTO merchantDTO) throws BusinessException {
        // 接受资质申请  更新到数据库中

        if (merchantId == null || merchantDTO == null) {
            throw new BusinessException(CommonErrorCode.E_100108);
        }

        // 在资质申请时  先查询校验数据库表中是否存在该商户id
        Merchant merchant = merchantMapper.selectById(merchantId);
        if (merchant == null) {
            throw new BusinessException(CommonErrorCode.E_200002);
        }

        Merchant merchant_update = MerchantCovert.INSTANCE.dto2entity(merchantDTO);
        merchant_update.setAuditStatus("1");
        merchant_update.setUsername(merchant.getUsername());
        merchant_update.setId(merchant.getId());
        merchant_update.setPassword(merchant.getPassword());
        merchant_update.setMobile(merchant.getMobile());
        merchant_update.setTenantId(merchant.getTenantId());  // 租户id

        // 更新
        merchantMapper.updateById(merchant_update);

    }

    @Override
    public StoreDTO createStore(StoreDTO storeDTO) throws BusinessException {

        Store store = StoreConvert.INSTANCE.dto2entity(storeDTO);
        log.info("商户下新增门店" + JSON.toJSONString(store));
        storeMapper.insert(store);
        return StoreConvert.INSTANCE.entity2dto(store);
    }

    @Override
    public StaffDTO createStaff(StaffDTO staffDTO) throws BusinessException {
        //1.校验手机号格式及是否存在
        String mobile = staffDTO.getMobile();
        Long merchantId = staffDTO.getMerchantId();

        // 判断手机号是否为空
        if (StringUtils.isBlank(mobile)) {
            throw new BusinessException(CommonErrorCode.E_100112);
        }


        // 判断用户的所属门店merchantId是否为空
        if (staffDTO.getStoreId() == null) {
            log.info("所在的门店不存在");
            throw new BusinessException(CommonErrorCode.E_200002);
        }


        // 判断用户名是否为空
        if (StringUtils.isBlank(staffDTO.getUsername())) {
            throw new BusinessException(CommonErrorCode.E_100110);
        }


        // 校验商户id和手机号的唯一性   判断数据库staff中是否纯在 校验唯一性
        if (isExistStaffByMobile(mobile, merchantId)) {
            throw new BusinessException(CommonErrorCode.E_100114);
        }


        String username = staffDTO.getUsername();

        //    条件查询   判断用户是否已经存在  并且在所属商家下
        if (isExistStaffByUserName(username, merchantId)) {
            throw new BusinessException(CommonErrorCode.E_100114);
        }

        Staff staff = StaffCovert.INSTANCE.dto2entity(staffDTO);

        if (staff != null) {
            staffMapper.insert(staff);
            return StaffCovert.INSTANCE.entity2dto(staff);
        }

        //
        return null;
    }

    /**
     * 为门店设置管理员
     *
     * @param storeId
     * @param staffId
     * @throws BusinessException
     */
    @Override
    public void bindStaffToStore(Long storeId, Long staffId) throws BusinessException {
        StoreStaff storeStaff = new StoreStaff();
        storeStaff.setStaffId(staffId);
        storeStaff.setStoreId(storeId);
        storeStaffMapper.insert(storeStaff);

    }

    /**
     * 分页查询商户下面的门店
     * @param storeDTO
     * @param pageNo
     * @param pageSize
     * @return
     */
    @Override
    public PageVO<StoreDTO> queryStoreByPage(StoreDTO storeDTO, Integer pageNo, Integer pageSize) {


        // 创建查询条件
        LambdaQueryWrapper<Store> wrapper = new LambdaQueryWrapper<>();


        if (storeDTO!=null&&storeDTO.getMerchantId()!=null){
            wrapper.eq(Store::getMerchantId,storeDTO.getMerchantId());
        }

        // 创建page对象
        Page<Store> page = new Page<Store>(pageNo,pageSize);

        // 进行分页查询
        IPage<Store> storeIPage = storeMapper.selectPage(page, wrapper);
        List<Store> records = storeIPage.getRecords();
        List<StoreDTO> storeDTOS = StoreConvert.INSTANCE.entitylist2dtolist(records);

        System.out.println(storeDTOS);

        return new PageVO<StoreDTO>(storeDTOS,storeIPage.getTotal(),pageNo,pageSize);
    }

    /**
     * 查询某个门店是否属于某个门店
     * @param storeId
     * @param merchantId
     * @return
     * @throws BusinessException
     */
    @Override
    public Boolean queryStoreInMercahnt(Long storeId, Long merchantId) throws BusinessException {
        Integer count = storeMapper.selectCount(new LambdaQueryWrapper<Store>().eq(Store::getMerchantId, merchantId).eq(Store::getId, storeId));
        return count>0;
    }


    /**
     * 根据手机号判断员工是否已在统一商户存在
     *
     * @param mobile
     * @param merchantId
     * @return
     */
    private boolean isExistStaffByMobile(String mobile, Long merchantId) {
        LambdaQueryWrapper<Staff> wrapper = new LambdaQueryWrapper<Staff>().eq(Staff::getMobile, mobile).eq(Staff::getMerchantId, merchantId);
        Integer count = staffMapper.selectCount(wrapper);
        return count > 0;
    }

    /**
     * 判断该用户是否已经存在  并且位于所属商家下
     *
     * @param userName
     * @param merchantId
     * @return
     */
    private boolean isExistStaffByUserName(String userName, Long merchantId) {
        LambdaQueryWrapper<Staff> lambdaQueryWrapper = new LambdaQueryWrapper<Staff>();
        lambdaQueryWrapper.eq(Staff::getUsername, userName).eq(Staff::getMerchantId, merchantId);
        int i = staffMapper.selectCount(lambdaQueryWrapper);
        return i > 0;
    }
}
