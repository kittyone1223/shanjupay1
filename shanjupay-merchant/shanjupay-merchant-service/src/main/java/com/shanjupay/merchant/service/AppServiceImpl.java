package com.shanjupay.merchant.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.shanjupay.common.domain.BusinessException;
import com.shanjupay.common.domain.CommonErrorCode;
import com.shanjupay.common.util.RandomUuidUtil;
import com.shanjupay.merchant.api.AppService;
import com.shanjupay.merchant.api.dto.AppDTO;
import com.shanjupay.merchant.convert.AppCovert;
import com.shanjupay.merchant.entity.App;
import com.shanjupay.merchant.entity.Merchant;
import com.shanjupay.merchant.mapper.AppMapper;
import com.shanjupay.merchant.mapper.MerchantMapper;
import org.apache.dubbo.config.annotation.Service;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.UUID;


@Service
public class AppServiceImpl implements AppService {

    @Autowired
    private AppMapper appMapper;

    @Autowired
    private MerchantMapper merchantMapper;

    @Override
    public AppDTO createApp(Long merchantId, AppDTO appDTO) throws BusinessException {

        Merchant merchant = merchantMapper.selectById(merchantId);
        if (merchant==null||merchantId==null){
            throw new BusinessException(CommonErrorCode.E_200002);
        }
        if (!"2".equals(merchant.getAuditStatus())){
            throw new BusinessException(CommonErrorCode.E_200003);
        }

        if (isExistAppName(appDTO.getAppName())){
            throw new BusinessException(CommonErrorCode.E_200004);
        }

        appDTO.setAppId(RandomUuidUtil.getUUID());
        appDTO.setMerchantId(merchant.getId());
        App app = AppCovert.INSTANCE.dto2entity(appDTO);
        appMapper.insert(app);
        return AppCovert.INSTANCE.entity2dto(app);

    }

    @Override
    public List<AppDTO> queryApplyMerchant(Long merchantId) throws BusinessException {
        List<App> apps = appMapper.selectList(new LambdaQueryWrapper<App>().eq(App::getMerchantId, merchantId));
        List<AppDTO> appDTOS = AppCovert.INSTANCE.listentity2dtolist(apps);
        return appDTOS;
    }

    @Override
    public AppDTO getAppById(String appId) throws BusinessException {
        App app = appMapper.selectOne(new LambdaQueryWrapper<App>().eq(App::getAppId, appId));
        AppDTO appDTO = AppCovert.INSTANCE.entity2dto(app);
        return appDTO;
    }

    @Override
    public Boolean queryAppInMercahnt(String appId, Long merchantId) {
        LambdaQueryWrapper<App> wrapper = new LambdaQueryWrapper<App>();
        Integer count = appMapper.selectCount(wrapper.eq(App::getAppId, appId).eq(App::getMerchantId, merchantId));
        return count>0;
    }

    /**
     * 校验用户名是否已被使用
     * @param appName
     * @return
     */
    private Boolean isExistAppName(String appName){
        Integer count = appMapper.selectCount(new QueryWrapper<App>().eq("APP_NAME", appName));
        if (count>0){
            return true;
        }
        return false;
    }
}
