package com.xiaojd.service.hospital;
import java.util.List;
import java.util.Map;
import com.xiaojd.entity.hospital.EngPtCf;
public interface EngPtCfService {

	public  EngPtCf loadById(String id);
	
	
	public void saveOrUpdate(EngPtCf cf);

	public void flush();

	public void clear();


	public List<EngPtCf> loadByCardAndName(String cardNo,String name,String idNo,String phoneNo);



	public List<EngPtCf> loadByPresNos(String presNos);


	public List<EngPtCf> loadByStatus(String cardNo, String name, String idNo,
			String phoneNo, String status);

	

	
	
}