package com.xiaojd.controller;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Iterator;
import java.util.List;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import com.xiaojd.base.tools.StringUtils;
import com.xiaojd.conn.ConManager;
import com.xiaojd.entity.hospital.EngPtCf;
import com.xiaojd.entity.hospital.EngPtDispense;
import com.xiaojd.entity.hospital.EngPtDrug;
import com.xiaojd.entity.hospital.EngPtMessage;
import com.xiaojd.entity.util.Config;
import com.xiaojd.service.hospital.EngPtCfService;
import com.xiaojd.service.hospital.EngPtDeliveryAddressService;
import com.xiaojd.service.hospital.EngPtDeliveryService;
import com.xiaojd.service.hospital.EngPtDispenseService;
import com.xiaojd.service.hospital.EngPtDrugService;
import com.xiaojd.service.hospital.EngPtMessageService;
import com.xiaojd.service.hospital.EngPtPharmacyService;
import com.xiaojd.service.hospital.EngPtUserService;


/**
 * @author  
 * 药易平台的DLL所有方法都集中在一起
 *
 */

@Controller
public class DLLController  {

	Logger loger = LoggerFactory.getLogger(DLLController.class);
	@Resource
	EngPtCfService engPtCfService;
	@Resource
	EngPtDrugService engPtDrugService;
	@Resource
	EngPtMessageService engPtMessageService;
	@Resource
	EngPtDispenseService engPtDispenseService;
	@Resource
	EngPtUserService engPtUserService;
	@Resource
	EngPtDeliveryAddressService engPtDeliveryAddressService;
	@Resource
	EngPtDeliveryService engPtDeliveryService;
	@Resource
	EngPtPharmacyService engPtPharmacyService;
	//处方状态
	public static String CF_CANCLE="0"; //  取消
	public static String CF_NEW ="1";//新开   
	//-------------------社区发药--------------
	public static String CF_DISPENSE_SUCCESS ="2";//已发药
	public static String CF_DISPENSE_RETURN ="3";//退方
	
	//------------------药店派送---------------
	public static String CF__DELIVERY_PAID ="10";//已付款
	public static String CF_DELIVERY_TO_PHARMACY ="11";//分配到药房
	public static String CF_DELIVERY_TO_COURIER="12";//分配派送员 
	public static String CF_DELIVERY_SUCCESS="15";//配送完成
	public static String CF_DELIVERY_ERROR="16";//配送异常
	
	

	//-------------------------------------------DLL接口调用---------------------------
	/**
	 * 医院HIS系统将上次到平台上有处方取消
	 * @param model
	 * @param request
	 * @param response
	 * @throws Exception
	 */
	@RequestMapping(value = "/engine/runCancelCf")
	public void engineRunCancelCf( HttpServletRequest request, HttpServletResponse response) throws Exception {
		response.addHeader("Access-Control-Allow-Origin", "*");
		String presNo = (String) request.getParameter("presNo");
		String dllEncoding = Config.getValue("engine", "DLLEncoding");
		presNo = java.net.URLDecoder.decode(presNo,dllEncoding);
		String xml ="";
		if(presNo ==null ||"".equals(presNo)) {
			xml =  "<root><isSuccess>0</isSuccess></root>";
		} else{
			response.setContentType("text/html; charset=gbk");
			EngPtCf  ptCf = engPtCfService.loadById(presNo);
			String status =ptCf.getStatus();
		
			if(status !=null && !"".equals(status)&&("1".equals(status)||"0".equals(status))) {//（0取消 1：新开的药才可以取消
				ptCf.setStatus("0");
				engPtCfService.saveOrUpdate(ptCf);
				xml =  "<root><isSuccess>1</isSuccess></root>";
			} else {
				xml =  "<root><isSuccess>0</isSuccess></root>";
			}
		}
		PrintWriter pw = response.getWriter();
		pw.print(xml);
		pw.flush();
		pw.close();
	}
	
	/**社区医院或药店通过平台调取处方
	 * @param model
	 * @param request
	 * @param response
	 * @throws Exception
	 */
	@RequestMapping(value = "/engine/runGetCf")
	public void engineRunＧetCf( HttpServletRequest request, HttpServletResponse response) throws Exception {
		response.addHeader("Access-Control-Allow-Origin", "*");
		String dllEncoding = Config.getValue("engine", "DLLEncoding");
		String cardNo =  java.net.URLDecoder.decode((String) request.getParameter("cardNo"),dllEncoding);
		String name =  java.net.URLDecoder.decode((String) request.getParameter("name"),dllEncoding);
		String phoneNo =  java.net.URLDecoder.decode((String) request.getParameter("phoneNo"),dllEncoding);
		String idNo =  java.net.URLDecoder.decode((String) request.getParameter("idNo"),dllEncoding);

		if(idNo ==null) {
			idNo="";
		}
		idNo = idNo.replaceAll(" ", "");
		if(cardNo == null) {
			cardNo ="";
		} 
		cardNo = cardNo.replaceAll(" ", "");
		if(phoneNo ==null) {
			phoneNo ="";
		}
		phoneNo = phoneNo.replaceAll(" ", "");
		if(name ==null) {
			name ="";
		}
		name = name.replaceAll(" ", "");
		
		   String xml = "<roots>";
		
			List<EngPtCf> ptCfList = engPtCfService.loadByCardAndName(cardNo, name, idNo,phoneNo);
			if(ptCfList == null ||ptCfList.size()  ==0) {
				xml +="<isSuccess>0<isSuccess></roots>";
			} else {
				for(int i=0;i<ptCfList.size();i++) {
					   xml +="<root>";
					   xml +=ptCfList.get(i).toXml();
					  List<EngPtDrug> ptDrugList = engPtDrugService.loadByCfid(ptCfList.get(i).getId());
					  xml += " <prescriptions>";
					  for(int j=0;j<ptDrugList.size();j++) {
						  xml += ptDrugList.get(j).toXml();
					  }
					  xml += " </prescriptions>";
					  
					 List<EngPtMessage> ptMessageList = engPtMessageService.loadByCfid(ptCfList.get(i).getId());
					 xml += " <infos>";
					 for(int n=0;n<ptMessageList.size();n++) {
						  xml += ptMessageList.get(n).toXml();
					 }
					 xml += " </infos>";
					 xml +="</root>";
				}
				xml +="<isSuccess>1<isSuccess></roots>";
			}
			
		response.setContentType("text/html; charset=gbk");
		PrintWriter pw = response.getWriter();
		pw.print(xml);
		pw.flush();
		pw.close();
	}
	
	
	/**社区医院将从平台调取的处方处理后，把处理结果反馈给平台
	 * @param model
	 * @param request
	 * @param response
	 * @throws Exception
	 */
	@RequestMapping(value = "/engine/runBackCf")
	public void engineRunBackCf( HttpServletRequest request, HttpServletResponse response) throws Exception {
		response.addHeader("Access-Control-Allow-Origin", "*");
		String dllEncoding = Config.getValue("engine", "DLLEncoding");
		String presNo =  java.net.URLDecoder.decode((String) request.getParameter("presNo"),dllEncoding);
		String status =  java.net.URLDecoder.decode((String) request.getParameter("status"),dllEncoding);
		String hospital =  java.net.URLDecoder.decode((String) request.getParameter("hospital"),dllEncoding);
		String dept =  java.net.URLDecoder.decode((String) request.getParameter("dept"),dllEncoding);
		String doctor =  java.net.URLDecoder.decode((String) request.getParameter("doctor"),dllEncoding);
		String comment =  java.net.URLDecoder.decode((String) request.getParameter("comment"),dllEncoding);
		String xml =  "<root><isSuccess>1</isSuccess></root>";
		if(presNo ==null ||"".endsWith(presNo)||status ==null ||"".endsWith(status)||hospital ==null ||"".endsWith(hospital)||dept ==null ||"".endsWith(dept)||doctor ==null ||"".endsWith(doctor)) {
			xml ="<root><isSuccess>0</isSuccess></root>";
		} else {
			EngPtCf  ptCf = engPtCfService.loadById(presNo);
			if(	ptCf!=null &&"1".equals(ptCf.getStatus())) {
				EngPtDispense dispense = new EngPtDispense(ptCf.getPresNo(),ptCf.getStatus());
				ptCf.setStatus(status);//修改处方状态
				engPtCfService.saveOrUpdate(ptCf);
				dispense.setHospital(hospital);
				dispense.setComment(comment);
				dispense.setDepartment(dept);
				dispense.setDoctor(doctor);
			    engPtDispenseService.saveOrUpdate(dispense);
			} else {
				xml ="<root><isSuccess>0</isSuccess></root>";//不是新开的药，无法修改
			}
		}
		response.setContentType("text/html; charset=gbk");
		PrintWriter pw = response.getWriter();
		pw.print(xml);
		pw.flush();
		pw.close();
	}
	
