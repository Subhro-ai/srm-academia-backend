package com.portal.academia_portal.service;
import com.portal.academia_portal.dto.AttendanceDetail;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class DataService {

    private final WebClient webClient;

    @Autowired
    public DataService(WebClient webClient) {
        this.webClient = webClient;
    }

    public List<AttendanceDetail> getAttendance(String cookie) {
        String attendanceUrl = "https://academia.srmist.edu.in/srm_university/academia-academic-services/page/My_Attendance";

        String rawHtml = webClient.get()
                .uri(attendanceUrl)
                .header(HttpHeaders.COOKIE, cookie)
                .header(HttpHeaders.USER_AGENT, "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/108.0.0.0 Safari/537.36")
                .retrieve()
                .bodyToMono(String.class)
                .block();

        if (rawHtml == null) {
            throw new IllegalStateException("Did not receive a response from the attendance page.");
        }

        String encodedHtml = extractEncodedContent(rawHtml);
        if (encodedHtml.isEmpty()) {
            throw new IllegalStateException("Could not extract encoded HTML from the response.");
        }


        String cleanHtml = decodeHtml(encodedHtml);

        Document doc = Jsoup.parse(cleanHtml);
        List<AttendanceDetail> attendanceList = new ArrayList<>();
        Elements tableRows = doc.select("table[style*=font-size :16px;][bgcolor=#FAFAD2] tr");

        for (int i = 1; i < tableRows.size(); i++) {
            Element row = tableRows.get(i);
            Elements cols = row.select("td");

            if (cols.size() < 9) continue;

            AttendanceDetail detail = new AttendanceDetail();
            detail.setCourseCode(cols.get(0).ownText().trim());
            detail.setCourseTitle(cols.get(1).text().trim());
            detail.setCourseCategory(cols.get(2).text().trim());
            detail.setCourseFaculty(cols.get(3).text().trim());
            detail.setCourseSlot(cols.get(4).text().trim());
            detail.setCourseConducted(Integer.parseInt(cols.get(6).text().trim()));
            detail.setCourseAbsent(Integer.parseInt(cols.get(7).text().trim()));
            detail.setCourseAttendance(cols.get(8).text().trim());

            attendanceList.add(detail);
        }

        return attendanceList;
    }

    private String extractEncodedContent(String rawHtml) {
        final Pattern pattern = Pattern.compile("pageSanitizer\\.sanitize\\('(.*)'\\);", Pattern.DOTALL);
        final Matcher matcher = pattern.matcher(rawHtml);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return "";
    }


    private String decodeHtml(String encodedHtml) {
        try {

            String partiallyCleaned = encodedHtml
                    .replaceAll("\\\\x([0-9A-Fa-f]{2})", "%$1")
                    .replaceAll("\\\\'", "'")
                    .replaceAll("\\\\\"", "\"");


            return URLDecoder.decode(partiallyCleaned, StandardCharsets.UTF_8.toString());
        } catch (UnsupportedEncodingException e) {

            throw new RuntimeException("Error decoding HTML", e);
        }
    }
}