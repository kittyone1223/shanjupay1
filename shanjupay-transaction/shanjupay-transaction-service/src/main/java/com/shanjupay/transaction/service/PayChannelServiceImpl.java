package com.shanjupay.transaction.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.shanjupay.common.cache.Cache;
import com.shanjupay.common.domain.BusinessException;
import com.shanjupay.common.domain.CommonErrorCode;
import com.shanjupay.common.util.RedisUtil;
import com.shanjupay.transaction.api.PayChannelService;
import com.shanjupay.transaction.api.dto.PayChannelDTO;
import com.shanjupay.transaction.api.dto.PayChannelParamDTO;
import com.shanjupay.transaction.api.dto.PlatformChannelDTO;
import com.shanjupay.transaction.convert.PayChannelParamConvert;
import com.shanjupay.transaction.convert.PlatformChannelConvert;
import com.shanjupay.transaction.entity.AppPlatformChannel;
import com.shanjupay.transaction.entity.PayChannelParam;
import com.shanjupay.transaction.entity.PlatformChannel;
import com.shanjupay.transaction.mapper.AppPlatformChannelMapper;
import com.shanjupay.transaction.mapper.PayChannelMapper;
import com.shanjupay.transaction.mapper.PayChannelParamMapper;
import com.shanjupay.transaction.mapper.PlatformChannelMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.dubbo.config.annotation.Service;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;


@Service
@Slf4j
public class PayChannelServiceImpl implements PayChannelService {


    @Autowired
    private PlatformChannelMapper platformChannelMapper;

    @Autowired
    private AppPlatformChannelMapper appPlatformChannelMapper;

    @Autowired
    private PayChannelMapper payChannelMapper;


    @Autowired
    private PayChannelParamMapper payChannelParamMapper;

    @Autowired
    private Cache cache;

    @Override
    public List<PlatformChannelDTO> queryPlatformChannel() throws BusinessException {
        List<PlatformChannel> platformChannels = platformChannelMapper.selectList(null);
        List<PlatformChannelDTO> platformChannelDTOS = PlatformChannelConvert.INSTANCE.listentity2listdto(platformChannels);
        return platformChannelDTOS;
    }

    @Override
    public void BindPlatformChannelForApp(String appId, String platformChannelCodes) throws BusinessException {

        AppPlatformChannel appPlatformChannel1 = appPlatformChannelMapper.selectOne(new LambdaQueryWrapper<AppPlatformChannel>().eq(AppPlatformChannel::getAppId, appId).eq(AppPlatformChannel::getPlatformChannel, platformChannelCodes));
        if (appPlatformChannel1==null){
            AppPlatformChannel appPlatformChannel = new AppPlatformChannel();
            appPlatformChannel.setAppId(appId);
            appPlatformChannel.setPlatformChannel(platformChannelCodes);
            appPlatformChannelMapper.insert(appPlatformChannel);
        }else {
            throw new BusinessException(CommonErrorCode.E_300010);
        }



    }

    @Override
    public int queryAppBindPlatformChannelStatus(String appId, String platformChannel) throws BusinessException {
        Integer count = appPlatformChannelMapper.selectCount(new LambdaQueryWrapper<AppPlatformChannel>().eq(AppPlatformChannel::getAppId, appId).eq(AppPlatformChannel::getPlatformChannel, platformChannel));
        if (count>0){
            log.info("该{appId}已经绑定了对应的服务",appId);
            System.out.println("该应用已经绑定了对应的服务");
            return 1;
        }
        System.out.println("应用未绑定当前服务");
        log.info("该{appId}已经绑定了对应的服务",appId);
        return 0;
    }

    @Override
    public List<PayChannelDTO> queryPayChannelByPlatformChannel(String platformChannelCode) {
        return payChannelMapper.selectPayChannelByPlatformChannel(platformChannelCode);
    }

    @Override
    public void savePayChannelParam(PayChannelParamDTO payChannelParamDTO) {

        if (payChannelParamDTO==null|| StringUtils.isEmpty(payChannelParamDTO.getAppId())||StringUtils.isEmpty(payChannelParamDTO.getPlatformChannelCode())||StringUtils.isBlank(payChannelParamDTO.getPayChannel())){

            throw new BusinessException(CommonErrorCode.E_300009);
        }

        // 根据appId和服务类型查询应用于服务类型绑定id
        // 查看当前服务是否进行    appId（应用）和服务类型进行绑定
        Long appPlatformChannelId = selectIdByAppPlatformChannel(payChannelParamDTO.getAppId(), payChannelParamDTO.getPlatformChannelCode());
        if (appPlatformChannelId==null){
            //应用未绑定该服务类型不可进行支付渠道参数配置
            throw new BusinessException(CommonErrorCode.E_300010);
        }

        //根据平台服务和appid查询对应PayChannelParam的所得APP_PLATFORM_CHANNEL_ID   查出对应的PayChannelParam
        PayChannelParam payChannelParam = payChannelParamMapper.selectOne(new LambdaQueryWrapper<PayChannelParam>().eq(PayChannelParam::getAppPlatformChannelId, appPlatformChannelId)
                                                                            .eq(PayChannelParam::getPayChannel,payChannelParamDTO.getPayChannel())
                            );
        if (payChannelParam!=null){
            payChannelParam.setChannelName(payChannelParamDTO.getChannelName());
            payChannelParam.setParam(payChannelParamDTO.getParam());
            PayChannelParam entity = PayChannelParamConvert.INSTANCE.dto2entity(payChannelParamDTO);
            payChannelParamMapper.updateById(entity);
            updateCache(payChannelParamDTO.getAppId(),payChannelParamDTO.getPayChannel());
        }else {
            PayChannelParam entity = PayChannelParamConvert.INSTANCE.dto2entity(payChannelParamDTO);
            entity.setAppPlatformChannelId(appPlatformChannelId);
            payChannelParamMapper.insert(entity);
            updateCache(payChannelParamDTO.getAppId(),payChannelParamDTO.getPayChannel());
        }


//
    }