	/**医院定时从平台读取处方的状态，然后刷新医院his系统的处方状态
	 * @param model
	 * @param request
	 * @param response
	 * @throws Exception
	 */
	@RequestMapping(value = "/engine/runRefreshCf")
	public void engineRunRefreshCf( HttpServletRequest request, HttpServletResponse response) throws Exception {
		response.addHeader("Access-Control-Allow-Origin", "*");
		String dllEncoding = Config.getValue("engine", "DLLEncoding");
		String presNos =  java.net.URLDecoder.decode((String) request.getParameter("presNos"),dllEncoding);
		String xml ="<root><infos>";
		if(presNos == null ||"".equals(presNos)) {
			xml ="<root><isSuccess>0</isSuccess></root>";
		} else {
			presNos =presNos.replaceAll(",", "','");
			presNos = "'" +presNos +"'";
			List<EngPtCf> ptCfs = engPtCfService.loadByPresNos(presNos);
			if(ptCfs == null ||ptCfs.size()==0) {
				xml ="<root><isSuccess>0</isSuccess></root>";
			} else {
	           for(int i=0;i<ptCfs.size();i++)  {
	        	   EngPtDispense  ptDispense = engPtDispenseService.loadByPresNo(ptCfs.get(i).getPresNo());
	        	   if(ptDispense == null) {
	        		   ptDispense = new EngPtDispense(ptCfs.get(i).getPresNo(),ptCfs.get(i).getStatus());
	        	   }
	        	   ptDispense.setStatus(ptCfs.get(i).getStatus());
			       xml += ptDispense.toXml();
	           }
				xml +="</infos><isSuccess>1</isSuccess></root>";
			}
		}
		response.setContentType("text/html; charset=gbk");
		PrintWriter pw = response.getWriter();
		pw.print(xml);
		pw.flush();
		pw.close();
	}
	
	
	@RequestMapping(value = "engine/runUpCf")
	public void engRunUpCf(ModelMap model, HttpServletRequest request, HttpServletResponse response) throws Exception {
		InputStream is = null;
		response.addHeader("Access-Control-Allow-Origin", "*");
		try {
			is = request.getInputStream();
			String engineEncoding = Config.getValue("engine", "hisEngineEncoding");
			InputStreamReader isr = new InputStreamReader(is, StringUtils.isEmpty(engineEncoding)?"GBK":engineEncoding);
			int ch;
			StringBuilder sbuf = new StringBuilder();
			while((ch = isr.read()) != -1){
				sbuf.append((char)ch);
			}
			String sb = sbuf.toString(); 
			String hisEngineLogFile = Config.getValue("engine", "hisEngineLog");
			if(StringUtils.isNotEmpty(hisEngineLogFile)){
				FileOutputStream fos = null;
				try{
					File logFile = new File(hisEngineLogFile);
					if(logFile != null){
						if(!logFile.getParentFile().exists()){
							logFile.getParentFile().mkdirs();
						}
						if(!logFile.exists()){
							logFile.createNewFile();
						}
						fos = new FileOutputStream(logFile);
						fos.write(sb.getBytes());
						fos.flush();
					}
				}catch(Exception e){
					e.printStackTrace();
					System.out.println("日志文件创建失败！");
				}finally{
					if(fos != null){
						try {
							fos.close();
						} catch (Exception e) {
							
						}
					}
				}//try-catch
			}
			

            String xml ="";
			xml = checkPtCf(sb.replaceAll("<root>", "").replaceAll("</root>",""));
			xml = xml.replaceAll("\\n", "");
			xml = xml.replaceAll("\\[[a-zA-Z0-9]+\\-.[a-zA-Z0-9]+\\]", "");
			
			xml = "<root>" + xml + "</root>";
			
			if(StringUtils.isNotEmpty(hisEngineLogFile)){
				FileOutputStream fos = null;
				try{
					hisEngineLogFile = hisEngineLogFile.replace("/", "\\");
					File logFile = new File(hisEngineLogFile.substring(0,hisEngineLogFile.lastIndexOf("\\"))+"\\output.log");
					if(logFile != null){
						if(!logFile.getParentFile().exists()){
							logFile.getParentFile().mkdirs();
						}
						if(!logFile.exists()){
							logFile.createNewFile();
						}
						fos = new FileOutputStream(logFile);
						fos.write(xml.getBytes());
						fos.flush();
					}
				}catch(Exception e){
					e.printStackTrace();
					System.out.println("日志文件创建失败！");
				}finally{
					if(fos != null){
						try {
							fos.close();
						} catch (Exception e) {
							
						}
					}
				}//try-catch
			}
			response.setContentType("text/html; charset=gbk");
			PrintWriter pw = response.getWriter();
			pw.print(xml);
			pw.flush();
			pw.close();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			is.close();
		}
	}
	
