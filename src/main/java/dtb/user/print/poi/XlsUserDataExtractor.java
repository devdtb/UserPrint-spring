package dtb.user.print.poi;

import org.apache.poi.hssf.usermodel.HSSFRow;
import org.springframework.stereotype.Component;

import dtb.user.print.entity.UserData;

@Component
public class XlsUserDataExtractor extends XlsUserExtractor<UserData>{
	@Override
	protected UserData extractData(HSSFRow row) throws Exception {
		UserData userData = new UserData();
		userData.setCtr(cleanCtr(getIntCellValue(row.getCell(0))));
		
		String[] names = getFirstLastName(getCellValue(row.getCell(1)));
		
		if(names.length > 0){
			userData.setLname(names[0].trim());
		}
		
		if(names.length > 1){
			userData.setFname(names[1].trim());
		}
		
		userData.setCnp(getCNPCellValue(row.getCell(2)).trim());
		userData.setAddress(getCellValue(row.getCell(3)).trim());
		userData.setIdnr(getCellValue(row.getCell(4)).trim());
		
		return userData;
	}
}
