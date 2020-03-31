package dtb.user.print.poi;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.CellType;
import org.springframework.stereotype.Component;

import dtb.user.print.entity.UserData;

@Component
public class XlsUserDataExtractor {
	
	public static final String NAME_SEPARATOR = "@";
	
	@SuppressWarnings("unused")
	public List<UserData> extractUsers(byte[] bytes) throws Exception {
		List<UserData> userDataList = new ArrayList<UserData>();
		
		InputStream stream = null;

		HSSFWorkbook workbook = null;
		
		Exception ex;
		try{
			stream = new ByteArrayInputStream(bytes);
			workbook = new HSSFWorkbook(stream);
			
			HSSFSheet sheet = workbook.getSheetAt(0);

			for(int i = 1; i < 65536; i++){
				HSSFRow row = sheet.getRow(i);
				
				if(row == null){
					break;
				}
				
				UserData userData = extractUser(row);
				
//				if(userData.getLname() == null || userData.getLname().length() == 0){
//					break;
//				}
				
				userDataList.add(userData);
			}
			
			return userDataList;
		} catch (Exception e) {
			ex = e;
		} finally {
			if(stream != null){
				stream.close();
			}
			
			if(workbook != null){
				workbook.close();
			}
		}
		
		if(ex != null){
			throw ex;
		}
		
		return userDataList;
	}
	
	private UserData extractUser(HSSFRow row) throws Exception{
		UserData userData = new UserData();
		userData.setCtr(getIntCellValue(row.getCell(0)));
		
		String name = getCellValue(row.getCell(1));
		String[] names = name.split(NAME_SEPARATOR);
		userData.setLname(names[0]);
		if(names.length > 1){
			userData.setFname(names[1]);
		}

		userData.setCnp(getCNPCellValue(row.getCell(2)));
		userData.setAddress(getCellValue(row.getCell(3)));
		userData.setIdnr(getCellValue(row.getCell(4)));
		
		return userData;
	}
	
	private String getCellValue(HSSFCell cell) throws Exception{
		String value = "";
		
		if(cell == null){
			return value;
		}
		
		CellType type = cell.getCellType();
		if(type == CellType.NUMERIC){
			value = String.valueOf(cell.getNumericCellValue());
		} else if(type == CellType.STRING) {
			value = cell.getStringCellValue();
		} else if(type == CellType.BLANK || type == CellType.ERROR) {
			//do nothing
		} else {
			throw new Exception("Cell Type not implemented " + type);
		}
		
		return value;
	}
	
	private String getIntCellValue(HSSFCell cell) throws Exception{
		String value = "";
		
		if(cell == null){
			return value;
		}
		
		CellType type = cell.getCellType();
		if(type == CellType.NUMERIC){
			value = String.valueOf((int) cell.getNumericCellValue());
		} else if(type == CellType.STRING) {
			value = cell.getStringCellValue();
		} else if(type == CellType.BLANK || type == CellType.ERROR) {
			//do nothing
		} else {
			throw new Exception("Cell Type not implemented " + type);
		}
		
		return value;
	}
	
	private String getCNPCellValue(HSSFCell cell) throws Exception{
		String value = "";
		
		if(cell == null){
			return value;
		}
		
		CellType type = cell.getCellType();
		if(type == CellType.NUMERIC){
			 NumberFormat numberFormatter = new DecimalFormat("#############");
			 value = numberFormatter.format(cell.getNumericCellValue());
		} else if(type == CellType.STRING) {
			value = cell.getStringCellValue();
		} else if(type == CellType.BLANK || type == CellType.ERROR) {
			//do nothing
		} else {
			System.out.println(type.toString());
			throw new Exception("Cell Type not implemented " + type);
		}
		
		return value;
	}
}
