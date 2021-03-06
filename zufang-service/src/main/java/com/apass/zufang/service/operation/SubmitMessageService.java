package com.apass.zufang.service.operation;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.apass.gfb.framework.exception.BusinessException;
import com.apass.gfb.framework.utils.BaseConstants;
import com.apass.gfb.framework.utils.DateFormatUtil;
import com.apass.zufang.domain.Response;
import com.apass.zufang.domain.dto.SubmitMessageQueryParams;
import com.apass.zufang.domain.entity.SubmitMessage;
import com.apass.zufang.domain.vo.SubmitMessageVo;
import com.apass.zufang.mapper.zfang.SubmitMessageMapper;
import com.apass.zufang.utils.ResponsePageBody;
@Service
public class SubmitMessageService {
	@Autowired
	private SubmitMessageMapper submitMessageMapper;
	/**
	 * getSubmitMessageList
	 * @param entity
	 * @return
	 */
	public ResponsePageBody<SubmitMessageVo> getSubmitMessageList(SubmitMessageQueryParams entity,SubmitMessageQueryParams count) {
		ResponsePageBody<SubmitMessageVo> pageBody = new ResponsePageBody<SubmitMessageVo>();
        List<SubmitMessage> list = submitMessageMapper.getSubmitMessageList(entity);
        List<SubmitMessageVo> list1 = new ArrayList<SubmitMessageVo>();
        for(SubmitMessage en : list){
        	SubmitMessageVo vo = new SubmitMessageVo();
        	BeanUtils.copyProperties(en, vo);
        	vo.setSubmitTimeStr(DateFormatUtil.dateToString(vo.getSubmitTime(),null));
        	vo.setCreatedTimeStr(DateFormatUtil.dateToString(vo.getCreatedTime(),null));
        	list1.add(vo);
        }
        pageBody.setRows(list1);
        list = submitMessageMapper.getSubmitMessageList(count);
        pageBody.setTotal(list.size());
        pageBody.setStatus(BaseConstants.CommonCode.SUCCESS_CODE);
        return pageBody;
	}
	/**
	 * 意见反馈新增
	 * @param entity
	 * @return
	 * @throws BusinessException 
	 */
	@Transactional(value="transactionManager",rollbackFor = { Exception.class,RuntimeException.class})
	public Response addSubmitMessage(SubmitMessage entity,String username) throws BusinessException {
		entity.setUpdatedUser(username);
		entity.setCreatedUser(username);
		if(submitMessageMapper.insertSelective(entity)!=1){
			throw new BusinessException("意见反馈新增失败！");
		}
		return Response.success("意见反馈新增成功！");
	}
}