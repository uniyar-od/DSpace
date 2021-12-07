package org.dspace.app.webui.ldn;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LDNUtils {
	
	private static Pattern handleRegexMatch = Pattern.compile("\\d{1,}\\/\\d{1,}");
	
	public static String getHandleFromURL(String url) {
		Matcher matcher = handleRegexMatch.matcher(url);
		StringBuilder handle=new StringBuilder();
		if (matcher.find())
		{
			handle.append(matcher.group(0));
		}
		return handle.toString();
	}
	
}
