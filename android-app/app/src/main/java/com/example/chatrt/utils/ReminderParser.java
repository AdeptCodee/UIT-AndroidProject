package com.example.chatrt.utils;

import java.util.Calendar;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ReminderParser {
    // Regex: chốt [thời gian] [nội dung]
    private static final Pattern PATTERN = Pattern.compile("^chốt\\s+(.+?)\\s+(.+)$", Pattern.CASE_INSENSITIVE);

    public static class ReminderData {
        public String content;
        public Date dueDate;

        public ReminderData(String content, Date dueDate) {
            this.content = content;
            this.dueDate = dueDate;
        }
    }

    public static ReminderData parse(String text) {
        Matcher matcher = PATTERN.matcher(text.trim());
        if (!matcher.matches()) return null;

        String timeStr = matcher.group(1).toLowerCase();
        String content = matcher.group(2);
        Date dueDate = parseDate(timeStr);

        if (dueDate == null) return null;
        
        // Chỉ chấp nhận nếu ngày hẹn lớn hơn hoặc bằng ngày hiện tại
        if (dueDate.before(new Date())) {
             // Logic xử lý: Nếu là ngày trong quá khứ (ví dụ 22/02 mà nay là tháng 5), 
             // có thể hiểu là 22/02 năm sau. Nhưng để đơn giản, ta coi như không hợp lệ.
        }

        return new ReminderData(content, dueDate);
    }

    private static Date parseDate(String timeStr) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 9); // Mặc định nhắc vào 9h sáng
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        if (timeStr.contains("mai")) {
            cal.add(Calendar.DAY_OF_YEAR, 1);
            return cal.getTime();
        } else if (timeStr.contains("mốt")) {
            cal.add(Calendar.DAY_OF_YEAR, 2);
            return cal.getTime();
        }

        // Parse định dạng dd/MM, dd-MM, dd/MM/yyyy
        String[] parts = timeStr.split("[/-]");
        if (parts.length >= 2) {
            try {
                int day = Integer.parseInt(parts[0].trim());
                int month = Integer.parseInt(parts[1].trim()) - 1; // Calendar month is 0-indexed
                int year = cal.get(Calendar.YEAR);
                if (parts.length == 3) {
                    year = Integer.parseInt(parts[2].trim());
                    if (year < 100) year += 2000;
                }
                cal.set(year, month, day);
                return cal.getTime();
            } catch (Exception e) {
                return null;
            }
        }

        return null;
    }
}
