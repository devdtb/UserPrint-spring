package dtb.user.print.util;

public class LocalStringUtil {
	public static String replaceDiacrit(String value){
		value = value.replaceAll("ă", "a");
		value = value.replaceAll("â", "a");
		value = value.replaceAll("î", "i");
		value = value.replaceAll("ș", "s");
		value = value.replaceAll("ț", "t");
		value = value.replaceAll("Ă", "A");
		value = value.replaceAll("Â", "A");
		value = value.replaceAll("Î", "I");
		value = value.replaceAll("Ș", "S");
		value = value.replaceAll("Ț", "T");
		
		return value;
	}
}
