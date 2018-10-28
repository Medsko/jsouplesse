package jsouplesse.dataaccess.dao;

import java.util.ArrayList;
import java.util.List;

/**
 * Holds all relevant data about a company.
 */
public class Company {

	private String name;
	
	private String homePageUrl;
	
	private String phoneNumber;
	
	private String mobileNumber;
	
	private List<String> emailAddressList = new ArrayList<>();
	
	private String emailAddress;
	
	public Company(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		
		String toString = "Bedrijf: " + name;
		
		if (mobileNumber != null && mobileNumber.length() != 0)
			toString += System.lineSeparator() + "mobiel telnr: " + mobileNumber;
		if (phoneNumber != null && phoneNumber.length() != 0)
			toString += "\t vast telnr: " + phoneNumber;
		if (emailAddressList.size() == 0 && emailAddress != null)
			toString += "\t email adres: " + emailAddress;
		else
			for (String emailAddress : emailAddressList)
				toString += System.lineSeparator() + "mogelijk email adres: " + emailAddress;
		if (homePageUrl != null && homePageUrl.length() != 0)
			toString += System.lineSeparator() + homePageUrl;
		
		return toString;
	}
	
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getHomePageUrl() {
		return homePageUrl;
	}

	public void setHomePageUrl(String homePageUrl) {
		this.homePageUrl = homePageUrl;
	}

	public String getPhoneNumber() {
		return phoneNumber;
	}

	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}

	public String getMobileNumber() {
		return mobileNumber;
	}

	public void setMobileNumber(String mobileNumber) {
		this.mobileNumber = mobileNumber;
	}

	public List<String> getDubiousEmailAddresses() {
		return emailAddressList;
	}
	
	public String getEmailAddress() {
		if (emailAddressList.size() == 0)
			return emailAddress;
		else
			return null;
	}

	public void addEmailAddress(String emailAddress) {
		if (this.emailAddress == null)
			this.emailAddress = emailAddress;
		else {
			// Apparently, more than one email address has been found.
			// This reduces credibility of each found email address.
			emailAddressList.add(this.emailAddress);
			emailAddressList.add(emailAddress);
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((homePageUrl == null) ? 0 : homePageUrl.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Company other = (Company) obj;
		if (homePageUrl == null) {
			if (other.homePageUrl != null)
				return false;
		} else if (!homePageUrl.equals(other.homePageUrl))
			return false;
		return true;
	}
}
