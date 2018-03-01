package com.apass.zufang.web.house;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Date;
import java.util.Map;

import javax.ws.rs.POST;
import javax.ws.rs.Path;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.apass.gfb.framework.exception.BusinessException;
import com.apass.gfb.framework.jwt.common.ListeningRegExpUtils;
import com.apass.gfb.framework.security.toolkit.SpringSecurityUtils;
import com.apass.gfb.framework.utils.CommonUtils;
import com.apass.gfb.framework.utils.GsonUtils;
import com.apass.zufang.domain.Response;
import com.apass.zufang.domain.dto.HouseQueryParams;
import com.apass.zufang.domain.enums.RentTypeEnums;
import com.apass.zufang.domain.vo.HouseBagVo;
import com.apass.zufang.domain.vo.HouseVo;
import com.apass.zufang.service.house.HouseService;
import com.apass.zufang.utils.FileUtilsCommons;
import com.apass.zufang.utils.ImageTools;
import com.apass.zufang.utils.ResponsePageBody;
import com.apass.zufang.utils.ValidateUtils;

@Controller
@RequestMapping("/house")
public class HouseControler {

	private static final Logger logger = LoggerFactory.getLogger(HouseControler.class);
	
	/** * 图片服务器地址*/
    @Value("${nfs.rootPath}")
    private String rootPath;
    
    /*** 房屋图片存放地址*/
    @Value("${nfs.house}")
    private String nfsHouse;
	
	@Autowired
	private HouseService houseService;
	
	/**
     * 房屋信息列表查询
     * @return
     */
	@ResponseBody
	@RequestMapping("/queryHouse")
	public Response getHouseList(HouseQueryParams dto){
		ResponsePageBody<HouseBagVo> respBody = new ResponsePageBody<HouseBagVo>();
        try {
        	respBody = houseService.getHouseListExceptDelete(dto);
        	respBody.setMsg("房屋信息列表查询成功!");
        	return Response.success("查询房屋信息成功！", respBody);
        } catch (Exception e) {
            logger.error("getHouseList EXCEPTION --- --->{}", e);
            respBody.setMsg("房屋信息列表查询失败!");
            return Response.fail("查询房屋信息失败!");
        }
	}
	
	
	/**
	 * 添加房屋信息
	 * @param paramMap
	 * @return
	 */
	@ResponseBody
    @RequestMapping("/addHouse")
	public Response addHouse(Map<String, Object> paramMap){
		try {
			logger.info("add house paramMap--->{}",GsonUtils.toJson(paramMap));
			validateParams(paramMap);
			HouseVo vo = getVoByParams(paramMap);
			houseService.addHouse(vo);
			return Response.successResponse();
		}catch (BusinessException e){
			logger.error("add house businessException---->{}",e);
			return Response.fail(e.getErrorDesc());
		}catch (Exception e) {
			logger.error("添加房屋信息失败，错误原因", e);
		    return Response.fail("添加房屋信息失败！");
		}
	}
	
	/**
	 * 修改房屋信息
	 * @return
	 */
	@ResponseBody
	@RequestMapping("/editHouse")
	public Response editHouse(Map<String, Object> paramMap){
		try {
			logger.info("edit house paramMap--->{}",GsonUtils.toJson(paramMap));
			validateParams(paramMap);
			HouseVo vo = getVoByParams(paramMap);
			houseService.editHouse(vo);
			return Response.success("");
		}catch (BusinessException e){
			logger.error("edit house businessException---->{}",e);
			return Response.fail(e.getErrorDesc());
		}catch (Exception e) {
			logger.error("修改房屋信息失败，错误原因", e);
		    return Response.fail("修改房屋信息失败！");
		}
	}
	
