package dtb.user.print.web.controller;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import dtb.user.print.UserPrintSpringApplication;
import dtb.user.print.entity.UserData;
import dtb.user.print.entity.UserPeriod;
import dtb.user.print.entity.UserUI;
import dtb.user.print.poi.DocxUserDataExporter;
import dtb.user.print.poi.XlsUserDataExtractor;
import dtb.user.print.poi.XlsUserPeriodExtractor;
import dtb.user.print.util.LocalStringUtil;
import dtb.user.print.util.UserMapper;
import dtb.user.print.web.dao.UserDataRepository;
import dtb.user.print.web.dao.UserPeriodRepository;

@Controller
public class UserPrintController {

	public static final String USER_DATA = "userData";
	public static final String USER_PERIOD = "userPeriod";
	
	public static final String CHANGE_ISSUE_DATE = "changeIssueDate";
	public static final String UPLOAD_FILE = "uploadFile";
	public static final String RESET_DATA = "resetData";
	
	public static final String ISSUE_DATE = "issueDate";
	public static final String ISSUE_DATE_STR = "issueDateStr";
	
	public static final DateFormat DATE_FORMAT = new SimpleDateFormat("dd.MM.yyyy");
	
	@Autowired
	private XlsUserDataExtractor xlsUserDataExtractor;
	@Autowired
	private XlsUserPeriodExtractor xlsUserPeriodExtractor;
	@Autowired
	private DocxUserDataExporter docxUserDataExporter;

	@Autowired
	private UserDataRepository userDataRepository;
	@Autowired
	private UserPeriodRepository userPeriodRepository;
	
	@Autowired
	private UserMapper userMapper;
	
	@RequestMapping("stop")
	public ModelAndView stoppApp() {
		ModelAndView mv = new ModelAndView("view");

		UserPrintSpringApplication.stopApplication();

		mv.addObject("msg", "Closing app....");

		return mv;
	}
	
	@RequestMapping("start")
	public ModelAndView startApp(HttpSession httpSession) {
		ModelAndView mv = new ModelAndView("redirect:/" + USER_DATA);
		return mv;
	}
	
	@RequestMapping(USER_DATA)
	public ModelAndView displayUserData(HttpSession httpSession) {
		ModelAndView mv = new ModelAndView(USER_DATA);
		mv.addObject("userKey", USER_DATA);
		
		Iterable<UserData> userDataItr = userDataRepository.findAll();
	    List<UserData> userDataList = new ArrayList<UserData>();
	    userDataItr.forEach(userDataList::add);
	    
		Iterable<UserPeriod> userPeriodItr = userPeriodRepository.findAll();
	    List<UserPeriod> userPeriodList = new ArrayList<UserPeriod>();
	    userPeriodItr.forEach(userPeriodList::add);

	    List<UserUI> userUIList = new ArrayList<UserUI>();
	    for(UserData userData : userDataList){
	    	UserPeriod userPeriod = new UserPeriod();
		    for(UserPeriod dbUserPeriod : userPeriodList){
		    	if(dbUserPeriod.getUserDataId() == userData.getUserDataId()){
		    		userPeriod = dbUserPeriod;
		    		break;
		    	}
		    }
		    
	    	UserUI userUI = new UserUI(userData, userPeriod);
	    	userUIList.add(userUI);
	    }
	    
		mv.addObject("userUIList", userUIList);
		
		setDateInRequest(mv, httpSession);
		
		return mv;
	}

	
	@RequestMapping(USER_PERIOD)
	public ModelAndView displayUserPeriod(HttpSession httpSession) {
		ModelAndView mv = new ModelAndView(USER_PERIOD);
		mv.addObject("userKey", USER_PERIOD);
		
		Iterable<UserPeriod> userPeriodItr = userPeriodRepository.findAll();
	    List<UserPeriod> userPeriodList = new ArrayList<UserPeriod>();
	    userPeriodItr.forEach(userPeriodList::add);
		
	    List<UserUI> userUIList = new ArrayList<UserUI>();
	    for(UserPeriod userPeriod : userPeriodList){
	    	UserData userData = new UserData();
	    	if(userPeriod.getUserDataId() > 0){
	    		Optional<UserData> userDataOpt = userDataRepository.findById(userPeriod.getUserDataId());
	    		userData = userDataOpt.orElse(new UserData());
	    	}
	    	
	    	UserUI userUI = new UserUI(userData, userPeriod);
	    	userUIList.add(userUI);
	    }
	    
		mv.addObject("userUIList", userUIList);
		
		setDateInRequest(mv, httpSession);
		
		return mv;
	}
	
	
	private ModelAndView setDateInRequest(ModelAndView mv, HttpSession httpSession) {
		Date issueDate = (Date) httpSession.getAttribute(ISSUE_DATE);
		if(issueDate == null){
			issueDate = new Date();
		}

		httpSession.setAttribute(ISSUE_DATE, issueDate);
		mv.addObject(ISSUE_DATE_STR, DATE_FORMAT.format(issueDate));
		mv.addObject("nameSeparator", XlsUserDataExtractor.NAME_SEPARATOR);
		return mv;
	}
	
