package com.apass.zufang.mapper.zfang;
import java.util.HashMap;
import java.util.List;

import com.apass.gfb.framework.mybatis.GenericMapper;
import com.apass.zufang.domain.dto.HouseAppointmentQueryParams;
import com.apass.zufang.domain.dto.HouseQueryParams;
import com.apass.zufang.domain.entity.House;
import com.apass.zufang.domain.vo.HouseAppSearchVo;
import com.apass.zufang.domain.vo.HouseAppointmentVo;
import com.apass.zufang.domain.vo.HouseBagVo;
import com.apass.zufang.domain.vo.HouseVo;
/**
 * Created by DELL on 2018/2/7.
 */
public interface HouseMapper extends GenericMapper<House,Long> {
	/**
	 * es初始化列表集合查询
	 * getHouseList
	 * @param entity
	 * @return
	 */
	public List<House> getHouseList(HouseQueryParams entity);

	/**
	 *  es初始化数量查询
	 * getHouseListCount
	 * @param entity
	 * @return
	 */
	public Integer getHouseListCount(HouseQueryParams entity);
	
	/**
	 * 房源信息管理
	 * @param entity
	 * @return
	 */
	public List<HouseBagVo> getHouseLists(HouseQueryParams entity);
	
	/**
	 * 房源信息管理数量查询
	 * @param entity
	 * @return
	 */
	public Integer getHouseListsCount(HouseQueryParams entity);
	/**
	 * 品牌公寓热门房源查询
	 * @param entity
	 * @return
	 */
	public List<HouseVo> getHotHouseList(HouseQueryParams entity);
	/**
	 * init城市
	 * @return 
	 */
	public List<HouseVo> initCity();
	/**
	 * 查询房源List
	 * @param paramMap
	 * @return
	 */
	public List<HouseVo> getHouseById(HashMap<String, String> paramMap);
	/**
	 * getHouseListForPhoneAppointment
	 * @param entity
	 * @return
	 */
	public List<HouseAppointmentVo> getHouseListForPhoneAppointment(HouseAppointmentQueryParams entity);

	List<HouseAppSearchVo> queryHouseBasicEntityByEntity(HouseQueryParams houseQueryParams);
}