	/**
	 * 删除房屋信息
	 * @param paramMap
	 * @return
	 */
	@ResponseBody
	@RequestMapping("/delHouse")
	public Response delHouse(Map<String, Object> paramMap){
		try {
			logger.info("del house paramMap--->{}",GsonUtils.toJson(paramMap));
			String id = CommonUtils.getValue(paramMap, "id");
			ValidateUtils.isNotBlank(id, "房屋Id为空！");
			houseService.deleteHouse(id, SpringSecurityUtils.getCurrentUser());
			return Response.success("删除房屋信息成功！");
		}catch (BusinessException e){
			logger.error("delete house businessException---->{}",e);
			return Response.fail(e.getErrorDesc());
		}catch (Exception e) {
			logger.error("删除房屋信息失败，错误原因", e);
		    return Response.fail("删除房屋信息失败！");
		}
	}
    
    
	@ResponseBody
	@RequestMapping("/downHouse")
	public Response downHouse(Map<String, Object> paramMap){
    	try {
    		logger.info("upOrDown house paramMap--->{}",GsonUtils.toJson(paramMap));
			String id = CommonUtils.getValue(paramMap, "id");
			ValidateUtils.isNotBlank(id, "房屋Id为空！");
			houseService.downHouse(id, SpringSecurityUtils.getCurrentUser());
			return Response.success("下架成功！");
		}catch (BusinessException e){
			logger.error("down house businessException---->{}",e);
			return Response.fail(e.getErrorDesc());
		}catch (Exception e) {
			logger.error("下架房屋信息失败，错误原因", e);
		    return Response.fail("操作失败！");
		}
    }
    
	@ResponseBody
	@RequestMapping("/upHouse")
    public Response upHouse(Map<String, Object> paramMap){
    	try {
    		logger.info("batchUp house paramMap--->{}",GsonUtils.toJson(paramMap));
			String id = CommonUtils.getValue(paramMap, "id");
			ValidateUtils.isNotBlank(id, "房屋Id为空！");
			houseService.upHouse(id, SpringSecurityUtils.getCurrentUser());
			return Response.success("上架成功!");
		}catch (BusinessException e){
			logger.error("bathUp house businessException---->{}",e);
			return Response.fail(e.getErrorDesc());
		}catch (Exception e) {
			logger.error("上架房屋信息失败，错误原因", e);
		    return Response.fail("操作失败！");
		}
    }
    
	@ResponseBody
	@RequestMapping("/bathUpHouse")
    public Response batchUp(Map<String, Object> paramMap){
    	try {
    		logger.info("batchUp house paramMap--->{}",GsonUtils.toJson(paramMap));
			String id = CommonUtils.getValue(paramMap, "id");
			ValidateUtils.isNotBlank(id, "房屋Id为空！");
			String message = houseService.upHouses(id, SpringSecurityUtils.getCurrentUser());
			return Response.success(message);
		}catch (BusinessException e){
			logger.error("bathUp house businessException---->{}",e);
			return Response.fail(e.getErrorDesc());
		}catch (Exception e) {
			logger.error("上架房屋信息失败，错误原因", e);
		    return Response.fail("操作失败！");
		}
    }
    
