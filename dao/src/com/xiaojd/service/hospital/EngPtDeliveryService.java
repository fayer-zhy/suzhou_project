package com.xiaojd.service.hospital;

import java.util.List;

import com.xiaojd.entity.hospital.EngPtDelivery;


public interface EngPtDeliveryService {
	
	public  EngPtDelivery loadByCfId(String id);
	
	public  EngPtDelivery loadById(long id);
	
	public void saveOrUpdateDelivery(EngPtDelivery deli);
	
	public long saveDelivery(EngPtDelivery delivery);
	
	public void flush();

	public void clear();
	
	public List<EngPtDelivery> loadByCourier(long courier_id);
	
	public List<EngPtDelivery> loadByPharmacyId(long pharmacyId);
}