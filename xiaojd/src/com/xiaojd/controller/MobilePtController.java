package com.xiaojd.controller;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import com.xiaojd.entity.hospital.EngPtDelivery;
import com.xiaojd.entity.hospital.EngPtDrug;
import com.xiaojd.entity.hospital.EngPtUser;
import com.xiaojd.entity.util.PTSTATUS;
import com.xiaojd.entity.hospital.EngPtCf;
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
 * 药易平台的所有方法都集中在一起
 *
 */
/**
 * @author CZ
 *
 */

@Controller
public class MobilePtController  {

	Logger loger = LoggerFactory.getLogger(MobilePtController.class);
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

	//---------------------人员角色------------------
	public static String PT_ADMIN ="1001"; //平台管理员
	public static String PT_COURIER_P ="1002";//药店
	public static String PT_COURIER ="1003";//药店
	
//---------------------------------------------------------手机端业务逻辑-----------------------------------------------------------------------  
			// 查看 新配送   区分药店和派送员角色
			@RequestMapping(value = "mobileNew")
			public String mobileNew(ModelMap model){
				HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
				EngPtUser user = (EngPtUser) request.getSession().getAttribute("XIAOJD-PTUSER");
				if(user.getRole().equals("1002")) { //药店
					List<EngPtDelivery>  ptDeliveryList = engPtDeliveryService.loadByPharmacyId(user.getPharmacyId());
					for(int i=0;i<ptDeliveryList.size();i++) {
						EngPtCf ptCf = engPtCfService.loadById(ptDeliveryList.get(i).getCfId());
						ptDeliveryList.get(i).setCfStatus(ptCf.getStatus());
						ptDeliveryList.get(i).setPresDateTime(ptCf.getPresDateTime());
					}
					model.put("ptDeliveryList", ptDeliveryList);
				} else if(user.getRole().equals("1003")) { //派送员
					List<EngPtDelivery>  ptDeliveryList = engPtDeliveryService.loadByCourier(user.getId());
					for(int i=0;i<ptDeliveryList.size();i++) {
						EngPtCf ptCf = engPtCfService.loadById(ptDeliveryList.get(i).getCfId());
						ptDeliveryList.get(i).setCfStatus(ptCf.getStatus());
						ptDeliveryList.get(i).setPresDateTime(ptCf.getPresDateTime());
						model.put("ptDeliveryList", ptDeliveryList);
					}
				}
				return "yaoyiPt/mobile/new";
			}
			
			// 查看 全部配送 区分药店和派送员角色
			@RequestMapping(value = "mobileAll")
			public String mobileAll(ModelMap model){
				HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
				EngPtUser user = (EngPtUser) request.getSession().getAttribute("XIAOJD-PTUSER");
				if(user.getRole().equals("1002")) { //药店
					List<EngPtDelivery>  ptDeliveryList = engPtDeliveryService.loadByPharmacyId(user.getPharmacyId());
					for(int i=0;i<ptDeliveryList.size();i++) {
						EngPtCf ptCf = engPtCfService.loadById(ptDeliveryList.get(i).getCfId());
						ptDeliveryList.get(i).setCfStatus(ptCf.getStatus());
						ptDeliveryList.get(i).setPresDateTime(ptCf.getPresDateTime());
					}
					model.put("ptDeliveryList", ptDeliveryList);
				} else if(user.getRole().equals("1003")) { //派送员
					List<EngPtDelivery>  ptDeliveryList = engPtDeliveryService.loadByCourier(user.getId());
					for(int i=0;i<ptDeliveryList.size();i++) {
						EngPtCf ptCf = engPtCfService.loadById(ptDeliveryList.get(i).getCfId());
						ptDeliveryList.get(i).setCfStatus(ptCf.getStatus());
						ptDeliveryList.get(i).setPresDateTime(ptCf.getPresDateTime());
						model.put("ptDeliveryList", ptDeliveryList);
					}
				}
				return "yaoyiPt/mobile/all";
			}
			
