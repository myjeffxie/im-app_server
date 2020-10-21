package cn.wildfirechat.app;


import cn.wildfirechat.app.pojo.CreateSessionRequest;
import com.google.gson.Gson;
import com.oracle.javafx.jmx.json.JSONReader;
import okhttp3.*;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class Test {
	public static void main(String[] args) {
		System.out.println("Hello");

		String createSession = "http://127.0.0.1:8888/pc_session";
		String loginSession = "http://127.0.0.1:8888/session_login/";
		for (int i = 0; i < 100; i++) {
			CreateSessionRequest request = new CreateSessionRequest();
			request.setDevice_name("pc"+i);
			request.setClientId("client"+i);
			request.setPlatform(3);
			request.setFlag(1);
			request.setToken("token"+i);
			httpJsonPost(createSession, new Gson().toJson(request), new Callback() {
				@Override
				public void onFailure(Call call, IOException e) {

				}

				@Override
				public void onResponse(Call call, Response response) throws IOException {
					String resStr = response.body().string();
					LOG.info("respose {}", resStr);

					JSONObject jo = new JSONObject(resStr);
					if (jo.getInt("code") == 0) {
						JSONObject result = jo.getJSONObject("result");
						String token = result.getString("token");
						LOG.info("the token is {}", token);
						httpJsonPost(loginSession + token, "", new Callback() {
							@Override
							public void onFailure(Call call, IOException e) {
								LOG.info("error");
							}

							@Override
							public void onResponse(Call call, Response response) throws IOException {
								LOG.info("success {}", response.body().string());
							}
						});
					}
				}
			});
		}
	}
	private static final Logger LOG = LoggerFactory.getLogger(Test.class);
	public static final MediaType JSONMediaType = MediaType.get("application/json; charset=utf-8");
	static private final Dispatcher dispatch = new Dispatcher();
	static {
		dispatch.setMaxRequests(200);
		dispatch.setMaxRequestsPerHost(200);
	}
	static private final OkHttpClient client = new OkHttpClient.Builder()
			.callTimeout(60, TimeUnit.SECONDS)
			.writeTimeout(60, TimeUnit.SECONDS)
			.readTimeout(60, TimeUnit.SECONDS)
			.dispatcher(dispatch)
			.build();

	public static void httpJsonPost(final String url, final String jsonStr, final Callback callback){
		LOG.info("POST to {} with data {}", url, jsonStr);
		if (url == null) {
			LOG.error("http post failure with empty url");
			return;
		}

		RequestBody body = RequestBody.create(JSONMediaType, jsonStr);
		Request request = new Request.Builder()
				.url(url)
				.post(body)
				.build();
		client.newCall(request).enqueue(new Callback() {
			@Override
			public void onFailure(Call call, IOException e) {
				e.printStackTrace();
				LOG.info("POST to {} with data {} failure", url, jsonStr);
				callback.onFailure(call, e);
			}

			@Override
			public void onResponse(Call call, Response response) throws IOException {
				LOG.info("POST to {} success with response: {}", url, response.body());
				callback.onResponse(call, response);

				try {
					if (response.body() != null) {
						response.body().close();
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
}
