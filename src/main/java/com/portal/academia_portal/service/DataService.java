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
import org.springframework.web.reactive.function.client.WebClientResponseException;

import com.portal.academia_portal.dto.AttendanceDetail;
import com.portal.academia_portal.dto.CalendarEvent;
import com.portal.academia_portal.dto.CourseInfo;
import com.portal.academia_portal.dto.CourseSlot;
import com.portal.academia_portal.dto.DayEvent;
import com.portal.academia_portal.dto.DaySchedule;
import com.portal.academia_portal.dto.Mark;
import com.portal.academia_portal.dto.MarkDetail;
import com.portal.academia_portal.dto.Month;
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
    String attendanceUrl = "https://academia.srmist.edu.in/srm_university/academia-academic-services/page/My_Attendance";
    String rawHtml = webClient.get().uri(attendanceUrl).header(HttpHeaders.COOKIE, cookie).header(HttpHeaders.USER_AGENT, "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/108.0.0.0 Safari/537.36").retrieve().bodyToMono(String.class).block();

    if (rawHtml == null) {
        throw new IllegalStateException("Did not receive a response from the attendance page.");
    }

    String encodedHtml = extractEncodedContent(rawHtml);
    if (encodedHtml.isEmpty()) {
        throw new IllegalStateException("Could not extract encoded HTML from the response.");
    }

    String cleanHtml = decodeHtml(encodedHtml);
    Document doc = Jsoup.parse(cleanHtml);
    List<MarkDetail> marksList = new ArrayList<>();
    
    // Find the paragraph that precedes the marks table
    Element p = doc.select("p:contains(Internal Marks Detail)").first();
    Element marksTable = null;
    if (p != null) {
        marksTable = p.nextElementSibling();
    }
    
    if (marksTable == null || !marksTable.tagName().equals("table")) {
        return marksList;
    }

    Elements rows = marksTable.select("tr");

    for (int i = 1; i < rows.size(); i++) { // Start from 1 to skip header row
        Element row = rows.get(i);
        Elements cells = row.select("td");
        if (cells.size() < 3) continue;

        String course = cells.get(0).text().trim();
        String category = cells.get(1).text().trim();
        Element marksCell = cells.get(2);
        
        if (course.isEmpty() || category.isEmpty() || marksCell.select("table").isEmpty()) {
            continue;
        }

        MarkDetail markDetail = new MarkDetail();
        markDetail.setCourse(course);
        markDetail.setCategory(category);

        List<Mark> individualMarks = new ArrayList<>();
        Elements markTds = marksCell.select("table td");
        
        for (Element markTd : markTds) {
            String strongText = markTd.select("strong").text().trim();
            if (strongText.isEmpty() || !strongText.contains("/")) continue;

            String[] parts = strongText.split("/");
            if (parts.length < 2) continue;

            String examName = parts[0].trim();
            String maxMarkStr = parts[1].trim();
            
            // Clone the td element to avoid modifying the original
            Element temp = markTd.clone();
            temp.select("strong").remove();
            String obtainedText = temp.text().trim();


            if (!obtainedText.isEmpty() && !maxMarkStr.isEmpty()) {
                try {
                    double obtainedMark = Double.parseDouble(obtainedText);
                    double maxMark = Double.parseDouble(maxMarkStr);

                    Mark mark = new Mark();
                    mark.setExam(examName);
                    mark.setMaxMark(maxMark);
                    mark.setObtained(obtainedMark);
                    individualMarks.add(mark);
                } catch (NumberFormatException e) {
                    System.err.println("Could not parse mark: " + obtainedText + " or " + maxMarkStr);
                }
            }
        }
        markDetail.setMarks(individualMarks);
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
    System.out.println("Batch Text: " + batchText);
    int batch;
    try {
        String batchNumberStr;
        String[] batchParts = batchText.trim().split("/");
        if (batchParts.length > 1) {

            batchNumberStr = batchParts[1];
        } else {

            batchNumberStr = batchParts[0];
        }
        batch = Integer.parseInt(batchNumberStr);
    } catch (NumberFormatException e) {
        logger.error("Could not parse batch number from text: '" + batchText + "'. Defaulting to batch 1.", e);

        batch = 1;
    }


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
        logger.warn("No schedule definition found for batch: " + batch + ". Trying with batch 1.");
        scheduleForBatch = TimetableData.BATCH_SLOTS.get(1);
        if(scheduleForBatch == null) {
             throw new IllegalStateException(
                "No schedule definition found for batch: " + batch
            );
        }
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
    float totalPercentage = 0;
    int count = 0;
    for (AttendanceDetail detail : attendanceDetails) {
      totalPercentage += Float.parseFloat(detail.getCourseAttendance().replace("%", ""));
      count++;
    }
    float totalAttendancePercentage = count == 0 ? 0 : totalPercentage / count;
    return new TotalAttendance(totalAttendancePercentage);
  }


