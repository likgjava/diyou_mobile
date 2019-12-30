package com.dy.baf.controller.wap.member;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.dy.baf.controller.wap.WapBaseController;
import com.dy.baf.entity.DyPhoneResponse;
import com.dy.baf.entity.SystemInfo;
import com.dy.baf.entity.common.MbMember;
import com.dy.baf.entity.common.MbMemberAnswer;
import com.dy.baf.entity.common.MbMemberQuestion;
import com.dy.baf.entity.common.MbMemberReviews;
import com.dy.baf.entity.common.MbMemberReviewsResult;
import com.dy.core.constant.Function;
import com.dy.core.constant.Module;
import com.dy.core.dao.query.QueryItem;
import com.dy.core.dao.query.Where;
import com.dy.core.entity.DyResponse;
import com.dy.core.entity.Page;
import com.dy.core.utils.Constant;
import com.dy.core.utils.DateUtil;

/**
 * @Description: 风险评估
 * @author panxh
 * @date 2016年7月8日 15:17:09
 * @version V1.0
 */
@Controller(value="wapRiskController")
public class RiskController extends WapBaseController {
	
	
	/**
	 * (phone,app)风险测评
	 * @return
	 */
	@RequestMapping("/mobileAnswer/record")
	public ModelAndView spreadlogPage(HttpServletRequest request) {
		String id = request.getParameter("id");
		String type = request.getParameter("type");
		ModelAndView view = new ModelAndView();
		try {
			SystemInfo system = new SystemInfo("member/mobileRiskAnswer.jsp");
			view = this.initMemberPageView(system);
			view.addObject("id", id);
			view.addObject("type", type);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return view;
	}
	
	/**
	 * 风险测评
	 * @return
	 * @throws Exception 
	 * @throws NumberFormatException 
	 */
	@RequestMapping("/answer/record")
	public ModelAndView record(HttpServletRequest request) throws Exception {
		String type = this.getRequest().getParameter("type");
		MbMember member = (MbMember) this.getSessionAttribute(Constant.SESSION_USER);	
		ModelAndView view = new ModelAndView();
		try {
			//判断是否测评过
			/*QueryItem queryItem = new QueryItem();
			queryItem.setWhere(Where.eq("member_id", member.getId()));
			List<MbMemberReviews> reviews = this.getListByEntity(queryItem, Module.MEMBER, Function.MB_REVIEWS, MbMemberReviews.class);
			
			
			if(null != reviews && 0 != reviews.size() && StringUtils.isBlank(type)){
				//判断评测是否有过期(测评时间满24个月)
				Calendar nowTime = Calendar.getInstance();   
			    Date nowDate = (Date) nowTime.getTime(); //得到当前时间  
			    Calendar afterTime = Calendar.getInstance();   
			    Date date = new Date(reviews.get(reviews.size()-1).getCreateTime()*1000);
			    afterTime.setTime(date);
			    afterTime.add(Calendar.YEAR, 2);
				if(afterTime.getTimeInMillis() < nowDate.getTime()){
					//已到期，请重新进行风险评测
					SystemInfo system = new SystemInfo("member/riskAnswer.jsp");
					view = this.initMemberPageView(system);
					view.addObject("type", "echo");
					return view;
				}
				
				view = new ModelAndView("redirect:/wap/answer/result");
			    return view;
			}*/
			
			SystemInfo system = new SystemInfo("member/riskAnswer.jsp");
			view = this.initMemberPageView(system);
			view.addObject("type", type);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return view;
	}
	
	/**
	 * 是否过期
	 * @return
	 * @throws Exception 
	 */
	@ResponseBody
	@RequestMapping("/answer/isExpire")
	public DyResponse isExpire() throws Exception{
		MbMember member = (MbMember) this.getSessionAttribute(Constant.SESSION_USER);
		//判断是否测评过
		QueryItem queryItem = new QueryItem();
		queryItem.setWhere(Where.eq("member_id", member.getId()));
		List<MbMemberReviews> reviews = this.getListByEntity(queryItem, Module.MEMBER, Function.MB_REVIEWS, MbMemberReviews.class);
		
		if(null != reviews && 0 != reviews.size()){
			//判断评测是否有过期(测评时间满24个月)
			Calendar nowTime = Calendar.getInstance();   
			Date nowDate = (Date) nowTime.getTime(); //得到当前时间  
			Calendar afterTime = Calendar.getInstance();   
			Date date = new Date(reviews.get(reviews.size()-1).getCreateTime()*1000);
			afterTime.setTime(date);
			afterTime.add(Calendar.YEAR, 2);
			if(afterTime.getTimeInMillis() < nowDate.getTime()){
				//已到期，请重新进行风险评测
				return this.createApproveError(230, "已到期，请重新进行风险评测");
			}
		}else{
			return this.createApproveError(220, "还未提交测评，请先进行测评");
		}
			
		return this.createSuccessJsonResonse(null);
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@ResponseBody
	@RequestMapping("/risk/getRiskRecordList")
	public DyPhoneResponse getRiskRecordList() {
		try {
			this.getRequest().getParameter("id");
			MbMember member = (MbMember) this.getSessionAttribute(Constant.SESSION_USER);
			if(member == null) return errorJsonResonse("尚未登录!");
			
			QueryItem queryItem = new QueryItem();
			List<Map> list = this.getListByMap(queryItem, Module.MEMBER, Function.MB_QUESTION);
			
			QueryItem queryReviews = new QueryItem();
			queryReviews.setWhere(Where.eq("member_id", member.getId()));
			queryReviews.setFields("id");
			queryReviews.setOrders("id,create_time asc");
			List<MbMemberReviews> reviews = this.getListByEntity(queryReviews, Module.MEMBER, Function.MB_REVIEWS, MbMemberReviews.class);
			
			List<Map<String, Object>> data = new ArrayList<Map<String, Object>>();
			for(Map map : list){
				
				QueryItem queryChose = new QueryItem();
				queryChose.setFields("id,answer_choose");
				queryChose.setWhere(Where.eq("question_id", map.get("id")));
				queryChose.setWhere(Where.eq("member_id", member.getId()));
				if(reviews.size() > 0){
					queryChose.setWhere(Where.eq("reviews_id", reviews.get(reviews.size()-1).getId()));
				}
				MbMemberAnswer answer = this.getOneByEntity(queryChose, Module.MEMBER, Function.MB_ANSWER, MbMemberAnswer.class);
				
				List<Map<String, Object>> answers = new ArrayList<Map<String, Object>>();
				if(answers.size() > 0){
					answers.remove(0);
				}
				Map dataMap = parseJSON2Map(map.get("answer").toString());
				dataMap = sortMapByKey(dataMap);
				Iterator iter = dataMap.entrySet().iterator();  //获得map的Iterator
				while(iter.hasNext()) {
					Map temp = new HashMap();
					Entry entry = (Entry)iter.next();
					if(StringUtils.isBlank((String)entry.getValue())){
						continue;
					}
					
					String chose = null==answer || StringUtils.isBlank(answer.getAnswerChoose())?"":answer.getAnswerChoose();
					temp.put("num", entry.getKey());
					temp.put("description", entry.getValue());
					temp.put("isChose", chose.contains(entry.getKey().toString()));
					answers.add(temp);
				}
				
				Map<String, Object> newData = new HashMap<String, Object>();
				newData.put("id", map.get("id"));
				newData.put("title", map.get("title"));
				newData.put("question", answers);
				
				data.add(newData);
			}
			
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("records", data);
			
			return this.successJsonResonse(map);
			
		} catch (Exception e) {
			logger.error(e.getMessage(),e);
			return errorJsonResonse(e.getMessage());
		}
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@ResponseBody
	@RequestMapping("/mobileAnswer/getRiskRecordList")
	public DyPhoneResponse getPhoneRiskRecordList(HttpServletRequest request) {
		try {
			String id = request.getParameter("id");
			MbMember member = this.getMember(Long.valueOf(id));
			
			QueryItem queryItem = new QueryItem();
			List<Map> list = this.getListByMap(queryItem, Module.MEMBER, Function.MB_QUESTION);
			
			QueryItem queryReviews = new QueryItem();
			queryReviews.setWhere(Where.eq("member_id", member.getId()));
			queryReviews.setFields("id");
			queryReviews.setOrders("id,create_time asc");
			List<MbMemberReviews> reviews = this.getListByEntity(queryReviews, Module.MEMBER, Function.MB_REVIEWS, MbMemberReviews.class);
			
			List<Map<String, Object>> data = new ArrayList<Map<String, Object>>();
			for(Map map : list){
				QueryItem queryChose = new QueryItem();
				queryChose.setFields("id,answer_choose");
				queryChose.setWhere(Where.eq("question_id", map.get("id")));
				queryChose.setWhere(Where.eq("member_id", member.getId()));
				if(reviews.size() > 0){
					queryChose.setWhere(Where.eq("reviews_id", reviews.get(reviews.size()-1).getId()));
				}
				MbMemberAnswer answer = this.getOneByEntity(queryChose, Module.MEMBER, Function.MB_ANSWER, MbMemberAnswer.class);
				
				List<Map<String, Object>> answers = new ArrayList<Map<String, Object>>();
				if(answers.size() > 0){
					answers.remove(0);
				}
				Map dataMap = parseJSON2Map(map.get("answer").toString());
				dataMap = sortMapByKey(dataMap);
				Iterator iter = dataMap.entrySet().iterator();  //获得map的Iterator
				while(iter.hasNext()) {
					Map temp = new HashMap();
					Entry entry = (Entry)iter.next();
					if(StringUtils.isBlank((String)entry.getValue())){
						continue;
					}
					
					String chose = null==answer || StringUtils.isBlank(answer.getAnswerChoose())?"":answer.getAnswerChoose();
					temp.put("num", entry.getKey());
					temp.put("description", entry.getValue());
					temp.put("isChose", chose.contains(entry.getKey().toString()));
					answers.add(temp);
				}
				
				Map<String, Object> newData = new HashMap<String, Object>();
				newData.put("id", map.get("id"));
				newData.put("title", map.get("title"));
				newData.put("question", answers);
				
				data.add(newData);
			}
			
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("records", data);
			
			return this.successJsonResonse(map);
			
		} catch (Exception e) {
			logger.error(e.getMessage(),e);
			return errorJsonResonse(e.getMessage());
		}
	}
	
	/**
	 * 风险测评填写提交
	 * @param page
	 * @return
	 * @throws Exception
	 */
	@ResponseBody
	@RequestMapping("/answer/submit")
	public DyResponse answerSubmit(HttpServletRequest request) throws Exception{
		
		MbMember user = (MbMember)getSessionAttribute(Constant.SESSION_USER);
		user = getMember(user.getId());
		if(user == null) return createLoginJsonResonse("尚未登录!");
		
		//String type = request.getParameter("type");
		QueryItem queryReviews = new QueryItem();
		queryReviews.setWhere(Where.eq("member_id", user.getId()));
		//MbMemberReviews isHaveReviews = this.getOneByEntity(queryReviews, Module.MEMBER, Function.MB_REVIEWS, MbMemberReviews.class);
		/*//是否重新评测
		if("echo".equals(type) && null != isHaveReviews){
			this.deleteById(isHaveReviews.getId(), Module.MEMBER, Function.MB_REVIEWS);
			QueryItem queryAnswer = new QueryItem();
			queryAnswer.setFields("id");
			queryAnswer.setWhere(Where.eq("member_id", user.getId()));
			List<MbMemberAnswer> answers = this.getListByEntity(queryAnswer, Module.MEMBER, Function.MB_ANSWER, MbMemberAnswer.class);
			for(MbMemberAnswer answer : answers){
				this.deleteById(answer.getId(), Module.MEMBER, Function.MB_ANSWER);
			}
		}else{
			if(null != isHaveReviews) return createLoginJsonResonse("已提交过，请勿重复提交");
		}*/
		
		QueryItem queryItem = new QueryItem();
		List<MbMemberQuestion> questionIds = this.getListByEntity(queryItem, Module.MEMBER, Function.MB_QUESTION, MbMemberQuestion.class);
		
		//选项是否没有选
		for (MbMemberQuestion questionId : questionIds) {
			String[] answerChooses = request.getParameterValues("answers_" + questionId.getId());
			if(null == answerChooses){
				return this.createApproveError("问题回答不全");
			}
		}
		
		//初始化用户评估表
		MbMemberReviews reviews = new MbMemberReviews();
		reviews.setCreateTime(DateUtil.getCurrentTime());
		reviews.setMemberId(user.getId());
		reviews.setMemberName(user.getName());
		reviews.setRole(user.getRole());
		reviews.setPhone(user.getPhone());
		this.insert(Module.MEMBER, Function.MB_REVIEWS, reviews);
		
		queryReviews.setFields("id");
		queryReviews.setOrders("id,create_time asc");
		List<MbMemberReviews> list = this.getListByEntity(queryReviews, Module.MEMBER, Function.MB_REVIEWS, MbMemberReviews.class);
		
		if(list.size() <= 0){
			return  this.createSuccessJsonResonse("提交失败");
		}
		reviews = list.get(list.size()-1);
		
		Integer allScores = 0;
		for (MbMemberQuestion questionId : questionIds) {
			//保存入库
			MbMemberAnswer answer = new MbMemberAnswer();
			answer.setMemberId(user.getId());
			answer.setMemberName(user.getName());
			answer.setQuestionId(questionId.getId());
			answer.setReviewsId(reviews.getId());
			
			String[] answerChooses = request.getParameterValues("answers_" + questionId.getId());
			if(null == answerChooses){
				this.insert(Module.MEMBER, Function.MB_ANSWER, answer);
				continue;
			} 
			
			String answerChoose = "";
			int count = 0;
			for(String chose : answerChooses){
				count++;
				answerChoose += chose + (count == answerChooses.length? "" : ",");
			}
			answer.setAnswerChoose(answerChoose);
			//新增二开功能,获取每题得分并累计
			Integer choseScore = 0;
			Map<String, Object> map = this.getOneByMap(new QueryItem(new Where("id", questionId.getId())), Module.MEMBER, Function.MB_QUESTION);
			Map<String,Object> mapScore = new HashMap<String, Object>();
			if(null != map && map.size() != 0){
				if(null != map.get("score")){
					mapScore = parseJSON2Map(map.get("score").toString());//转成map
					choseScore = Integer.valueOf(null==mapScore.get(answerChoose)?"0":mapScore.get(answerChoose).toString());
				}
			}
			allScores += choseScore;
			
			this.insert(Module.MEMBER, Function.MB_ANSWER, answer);
		}
		
		//根据总得分得出评测结果
		QueryItem queryResult = new QueryItem();
		List<MbMemberReviewsResult> results = this.getListByEntity(queryResult, Module.MEMBER, Function.MB_REVIEWSRESULT, MbMemberReviewsResult.class);
		for(MbMemberReviewsResult result : results){
			Integer min = result.getMinVal();
			Integer max = result.getMaxVal();
			if(min <= allScores && max >= allScores){
				//更新评估结果表 level 更新
				reviews.setMemberId(user.getId());
				reviews.setMemberName(user.getName());
				reviews.setRole(user.getRole());
				reviews.setStatus(1);
				reviews.setCreateTime(DateUtil.getCurrentTime());
				reviews.setLevel(Integer.valueOf(result.getId().toString()));
				this.updateById(Module.MEMBER, Function.MB_REVIEWS, reviews);
			}else{
				continue;
			}
		}
		
		return  this.createSuccessJsonResonse("提交成功");
	}
	
	/**
	 * (phone, app)风险测评填写提交
	 * @param page
	 * @return
	 * @throws Exception
	 */
	@ResponseBody
	@RequestMapping("/mobileAnswer/submit")
	public DyResponse mobileAnswerSubmit(HttpServletRequest request) throws Exception{
		String id = request.getParameter("id");
		MbMember user = this.getMember(Long.valueOf(id));
		
		//String type = request.getParameter("type");
		QueryItem queryReviews = new QueryItem();
		queryReviews.setWhere(Where.eq("member_id", user.getId()));
		//MbMemberReviews isHaveReviews = this.getOneByEntity(queryReviews, Module.MEMBER, Function.MB_REVIEWS, MbMemberReviews.class);
		//是否重新评测
		/*if("echo".equals(type) && null != isHaveReviews){
			this.deleteById(isHaveReviews.getId(), Module.MEMBER, Function.MB_REVIEWS);
			QueryItem queryAnswer = new QueryItem();
			queryAnswer.setFields("id");
			queryAnswer.setWhere(Where.eq("member_id", user.getId()));
			List<MbMemberAnswer> answers = this.getListByEntity(queryAnswer, Module.MEMBER, Function.MB_ANSWER, MbMemberAnswer.class);
			for(MbMemberAnswer answer : answers){
				this.deleteById(answer.getId(), Module.MEMBER, Function.MB_ANSWER);
			}
		}else{
			if(null != isHaveReviews) return createLoginJsonResonse("已提交过，请勿重复提交");
		}*/
		
		QueryItem queryItem = new QueryItem();
		List<MbMemberQuestion> questionIds = this.getListByEntity(queryItem, Module.MEMBER, Function.MB_QUESTION, MbMemberQuestion.class);
		
		//选项是否没有选
		for (MbMemberQuestion questionId : questionIds) {
			String[] answerChooses = request.getParameterValues("answers_" + questionId.getId());
			if(null == answerChooses){
				return this.createApproveError("问题回答不全");
			}
		}
		
		//初始化用户评估表
		MbMemberReviews reviews = new MbMemberReviews();
		reviews.setCreateTime(DateUtil.getCurrentTime());
		reviews.setMemberId(user.getId());
		reviews.setMemberName(user.getName());
		reviews.setRole(user.getRole());
		reviews.setPhone(user.getPhone());
		this.insert(Module.MEMBER, Function.MB_REVIEWS, reviews);
		
		queryReviews.setFields("id");
		queryReviews.setOrders("id,create_time asc");
		List<MbMemberReviews> list = this.getListByEntity(queryReviews, Module.MEMBER, Function.MB_REVIEWS, MbMemberReviews.class);
		
		if(list.size() <= 0){
			return  this.createErrorJsonResonse("提交失败");
		}
		reviews = list.get(list.size()-1);
		
		Integer allScores = 0;
		for (MbMemberQuestion questionId : questionIds) {
			//保存入库
			MbMemberAnswer answer = new MbMemberAnswer();
			answer.setMemberId(user.getId());
			answer.setMemberName(user.getName());
			answer.setQuestionId(questionId.getId());
			answer.setReviewsId(reviews.getId());
			
			String[] answerChooses = request.getParameterValues("answers_" + questionId.getId());
			if(null == answerChooses){
				this.insert(Module.MEMBER, Function.MB_ANSWER, answer);
				continue;
			} 
			
			String answerChoose = "";
			int count = 0;
			for(String chose : answerChooses){
				count++;
				answerChoose += chose + (count == answerChooses.length? "" : ",");
			}
			answer.setAnswerChoose(answerChoose);
			
			//新增二开功能,获取每题得分并累计
			Integer choseScore = 0;
			Map<String, Object> map = this.getOneByMap(new QueryItem(new Where("id", questionId.getId())), Module.MEMBER, Function.MB_QUESTION);
			Map<String,Object> mapScore = new HashMap<String, Object>();
			if(null != map && map.size() != 0){
				if(null != map.get("score")){
					mapScore = parseJSON2Map(map.get("score").toString());//转成map
					choseScore = Integer.valueOf(null==mapScore.get(answerChoose)?"0":mapScore.get(answerChoose).toString());
				}
			}
			allScores += choseScore;
			
			this.insert(Module.MEMBER, Function.MB_ANSWER, answer);
		}
		
		//根据总得分得出评测结果
		QueryItem queryResult = new QueryItem();
		List<MbMemberReviewsResult> results = this.getListByEntity(queryResult, Module.MEMBER, Function.MB_REVIEWSRESULT, MbMemberReviewsResult.class);
		for(MbMemberReviewsResult result : results){
			Integer min = result.getMinVal();
			Integer max = result.getMaxVal();
			if(min <= allScores && max >= allScores){
				//更新评估结果表 level 更新
				reviews.setMemberId(user.getId());
				reviews.setMemberName(user.getName());
				reviews.setRole(user.getRole());
				reviews.setStatus(1);
				reviews.setCreateTime(DateUtil.getCurrentTime());
				reviews.setLevel(Integer.valueOf(result.getId().toString()));
				this.updateById(Module.MEMBER, Function.MB_REVIEWS, reviews);
			}else{
				continue;
			}
		}
		
		return  this.createSuccessJsonResonse("提交成功");
	}
	
	/**
	 * 风险测评结果查看
	 * @return
	 * @throws Exception 
	 */
	@RequestMapping("/answer/result")
	public ModelAndView answerResult() throws Exception {	
		MbMember user = (MbMember)getSessionAttribute(Constant.SESSION_USER);
		user = getMember(user.getId());		
		ModelAndView view = new ModelAndView();
		try {
			SystemInfo system = new SystemInfo("member/result.jsp");
			view = this.initMemberPageView(system);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return view;
	}
	
	/**
	 * 风险测评结果
	 * @param page
	 * @return
	 * @throws Exception
	 */
	@ResponseBody
	@RequestMapping("/answer/resultData")
	public DyResponse getResultData() throws Exception{

		MbMember user = (MbMember)getSessionAttribute(Constant.SESSION_USER);
		user = getMember(user.getId());
		if(user == null) return createLoginJsonResonse("尚未登录!");
		
		
		//判断是否提交过
		QueryItem queryReviews = new QueryItem();
		queryReviews.setWhere(Where.eq("member_id", user.getId()));
		queryReviews.setOrders("id,create_time asc");
		List<MbMemberReviews> reviews = this.getListByEntity(queryReviews, Module.MEMBER, Function.MB_REVIEWS, MbMemberReviews.class);
		
		if(null == reviews || reviews.size() == 0){
			return this.createApproveError(230, "还未提交测评，请先进行测评");
		}
	
		//判断评测是否有过期(测评时间满24个月)
	    Calendar afterTime = Calendar.getInstance();   
	    Date date = new Date(reviews.get(reviews.size()-1).getCreateTime()*1000);
	    afterTime.setTime(date);
	    afterTime.add(Calendar.YEAR, 2);
	    
	    //备注： 测试需要 ：“<” 改为   “>”;
	    String isExpire = "0";
		if(afterTime.getTimeInMillis() < System.currentTimeMillis()){
			isExpire = "1";//已到期，请重新进行风险评测
		}
		
		//查询评估结果
		QueryItem queryResult = new QueryItem();
    	queryResult.setFields("id,name");
    	queryResult.setWhere(Where.eq("id", reviews.get(reviews.size()-1).getLevel()));
    	MbMemberReviewsResult result = this.getOneByEntity(queryResult, Module.MEMBER, Function.MB_REVIEWSRESULT, MbMemberReviewsResult.class);
		
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("level", result.getName());
		map.put("isExpire", isExpire);
		
		return this.createSuccessJsonResonse(map);
		
	}
	
	@ResponseBody
	@RequestMapping("/answer/isecho")
	public DyResponse answerIsecho(HttpServletRequest request) throws Exception{
		
		MbMember user = (MbMember)getSessionAttribute(Constant.SESSION_USER);
		user = getMember(user.getId());
		if(user == null) return createLoginJsonResonse("尚未登录!");
		
		QueryItem queryReviews = new QueryItem();
		queryReviews.setWhere(Where.eq("member_id", user.getId()));
		queryReviews.setFields("id");
		queryReviews.setOrders("id,create_time asc");
		List<MbMemberReviews> reviews = this.getListByEntity(queryReviews, Module.MEMBER, Function.MB_REVIEWS, MbMemberReviews.class);
		
		DyResponse res = new DyResponse();
		if(null == reviews || reviews.size() == 0){
			res.setStatus(210);
			return  res;
		}
		
		res.setStatus(220);
		return  res;
	}
	
	@ResponseBody
	@RequestMapping("/mobileAnswer/isecho")
	public DyResponse mobileAnswerIsecho(HttpServletRequest request) throws Exception{
		String id = request.getParameter("id");
		MbMember user = this.getMember(Long.valueOf(id));
		
		QueryItem queryReviews = new QueryItem();
		queryReviews.setWhere(Where.eq("member_id", user.getId()));
		queryReviews.setFields("id");
		queryReviews.setOrders("id,create_time asc");
		List<MbMemberReviews> reviews = this.getListByEntity(queryReviews, Module.MEMBER, Function.MB_REVIEWS, MbMemberReviews.class);
		
		DyResponse res = new DyResponse();
		if(null == reviews || reviews.size() == 0){
			res.setStatus(210);
			return  res;
		}
		
		res.setStatus(220);
		return  res;
	}
	/**
	 * 风险测评推介标的
	 * @return
	 * @throws Exception 
	 */
	@RequestMapping("/answer/introduce")
	public ModelAndView introduceLoan() throws Exception {
		ModelAndView view = new ModelAndView();
		try {
			//获取最近发布的三个标的	
			QueryItem item=new QueryItem();
			List<Where> whereList = new ArrayList<Where>();
			whereList.add(Where.eq("status", 3));
			whereList.add(Where.ge("overdue_time", DateUtil.getCurrentTime()));
			item.setOrders("verify_time desc");
			item.setLimit(3);		
			Page loanList =this.getPageByMap(item, Module.LOAN, Function.LN_LOAN);
			List<Map> loanMapList=loanList.getItems();
			
			SystemInfo system = new SystemInfo("member/introduce.jsp");
			view = this.initMemberPageView(system);		
			view.addObject("loanMapList",loanMapList);		
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return view;
	}
	/**
	 * json字符串转Map
	 * @param jsonStr
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private Map<String, Object> parseJSON2Map(String jsonStr) {
		Map<String, Object> map = new HashMap<String, Object>();
		// 最外层解析
		JSONObject json = JSONObject.fromObject(jsonStr);
		for (Object k : json.keySet()) {
			Object v = json.get(k);
			// 如果内层还是数组的话，继续解析
			if (v instanceof JSONArray) {
				List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
				Iterator<JSONObject> it = ((JSONArray) v).iterator();
				while (it.hasNext()) {
					JSONObject json2 = it.next();
					list.add(parseJSON2Map(json2.toString()));
				}
				map.put(k.toString(), list);
			} else {
				map.put(k.toString(), v);
			}
		}
		return map;
	}
	
	 /** 
     * 使用 Map按key进行排序 
     * @param map 
     * @return 
     */  
    public static Map<String, String> sortMapByKey(Map<String, String> map) {  
        if (map == null || map.isEmpty()) {  
            return null;  
        }  
        Map<String, String> sortMap = new TreeMap<String, String>(new MapKeyComparator());  
        sortMap.putAll(map);  
        return sortMap;  
    }  
}

//比较器类  
class MapKeyComparator implements Comparator<String>{  
	public int compare(String str1, String str2) {  
		return str1.compareTo(str2);  
	}  
}