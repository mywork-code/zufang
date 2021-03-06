
package com.apass.zufang.service.house;
import com.apass.gfb.framework.exception.BusinessException;
import com.apass.gfb.framework.utils.BaseConstants;
import com.apass.gfb.framework.utils.DateFormatUtil;
import com.apass.zufang.domain.Response;
import com.apass.zufang.domain.common.WorkCityJd;
import com.apass.zufang.domain.dto.HouseQueryParams;
import com.apass.zufang.domain.dto.WorkCityJdParams;
import com.apass.zufang.domain.entity.Apartment;
import com.apass.zufang.domain.entity.House;
import com.apass.zufang.domain.entity.HouseImg;
import com.apass.zufang.domain.entity.HouseLocation;
import com.apass.zufang.domain.entity.HousePeizhi;
import com.apass.zufang.domain.enums.BusinessHouseTypeEnums;
import com.apass.zufang.domain.enums.CityEnums;
import com.apass.zufang.domain.enums.EditFalgEnums;
import com.apass.zufang.domain.enums.HouseAuditEnums;
import com.apass.zufang.domain.enums.IsDeleteEnums;
import com.apass.zufang.domain.vo.HouseAppSearchVo;
import com.apass.zufang.domain.vo.HouseBagVo;
import com.apass.zufang.domain.vo.HouseVo;
import com.apass.zufang.domain.vo.WorkCityJdVo;
import com.apass.zufang.mapper.zfang.ApartmentMapper;
import com.apass.zufang.mapper.zfang.HouseImgMapper;
import com.apass.zufang.mapper.zfang.HouseLocationMapper;
import com.apass.zufang.mapper.zfang.HouseMapper;
import com.apass.zufang.mapper.zfang.HousePeizhiMapper;
import com.apass.zufang.mapper.zfang.WorkCityJdMapper;
import com.apass.zufang.search.dao.HouseEsDao;
import com.apass.zufang.search.entity.HouseEs;
import com.apass.zufang.search.utils.Pinyin4jUtil;
import com.apass.zufang.utils.ResponsePageBody;
import com.apass.zufang.utils.ToolsUtils;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;
/**
 * 房源管理
 * @author Administrator
 *
 */
@Service
public class HouseService {
	private static final Logger logger = LoggerFactory.getLogger(HouseService.class);
	
	@Value("${zufang.image.uri}")
    private String imageUri;
	
	@Autowired
	private HouseMapper houseMapper;
	@Autowired
	private ApartmentMapper apartmentMapper;
	@Autowired
	private HouseLocationMapper locationMapper;
	@Autowired
	private HousePeizhiMapper peizhiMapper; 
	@Autowired
	private HouseImgMapper imgMapper;
	@Autowired
	private HouseImgService imgService;
	@Autowired
	private HousePeiZhiService peizhiService;
	@Autowired
	private HouseLocationService  locationService;
	@Autowired
	private HouseEsDao houseEsDao;
	@Autowired
    private WorkCityJdMapper cityJdMapper;
	/**
	 * readEntity
	 * @param id
	 * @return
	 */
	public House readEntity(Long id){
		return houseMapper.selectByPrimaryKey(id);
	}
	/**
	 * updateEntity
	 * @param entity
	 * @return
	 */
	@Transactional(value="transactionManager",rollbackFor = { Exception.class,RuntimeException.class})
	public Integer updateEntity(House entity){
		return houseMapper.updateByPrimaryKeySelective(entity);
	}
	/*** 房屋信息管理列表 */
	public ResponsePageBody<HouseBagVo> getHouseListExceptDelete(HouseQueryParams dto){
		ResponsePageBody<HouseBagVo> body = new ResponsePageBody<>();
		dto.setIsDelete(IsDeleteEnums.IS_DELETE_00.getCode());
		
		List<Integer> status = Lists.newArrayList();
		status.add(BusinessHouseTypeEnums.ZT_1.getCode());
		status.add(BusinessHouseTypeEnums.ZT_2.getCode());
		status.add(BusinessHouseTypeEnums.ZT_3.getCode());
		status.add(BusinessHouseTypeEnums.ZT_5.getCode());
		dto.setStatus(status);
		List<HouseBagVo> houseList = houseMapper.getHouseLists(dto);
		body.setRows(houseList);
		body.setTotal(houseMapper.getHouseListsCount(dto));
		body.setStatus(BaseConstants.CommonCode.SUCCESS_CODE);
		return body;
	}
	
