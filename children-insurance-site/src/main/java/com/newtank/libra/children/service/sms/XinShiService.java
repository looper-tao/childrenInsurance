package com.newtank.libra.children.service.sms;

import com.newtank.libra.children.entity.SMSProvider;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.*;

;

/**
 * Created by looper on 2017/9/27.
 */
public class XinShiService implements SMSService {

  private static final Logger LOGGER = LoggerFactory.getLogger(XinShiService.class);

  private static final String XINSHI_USERID = "1079";
  private static final String XINSHI_ACTION = "send";

  private SMSProvider smsProvider;

  /**
   * 解析短信发送结果
   */
  private static boolean parseResult(String result) {
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    try {
      DocumentBuilder builder = factory.newDocumentBuilder();
      Document document = builder.parse(new ByteArrayInputStream(result.getBytes()));
      Node returnStatus = document.getElementsByTagName("returnstatus").item(0).getFirstChild();
      if (returnStatus != null && returnStatus.getNodeValue().equals("Success")) {
        return true;
      }
    } catch (Exception e) {
      LOGGER.error("短信发送结果解析异常：" + e.getMessage());
      return false;
    }
    return false;
  }

  private static String formatDateTime(Date date) {
    if (date == null) {
      return null;
    }
    return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date);
  }

  /**
   * 发送短信
   *
   * @param mobilePhone 要发送的手机号
   * @param content     要发送的内人
   * @return 是否发送成功
   */
  @Override
  public boolean send(String mobilePhone, String content) {
    Map<String, String> params = assembleRequestData(mobilePhone, content, smsProvider);
    List<NameValuePair> parameters = new ArrayList<>(params.size());
    for (Map.Entry<String, String> entry : params.entrySet()) {
      parameters.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
    }
    try {
      UrlEncodedFormEntity entity = new UrlEncodedFormEntity(parameters, CHARSET);
      HttpClient client = new DefaultHttpClient();

      HttpPost httppost = new HttpPost(smsProvider.getSendUrl());
      httppost.setEntity(entity);
      HttpResponse response = client.execute(httppost);
      String responseString = EntityUtils.toString(response.getEntity(), CHARSET);
      LOGGER.info("sms send result:" + responseString);

      return parseResult(responseString);

    } catch (UnsupportedEncodingException e) {
      LOGGER.error("短信参数编码异常：" + e.getMessage());
    } catch (IOException e) {
      LOGGER.error("发送短信异常：" + e.getMessage());
    }

    return false;
  }

  @Override
  public SMSProvider getSmsProvider() {
    return smsProvider;
  }

  @Override
  public void setSmsProvider(SMSProvider smsProvider) {
    this.smsProvider = smsProvider;
  }

  /**
   * 拼接短信发送所需的参数
   *
   * @param mobilePhone 手机号
   * @param content     要发送的短信内容
   * @return 拼接后的参数
   */
  private Map<String, String> assembleRequestData(String mobilePhone, String content, SMSProvider smsProvider) {
    Map<String, String> params = new HashMap<String, String>();
    params.put("userid", XINSHI_USERID);
    params.put("account", smsProvider.getAccount());
    params.put("password", smsProvider.getPassword());
    params.put("action", XINSHI_ACTION);
    params.put("mobile", mobilePhone);
    params.put("sendTime", formatDateTime(new Date()));
    params.put("content", content);

    return params;
  }
}
