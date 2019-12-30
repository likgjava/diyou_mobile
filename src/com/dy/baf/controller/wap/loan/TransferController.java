/**
 * 
 */
package com.dy.baf.controller.wap.loan;

import java.util.HashMap;
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
import com.dy.baf.entity.common.FnTenderTransfer;
import com.dy.baf.entity.common.MbMember;
import com.dy.baf.service.loan.BuyTransferService;
import com.dy.baf.service.loan.TransferService;
import com.dy.baf.utils.AppSecurityUtil;
import com.dy.core.dao.query.QueryItem;
import com.dy.core.dao.query.Where;
import com.dy.core.utils.Constant;
import com.dy.core.utils.RequestUtil;
import com.dy.core.utils.StringUtils;

/**
 * 我的债权
 */
@Controller(value="wapTransferController")
public class TransferController extends WapBaseController {
	@Autowired
	private TransferService transferService;
	@Autowired
	private BuyTransferService buyTransferService;
	/**
	 * 我的投资记录
	 * @return
	 */
	@RequestMapping(value="/transfer/mytransfer",method=RequestMethod.GET)
	public ModelAndView myTransfer() {
		ModelAndView view = new ModelAndView();
		try {
			SystemInfo system = new SystemInfo();
			system.setContentPage("transfer/mytransfer.jsp");
			view = this.initCommonPageView(system);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return view;
	}
	/**
	 * 债权盈亏
	 * @param request
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value="/transfer/mytransfer",method=RequestMethod.POST)
	public DyPhoneResponse myTransfer(HttpServletRequest request) throws Exception{
		MbMember member = (MbMember) this.getSessionAttribute(Constant.SESSION_USER) ;
		
		return this.transferService.myTransfer(member.getId());
	}
	
	/**
	 * 我的债权转让列表/购买记录列表获取
	 * @throws Exception 
	 */
	@ResponseBody
	@RequestMapping(value="/transfer/mytransferList",method=RequestMethod.POST)
	public DyPhoneResponse myTransferList(HttpServletRequest request){
		try {
			String page = request.getParameter("page");
			MbMember member = (MbMember) this.getSessionAttribute(Constant.SESSION_USER) ;
			String memberId = member.getId().toString();
			String statusNid = RequestUtil.getString(request, "status", "transfer");
			return this.transferService.getTransferList(memberId,statusNid,page,null);
		} catch (Exception e) {
			logger.error(e.getMessage(),e);
			return errorJsonResonse(e.getMessage());
		}
	}
	/**
	 * 债权转让详情
	 * @return
	 */
	@RequestMapping(value="/transfer/myTransferDetail",method=RequestMethod.GET)
	public ModelAndView myTransferDetail(){
		ModelAndView view = new ModelAndView();
		try {
			SystemInfo system = new SystemInfo();
			system.setContentPage("transfer/mytransferView.jsp");
			view = this.initCommonPageView(system);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return view;
	}
	
	/**
	 * 债权转让详情
	 * @throws Exception 
	 */
	@ResponseBody
	@RequestMapping(value="/transfer/myTransferDetail",method=RequestMethod.POST)
	public DyPhoneResponse myTransferDetail(HttpServletRequest request) throws Exception{
		MbMember member = (MbMember) this.getSessionAttribute(Constant.SESSION_USER) ;
		String tenderId = request.getParameter("id");
		return this.transferService.transferInfo(member.getId().toString(),tenderId);
	}
	/**
	 * 债权转让金额
	 * @throws Exception 
	 */
	@ResponseBody
	@RequestMapping("/transfer/calculatorAmount")
	public DyPhoneResponse calculatorAmount(HttpServletRequest request) throws Exception{
//		Double coefficient = RequestUtil.getDouble(request, "coefficient", 0D) ;
		return null;
	}
	
	/**
	 * 债权转让
	 * @throws Exception 
	 */
	@ResponseBody
	@RequestMapping("/transfer/transfersub")
	public DyPhoneResponse transfersub(HttpServletRequest request) throws Exception{
		MbMember member = (MbMember) this.getSessionAttribute(Constant.SESSION_USER) ;
		String memberId = member.getId().toString() ;
		String tenderId = request.getParameter("tender_id") ;
		String coefficient = request.getParameter("coefficient");
		String paypassword = request.getParameter("paypassword");
		return this.transferService.transferSub(memberId,tenderId,coefficient,paypassword);
	}
	
	/**
	 * 撤销转让
	 * @throws Exception 
	 */
	@ResponseBody
	@RequestMapping("/transfer/cancelTransfer")
	public DyPhoneResponse cancelTransfer(HttpServletRequest request) throws Exception{
		String id = request.getParameter("id");
		FnTenderTransfer transfer = getTransfer(id);
		if (transfer != null) {
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("cancelCount", transfer.getCancelCount());
			return successJsonResonse(map);
		}		
		return null;
	}
	
	/**
	 * 根据id获取债权
	 * @throws Exception 
	 * 
	 */
	private FnTenderTransfer getTransfer(String id) throws Exception{
		QueryItem transferItem  = new QueryItem();
		transferItem.getWhere().add(Where.eq("id", id));
		FnTenderTransfer transfer = this.getOneByEntity(transferItem, "loan", "transfer", FnTenderTransfer.class);
		return transfer;
	}
	
	
	/**
	 * 撤销债权转让弹窗
	 * @throws Exception 
	 */
	@ResponseBody
	@RequestMapping("/transfer/cancel")
	public DyPhoneResponse cancel(HttpServletRequest request) throws Exception{
		String id = request.getParameter("id");
		return this.transferService.cancelSubmit(this.getMemberId().toString(),id);
	}
	
	
	
	/**
	 * 债权详情
	 * @return
	 */
	@RequestMapping(value="/transfer/transferInfo",method=RequestMethod.GET)
	public ModelAndView transferInfo() {
		ModelAndView view = new ModelAndView();
		try {
			SystemInfo system = new SystemInfo();
			system.setContentPage("transfer/transferView.jsp");
			view = this.initCommonPageView(system);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return view;
	}
	
	/**
	 * 已购债权详情
	 * @return
	 */
	@RequestMapping(value="/transfer/myTransferInfo",method=RequestMethod.GET)
	public ModelAndView myTransferInfo() {
		ModelAndView view = new ModelAndView();
		try {
			SystemInfo system = new SystemInfo();
			system.setContentPage("transfer/myTransferInfo.jsp");
			view = this.initCommonPageView(system);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return view;
	}
	
	/**
	 * 债权购买记录详情
	 * @throws Exception 
	 */
	@ResponseBody
	@RequestMapping(value="/transfer/mytransferInfo",method=RequestMethod.POST)
	public DyPhoneResponse mytransferInfo(HttpServletRequest request) throws Exception{
		String memberId = this.getMemberId().toString();
		String transferId = request.getParameter("id");
		String page = request.getParameter("page");
		String epage = request.getParameter("epage");
		Map<String,Object> map =  this.transferService.myTransferBuyInfo(memberId,transferId,page,epage);
		return successJsonResonse(map);
	}
	
	/**
	 * 开始购买
	 * @throws Exception 
	 */
	@ResponseBody
	@RequestMapping("/transfer/buyTransferSub")
	public DyPhoneResponse buyTransferSub(HttpServletRequest request) throws Exception{
		String memberId = this.getMemberId().toString();
		String transferId = request.getParameter("transfer_id");
		String loanId = request.getParameter("loan_id");
		String paypassword = request.getParameter("paypassword");
		return this.buyTransferService.buySubmit(transferId,loanId,paypassword,memberId,this.getRemoteIp());
	}
}