private String getCalendarUrl() {
    LocalDate currentDate = LocalDate.now();
    int currentYear = currentDate.getYear();
    int month = currentDate.getMonthValue();
    String academicYearString;
    String semesterType;

    if (month >= 1 && month <= 6) {
        semesterType = "EVEN";
        academicYearString = (currentYear - 1) + "_" + String.valueOf(currentYear).substring(2);
    } else {
        semesterType = "ODD";
        academicYearString = currentYear + "_" + String.valueOf(currentYear + 1).substring(2);
    }

    return "https://academia.srmist.edu.in/srm_university/academia-academic-services/page/Academic_Planner_" + academicYearString + "_" + semesterType;
}

public List<Month> getCalendar(String cookie) {
    String calendarUrl = getCalendarUrl();
    
    String rawHtml = "";
    try {
        rawHtml = webClient.get().uri(calendarUrl)
            .header(HttpHeaders.COOKIE, cookie)
            .header(HttpHeaders.USER_AGENT, "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/108.0.0.0 Safari/537.36")
            .retrieve()
            .bodyToMono(String.class)
            .block();
    } catch (WebClientResponseException.NotFound ex) {
        logger.error("Calendar page not found at " + calendarUrl);
        return new ArrayList<>();
    }

    if (rawHtml == null || rawHtml.isEmpty()) {
        throw new IllegalStateException("Did not receive a response from the calendar page: " + calendarUrl);
    }

    Document doc = Jsoup.parse(rawHtml);
    Element zmlDiv = doc.selectFirst("div.zc-pb-embed-placeholder-content");
    if (zmlDiv == null) {
        logger.warn("Could not find the 'zmlvalue' div on the calendar page.");
        return new ArrayList<>();
    }

    String zmlValue = zmlDiv.attr("zmlvalue");
    if (zmlValue.isEmpty()) {
        logger.warn("'zmlvalue' attribute is empty.");
        return new ArrayList<>();
    }

    Document innerDoc = Jsoup.parse(zmlValue);
    Element mainTable = innerDoc.selectFirst("table[bgcolor='#FAFCFE']");
    if (mainTable == null) {
        logger.warn("Could not find the main calendar table inside 'zmlvalue'.");
        return new ArrayList<>();
    }

    List<Month> months = new ArrayList<>();
    Element headerRow = mainTable.selectFirst("tr");
    if(headerRow == null) return new ArrayList<>();
    
    Elements ths = headerRow.select("th");
    for (int i = 0; ; i++) {
        int monthNameThIndex = i * 5 + 2;
        if (monthNameThIndex >= ths.size()) break;
        Element monthTh = ths.get(monthNameThIndex);
        if (monthTh == null) break;
        String monthName = monthTh.selectFirst("strong").text().trim();
        if (!monthName.isEmpty()) {
            months.add(new Month(monthName, new ArrayList<>()));
        } else {
            break;
        }
    }

    Elements dataRows = mainTable.select("tr:gt(0)");
    for (Element row : dataRows) {
        Elements tds = row.select("td");
        for (int monthIndex = 0; monthIndex < months.size(); monthIndex++) {
            Month currentMonth = months.get(monthIndex);
            int offset = monthIndex * 5;
            if (offset + 3 >= tds.size()) continue;

            String date = tds.get(offset).text().trim();
            if (date.isEmpty()) continue;

            String day = tds.get(offset + 1).text().trim();
            String event = tds.get(offset + 2).selectFirst("strong").text().trim();
            String dayOrder = tds.get(offset + 3).text().trim();

            currentMonth.getDays().add(new DayEvent(date, day, event, dayOrder));
        }
    }

    return months;
}
}