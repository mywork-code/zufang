package com.apass.zufang.web.apartment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import com.apass.gfb.framework.security.toolkit.SpringSecurityUtils;
import com.apass.gfb.framework.utils.BaseConstants.CommonCode;
import com.apass.zufang.domain.Response;
import com.apass.zufang.domain.entity.Apartment;
import com.apass.zufang.service.ApartmentService;
import com.apass.zufang.utils.ResponsePageBody;
/**
 * 公寓信息管理
 * @author Administrator
 *
 */
@Controller
@RequestMapping(value = "/apartment/apartmentController")
public class ApartmentController {
	private static final Logger LOGGER  = LoggerFactory.getLogger(ApartmentController.class);
	@Autowired
	public ApartmentService apartmentService;
	/**
     * 公寓信息管理页面
     */
    @RequestMapping("/init")
    public String init() {
        return "apartment/apartmentManagement";
    }
    /**
     * 限时购活动商品列表查询
     * @param request
     * @return
     */
    @ResponseBody
    @RequestMapping("/getApartmentList")
    public ResponsePageBody<Apartment> getApartmentList(Apartment entity) {
        ResponsePageBody<Apartment> respBody = new ResponsePageBody<Apartment>();
        try {
            ResponsePageBody<Apartment> pagination = apartmentService.getApartmentList(entity);
            respBody.setTotal(pagination.getTotal());
            respBody.setRows(pagination.getRows());
            respBody.setStatus(CommonCode.SUCCESS_CODE);
        } catch (Exception e) {
            LOGGER.error("限时购活动列表查询失败", e);
            respBody.setMsg("限时购活动列表查询失败");
        }
        return respBody;
    }
    /**
     * 保存公寓
     * @param entity
     * @return
     */
    @ResponseBody
    @RequestMapping("/addApartment")
	public Response addApartment(@RequestBody Apartment entity){
		try{
			String username = SpringSecurityUtils.getCurrentUser();
			return apartmentService.addApartment(entity,username);
		}catch(Exception e){
			return null;
		}
	}
}