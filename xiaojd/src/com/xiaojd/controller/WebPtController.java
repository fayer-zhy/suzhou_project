package com.xiaojd.controller;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import com.xiaojd.entity.hospital.EngPtDelivery;
import com.xiaojd.entity.hospital.EngPtDeliveryAddress;
import com.xiaojd.entity.hospital.EngPtCf;
import com.xiaojd.entity.hospital.EngPtDrug;
import com.xiaojd.entity.hospital.EngPtPharmacy;
import com.xiaojd.entity.hospital.EngPtUser;
import com.xiaojd.entity.util.PTSTATUS;
import com.xiaojd.service.hospital.EngPtCfService;
import com.xiaojd.service.hospital.EngPtDeliveryAddressService;
import com.xiaojd.service.hospital.EngPtDeliveryService;
import com.xiaojd.service.hospital.EngPtDispenseService;
import com.xiaojd.service.hospital.EngPtDrugService;
import com.xiaojd.service.hospital.EngPtMessageService;
import com.xiaojd.service.hospital.EngPtPharmacyService;
import com.xiaojd.service.hospital.EngPtUserService;

/**
 * @author CZ
 *
 */

@Controller
public class WebPtController  {

	Logger loger = LoggerFactory.getLogger(WebPtController.class);
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
	public static String PT_COURIER_P ="1002";//药房
	public static String PT_COURIER ="1003";//平台配送员 
	
//-------------------------------------------业务逻辑-----------------------------------------
			/**
			 * 根据病人基本信息查询处方
			 * @param 查询字段
			 * @param 查询的值
			 * @return
			 */
			@RequestMapping(value = "loadCfByPatientInfoType")
			@ResponseBody
			public  Map<String, Object> loadCfByPatientInfo( String type, String value) {
				String  idCard ="";
				String cardNo ="";
				String phoneNo ="";
				String name   = "";
				Map<String, Object> ret = new HashMap<String, Object>();
	/*			if(type == null || "".equals(type)) {
					   ret.put("success", "false");
					   return ret;	
				} else {*/
					type = type.replaceAll(" ", "");
				//}
/*				if(value == null || "".equals(value))  {
					   ret.put("success", "false");
				       return ret;	
				} else {*/
					value = value.replaceAll(" ", "");
				//}
				if("idCard".equals(type)) {
					idCard = value;
				} else if ("cardNo".equals(type)) {
					cardNo = value;
				} else if ("phoneNo".equals(type)) {
					phoneNo =value;
				} else if("name".equals(type)) {
					name = value;
				} else {
					type="";
				}
				
		/*		if("".equals(type)||"".equals(value)) {
					   ret.put("success", "false");
				       return ret;	
				}*/
				

				List<EngPtCf> ptCfList = new ArrayList<EngPtCf>();
			    ptCfList = engPtCfService.loadByCardAndName(cardNo, name, idCard,phoneNo);
				if(ptCfList!=null&&ptCfList.size()>0){
					ret.put("data", ptCfList);
					ret.put("success", "true");
					return ret;			
					}	
				   ret.put("success", "false");
				   return ret;	
				}
					
			
			/**查询当前所有用户
			 * @param patientNo
			 * @param id
			 * @return
			 */
			@RequestMapping(value = "loadAllUser")
			public @ResponseBody List<EngPtUser> loadAllUser() {	
					List<EngPtUser> listUser = engPtUserService.getUsers();
					if (listUser!=null && listUser.size()>0) {
						return listUser;
					}			
				   return null;
			}
			//备注
			
			/**修改用户，包括逻辑删除
			 * @param patientNo
			 * @param id
			 * @return
			 */
			@RequestMapping(value = "deleteOrUpdateUser")
			public @ResponseBody Map<String, Object> deleteOrUpdateUser(Long id,String name,String code,String roleName,String pwd,String operation) {
				Map<String, Object> ret = new HashMap<String, Object>();
				   if("DELETE".equals(operation) && id !=null && !"".equals(id)) {
					   EngPtUser user   = engPtUserService.findOrgRoleById(id);
					   if(user == null)  {
						   ret.put("success", "false");
						   ret.put("message","用户不存在");
					   } else {
						   user.setStatus("0");//状态置为无效 0
						   engPtUserService.saveOrUpdataUser(user);
						   ret.put("success", "true");
						   ret.put("message","删除成功");
					   }
				   }
				   if("CREATE".equals(operation)||"UPDATE".equals(operation)) {
					   EngPtUser user = new EngPtUser();
					   if("UPDATE".equals(operation)) {
						   user = engPtUserService.findOrgRoleById(id);
					   }else if("CREATE".equals(operation)) {
						   user.setStatus("1");//状态置为使用
					   }
					   user.setName(name);
					   user.setCode(code);
					   user.setPwd(pwd);
	                   user.setRoleName(roleName);
	            	   engPtUserService.saveOrUpdataUser(user);
	            	   if("UPDATE".equals(operation)) {
	            		   ret.put("success", "true");
						   ret.put("message","修改成功");
	            	   } 
	            	   if("CREATE".equals(operation)) {
	            		   ret.put("success", "true");
						   ret.put("message","添加成功");
	            	   } 
				   }
				   return ret;
			}
			
