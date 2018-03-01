package com.apass.zufang.service.house;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.apass.zufang.domain.entity.Apartment;
import com.apass.zufang.domain.entity.HouseImg;
import com.apass.zufang.domain.vo.HouseVo;
import com.apass.zufang.mapper.zfang.ApartmentMapper;
import com.apass.zufang.mapper.zfang.HouseImgMapper;
import com.apass.zufang.mapper.zfang.HouseMapper;

@Component
public class ApartHouseService {
	
	@Autowired
	private ApartmentMapper apartmentMapper;
	@Autowired
	private HouseMapper houseMapper;
	@Autowired
	private HouseImgMapper houseImgMapper;

    /**
     * initImg
     * @return
     */
    public List<String> initImg() {
    	List<HouseImg> initImg = houseImgMapper.initImg();
    	List<String> initCity = new ArrayList<>();
    	for (int i = 0; i < initImg.size(); i++) {
    		initCity.add(initImg.get(i).getUrl());
    	}
    	return initCity;
    }

	/**
	 * 获取公寓Id
	 * @return
	 */
	public List<Apartment> getApartByCity(Apartment entity) {
		return apartmentMapper.getApartByCity(entity);
	}
	
	/**
	 * 查询房源List
	 * @return
	 */
	public List<HouseVo> getHouseByCodes(ArrayList<String> list) {
		return houseMapper.getHouseByCodes(list);
	}

}