			// 查看 个人中心 区分药店和派送员角色
			@RequestMapping(value = "mobileUser")
			public String mobileUser(ModelMap model){
				return "yaoyiPt/mobile/all";
			}
            
			@RequestMapping(value = "mobilePtDelivertyToDatail")
			public String mobilePtDelivertyToDatail(ModelMap model,@RequestParam long DelivertyId ) {
				HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
				EngPtUser user = (EngPtUser) request.getSession().getAttribute("XIAOJD-PTUSER");
				EngPtDelivery ptDelivery = engPtDeliveryService.loadById(DelivertyId);
				EngPtCf ptCf = engPtCfService.loadById(ptDelivery.getCfId());
				List<EngPtDrug> drugs = engPtDrugService.loadByCfid(ptCf.getId());
				if(ptDelivery.getCourierId() >0) {
					EngPtUser userTemp  =engPtUserService.findById(ptDelivery.getCourierId());
					ptDelivery.setCourierName(userTemp.getName());
				} else if(ptCf.getStatus().equals("11") && user.getRole().equals("1002")&&user.getPharmacyId()==ptDelivery.getPharmacyId()) { //新开的派送，选择派送员
					List<EngPtUser> ptUserList =  engPtUserService.getUsersByPharmacy(ptDelivery.getPharmacyId());
					model.put("ptUserList", ptUserList);
				} 
				model.put("drugs", drugs);
				model.put("ptCf", ptCf);
				model.put("ptDelivery", ptDelivery);
				return  "yaoyiPt/mobile/detail";
			}		
			
			
			/**
			 * 订单派送给送货员
			 * @param cfId
			 * @param courierId
			 */
			@RequestMapping(value = "ptDelivertyToCourier")
			public @ResponseBody 
			Map<String, String> ptDelivertyToCourier(@RequestParam String cfId,@RequestParam long courierId) {		
				   Map<String, String> ret = new HashMap<String, String>();
					EngPtCf  ptCf = engPtCfService.loadById(cfId);
					if(ptCf == null) {
						ret.put("status", "-1");
						ret.put("message", "查询不到该处方！");
					} else {
						if(PTSTATUS.CF_DELIVERY_TO_PHARMACY.toString() .equals(ptCf.getStatus())) {
							ptCf.setStatus(PTSTATUS.CF_DELIVERY_TO_COURIER .getStatus());//分配到派送员
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
			
			/**
			 * 修改处方状态
			 * @param cfId
			 * @param status
			 * @param handler
			 */
			@RequestMapping(value = "changePtCfStatus")
			public @ResponseBody 
			void changePtCfStatus(@RequestParam String cfId,@RequestParam String status) {		
					EngPtCf  ptCf = engPtCfService.loadById(cfId);
					ptCf.setStatus(status);
					engPtCfService.saveOrUpdate(ptCf);			
					EngPtDelivery engDelivery=engPtDeliveryService.loadByCfId(cfId);
					if(PTSTATUS.CF__DELIVERY_PAID.getStatus() .equals(status)){
						//engDelivery.setChargeTime(new Timestamp(System.currentTimeMillis()));
						//engDelivery.setCashier(handler);
					}		
					if(PTSTATUS.CF_DELIVERY_TO_PHARMACY .getStatus().equals(status)){
						//engDelivery.setWaitTime(new Timestamp(System.currentTimeMillis()));
					}
					if(PTSTATUS.CF_DELIVERY_TO_COURIER.getStatus().equals(status)){
						//engDelivery.setStartTime(new Timestamp(System.currentTimeMillis()));
						//engDelivery.setCourier(handler);
					}
					if(PTSTATUS.CF_DELIVERY_SUCCESS.getStatus().equals(status)){
						//engDelivery.setCompleteTime(new Timestamp(System.currentTimeMillis()));
					}
					engPtDeliveryService.saveOrUpdateDelivery(engDelivery);				
			}		
}