			/**根据病人处方号查找病人处方,和药品信息
			 * @param id
			 * @return
			 */
			@RequestMapping(value = "loadPtCfById")
			public String loadPtCfById(ModelMap resultMap,@RequestParam String id) {
				Map<String, Object> ret = null;
				if(id != null && id.trim().length() > 0){			
					ret = new HashMap<String, Object>();
					EngPtCf cf = engPtCfService.loadById(id);
					List<EngPtDrug> drugs = engPtDrugService.loadByCfid(id);
					List<String> areaList = engPtPharmacyService.getPharmacysAreas();
					resultMap.put("cf", cf);
					resultMap.put("drugs", drugs);
				    resultMap.put("areaList", areaList);
				    if(Integer.parseInt(cf.getStatus())>10) {
					    EngPtDelivery ptDelivery =engPtDeliveryService.loadByCfId(id);
					    EngPtPharmacy  ptPharmacy= engPtPharmacyService.loadById(ptDelivery.getPharmacyId());
					    resultMap.put("ptDelivery", ptDelivery);
					    resultMap.put("ptPharmacy", ptPharmacy);
				    }

				}
				return "yaoyiPt/web/refer";
			}

            
			
	
			/**查询病人基本信息，包括地址，手机号
			 * @param patientNo
			 * @param id
			 * @return
			 */
			@RequestMapping(value = "loadPtDeliveryAddressByPatientNo")
			public @ResponseBody 
			List<EngPtDeliveryAddress> loadPtDeliveryAddressByPatientNo(@RequestParam String patientNo) {	
					List<EngPtDeliveryAddress> address = engPtDeliveryAddressService.loadByPatientNo(patientNo);
					if (address!=null && address.size()>0) {
						return address;
					}			
				   return null;
			}
			
			/**保存病人基本信息，包括地址，手机号
			 * @param patientNo
			 * @param id
			 * @return
			 */
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
			

			
			/**
			 * 删除病人地址信息
			 * @param id
			 * @param name
			 * @param address
			 * @param phoneNo
			 */
			@RequestMapping(value = "deleteDeliveryAddress")
			public @ResponseBody 
			Map<String, Object> deleteDeliveryAddress(@RequestParam Long id) {
					engPtDeliveryAddressService.deleteById(id);	
			        Map<String, Object> ret = new HashMap<String, Object>();	
			        ret.put("success", "true");
					ret.put("message","删除成功");
					return ret;
			}
		    
