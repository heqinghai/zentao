package com.zentao;

import java.io.IOException;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

public class ZenTaoLogin {

	/**
	 * cookie�����zentaosid������zentaosid��¼�������޷���¼��ѯ
	 * 
	 * @param username
	 *            ������¼�û���
	 * @param password
	 *            ������¼����
	 * @param zentaosid
	 *            ������У���Ƿ���ڣ������޷���¼
	 * @return void
	 * @throws Exception
	 */

	public void zentaoLogin(String username, String password, String zentaosid) throws Exception {

		if (username.isEmpty() || password.isEmpty() || zentaosid.isEmpty()) {
			throw new Exception("����������¼����");
		}

		CloseableHttpClient httpClient = HttpClients.createDefault();
		try {
			HttpPost httpPost = new HttpPost(Param.LOGIN_URL);

			// ��������ͷ -->����������Ҫ���õ�¼�ӿ�����
			httpPost.setHeader("Accept", "application/json, text/javascript, */*; q=0.01");
			httpPost.setHeader("Accept-Encoding", "gzip, deflate");
			httpPost.setHeader("Accept-Language", "zh-CN,zh;q=0.9");
			httpPost.setHeader("Content-Type", "application/x-www-form-urlencoded");// ����Ҫ����һ����ֵ�ԣ�boundary=--------------------------161370145786553934711274
			httpPost.setHeader("Cookie",
					"ang=zh-cn; theme=default; windowWidth=1920; windowHeight=974; zentaosid=" + zentaosid);

			// ���� application/x-www-form-urlencoded ���������� -->����ʱ��д��
			String postBody = "account=" + username + "&password=" + password + "&referer=" + Param.REFER_URL;
			StringEntity postEntity = new StringEntity(postBody, "UTF-8");
			httpPost.setEntity(postEntity);

			// ��������
			CloseableHttpResponse response = httpClient.execute(httpPost);
			// ��ȡ��Ӧ��
			HttpEntity responseEntity = response.getEntity();
			// ������Ӧ�嵽�ַ���
			System.out.println(
					"**************login response: \n" + EntityUtils.toString(responseEntity) + "\n**************");

		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			// �ر�����,�ͷ���Դ
			try {
				httpClient.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}

	/**
	 * ��������ҳ��ȡzentaosid�����ڵ�¼��֤
	 * 
	 * @return String
	 */

	public String getZentaoID() {
		CloseableHttpClient httpClient = HttpClients.createDefault();

		try {
			HttpGet httpGet = new HttpGet(Param.HOST + Param.REFER_URL);
			httpGet.setHeader("Accept",
					"text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8");
			httpGet.setHeader("Accept-Encoding", "gzip, deflate");
			httpGet.setHeader("Accept-Language", "zh-CN,zh;q=0.9");
			CloseableHttpResponse response = httpClient.execute(httpGet);

			// ������Ӧͷ��
			Header[] headers = response.getAllHeaders();
			for (Header header : headers) {
				// System.out.println(header.getName() + "--->" +
				// header.getValue());
				String tempStr = header.getValue();
				if (tempStr.contains("zentaosid")) {
					return tempStr.split(";")[0].split("=")[1];
				}
			}

		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			// �ر�����,�ͷ���Դ
			try {
				httpClient.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		return "";
	}

}