	@ResponseBody
	@RequestMapping("/delpicture")
    public Response deletePicture(Map<String, Object> paramMap){
    	try {
    		logger.info("delpicture paramMap--->{}",GsonUtils.toJson(paramMap));
			String id = CommonUtils.getValue(paramMap, "id");
			ValidateUtils.isNotBlank(id, "图片Id为空！");
			houseService.delPicture(id);
			return Response.success("删除成功!");
		}catch (BusinessException e){
			logger.error("delpicture businessException---->{}",e);
			return Response.fail(e.getErrorDesc());
		}catch (Exception e) {
			logger.error("删除图片信息失败，错误原因", e);
		    return Response.fail("删除失败！");
		}
    }
	
    
    @ResponseBody
    @RequestMapping(value = "/uppicture", method = RequestMethod.POST)
    public Response uploadPicture(MultipartFile file){
    	try{
    		if(null == file){
        		throw new BusinessException("上传文件不能为空!");
        	}
    		boolean checkImgType = ImageTools.checkImgType(file);// 图片类型
        	boolean checkImgSize320 = ImageTools.checkImgSize(file,750,320);// 尺寸
        	boolean checkImgSize562 = ImageTools.checkImgSize(file, 750, 562);//尺寸
        	int size = file.getInputStream().available();
        	
        	if(!((checkImgType && checkImgSize320) || (checkImgType && checkImgSize562))){
        		throw new BusinessException("文件尺寸不符,上传图片尺寸必须是宽：750px,高：562px或者320px,格式：.jpg,.png");
        	}else if(size > 1024 * 1024 * 2){
        		file.getInputStream().close();
        		throw new BusinessException("文件不能大于2MB!");
        	}
        	String fileName = "logo_" + System.currentTimeMillis() + file.getName();
            String url = nfsHouse + fileName;
            /*** 上传文件*/
            FileUtilsCommons.uploadFilesUtil(rootPath, url, file);
            return Response.success("success",url);
        }catch (BusinessException e){
			logger.error("delpicture businessException---->{}",e);
			return Response.fail(e.getErrorDesc());
		}catch (Exception e) {
			logger.error("上传house logo失败!", e);
            return Response.fail("上传商品大图失败!");
        }
    }
    
