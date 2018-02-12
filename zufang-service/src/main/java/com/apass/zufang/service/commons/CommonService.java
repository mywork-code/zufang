package com.apass.zufang.service.commons;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.apass.zufang.domain.entity.HouseLocation;

public class CommonService {

	/**
	 * 默认地球半径
	 */
	private static double EARTH_RADIUS = 6367000.0; // 单位：m

	/**
	 * 计算经纬度点对应正方形4个点的坐标
	 * 
	 * @param longitude
	 *            经度
	 * @param latitude
	 *            纬度
	 * @param distance
	 *            距离
	 * @return
	 */
	public static Map<String, double[]> returnLLSquarePoint(double longitude,
			double latitude, double distance) {
		Map<String, double[]> squareMap = new HashMap<String, double[]>();
		// 计算经度弧度,从弧度转换为角度
		double dLongitude = 2 * (Math.asin(Math.sin(distance
				/ (2 * EARTH_RADIUS))
				/ Math.cos(Math.toRadians(latitude))));
		dLongitude = Math.toDegrees(dLongitude);
		// 计算纬度角度
		double dLatitude = distance / EARTH_RADIUS;
		dLatitude = Math.toDegrees(dLatitude);
		// 正方形
		double[] leftTopPoint = { latitude + dLatitude, longitude - dLongitude };
		double[] rightTopPoint = { latitude + dLatitude, longitude + dLongitude };
		double[] leftBottomPoint = { latitude - dLatitude,
				longitude - dLongitude };
		double[] rightBottomPoint = { latitude - dLatitude,
				longitude + dLongitude };
		squareMap.put("leftTopPoint", leftTopPoint);
		squareMap.put("rightTopPoint", rightTopPoint);
		squareMap.put("leftBottomPoint", leftBottomPoint);
		squareMap.put("rightBottomPoint", rightBottomPoint);
		return squareMap;
	}

	/**
	 * 获取两个经纬度点的距离
	 * 
	 * @param goalLat
	 *            目的地纬度
	 * @param goalLng
	 *            目的地经度
	 * @param lat
	 *            附近房源纬度
	 * @param lng
	 *            附近房源经度
	 * @return
	 */
	public double distanceSimplify(double goalLat, double goalLng, double lat,
			double lng) {
		double dx = goalLng - lng; // 经度差值
		double dy = goalLat - lat; // 纬度差值
		double b = (goalLat + lat) / 2.0; // 平均纬度
		double Lx = Math.toRadians(dx) * EARTH_RADIUS
				* Math.cos(Math.toRadians(b)); // 东西距离
		double Ly = EARTH_RADIUS * Math.toRadians(dy); // 南北距离
		return Math.sqrt(Lx * Lx + Ly * Ly); // 用平面的矩形对角距离公式计算总距离
	}

	/**
	 * 获取附近房源
	 * 
	 * @param houseId
	 *            目标房源
	 * @param number
	 *            附近房源数量
	 * @return
	 */

	public List<HouseLocation> getNearbyhouseInfo(long houseId, int number) {
		// setp 1 根据目标房源id查询目标房源所在位置信息 (province，citycode)
		HouseLocation goalLocation = new HouseLocation();
		// setp 2 根据目标房源的所在位置查询所在城市的所有房源
		List<HouseLocation> houseLocationList = new ArrayList<HouseLocation>();

		// setp 3 计算目标房源和附近房源的距离，并绑定映射关系
		Map<Double, Long> houseDistanceMap = new HashMap<Double, Long>();
		double[] resultArray = new double[houseLocationList.size()];
		for (HouseLocation houseLocation : houseLocationList) {
			double distance = distanceSimplify(goalLocation.getLatitude(),
					goalLocation.getLongitude(), houseLocation.getLatitude(),
					houseLocation.getLongitude());
			houseDistanceMap.put(distance, houseLocation.getHouseId());
			Arrays.fill(resultArray, distance);
		}
		// setp 4 对距离按照升序排序
		Arrays.sort(resultArray);
		// setp 5 取得前number的houseId 的list
		List<Long> houseIdList = new ArrayList<Long>();
		for (int i = 0; i < number; i++) {
			double disance = resultArray[i];
			houseIdList.add(houseDistanceMap.get(disance));
		}
		// setp 6 根据list 查询附近房源的具体信息
		List<HouseLocation> result = new ArrayList<HouseLocation>();
		return result;
	}

}
