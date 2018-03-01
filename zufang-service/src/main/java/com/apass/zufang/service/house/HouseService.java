package com.apass.zufang.service.house;

import java.io.File;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.apass.gfb.framework.exception.BusinessException;
import com.apass.gfb.framework.utils.BaseConstants;
import com.apass.gfb.framework.utils.DateFormatUtil;
import com.apass.zufang.domain.dto.HouseQueryParams;
import com.apass.zufang.domain.entity.Apartment;
import com.apass.zufang.domain.entity.House;
import com.apass.zufang.domain.entity.HouseImg;
import com.apass.zufang.domain.entity.HouseLocation;
import com.apass.zufang.domain.entity.HousePeizhi;
import com.apass.zufang.domain.enums.BusinessHouseTypeEnums;
import com.apass.zufang.domain.enums.HouseAuditEnums;
import com.apass.zufang.domain.enums.IsDeleteEnums;
import com.apass.zufang.domain.vo.HouseBagVo;
import com.apass.zufang.domain.vo.HouseVo;
import com.apass.zufang.mapper.zfang.ApartmentMapper;
import com.apass.zufang.mapper.zfang.HouseImgMapper;
import com.apass.zufang.mapper.zfang.HouseLocationMapper;
import com.apass.zufang.mapper.zfang.HouseMapper;
import com.apass.zufang.mapper.zfang.HousePeizhiMapper;
import com.apass.zufang.search.dao.HouseEsDao;
import com.apass.zufang.search.entity.HouseEs;
import com.apass.zufang.search.utils.Pinyin4jUtil;
import com.apass.zufang.utils.ResponsePageBody;
import com.apass.zufang.utils.ToolsUtils;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * 房源管理
 * @author Administrator
 *
 */
@Service
public class HouseService {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(HouseService.class);
	
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

	/*** 房屋信息管理列表 */
	public ResponsePageBody<HouseBagVo> getHouseListExceptDelete(HouseQueryParams dto){
		ResponsePageBody<HouseBagVo> body = new ResponsePageBody<>();
		dto.setIsDelete(IsDeleteEnums.IS_DELETE_00.getCode());
		
		List<Integer> status = Lists.newArrayList();
		status.add(BusinessHouseTypeEnums.ZT_WSHAGNJIA_1.getCode());
		status.add(BusinessHouseTypeEnums.ZT_SHAGNJIA_2.getCode());
		status.add(BusinessHouseTypeEnums.ZT_XIAJIA_3.getCode());
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
		status.add(BusinessHouseTypeEnums.ZT_XIUGAI_5.getCode());
		dto.setStatus(status);
		List<HouseBagVo> houseList = houseMapper.getHouseLists(dto);
		body.setRows(houseList);
		body.setTotal(houseMapper.getHouseListsCount(dto));
		body.setStatus(BaseConstants.CommonCode.SUCCESS_CODE);
		return body;
	}
	
	/*** 添加房屋信息* @throws BusinessException*/
	@Transactional(value="transactionManager",rollbackFor = { Exception.class,RuntimeException.class})
	public void addHouse(HouseVo houseVo) throws BusinessException{
		
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
		locationService.insertOrUpdateLocation(houseVo);
		/*** 添加图片入库*/
		imgService.insertImg(houseVo);
		/*** 添加配置入库*/
		peizhiService.insertPeiZhi(houseVo);
	}
	
