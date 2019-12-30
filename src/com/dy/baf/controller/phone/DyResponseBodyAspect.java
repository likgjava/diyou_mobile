package com.dy.baf.controller.phone;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.dy.baf.entity.DyPhoneResponse;
import com.dy.baf.utils.AppSecurityUtil;
import com.dy.baf.utils.CustomHttpServletRequest;
import com.dy.core.entity.DyResponse;
import com.dy.core.utils.JsonUtils;

@Aspect
@Component
@EnableAspectJAutoProxy
public class DyResponseBodyAspect {
	protected Logger logger = Logger.getLogger(this.getClass());
	
	public static final String FRONT_INTERCEPTOR_PATH = "/phone/.*";
	public static final String NO_INTERCEPTOR_PATH = "/phone/.*/.*/((notify)|(return))";

	
	
	@Around(value="@annotation(org.springframework.web.bind.annotation.ResponseBody)", argNames="pjp")
    public Object aroundAdvice(final ProceedingJoinPoint pjp) throws Throwable {
		HttpServletRequest request = ((ServletRequestAttributes)RequestContextHolder.getRequestAttributes()).getRequest();
		String requestUrl = request.getRequestURI();
		try {
			if (requestUrl.matches(FRONT_INTERCEPTOR_PATH) && !requestUrl.matches(NO_INTERCEPTOR_PATH)) {
				Object[] requestParamters = pjp.getArgs();
				if(requestParamters != null && requestParamters.length >= 2 && !isInterceptorPath(requestUrl)) {
					if(requestParamters[0] == null || requestParamters[1] == null){
						return returnEncryptJson(errorJsonResonse("签名错误"));
					}
					
					String strSign = (String)requestParamters[0];
					String strAes = (String)requestParamters[1];
					
					//验证签名
					if(!AppSecurityUtil.signVerify(strSign, strAes)){
						return returnEncryptJson(errorJsonResonse("签名错误"));
					}
				} 
				DyPhoneResponse phoneResponse = new DyPhoneResponse();
				
				if(isInterceptorPath(requestUrl)){
					String diyou = request.getParameter("diyou");
					String xmdy = request.getParameter("xmdy");
					//验证签名
					if(!AppSecurityUtil.signVerify(xmdy, diyou)){
						return returnEncryptJson(errorJsonResonse("签名错误"));
					}
					
					
					
					Object[] args = pjp.getArgs();
					Map<String, String> paramsMap = AppSecurityUtil.getCamelParamters(diyou);
					CustomHttpServletRequest customRequest = new CustomHttpServletRequest(request);  
					try {
						Method method = TrustRequestDataUtil.class.getMethod(args[1].toString(), Map.class);
						method.invoke(TrustRequestDataUtil.class, paramsMap);
					} catch (Exception e) {}
					
					customRequest.setMap(paramsMap);
					Object obj = pjp.proceed(new Object[]{customRequest, args[1], args[2]});
					if(obj instanceof DyResponse){
						//返回数据格式不一样，在此需要统一转换为DyPhoneResponse
						DyResponse response = (DyResponse)obj;
						phoneResponse = pcTurnApp(response);
					}
				}else{
					Object obj = pjp.proceed();
					phoneResponse = (DyPhoneResponse)obj;
				}
				//加密返回结果
				return returnEncryptJson(phoneResponse);
			}else{
				return pjp.proceed();
			}
		} catch (Exception e) {
			logger.error(e);
			return returnEncryptJson(errorJsonResonse(e.getMessage()));
		}
    }
	
	
	private DyPhoneResponse errorJsonResonse(Object errorMsg) {
		DyPhoneResponse response = new DyPhoneResponse();
		response.setCode(DyPhoneResponse.NO);
		response.setDescription(errorMsg);
		response.setResult(DyPhoneResponse.ERROR);
		return response;
	}
	
	
	/**
	 * 重新定义DyPhoneResponse，只返回xmdy、diyou两个参数
	 * @param dyResponse
	 * @return
	 * @throws Exception 
	 */
	private DyPhoneResponse returnEncryptJson(DyPhoneResponse dyResponse) throws Exception{
		DyPhoneResponse response = new DyPhoneResponse();
		
		//数据加密
		String jsonText = JsonUtils.object2JsonNoEscaping(dyResponse);
		String strAes = AppSecurityUtil.encryptDiyou(jsonText);
		String strSign = AppSecurityUtil.encryptXmdy(strAes);
		response.setXmdy(strSign);
		response.setDiyou(strAes);
		response.setCode(null);
		response.setDescription(null);
		return response;
	}
	
	
	/**
	 * DyResponse 转 DyPhoneResponse
	 * @param response
	 * @return
	 */
	private DyPhoneResponse pcTurnApp(DyResponse response){
		DyPhoneResponse phoneResponse = new DyPhoneResponse();
		if(response.getStatus() ==  response.OK){
			phoneResponse.setCode(DyPhoneResponse.OK);
			phoneResponse.setResult(DyPhoneResponse.SUCCESS);
			phoneResponse.setData(response.getData());
			phoneResponse.setDescription(response.getDescription());
		}else{
			phoneResponse.setCode(DyPhoneResponse.NO);
			phoneResponse.setResult(DyPhoneResponse.ERROR);
			phoneResponse.setDescription(response.getDescription());
		}
		return phoneResponse;
	}

	
	
	/**
	 * 判断请求是否需要另外数据处理
	 * @param path
	 * @return
	 */
	private static boolean isInterceptorPath(String path) {
		List<String> list = getUrlList();
		return list.contains(path);
	}
	
	/**
	 * 需要对数据进行处理的url
	 * @return
	 */
	private static List<String> getUrlList() {
		List<String> list = new ArrayList<String>();
		list.add("/phone/trust/register");
		list.add("/phone/trust/recharge");
		list.add("/phone/trust/withdraw");
		list.add("/phone/trust/bindBankCard");
		list.add("/phone/trust/unBindBankCard");
		list.add("/phone/trust/tender");
		list.add("/phone/trust/tenderRoam");
		list.add("/phone/trust/buyTransfer");
		list.add("/phone/trust/repay");
		list.add("/phone/trustDirect/repay");
		return list;
	}
	
	
    
}