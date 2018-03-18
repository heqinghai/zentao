package com.zentao;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

public class ZenTaoUtil {

	private String username;
	private String zentaosid;

	// �����û����������¼
	public ZenTaoUtil(String username, String password) throws Exception {
		this.username = username;

		ZenTaoLogin zt = new ZenTaoLogin();
		this.zentaosid = zt.getZentaoID();

		if (this.zentaosid.isEmpty()) {
			throw new Exception("**************zentaosid not found **************\n");
		}

		zt.zentaoLogin(username, password, this.zentaosid);
	}

	/**
	 * ���������������ԣ�n/a����ͨ����pass����ʧ�ܣ�fail��
	 * 
	 * @param moduleID
	 *            ָ��ģ���ID
	 * @param caseID
	 *            �������������ID
	 * @param resultList
	 *            �����pass��fail��n/a�������ж�Ӧ��ÿһ������������룬����Ͳ�����һ��
	 * @param reasonList
	 *            ʵ������������е�ÿһ�������������д������Ͳ�����һ��
	 */
	public boolean caseTagByHttp(String moduleID, String caseID, String[] resultList, String[] reasonList)
			throws Exception {

		if (moduleID.isEmpty() || caseID.isEmpty()) {
			throw new Exception("**************moduleID ����caseID�ǿյģ� ����������ǲ����Ƿ���ȷ**************");
		}

		// �ָ���
		String tempBoundary = "----WebKitFormBoundaryXPZRoJ4Bj9KNIVgc";

		// ����moduleID��caseID��ѯ��caseUrl
		HtmlUtil hu = new HtmlUtil();
		String tempCaseUrl = hu.getCaseUrlByModuleID(this.zentaosid, moduleID, caseID);
		System.out.println("**************tempCaseUrl: \n" + tempCaseUrl + "\n**************");
		String caseUrl = tempCaseUrl.split("\\;")[1];

		if (caseUrl.isEmpty()) {
			throw new Exception("caseUrl��ȡʧ��");
		}

		String versionID = tempCaseUrl.split("\\;")[2];
		// System.out.println("**************caseUrl: " + caseUrl +
		// "\n**************");
		// System.out.println("**************versionID: " + versionID +
		// "\n**************");

		// ����caseUrl��ѯstepID��realsID
		HashMap<String, String> paramsMap = hu.getCaseParamByUrl(caseID, caseUrl, moduleID, this.zentaosid);

		CloseableHttpClient httpClient = HttpClients.createDefault();
		try {
			HttpPost httpPost = new HttpPost(Param.HOST + caseUrl);

			httpPost.setHeader("Accept", "application/json, text/javascript, */*; q=0.01");
			httpPost.setHeader("Accept-Encoding", "gzip, deflate");
			httpPost.setHeader("Accept-Language", "zh-CN,zh;q=0.9");
			httpPost.setHeader("Content-Type", "multipart/form-data; boundary=" + tempBoundary);// ����Ҫ����һ����ֵ�ԣ�boundary=--------------------------161370145786553934711274
			httpPost.setHeader("Cookie", "qaBugOrder=id_desc; keepLogin=on; za=" + this.username
					+ "; preBranch=0; lang=zh-cn; theme=default; bugModule=0; caseSide=show; zp=f0947ae6bf9cad7c1f20b26ab7359d70f15014e2; checkedItem=; lastProject=7; preProductID=2; lastProduct=2; caseModule="
					+ moduleID + "; selfClose=0; windowWidth=1792; windowHeight=699; zentaosid=" + this.zentaosid);

			StringBuffer sb = new StringBuffer();
			for (Map.Entry<String, String> temp : paramsMap.entrySet()) {
				// System.out.println(temp.getKey() + "-->" + temp.getValue());

				if (temp.getKey().equalsIgnoreCase(caseID)) {

					String[] tempArray = temp.getValue().split("\\;");

					// �жϳ���
					if (tempArray.length != resultList.length || tempArray.length != reasonList.length) {
						throw new Exception(
								"**************resultList[] ���� reasonList[] ���� �� ����ִ�в�������һ�£��������ã���**************");
					}

					for (int i = 0; i < tempArray.length; i++) {

						String tempStepID = tempArray[i].split(",")[0];
						String tempRealsID = tempArray[i].split(",")[1];

						if (!Arrays.asList(Param.RESULT_LIST).contains(resultList[i])) {
							throw new Exception("**************resultList[] �а����Ƿ����ֶΣ��������ã���**************");
						}

						sb.append("--" + tempBoundary + "\n" + "Content-Disposition: form-data; name=\"" + tempStepID
								+ "\"" + "\n\n" + resultList[i] + "\n");
						sb.append("--" + tempBoundary + "\n" + "Content-Disposition: form-data; name=\"" + tempRealsID
								+ "\"" + "\n\n" + reasonList[i] + "\n");
					}
				}

			}

			sb.append("--" + tempBoundary + "\n" + "Content-Disposition: form-data; name=\"case\"" + "\n\n" + caseID
					+ "\n");
			sb.append("--" + tempBoundary + "\n" + "Content-Disposition: form-data; name=\"version\"" + "\n\n"
					+ versionID + "\n");
			sb.append("--" + tempBoundary + "--\n");

			// System.out.println("**************post body: \n" + sb.toString()
			// + "\n**************");

			StringEntity postEntity = new StringEntity(sb.toString(), "UTF-8");
			httpPost.setEntity(postEntity);

			CloseableHttpResponse response = httpClient.execute(httpPost);
			HttpEntity responseEntity = response.getEntity();
			System.out
					.println("**************case flag: \n" + EntityUtils.toString(responseEntity) + "\n**************");

			if (response.getStatusLine().getStatusCode() == 200) {
				return true;
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

		return false;

	}

}
