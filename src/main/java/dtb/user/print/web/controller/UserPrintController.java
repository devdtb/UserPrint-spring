package dtb.user.print.web.controller;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import dtb.user.print.UserPrintSpringApplication;
import dtb.user.print.entity.UserData;
import dtb.user.print.poi.DocxUserDataExporter;
import dtb.user.print.poi.XlsUserDataExtractor;
import dtb.user.print.web.dao.UserDataRepository;

@Controller
public class UserPrintController {

	public static final String ISSUE_DATE = "issueDate";
	public static final String ISSUE_DATE_STR = "issueDateStr";
	
	public static final DateFormat DATE_FORMAT = new SimpleDateFormat("dd.MM.yyyy");
	
	@Autowired
	private XlsUserDataExtractor xlsUserDataExtractor;
	@Autowired
	private DocxUserDataExporter docxUserDataExporter;

	@Autowired
	private UserDataRepository userDataRepository;

	@RequestMapping("start")
	public ModelAndView displayMainTable(HttpSession httpSession) {
		ModelAndView mv = new ModelAndView("view");

		Iterable<UserData> userDataItr = userDataRepository.findAll();
	    List<UserData> userDataList = new ArrayList<UserData>();
	    userDataItr.forEach(userDataList::add);
		
		mv.addObject("userDataList", userDataList);
		
		Date issueDate = (Date) httpSession.getAttribute(ISSUE_DATE);
		if(issueDate == null){
			issueDate = new Date();
		}

		httpSession.setAttribute(ISSUE_DATE, issueDate);
		mv.addObject(ISSUE_DATE_STR, DATE_FORMAT.format(issueDate));
		mv.addObject("nameSeparator", XlsUserDataExtractor.NAME_SEPARATOR);
		
		return mv;
	}

	@RequestMapping("stop")
	public ModelAndView stoppApp() {
		ModelAndView mv = new ModelAndView("view");

		UserPrintSpringApplication.stopApplication();

		mv.addObject("msg", "Closing app....");

		return mv;
	}
	
	@RequestMapping("changeIssueDate")
	public ModelAndView changeDate(@RequestParam(ISSUE_DATE) String issueDateStr, HttpSession httpSession) {
		ModelAndView mv = new ModelAndView("redirect:/start");

		Date issueDate = null;
		try{
			issueDate = DATE_FORMAT.parse(issueDateStr);
		} catch (Exception e) {
			
		}
		
		if(issueDate == null){
			issueDate = new Date();
		}

		httpSession.setAttribute(ISSUE_DATE, issueDate);
		
		return mv;
	}

	@RequestMapping("upload")
	public ModelAndView uploadFile(@RequestParam("file") MultipartFile file, RedirectAttributes redirectAttributes) {
		ModelAndView mv = new ModelAndView("redirect:/start");

		if (file.isEmpty()) {
			System.out.println("File is empty.....");
		}

		try {

			// Get the file and save it somewhere
			byte[] bytes = file.getBytes();
			List<UserData> userDataList = xlsUserDataExtractor.extractUsers(bytes);

			userDataRepository.saveAll(userDataList);
		} catch (Exception e) {
			e.printStackTrace();
			mv.addObject("error", e.toString());
		}

		return mv;
	}

	@RequestMapping("reset")
	public ModelAndView uploadFile() {
		ModelAndView mv = new ModelAndView("redirect:/start");

		userDataRepository.deleteAll();
		
		return mv;
	}
	
	@RequestMapping(value = "download", method = RequestMethod.GET)
	public ResponseEntity<ByteArrayResource> print(@RequestParam("id") long id, HttpSession httpSession) throws Exception {
		Optional<UserData> userDataOpt = userDataRepository.findById(id);
		
		if(!userDataOpt.isPresent()){
			throw new Exception("No user could be found with id " + id);
		}
		
		
		Date issueDate = (Date) httpSession.getAttribute(ISSUE_DATE);
		if(issueDate == null){
			issueDate = new Date();
		}
		
		UserData userData = userDataOpt.get();
		
		String fileName = userData.getLname().trim();
		fileName += userData.getFname() != null ? "_" + userData.getFname().trim() : "";
		fileName += userData.getCnp() != null ? "_" + userData.getCnp().trim() : "";
		fileName += ".docx";
		fileName = fileName.replaceAll(" ", "_");
		fileName = fileName.replaceAll("ă", "a");
		fileName = fileName.replaceAll("â", "a");
		fileName = fileName.replaceAll("î", "i");
		fileName = fileName.replaceAll("ș", "s");
		fileName = fileName.replaceAll("ț", "t");
		fileName = fileName.replaceAll("Ă", "A");
		fileName = fileName.replaceAll("Â", "A");
		fileName = fileName.replaceAll("Î", "I");
		fileName = fileName.replaceAll("Ș", "S");
		fileName = fileName.replaceAll("Ț", "T");
		
        byte[] data = docxUserDataExporter.getDocx(userData, issueDate);
        ByteArrayResource resource = new ByteArrayResource(data);
 
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=" + fileName)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .contentLength(data.length)
                .body(resource);
	}

	@RequestMapping(value = "printMultiple", method = RequestMethod.POST)
	public ModelAndView printMultiple(@RequestParam("ids") List<String> ids, @RequestParam("issueDate") String issueDate) {

		System.out.println(ids);
		System.out.println(issueDate);

		ModelAndView mv = new ModelAndView("view");

		return mv;
	}

}
