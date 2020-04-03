package dtb.user.print.util;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import dtb.user.print.entity.UserData;
import dtb.user.print.entity.UserPeriod;

@Component
public class UserMapper {
	
	public List<UserData> getPerfectMatch(UserPeriod userPeriod, List<UserData> userDataList){
		List<UserData> userDataMappingList = new ArrayList<UserData>();
		
		String lname = getCleanedString(userPeriod.getLname());
		String fname = getCleanedString(userPeriod.getFname());
		String cnp = StringUtils.hasLength(userPeriod.getCnp()) ? userPeriod.getCnp() : "";
		
		for(UserData userData : userDataList){
			String udLname = getCleanedString(userData.getLname());
			String udFname = getCleanedString(userData.getFname());
			String udCnp = StringUtils.hasLength(userData.getCnp()) ? userData.getCnp() : "";

			//cnp match
			if(StringUtils.hasLength(cnp) && StringUtils.hasLength(udCnp)){
				if(cnp.equalsIgnoreCase(udCnp)){
					userDataMappingList.add(userData);
					break;
				}
			}
			
			//complete name match
			if(lname.equalsIgnoreCase(udLname) && fname.equalsIgnoreCase(udFname)){
				userDataMappingList.add(userData);
				break;
			}
		}
		
		return userDataMappingList;
	}
	
	public List<UserData> getBestMatch(UserPeriod userPeriod, List<UserData> userDataList){
		List<UserData> userDataMappingList = new ArrayList<UserData>();
		
		int charMatchLength = 3;
		
		String lname = getCleanedString(userPeriod.getLname());
		String fname = getCleanedString(userPeriod.getFname());
		String cnp = StringUtils.hasLength(userPeriod.getCnp()) ? userPeriod.getCnp() : "";

		for(UserData userData : userDataList){
			String udCnp = StringUtils.hasLength(userData.getCnp()) ? userData.getCnp() : "";

			//cnp match
			if(StringUtils.hasLength(cnp) && StringUtils.hasLength(udCnp)){
				if(cnp.equalsIgnoreCase(udCnp)){
					userDataMappingList.add(userData);
				}
			}
		}
		
		for(UserData userData : userDataList){	
			String udLname = getCleanedString(userData.getLname());
			String udFname = getCleanedString(userData.getFname());
			
			//complete name match
			if(lname.equalsIgnoreCase(udLname) && fname.equalsIgnoreCase(udFname)){
				if(!userDataMappingList.contains(userData)){
					userDataMappingList.add(userData);
				}
			}
		}
		
		for(UserData userData : userDataList){	
			String udLname = getCleanedString(userData.getLname());
			
			//lname match
			if(lname.equalsIgnoreCase(udLname)){
				if(!userDataMappingList.contains(userData)){
					userDataMappingList.add(userData);
				}
			}
		}
		
		for(UserData userData : userDataList){	
			String udLname = getCleanedString(userData.getLname());
			
			//lname some chars match
			
			if(matchesPartialy(lname, udLname, 0, charMatchLength)){
				if(!userDataMappingList.contains(userData)){
					userDataMappingList.add(userData);
				}
			}
		}
		
		for(UserData userData : userDataList){	
			String udFname = getCleanedString(userData.getFname());
			
			//fname some chars match
			if(matchesPartialy(fname, udFname, 0, charMatchLength)){
				if(!userDataMappingList.contains(userData)){
					userDataMappingList.add(userData);
				}
			}
		}
		
		return userDataMappingList;
	}
	
	private boolean matchesPartialy(String value1, String value2, int beginIndex, int endIndex){
		String[] token1Arr = value1.split(" ");
		String[] token2Arr = value2.split(" "); 
		
		for(String token1 : token1Arr){
			if(beginIndex < 0 || beginIndex > token1.length() - 1){
				continue;
			}
			if(endIndex < 1 || endIndex > token1.length()){
				continue;
			}
			
			String subStr1 = token1.substring(beginIndex, endIndex);

			for(String token2 : token2Arr){
				if(beginIndex < 0 || beginIndex > token2.length() - 1){
					continue;
				}
				if(endIndex < 1 || endIndex > token2.length()){
					continue;
				}
				
				String subStr2 = token2.substring(beginIndex, endIndex);
				
				if(subStr1.equalsIgnoreCase(subStr2)){
					return true;
				}
			}
		}
		
		
		return false;
	}
	
	private String getCleanedString(String value){
		if(!StringUtils.hasLength(value)){
			return "";
		}
		
		return LocalStringUtil.replaceDiacrit(value.toLowerCase());
	}

}