	/*** 房屋信息审核管理列表*/
	public ResponsePageBody<HouseBagVo> getHouseAuditListExceptDelete(HouseQueryParams dto){
		ResponsePageBody<HouseBagVo> body = new ResponsePageBody<>();
		dto.setIsDelete(IsDeleteEnums.IS_DELETE_00.getCode());
		
		List<Integer> status = Lists.newArrayList();
		status.add(BusinessHouseTypeEnums.ZT_5.getCode());
		dto.setStatus(status);
		List<HouseBagVo> houseList = houseMapper.getHouseLists(dto);
		body.setRows(houseList);
		body.setTotal(houseMapper.getHouseListsCount(dto));
		body.setStatus(BaseConstants.CommonCode.SUCCESS_CODE);
		return body;
	}
	
	/*** 添加房屋信息* @throws BusinessException*/
	@Transactional(value="transactionManager",rollbackFor = { Exception.class,RuntimeException.class})
	public Map<String,Object> addHouse(HouseVo houseVo) throws BusinessException{
		Map<String,Object> returnMap = Maps.newHashMap();

		Apartment part = apartmentMapper.selectByPrimaryKey(houseVo.getApartmentId());
		if(null == part || StringUtils.isBlank(part.getCode())){
			throw new BusinessException("房屋所属公寓的编号不存在!");
		}
		House house = new House();
		BeanUtils.copyProperties(houseVo, house);
		house.setCode(ToolsUtils.getLastStr(part.getCode(), 2).concat(String.valueOf(ToolsUtils.fiveRandom())));
		
		/*** 添加房屋信息入库*/
		houseMapper.insertSelective(house);
		houseVo.setHouseId(house.getId());

		/*** 添加位置入库*/
		@SuppressWarnings("unused")
		Long locationId = locationService.insertOrUpdateLocation(houseVo);
		/*** 添加图片入库*/
		imgService.insertImg(houseVo);
		/*** 添加配置入库*/
		peizhiService.insertPeiZhi(houseVo);

		returnMap.put("houseId",house.getId());
		returnMap.put("houseCode",house.getCode());
		return returnMap;
	}
	
	/**
	 * 修改房屋信息
	 * @param houseVo
	 * @throws BusinessException 
	 */
	@Transactional(value="transactionManager",rollbackFor = { Exception.class,RuntimeException.class})
	public void editHouse(HouseVo houseVo) throws BusinessException{
		if(null == houseVo.getHouseId()){
			throw new BusinessException("房屋Id不能为空!");
		}
		House house = houseMapper.selectByPrimaryKey(houseVo.getHouseId());
		BeanUtils.copyProperties(houseVo, house);
		
		if(!houseVo.getApartmentId().equals(house.getApartmentId())){
			Apartment part = apartmentMapper.selectByPrimaryKey(houseVo.getApartmentId());
			if(null == part || StringUtils.isBlank(part.getCode())){
				throw new BusinessException("房屋所属公寓的编号不存在!");
			}
			house.setCode(ToolsUtils.getLastStr(part.getCode(), 2).concat(String.valueOf(ToolsUtils.fiveRandom())));
		}
		
		/** 如果是首次添加，修改，状态不变，如果是下架，修改后，状态变为5*/
		if(house.getStatus().intValue() == BusinessHouseTypeEnums.ZT_3.getCode()){
			house.setEditFlag(EditFalgEnums.EditFalg_1.getCode().byteValue());
		}
		/*** 修改房屋信息*/
		houseMapper.updateByPrimaryKeySelective(house);
		
		/*** 修改位置信息*/
		locationService.insertOrUpdateLocation(houseVo);
		
		imgService.deleteImgByHouseId(house.getId());//删除图片记录
		/*** 添加图片入库*/
		imgService.insertImg(houseVo);
		
		peizhiService.deletePeiZhiByHouseId(house.getId());//删除配置记录
		/*** 添加配置入库*/
		peizhiService.insertPeiZhi(houseVo);
	}
	
