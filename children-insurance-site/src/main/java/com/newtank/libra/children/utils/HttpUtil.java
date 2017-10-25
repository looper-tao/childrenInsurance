package com.newtank.libra.children.utils;

import javax.servlet.http.HttpServletRequest;

public class HttpUtil {

  /**
   * 获取客户端的Ip地址
   *
   * @return IP地址
   */
  public static String getIp(HttpServletRequest request) {
    String ip = request.getHeader("X-Forwarded-For");
    //FIXME
    if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
      if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
        ip = request.getHeader("Proxy-Client-IP");
      }
      if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
        ip = request.getHeader("WL-Proxy-Client-IP");
      }
      if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
        ip = request.getHeader("HTTP_CLIENT_IP");
      }
      if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
        ip = request.getHeader("HTTP_X_FORWARDED_FOR");
      }
      if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
        ip = request.getRemoteAddr();
      }
    } else if (ip.length() > 15) {
      String[] ips = ip.split(",");
      for (String result : ips) {
        if (!("unknown".equalsIgnoreCase(result))) {
          ip = result;
          break;
        }
      }
    }
    return ip;
  }
}