	@RequestMapping(value = "engine/runUpEmr")
	public void engRunUpEmr(ModelMap model, HttpServletRequest request, HttpServletResponse response) throws Exception {
		InputStream is = null;
		response.addHeader("Access-Control-Allow-Origin", "*");
		try {
			is = request.getInputStream();
			String engineEncoding = Config.getValue("engine", "hisEngineEncoding");
			InputStreamReader isr = new InputStreamReader(is, StringUtils.isEmpty(engineEncoding)?"GBK":engineEncoding);
			int ch;
			StringBuilder sbuf = new StringBuilder();
			while((ch = isr.read()) != -1){
				sbuf.append((char)ch);
			}
			String sb = sbuf.toString(); 
			String hisEngineLogFile = Config.getValue("engine", "hisEngineLog");
			if(StringUtils.isNotEmpty(hisEngineLogFile)){
				FileOutputStream fos = null;
				try{
					File logFile = new File(hisEngineLogFile);
					if(logFile != null){
						if(!logFile.getParentFile().exists()){
							logFile.getParentFile().mkdirs();
						}
						if(!logFile.exists()){
							logFile.createNewFile();
						}
						fos = new FileOutputStream(logFile);
						fos.write(sb.getBytes());
						fos.flush();
					}
				}catch(Exception e){
					e.printStackTrace();
					System.out.println("日志文件创建失败！");
				}finally{
					if(fos != null){
						try {
							fos.close();
						} catch (Exception e) {
							
						}
					}
				}//try-catch
			}
			
			String xml = "";	
			xml = xml.replaceAll("\\n", "");
			xml = xml.replaceAll("\\[[a-zA-Z0-9]+\\-.[a-zA-Z0-9]+\\]", "");
			
			xml = "<root>" + xml + "</root>";
			
			if(StringUtils.isNotEmpty(hisEngineLogFile)){
				FileOutputStream fos = null;
				try{
					hisEngineLogFile = hisEngineLogFile.replace("/", "\\");
					File logFile = new File(hisEngineLogFile.substring(0,hisEngineLogFile.lastIndexOf("\\"))+"\\output.log");
					if(logFile != null){
						if(!logFile.getParentFile().exists()){
							logFile.getParentFile().mkdirs();
						}
						if(!logFile.exists()){
							logFile.createNewFile();
						}
						fos = new FileOutputStream(logFile);
						fos.write(xml.getBytes());
						fos.flush();
					}
				}catch(Exception e){
					e.printStackTrace();
					System.out.println("日志文件创建失败！");
				}finally{
					if(fos != null){
						try {
							fos.close();
						} catch (Exception e) {
							
						}
					}
				}//try-catch
			}
			response.setContentType("text/html; charset=gbk");
			PrintWriter pw = response.getWriter();
			pw.print(xml);
			pw.flush();
			pw.close();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			is.close();
		}
	}
	
	
	@RequestMapping(value = "engine/runUpLis")
	public void engRunUpLis(ModelMap model, HttpServletRequest request, HttpServletResponse response) throws Exception {
		InputStream is = null;
		response.addHeader("Access-Control-Allow-Origin", "*");
		try {
			is = request.getInputStream();
			String engineEncoding = Config.getValue("engine", "hisEngineEncoding");
			InputStreamReader isr = new InputStreamReader(is, StringUtils.isEmpty(engineEncoding)?"GBK":engineEncoding);
			int ch;
			StringBuilder sbuf = new StringBuilder();
			while((ch = isr.read()) != -1){
				sbuf.append((char)ch);
			}
			String sb = sbuf.toString(); 
			String hisEngineLogFile = Config.getValue("engine", "hisEngineLog");
			if(StringUtils.isNotEmpty(hisEngineLogFile)){
				FileOutputStream fos = null;
				try{
					File logFile = new File(hisEngineLogFile);
					if(logFile != null){
						if(!logFile.getParentFile().exists()){
							logFile.getParentFile().mkdirs();
						}
						if(!logFile.exists()){
							logFile.createNewFile();
						}
						fos = new FileOutputStream(logFile);
						fos.write(sb.getBytes());
						fos.flush();
					}
				}catch(Exception e){
					e.printStackTrace();
					System.out.println("日志文件创建失败！");
				}finally{
					if(fos != null){
						try {
							fos.close();
						} catch (Exception e) {
							
						}
					}
				}//try-catch
			}
			
			String xml = "";
			xml = xml.replaceAll("\\n", "");
			xml = xml.replaceAll("\\[[a-zA-Z0-9]+\\-.[a-zA-Z0-9]+\\]", "");
			
			xml = "<root>" + xml + "</root>";
			
			if(StringUtils.isNotEmpty(hisEngineLogFile)){
				FileOutputStream fos = null;
				try{
					hisEngineLogFile = hisEngineLogFile.replace("/", "\\");
					File logFile = new File(hisEngineLogFile.substring(0,hisEngineLogFile.lastIndexOf("\\"))+"\\output.log");
					if(logFile != null){
						if(!logFile.getParentFile().exists()){
							logFile.getParentFile().mkdirs();
						}
						if(!logFile.exists()){
							logFile.createNewFile();
						}
						fos = new FileOutputStream(logFile);
						fos.write(xml.getBytes());
						fos.flush();
					}
				}catch(Exception e){
					e.printStackTrace();
					System.out.println("日志文件创建失败！");
				}finally{
					if(fos != null){
						try {
							fos.close();
						} catch (Exception e) {
							
						}
					}
				}//try-catch
			}
			response.setContentType("text/html; charset=gbk");
			PrintWriter pw = response.getWriter();
			pw.print(xml);
			pw.flush();
			pw.close();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			is.close();
		}
	}
	//-----------------------------------------页面跳转（老版本勿删）---------------------------

/*	@RequestMapping(value = "/homePt")
	public String home(ModelMap model){
		HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
		EngPtUser user = (EngPtUser) request.getSession().getAttribute("XIAOJD-PTUSER");
		String  roleId=user.getRole();		
		String validStr = "";
		if ("1001".equals(roleId)) {
			return "yaoyiPt/refer";		
		}else if ("1002".equals(roleId)){			
			return "yaoyiPt/deliveryInfo";
		}else{
				request.getSession().removeAttribute("XIAOJD-PTUSER");
				validStr = "该用户无权查看系统！";
				model.put("error", validStr);
				request.getSession().setAttribute("XIAOJD-PTURL", "loginPt");
				return "yaoyiPt/redirect";
		}	
	}
	
	
	
	@RequestMapping(value = "/loginPt")
	public String loginPt(ModelMap model, EngPtUser user){
		String validStr = "";//orgUserService.isValidUser(user);
		if(user.getCode() == null || user.getCode().isEmpty())
			validStr = "用户名不能为空！";
		else if(user.getPwd() == null || user.getPwd().isEmpty())
			validStr = "密码不能为空！";
		else {
			List dbUsers = engPtUserService.getUserByCode(user.getCode());
			if(dbUsers != null && dbUsers.size() > 0) {
				Object[] dbUser = (Object[])dbUsers.get(0);
				if(user.getPwd().equals(dbUser[2].toString())) {
					user.setId(dbUser[0].toString());
					user.setName(dbUser[1].toString());
					user.setPwd(dbUser[2].toString());
					user.setCode(dbUser[3].toString());
					user.setPharmacyId(dbUser[4].toString());
					user.setRole(dbUser[5].toString());
					user.setStatus(dbUser[6].toString());
					user.setPharmacy(dbUser[7].toString());
					validStr = "success";
				} else
					validStr = "密码错误！";
			} else {
				validStr = "该用户不存在！";
			}
		}
		
		if ("success".equals(validStr)) {
			HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
			request.getSession().setAttribute("XIAOJD-PTUSER", user);
			request.getSession().setMaxInactiveInterval(30*60);
            return "/yaoyiPt/redirect";
        } else {
        	model.put("error", validStr);
            return "/yaoyiPt/login";
        }
	}
	
    // 退出系统
	@RequestMapping(value = "/loginPtA")
	public String loginPtA(ModelMap model){
		HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
		request.getSession().removeAttribute("XIAOJD-PTUSER");
		return "yaoyiPt/login";
	}
	
	        // 退出系统
			@RequestMapping(value = "/logoutPt")
	 public String logoutPt(ModelMap model){
			HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
			request.getSession().removeAttribute("XIAOJD-PTUSER");		
			return "yaoyiPt/login";
	}
//-------------------------------------------业务逻辑-----------------------------------------
			*//**
			 * 根据病人基本信息查询处方
			 * @param idCard
			 * @param cardNo
			 * @param phoneNo
			 * @param name
			 * @param status
			 * @return
			 *//*
			@RequestMapping(value = "loadCfByPatientInfo")
			@ResponseBody
			public  Map<String, Object> loadCfByPatientInfo( String idCard, String cardNo, String phoneNo
					, String name, String status) {
				
				if(idCard ==null) {
					idCard="";
				}
				idCard = idCard.replaceAll(" ", "");
				if(cardNo == null) {
					cardNo ="";
				} 
				cardNo = cardNo.replaceAll(" ", "");
				if(phoneNo ==null) {
					phoneNo ="";
				}
				phoneNo = phoneNo.replaceAll(" ", "");
				if(name ==null) {
					name ="";
				}
				name = name.replaceAll(" ", "");
				if(status ==null) {
					status ="";
				}
				status = status.replaceAll(" ", "");
				Map<String, Object> ret = new HashMap<String, Object>();
				List<EngPtCf> ptCfList = new ArrayList<EngPtCf>();
				if(!"".equals(status)){
					 ptCfList = engPtCfService.loadByStatus(cardNo, name, idCard,phoneNo,status);
				}else{
					 ptCfList = engPtCfService.loadByCardAndName(cardNo, name, idCard,phoneNo);
				}
				if(ptCfList!=null&&ptCfList.size()>0){
					ret.put("data", ptCfList);
					ret.put("success", "true");
					return ret;			
					}	
				   ret.put("success", "false");
				   return ret;	
				}
					
			//备注

	
			*//**查询病人基本信息，包括地址，手机号
			 * @param patientNo
			 * @param id
			 * @return
			 *//*
			@RequestMapping(value = "loadPtDeliveryAddressByPatientNo")
			public @ResponseBody 
			List<EngPtDeliveryAddress> loadPtDeliveryAddressByPatientNo(@RequestParam String patientNo) {	
					List<EngPtDeliveryAddress> address = engPtDeliveryAddressService.loadByPatientNo(patientNo);
					if (address!=null && address.size()>0) {
						return address;
					}			
				   return null;
			}
			
			*//**保存病人基本信息，包括地址，手机号
			 * @param patientNo
			 * @param id
			 * @return
			 *//*
			@RequestMapping(value = "savePtDeliveryAddress")
			public @ResponseBody Long savePtDeliveryAddress(String id,String  patientNo,String phoneNo,String address,String name ) {	
				     EngPtDeliveryAddress  deliveryAddress = new EngPtDeliveryAddress();
					 Long idLong =0l;
				     if(id!=null && !"".equals(id)) {
				    	try {
				    		 idLong = Long.parseLong(id);
				    	 } catch(Exception e) {
				    		 idLong =-1l;
				    	 }
				     } 
				     deliveryAddress.setAddress(address);
				     deliveryAddress.setName(name);
				     deliveryAddress.setPatientNo(patientNo);
				     deliveryAddress.setPhoneNo(phoneNo);
				     if(idLong ==0) {
					     idLong =engPtDeliveryAddressService.saveAddress(deliveryAddress);
				     }
				     if(idLong >0){
				    	  deliveryAddress.setId(idLong);
				    	  engPtDeliveryAddressService.saveOrUpdate(deliveryAddress);
				     }
				     return idLong;	  
			}
			
			*//**
			 * 保存病人基本信息
			 * @param id
			 * @param name
			 * @param address
			 * @param phoneNo
			 *//*
			@RequestMapping(value = "savePatientInfo")
			public @ResponseBody 
			void savePatientInfo(@RequestParam String id,@RequestParam String name,@RequestParam String address,@RequestParam String phoneNo) {	
				if(!"".equals(id)){
					EngPtDeliveryAddress patientInfo = engPtDeliveryAddressService.loadById(id);
					patientInfo.setAddress(address);
					patientInfo.setName(name);
					patientInfo.setPhoneNo(phoneNo);
					engPtDeliveryAddressService.saveOrUpdate(patientInfo);
				}			
			}
			
			*//**根据病人处方号查找病人处方,和药品信息
			 * @param id
			 * @return
			 *//*
			@RequestMapping(value = "loadPtCfById")
			public @ResponseBody 
			Map<String, Object> loadPtCfById(@RequestParam String id) {
				Map<String, Object> ret = null;
				if(id != null && id.trim().length() > 0){			
					ret = new HashMap<String, Object>();
					EngPtCf cf = engPtCfService.loadById(id);
					List<EngPtDrug> item = engPtDrugService.loadByCfid(id);
					ret.put("data", cf);
					ret.put("item", item);
				}
				return ret;
			}
			
			*//**
			 * 根据病人处方号查找派送信息
			 * @param id
			 * @param patientNo
			 * @return
			 *//*
			@RequestMapping(value = "loadPtDeliveryById")
			public @ResponseBody 
			Map<String, Object> loadPtDeliveryById(@RequestParam String id,@RequestParam String patientNo) {
				if(id != null && id.trim().length() > 0){			
					Map<String, Object> ret = new HashMap<String, Object>();			
					List<EngPtDrug> item = engPtDrugService.loadByCfid(id);
					ret.put("item", item);
					if("".equals(patientNo)&&patientNo!=null){
						List<EngPtDeliveryAddress> patientInfoList = engPtDeliveryAddressService.loadByPatientNo(patientNo);
						if (patientInfoList!=null && patientInfoList.size()>0) {
							ret.put("data", patientInfoList);
						}	
					}	
					return ret;
				}
				return null;
			}
			
			*//**
			 * 通过快递员的编号查找药派送的处方
			 * @param courier
			 * @param status
			 * @return
			 *//*
			@RequestMapping(value = "loadPtCfByCourier")
			public @ResponseBody 
			Map<String, Object> loadPtCfByCourier(@RequestParam long courierId,@RequestParam String status) {
				    Map<String, Object> ret = new HashMap<String, Object>();
					List<EngPtDelivery> deliList = engPtDeliveryService.loadByCourier(courierId);
					List<EngPtCf> ptCfList = new ArrayList<EngPtCf>();
	
				    for(int i=0;i<deliList.size();i++){
							EngPtDelivery deli= deliList.get(i);
							String id = deli.getCfId();
							EngPtCf ptCf = engPtCfService.loadById(id);
							String cfStatus=ptCf.getStatus();
							if(status.equals(cfStatus)){
								ptCfList.add(ptCf);
								}
						}						
					ret.put("data", ptCfList);
					ret.put("deli", deliList);
				return ret;
			}
			
//-----------------------------------------------------------------------修改处方状态，编辑订单-----------------------	
			*//**
			 * 修改处方状态 付款
			 * @param cfId
			 * @param status
			 * @param handler
			 *//*
			@RequestMapping(value = "changePtCfStatusToPaid")
			public @ResponseBody 
			Map<String, String> changePtCfStatusToPaid(@RequestParam String cfId) {		
				   Map<String, String> ret = new HashMap<String, String>();
					EngPtCf  ptCf = engPtCfService.loadById(cfId);
					if(ptCf == null) {
						ret.put("status", "-1");
						ret.put("message", "查询不到该处方！");
					} else {
						if(CF_NEW.equals(ptCf.getStatus())) {
							ptCf.setStatus(CF__DELIVERY_PAID );//收款
							engPtCfService.saveOrUpdate(ptCf);
							ret.put("status", "1");
							ret.put("message", "付款成功！");
							return ret;
						} else {
							ret.put("status", "-1");
							ret.put("message", "处方状态异常！");
						}
					}
					return ret;
			}
			
			
			*//**	添加派送信息,   已付款->分配到药房
			 * @param cfId  处方ID
			 * @param pharmacyId 药房ID
			 * @param address 收件人地址
			 * @param phoneNo 收件人联系电话
			 * @param name 收件人
			 * @param remark 备注
			 *//*
			@RequestMapping(value = "saveCfDeliverInfo")
			public @ResponseBody 
			Map<String,String> saveCfDeliverInfo(@RequestParam String cfId,@RequestParam long pharmacyId,
					@RequestParam String address,@RequestParam String phoneNo,
					@RequestParam String name,@RequestParam String  remark) {
				    Map<String,String> map = new HashMap<String,String>();
					EngPtCf ptCf= engPtCfService.loadById(cfId);
					String status = ptCf.getStatus();
					//先查询处方状态，只有已付款的才可以添加派送
					if(status==null ||!CF__DELIVERY_PAID.equals(status)) {
						map.put("status", "-1");//该处方订单已经存在
    					map.put("message","处方状态异常");
    					return map;
					}
					EngPtDelivery deli = engPtDeliveryService.loadByCfId(cfId);
					if(deli != null) {
						map.put("status", "-1");//该处方订单已经存在
    					map.put("message","该处方订单已经存在");
    					return map;
					} else {
						deli = new  EngPtDelivery();
						deli.setCfId(cfId);
						deli.setPharmacyId(pharmacyId);
						deli.setPhoneNo(phoneNo);
						deli.setAddress(address);
						deli.setName(name);
						deli.setRemark(remark);
						deli.setPharmacyTime(new Timestamp(System.currentTimeMillis()));
						long deliveryId = engPtDeliveryService.saveDelivery(deli);
						ptCf.setStatus(CF_DELIVERY_TO_PHARMACY );//分配到药房
						engPtCfService.saveOrUpdate(ptCf);	
						map.put("status", "1");
						map.put("message", "订单生成成功");
						map.put("delivery","" + deliveryId);
						return map;
					}
			}

			*//**分配到药房后可以修改派送基本信息,不修改状态
			 * @param cfId  处方ID
			 * @param address 收件人地址
			 * @param phoneNo 收件人联系电话
			 * @param name 收件人
			 * @param remark 备注
			 *//*
			@RequestMapping(value = "saveCfDeliveryPharmacy")
			public @ResponseBody 
			Map<String,String> saveCfDeliveryPharmacy(@RequestParam String cfId,
					@RequestParam String address,@RequestParam String phoneNo,
					@RequestParam String name,@RequestParam String  remark) {
				    Map<String,String> map = new HashMap<String,String>();
					EngPtCf ptCf= engPtCfService.loadById(cfId);
					String status = ptCf.getStatus();
					//先查询处方状态，只有分配到药房才可以添加派送
					if(status==null ||!CF_DELIVERY_TO_PHARMACY .equals(status)) {
						map.put("status", "-1");//该处方订单已经存在
    					map.put("message","处方状态异常");
    					return map;
					}
					
					EngPtDelivery deli = engPtDeliveryService.loadByCfId(cfId);
					if(deli != null) {
						deli.setPhoneNo(phoneNo);
						deli.setAddress(address);
						deli.setName(name);
						deli.setRemark(remark);
					    engPtDeliveryService.saveOrUpdateDelivery(deli);
						map.put("status", "1");
						map.put("message", "订单修改成功");
						return map;
					} 
					map.put("status", "-1");
					map.put("message", "订单不存在");
					return map;
			}
			
			
		

			
			*//**
			 * 忘记密码修改  ptChangPassword
			 * @return
			 * @throws Exception
			 *//*
			@RequestMapping(value = "ptChangePassword")
			public  String ptChangPassword() throws Exception {
				return "yaoyiPt/changePassword";
			}
			
			*//**
			 * 订单查询页面
			 * @return
			 * @throws Exception
			 *//*
			@RequestMapping(value = "ptDetail")
			public   String getPtDetail(ModelMap resultMap,@RequestParam String id) throws Exception {
				if(id != null && id.trim().length() > 0){			
					EngPtCf cf = engPtCfService.loadById(id);
					List<EngPtDrug> item = engPtDrugService.loadByCfid(id);
					resultMap.put("cf", cf);
					resultMap.put("drugs", item);
				}
				return "yaoyiPt/detail";
			}
			
			*//**
			 * 订单分配到药房
			 * @return
			 * @throws Exception
			 *//*
			@RequestMapping(value = "ptDistributionPharmacy")
		public  String ptDistributionPharmacy(ModelMap resultMap,@RequestParam String id)  throws Exception {
				
     		if(id != null && id.trim().length() > 0){			
					EngPtCf cf = engPtCfService.loadById(id);
					List<String> areaList = engPtPharmacyService.getPharmacysAreas();
					resultMap.put("cf", cf);
					resultMap.put("areaList", areaList);
			}	
				return "yaoyiPt/informationReconfirmation";
			}
            
			
			*//**
			 * 订单分配到药房
			 * @return
			 * @throws Exception
			 *//*
			@RequestMapping(value = "ptGetPharmacyByArea")
		public @ResponseBody List<EngPtPharmacy> ptGetPharmacyByArea(ModelMap resultMap,@RequestParam String area)  throws Exception {				
     		if(area != null && area.trim().length() > 0){
					List<EngPtPharmacy> pharmacyList = engPtPharmacyService.getPharmacysByArea(area); 
					return   pharmacyList;
     		} else {
     			   return  null;
     		}
		}

			*//**
			 * 订单分配,派送  11 分配到药房,可以编辑地址
			 * @return
			 * @throws Exception
			 *//*
			@RequestMapping(value = "ptDistribution")
		   public  String getPtDistribution(ModelMap resultMap,@RequestParam String id)  throws Exception {
				EngPtCf cf = new EngPtCf();
				EngPtDelivery  engDelivery = new  EngPtDelivery();
				EngPtPharmacy engPtPharmacy = new EngPtPharmacy();
				List<EngPtDrug> item = new  ArrayList<EngPtDrug>();
				if(id != null && id.trim().length() > 0){			
					cf = engPtCfService.loadById(id);
					if(cf != null) {
						//分配到药房,可以编辑地址
						if(CF_DELIVERY_TO_PHARMACY.equals(cf.getStatus())) {
							engDelivery = engPtDeliveryService.loadByCfId(id);
							item = engPtDrugService.loadByCfid(id);
							engPtPharmacy =  engPtPharmacyService.loadById(engDelivery.getPharmacyId());
						}
					} else {
						cf = new EngPtCf();
					}
				}
				resultMap.put("cf", cf);
				resultMap.put("drugs", item);
				resultMap.put("delivery", engDelivery);
				resultMap.put("pharmacy", engPtPharmacy);
				
				return "yaoyiPt/distribution";
			}
			
			
			*//**
			 * 订单进度
			 * @return
			 * @throws Exception
			 *//*
			@RequestMapping(value = "ptDeliveryProgress")
			public  String ptDeliveryProgress(ModelMap resultMap,@RequestParam String id)  throws Exception {
				EngPtCf cf = new EngPtCf();
				EngPtDelivery  engDelivery = new  EngPtDelivery();
				EngPtPharmacy engPtPharmacy = new EngPtPharmacy();
				cf = engPtCfService.loadById(id);
				if(cf!=null) {
				   engDelivery = engPtDeliveryService.loadByCfId(id);
				   engPtPharmacy =  engPtPharmacyService.loadById(engDelivery.getPharmacyId());
				}
				cf = new EngPtCf();
				resultMap.put("cf", cf);
				resultMap.put("delivery", engDelivery);
				resultMap.put("pharmacy", engPtPharmacy);
				return "yaoyiPt/orderInformation";
		 }		
			
			
	//----------------------------------------手机端登录访问----------------------------
			@RequestMapping(value = "/mobileHomePt")
			public String mobileHome(ModelMap model){
				HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
				EngPtUser user = (EngPtUser) request.getSession().getAttribute("XIAOJD-PTUSER");
				String  roleId=user.getRole();		
				String validStr = "";
	            if ("1002".equals(roleId)){			
					   return "yaoyiPt/mobile/all";
				}else{
						request.getSession().removeAttribute("XIAOJD-PTUSER");
						validStr = "该用户无权查看系统！";
						model.put("error", validStr);
						request.getSession().setAttribute("XIAOJD-PTURL", "mobile/loginPt");
						return "yaoyiPt/mobile/redirect";
				}	
			}
			
			
			
			@RequestMapping(value = "mobileLoginPt")
			public String mobileLoginPt(ModelMap model, EngPtUser user){
				String validStr = "";
				if(user.getCode() == null || user.getCode().isEmpty())
					validStr = "用户名不能为空！";
				else if(user.getPwd() == null || user.getPwd().isEmpty())
					validStr = "密码不能为空！";
				else {
					List dbUsers = engPtUserService.getUserByCode(user.getCode());
					if(dbUsers != null && dbUsers.size() > 0) {
						Object[] dbUser = (Object[])dbUsers.get(0);
						if(user.getPwd().equals(dbUser[2].toString())) {
							user.setId(dbUser[0].toString());
							user.setName(dbUser[1].toString());
							user.setPwd(dbUser[2].toString());
							user.setCode(dbUser[3].toString());
							user.setPharmacyId(dbUser[4].toString());
							user.setRole(dbUser[5].toString());
							user.setStatus(dbUser[6].toString());
							user.setPharmacy(dbUser[7].toString());
							validStr = "success";
						} else
							validStr = "密码错误！";
					} else {
						validStr = "该用户不存在！";
					}
				}
				
				if ("success".equals(validStr)) {
					HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
					request.getSession().setAttribute("XIAOJD-PTUSER", user);
					request.getSession().setMaxInactiveInterval(30*60);
		            return "/yaoyiPt/mobile/redirect";
		        } else {
		        	model.put("error", validStr);
		            return "/yaoyiPt/mobile/login";
		        }
			}
			
		    // 退出系统
			@RequestMapping(value = "mobileLoginPtA")
			public String mobileloginPtA(ModelMap model){
				HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
				request.getSession().removeAttribute("XIAOJD-PTUSER");
				return "yaoyiPt/mobile/login";
			}
//-----------------------------------------------------------------------------------------------------------------------------------		
		   
			// 查看 新配送   区分药店和派送员角色
			@RequestMapping(value = "mobileNew")
			public String mobileNew(ModelMap model){
				return "yaoyiPt/mobile/new";
			}
			
			// 查看 全部配送 区分药店和派送员角色
			@RequestMapping(value = "mobileAll")
			public String mobileAll(ModelMap model){
				return "yaoyiPt/mobile/all";
			}
			
			// 查看 个人中心 区分药店和派送员角色
			@RequestMapping(value = "mobileUser")
			public String mobileUser(ModelMap model){
				return "yaoyiPt/mobile/all";
			}
			
			
			*//**
			 * 订单派送给送货员
			 * @param cfId
			 * @param courierId
			 *//*
			@RequestMapping(value = "ptDelivertyToCourier")
			public @ResponseBody 
			Map<String, String> ptDelivertyToCourier(@RequestParam String cfId,@RequestParam long courierId) {		
				   Map<String, String> ret = new HashMap<String, String>();
					EngPtCf  ptCf = engPtCfService.loadById(cfId);
					if(ptCf == null) {
						ret.put("status", "-1");
						ret.put("message", "查询不到该处方！");
					} else {
						if(CF_DELIVERY_TO_PHARMACY .equals(ptCf.getStatus())) {
							ptCf.setStatus(CF_DELIVERY_TO_COURIER );//分配到派送员
							EngPtDelivery engDelivery = engPtDeliveryService.loadByCfId(cfId);
							engDelivery.setCourierId(courierId);//添加派送员
							engDelivery.setCashierStartTime(new Timestamp(System.currentTimeMillis()));
							engPtDeliveryService.saveOrUpdateDelivery(engDelivery);
							engPtCfService.saveOrUpdate(ptCf);
							ret.put("status", "1");
							ret.put("message", "分配到派送员成功！");
							return ret;
						} else {
							ret.put("status", "-1");
							ret.put("message", "处方状态异常！");
						}
					}
					return ret;
			}
			
			*//**
			 * 修改处方状态
			 * @param cfId
			 * @param status
			 * @param handler
			 *//*
			@RequestMapping(value = "changePtCfStatus")
			public @ResponseBody 
			void changePtCfStatus(@RequestParam String cfId,@RequestParam String status) {		
					EngPtCf  ptCf = engPtCfService.loadById(cfId);
					ptCf.setStatus(status);
					engPtCfService.saveOrUpdate(ptCf);			
					EngPtDelivery engDelivery=engPtDeliveryService.loadByCfId(cfId);
					if(CF__DELIVERY_PAID .equals(status)){
						//engDelivery.setChargeTime(new Timestamp(System.currentTimeMillis()));
						//engDelivery.setCashier(handler);
					}		
					if(CF_DELIVERY_TO_PHARMACY .equals(status)){
						//engDelivery.setWaitTime(new Timestamp(System.currentTimeMillis()));
					}
					if(CF_DELIVERY_TO_COURIER.equals(status)){
						//engDelivery.setStartTime(new Timestamp(System.currentTimeMillis()));
						//engDelivery.setCourier(handler);
					}
					if("CF_DELIVERY_SUCCESS".equals(status)){
						//engDelivery.setCompleteTime(new Timestamp(System.currentTimeMillis()));
					}
					engPtDeliveryService.saveDelivery(engDelivery);				
			}*/
			
