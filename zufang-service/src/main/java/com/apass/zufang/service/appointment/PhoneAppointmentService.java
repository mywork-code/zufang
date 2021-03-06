package com.apass.zufang.service.appointment;
import java.util.Date;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.apass.gfb.framework.exception.BusinessException;
import com.apass.gfb.framework.utils.BaseConstants;
import com.apass.gfb.framework.utils.DateFormatUtil;
import com.apass.zufang.domain.Response;
import com.apass.zufang.domain.dto.ApprintmentJourneyQueryParams;
import com.apass.zufang.domain.dto.HouseAppointmentQueryParams;
import com.apass.zufang.domain.entity.HousePeizhi;
import com.apass.zufang.domain.entity.ReserveHouse;
import com.apass.zufang.domain.entity.ReserveRecord;
import com.apass.zufang.domain.enums.BusinessHouseTypeEnums;
import com.apass.zufang.domain.enums.ReserveStatusEnums;
import com.apass.zufang.domain.vo.HouseAppointmentVo;
import com.apass.zufang.domain.vo.ReserveHouseVo;
import com.apass.zufang.service.house.HousePeiZhiService;
import com.apass.zufang.utils.ResponsePageBody;
@Service
public class PhoneAppointmentService {
	@Autowired
	private HousePeiZhiService housePeiZhiService;
	@Autowired
	private ReserveHouseService reserveHouseService;
	@Autowired
	private ReserveRecordService reserveRecordService;
	/**
	 * 电话预约管理 房源列表查询
	 * @param entity
	 * @return
	 */
	public ResponsePageBody<HouseAppointmentVo> getHouseListForPhoneAppointment(HouseAppointmentQueryParams entity,HouseAppointmentQueryParams count) {
		ResponsePageBody<HouseAppointmentVo> pageBody = new ResponsePageBody<HouseAppointmentVo>();
        List<HouseAppointmentVo> list = reserveHouseService.getHouseListForPhoneAppointment(entity);
        for(HouseAppointmentVo vo : list){
        	//户型
        	vo.setHouseAll(vo.getHouseRoom()+"室"+vo.getHouseHall()+"厅"+vo.getHouseWei()+"卫");
        	//面积
        	vo.setHouseAcreage(vo.getHouseAcreage().substring(0, vo.getHouseAcreage().length()-2));
        	vo.setHouseRoomAcreage(vo.getHouseRoomAcreage().substring(0, vo.getHouseRoomAcreage().length()-2));
        	//房源状态
        	Integer status = Integer.parseInt(vo.getHouseStatus());
        	if(status==BusinessHouseTypeEnums.ZT_1.getCode()){
        		vo.setHouseStatus(BusinessHouseTypeEnums.ZT_1.getMessage());
        	}else if(status==BusinessHouseTypeEnums.ZT_2.getCode()){
        		vo.setHouseStatus(BusinessHouseTypeEnums.ZT_2.getMessage());
        	}else if(status==BusinessHouseTypeEnums.ZT_3.getCode()){
        		vo.setHouseStatus(BusinessHouseTypeEnums.ZT_3.getMessage());
        	}else if(status==BusinessHouseTypeEnums.ZT_4.getCode()){
        		vo.setHouseStatus(BusinessHouseTypeEnums.ZT_4.getMessage());
        	}else{
        		vo.setHouseStatus(BusinessHouseTypeEnums.ZT_5.getMessage());
        	}
        	//付款方式
        	String zujintype = vo.getHouseZujinType();
        	Integer zujinType = Integer.parseInt(zujintype.toString());
        	if(zujinType==BusinessHouseTypeEnums.YJLX_1.getCode()){
        		vo.setHouseZujinType(BusinessHouseTypeEnums.YJLX_1.getMessage());
        	}else if(zujinType==BusinessHouseTypeEnums.YJLX_2.getCode()){
        		vo.setHouseZujinType(BusinessHouseTypeEnums.YJLX_2.getMessage());
        	}else if(zujinType==BusinessHouseTypeEnums.YJLX_3.getCode()){
        		vo.setHouseZujinType(BusinessHouseTypeEnums.YJLX_3.getMessage());
        	}else if(zujinType==BusinessHouseTypeEnums.YJLX_4.getCode()){
        		vo.setHouseZujinType(BusinessHouseTypeEnums.YJLX_4.getMessage());
        	}else if(zujinType==BusinessHouseTypeEnums.YJLX_5.getCode()){
        		vo.setHouseZujinType(BusinessHouseTypeEnums.YJLX_5.getMessage());
        	}else if(zujinType==BusinessHouseTypeEnums.YJLX_6.getCode()){
        		vo.setHouseZujinType(BusinessHouseTypeEnums.YJLX_6.getMessage());
        	}else if(zujinType==BusinessHouseTypeEnums.YJLX_7.getCode()){
        		vo.setHouseZujinType(BusinessHouseTypeEnums.YJLX_7.getMessage());
        	}else if(zujinType==BusinessHouseTypeEnums.YJLX_8.getCode()){
        		vo.setHouseZujinType(BusinessHouseTypeEnums.YJLX_8.getMessage());
        	}else if(zujinType==BusinessHouseTypeEnums.YJLX_9.getCode()){
        		vo.setHouseZujinType(BusinessHouseTypeEnums.YJLX_9.getMessage());
        	}else{
        		vo.setHouseZujinType(BusinessHouseTypeEnums.YJLX_10.getMessage());
        	}
        	//出租类型
        	String renttype = vo.getHouseRentType();
        	Integer rentType = Integer.parseInt(renttype.toString());
        	if(rentType==BusinessHouseTypeEnums.HZ_0.getCode()){
        		vo.setHouseRentType(BusinessHouseTypeEnums.HZ_0.getMessage());
        	}else if(rentType==BusinessHouseTypeEnums.HZ_1.getCode()){
        		vo.setHouseRentType(BusinessHouseTypeEnums.HZ_1.getMessage());
        	}else if(rentType==BusinessHouseTypeEnums.HZ_2.getCode()){
        		vo.setHouseRentType(BusinessHouseTypeEnums.HZ_2.getMessage());
        	}
        	//创建时间
        	vo.setCreatedDateTime(DateFormatUtil.dateToString(vo.getCreatedTime(),null));
        	List<HousePeizhi> pzlist = housePeiZhiService.getHousePeizhiList(vo.getHouseId());
        	for(HousePeizhi pz : pzlist){
        	    if(StringUtils.equals("独卫", pz.getName())){
                    vo.setHouseKitchenFalg("是");
                    break;
                }
        	    vo.setHouseKitchenFalg("否");
        	}
        	for(HousePeizhi pz : pzlist){
                if(StringUtils.equals("可做饭", pz.getName())){
                    vo.setHouseToiletFalg("是");
                    break;
                }
                vo.setHouseToiletFalg("否");
            }
        }
        pageBody.setRows(list);
        list = reserveHouseService.getHouseListForPhoneAppointment(count);
        pageBody.setTotal(list.size());
        pageBody.setStatus(BaseConstants.CommonCode.SUCCESS_CODE);
        return pageBody;
	}
	/**
	 * 电话预约管理 预约看房记录新增
	 * @param entity
	 * @return
	 * @throws BusinessException 
	 */
	@Transactional(value="transactionManager",rollbackFor = { Exception.class,RuntimeException.class})
	public Response addReserveHouse(ReserveHouse entity,String user,Date reserveDate,String houserId) throws BusinessException {
		Long syscruent = new Date().getTime();
		if(reserveDate==null){
			throw new BusinessException("看房时间格式化出错！");
		}
		if(reserveDate.getTime()<syscruent){
			throw new BusinessException("看房时间选择错误,请重新选择！");
		}
		ApprintmentJourneyQueryParams count = new ApprintmentJourneyQueryParams();
		count.setTelphone(entity.getTelphone());
		count.setHouseId(houserId);
		List<ReserveHouseVo> list = reserveHouseService.getReserveHouseList(count);
		for(ReserveHouseVo vo : list){
			if(syscruent<vo.getReserveDate().getTime()&&vo.getReserveStatus()!=null&&!StringUtils.equals(vo.getReserveStatus().toString(), ReserveStatusEnums.CANCEL_3.getCode())){
				String rdate = DateFormatUtil.dateToString(vo.getReserveDate(),DateFormatUtil.YYYY_MM_DD_HH_MM);
				throw new BusinessException("该租客已经在（"+rdate+"）时间预约完成该处房源，如需继续预约请取消有效行程，或者等待该用户该房源有效看房行程结束才能继续预约！");
			}
		}
		entity.setType((byte)2);
		entity.setReserveDate(reserveDate);
		entity.setIsDelete("00");
		entity.setCreatedTime(new Date());
		entity.setUpdatedTime(new Date());
		entity.setReserveStatus((byte)1);//新增的看房行程  为1：已预约（首次预约成功默认状态）  默认状态
		if(reserveHouseService.createEntity(entity)!=1){
			throw new BusinessException("电话预约管理 预约看房新增失败！");
		}
		ReserveRecord record = new ReserveRecord();
		record.setReserveHouseId(entity.getId());
		record.setOperateType((byte)1);//新增的看房行程 :新增的看房记录  操作类型，1:预约看房   默认状态
		record.setReserveDate(reserveDate);
		record.setOperateTime(new Date());
		record.setRemark("用户第1次预约操作成功！");
		record.setCreatedTime(new Date());
		record.setUpdatedTime(new Date());
		record.setCreatedUser(user);
		record.setUpdatedUser(user);
		if(reserveRecordService.createEntity(record)!=1){
			throw new BusinessException("电话预约管理 预约看房变更记录新增失败！");
		}
		return Response.success("电话预约管理 预约看房新增成功！");
	}
	@SuppressWarnings("unused")
	private List<HouseAppointmentVo> checkHouseList(List<HouseAppointmentVo> list){
		for(HouseAppointmentVo vo : list){
        	List<HousePeizhi> peizhiList = housePeiZhiService.getHousePeizhiList(vo.getHouseId());
        	Boolean houseKitchenFalg = checkhouseKitchenToiletFalg(peizhiList,"可做饭");
        	if(houseKitchenFalg){
        		vo.setHouseKitchenFalg("是");
        	}else{
        		vo.setHouseKitchenFalg("否");
        	}
        	Boolean houseToiletFalg = checkhouseKitchenToiletFalg(peizhiList,"独卫");
        	if(houseToiletFalg){
        		vo.setHouseToiletFalg("是");
        	}else{
        		vo.setHouseToiletFalg("否");
        	}
		}
		return list;
	}
	private Boolean checkhouseKitchenToiletFalg(List<HousePeizhi> peizhiList, String peizhiName) {
    	for(HousePeizhi peizhi : peizhiList){
    		if(StringUtils.equals(peizhi.getName(), peizhiName)){
    			return true;
    		}
    	}
    	return false;
	}
}