	@RequestMapping(CHANGE_ISSUE_DATE + "/" + USER_DATA)
	public ModelAndView changeUserDataDate(@RequestParam(ISSUE_DATE) String issueDateStr, HttpSession httpSession) {
		ModelAndView mv = new ModelAndView("redirect:/" + USER_DATA);
		setIssueDate(issueDateStr, mv, httpSession);
		return mv;
	}
	
	@RequestMapping(CHANGE_ISSUE_DATE + "/" + USER_PERIOD)
	public ModelAndView changeUserPeriodDate(@RequestParam(ISSUE_DATE) String issueDateStr, HttpSession httpSession) {
		ModelAndView mv = new ModelAndView("redirect:/" + USER_PERIOD);
		setIssueDate(issueDateStr, mv, httpSession);
		return mv;
	}
	
	private ModelAndView setIssueDate(String issueDateStr, ModelAndView mv, HttpSession httpSession){
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

	@RequestMapping(UPLOAD_FILE + "/" + USER_DATA)
	public ModelAndView uploadUserDataFile(@RequestParam("file") MultipartFile file, RedirectAttributes redirectAttributes) {
		ModelAndView mv = new ModelAndView("redirect:/" + USER_DATA);

		if (file.isEmpty()) {
			System.out.println("File is empty.....");
		}

		try {
			byte[] bytes = file.getBytes();
			List<UserData> userDataList = xlsUserDataExtractor.extractUsers(bytes);

			userDataRepository.saveAll(userDataList);
		} catch (Exception e) {
			e.printStackTrace();
			mv.addObject("error", e.toString());
		}

		return mv;
	}
	
	@RequestMapping(UPLOAD_FILE + "/" + USER_PERIOD)
	public ModelAndView uploadUserPeriodFile(@RequestParam("file") MultipartFile file, RedirectAttributes redirectAttributes) {
		ModelAndView mv = new ModelAndView("redirect:/" + USER_PERIOD);

		if (file.isEmpty()) {
			System.out.println("File is empty.....");
		}

		try {
			byte[] bytes = file.getBytes();
			List<UserPeriod> userPeriodList = xlsUserPeriodExtractor.extractUsers(bytes);

			userPeriodRepository.saveAll(userPeriodList);
		} catch (Exception e) {
			e.printStackTrace();
			mv.addObject("error", e.toString());
		}

		return mv;
	}

	@RequestMapping(RESET_DATA + "/" + USER_DATA)
	public ModelAndView resetUserData() {
		ModelAndView mv = new ModelAndView("redirect:/" + USER_DATA);

		userDataRepository.deleteAll();
		
		Iterable<UserPeriod> userPeriodItr = userPeriodRepository.findAll();
	    List<UserPeriod> userPeriodList = new ArrayList<UserPeriod>();
	    userPeriodItr.forEach(userPeriodList::add);
	    for(UserPeriod userPeriod : userPeriodList){
	    	if(userPeriod.getUserDataId() > 0){
	    		userPeriod.setUserDataId(0);
	    		userPeriodRepository.save(userPeriod);
	    	}
	    }
		
		return mv;
	}
	
	@RequestMapping(RESET_DATA + "/" + USER_PERIOD)
	public ModelAndView resetUserPeriod() {
		ModelAndView mv = new ModelAndView("redirect:/" + USER_PERIOD);

		userPeriodRepository.deleteAll();
		
		return mv;
	}
	
	@RequestMapping("mapTables/" + USER_PERIOD)
	public ModelAndView mapTables(HttpSession httpSession) {
		ModelAndView mv = new ModelAndView("redirect:/" + USER_PERIOD);
		
		Iterable<UserData> userDataItr = userDataRepository.findAll();
	    List<UserData> userDataList = new ArrayList<UserData>();
	    userDataItr.forEach(userDataList::add);
		
		Iterable<UserPeriod> userPeriodItr = userPeriodRepository.findAll();
	    List<UserPeriod> userPeriodList = new ArrayList<UserPeriod>();
	    userPeriodItr.forEach(userPeriodList::add);
	    for(UserPeriod userPeriod : userPeriodList){
	    	List<UserData> userDataMappingList= userMapper.getPerfectMatch(userPeriod, userDataList);
	    	if(userDataMappingList != null && !userDataMappingList.isEmpty()){
	    		userPeriod.setUserDataId(userDataMappingList.get(0).getUserDataId());
	    		userPeriodRepository.save(userPeriod);
	    	}
	    }
		
		return mv;
	}
	
	@RequestMapping("mapUser/" + USER_PERIOD)
	public ModelAndView mapUser(@RequestParam("userPeriodId") long userPeriodId, @RequestParam("userDataId") long userDataId, HttpSession httpSession) throws Exception {
		ModelAndView mv = new ModelAndView("redirect:/" + USER_PERIOD);

		Optional<UserPeriod> userPeriodOpt = userPeriodRepository.findById(userPeriodId);
		if(!userPeriodOpt.isPresent()){
			throw new Exception("No userPeriod could be found with id " + userPeriodId);
		}

		UserPeriod userPeriod = userPeriodOpt.get();
		userPeriod.setUserDataId(userDataId);
		userPeriodRepository.save(userPeriod);
		
		return mv;
	}
	
	@RequestMapping("selectUser/" + USER_PERIOD)
	public ModelAndView selectUser(@RequestParam("userPeriodId") long userPeriodId, HttpSession httpSession) throws Exception {
		ModelAndView mv = new ModelAndView("userMapping");
		mv.addObject("userKey", USER_PERIOD);
		
		Iterable<UserData> userDataItr = userDataRepository.findAll();
	    List<UserData> userDataList = new ArrayList<UserData>();
	    userDataItr.forEach(userDataList::add);
		
		Optional<UserPeriod> userPeriodOpt = userPeriodRepository.findById(userPeriodId);
		if(!userPeriodOpt.isPresent()){
			throw new Exception("No userPeriod could be found with id " + userPeriodId);
		}
		
		UserPeriod userPeriod = userPeriodOpt.get();
    	List<UserData> userDataMappingList= userMapper.getBestMatch(userPeriod, userDataList);
    	
    	mv.addObject("userPeriod", userPeriod);
    	mv.addObject("userDataMappingList", userDataMappingList);
		
		return mv;
	}
	
	@RequestMapping(value = "download/" + USER_DATA, method = RequestMethod.GET)
	public ResponseEntity<ByteArrayResource> downloadFromUserData(@RequestParam("userDataId") long userDataId, HttpSession httpSession) throws Exception {
		Optional<UserData> userDataOpt = userDataRepository.findById(userDataId);
		
		if(!userDataOpt.isPresent()){
			throw new Exception("No userData could be found with id " + userDataId);
		}
		
		Date issueDate = (Date) httpSession.getAttribute(ISSUE_DATE);
		if(issueDate == null){
			issueDate = new Date();
		}
		
		UserData userData = userDataOpt.get();
		UserPeriod userPeriod = null;
		
		Iterable<UserPeriod> userPeriodItr = userPeriodRepository.findAll();
	    List<UserPeriod> userPeriodList = new ArrayList<UserPeriod>();
	    userPeriodItr.forEach(userPeriodList::add);

		for (UserPeriod dbUserPeriod : userPeriodList) {
			if (dbUserPeriod.getUserDataId() == userData.getUserDataId()) {
				userPeriod = dbUserPeriod;
				break;
			}
		}
		
		return download(userData, userPeriod, issueDate);
	}
	
	@RequestMapping(value = "download/" + USER_PERIOD, method = RequestMethod.GET)
	public ResponseEntity<ByteArrayResource> downloadFromUserPeriod(@RequestParam("userPeriodId") long userPeriodId, HttpSession httpSession) throws Exception {
		Optional<UserPeriod> userPeriodOpt = userPeriodRepository.findById(userPeriodId);
		
		if(!userPeriodOpt.isPresent()){
			throw new Exception("No userPeriod could be found with id " + userPeriodId);
		}

		Date issueDate = (Date) httpSession.getAttribute(ISSUE_DATE);
		if(issueDate == null){
			issueDate = new Date();
		}
		
		UserData userData = null;
		UserPeriod userPeriod = userPeriodOpt.get();
		
		if(userPeriod.getUserDataId() > 0){
			Optional<UserData> userDataOpt = userDataRepository.findById(userPeriod.getUserDataId());
			userData = userDataOpt.get();
		}

		return download(userData, userPeriod, issueDate);
	}

	private ResponseEntity<ByteArrayResource> download(UserData userData, UserPeriod userPeriod, Date issueDate) throws Exception {
        byte[] data = docxUserDataExporter.getDocx(userData, userPeriod, issueDate);
        ByteArrayResource resource = new ByteArrayResource(data);
 
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=" + getFileName(userData, userPeriod))
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .contentLength(data.length)
                .body(resource);
	}
	
	

	
	@RequestMapping(value = "downloadMultiple/" + USER_DATA, method = RequestMethod.POST)
	public ResponseEntity<ByteArrayResource> downloadMultipleFromUserData(@RequestParam("userDataIds") List<String> userDataIds, HttpSession httpSession) throws Exception {

		Map<String, byte[]> files = new HashMap<String, byte[]>();
		
		for(String userDataIdStr :  userDataIds){
			long userDataId = 0;
			try{
				userDataId = Long.parseLong(userDataIdStr);
			} catch(Exception e){
				e.printStackTrace();
			}
					
			if(userDataId == 0){
				continue; //nothing to do here....
			}
			
			Optional<UserData> userDataOpt = userDataRepository.findById(userDataId);
			
			if(!userDataOpt.isPresent()){
				continue; //nothing to do here....
			}
			
			UserData userData = userDataOpt.get();
			UserPeriod userPeriod = null;
			
			Iterable<UserPeriod> userPeriodItr = userPeriodRepository.findAll();
		    List<UserPeriod> userPeriodList = new ArrayList<UserPeriod>();
		    userPeriodItr.forEach(userPeriodList::add);

		    for(UserPeriod dbUserPeriod : userPeriodList){
		    	if(dbUserPeriod.getUserDataId() == userDataId){
		    		userPeriod = dbUserPeriod;
		    		break;
		    	}
		    }
			
			Date issueDate = (Date) httpSession.getAttribute(ISSUE_DATE);
			if(issueDate == null){
				issueDate = new Date();
			}
			
			String fileName = getFileName(userData, userPeriod);
			byte[] fileBytes = docxUserDataExporter.getDocx(userData, userPeriod, issueDate);
			files.put(fileName, fileBytes);
		}
		
		return downloadZip(files);
	}
	
	@RequestMapping(value = "downloadMultiple/" + USER_PERIOD, method = RequestMethod.POST)
	public ResponseEntity<ByteArrayResource> downloadMultipleFromUserPeriod(@RequestParam("userPeriodIds") List<String> userPeriodIds, HttpSession httpSession) throws Exception {

		Map<String, byte[]> files = new HashMap<String, byte[]>();
		
		for(String userPeriodIdStr :  userPeriodIds){
			long userPeriodId = 0;
			try{
				userPeriodId = Long.parseLong(userPeriodIdStr);
			} catch(Exception e){
				e.printStackTrace();
			}
					
			if(userPeriodId == 0){
				continue; //nothing to do here....
			}
			
			Optional<UserPeriod> userPeriodOpt = userPeriodRepository.findById(userPeriodId);
			
			if(!userPeriodOpt.isPresent()){
				continue; //nothing to do here....
			}
			
			UserData userData = null;
			UserPeriod userPeriod = userPeriodOpt.get();
			
			if(userPeriod.getUserDataId() > 0){
				Optional<UserData> userDataOpt = userDataRepository.findById(userPeriod.getUserDataId());
				userData = userDataOpt.get();
			}
			
			Date issueDate = (Date) httpSession.getAttribute(ISSUE_DATE);
			if(issueDate == null){
				issueDate = new Date();
			}
			
			String fileName = getFileName(userData, userPeriod);
			byte[] fileBytes = docxUserDataExporter.getDocx(userData, userPeriod, issueDate);
			files.put(fileName, fileBytes);
		}
		
		return downloadZip(files);
	}

	
	private String getFileName(UserData userData, UserPeriod userPeriod){
		String lname = userData != null && StringUtils.hasLength(userData.getLname())
				? userData.getLname()
				: userPeriod != null && StringUtils.hasLength(userPeriod.getLname())
						? userPeriod.getLname() : "";
		lname = lname.trim();

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
						
		String fileName = lname;
		fileName += StringUtils.hasLength(fname) ? "_" + fname : "";
		fileName += StringUtils.hasLength(cnp) ? "_" + cnp : "";
		fileName += ".docx";
		fileName = fileName.replaceAll(" ", "_");
		fileName = LocalStringUtil.replaceDiacrit(fileName);
		
		return fileName;
	}
	
	private ResponseEntity<ByteArrayResource> downloadZip(Map<String, byte[]> files) throws IOException{
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		ZipOutputStream zipOut = new ZipOutputStream(bos);
		for (Map.Entry<String, byte[]> entry : files.entrySet()) {
			InputStream fis = new ByteArrayInputStream(entry.getValue());
			ZipEntry zipEntry = new ZipEntry(entry.getKey());
			zipOut.putNextEntry(zipEntry);

			byte[] bytes = new byte[1024];
			int length;
			while ((length = fis.read(bytes)) >= 0) {
				zipOut.write(bytes, 0, length);
			}
			fis.close();
			
			zipOut.closeEntry();
		}

		byte[] zipBytes = bos.toByteArray();
		ByteArrayResource resource = new ByteArrayResource(zipBytes);
 
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=user.zip")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .contentLength(zipBytes.length)
                .body(resource);
	}
}