            static private String checkPtCf(String s) {
            	Connection con = null;
    			String presNo = StringUtils.find(s, "<presNo>(###*?)</presNo>");
				try {
					con = ConManager.getConn();
					String sql4 = "select status from eng_pt_cf where pres_no = ? ";
					PreparedStatement pst4 = con.prepareStatement(sql4);
					pst4.setString(1, presNo);
					ResultSet rs4 = pst4.executeQuery();
					if (rs4.next()) {
						String status = rs4.getString("status");
					   if("0".equals(status)||"1".equals(status)||"3".equals(status) ) {	//只有0取消/1新建/3退方的处方才可以修改
							writePtDB(s);
							return s+ "<isSuccess>1</isSuccess>";
						} else {
							return s+ "<infos></infos><isSuccess>0</isSuccess>";
						}
					} else {
						writePtDB(s);//只有新建的处方才可以修改
						return s+"<isSuccess>1</isSuccess>";
					}	
					
				} catch (Exception a) {
					a.printStackTrace();
				} finally {
					ConManager.close(con);
				}
				return s+"<isSuccess>0</isSuccess>";
            }
			static private void writePtDB(String s) {
				
			Connection con = null;
			String presNo = StringUtils.find(s, "<presNo>(###*?)</presNo>");
			String patientNo = StringUtils.find(s, "<patientNo>(###*?)</patientNo>");
			String presDatetime = StringUtils.find(s,"<presDatetime>(###*?)</presDatetime>");
			if ("".equals(presNo))
				return;
			if (presNo.startsWith("Z0") || presNo.startsWith("z0")) {
				presNo = "Z" + patientNo + presDatetime.substring(0, 10);
			}
			try {
				con = ConManager.getConn();
				if (StringUtils.find(s, "<prescriptions>(###*)</prescriptions>").trim().length() == 0) {
					return;
				}
				
				//覆盖以前处方记录
				String sql1 = "delete from eng_pt_drug where cfid =?";
				String sql2 = "delete from eng_pt_cf where pres_no = ?";
				String sql3 = "delete from eng_pt_message where cf_id =?";
				String sql4 = "select id from eng_pt_cf where pres_no = ?";
				//String sql5 = "delete from eng_pt_dispense where pres_no =?";

				PreparedStatement pst4 = con.prepareStatement(sql4);
				pst4.setString(1, presNo);
				ResultSet rs4 = pst4.executeQuery();
				if (rs4.next()) {
					String cfId = rs4.getString("id");
					PreparedStatement pst1 = con.prepareStatement(sql1);
					pst1.setString(1, cfId);
					pst1.execute();
					pst1.close();
					PreparedStatement pst2 = con.prepareStatement(sql2);
					pst2.setString(1, presNo);
					pst2.execute();
					pst2.close();
					PreparedStatement pst3 = con.prepareStatement(sql3);
					pst3.setString(1, cfId);
					pst3.execute();
					pst3.close();
					//PreparedStatement pst5 = con.prepareStatement(sql5);
					//pst5.setString(1, cfId);
					//pst5.execute();
					//pst5.close();
				}
				rs4.close();
				pst4.close();
				
				String t = StringUtils.find(s, "<patient>(###*?)</patient>");
				EngPtCf cf = new EngPtCf();
				cf.setId(presNo);
				cf.setAddress(StringUtils.find(t, "<address>(###*)</address>"));
				cf.setAge(StringUtils.find(t, "<age>(###*)</age>"));
				cf.setAllergyList(StringUtils.find(t,"<allergyList>(###*)</allergyList>"));
				cf.setBedNo(StringUtils.find(t, "<bedNo>(###*)</bedNo>"));
				cf.setBirthWeight(StringUtils.find(t,"<birthWeight>(###*)</birthWeight>"));
				cf.setBreastFeeding(StringUtils.find(t,"<breastFeeding>(###*)</breastFeeding>"));
				cf.setCcr(StringUtils.find(t, "<ccr>(###*)</ccr>"));
				cf.setDepartId(StringUtils.find(t, "<departID>(###*)</departID>"));
				cf.setDepartment(StringUtils.find(t,"<department>(###*?)</department>"));
				cf.setDiagnose(StringUtils.find(t, "<diagnose>(###*)</diagnose>"));
				cf.setDialysis(StringUtils.find(t, "<dialysis>(###*)</dialysis>"));
				cf.setDocId(StringUtils.find(t, "<docID>(###*)</docID>"));
				cf.setDocName(StringUtils.find(t, "<docName>(###*)</docName>"));
				cf.setHeight(StringUtils.find(t, "<height>(###*)</height>"));
				cf.setIdCard(StringUtils.find(t, "<IDCard>(###*)</IDCard>"));
				cf.setName(StringUtils.find(t, "<name>(###*)</name>"));
				cf.setPatientNo(StringUtils.find(t, "<patientNo>(###*)</patientNo>"));
				cf.setPayType(StringUtils.find(t, "<payType>(###*)</payType>"));
				cf.setPhoneNo(StringUtils.find(t, "<phoneNo>(###*)</phoneNo>"));
				cf.setPregnancy(StringUtils.find(t, "<pregnancy>(###*)</pregnancy>"));
				String pdt = StringUtils.find(t,"<presDatetime>(###*)</presDatetime>");
				cf.setCreateDate(new Timestamp(System.currentTimeMillis()));
				cf.setUpdateDate(new Timestamp(System.currentTimeMillis()));
				try {
					cf.setPresDateTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
							.format(StringUtils.double2Date(Double.parseDouble(pdt))));
				} catch (Exception e) {
					cf.setPresDateTime(pdt);
				}
				cf.setPresNo(presNo);
				cf.setPresType(StringUtils.find(t,"<presType>(###*)</presType>"));
				cf.setProxIdCard(StringUtils.find(t,"<proxIDCard>(###*)</proxIDCard>"));
				cf.setProxName(StringUtils.find(t, "<proxName>(###*)</proxName>"));
				cf.setSex(StringUtils.find(t, "<sex>(###*)</sex>"));
				cf.setTimeOfPreg(StringUtils.find(t,"<timeOfPreg>(###*)</timeOfPreg>"));
				cf.setTotalAmount(StringUtils.find(t,"<totalAmount>(###*)</totalAmount>"));
				cf.setWeight(StringUtils.find(t, "<weight>(###*)</weight>"));
				cf.setPresSource(StringUtils.find(t,"<presSource>(###*)</presSource>"));
				cf.setPharmChkId(StringUtils.find(t,"<pharmChkId>(###*)</pharmChkId>"));
				cf.setPharmChkName(StringUtils.find(t,"<pharmChkName>(###*)</pharmChkName>"));
				cf.setPharmChkTitle(StringUtils.find(t,"<pharmChkTitle>(###*)</pharmChkTitle>"));
				cf.setDocTitle(StringUtils.find(t, "<docTitle>(###*)</docTitle>"));
				cf.setCardNo(StringUtils.find(t, "<cardNo>(###*)</cardNo>"));
				cf.setAst(StringUtils.find(t, "<ast>(###*)</ast>"));
				cf.setScr(StringUtils.find(t, "<scr>(###*)</scr>"));
				cf.setAlt(StringUtils.find(t, "<alt>(###*)</alt>"));
				cf.setBsa(StringUtils.find(t, "<bsa>(###*)</bsa>"));
				cf.setDrugSensivity(StringUtils.find(t, "<drugSensivity>(###*)</drugSensivity>"));
				cf.setStatus("1");//新开
				cf.save(con);
				//添加处方时，同时添加
				//EngPtDispense ptMessage = new EngPtDispense();
				//ptMessage.setPresNo(presNo);
				//ptMessage.save(con);
				
				t = StringUtils.find(s, "<prescriptions>(###*)</prescriptions>");
				Iterator<String> it = StringUtils.findAll(t,"<prescription>(###*?)</prescription>");
				int i = 0;
				while (it.hasNext()) {
					String p = it.next();			
					EngPtDrug item = new EngPtDrug();
					item.setId(cf.getId()+"_"+(i+1));
					item.setCfid(cf.getId());
					item.setDrug(StringUtils.find(p, "<drug>(###*)</drug>"));
					item.setDrugName(StringUtils.find(p,"<drugName>(###*)</drugName>"));
					item.setPrepForm(StringUtils.find(p,"<prepForm>(###*)</prepForm>"));
					item.setStartDate(StringUtils.find(p,"<startTime>(###*)</startTime>"));
					item.setType(StringUtils.find(p, "<type>(###*)</type>"));
					item.setAdminArea(StringUtils.find(p,"<adminArea>(###*)</adminArea>"));
					item.setAdminDose(StringUtils.find(p,"<adminDose>(###*)</adminDose>"));
					item.setAdminFrequency(StringUtils.find(p,"<adminFrequency>(###*)</adminFrequency>"));
					item.setAdminMethod(StringUtils.find(p,"<adminMethod>(###*)</adminMethod>"));
					item.setAdminRoute(StringUtils.find(p,"<adminRoute>(###*)</adminRoute>"));
					item.setContinueDay(StringUtils.find(p,"<continueDays>(###*)</continueDays>"));
					item.setFirstUse(StringUtils.find(p,"<firstUse>(###*)</firstUse>"));
					item.setSkinTest(StringUtils.find(p,"<skinTest>(###*)</skinTest>"));
					item.setGroupNo(StringUtils.find(p, "<groupNo>(###*)</groupNo>"));
					item.setAdminMethod(StringUtils.find(p, "<adminMethod>(###*)</adminMethod>"));
					item.setPack(StringUtils.find(p, "<package>(###*)</package>"));
					item.setPackUnit(StringUtils.find(p,"<packUnit>(###*)</packUnit>"));	
					item.setRegName(StringUtils.find(p, "<regName>(###*)</regName>"));
					item.setSpec(StringUtils.find(p,"<specification>(###*)</specification>"));
					item.setQty(StringUtils.find(p,"<qty>(###*)</qty>"));
					item.setQtyUnit(StringUtils.find(p,"<qtyUnit>(###*)</qtyUnit>"));
					item.setContent(StringUtils.find(p,"<content>(###*)</content>"));
					item.setContentUnit(StringUtils.find(p,"<contentUnit>(###*)</contentUnit>"));
					item.setQuantity(StringUtils.find(p,"<quantity>(###*)</quantity>"));
					item.setDispenseUnit(StringUtils.find(p,"<dispenseUnit>(###*)</dispenseUnit>"));
					item.setUnitPrice(StringUtils.find(p,"<unitPrice>(###*)</unitPrice>"));
					item.setAmount(StringUtils.find(p, "<amount>(###*)</amount>"));
					item.save(con);
					i++;
				}

				//Iterator<com.dh.health.hospital.Info> messages = ResultInfo.iterator();
				//while (messages.hasNext()) {
				//	com.dh.health.hospital.Info info = messages.next();
				//	if (info.getSeverity() < 3) {
				//		continue;
				//	}
					//EngPtMessage m = new EngPtMessage();
				//	m.setDrugCode(info.getDrugCode().replaceFirst("\\*", ""));
				//	m.setDrugName(info.getDrugName().replaceFirst("\\*", ""));
				//	m.setMessage(info.getMessage().trim().replaceAll("\\s+", ""));
				//	m.setAdvice(info.getAdvice().trim().replaceAll("\\s+", ""));
				//	m.setSeverity(info.getSeverity() + "");
				//	m.setSource(info.getSource().trim());
				//	m.setPresDateTime(cf.getPresDateTime());
				//	m.setDocName(cf.getDocName());
				//	m.setDepartment(cf.getDepartment());
				//	m.setMessageId(info.getMessageId());
				//	m.setType(info.getType());
					//m.setDetailId(info.getDetailId()); 为空值
				//	m.save(con);
				//}

			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				ConManager.close(con);
			}
		}
			
}