	/**
	 * 删除房屋信息
	 * @param id 房屋Id
	 * @throws BusinessException 
	 */
	@Transactional(value="transactionManager",rollbackFor = { Exception.class,RuntimeException.class})
	public void deleteHouse(String id,String updateUser) throws BusinessException{
		if(StringUtils.isBlank(id)){
			throw new BusinessException("房屋Id不能为空!");
		}
		House house = houseMapper.selectByPrimaryKey(Long.parseLong(id));
		/**如果查询房屋信息为空*/
		if(null == house){
			throw new BusinessException("房屋信息不存在！");
		}
		/***房屋状态为上架或者删除时，不允许删除*/
		if(house.getStatus().intValue() == BusinessHouseTypeEnums.ZT_4.getCode()){
			throw new BusinessException("房屋状态不允许删除!");
		}
		if(house.getStatus().intValue() == BusinessHouseTypeEnums.ZT_2.getCode()){
			throw new BusinessException("请将房源下架后重试!");
		}
		house.setStatus(BusinessHouseTypeEnums.ZT_4.getCode().byteValue());
		house.setIsDelete(IsDeleteEnums.IS_DELETE_01.getCode());
		house.setUpdatedTime(new Date());
		house.setUpdatedUser(updateUser);
		houseMapper.updateByPrimaryKeySelective(house);
		
		locationService.deleteLocationByHouseId(house.getId());//删除地址记录
		imgService.deleteImgByHouseId(house.getId());//删除图片记录
		peizhiService.deletePeiZhiByHouseId(house.getId());//删除配置记录
	}
	
	/*** 根据房屋Id,获取房屋信息*/
	public Map<String,Object> getHouseDetail(String id) throws BusinessException{
		
		Map<String,Object> values = Maps.newHashMap();
		
		House house = houseMapper.selectByPrimaryKey(Long.parseLong(id));
		if(null == house){
			throw new BusinessException("房屋信息不存在!");
		}
		
		HouseLocation location = locationMapper.getLocationByHouseId(house.getId());
		
		WorkCityJdVo locationVo = getVoByPo(location);
		
		List<HouseImg> imgs = imgMapper.getImgByRealHouseId(house.getId());
		List<HousePeizhi> peizhis = peizhiMapper.getPeiZhiByHouseId(house.getId());
		for(HouseImg img : imgs){
            if(!img.getUrl().contains("http")){
                img.setUrl(imageUri+img.getUrl());
            }
        }
		values.put("house", house);
		values.put("location",location);
		values.put("locationVo",locationVo);
		values.put("imgs",imgs);
		values.put("peizhis",peizhis);
		values.put("imgProfix",imageUri);
		return values;
	}
	
