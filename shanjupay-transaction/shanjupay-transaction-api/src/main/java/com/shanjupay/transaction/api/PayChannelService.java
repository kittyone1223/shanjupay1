package com.shanjupay.transaction.api;

import com.shanjupay.common.domain.BusinessException;
import com.shanjupay.transaction.api.dto.PayChannelDTO;
import com.shanjupay.transaction.api.dto.PayChannelParamDTO;
import com.shanjupay.transaction.api.dto.PlatformChannelDTO;

import java.util.List;

public interface PayChannelService {
    /**
     * 获取平台服务中心
     *
     * @return
     * @throws BusinessException
     */
    List<PlatformChannelDTO> queryPlatformChannel() throws BusinessException;


    /**
     * 应用绑定交易对应的服务
     *
     * @param appId
     * @param platformChannelCodes
     * @throws BusinessException
     */
    void BindPlatformChannelForApp(String appId, String platformChannelCodes) throws BusinessException;


    /**
     * 应用是否绑定了某个服务类型  状态
     *
     * @param appId
     * @param platformChannel
     * @return
     * @throws BusinessException
     */
    int queryAppBindPlatformChannelStatus(String appId, String platformChannel) throws BusinessException;


    /**
     * 根据平台服务的PLATFORM_CHANNEL   来获取PLATFORM_CHANNEL
     *
     * @param platformChannelCode
     * @return
     */
    List<PayChannelDTO> queryPayChannelByPlatformChannel(String platformChannelCode);


    /**
     * 保存支付渠道参数
     *
     * @param payChannelParamDTO
     */
    void savePayChannelParam(PayChannelParamDTO payChannelParamDTO);


    /**
     * 获取指定应用指定服务类型下所包含的原始支付渠道参数列表
     *
     * @param appId           应用id
     * @param platformChannel 服务类型
     * @return
     */
    List<PayChannelParamDTO> queryPayChannelParamByAppAndPlatform(String appId, String platformChannel) throws BusinessException;


    /*** 获取指定应用指定服务类型下所包含的某个原始支付参数 * @param appId * @param platformChannel * @param payChannel * @return * @throws BusinessException */
    PayChannelParamDTO queryParamByAppPlatformAndPayChannel(String appId, String platformChannel, String payChannel) throws BusinessException;
}
