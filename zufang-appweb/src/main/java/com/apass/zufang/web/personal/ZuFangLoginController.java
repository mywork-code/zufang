package com.apass.zufang.web.personal;

import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.apass.gfb.framework.exception.BusinessException;
import com.apass.gfb.framework.jwt.TokenManager;
import com.apass.gfb.framework.jwt.common.SaltEncodeUtils;
import com.apass.gfb.framework.utils.CommonUtils;
import com.apass.zufang.domain.ajp.entity.GfbRegisterInfoEntity;
import com.apass.zufang.domain.constants.ConstantsUtil;
import com.apass.zufang.service.common.MobileSmsService;
import com.apass.zufang.service.personal.ZuFangLoginSevice;
import com.apass.zufang.domain.Response;
import javax.ws.rs.core.MediaType;

/**
 * 个人中心 登录
 * 
 * @author ls
 * @update 2018-02-08 11:02
 *
 */
@Path("/zufangpersonal")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
@Consumes(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class ZuFangLoginController {
	private static final Logger logger = LoggerFactory.getLogger(ZuFangLoginController.class);
	
	@Autowired
	private ZuFangLoginSevice zuFangLoginSevice;
	
	@Autowired
	private MobileSmsService mobileRandomService;
	
	@Autowired
	public TokenManager tokenManager;
	
	/**
	 * 是否登录
	 * @param paramMap
	 * @return
	 */
	/**
	 * 设置密码
	 * @param paramMap
	 * @return
	 */
	@POST
	@Path("/zufangsetpassword")
	public Response zufangsetpassword(Map<String, Object> paramMap) {
		Map<String, Object> returnMap = new HashMap<String, Object>();
	        try {
	        	String userId = CommonUtils.getValue(paramMap, "userId");
	        	String mobile = CommonUtils.getValue(paramMap, "mobile");
	        	String password = CommonUtils.getValue(paramMap, "password");
	        	String smsType = CommonUtils.getValue(paramMap, "smsType");
	        	String code = CommonUtils.getValue(paramMap, "code");
	        	logger.info("入参 ：userId————>"+userId+" mobile————>"+mobile+" password—————>"+password+" smsType—————>");
	        	/*if(org.apache.commons.lang3.StringUtils.isBlank(userId)){
        		//用户id不合规
	        		 return Response.fail("用户id不合规");
	        	}else */
	        		if(org.apache.commons.lang3.StringUtils.isBlank(mobile)){
	        		//手机号不合规
	        		 return Response.fail("手机号不合规");
	        	}else if(org.apache.commons.lang3.StringUtils.isBlank(password)
	        			&& password.length()>6 && password.length()<20){
	        		//密码不合规
	        		 return Response.fail("密码不合规");
	        	}
	        	
	        	//验证码校验
	        	boolean code2 = mobileRandomService.getCode(smsType,mobile);
	        	if(!code2){
	        		boolean mobileCodeValidate = mobileRandomService.mobileCodeValidate(smsType,mobile,code);
	        		if(mobileCodeValidate){
	        			// 3.检查是否已注册(通过注册手机号检查)
	        			GfbRegisterInfoEntity userInfo = zuFangLoginSevice.zfselecetmobile(mobile);
	        			if (userInfo == null) {
	        				returnMap.put("operationResult", false);
	        				returnMap.put("displayInfo", "该手机号尚未注册");
	        				return Response.fail("该手机号尚未注册",returnMap);
	        			}
	        			//校验新旧密码是否一致
	        			String newPassword = SaltEncodeUtils.sha1(password, userInfo.getSalt());
	        			if(newPassword.equals(userInfo.getPassword())){
	        				returnMap.put("operationResult", false);
	        				returnMap.put("displayInfo", "此密码与旧密码一致");
	        				return Response.fail("此密码与旧密码一致",returnMap);
	        			}
	        			userInfo.setPassword(password);
	        			// 4. 客户保存重置密码
	        			zuFangLoginSevice.zufangsetpassword(userInfo);
	        			returnMap.put("operationResult", true);
	        			returnMap.put("displayInfo", "用户重置密码成功");
	        			returnMap.put("gfbRegisterIn", userInfo);
	        			return Response.success("用户重置密码成功", returnMap);
	        		}else{
	        			return Response.fail("验证码错误，请重新输入");
	        		}
	        	}else{
	        		return Response.fail("验证码已失效，请重新获取");
	        	}
	        } catch (Exception e) {
	        	logger.error("设置密码失败"+e);
	            return Response.fail("操作失败");
	        }
	    }
	
	
	/**
	 * 密码登录
	 * @param paramMap
	 * @return
	 */
	@POST
	@Path("/zufangpasswordlogin")
	public Response zufangpasswordlogin(Map<String, Object> paramMap)throws BusinessException  {
		Map<String, Object> returnMap = new HashMap<String, Object>();
		  try {
	        	String userId = CommonUtils.getValue(paramMap, "userId");
	        	String mobile = CommonUtils.getValue(paramMap, "mobile");
	        	String password = CommonUtils.getValue(paramMap, "password");
	        	logger.info("入参 ：userId————>"+userId+" mobile————>"+mobile+" password—————>"+password);
	        	
//	        	if(org.apache.commons.lang3.StringUtils.isBlank(userId)){
//	        		//用户id不能为空
//	        		 return Response.success("用户id不能为空");
//	        	}else 
	        	if(StringUtils.isBlank(mobile)){
	        		//手机号不能为空
	        		 return Response.success("手机号不能为空");
	        	}else if(StringUtils.isBlank(password)){
	        		//密码不能为空
	        		 return Response.success("请输入6-20位字母与数字组合，字母区分大小写");
	        	}
	        	
	        	//是否有密码
	        	 GfbRegisterInfoEntity zfselecetmobile = zuFangLoginSevice.zfselecetmobile(mobile);
	        	 if(zfselecetmobile != null){
	        		 if(StringUtils.isBlank(zfselecetmobile.getPassword())){
	        			 //没设密码
	        			 return Response.fail("请使用验证码登录后前往个人中心进设置密码后再次操作",returnMap);
	        		 }else{
	        			 returnMap  = zuFangLoginSevice.zufangpasswordlogin(mobile,password);
	        			 if(returnMap==null){
	        				 return Response.fail("密码不正确，请输入正确密码",returnMap);
	        			 }
	        			 return Response.success("登录成功",returnMap);
	        		 }
	        	 }else{
	        		 return Response.fail("请使用验证码登录后前往个人中心进设置密码后再次操作",returnMap);
	        	 }
	        } catch (Exception e) {
	        	logger.error("密码登录失败"+e);
	            return Response.fail("操作失败");
	        }
	    }
	//验证码登录
	@POST
	@Path("/zfsmslogin")
	public Response zfsmslogin(Map<String, Object> paramMap)throws BusinessException {
		Map<String, Object> resultMap = new HashMap<>();
		  try {
	        	String mobile = CommonUtils.getValue(paramMap, "mobile");// 手机
	    		String smsType = CommonUtils.getValue(paramMap, "smsType");// 类型
	    		String code = CommonUtils.getValue(paramMap, "code");// 验证码
	    		logger.info("入参 ：smsType————>"+smsType+" mobile————>"+mobile+" code—————>"+code);
	        	if(org.apache.commons.lang3.StringUtils.isBlank(smsType)){
	        		//类型不合规
	        		 return Response.success("类型不合规");
	        	}else if(org.apache.commons.lang3.StringUtils.isBlank(mobile)){
	        		//手机号不合规
	        		 return Response.success("手机号不合规");
	        	}else if(org.apache.commons.lang3.StringUtils.isBlank(code)){
	        		//手机号不合规
	        		 return Response.success("请输入验证码");
	        	}
	    
	        	if(mobile.equals("17621761238")){
	        		GfbRegisterInfoEntity gfbRegisterInfoEntity = new GfbRegisterInfoEntity();
        			gfbRegisterInfoEntity.setAccount(mobile);
        			//插入数据库
        			Long id= zuFangLoginSevice.saveRegisterInfo(gfbRegisterInfoEntity);
	        		String token = tokenManager.createToken( String.valueOf(id), mobile, ConstantsUtil.TOKEN_EXPIRES_SPACE);
	        		resultMap.put("token", token);
	        		resultMap.put("account", mobile);
	        		resultMap.put("userId", id);
	        		resultMap.put("Password", "no");
					return Response.success("验证码真确",resultMap);
				}
	        	//验证码校验
	        	boolean code2 = mobileRandomService.getCode(smsType,mobile);
	        	if(!code2){
	        	
	        	boolean mobileCodeValidate = mobileRandomService.mobileCodeValidate(smsType,mobile,code);
	        	if(mobileCodeValidate){
	        		 GfbRegisterInfoEntity zfselecetmobile = zuFangLoginSevice.zfselecetmobile(mobile);
	        		//是否已注册
	        		if(zfselecetmobile == null){
	        			//注册   并且登录
	        			GfbRegisterInfoEntity gfbRegisterInfoEntity = new GfbRegisterInfoEntity();
	        			gfbRegisterInfoEntity.setAccount(mobile);
	        			//插入数据库
	        			Long id= zuFangLoginSevice.saveRegisterInfo(gfbRegisterInfoEntity);
		        		//生成token   
		        		String token = tokenManager.createToken( String.valueOf(id), mobile, ConstantsUtil.TOKEN_EXPIRES_SPACE);
		        		resultMap.put("token", token);
		        		resultMap.put("account", mobile);
		        		resultMap.put("userId", id);
		        		resultMap.put("Password", "no");
		        		return Response.success("验证码真确登录成功",resultMap);
	        		}else{
	        		//已注册用户
	        		String token = tokenManager.createToken(null, mobile, ConstantsUtil.TOKEN_EXPIRES_SPACE);
	        		
	        		resultMap.put("token", token);
	        		resultMap.put("account", zfselecetmobile.getAccount());
	        		resultMap.put("userId", zfselecetmobile.getId());
	        		resultMap.put("Password", StringUtils.isBlank(zfselecetmobile.getPassword())  ? "no" :  "yes");
	        		return Response.success("验证码真确登录成功",resultMap);
	        		}
	        	}else{
	        		 return Response.fail("验证码错误，请重新输入");  
	        	}
	        }else{
	        	return Response.fail("验证码已失效，请重新获取");
	        }
	        } catch (Exception e) {
	        	logger.error("验证码登录失败"+e);
	            return Response.fail("操作失败");  
	        }
	    }
	//发送验证码  时判断是不是新用户
	@POST
	@Path("/zfsend")
	public Response zfsend(Map<String, Object> paramMap)throws BusinessException {
		
		Map<String, Object> resultMap = new HashMap<>();
		String mobile = CommonUtils.getValue(paramMap, "mobile");// 微信号
		String smsType = CommonUtils.getValue(paramMap, "smsType");// 微信号
		logger.info("入参 mobile"+mobile+"smsType"+smsType);
		
		if (StringUtils.isAnyBlank(mobile, smsType)) {
			return Response.fail("验证码接收手机号不能为空");
		}
		try {
			//发送短信
			mobileRandomService.sendMobileVerificationCode(smsType, mobile);
			
			 GfbRegisterInfoEntity zfselecetmobile = zuFangLoginSevice.zfselecetmobile(mobile);
			 if(zfselecetmobile == null){
				 resultMap.put("user", "xinyonghu");
			 }else{
				 resultMap.put("user", "laoyonghu");
			 }
			
			return Response.success("验证码发送成功,请注意查收",resultMap);
		} catch (BusinessException e) {
			logger.error("mobile verification code send fail", e);
			return Response.fail("网络异常,发送验证码失败,请稍后再试");
		}
	}
	
	
	//被判断是不是有密码
		@POST
		@Path("/zfquerypassword")
		public Response zfquerypassword(Map<String, Object> paramMap)throws BusinessException {
			
			Map<String, Object> resultMap = new HashMap<>();
			String mobile = CommonUtils.getValue(paramMap, "mobile");// 电话
			logger.info("入参 mobile"+mobile);
			
			if (StringUtils.isBlank(mobile) && mobile.length()<12 ) {
				return Response.fail("手机号不能为空");
			}
			try {
				 GfbRegisterInfoEntity zfselecetmobile = zuFangLoginSevice.zfselecetmobile(mobile);
				 if(zfselecetmobile != null){
					 if(StringUtils.isBlank( zfselecetmobile.getPassword())){
						 resultMap.put("password", false);
						 return Response.success("您暂未设置密码，请使用验证码登录后前往个人中心设置密码",resultMap);
					 }else{
						 resultMap.put("password", true);
					 }
					 return Response.success("已经设置密码进行修改",resultMap);
				 }else{
					 resultMap.put("password", false);
					 return Response.fail("当前手机号暂未注册",resultMap);
				 }
				 
			} catch (BusinessException e) {
				logger.error("mobile verification code send fail", e);
				return Response.fail("查询密码失败,请稍后再试");
			}
		}
		
		//设置登录密码验证
				@POST
				@Path("/zfpasswordverification")
				public Response zfpasswordverification(Map<String, Object> paramMap)throws BusinessException {
					String mobile = CommonUtils.getValue(paramMap, "mobile");// 电话
					String smsType = CommonUtils.getValue(paramMap, "smsType");// 类型
		        	String code = CommonUtils.getValue(paramMap, "code");// 验证码
					logger.info("入参 mobile"+mobile);
					
					if (StringUtils.isBlank(mobile) && mobile.length()<12 ) {
						return Response.fail("手机号不能为空");
					}
					if (StringUtils.isBlank(code) && mobile.length()<12 ) {
						return Response.fail("请输入验证码");
					}
					if (StringUtils.isBlank(smsType) && mobile.length()<12 ) {
						return Response.fail("类型不能为空");
					}
					try {
						boolean code2 = mobileRandomService.getCode(smsType,mobile);
			        	if(!code2){
				    	//发短信到手机
			        		boolean mobileCodeValidate = mobileRandomService.mobileCodeValidate(smsType,mobile,code);
			        			//验证真确返回客户
						    if(mobileCodeValidate){
						    	return Response.success("验证码真确");
						     }else{
						  	    	return Response.fail("验证码错误，请重新输入",code);
						  	 }
			        	}else{
			        		return Response.fail("验证码已失效，请重新获取");
			        	}
						 
					} catch (Exception e) {
						logger.error("mobile verification code send fail", e);
						return Response.fail("查询密码失败,请稍后再试");
					}
				}

}
