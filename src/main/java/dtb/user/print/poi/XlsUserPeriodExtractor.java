package dtb.user.print.poi;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import dtb.user.print.entity.UserPeriod;

@Component
public class XlsUserPeriodExtractor extends XlsUserExtractor<UserPeriod>{
	@Override
	protected List<UserPeriod> extractRows(HSSFSheet sheet) throws Exception {
		List<UserPeriod> userList = new ArrayList<UserPeriod>();
		
		for(int i = 1; i < 65536; i++){
			HSSFRow row = sheet.getRow(i);
			
			if(row == null){
				break;
			}
			
			UserPeriod userPeriod = extractData(row);
			
			HSSFRow nextRow = sheet.getRow(i + 1);
			String cnp = extractCNPFromNextRow(nextRow);
			
			if(cnp != null){
				userPeriod.setCnp(cnp);
				i++;
			} else if(userPeriod.getFname() != null){
				String[] cnpArr = userPeriod.getFname().split(" CNP ");
				if(cnpArr.length > 1){
					userPeriod.setCnp(cnpArr[1].trim());
					userPeriod.setFname(userPeriod.getFname().replace(" CNP " + cnpArr[1], "").trim());
				}
			}
			
			if(StringUtils.hasLength(userPeriod.getCtr()) ||
					StringUtils.hasLength(userPeriod.getLname()) ||
					StringUtils.hasLength(userPeriod.getFname()) ||
					StringUtils.hasLength(userPeriod.getCnp())){
				userList.add(userPeriod);
			}
			
		}
		
		return userList;
	}
	
	@Override
	protected UserPeriod extractData(HSSFRow row) throws Exception {
		UserPeriod userPeriod = new UserPeriod();
		userPeriod.setCtr(cleanCtr(getIntCellValue(row.getCell(0))));
		
		String[] names = getFirstLastName(getCellValue(row.getCell(1)));
		
		if(names.length > 0){
			userPeriod.setLname(names[0].trim());
		}
		
		if(names.length > 1){
			userPeriod.setFname(names[1].trim());
		}

		userPeriod.setStartDate(extractDate(row.getCell(3)));
		
		return userPeriod;
	}
	
	protected String extractCNPFromNextRow(HSSFRow nextRow){
		String cnp = null;
		if(nextRow == null || nextRow.getCell(1) == null){
			return cnp;
		}
		
		String rawCnp = null;
		try {
			rawCnp = getCellValue(nextRow.getCell(1));
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		if(rawCnp == null || !rawCnp.startsWith("CNP")){
			return cnp;
		}
		
		cnp = rawCnp.replaceAll("CNP", "").replace(" ", "").trim();
		
		return cnp;
	}
	
	protected Date extractDate(HSSFCell cell){
		Date date = null;
		if(cell == null){
			return date;
		}
		
		try {
			String cellValueStr = getCellValue(cell);
			if(cellValueStr == null || cellValueStr.length() < 5){
				return date;
			}
			
			String dd = cellValueStr.substring(0, 2);
			String mm = cellValueStr.substring(3, 5);

			date = Date.valueOf("2020-" + mm + "-" + dd);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return date;
	}
	

}