	/**
	 * 验证所传参数
	 * @param paramMap
	 * @throws BusinessException
	 */
	public void validateParams(Map<String, Object> paramMap) throws BusinessException{
		
		String apartmentId = CommonUtils.getValue(paramMap, "apartmentId");//公寓id
		String phone = CommonUtils.getValue(paramMap,"phone");//管理员手机号
		String rentType = CommonUtils.getValue(paramMap, "rentType");//出租方式
		String communityName = CommonUtils.getValue(paramMap, "communityName");//小区名称
		
	    String province = CommonUtils.getValue(paramMap, "province"); // 省
	    String city = CommonUtils.getValue(paramMap, "city"); // 市
	    String street = CommonUtils.getValue(paramMap, "street"); // 街道
	    String district = CommonUtils.getValue(paramMap, "district"); //区
	    String detailAddr = CommonUtils.getValue(paramMap, "detailAddr"); // 详细地址
	    
	    String acreage = CommonUtils.getValue(paramMap, "acreage");
	    String roomAcreage = CommonUtils.getValue(paramMap, "roomAcreage");
	    
	    String room = CommonUtils.getValue(paramMap, "room"); //室
	    String hall = CommonUtils.getValue(paramMap, "hall"); //厅
	    String wei = CommonUtils.getValue(paramMap, "wei"); //卫
	    String floor = CommonUtils.getValue(paramMap, "floor"); //第几层
	    String totalFloor = CommonUtils.getValue(paramMap, "totalFloor"); //总共的楼层数
	    
	    String liftType = CommonUtils.getValue(paramMap, "liftType");//有无电梯
	    
	    
	    String totalDoors = CommonUtils.getValue(paramMap, "liftType");//几户合租
	    String hezuResource = CommonUtils.getValue(paramMap, "hezuResource");//出租介绍
	    String hezuChaoxiang = CommonUtils.getValue(paramMap, "hezuChaoxiang");//朝向
	    
	    String peizhi = CommonUtils.getValue(paramMap,"peizhi");//配置
	    
	    String rentAmt = CommonUtils.getValue(paramMap, "rentAmt");
	    String zujinType = CommonUtils.getValue(paramMap, "zujinType");
	    
	    String chaoxiang = CommonUtils.getValue(paramMap, "chaoxiang");
	    String zhuangxiu = CommonUtils.getValue(paramMap, "zhuangxiu");
	    
	    String title = CommonUtils.getValue(paramMap, "title");
	    
	    String picturs = CommonUtils.getValue(paramMap,"pictures");//图片
	    
	    ValidateUtils.isNotBlank(apartmentId, "请选择所属公寓");
	    ValidateUtils.isNotBlank(phone, "请填写手机号码");
	    if(!ListeningRegExpUtils.mobile(phone)){
	    	throw new BusinessException("请正确填写11位手机号码");
	    }
		ValidateUtils.isNotBlank(rentType, "请选择出租方式");
		ValidateUtils.isNotBlank(communityName, "请填写小区名称");
		ValidateUtils.checkLength(communityName, 2, 20, "2-20个字，可填写汉字，数字，不能填写特殊字符");
	    ValidateUtils.isNotBlank(province, "请选择省份");
	    ValidateUtils.isNotBlank(city, "请选择城市");
	    ValidateUtils.isNotBlank(district, "请选择区域");
	    ValidateUtils.isNotBlank(street, "请选择街道");
	    ValidateUtils.isNotBlank(detailAddr, "请填写详细地址");
	    ValidateUtils.checkLength(detailAddr, 2, 30, "2-30个字，可填写汉字，数字，不能填写特殊字符");
	    
	    ValidateUtils.isNotBlank(room, "请填写室");
	    ValidateUtils.checkNumberRange(room, 1, 0,"室");
	    ValidateUtils.isNotBlank(hall, "请填写厅");
	    ValidateUtils.checkNumberRange(hall, 0, 0, "厅");
	    ValidateUtils.isNotBlank(wei, "请填写卫");
	    ValidateUtils.checkNumberRange(wei, 0, 0, "卫");
	    
	    ValidateUtils.isNotBlank(floor, "请填写楼层");
	    ValidateUtils.checkNumberRange(floor, -9, 99, "楼层分布");
	    ValidateUtils.isNotBlank(totalFloor, "请填写总楼层");
	    ValidateUtils.checkNumberRange(totalFloor, 1, 99, "总楼层");
	    
	    ValidateUtils.isNotBlank(liftType, "请选择电梯情况");
	    ValidateUtils.isNotBlank(chaoxiang, "请选择朝向");
	    ValidateUtils.isNotBlank(zhuangxiu, "请选择装修情况");
	    ValidateUtils.isNotBlank(acreage, "请填写房屋面积");
    	ValidateUtils.checkNonNumberRange(acreage, 1, 9999, "房屋面积");
	    
	    if(StringUtils.equals(RentTypeEnums.HZ_HEZU_2.getCode()+"", rentType)){//如果出租类型为合租
	    	
	    	ValidateUtils.isNotBlank(totalDoors, "请填写合租户数");
	    	ValidateUtils.checkNonNumberRange(totalDoors, 1, 99, "合租户数");
	    	
	    	ValidateUtils.isNotBlank(hezuResource, "请选择出租间介绍");
	    	ValidateUtils.isNotBlank(hezuChaoxiang, "请选择出租间朝向");
	    	
	    	ValidateUtils.isNotBlank(roomAcreage, "请填写房屋面积");
	    	ValidateUtils.checkNonNumberRange(roomAcreage, 1, 9999, "房屋面积");
	    }
	    
	    ValidateUtils.isNotBlank(peizhi, "请选择房屋配置");
	    ValidateUtils.isNotBlank(rentAmt, "请填写租金");
	    ValidateUtils.checkNumberRange(rentAmt, 0, 0, "租金");
	    ValidateUtils.isNotBlank(zujinType, "请选择租金支付方式");
	    
	    ValidateUtils.isNotBlank(title, "请填写房源标题");
		ValidateUtils.checkLength(title, 6, 30, "请填写6-30个字");
		ValidateUtils.isNotBlank(picturs, "请上传图片");
	}
	
