package com.apass.zufang.mapper.zfang;
import java.util.List;
import com.apass.gfb.framework.mybatis.GenericMapper;
import com.apass.zufang.domain.dto.ApprintmentJourneyQueryParams;
import com.apass.zufang.domain.entity.HouseShowingsEntity;
import com.apass.zufang.domain.entity.ReserveHouse;
import com.apass.zufang.domain.vo.ReservationsShowingsEntity;
import com.apass.zufang.domain.vo.ReserveHouseVo;
/**
 * Created by DELL on 2018/2/26.
 */
public interface ReserveHouseMapper extends GenericMapper<ReserveHouse,Long> {
	/**
	 * 预约行程管理 预约看房记录列表查询
	 * @param entity
	 * @return
	 */
	public List<ReserveHouseVo> getReserveHouseList(ApprintmentJourneyQueryParams entity);
	/**
	 * selectrepeat
	 * @param telphone
	 */
	public Integer selectrepeat(String telphone);
	/**
	 * 分页查询
	 * @param crmety
	 * @return
	 */
	public List<HouseShowingsEntity> getHouseLists(ReservationsShowingsEntity crmety);
	/**
	 * 记录数
	 * @param crmety
	 * @return
	 */
	public Integer getCount(String telphone);
	/**
	 * 预约是否过期
	 * @param telphone
	 * @return
	 */
	public Integer queryOverdue(ReservationsShowingsEntity reservationsShowingsEntity);
	/**
	 * 是否重复
	 * @param setreserveHouse
	 * @return
	 */
	public Integer selectRepeat(ReserveHouse setreserveHouse);
}