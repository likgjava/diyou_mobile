package com.dy.baf.controller.wap.member;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.dy.baf.controller.wap.WapBaseController;
import com.dy.baf.entity.DyPhoneResponse;
import com.dy.baf.entity.SystemInfo;
import com.dy.baf.entity.common.MbMember;
import com.dy.baf.service.member.EmailService;
import com.dy.core.constant.Function;
import com.dy.core.constant.Module;
import com.dy.core.dao.query.QueryItem;
import com.dy.core.dao.query.Where;
import com.dy.core.utils.GetUtils;
import com.dy.core.utils.RequestUtil;
import com.dy.core.utils.StringUtils;
/**
 * 邮箱管理
 */
@Controller(value="wapEmailController")
public class EmailController extends WapBaseController  {
	@Autowired
	private EmailService emailService;
	/**
	 * 邮箱认证
	 * @return
	 */
	@RequestMapping(value="/member/checkEmail",method=RequestMethod.GET)
	public ModelAndView checkEmail(){
		ModelAndView view = new ModelAndView();
		try {
			SystemInfo system = new SystemInfo();
			system.setContentPage("member/approveEmail.jsp");
			view = this.initCommonPageView(system);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return view;
	}
	
	/**
	 * 邮箱认证提交
	 * @throws Exception 
	 */
	@ResponseBody
	@RequestMapping(value="/member/checkEmail",method=RequestMethod.POST)
	public DyPhoneResponse checkEmail(HttpServletRequest request) throws Exception{
		String email = request.getParameter("email");
		if(!StringUtils.checkEmail(email)){
			return errorJsonResonse("邮箱格式不正确");
		}
		if(StringUtils.isBlank(email)){
			return errorJsonResonse("邮箱不能为空");
		}
		//判断邮箱是否被使用
		List<Where> whereList = new ArrayList<Where>();
		whereList.add(Where.eq("status", 1));
		whereList.add(Where.eq("email", email));
		whereList.add(Where.notEq("id", this.getMemberId()));
		String errorMsg = validateExist(email, "邮箱", Module.MEMBER, Function.MB_MEMBER, whereList);
		if(org.apache.commons.lang.StringUtils.isNotEmpty(errorMsg)){
			return errorJsonResonse(errorMsg);
		}
		this.setSessionAtrribute("approveEmail", email);
		return successJsonResonse("验证通过") ;
	}
	
	/**
	 * 邮箱认证第二步
	 * @return
	 */
	@RequestMapping(value="/member/approveemail",method=RequestMethod.GET)
	public ModelAndView approveEmail(){
		ModelAndView view = new ModelAndView();
		try {
			SystemInfo system = new SystemInfo();
			system.setContentPage("member/approveEmail2.jsp");
			view = this.initCommonPageView(system);
			view.addObject("approveEmail", this.getSessionAttribute("approveEmail")) ;
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return view;
	}
	
	/**
	 * 邮箱认证第二步提交
	 * @param request
	 * @return
	 * @throws Exception 
	 */
	@ResponseBody
	@RequestMapping(value="/member/approveemail",method=RequestMethod.POST)
	public DyPhoneResponse approveEmail(HttpServletRequest request) throws Exception{
		String code = request.getParameter("code");
		String email = this.getSessionAttribute("approveEmail") != null ? (String)this.getSessionAttribute("approveEmail") : "";
		Map<String, Object> sessionMap = (Map<String, Object>) this.getSessionAttribute("sessionMap") ;
		return this.emailService.approveEmail(email, code, this.getMemberId(), sessionMap) ;
	}
	
	/**
	 * 修改邮箱
	 */
	@RequestMapping(value="/member/updateemailone",method=RequestMethod.GET)
	public ModelAndView updateEmailOne(){
		ModelAndView view = new ModelAndView();
		try {
			SystemInfo system = new SystemInfo();
			system.setContentPage("member/updateEmailone.jsp");
			view = this.initCommonPageView(system);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return view;
	}
	
	/**
	 * 修改邮箱第一步
	 */
	@ResponseBody
	@RequestMapping(value="/member/updateEmailone",method=RequestMethod.POST)
	public DyPhoneResponse updateEmailOne(HttpServletRequest request){
		try {
			return this.emailService.getAppEmail(this.getMemberId().toString());
		} catch (Exception e) {
			logger.error(e);
			return errorJsonResonse(e.getMessage());
		}
	}
	
	/**
	 * 修改邮箱第二步
	 * @throws Exception 
	 */
	@ResponseBody
	@RequestMapping(value="/member/updateemailtwo",method=RequestMethod.POST)
	public DyPhoneResponse updateEmailTwo(HttpServletRequest request) throws Exception{
		String email = this.getMember(this.getMemberId()).getEmail() ;
		String code = request.getParameter("code") ;
		String type = request.getParameter("type") ;
		DyPhoneResponse response = this.emailService.verifyCode(email, code, type) ;
		return response ;
	}
	
	/**
	 * 修改邮箱第三步
	 * @throws Exception 
	 */
	@ResponseBody
	@RequestMapping(value="/member/updateemailthree",method=RequestMethod.POST)
	public DyPhoneResponse updateEmailThree(HttpServletRequest request) throws Exception{
		String email = request.getParameter("email") ;
		String code = request.getParameter("code") ;
		String type = request.getParameter("type") ;
		Map<String, Object> sessionMap = (Map<String, Object>) this.getSessionAttribute("sessionMap") ;
		return this.emailService.approveEmail(email, code, this.getMemberId(),sessionMap ) ;
	}
	
	/**
	 * 发送邮件
	 * @param email
	 * @param type
	 * @return
	 * @throws Exception
	 */
	@ResponseBody
	@RequestMapping("/member/sendemail")
	public DyPhoneResponse sendEmail(HttpServletRequest request) {
		try {
			String memberId = "" ;
			String email = request.getParameter("email"); 
			String emailCode = request.getParameter("email_code"); 
			String type = RequestUtil.getString(request, "type", "reset"); 
			if("approve".equals(type)){
				email = (String) this.getSessionAttribute("approveEmail");
			}else if("password".equals(type)){
				type = "recover";
				email = (String) this.getSessionAttribute(PasswordController.SEARCH_ACCOUNT) ;
				QueryItem queryItem = new QueryItem();
				queryItem.getWhere().add(Where.eq("email", email));
				MbMember member = this.getOneByEntity(queryItem,Module.MEMBER,Function.MB_MEMBER,MbMember.class);
				memberId = member.getId().toString() ;
				email = (String) this.getSessionAttribute(PasswordController.SEARCH_ACCOUNT);
			}
			//验证新邮箱时对邮箱验证
			if(StringUtils.isBlank(email)&&"again".equals(type)){
				return errorJsonResonse("请先输入邮箱号") ;
			}
			if(StringUtils.isBlank(memberId)){
				memberId = this.getMemberId().toString();
			}
			if(StringUtils.isBlank(email)){
				email = this.getMember(this.getMemberId()).getEmail() ;
			}
			if(!StringUtils.checkEmail(email)){
				return errorJsonResonse("邮箱格式错误") ;
			}
			//发送激活邮箱,将验证码和email存入session
			if(StringUtils.isBlank(emailCode)){
				emailCode = GetUtils.sixCode();
			}
			DyPhoneResponse response = this.emailService.sendemail(email, emailCode, memberId, type) ;
			if(DyPhoneResponse.OK == response.getCode()){
				Map<String,Object> data = (Map<String, Object>) response.getData() ;
				response.setData(data.get("email")) ;
			}
			return response;
		} catch (Exception e) {
			logger.error(e.getStackTrace());
			return errorJsonResonse(e.getMessage());
		}
	}
}
