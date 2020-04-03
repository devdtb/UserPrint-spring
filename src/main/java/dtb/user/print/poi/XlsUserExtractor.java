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

public abstract class XlsUserExtractor<T> {
	
	public static final String NAME_SEPARATOR = "@";
	
	protected abstract T extractData(HSSFRow row) throws Exception;
		
	@SuppressWarnings("unused")
	public List<T> extractUsers(byte[] bytes) throws Exception {
		List<T> userList = new ArrayList<T>();
		
		InputStream stream = null;

		HSSFWorkbook workbook = null;
		
		Exception ex;
		try{
			stream = new ByteArrayInputStream(bytes);
			workbook = new HSSFWorkbook(stream);
			HSSFSheet sheet = workbook.getSheetAt(0);
			userList = extractRows(sheet);
			return userList;
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
		
		return userList;
	}
	
	protected List<T> extractRows(HSSFSheet sheet) throws Exception {
		List<T> userList = new ArrayList<T>();
		
		for(int i = 1; i < 65536; i++){
			HSSFRow row = sheet.getRow(i);
			
			if(row == null){
				break;
			}
			
			T t = extractData(row);
			userList.add(t);
		}
		
		return userList;
	}
	
	protected String[] getFirstLastName(String name) throws Exception {
		String[] names = new String[]{"", ""};
		
		if(name == null){
			return names;
		}
		
		if(name.contains("@")){
			names = name.split(NAME_SEPARATOR);
		} else {
			names = name.split(" ", 2);
		}

		return names;
	}
	
	protected String getCellValue(HSSFCell cell) throws Exception{
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
		
		return cleanString(value);
	}
	
	protected String getIntCellValue(HSSFCell cell) throws Exception{
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
		
		return cleanString(value);
	}
	
	protected String getCNPCellValue(HSSFCell cell) throws Exception{
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

		return cleanString(value);
	}
	
	protected String cleanString(String value){
		return value.replaceAll("\\s", " ").replaceAll("  ", " ").trim();
	}
	
	protected String cleanCtr(String value){
		return value.replaceAll("[^0-9]", "").trim();
	}
}
