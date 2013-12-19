package eu.ourspace.Structures;

import org.json.JSONObject;

import android.text.Html;



public class RecentActivity extends Object {
	public int topicId;
	public String photo;
	public String name;
	public String location;
	public String subject;
	public String description;
	public String language;
	public String imgUrl;
	public int phaseId;
	
	public RecentActivity() {
		topicId = 0;
		photo = "";
		name = "";
		location = "";
		subject = "";
		description = "";
		language = "";
		imgUrl = "";
		phaseId = 0;
	}
	
	public RecentActivity(int topicId, String photo, String name, String location,
			String subject, String description, String language, String imgUrl, int phaseId) {
		this.topicId = topicId;
		this.photo = photo;
		this.name = name;
		this.location = location;
		this.subject = subject;
		this.description = description;
		this.language = language;
		this.imgUrl = imgUrl;
		this.phaseId = phaseId;
	}
	
	
	public RecentActivity(JSONObject obj) {
		this();
		
		if (obj == null)
			return;
		
		String firstName = obj.optString("FirstName", "");
		String lastName = obj.optString("LastName", "");
		
		topicId = obj.optInt("ThreadId", 0);
		subject = obj.optString("Subject", "");
		description = obj.optString("Body", "");
		location = obj.optString("Country", "");
		language = obj.optString("ThreadLanguage", "en");
		imgUrl = obj.optString("ImgUrl", "");
		phaseId = obj.optInt("PhaseId", 0);
		
		description = Html.fromHtml(description).toString();
		description = description.replaceAll("\\<.*?>","");
		description = Html.fromHtml(description).toString();
		
		try {
			language = language.substring(0, 2);
			description = description.substring(0, Math.min(description.length(), 30)) + "..."; // trim the first 30 chars
			name = firstName + " " + lastName.substring(0, 1) + ".";
		} catch (IndexOutOfBoundsException e) {
		}
		
	}
}