	public WorkCityJdVo getVoByPo(HouseLocation location) throws BusinessException{
		
		if(null == location){
			return null;
		}
		WorkCityJdParams provice = new WorkCityJdParams();
		provice.setProvince(location.getProvince());
		provice.setParentCode("0");
		WorkCityJd p = cityJdMapper.selectCodeByName(provice);
		if(null == p){
			logger.error("selectCodeByProvinceName is failed!");
			throw new BusinessException("查询省份编码失败!");
		}
		String cityName = location.getCity();
		String districtName = location.getDistrict();
		String streetName = location.getStreet();
		if(CityEnums.isContains(location.getProvince())){
			cityName = location.getDistrict();
			districtName = location.getStreet();
			streetName = "0";
		}
		
		WorkCityJdParams city = new WorkCityJdParams();
		city.setCity(cityName);
		city.setParentCode(p.getCode());
		WorkCityJd c = cityJdMapper.selectCodeByName(city);
		WorkCityJd d = null;
		WorkCityJd t = null;
		if(null == c){
//			logger.error("selectCodeByCityName is failed!");
//			throw new BusinessException("查询城市编码失败!");
			c = new WorkCityJd();
			d = new WorkCityJd();
			t = new WorkCityJd();

		} else{

			if(StringUtils.isEmpty(districtName)){
				//第三方对接的房源 该字段可能没有值
				d = new WorkCityJd();
			}else{

				WorkCityJdParams district = new WorkCityJdParams();
				district.setDistrict(districtName);
				district.setParentCode(c.getCode());
				d = cityJdMapper.selectCodeByName(district);
				if(null == d){
//			logger.error("selectCodeByDistrictName is failed!");
//			throw new BusinessException("查询区县编码失败!");
					//蛋壳公寓该字段无法和我们地址库对应
					d = new WorkCityJd();
				}
			}


			if(d != null){
				WorkCityJdParams street = new WorkCityJdParams();
				street.setStreet(streetName);
				street.setParentCode(d.getCode());
				t = cityJdMapper.selectCodeByName(street);
			} else{
				t = new WorkCityJd();
			}
		}

		WorkCityJdVo vo = new WorkCityJdVo();
		
		vo.setProvince(p.getProvince());
		vo.setProvinceCode(p.getCode());
		if(CityEnums.isContains(location.getProvince())){
			vo.setCity(p.getProvince());
			vo.setCityCode(p.getCode()+"zxs");
			vo.setDistrict(c.getCity());
			vo.setDistrictCode(c.getCode()+"T");
			vo.setStreet(d.getDistrict());
			vo.setStreetCode(d.getCode());
		}else{
			vo.setCity(c.getCity());
			vo.setCityCode(c.getCode());
			vo.setDistrict(d.getDistrict());
			vo.setDistrictCode(d.getCode());
			if(t != null){
				vo.setStreet(t.getTowns());
				vo.setStreetCode(t.getCode());
			}
		}
		
		return vo;
	}
	
	@Transactional(value="transactionManager",rollbackFor = { Exception.class,RuntimeException.class})
	public void auditHouse(String id,String status,String updateUser) throws BusinessException{
		if(StringUtils.isBlank(id)){
			throw new BusinessException("房屋Id不能为空!");
		}
		House house = houseMapper.selectByPrimaryKey(Long.parseLong(id));
		/**如果查询房屋信息为空*/
		if(null == house){
			throw new BusinessException("房屋信息不存在！");
		}
		if(house.getStatus().intValue() != BusinessHouseTypeEnums.ZT_5.getCode()){
			throw new BusinessException("房屋状态不允许操作!");
		}
		
		if(StringUtils.equals(HouseAuditEnums.HOUSE_AUDIT_0.getCode(), status)){
			house.setStatus(BusinessHouseTypeEnums.ZT_2.getCode().byteValue());
			house.setEditFlag(EditFalgEnums.EditFalg_0.getCode().byteValue());
			houseAddEs(house.getId());
		}else{
			if(house.getEditFlag().intValue() == EditFalgEnums.EditFalg_2.getCode()){
				house.setStatus(BusinessHouseTypeEnums.ZT_1.getCode().byteValue());
			}else{
				house.setStatus(BusinessHouseTypeEnums.ZT_3.getCode().byteValue());
			}
		}
		house.setUpdatedTime(new Date());
		house.setUpdatedUser(updateUser);
		houseMapper.updateByPrimaryKeySelective(house);
	}
	