	/**
	 * 修改房屋信息
	 * @param houseVo
	 * @throws BusinessException 
	 */
	@Transactional(rollbackFor = { Exception.class,RuntimeException.class})
	public void editHouse(HouseVo houseVo) throws BusinessException{
		if(null == houseVo.getHouseId()){
			throw new BusinessException("房屋Id不能为空!");
		}
		Apartment part = apartmentMapper.selectByPrimaryKey(houseVo.getApartmentId());
		if(null == part || StringUtils.isBlank(part.getCode())){
			throw new BusinessException("房屋所属公寓的编号不存在!");
		}
		House house = houseMapper.selectByPrimaryKey(houseVo.getHouseId());
		BeanUtils.copyProperties(houseVo, house);
		house.setCode(ToolsUtils.getLastStr(part.getCode(), 2).concat(String.valueOf(ToolsUtils.fiveRandom())));
		
		/** 如果是首次添加，修改，状态不变，如果是下架，修改后，状态变为5*/
		if(house.getStatus().intValue() == BusinessHouseTypeEnums.ZT_XIAJIA_3.getCode()){
			house.setStatus(BusinessHouseTypeEnums.ZT_XIUGAI_5.getCode().byteValue());
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
	@Transactional(rollbackFor = { Exception.class,RuntimeException.class})
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
		if(house.getStatus().intValue() == BusinessHouseTypeEnums.ZT_SHANGCHU_4.getCode()){
			throw new BusinessException("房屋状态不允许删除!");
		}
		if(house.getStatus().intValue() == BusinessHouseTypeEnums.ZT_SHAGNJIA_2.getCode()){
			throw new BusinessException("请将房源下架后重试!");
		}
		house.setStatus(BusinessHouseTypeEnums.ZT_SHANGCHU_4.getCode().byteValue());
		house.setIsDelete(IsDeleteEnums.IS_DELETE_01.getCode());
		house.setUpdatedTime(new Date());
		house.setUpdatedUser(updateUser);
		houseMapper.updateByPrimaryKeySelective(house);
	}
	
	/*** 根据房屋Id,获取房屋信息*/
	public Map<String,Object> getHouseDetail(String id) throws BusinessException{
		
		Map<String,Object> values = Maps.newHashMap();
		
		House house = houseMapper.selectByPrimaryKey(Long.parseLong(id));
		if(null == house){
			throw new BusinessException("房屋信息不存在!");
		}
		
		HouseLocation location = locationMapper.getLocationByHouseId(house.getId());
		
		List<HouseImg> imgs = imgMapper.getImgByRealHouseId(house.getId());
		
		List<HousePeizhi> peizhis = peizhiMapper.getPeiZhiByHouseId(house.getId());
		
		values.put("house", house);
		values.put("location",location);
		values.put("imgs",imgs);
		values.put("peizhis",peizhis);
		
		return values;
	}
	
	@Transactional(rollbackFor = { Exception.class,RuntimeException.class})
	public void auditHouse(String id,String status,String updateUser) throws BusinessException{
		if(StringUtils.isBlank(id)){
			throw new BusinessException("房屋Id不能为空!");
		}
		House house = houseMapper.selectByPrimaryKey(Long.parseLong(id));
		/**如果查询房屋信息为空*/
		if(null == house){
			throw new BusinessException("房屋信息不存在！");
		}
		if(house.getStatus().intValue() != BusinessHouseTypeEnums.ZT_XIUGAI_5.getCode()){
			throw new BusinessException("房屋状态不允许操作!");
		}
		if(StringUtils.equals(HouseAuditEnums.HOUSE_AUDIT_0.getCode(), status)){
			house.setStatus(BusinessHouseTypeEnums.ZT_SHAGNJIA_2.getCode().byteValue());
			houseAddEs(house.getId());
		}else{
			house.setStatus(BusinessHouseTypeEnums.ZT_XIAJIA_3.getCode().byteValue());
		}
		house.setUpdatedTime(new Date());
		house.setUpdatedUser(updateUser);
		houseMapper.updateByPrimaryKeySelective(house);
	}
	
	@Transactional(rollbackFor = { Exception.class,RuntimeException.class})
	public void downHouse(String id,String updateUser) throws BusinessException{
		if(StringUtils.isBlank(id)){
			throw new BusinessException("房屋Id不能为空!");
		}
		House house = houseMapper.selectByPrimaryKey(Long.parseLong(id));
		/**如果查询房屋信息为空*/
		if(null == house){
			throw new BusinessException("房屋信息不存在！");
		}
		/***房屋状态为上架或者删除时，不允许删除*/
		if(house.getStatus().intValue() == BusinessHouseTypeEnums.ZT_SHANGCHU_4.getCode()){
			throw new BusinessException("房屋状态不允许进行上下架操作!");
		}
		
		if(house.getStatus().intValue() == BusinessHouseTypeEnums.ZT_SHAGNJIA_2.getCode()){
			house.setStatus(BusinessHouseTypeEnums.ZT_XIAJIA_3.getCode().byteValue());
			house.setUpdatedTime(new Date());
			house.setUpdatedUser(updateUser);
			houseMapper.updateByPrimaryKeySelective(house);
			houseDeleteEs(house.getId());
		}
	}
	
	@Transactional(rollbackFor = { Exception.class,RuntimeException.class})
	public String upHouse(String id,String updateUser) throws BusinessException{
		
		if(StringUtils.isBlank(id)){
			throw new BusinessException("房屋Id不能为空!");
		}
		House house = houseMapper.selectByPrimaryKey(Long.parseLong(id));
		int status = house.getStatus().intValue();
		//如果房屋的状态为待上架，正常上架
		if(house.getStatus().intValue() == BusinessHouseTypeEnums.ZT_SHANGCHU_4.getCode()){
			return "房屋状态不允许操作！";
		}
		if(house.getStatus().intValue() == BusinessHouseTypeEnums.ZT_WSHAGNJIA_1.getCode() 
				|| house.getStatus().intValue() == BusinessHouseTypeEnums.ZT_XIAJIA_3.getCode()){
			if(house.getStatus().intValue() == BusinessHouseTypeEnums.ZT_WSHAGNJIA_1.getCode()){
				house.setStatus(BusinessHouseTypeEnums.ZT_XIUGAI_5.getCode().byteValue());
			}else{
				house.setStatus(BusinessHouseTypeEnums.ZT_SHAGNJIA_2.getCode().byteValue());
				houseAddEs(house.getId());
			}
			house.setUpdatedTime(new Date());
			house.setUpdatedUser(updateUser);
			houseMapper.updateByPrimaryKeySelective(house);
		}
		return status == 1 ? "房屋上架成功!":"首次录入房源需审核，通过后自动上架，请等待审核结果!";
	}
	
	@Transactional(rollbackFor = { Exception.class,RuntimeException.class})
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
			if(house.getStatus().intValue() == BusinessHouseTypeEnums.ZT_SHANGCHU_4.getCode()){
				continue;
			}
			//如果房屋的状态为待上架，正常上架
			if(house.getStatus().intValue() == BusinessHouseTypeEnums.ZT_WSHAGNJIA_1.getCode() 
					|| house.getStatus().intValue() == BusinessHouseTypeEnums.ZT_XIAJIA_3.getCode()){
				if(house.getStatus().intValue() == BusinessHouseTypeEnums.ZT_WSHAGNJIA_1.getCode()){
					house.setStatus(BusinessHouseTypeEnums.ZT_XIUGAI_5.getCode().byteValue());
					others++;
				}else{
					house.setStatus(BusinessHouseTypeEnums.ZT_SHAGNJIA_2.getCode().byteValue());
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
		return "";
	}
	
	/*** 删除图片信息 * @throws BusinessException */
	@Transactional(rollbackFor = { Exception.class,RuntimeException.class})
	public void delPicture(String id) throws BusinessException{
		
		if(StringUtils.isBlank(id)){
			throw new BusinessException("图片Id不能为空!");
		}
		HouseImg img = imgMapper.selectByPrimaryKey(Long.parseLong(id));
		if(null == img){
			throw new BusinessException("图片信息不存在!");
		}
		/*** 是否要删除原图片 */
		File file = new File(img.getUrl());
		if(file.exists()){
			file.delete();
		}
		img.setIsDelete(IsDeleteEnums.IS_DELETE_01.getCode());
		img.setUpdatedTime(new Date());
		imgMapper.updateByPrimaryKey(img);
	}
	
	
	public House readEntity(Long id){
		return houseMapper.selectByPrimaryKey(id);
	}
	@Transactional(rollbackFor = { Exception.class})
	public Integer updateEntity(House entity){
		return houseMapper.updateByPrimaryKeySelective(entity);
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
		statuList.add(BusinessHouseTypeEnums.ZT_SHAGNJIA_2.getCode());
		dto.setStatus(statuList);
		dto.setIsDelete(IsDeleteEnums.IS_DELETE_00.getCode());
		dto.setListTimeStr("yes");
		dto.setPage(index);
		dto.setRows(bach_size);
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
			LOGGER.info("houseEsList add houseId {} ...", houseEs.getId());
			houseEsList.add(houseEs);
		}
		LOGGER.info("houseEsList add houseSize {} ...", houseEsList.size());
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
				houseEs.setDescription(h.getDescription());
				houseEs.setDescriptionPinyin(Pinyin4jUtil.converterToSpell(h.getDescription()));
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

				//增加价格区间标记priceFlag
				int priceFlag = 6;
				if (h.getRentAmt().compareTo(new BigDecimal(1500)) < 0) {
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
			}
			List<HouseImg> hImgs = imgMapper.getImgByRealHouseId(houseId);
			if(CollectionUtils.isNotEmpty(hImgs)){
				for(HouseImg hImg: hImgs){
					if(hImg!=null){
						houseEs.setUrl(hImg.getUrl());
					}
				}
			}
			List<HousePeizhi> peiZhiByHouses = peizhiMapper.getPeiZhiByHouseId(houseId);
			if(CollectionUtils.isNotEmpty(peiZhiByHouses)){
				for(HousePeizhi hPeizhi: peiZhiByHouses){
					if(hPeizhi!=null){
						houseEs.setConfigName(hPeizhi.getName());
						houseEs.setConfigNamePinyin(Pinyin4jUtil.converterToSpell(hPeizhi.getName()));
					}
				}
			}
			return  houseEs;
		}catch (Exception e){
			LOGGER.error("--------houseInfoToHouseEs Exception------{}",e);
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



}