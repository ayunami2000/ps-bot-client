package me.ayunami2000.psbot;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;

public class OpenaiThread implements Runnable{
	public static boolean enabled=false;
	// public static String unm="";
	public static String cmd="";
	public static String apiKey="";
	public static int msgLimit = 6;
	public static int delay = 750;

	@Override
	public void run() {
		if (apiKey.isEmpty()) {
			PsBot.chatMsg("Error: No API key specified!");
			return;
		}
		enabled = true;
		PsBot.chatMsg("Enabled Openai!");
		while (enabled) {
			if (cmd.isEmpty()) {
				try {
					Thread.sleep(50);
				} catch (InterruptedException e) {
				}
			} else {
				String input = cmd;
				cmd = "";
				// String user = unm;
				// unm = "";
				String res = query(input, PsBot.mc.isInSingleplayer() ? null : PsBot.mc.getNetworkHandler().getConnection().getAddress().toString().split("/")[0]);
				if (!enabled) break;
				if (res != null) {
					// we have the result!! :D
					res = res.trim().replace("§", "&").replace("\t", "    ").replaceAll("\n+", "\n");
					String[] msgs = Pattern.compile("(?:.{1,256}(?:\\n|$|\\b)|.{256})")
							.matcher(res)
							.results()
							.map(MatchResult::group)
							.toArray(String[]::new);
					for (int i = 0; i < msgs.length && (msgLimit == 0 || i < msgLimit); i++) {
						String m = msgs[i].replace("\n", " ").trim();
						if (m.isEmpty()) continue;
						if (!enabled) break;
						PsBot.sendChatOrCommand(m);
						try {
							Thread.sleep(delay);
						} catch (InterruptedException e) {
						}
					}
				}
			}
		}
		cmd = "";
		// unm = "";
	}

	private static String query(String input, String user) {
		try {
			URL url = new URL("https://api.openai.com/v1/completions");
			HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
			httpConn.setRequestMethod("POST");

			httpConn.setRequestProperty("Content-Type", "application/json; charset=utf-8");
			httpConn.setRequestProperty("Authorization", "Bearer " + apiKey);

			httpConn.setDoOutput(true);
			JsonObject jsonObject = new JsonObject();
			jsonObject.addProperty("model", "text-davinci-003");
			jsonObject.addProperty("prompt", input);
			jsonObject.addProperty("temperature", 0.7);
			jsonObject.addProperty("max_tokens", 256);
			jsonObject.addProperty("top_p", 1);
			jsonObject.addProperty("frequency_penalty", 0);
			jsonObject.addProperty("presence_penalty", 0);
			if (user != null && !user.isEmpty()) jsonObject.addProperty("user", user);
			httpConn.getOutputStream().write(jsonObject.toString().getBytes(StandardCharsets.UTF_8));
			httpConn.getOutputStream().flush();
			httpConn.getOutputStream().close();

			InputStream responseStream = httpConn.getResponseCode() / 100 == 2
					? httpConn.getInputStream()
					: httpConn.getErrorStream();
			String response = new String(responseStream.readAllBytes(), StandardCharsets.UTF_8);
			JsonElement resJson = JsonParser.parseString(response);
			if (!resJson.isJsonObject()) {
				return wrapError("An error occurred parsing the response.");
			}
			JsonObject resJsonObject = resJson.getAsJsonObject();
			if (resJsonObject.has("choices")) {
				JsonArray choices = resJsonObject.getAsJsonArray("choices");
				if (choices.size() == 0) {
					return null;
				} else {
					return choices.get(0).getAsJsonObject().get("text").getAsString();
				}
			} else if (resJsonObject.has("error")) {
				return wrapError(resJsonObject.getAsJsonObject("error").get("message").getAsString());
			} else {
				return wrapError("An error occurred parsing the response.");
			}
		} catch (IOException e) {
			e.printStackTrace();
			return wrapError(e.getMessage());
		}
	}

	private static String wrapError(String error) {
		System.err.println("OpenAI Error: " + error);
		return error;
	}
}
