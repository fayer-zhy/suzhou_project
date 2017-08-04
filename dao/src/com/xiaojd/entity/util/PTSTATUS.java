package com.xiaojd.entity.util;

/**
 * 药店处方状态
 * @author CZ
 *
 */
public enum PTSTATUS {
	
	CF_CANCLE("0","取消"),
	CF_NEW("1","新开"),
	//-------------------社区发药--------------
	CF_DISPENSE_SUCCESS("2","已发药"),
	CF_DISPENSE_RETURN("3","退方"),
	//------------------药店派送---------------
	CF__DELIVERY_PAID("10","已付款"),
	CF_DELIVERY_TO_PHARMACY("11","分配到药房"),
	CF_DELIVERY_TO_COURIER("12","分配派送员 "),
	CF_DELIVERY_SUCCESS("15","配送完成"),
	CF_DELIVERY_ERROR("16","配送异常");
	private  String   status;
	private  String   name;
	
	private  PTSTATUS(String status,String name) {
		this.name =name;
		this.status =status;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	   // 普通方法  
    public static String getNameByStatus(String status) {  
        for (PTSTATUS c : PTSTATUS.values()) {  
            if (c.getStatus().equals(status)) {  
                return c.name;  
            }  
        }  
        return "状态异常";  
    }  

}
