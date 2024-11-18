package com.monthlyib.server.utils;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mobile.device.Device;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Slf4j
public class WebUtils {

    private static final String[] IP_HEADER_CANDIDATES = {
            "X-Forwarded-For",
            "Proxy-Client-IP",
            "WL-Proxy-Client-IP",
            "HTTP_X_FORWARDED_FOR",
            "HTTP_X_FORWARDED",
            "HTTP_X_CLUSTER_CLIENT_IP",
            "HTTP_CLIENT_IP",
            "HTTP_FORWARDED_FOR",
            "HTTP_FORWARDED",
            "HTTP_VIA",
            "REMOTE_ADDR"
    };
    public static String device() {
        HttpServletRequest request = request();

        if (request == null) {
            return "정보없음 (Request NULL)";
        }
        String header = request.getHeader("X-Device-Custom");
        if (header != null && header.equals("true")) {
            return "모바일/태블릿";
        } else if (header != null && header.equals("false")){
            return "컴퓨터";
        }
        Device device = (Device) request.getAttribute("currentDevice");
        if (device == null) {
            return "정보없음";
        }

        if (device.isMobile()) {
            return "모바일";
        } else if (device.isTablet()) {
            return "태블릿";
        } else if (device.isNormal()) {
            return "컴퓨터";
        } else {
            return "알수없음";
        }
    }

    private static HttpServletRequest request() {
        try {
            ServletRequestAttributes sra = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
            return sra.getRequest();
        } catch (RuntimeException e) {
            log.info("request 정보를 가져오지 못하였습니다. - {}", e.getMessage());
            log.info("{}", ErrorUtils.trace(e.getStackTrace()));
            return null;
        }
    }

    public static String ip() {
        HttpServletRequest request = request();
        if (request == null) {
            return "unknown(null)";
        }

        for (String header : IP_HEADER_CANDIDATES) {
            String ip = request.getHeader(header);
            if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
                return (ip.split(",")[0]).split(":")[0];
            }
        }

        String ip = request.getRemoteAddr();
        if ("0:0:0:0:0:0:0:1".equals(ip)) ip = "127.0.0.1";
        return ip;
    }


}
