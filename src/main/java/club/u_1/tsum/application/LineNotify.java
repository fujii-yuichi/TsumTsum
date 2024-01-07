/*
 * Copyright (c) 2024 Fujii Yuichi - MIT license
 */
package club.u_1.tsum.application;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Objects;
import java.util.stream.Collectors;

public class LineNotify {

	private final String token;

	public LineNotify(String token) {
		this.token = token;
	}

	public void notify(String message) {
		HttpURLConnection connection = null;
		try {
			URL url = new URL("https://notify-api.line.me/api/notify");
			connection = (HttpURLConnection) url.openConnection();
			connection.setDoOutput(true);
			connection.setRequestMethod("POST");
			connection.addRequestProperty("Authorization", "Bearer " + token);
			try (
				OutputStream os = connection.getOutputStream();
				PrintWriter writer = new PrintWriter(os)) {
				writer.append("message=").append(URLEncoder.encode(message, "UTF-8")).flush();
				try (
					InputStream is = connection.getInputStream();
					BufferedReader r = new BufferedReader(new InputStreamReader(is))) {
					String res = r.lines().collect(Collectors.joining());
					if (!res.contains("\"message\":\"ok\"")) {
						my.println(res);
						my.println("なんか失敗したっぽい");
					}
				}
			}
		} catch (Exception e) {
			myLogger.error(e);
		} finally {
			if (connection != null) {
				connection.disconnect();
			}
		}
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		LineNotify that = (LineNotify) o;
		return Objects.equals(token, that.token);
	}

	@Override
	public int hashCode() {
		return Objects.hash(token);
	}

	@Override
	public String toString() {
		return "LineNotify{" +
			"token='" + token + '\'' +
			'}';
	}

}