	@Transactional(value="transactionManager",rollbackFor = { Exception.class,RuntimeException.class})
	public void downHouse(String id,String updateUser) throws BusinessException{
		if(StringUtils.isBlank(id)){
			throw new BusinessException("房屋Id不能为空!");
		}
		House house = houseMapper.selectByPrimaryKey(Long.parseLong(id));
		/**如果查询房屋信息为空*/
		if(null == house){
			throw new BusinessException("房屋信息不存在！");
		}
		/***房屋状态不为上架，不允许下架*/
		if(house.getStatus().intValue() != BusinessHouseTypeEnums.ZT_2.getCode()){
			throw new BusinessException("房屋状态不允许进行下架操作!");
		}
		house.setStatus(BusinessHouseTypeEnums.ZT_3.getCode().byteValue());
		house.setUpdatedTime(new Date());
		house.setUpdatedUser(updateUser);
		house.setDelistTime(new Date());
		houseMapper.updateByPrimaryKeySelective(house);
		houseDeleteEs(house.getId());
	}
	
	@Transactional(value="transactionManager",rollbackFor = { Exception.class,RuntimeException.class})
	public String upHouse(String id,String updateUser) throws BusinessException{
		
		if(StringUtils.isBlank(id)){
			throw new BusinessException("房屋Id不能为空!");
		}
		House house = houseMapper.selectByPrimaryKey(Long.parseLong(id));
		int status = 2;
		//如果房屋的状态为待上架，正常上架
		if(house.getStatus().intValue() == BusinessHouseTypeEnums.ZT_4.getCode()){
			return "房屋状态不允许操作！";
		}
		if(house.getStatus().intValue() == BusinessHouseTypeEnums.ZT_1.getCode() 
				|| house.getStatus().intValue() == BusinessHouseTypeEnums.ZT_3.getCode()){
			
			if((house.getStatus().intValue() == BusinessHouseTypeEnums.ZT_1.getCode() &&
					house.getEditFlag().intValue() == EditFalgEnums.EditFalg_2.getCode())
					|| (house.getStatus().intValue() == BusinessHouseTypeEnums.ZT_3.getCode() &&
							house.getEditFlag().intValue() == EditFalgEnums.EditFalg_1.getCode())){
				house.setStatus(BusinessHouseTypeEnums.ZT_5.getCode().byteValue());
				status = 1;
			}else if(house.getStatus().intValue() == BusinessHouseTypeEnums.ZT_3.getCode() &&
					house.getEditFlag().intValue() == EditFalgEnums.EditFalg_0.getCode()){
				house.setStatus(BusinessHouseTypeEnums.ZT_2.getCode().byteValue());
				house.setListTime(new Date());
				houseAddEs(house.getId());
				status = 0;
			}
			house.setUpdatedTime(new Date());
			house.setUpdatedUser(updateUser);
			houseMapper.updateByPrimaryKeySelective(house);
		}
		
		return status == 1 ? "该房源需审核，通过后自动上架，请等待审核结果!":"房屋上架成功!";
	}
	
