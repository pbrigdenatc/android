package eu.ourspace.Structures;

import org.json.JSONObject;



public class Topic extends Object {
	public int topicId;
	public int categoryId;
	public String categoryName;
	public String title;
	public int postsCount;
	public String lang;
	public String body;
	public int phaseId;
	public String name;
	
	public Topic() {
		topicId = 0;
		categoryId = 0;
		categoryName = "";
		title = "";
		postsCount = 0;
		lang = "en";
		body = "";
		phaseId = 0;
		name = "";
	}
	
	public Topic(int topicId, int categoryId, String categoryName, String title, int postsCount,
			String lang, String body, int phaseId, String name) {
		this.topicId = topicId;
		this.categoryId = categoryId;
		this.categoryName = categoryName;
		this.title = title;
		this.postsCount = postsCount;
		this.lang = lang;
		this.body = body;
		this.phaseId = phaseId;
		this.name = name;
	}
	
	
	// parsing when reading in a list
	public Topic(JSONObject obj) {
		this();
		
		if (obj == null)
			return;
		
		String firstName = obj.optString("FirstName", "");
		String lastName = obj.optString("LastName", "");
		
		topicId = obj.optInt("ThreadId", -1);
		if (topicId == -1)
			topicId = obj.optInt("ThreadID", 0);
		categoryId = obj.optInt("ForumId", 0);
		categoryName = obj.optString("Name", "");
		title = obj.optString("Subject", "");
		postsCount = obj.optInt("Replies", 0);
		body = obj.optString("Body", "");
		phaseId = obj.optInt("PhaseId", 0);
		lang = obj.optString("ThreadLanguage", "en");
		try {
			lang = lang.substring(0, 2);
			name = firstName + " " + lastName.substring(0, 1) + ".";
		} catch (IndexOutOfBoundsException e) {}
		
	}
	
}
