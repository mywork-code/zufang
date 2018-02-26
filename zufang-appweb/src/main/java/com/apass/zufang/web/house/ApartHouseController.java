package com.apass.zufang.web.house;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import com.apass.gfb.framework.exception.BusinessException;
import com.apass.gfb.framework.logstash.LOG;
import com.apass.gfb.framework.utils.CommonUtils;
import com.apass.gfb.framework.utils.GsonUtils;
import com.apass.zufang.domain.Response;
import com.apass.zufang.domain.entity.Apartment;
import com.apass.zufang.service.house.ApartHouseService;
import com.apass.zufang.utils.ValidateUtils;

@Path("/ApartHouse")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
@Consumes(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class ApartHouseController {

    @Autowired
    private ApartHouseService apartHouseService;
	
	/**
	 * initImg
	 * @return
	 */
	@POST
	@Path("/initImg")
	public Response initImg() {
		try {
			// 获取市区
			List<String> cityList = apartHouseService.initImg();
			return Response.success("初始城市地址成功！", GsonUtils.toJson(cityList));
		} catch (Exception e) {
			LOG.error("初始城市地址失败！", e);
			return Response.fail("初始城市地址失败！");
		}
	}
	
	/**
	 * 查询公寓Id
	 * @return
	 */
	@POST
	@Path("/getApartID")
	public Response getApartByCity(Map<String, String> paramMap) {
		
		try {
	
			String city = paramMap.get("city");
			Apartment apartment = new Apartment();
			apartment.setCity(city);
			List<Apartment> resultApartment = apartHouseService.getApartByCity(apartment);
	
			return Response.success("success", GsonUtils.toJson(resultApartment));
			} catch (Exception e) {
				LOG.error("查询品牌公寓失败！", e);
				return Response.fail("查询品牌公寓失败！");
			}
	}
	/**
	 * 查询房源List
	 * @return
	 */
	@POST
	@Path("/getHouseByID")
	public Response getHouseByID(Map<String, String> paramMap) {
		
		try {
			String str = paramMap.get("list");
			if (StringUtils.isBlank(str)) {
				return Response.fail("请求参数为空！");
			}
			String[] split = str.split(",");
			ArrayList<String> list = new ArrayList<>();
			for (int i = 0; i < split.length; i++) {
				list.add(split[i]);
			}
			List<Apartment> apartList = apartHouseService.getHouseByID(list);
			return Response.success("success", GsonUtils.toJson(apartList));
		} catch (Exception e) {
			LOG.error("查询品牌公寓失败！", e);
			return Response.fail("查询品牌公寓失败！");
		}
	}
	/**
	 * 品牌公寓
	 * @return
	 */
	@POST
	@Path("/getApartGongyu")
	public Response getApartGongyu(Map<String, Object> paramMap) {
		
		try {
			
			String communityName = CommonUtils.getValue(paramMap, "communityName");// 区域
			ValidateUtils.isNotBlank(communityName, "品牌名称无数据");
			
			Apartment apartment = new Apartment();
			apartment.setCompanyName(communityName);
			
			List<Apartment> resultApartment = apartHouseService.getApartGongyu(apartment);
			
			return Response.success("success", GsonUtils.toJson(resultApartment));
		}catch (BusinessException e){
			LOG.error("查询品牌公寓失败！",e);
			return Response.fail(e.getErrorDesc());
		} catch (Exception e) {
			LOG.error("查询品牌公寓失败！", e);
			return Response.fail("查询品牌公寓失败！");
		}
	}
	
}