	@Transactional(value="transactionManager",rollbackFor = { Exception.class,RuntimeException.class})
	public String upHouses(String id,String updateUser) throws BusinessException{
		
		if(StringUtils.isBlank(id)){
			throw new BusinessException("房屋Id不能为空!");
		}
		String[] ids = StringUtils.split(id,",");
		
		int waitUp = 0;//未修改的房屋信息统计
		int others = 0;//修改后的房屋信息统计
		
		for (String str : ids) {
			House house = houseMapper.selectByPrimaryKey(Long.parseLong(str));
			//如果房屋状态为删除，则跳过
			if(house.getStatus().intValue() == BusinessHouseTypeEnums.ZT_4.getCode()){
				continue;
			}
			//如果房屋的状态为待上架，正常上架
			if(house.getStatus().intValue() == BusinessHouseTypeEnums.ZT_1.getCode() 
					|| house.getStatus().intValue() == BusinessHouseTypeEnums.ZT_3.getCode()){
				
				if((house.getStatus().intValue() == BusinessHouseTypeEnums.ZT_1.getCode()
						&&house.getEditFlag().intValue() == EditFalgEnums.EditFalg_2.getCode())
						|| (house.getStatus().intValue() == BusinessHouseTypeEnums.ZT_3.getCode() &&
						house.getEditFlag().intValue() == EditFalgEnums.EditFalg_1.getCode())){
					house.setStatus(BusinessHouseTypeEnums.ZT_5.getCode().byteValue());
					others++;
				}else if(house.getStatus().intValue() == BusinessHouseTypeEnums.ZT_3.getCode() &&
						house.getEditFlag().intValue() == EditFalgEnums.EditFalg_0.getCode()){
					house.setStatus(BusinessHouseTypeEnums.ZT_2.getCode().byteValue());
					house.setListTime(new Date());
					houseAddEs(house.getId());
					waitUp++;
				}
				house.setUpdatedTime(new Date());
				house.setUpdatedUser(updateUser);
				houseMapper.updateByPrimaryKeySelective(house);
			}
		}
		
		if(others == 0 && waitUp == 0){
			return "不能重复上架！";
		}
		if(waitUp == 0){
			return "部分房源上架成功，剩余房源需审核，通过后自动上架，请等待审核结果!";
		}
		if(others == 0){
			return "房源批量上架成功！";
		}
		return "部分房源上架成功，剩余房源需审核，通过后自动上架，请等待审核结果!";
	}
	
	/*** 删除图片信息 * @throws BusinessException */
	@Transactional(value="transactionManager",rollbackFor = { Exception.class,RuntimeException.class})
	public void delPicture(String id) throws BusinessException{
		
		if(StringUtils.isBlank(id)){
			throw new BusinessException("图片Id不能为空!");
		}
		HouseImg img = imgMapper.selectByPrimaryKey(Long.parseLong(id));
		if(null == img){
			throw new BusinessException("图片信息不存在!");
		}
		img.setIsDelete(IsDeleteEnums.IS_DELETE_01.getCode());
		img.setUpdatedTime(new Date());
		imgMapper.updateByPrimaryKey(img);
	}
	/**
	 * 查询要上传ES的数据库数据
	 * @param index
	 * @param bach_size
     * @return
     */
	public List<House> selectUpGoods(int index, int bach_size) {
		HouseQueryParams dto = new HouseQueryParams();
		List<Integer> statuList = Lists.newArrayList();
		statuList.add(BusinessHouseTypeEnums.ZT_2.getCode());
		dto.setStatus(statuList);
		dto.setIsDelete(IsDeleteEnums.IS_DELETE_00.getCode());
		dto.setListTimeStr("yes");
		dto.setPageParams(bach_size,index);
		List<House> houseList = houseMapper.getHouseList(dto);

		return houseList;
	}

	/**
	 * HouseList转HoueEsList
	 * @param houses
	 * @return
     */
	public List<HouseEs> getHouseEsList(List<House> houses) {
		List<HouseEs> houseEsList = Lists.newArrayList();
		for (House h : houses) {
			Long houseId = h.getId();

			HouseEs houseEs = houseInfoToHouseEs(houseId);
			if (null == houseEs) {
				continue;
			}
			logger.info("houseEsList add houseId {} ...", houseEs.getId());
			houseEsList.add(houseEs);
		}
		logger.info("houseEsList add houseSize {} ...", houseEsList.size());
		return houseEsList;
	}

