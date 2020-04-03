package dtb.user.print.poi;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import dtb.user.print.entity.UserData;
import dtb.user.print.entity.UserPeriod;

@Component
public class DocxUserDataExporter {
	private DateFormat dd_mm_yyyy = new SimpleDateFormat("dd.MM.yyyy");
	private DateFormat yyyy = new SimpleDateFormat("yyyy");
	private DateFormat mm = new SimpleDateFormat("MM");
	private DateFormat dd = new SimpleDateFormat("dd");

	public byte[] getDocx(UserData userData, UserPeriod userPeriod, Date issueDate) throws Exception {
		byte[] docxBytes = new byte[0];

		Map<String, Object> replacementMap = getReplacementMap(userData, userPeriod, issueDate);

		ClassLoader classLoader = DocxUserDataExporter.class.getClassLoader();
		InputStream is = classLoader.getResourceAsStream("aviz_epidemiologic_matrix.docx");

		XWPFDocument doc = new XWPFDocument(OPCPackage.open(is));

		for (XWPFParagraph p : doc.getParagraphs()) {
			replaceInParagraph(p, replacementMap);
		}
		for (XWPFTable tbl : doc.getTables()) {
			for (XWPFTableRow row : tbl.getRows()) {
				for (XWPFTableCell cell : row.getTableCells()) {
					for (XWPFParagraph p : cell.getParagraphs()) {
						replaceInParagraph(p, replacementMap);
					}
				}
			}
		}

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		doc.write(baos);
		docxBytes = baos.toByteArray();

		if (doc != null) {
			doc.close();
		}

		if (baos != null) {
			baos.close();
		}

		return docxBytes;
	}

	private Map<String, Object> getReplacementMap(UserData userData, UserPeriod userPeriod, Date issueDate) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(issueDate);

		Calendar from = (Calendar) cal.clone();
		Calendar to = (Calendar) cal.clone();

		if(userPeriod != null && userPeriod.getStartDate() != null){
			from.setTime(userPeriod.getStartDate());
			to.setTime(userPeriod.getStartDate());
			to.add(Calendar.DAY_OF_MONTH, 13);
		} else {
			from.add(Calendar.DAY_OF_MONTH, -14);
			to.add(Calendar.DAY_OF_MONTH, -1);
		}

		Map<String, Object> replacementMap = new HashMap<String, Object>();
		replacementMap.put("${yyyy}", yyyy.format(issueDate));
		replacementMap.put("${mm}", mm.format(issueDate));
		replacementMap.put("${dd}", dd.format(issueDate));
		replacementMap.put("${dd.mm.yyyy}", dd_mm_yyyy.format(issueDate));
		
		String lname = userData != null && StringUtils.hasLength(userData.getLname())
				? userData.getLname()
				: userPeriod != null && StringUtils.hasLength(userPeriod.getLname())
						? userPeriod.getLname() : "";
						
		String fname = userData != null && StringUtils.hasLength(userData.getFname())
				? userData.getFname()
				: userPeriod != null && StringUtils.hasLength(userPeriod.getFname())
						? userPeriod.getFname() : "";
						fname = fname.trim();
						
		String cnp = userData != null && StringUtils.hasLength(userData.getCnp()) 
				? userData.getCnp()
				: userPeriod != null && StringUtils.hasLength(userPeriod.getCnp()) 
						? userPeriod.getCnp() : "";		
						cnp = cnp.trim();
		
		//1 and 2 -> 1900; 5 + 6 -> 2000						
		String century = "";
		if(cnp.startsWith("1") || cnp.startsWith("2")){
			century = "19";
		} else if(cnp.startsWith("5") || cnp.startsWith("6")){
			century = "20";
		}
					
						
		String address = userData != null && userData.getAddress() != null ? userData.getAddress() : "X";					
						
		replacementMap.put("${fname}", fname);
		replacementMap.put("${lname}", lname);
		replacementMap.put("${address}", address);
		replacementMap.put("${bd.yyyy}", century + extractFromCNP(cnp, 1, 3));
		replacementMap.put("${bd.mm}", extractFromCNP(cnp, 3, 5));
		replacementMap.put("${bd.dd}", extractFromCNP(cnp, 5, 7));
		replacementMap.put("${period}", dd_mm_yyyy.format(from.getTime()) + " - " + dd_mm_yyyy.format(to.getTime()));
		
		for(int i = 1; i <= 13; i++){
			replacementMap.put("${" + i + "}", extractFromCNP(cnp, i-1, i));
		}

