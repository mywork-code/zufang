package com.apass.zufang.noauth;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import com.apass.gfb.framework.exception.BusinessException;
import com.apass.zufang.domain.Response;
import com.apass.zufang.utils.FileUtilsCommons;
import com.apass.zufang.utils.ImageTools;
import com.google.common.collect.Maps;
@Controller
@RequestMapping("/application")
public class UpLoadPictureController {
	private static final Logger logger = LoggerFactory.getLogger(UpLoadPictureController.class);
	/** * 图片服务器地址*/
    @Value("${nfs.rootPath}")
    private String rootPath;
    /*** 房屋图片存放地址*/
    @Value("${nfs.house}")
    private String nfsHouse;
    
    @Value("${zufang.image.uri}")
    private String imageUri;
    
	@ResponseBody
    @RequestMapping(value = "/uppicture320", method = RequestMethod.POST)
	public Response uploadPicture320(@ModelAttribute("file") MultipartFile file){
		return uploadImg(file, 757, 562);
    }
	
	@ResponseBody
    @RequestMapping(value = "/uppicture562", method = RequestMethod.POST)
	public Response uploadPicture562(@ModelAttribute("file") MultipartFile file){
		return uploadImg(file, 750, 562);
    }
	
	public Response uploadImg(MultipartFile file,int widths,int heights){
		try{
    		if(null == file){
        		throw new BusinessException("上传文件不能为空!");
        	}
    		boolean checkImgType = ImageTools.checkImgType(file);// 图片类型
        	boolean checkImgSize = ImageTools.checkImgSize(file,widths,heights);// 尺寸
        	int size = file.getInputStream().available();
        	
        	if(!(checkImgType && checkImgSize)){
        		throw new BusinessException("文件尺寸不符,上传图片尺寸必须是宽："+widths+"px,高："+heights+"px,格式：.jpg,.png");
        	}else if(size > 1024 * 1024 * 2){
        		file.getInputStream().close();
        		throw new BusinessException("文件不能大于2MB!");
        	}
        	String imgType = ImageTools.getImgType(file);
        	String fileName = "logo_" + System.currentTimeMillis()+"_"+ file.getName()+ "." + imgType;
            String url = nfsHouse + fileName;
            /*** 上传文件*/
            FileUtilsCommons.uploadFilesUtil(rootPath, url, file);
            Map<String,Object> values = Maps.newHashMap();
            values.put("url",url);
            values.put("fullurl",imageUri+url);
            return Response.success("success",values);
        }catch (BusinessException e){
			logger.error("delpicture businessException---->{}",e);
			return Response.fail(e.getErrorDesc());
		}catch (Exception e) {
			logger.error("上传house logo失败!", e);
			return Response.fail("上传图片失败!");
        }
    }

	/**
     * 编辑精选商品的排序和图片
     * @param siftGoodFileModel
     * @return
	 * @throws BusinessException 
     */
    @ResponseBody
    @RequestMapping(value = "/upLoadCompanyLogo", method = RequestMethod.POST)
    public Response upLoadCompanyLogo(@ModelAttribute("file") MultipartFile file) {
        InputStream is = null;
        try {
        	if(file == null){
        		throw new BusinessException("上传图片为空!");
        	}
            is = file.getInputStream();
            int size = is.available();// 大小
            String imgType = ImageTools.getImgType(file);
            String fileName = "companyLogo_" + System.currentTimeMillis()+ "." + imgType;
            String url = nfsHouse + fileName;
            //图片校验
            boolean checkCompanyLogoSize = ImageTools.checkCompanyLogoSize(file);// 尺寸
            boolean checkImgType = ImageTools.checkImgType(file);// 类型
            if (!checkCompanyLogoSize) {
                return Response.fail("图片尺寸格式不符,尺寸要求:120*120");
            }else if (!checkImgType) {
                return Response.fail("图片尺寸格式不符,格式要求：.jpg,.png");
            } else if (size > 1024 * 500) {
                return Response.fail("图片不能大于501kb!");
            }
            FileUtilsCommons.uploadFilesUtil(rootPath, url, file);
            return Response.success("上传公司LOGO成功！",url);
        } catch (Exception e) {
        	logger.error("upLoadCompanyLogo Exception---->{}",e);
            return Response.fail("上传公司LOGO失败!");
        }finally{
            try {
                if(is!=null){
                    is.close();
                }
            } catch (IOException e) {
                logger.error("IOException businessException---->{}",e);
            }
        }
    }
    @ResponseBody
    @RequestMapping(value = "/upLoadHotHouseImg", method = RequestMethod.POST)
    public Response upLoadHotHouseImg(@ModelAttribute("file") MultipartFile file) {
        InputStream is = null;
        try {
        	if(file == null){
        		throw new BusinessException("上传图片为空!");
        	}
            is = file.getInputStream();
            int size = is.available();// 大小
            String imgType = ImageTools.getImgType(file);
            String fileName = "companyLogo_" + System.currentTimeMillis()+ "." + imgType;
            String url = nfsHouse + fileName;
            //图片校验
            boolean checkHotHouseImgSize = ImageTools.checkHotHouseImgSize(file);// 尺寸
            boolean checkImgType = ImageTools.checkImgType(file);// 类型
            if (!checkHotHouseImgSize) {
                return Response.fail("图片尺寸格式不符,尺寸要求：750*320");
            }else if (!checkImgType) {
                return Response.fail("图片尺寸格式不符,格式要求：.jpg,.png");
            } else if (size > 1024 * 1024) {
                return Response.fail("图片不能大于1024kb!");
            }
            FileUtilsCommons.uploadFilesUtil(rootPath, url, file);
            return Response.success("上传热门图片成功！",url);
        } catch (Exception e) {
        	logger.error("upLoadCompanyLogo Exception---->{}",e);
            return Response.fail("上传热门图片失败!");
        }finally{
            try {
                if(is!=null){
                    is.close();
                }
            } catch (IOException e) {
                logger.error("IOException businessException---->{}",e);
            }
        }
    }
}