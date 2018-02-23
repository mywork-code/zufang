package com.apass.zufang.mapper.ajp.searchhistory;

import java.util.List;

import com.apass.gfb.framework.annotation.MyBatisRepository;
import com.apass.gfb.framework.mybatis.support.BaseMybatisRepository;
import com.apass.gfb.framework.utils.CommonUtils;
import com.apass.zufang.domain.ajp.entity.ZfangSearchEntity;

/**
 * 
 * 搜索历史
 * @author ls
 * @update 2018-02-09 11:02
 *
 */
@MyBatisRepository
public class SearchHistoryRepository extends BaseMybatisRepository<ZfangSearchEntity, Long>  {
	/**
	 * 设备号查询
	 * @param deviceId
	 * @return
	 */
	public List<String> queryDeviceIdHistory(String deviceId) {
		return getSqlSession().selectOne(getSQL("queryDeviceIdHistory"), deviceId);
	}
	/**
	 * 用户id查询
	 * @param customerId
	 * @return
	 */
	public List<String> queryCustomerIdHistory(String userId) {
		return getSqlSession().selectOne(getSQL("queryCustomerIdHistory"), userId);
	}
	
	/**
	 * 设备ID删除
	 * @param deviceId
	 * @return
	 */
	public Object deleteDeviceIdHistory(String deviceId) {
		return getSqlSession().selectOne(getSQL("deleteDeviceIdHistory"), deviceId);
		
	}
	
	/**
	 * 用户id删除
	 * @param customerId
	 * @return
	 */
	public Object deleteCustomerIdHistory(String customerId) {
		return getSqlSession().selectOne(getSQL("deleteCustomerIdHistory"), customerId);
	}
	
	
}
