package com.dy.baf.controller.phone.member;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.dy.baf.controller.phone.AppBaseController;
import com.dy.baf.entity.DyPhoneResponse;
import com.dy.baf.service.member.ApproveRealnameService;
import com.dy.baf.utils.AppSecurityUtil;

/**
 * 
 * 
 * @Description: 实名认证
 * @author 波哥
 * @date 2015年9月9日 上午9:16:25 
 * @version V1.0
 */
@Controller(value="appApproveRealnameController")
public class ApproveRealnameController extends AppBaseController {
	
	@Autowired
	private ApproveRealnameService approveRealnameService;
	
	@ResponseBody
	@RequestMapping("approve/approveRealName")
	public DyPhoneResponse realnameApprove(String xmdy, String diyou,HttpServletRequest request,HttpServletResponse response) {
		try {
			Map<String, String> paramsMap = AppSecurityUtil.getParamters(diyou);
			String realname = paramsMap.get("realname");
			String card_id = paramsMap.get("card_id");
			String login_token = paramsMap.get("login_token");
			return this.approveRealnameService.realnameApprove(request, response, realname, card_id, login_token);
		} catch (Exception e) {
			logger.error(e.getMessage(),e);
			return errorJsonResonse(e.getMessage());
		}
	}
}