    /**
     * 查询
     * @param appId           应用id
     * @param platformChannel 服务类型
     * @return
     * @throws BusinessException
     */
    @Override
    public List<PayChannelParamDTO> queryPayChannelParamByAppAndPlatform(String appId, String platformChannel) throws BusinessException {


        // 从缓存中查询
        //1.key的构建
        String redisKey = RedisUtil.keyBuilder(appId, platformChannel);
        Boolean exists = cache.exists(redisKey);
        if (exists){
            // 从缓存redis中取出key对应的values
            String value = cache.get(redisKey);
            // 将value转化成对象
            List<PayChannelParamDTO> payChannelParams = JSONObject.parseArray(value, PayChannelParamDTO.class);
            return payChannelParams;
        }

        // 查出应用id和服务类型代码在app_platform_channel的主键
        Long appPlatformChannelId = selectIdByAppPlatformChannel(appId, platformChannel);
        // 根据appPlatformChannelId  从pay_channel_param查询所有支付参数
        List<PayChannelParam> payChannelParams = payChannelParamMapper.selectList(new LambdaQueryWrapper<PayChannelParam>().eq(PayChannelParam::getAppPlatformChannelId, appPlatformChannelId));
        List<PayChannelParamDTO> payChannelParamDTOS = PayChannelParamConvert.INSTANCE.listentity2listdto(payChannelParams);
        // 首次查询加入到redis
        updateCache(appId,platformChannel);
        return payChannelParamDTOS;
    }

    @Override
    public PayChannelParamDTO queryParamByAppPlatformAndPayChannel(String appId, String platformChannel, String payChannel) throws BusinessException {
        // 查出应用id和服务类型代码在app_platform_channel的主键
        Long appPlatformChannelId = selectIdByAppPlatformChannel(appId, platformChannel);
        List<PayChannelParam> payChannelParams = payChannelParamMapper.selectList(new LambdaQueryWrapper<PayChannelParam>().eq(PayChannelParam::getAppPlatformChannelId, appPlatformChannelId));
        for (PayChannelParam payChannelParam : payChannelParams) {
            if (payChannelParam.getPayChannel().equals(payChannel)){
                return PayChannelParamConvert.INSTANCE.entity2dto(payChannelParam);
            }
        }
        return null;
    }

    private Long selectIdByAppPlatformChannel(String appId,String platformChannelCode){
        AppPlatformChannel appPlatformChannel = appPlatformChannelMapper.selectOne(new LambdaQueryWrapper<AppPlatformChannel>().eq(AppPlatformChannel::getAppId, appId).eq(AppPlatformChannel::getPlatformChannel, platformChannelCode));
        if (appPlatformChannel!=null){
            return appPlatformChannel.getId();
        }
        return null;
    }

    /**
     * 更新缓存
     * @param appId
     * @param platformChannel
     */
    private void updateCache(String appId,String platformChannel) {
        //处理缓存redis
        //   key的构建 如：SJ_PAY_PARAM:b910da455bc84514b324656e1088320b:shanju_c2b
        String redisKey = RedisUtil.keyBuilder(appId, platformChannel);
        // 2.查询redis  检查key是否存在
        Boolean exists = cache.exists(redisKey);
        if (exists) {
            cache.del(redisKey);
        }

        //3.从数据库查询应用的服务类型对应的实际支付参数  并重新存入缓存
        //List<PayChannelParamDTO> payChannelParamDTOS = queryPayChannelParamByAppAndPlatform(appId, platformChannel);
        // 查出应用id和服务类型代码在app_platform_channel的主键
        Long appPlatformChannelId = selectIdByAppPlatformChannel(appId, platformChannel);
        // 根据appPlatformChannelId  从pay_channel_param查询所有支付参数
        List<PayChannelParam> payChannelParams = payChannelParamMapper.selectList(new LambdaQueryWrapper<PayChannelParam>().eq(PayChannelParam::getAppPlatformChannelId, appPlatformChannelId));
        List<PayChannelParamDTO> payChannelParamDTOS = PayChannelParamConvert.INSTANCE.listentity2listdto(payChannelParams);


        if (payChannelParamDTOS != null) {
            // 存入缓存
            cache.set(redisKey, JSON.toJSON(payChannelParamDTOS).toString());
        }

    }
}