	/**
	 * House转HouseEs
	 * @param houseId:房源id
	 * @return
	 */
	private HouseEs houseInfoToHouseEs(Long houseId) {
		House h = houseMapper.selectByPrimaryKey(houseId);
		HouseEs houseEs = new HouseEs();
		try{
			if(h!=null) {
				houseEs.setId(Integer.valueOf(h.getId() + ""));
				houseEs.setHouseId(h.getId());
				houseEs.setCode(h.getCode());
				houseEs.setApartmentId(h.getApartmentId());
				houseEs.setType(h.getType());
				houseEs.setSortNo(h.getSortNo());
				houseEs.setRentType(h.getRentType());
				houseEs.setCommunityName(h.getCommunityName());
				houseEs.setCommunityNamePinyin(Pinyin4jUtil.converterToSpell(h.getCommunityName()));
				houseEs.setAcreage(h.getAcreage());
				houseEs.setRoom(h.getRoom());
				houseEs.setHouseTitle(h.getTitle());
				houseEs.setHouseTitlePinyin(Pinyin4jUtil.converterToSpell(h.getTitle()));
				houseEs.setHall(h.getHall());
				houseEs.setWei(h.getWei());
				houseEs.setFloor(h.getFloor());
				houseEs.setTotalFloor(h.getTotalFloor());
				houseEs.setLiftType(h.getLiftType());
				houseEs.setRentAmt(h.getRentAmt());
				houseEs.setZujinType(h.getZujinType());
				houseEs.setChaoxiang(h.getChaoxiang());
				houseEs.setZhuangxiu(h.getZhuangxiu());
				houseEs.setStatus(h.getStatus());
				houseEs.setListTime(h.getListTime());
				String listTimeStr = DateFormatUtil.dateToString(h.getListTime(), DateFormatUtil.YYYY_MM_DD_HH_MM_SS);
				houseEs.setListTimeStr(listTimeStr);
				houseEs.setDelistTime(h.getDelistTime());
				String delistStr = DateFormatUtil.dateToString(h.getDelistTime(), DateFormatUtil.YYYY_MM_DD_HH_MM_SS);
				houseEs.setDelistTimeStr(delistStr);
				houseEs.setCreatedTime(h.getCreatedTime());
				houseEs.setCreatedTimeStr(DateFormatUtil.dateToString(h.getCreatedTime(), DateFormatUtil.YYYY_MM_DD_HH_MM_SS));
				houseEs.setUpdatedTime(h.getUpdatedTime());
				houseEs.setUpdatedTimeStr(DateFormatUtil.dateToString(h.getUpdatedTime(), DateFormatUtil.YYYY_MM_DD_HH_MM_SS));
				houseEs.setCreatedUser(h.getCreatedUser());
				houseEs.setUpdatedUser(h.getUpdatedUser());
				houseEs.setIsDelete(h.getIsDelete());
				houseEs.setPageView(h.getPageView());
				houseEs.setHousekeeperTel(h.getHousekeeperTel());
				houseEs.setTotalDoors(h.getTotalDoors());
				houseEs.setHezuChaoxiang(h.getHezuChaoxiang());
				houseEs.setHezuResource(h.getHezuResource());
				houseEs.setAcreage(h.getAcreage());
				houseEs.setRoomAcreage(h.getRoomAcreage());

				//增加价格区间标记priceFlag
				int priceFlag = 6;
				if (h.getRentAmt().compareTo(new BigDecimal(1500)) < 0 && h.getRentAmt().compareTo(new BigDecimal(0))>0) {
					priceFlag = 1;
				} else if (h.getRentAmt().compareTo(new BigDecimal(1500)) >= 0 && h.getRentAmt().compareTo(new BigDecimal(2500)) <= 0) {
					priceFlag = 2;
				} else if (h.getRentAmt().compareTo(new BigDecimal(2501)) >= 0 && h.getRentAmt().compareTo(new BigDecimal(3500)) <= 0) {
					priceFlag = 3;
				} else if (h.getRentAmt().compareTo(new BigDecimal(3501)) >= 0 && h.getRentAmt().compareTo(new BigDecimal(5500)) <= 0) {
					priceFlag = 4;
				} else if (h.getRentAmt().compareTo(new BigDecimal(5501)) >= 0) {
					priceFlag = 5;
				}
				houseEs.setPriceFlag(priceFlag);
			}
			Apartment apartment = apartmentMapper.selectByPrimaryKey(h.getApartmentId());
			if(apartment != null){
				houseEs.setCompanyName(apartment.getCompanyName());
				houseEs.setApartmentName(apartment.getName());
				houseEs.setApartmentNamePinyin(Pinyin4jUtil.converterToSpell(apartment.getName()));
			}

			HouseLocation hLocation = locationMapper.getLocationByHouseId(houseId);
			if(hLocation!=null){
				houseEs.setProvince(hLocation.getProvince());
				houseEs.setCity(hLocation.getCity());
				houseEs.setDistrict(hLocation.getDistrict());
				houseEs.setStreet(hLocation.getStreet());
				houseEs.setDetailAddr(hLocation.getDetailAddr());
				houseEs.setDetailAddrPinyin(Pinyin4jUtil.converterToSpell(hLocation.getDetailAddr()));
				houseEs.setLongitude(hLocation.getLongitude());
				houseEs.setLatitude(hLocation.getLatitude());
				//左:纬度,右:经度
				houseEs.setLocation(hLocation.getLatitude()+","+hLocation.getLongitude());
			}
			List<HouseImg> hImgs = imgMapper.getImgByRealHouseId(houseId);
			if(CollectionUtils.isNotEmpty(hImgs)){
				StringBuffer sb = new StringBuffer();
				for(int i=0; i<hImgs.size(); i++){
					if(hImgs.get(i)!=null){
						if(i<(hImgs.size()-1)){
							sb = sb.append(hImgs.get(i).getUrl()+",");
						}else {
							sb = sb.append(hImgs.get(i).getUrl());
						}
					}
				}
				houseEs.setUrl(sb.toString());
			}
			List<HousePeizhi> peiZhiByHouses = peizhiMapper.getPeiZhiByHouseId(houseId);
			if(CollectionUtils.isNotEmpty(peiZhiByHouses)){
				StringBuffer sb = new StringBuffer();
				for(int i=0; i<peiZhiByHouses.size(); i++){
					if(peiZhiByHouses.get(i)!=null){
						if(i<(peiZhiByHouses.size()-1)){
							sb = sb.append(peiZhiByHouses.get(i).getName()+",");
						}else {
							sb = sb.append(peiZhiByHouses.get(i).getName());
						}
					}
				}
				houseEs.setConfigName(sb.toString());
				houseEs.setConfigNamePinyin(Pinyin4jUtil.converterToSpell(sb.toString()));
			}
			return  houseEs;
		}catch (Exception e){
			logger.error("--------houseInfoToHouseEs Exception------{}",e);
		}


		return null;
	}