		return replacementMap;
	}
	
	private String extractFromCNP(String cnp, int beginIndex, int endIndex){
		String extract = "X";
		
		if(!StringUtils.hasLength(cnp)){
			return extract;
		}
		
		if(beginIndex > cnp.length() -1 || endIndex > cnp.length()){
			return extract;
		}
		
		return cnp.substring(beginIndex, endIndex);
	}

	/**
	 * Replaces placeholders to replacement values in a given paragraph.
	 * Expected placeholder format is: ${MY_PLACEHOLDER_NAME}
	 *
	 * @param paragraph
	 *            Paragraph with placeholders.
	 * @param placeholdersToReplacements
	 *            Collection of placeholder name keys and replacement values.
	 */
	public static void replaceInParagraph(XWPFParagraph paragraph, Map<String, Object> placeholdersToReplacements) {

		final String PLACEHOLDER_PREFIX = "${";
		final String PLACEHOLDER_POSTFIX = "}";

		String text = paragraph.getText();

		if (!text.contains(PLACEHOLDER_PREFIX)) {
			return;
		}

		boolean isReplacementStarted = false;
		boolean isClosing;
		StringBuilder placeholderName = new StringBuilder();
		StringBuilder prePlaceholderText = new StringBuilder();
		StringBuilder postPlaceholderText = new StringBuilder();

		for (XWPFRun run : paragraph.getRuns()) {

			if (run.getText(0) == null) {
				continue;
			}

			isClosing = run.getText(0).contains(PLACEHOLDER_POSTFIX);

			if (!isReplacementStarted && run.getText(0).contains(PLACEHOLDER_PREFIX)) {

				// Start replacements:

				String runText = run.getText(0);
				int prefixIndex = runText.indexOf(PLACEHOLDER_PREFIX);
				int postfixIndex = runText.indexOf(PLACEHOLDER_POSTFIX);

				String placeholderText = runText.substring(prefixIndex >= 0 ? prefixIndex : 0,
						postfixIndex >= 0 ? postfixIndex + 1 : runText.length());

				placeholderName.append(placeholderText.replace(PLACEHOLDER_PREFIX, ""));

				prePlaceholderText.append(runText, 0, prefixIndex >= 0 ? prefixIndex : 0);

				postPlaceholderText.append(runText.substring(postfixIndex > 0 ? postfixIndex + 1 : runText.length()));

				run.setText("", 0);
				isReplacementStarted = true;
			}

			if (isReplacementStarted) {

				// Accumulate placholder parts:

				String runText = run.getText(0);
				int prefixIndex = runText.indexOf(PLACEHOLDER_PREFIX);
				int postfixIndex = runText.indexOf(PLACEHOLDER_POSTFIX);

				String placeholderText = runText.substring(prefixIndex >= 0 ? prefixIndex : 0,
						postfixIndex >= 0 ? postfixIndex + 1 : runText.length());

				if (isReplacementStarted && !isClosing) {
					placeholderText = runText;
				}

				placeholderName.append(placeholderText.replace(PLACEHOLDER_POSTFIX, ""));

				prePlaceholderText.append(runText.substring(0, prefixIndex >= 0 ? prefixIndex : 0));

				postPlaceholderText.append(runText.substring(postfixIndex > 0 ? postfixIndex + 1 : runText.length()));

				run.setText("", 0);
			}

			if (isReplacementStarted && isClosing) {

				// Finilize replacements:

				String runText = run.getText(0);
				int prefixIndex = runText.indexOf(PLACEHOLDER_PREFIX);
				int postfixIndex = runText.indexOf(PLACEHOLDER_POSTFIX);
				String placeholderText = runText.substring(prefixIndex >= 0 ? prefixIndex : 0,
						postfixIndex >= 0 ? postfixIndex + 1 : runText.length());
				placeholderName.append(placeholderText);

				prePlaceholderText.append(runText.substring(0, prefixIndex >= 0 ? prefixIndex : 0));

				postPlaceholderText.append(runText.substring(postfixIndex > 0 ? postfixIndex + 1 : runText.length()));

				placeholderName.append(run.getText(0).replace(PLACEHOLDER_POSTFIX, ""));
				boolean endsWithPostfix = placeholderName.lastIndexOf(PLACEHOLDER_POSTFIX) == placeholderName.length()
						- PLACEHOLDER_POSTFIX.length();

				if (endsWithPostfix) {

					placeholderName.replace(placeholderName.lastIndexOf(PLACEHOLDER_POSTFIX), placeholderName.length(),
							"");
				}

				String placeholderValue = "";

				for (String placeholder : placeholdersToReplacements.keySet()) {

					// Choose placeholder:

					String placeholderCore = placeholder.replace(PLACEHOLDER_PREFIX, "").replace(PLACEHOLDER_POSTFIX,
							"");

					placeholderValue = placeholderCore.equals(placeholderName.toString())
							? (String) placeholdersToReplacements.get(placeholder) : "";

					if (!placeholderValue.isEmpty()) {

						break;
					}
				}

				run.setText(prePlaceholderText + placeholderValue + postPlaceholderText, 0);

				isReplacementStarted = false;

				placeholderName = new StringBuilder();
				prePlaceholderText = new StringBuilder();
				postPlaceholderText = new StringBuilder();
			}
		}
	}
}
