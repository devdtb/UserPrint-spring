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

import dtb.user.print.entity.UserData;

@Component
public class DocxUserDataExporter {
	private DateFormat dd_mm_yyyy = new SimpleDateFormat("dd.MM.yyyy");
	private DateFormat yyyy = new SimpleDateFormat("yyyy");
	private DateFormat mm = new SimpleDateFormat("MM");
	private DateFormat dd = new SimpleDateFormat("dd");

	public byte[] getDocx(UserData userData, Date issueDate) throws Exception {
		byte[] docxBytes = new byte[0];

		Map<String, Object> replacementMap = getReplacementMap(userData, issueDate);

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

	private Map<String, Object> getReplacementMap(UserData userData, Date issueDate) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(issueDate);

		Calendar from = (Calendar) cal.clone();
		from.add(Calendar.DAY_OF_MONTH, -14);
		Calendar to = (Calendar) cal.clone();
		to.add(Calendar.DAY_OF_MONTH, -1);

		Map<String, Object> replacementMap = new HashMap<String, Object>();
		replacementMap.put("${yyyy}", yyyy.format(issueDate));
		replacementMap.put("${mm}", mm.format(issueDate));
		replacementMap.put("${dd}", dd.format(issueDate));
		replacementMap.put("${dd.mm.yyyy}", dd_mm_yyyy.format(issueDate));
		replacementMap.put("${fname}", userData.getFname() != null ? userData.getFname() : "");
		replacementMap.put("${lname}", userData.getLname() != null ? userData.getLname() : "");
		replacementMap.put("${address}", userData.getAddress() != null ? userData.getAddress() : "");
		replacementMap.put("${bd.yyyy}", userData.getCnp() != null ? userData.getCnp().substring(1, 3) : "");
		replacementMap.put("${bd.mm}", userData.getCnp() != null ? userData.getCnp().substring(3, 5) : "");
		replacementMap.put("${bd.dd}", userData.getCnp() != null ? userData.getCnp().substring(5, 7) : "");
		replacementMap.put("${period}", dd_mm_yyyy.format(from.getTime()) + " - " + dd_mm_yyyy.format(to.getTime()));
		replacementMap.put("${1}", userData.getCnp() != null ? Character.toString(userData.getCnp().charAt(0)) : "");
		replacementMap.put("${2}", userData.getCnp() != null ? Character.toString(userData.getCnp().charAt(1)) : "");
		replacementMap.put("${3}", userData.getCnp() != null ? Character.toString(userData.getCnp().charAt(2)) : "");
		replacementMap.put("${4}", userData.getCnp() != null ? Character.toString(userData.getCnp().charAt(3)) : "");
		replacementMap.put("${5}", userData.getCnp() != null ? Character.toString(userData.getCnp().charAt(4)) : "");
		replacementMap.put("${6}", userData.getCnp() != null ? Character.toString(userData.getCnp().charAt(5)) : "");
		replacementMap.put("${7}", userData.getCnp() != null ? Character.toString(userData.getCnp().charAt(6)) : "");
		replacementMap.put("${8}", userData.getCnp() != null ? Character.toString(userData.getCnp().charAt(7)) : "");
		replacementMap.put("${9}", userData.getCnp() != null ? Character.toString(userData.getCnp().charAt(8)) : "");
		replacementMap.put("${10}", userData.getCnp() != null ? Character.toString(userData.getCnp().charAt(9)) : "");
		replacementMap.put("${11}", userData.getCnp() != null ? Character.toString(userData.getCnp().charAt(10)) : "");
		replacementMap.put("${12}", userData.getCnp() != null ? Character.toString(userData.getCnp().charAt(11)) : "");
		replacementMap.put("${13}", userData.getCnp() != null ? Character.toString(userData.getCnp().charAt(12)) : "");

		return replacementMap;
	}

	private String setValues(String text, Map<String, String> replacementMap) {
		for (Map.Entry<String, String> entry : replacementMap.entrySet()) {
			if (text.contains(entry.getKey())) {
				text.replaceAll(entry.getKey(), entry.getValue());
			}
		}

		return text;
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