	/***
	 * paramToVo
	 * @param paramMap
	 * @return
	 */
	public HouseVo getVoByParams(Map<String, Object> paramMap){
		
		HouseVo house = new HouseVo();
		String apartmentId = CommonUtils.getValue(paramMap, "apartmentId");
		house.setApartmentId(Long.parseLong(apartmentId));
		String phone = CommonUtils.getValue(paramMap, "phone");
		house.setHousekeeperTel(phone);
		String rentType = CommonUtils.getValue(paramMap, "rentType");
		house.setRentType(Byte.valueOf(rentType));
		String communityName = CommonUtils.getValue(paramMap, "communityName");
		house.setCommunityName(communityName);
	    String province = CommonUtils.getValue(paramMap, "province"); // 省
	    house.setProvince(province);
	    String city = CommonUtils.getValue(paramMap, "city"); // 市
	    house.setCity(city);
	    String district = CommonUtils.getValue(paramMap, "district"); // 区
	    house.setDistrict(district);
	    String street = CommonUtils.getValue(paramMap, "street"); //街道
	    house.setStreet(street);
	    String detailAddr = CommonUtils.getValue(paramMap, "detailAddr"); // 详细地址
	    house.setDetailAddr(detailAddr);
	    
	    String acreage = CommonUtils.getValue(paramMap, "acreage");
	    house.setAcreage(new BigDecimal(acreage));
	    
	    String room = CommonUtils.getValue(paramMap, "room"); //室
	    house.setRoom(Integer.parseInt(room));
	    String hall = CommonUtils.getValue(paramMap, "hall"); //厅
	    house.setHall(Integer.parseInt(hall));
	    String wei = CommonUtils.getValue(paramMap, "wei"); //卫
	    house.setWei(Integer.parseInt(wei));
	    String floor = CommonUtils.getValue(paramMap, "floor"); //第几层
	    house.setFloor(Integer.parseInt(floor));
	    String totalFloor = CommonUtils.getValue(paramMap, "totalFloor"); //总共的楼层数
	    house.setTotalFloor(Integer.parseInt(totalFloor));
	    
	    String liftType = CommonUtils.getValue(paramMap, "liftType");//有无电梯
	    house.setLiftType(Byte.valueOf(liftType));
	    String rentAmt = CommonUtils.getValue(paramMap, "rentAmt");
	    house.setRentAmt(new BigDecimal(rentAmt));
	    String zujinType = CommonUtils.getValue(paramMap, "zujinType");
	    house.setZujinType(Byte.valueOf(zujinType));
	    
	    String chaoxiang = CommonUtils.getValue(paramMap, "chaoxiang");
	    house.setChaoxiang(Byte.valueOf(chaoxiang));
	    String zhuangxiu = CommonUtils.getValue(paramMap, "zhuangxiu");
	    house.setZhuangxiu(Byte.valueOf(zhuangxiu));
	    
	    String peizhi = CommonUtils.getValue(paramMap,"peizhi");//配置
	    String picturs = CommonUtils.getValue(paramMap,"pictures");//图片
	    
	    String[] peizhis = StringUtils.split(peizhi, ",");
	    house.setConfigs(Arrays.asList(peizhis));
	    
	    String[] pictures = StringUtils.split(picturs,",");
	    house.setPictures(Arrays.asList(pictures));
	    
	    String totalDoors = CommonUtils.getValue(paramMap, "liftType");//几户合租
	    String hezuResource = CommonUtils.getValue(paramMap, "hezuResource");//出租介绍
	    String hezuChaoxiang = CommonUtils.getValue(paramMap, "hezuChaoxiang");//朝向
	    
	    house.setTotalDoors(totalDoors);
	    house.setHezuResource(Byte.valueOf(hezuResource));
	    house.setHezuChaoxiang(Byte.valueOf(hezuChaoxiang));
	    
	    String title = CommonUtils.getValue(paramMap, "title");
	    house.setTitle(title);
		
	    String houseId = CommonUtils.getValue(paramMap,"houseId");
	    Date date = new Date();
	    String operateName = SpringSecurityUtils.getCurrentUser();
	    if(StringUtils.isBlank(houseId)){
	    	house.setCreatedTime(date);
	    	house.setCreatedUser(operateName);
	    }
	    house.setUpdatedTime(date);
	    house.setUpdatedUser(operateName);
	    house.setHouseId(Long.parseLong(houseId));
		return house;
	}
}
