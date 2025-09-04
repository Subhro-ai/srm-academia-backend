package com.portal.academia_portal.service;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.portal.academia_portal.dto.AttendanceDetail;
import com.portal.academia_portal.dto.CourseInfo;
import com.portal.academia_portal.dto.CourseSlot;
import com.portal.academia_portal.dto.DaySchedule;
import com.portal.academia_portal.dto.Mark;
import com.portal.academia_portal.dto.MarkDetail;
import com.portal.academia_portal.dto.TimetableData;
import com.portal.academia_portal.dto.TotalAttendance;
import com.portal.academia_portal.dto.UserInfo;

@Service
public class DataService {

  private final WebClient webClient;
  private static final Logger logger =
    LoggerFactory.getLogger(DataService.class);

  @Autowired
  public DataService(WebClient webClient) {
    this.webClient = webClient;
  }

  public List<AttendanceDetail> getAttendance(String cookie) {
    String attendanceUrl =
      "https://academia.srmist.edu.in/srm_university/academia-academic-services/page/My_Attendance";

    String rawHtml = webClient
      .get()
      .uri(attendanceUrl)
      .header(HttpHeaders.COOKIE, cookie)
      .header(
        HttpHeaders.USER_AGENT,
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/108.0.0.0 Safari/537.36"
      )
      .retrieve()
      .bodyToMono(String.class)
      .block();

    if (rawHtml == null) {
      throw new IllegalStateException(
        "Did not receive a response from the attendance page."
      );
    }

    String encodedHtml = extractEncodedContent(rawHtml);
    if (encodedHtml.isEmpty()) {
      throw new IllegalStateException(
        "Could not extract encoded HTML from the response."
      );
    }

    String cleanHtml = decodeHtml(encodedHtml);

    Document doc = Jsoup.parse(cleanHtml);
    List<AttendanceDetail> attendanceList = new ArrayList<>();
    Elements tableRows = doc.select("table[style*=font-size :16px;][bgcolor=#FAFAD2] tr:not(:contains(Total))");


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
      detail.setCourseConducted(
        Integer.parseInt(cols.get(6).text().trim())
      );
      detail.setCourseAbsent(Integer.parseInt(cols.get(7).text().trim()));
      detail.setCourseAttendance(cols.get(8).text().trim());

      attendanceList.add(detail);
    }
    System.out.println(attendanceList);
    return attendanceList;
  }

  private String extractEncodedContent(String rawHtml) {
    final Pattern pattern = Pattern.compile(
      "pageSanitizer\\.sanitize\\('(.*)'\\);",
      Pattern.DOTALL
    );
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

      return URLDecoder.decode(
        partiallyCleaned,
        StandardCharsets.UTF_8.toString()
      );
    } catch (UnsupportedEncodingException e) {
      throw new RuntimeException("Error decoding HTML", e);
    }
  }

  public List<MarkDetail> getMarks(String cookie) {
    String attendanceUrl =
      "https://academia.srmist.edu.in/srm_university/academia-academic-services/page/My_Attendance";
    String rawHtml = webClient
      .get()
      .uri(attendanceUrl)
      .header(HttpHeaders.COOKIE, cookie)
      .header(
        HttpHeaders.USER_AGENT,
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/108.0.0.0 Safari/537.36"
      )
      .retrieve()
      .bodyToMono(String.class)
      .block();

    if (rawHtml == null) {
      throw new IllegalStateException(
        "Did not receive a response from the attendance page."
      );
    }

    String encodedHtml = extractEncodedContent(rawHtml);
    if (encodedHtml.isEmpty()) {
      throw new IllegalStateException(
        "Could not extract encoded HTML from the response."
      );
    }
    String cleanHtml = decodeHtml(encodedHtml);
    Document doc = Jsoup.parse(cleanHtml);
    List<MarkDetail> marksList = new ArrayList<>();
    Elements tableRows = doc.select("table[border=1][align=center] > tbody > tr");
    for (int i = 0; i < tableRows.size(); i++) {
      Element row = tableRows.get(i);
      Elements cols = row.select("td");
      if (cols.size() < 3) {
        continue;
      }
      MarkDetail markDetail = new MarkDetail();
      markDetail.setCourse(cols.get(0).text().trim());
      markDetail.setCategory(cols.get(1).text().trim());
      Element marksInnerTable = cols.get(2).selectFirst("table");
      if (marksInnerTable != null) {
        List<Mark> individualMarks = new ArrayList<>();
        Elements markCells = marksInnerTable.select("td");

        for (Element markCell : markCells) {
          String strongText = markCell.select("strong").text().trim();
          if (strongText.isEmpty() || !strongText.contains("/")) {
            continue;
          }

          String[] parts = strongText.split("/");
          String examName = parts[0].trim();
          double maxMark = Double.parseDouble(parts[1].trim());

          String obtainedText = markCell.ownText().trim();
          double obtainedMark = Double.parseDouble(obtainedText);

          Mark mark = new Mark();
          mark.setExam(examName);
          mark.setMaxMark(maxMark);
          mark.setObtained(obtainedMark);
          individualMarks.add(mark);
        }
        markDetail.setMarks(individualMarks);
      }
      marksList.add(markDetail);
    }

    return marksList;
  }

  public List<DaySchedule> getTimetable(String cookie) {
    String timetableUrl = getTimetableUrl();

    String rawHtml = webClient
      .get()
      .uri(timetableUrl)
      .header(HttpHeaders.COOKIE, cookie)
      .header(
        HttpHeaders.USER_AGENT,
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/108.0.0.0 Safari/537.36"
      )
      .retrieve()
      .bodyToMono(String.class)
      .block();

    if (rawHtml == null) {
      throw new IllegalStateException(
        "Did not receive a response from the timetable page."
      );
    }

    String cleanHtml = decodeHtml(extractEncodedContent(rawHtml));
    Document doc = Jsoup.parse(cleanHtml);

    String batchText = getTextFromTableRow(doc, "Batch:");
    if (batchText == null || batchText.trim().isEmpty()) {
      throw new IllegalStateException(
        "Could not determine batch from the timetable page."
      );
    }
    int batch = Integer.parseInt(batchText.trim());

    Map<String, CourseInfo> slotMap = new HashMap<>();

    // --- Start of Corrected Logic ---

    // 1. Select the table and get ALL table cells (td), skipping the headers
    Elements allCells = doc.select(".course_tbl td");

    // 2. Process the cells in chunks of 11, starting after the header row (index 11)
    for (int i = 11; i < allCells.size(); i += 11) {
      Elements cols = new Elements(
        allCells.subList(i, Math.min(i + 11, allCells.size()))
      );
      if (cols.size() < 11) continue; // Ensure we have a full set of columns for a course

      // 3. Use the correct column indexes to create the CourseInfo object
      CourseInfo info = new CourseInfo(
        cols.get(2).text().trim(), // Course Title
        cols.get(1).text().trim(), // Course Code
        cols.get(6).text().trim(), // Course Type
        cols.get(5).text().trim(), // Category
        cols.get(9).text().trim() // Room No.
      );

      String[] slots = cols.get(8).text().trim().split("-");
      for (String slot : slots) {
        if (!slot.trim().isEmpty()) {
          slotMap.put(slot.trim(), info);
        }
      }
    }

    // --- End of Corrected Logic ---

    List<DaySchedule> timetable = new ArrayList<>();
    List<TimetableData.DayDefinition> scheduleForBatch = TimetableData.BATCH_SLOTS.get(
      batch
    );

    if (scheduleForBatch == null) {
      throw new IllegalStateException(
        "No schedule definition found for batch: " + batch
      );
    }

    for (TimetableData.DayDefinition dayDef : scheduleForBatch) {
      DaySchedule daySchedule = new DaySchedule();
      daySchedule.setDayOrder(dayDef.dayOrder());

      List<CourseSlot> courseSlots = new ArrayList<>();
      for (int j = 0; j < dayDef.slots().size(); j++) {
        String slotName = dayDef.slots().get(j);
        CourseInfo courseInfo = slotMap.get(slotName);

        CourseSlot courseSlot = new CourseSlot();
        courseSlot.setSlot(slotName);
        courseSlot.setTime(dayDef.time().get(j));

        if (courseInfo != null) {
          courseSlot.setClass(true);
          courseSlot.setCourseTitle(courseInfo.courseTitle());
          courseSlot.setCourseCode(courseInfo.courseCode());
          courseSlot.setCourseType(courseInfo.courseType());
          courseSlot.setCourseCategory(courseInfo.courseCategory());
          courseSlot.setCourseRoomNo(courseInfo.courseRoomNo());
        } else {
          courseSlot.setClass(false);
        }
        courseSlots.add(courseSlot);
      }
      daySchedule.setClasses(courseSlots);
      timetable.add(daySchedule);
    }

    return timetable;
  }

  private String getTimetableUrl() {
    LocalDate currentDate = LocalDate.now();
    int currentYear = currentDate.getYear();
    String academicYear =
      (currentYear - 2) + "_" + String.valueOf(currentYear - 1).substring(2);
    return (
      "https://academia.srmist.edu.in/srm_university/academia-academic-services/page/My_Time_Table_" +
      academicYear
    );
  }

  public UserInfo getUserInfo(String cookie) {
    String timetableUrl = getTimetableUrl();

    String rawHtml = webClient
      .get()
      .uri(timetableUrl)
      .header(HttpHeaders.COOKIE, cookie)
      .header(
        HttpHeaders.USER_AGENT,
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/108.0.0.0 Safari/537.36"
      )
      .retrieve()
      .bodyToMono(String.class)
      .block();

    if (rawHtml == null) {
      throw new IllegalStateException(
        "Did not receive a response from the user info page."
      );
    }

    String cleanHtml = decodeHtml(extractEncodedContent(rawHtml));
    Document doc = Jsoup.parse(cleanHtml);

    UserInfo userInfo = new UserInfo();

    userInfo.setRegNumber(getTextFromTableRow(doc, "Registration Number:"));
    userInfo.setName(getTextFromTableRow(doc, "Name:"));
    userInfo.setProgram(getTextFromTableRow(doc, "Program:"));
    userInfo.setDepartment(
      getTextFromTableRow(doc, "Department:").split("-")[0].trim()
    );
    userInfo.setSection(
      getTextFromTableRow(doc, "Department:").replaceAll(".*Section|\\(|\\)", "").trim()
    );
    userInfo.setSemester(getTextFromTableRow(doc, "Semester:"));
    userInfo.setBatch(getTextFromTableRow(doc, "Batch:"));

    userInfo.setMobile(
      getTextFromTableRow(doc, "Mobile:") != null
        ? getTextFromTableRow(doc, "Mobile:")
        : "N/A"
    );

    return userInfo;
  }

  private String getTextFromTableRow(Document doc, String label) {
    Element cell = doc.selectFirst("td:contains(" + label + ")");
    if (cell != null && cell.nextElementSibling() != null) {
      return cell.nextElementSibling().text().trim();
    }
    return "";
  }

  public TotalAttendance getTotalAttendancePercentage(String cookie) {
    List<AttendanceDetail> attendanceDetails = getAttendance(cookie);
    int totalConducted = 0;
    int totalAbsent = 0;
    for (AttendanceDetail detail : attendanceDetails) {
      totalConducted += detail.getCourseConducted();
      totalAbsent += detail.getCourseAbsent();
    }
    double totalAttendancePercentage = 0;
    if (totalConducted > 0) {
      totalAttendancePercentage =
        ((double) (totalConducted - totalAbsent) / totalConducted) * 100;
    }
    return new TotalAttendance(totalAttendancePercentage);
  }
}