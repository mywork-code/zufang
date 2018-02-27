package com.apass.zufang.web.house;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.apass.gfb.framework.utils.CommonUtils;
import com.apass.zufang.domain.Response;
import com.apass.zufang.domain.entity.HouseInfoRela;
import com.apass.zufang.service.house.HouseInfoService;
import com.apass.zufang.service.house.HouseService;

@Path("/house")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
@Consumes(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class HouseInfoController {

	private static final Logger LOGGER = LoggerFactory.getLogger(HouseInfoController.class);
	
	@Autowired
	private HouseService houseService;

	@Autowired
	private HouseInfoService houseInfoService;

	/**
	 * 查根据查询条件显示房屋信息
	 * 
	 * @param paramMap
	 * @return
	 */
	@POST
	@Path("/getHouseInfo")
	public Response getHouseInfo(Map<String, Object> paramMap) {
		String houseId = CommonUtils.getValue(paramMap, "houseId");
		String province = CommonUtils.getValue(paramMap, "province");
		String city = CommonUtils.getValue(paramMap, "city");
		String minRentAmt = CommonUtils.getValue(paramMap, "minRentAmt");
		String maxRentAmt = CommonUtils.getValue(paramMap, "maxRentAmt");
		// if(StringUtils.isAnyEmpty(houseId,province,city,minRentAmt,maxRentAmt)){
		// return Response.fail("houseId不能为空");
		// }
		Map<String, Object> resultMap = new HashMap<String, Object>();
		HouseInfoRela queryCondition = new HouseInfoRela();
		try {
			if (StringUtils.isNotBlank(houseId)) {
				queryCondition.setHouseId(Long.valueOf(houseId));
			}
			queryCondition.setProvince(province);
			queryCondition.setCity(city);
			queryCondition.setMinRentAmt(new BigDecimal(minRentAmt) );
			queryCondition.setMaxRentAmt(new BigDecimal(maxRentAmt));

			List<HouseInfoRela> houseInfoList = houseInfoService
					.queryHouseInfoRela(queryCondition);
			resultMap.put("houseInfoList", houseInfoList);
			return Response.success("操作成功", resultMap);
		} catch (Exception e) {
			 LOGGER.error(e.getMessage(), e);
	        return Response.fail("操作失败");
        }
	}

	/**
	 * 房屋详情页：查根据houseId显示房屋信息以及附近房源
	 * 
	 * @param paramMap
	 * @return
	 */
	@POST
	@Path("/getHouseInfoRela")
	public Response getHouseInfoRela(Map<String, Object> paramMap) {
		try {
			String houseId = CommonUtils.getValue(paramMap, "houseId");
			Map<String, Object> resultMap = new HashMap<String, Object>();
			// 目标房源信息
			HouseInfoRela queryCondition =new HouseInfoRela();
			queryCondition.setHouseId(Long.valueOf(houseId));
			List<HouseInfoRela> targetHouseInfoList =houseInfoService.queryHouseInfoRela(queryCondition);
			resultMap.put("targetHouseInfo", targetHouseInfoList.get(0));
			// 附近房源信息
			List<HouseInfoRela> nearlyHouseInfoList =houseInfoService.getNearbyhouseInfo(Long.valueOf(houseId),10);
			resultMap.put("nearlyHouseInfoList", nearlyHouseInfoList);
			return Response.success("操作成功", resultMap);
		} catch (Exception e) {
			LOGGER.error(e.getMessage(), e);
			return Response.fail("操作失败");
		}
	}

}