			/**
			 * 修改处方状态 付款
			 * @param cfId
			 * @param status
			 * @param handler
			 */
			@RequestMapping(value = "changePtCfStatusToPaid")
			public @ResponseBody 
			Map<String, String> changePtCfStatusToPaid(@RequestParam String cfId) {		
				   Map<String, String> ret = new HashMap<String, String>();
					EngPtCf  ptCf = engPtCfService.loadById(cfId);
					if(ptCf == null) {
						ret.put("success", "false");
						ret.put("message", "查询不到该处方！");
					} else {
						if(PTSTATUS.CF_NEW.getStatus().equals(ptCf.getStatus())) {
							ptCf.setStatus(PTSTATUS.CF__DELIVERY_PAID .getStatus());//收款
							engPtCfService.saveOrUpdate(ptCf);
							ret.put("success", "true");
							ret.put("message", "付款成功！");
							return ret;
						} else {
							ret.put("success", "false");
							ret.put("message", "处方状态异常！");
						}
					}
					return ret;
			}
			
			
			/**	添加派送信息,   已付款->分配到药房
			 * @param cfId  处方ID
			 * @param pharmacyId 药房ID
			 * @param address 收件人地址
			 * @param phoneNo 收件人联系电话
			 * @param name 收件人
			 * @param remark 备注
			 */
			@RequestMapping(value = "saveCfDeliverInfo")
			public @ResponseBody 
			Map<String,String> saveCfDeliverInfo(@RequestParam String cfId,@RequestParam long pharmacyId,
					@RequestParam String address,@RequestParam String phoneNo,
					@RequestParam String name,@RequestParam String  remark) {
				    Map<String,String> map = new HashMap<String,String>();
					EngPtCf ptCf= engPtCfService.loadById(cfId);
					String status = ptCf.getStatus();
					//先查询处方状态，只有已付款的才可以添加派送
					if(status==null ||!PTSTATUS.CF__DELIVERY_PAID.getStatus().equals(status)) {
						map.put("success", "false");//该处方订单已经存在
    					map.put("message","处方状态异常");
    					return map;
					}
					
					EngPtDelivery deli = engPtDeliveryService.loadByCfId(cfId);
					if(deli != null) {
						map.put("success", "false");//该处方订单已经存在
    					map.put("message","该处方订单已经存在");
    					return map;
					} else {
						deli = new  EngPtDelivery();
						deli.setCfId(cfId);
						deli.setPharmacyId(pharmacyId);
						deli.setPhoneNo(phoneNo.trim());
						deli.setAddress(address.trim());
						deli.setName(name.trim());
						deli.setRemark(remark.trim());
						deli.setPharmacyTime(new Timestamp(System.currentTimeMillis()));
						long deliveryId = engPtDeliveryService.saveDelivery(deli);
						ptCf.setStatus(PTSTATUS.CF_DELIVERY_TO_PHARMACY.getStatus() );//分配到药房
						engPtCfService.saveOrUpdate(ptCf);	
						map.put("success", "true");
						map.put("message", "订单生成成功");
						map.put("delivery","" + deliveryId);
						return map;
					}
			}
//		--------------------------------------------------------------------------------------------------------
			/**
			 * 根据病人处方号查找派送信息
			 * @param id
			 * @param patientNo
			 * @return
			 */
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
			
			/**
			 * 通过快递员的编号查找药派送的处方
			 * @param courier
			 * @param status
			 * @return
			 */
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
		
			/**分配到药房后可以修改派送基本信息,不修改状态
			 * @param cfId  处方ID
			 * @param address 收件人地址
			 * @param phoneNo 收件人联系电话
			 * @param name 收件人
			 * @param remark 备注
			 */
			@RequestMapping(value = "saveCfDeliveryPharmacy")
			public @ResponseBody 
			Map<String,String> saveCfDeliveryPharmacy(@RequestParam String cfId,
					@RequestParam String address,@RequestParam String phoneNo,
					@RequestParam String name,@RequestParam String  remark) {
				    Map<String,String> map = new HashMap<String,String>();
					EngPtCf ptCf= engPtCfService.loadById(cfId);
					String status = ptCf.getStatus();
					//先查询处方状态，只有分配到药房才可以添加派送
					if(status==null ||!PTSTATUS.CF_DELIVERY_TO_PHARMACY.getStatus() .equals(status)) {
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
					
			/**
			 * 忘记密码修改  ptChangPassword
			 * @return
			 * @throws Exception
			 */
			@RequestMapping(value = "ptChangePassword")
			public  String ptChangPassword() throws Exception {
				return "yaoyiPt/changePassword";
			}
			
			/**
			 * 订单查询页面
			 * @return
			 * @throws Exception
			 */
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
			
			/**
			 * 订单分配到药房
			 * @return
			 * @throws Exception
			 */
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
            
			/**
			 * 订单分配到药房
			 * @return
			 * @throws Exception
			 */
			@RequestMapping(value = "ptGetPharmacyByArea")
		public @ResponseBody List<EngPtPharmacy> ptGetPharmacyByArea(ModelMap resultMap,@RequestParam String area)  throws Exception {				
     		if(area != null && area.trim().length() > 0){
					List<EngPtPharmacy> pharmacyList = engPtPharmacyService.getPharmacysByArea(area); 
					return   pharmacyList;
     		} else {
     			   return  null;
     		}
		}

			/**
			 * 订单分配,派送  11 分配到药房,可以编辑地址
			 * @return
			 * @throws Exception
			 */
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
						if(PTSTATUS.CF_DELIVERY_TO_PHARMACY.getStatus().equals(cf.getStatus())) {
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
			
			
			/**
			 * 订单进度
			 * @return
			 * @throws Exception
			 */
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
			
		
}