	public void houseAddEs(Long houseId){
		HouseEs es = houseInfoToHouseEs(houseId);
		houseEsDao.add(es);
	}

	public void houseDeleteEs(Long houseId){
		HouseEs es = houseInfoToHouseEs(houseId);
		houseEsDao.delete(es);
	}

	public void houseUpdateEs(Long houseId){
		HouseEs es = houseInfoToHouseEs(houseId);
		houseEsDao.update(es);
	}
	public List<HouseAppSearchVo> queryHouseBasicEntityByEntity(HouseQueryParams houseQueryParams) {
		return houseMapper.queryHouseBasicEntityByEntity(houseQueryParams);
	}
	@Transactional(value="transactionManager",rollbackFor = { Exception.class,RuntimeException.class})
	public Response upHouseForDanke(String[] strarr, String user) throws BusinessException {
		StringBuffer sb = new StringBuffer();
		String message = null;
    	for(String str : strarr){
    		if(StringUtils.isNotBlank(str)){
    			sb.append("房源信息：").append(str).append(",").append("上架详情：");
    			message = upHouse(str, user);
    			if(!message.endsWith("成功!")){
    				auditHouse(str, "0", user);
    				sb.append("房屋上架成功!");
    			}else{
    				sb.append(message);
    			}
    		}
    	}
    	return Response.success(sb.toString());
	}
}
