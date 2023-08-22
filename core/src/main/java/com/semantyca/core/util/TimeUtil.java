package com.semantyca.core.util;


import com.semantyca.core.localization.LanguageCode;
import com.semantyca.core.server.EnvConst;
import com.semantyca.core.server.Environment;
import org.jboss.logging.Logger;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TimeUtil {

    private static final Logger LOGGER = Logger.getLogger(TimeUtil.class);

    public static final SimpleDateFormat DATE_TIME_FORMAT = new SimpleDateFormat(EnvConst.DEFAULT_DATETIME_FORMAT);
    public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat(EnvConst.DEFAULT_DATE_FORMAT);
    public static final SimpleDateFormat DATE_FORMAT_PG = new SimpleDateFormat("MM-dd-yyyy");
    public static final SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("kk:mm:ss");
    public static final SimpleDateFormat DATE_TIME_FORMAT_FOR_FILE_NAMING = new SimpleDateFormat("dd-MM-yyyy_kk-mm");

    private static final int NUM_PATTERNS = 8;
    private static Pattern[] datePatterns = new Pattern[NUM_PATTERNS];
    private static String[] dateFormats = new String[NUM_PATTERNS];

    public static String timeConvert(int time) {
        return time / 24 / 60 + ":" + time / 60 % 24 + ':' + time % 60;
    }

    public static Date stringToDateTime(String val) {
        try {
            return DATE_TIME_FORMAT.parse(val);
        } catch (ParseException e) {
            try {
                return DATE_FORMAT.parse(val);
            } catch (ParseException e1) {
                LOGGER.error("Unable convert text to date \"" + val + "\" (supposed format "
                        + DATE_TIME_FORMAT + " or " + DATE_FORMAT + ")");
                return null;
            }
        }
    }

    public static Date convertTextToDate(String dateInString) {
        try {
            return DATE_FORMAT.parse(dateInString);
        } catch (ParseException e) {
            LOGGER.error(e);
            return null;
        }
    }

    public static String dateToStringSilently(Date date) {
        try {
            return DATE_FORMAT.format(date);
        } catch (Exception e) {
            return "";
        }
    }

    public static String dateToPGString(Date date) {
        return DATE_FORMAT_PG.format(date);
    }

    public static String timeToStringSilently(Date date) {
        try {
            return TIME_FORMAT.format(date);
        } catch (Exception e) {
            return "";
        }
    }

    public static String dateTimeToStringSilently(Date date) {
        try {
            return DATE_TIME_FORMAT.format(date);
        } catch (Exception e) {
            return "";
        }
    }

    public static String dateTimeToFileNameSilently(Date date) {
        try {
            return DATE_TIME_FORMAT_FOR_FILE_NAMING.format(date);
        } catch (Exception e) {
            return "";
        }
    }

    public static Date stringToDateWithPassion(String dateParam) {
        boolean flag = true;
        int i = 0;
        Matcher matcher;
        SimpleDateFormat sdf = null;

        while ((i < NUM_PATTERNS) && flag) {
            matcher = datePatterns[i].matcher(dateParam);
            if (matcher.matches()) {
                sdf = new SimpleDateFormat(dateFormats[i]);
                flag = false;
            }
            i++;
        }

        try {
            return sdf.parse(dateParam);
        } catch (ParseException e) {
            return null;
        } catch (NullPointerException e) {
            return null;
        } catch (Exception e) {
            LOGGER.error(e);
            return null;
        }

    }

    public static String getTimeDiffInMilSec(long start_time) {
        long time = System.currentTimeMillis() - start_time;
        int sec = (int) time / 1000;
        int msec = (int) time % 1000;
        return Integer.toString(sec) + "." + Integer.toString(msec);
    }

    public static float getTimeDiffInSec(long start_time) {
        long time = System.currentTimeMillis() - start_time;
        float sec = (int) time / 1000;
        float msec = (int) time % 1000;
        return sec + msec;
    }

    public static int getDiffBetween(Date startTime, Date endTime, TimeUnit timeUnit) {
        final int MILLI_TO_HOUR = 1000 * 60 * 60;
        return -(int) (startTime.getTime() - endTime.getTime()) / MILLI_TO_HOUR;
    }

    public static Date getRndDate() {
        GregorianCalendar gc = new GregorianCalendar();
        int year = NumberUtil.getRandomNumber(1900, 2017);
        gc.set(GregorianCalendar.YEAR, year);
        int dayOfYear = NumberUtil.getRandomNumber(1, gc.getActualMaximum(GregorianCalendar.DAY_OF_YEAR));
        gc.set(GregorianCalendar.DAY_OF_YEAR, dayOfYear);
        return gc.getTime();

    }

    public static Date getRndDateBetween(Date from, Date to) {
        long random = ThreadLocalRandom.current().nextLong(from.getTime(), to.getTime());
        return new Date(random);
    }


    public static void init() {

    }

    public static List<String> measureDates(List<Date> dates) {
        List result = new ArrayList();

        int size = dates.size();
        LocalDateTime absoluteStart = LocalDateTime.ofInstant(dates.get(0).toInstant(), ZoneId.systemDefault());
        LocalDateTime absoluteEnd = LocalDateTime.ofInstant(dates.get(size - 1).toInstant(), ZoneId.systemDefault());
        int maxDiffMinutes = (int) (long) absoluteStart.until(absoluteEnd, ChronoUnit.MINUTES);
        int fraction = maxDiffMinutes / 100;

        for (int i = 0; i < size; i++) {
            boolean isAllowToCalc = false;
            int percent = -1;
            Date firstDate = dates.get(i);
            Date secondDate = null;
            try {
                secondDate = dates.get(i + 1);
                isAllowToCalc = true;
            } catch (IndexOutOfBoundsException e) {
                secondDate = new Date();
            }

            LocalDateTime date1 = LocalDateTime.ofInstant(firstDate.toInstant(), ZoneId.systemDefault());
            LocalDateTime date2 = LocalDateTime.ofInstant(secondDate.toInstant(), ZoneId.systemDefault());

            if (isAllowToCalc) {
                percent = (int) (long) absoluteStart.until(date2, ChronoUnit.MINUTES) / fraction;
            }

            LocalDateTime tempDateTime = LocalDateTime.from(date1);

            long diffInDays = tempDateTime.until(date2, ChronoUnit.DAYS);
            tempDateTime = tempDateTime.plusDays(diffInDays);

            long diffInHours = tempDateTime.until(date2, ChronoUnit.HOURS);
            tempDateTime = tempDateTime.plusHours(diffInHours);

            long diffInMinutes = tempDateTime.until(date2, ChronoUnit.MINUTES);

            tempDateTime = tempDateTime.plusMinutes(diffInMinutes);


            result.add(dateTimeToStringSilently(firstDate) + " - " + dateTimeToStringSilently(secondDate) +
                    "\n" + diffInDays + " days, " + diffInHours + " hours, " + diffInMinutes + " minutes, " + percent + "%");

        }

        return result;

    }

    public static String getTimeTextByCurrent(Date from, LanguageCode lang) {
        LocalDateTime date1 = LocalDateTime.ofInstant(from.toInstant(), ZoneId.systemDefault());
        LocalDateTime date2 = LocalDateTime.ofInstant(new Date().toInstant(), ZoneId.systemDefault());

        LocalDateTime tempDateTime = LocalDateTime.from(date1);

        long diffInDays = tempDateTime.until(date2, ChronoUnit.DAYS);
        tempDateTime = tempDateTime.plusDays(diffInDays);

        long diffInHours = tempDateTime.until(date2, ChronoUnit.HOURS);
        tempDateTime = tempDateTime.plusHours(diffInHours);

        long diffInMinutes = tempDateTime.until(date2, ChronoUnit.MINUTES);

        return getTimeText(lang, diffInDays, diffInHours, diffInMinutes, true);
    }

    public static String getTimeText(LanguageCode lang, long diffInDays, long diffInHours, long diffInMinutes, boolean isLastStage) {

        String timeText = "";
        if (diffInDays >= 1) {
            if (diffInDays == 1) {
                timeText = diffInDays + Environment.vocabulary.getWord("count_days.0",
                        lang).replace("{{count}}", "");
            } else if (diffInDays == 2 || diffInDays == 3 || diffInDays == 4) {
                timeText = diffInDays + Environment.vocabulary.getWord("count_days.1",
                        lang).replace("{{count}}", "");
            } else {
                timeText = diffInDays + Environment.vocabulary.getWord("count_days.2",
                        lang).replace("{{count}}", "");
            }

        }

        if (diffInHours > 1) {
            timeText += " ";
            if (diffInHours == 1) {
                timeText += diffInHours + " " + Environment.vocabulary.getWord("hours.0", lang);
            } else if (diffInHours == 2 || diffInHours == 3 || diffInHours == 4) {
                timeText += diffInHours + " " + Environment.vocabulary.getWord("hours.1", lang);
            } else {
                timeText += diffInHours + " " + Environment.vocabulary.getWord("hours.2", lang);
            }
        }

        if (isLastStage) {
            timeText = Environment.vocabulary.getWord("more_than", lang) + " " + timeText;
        } else {
            if (diffInMinutes < 1) {
                timeText += Environment.vocabulary.getWord("less_than_min", lang);
            } else if (diffInMinutes == 1) {
                timeText += " " + diffInMinutes + " " + Environment.vocabulary.getWord("minutes.0", lang);
            } else if (diffInMinutes == 2 || diffInMinutes == 3 || diffInMinutes == 4) {
                timeText += " " + diffInMinutes + " " + Environment.vocabulary.getWord("minutes.1", lang);
            } else {
                timeText += " " + diffInMinutes + " " + Environment.vocabulary.getWord("minutes.2", lang);
            }
        }

        return timeText;
    }

    static {
        datePatterns[0] = Pattern.compile("[0-9]{2}-[0-9]{2}-[0-9]{4}[ ]{1,}[0-9]{2}:[0-9]{2}:[0-9]{2}");
        dateFormats[0] = "dd-MM-yyyy HH:mm:ss";

        datePatterns[1] = Pattern.compile("[0-9]{2}-[0-9]{2}-[0-9]{4}");
        dateFormats[1] = "dd-MM-yyyy";

        datePatterns[2] = Pattern.compile("[0-9]{2}.[0-9]{2}.[0-9]{4}[ ]{1,}[0-9]{2}:[0-9]{2}:[0-9]{2}");
        dateFormats[2] = "dd.MM.yyyy HH:mm:ss";

        datePatterns[3] = Pattern.compile("[0-9]{2}.[0-9]{2}.[0-9]{4}[ ]{1,}[0-9]{2}:[0-9]{2}");
        dateFormats[3] = "dd.MM.yyyy HH:mm";

        datePatterns[4] = Pattern.compile("[0-9]{2}.[0-9]{2}.[0-9]{4}");
        dateFormats[4] = "dd.MM.yyyy";

        datePatterns[5] = Pattern.compile("[0-9]{4}-[0-9]{2}-[0-9]{2}[ ]{1,}[0-9]{2}:[0-9]{2}:[0-9]{2}");
        dateFormats[5] = "yyyy-MM-dd HH:mm:ss";

        datePatterns[6] = Pattern.compile("[0-9]{4}-[0-9]{2}-[0-9]{2}");
        dateFormats[6] = "yyyy-MM-dd";

        datePatterns[7] = Pattern.compile("(0[1-9]|10|11|12)20[0-9]{2}$");
        dateFormats[7] = "MMyyyy";
    }


    public static void main(String[] args) {
        init();
        List<Date> exampleDates = new ArrayList();
        exampleDates.add(stringToDateWithPassion("01.09.2017"));
        exampleDates.add(stringToDateWithPassion("05.09.2017 24:56"));
        exampleDates.add(stringToDateWithPassion("07.09.2017 09:45"));
        exampleDates.add(stringToDateWithPassion("10.09.2017 10:12"));
        exampleDates.add(stringToDateWithPassion("16.09.2017"));
        exampleDates.add(stringToDateWithPassion("122007"));
        exampleDates.add(stringToDateWithPassion("122015"));

        List dates = measureDates(exampleDates);

        for (int i = 0; i < dates.size(); i++) {
            System.out.println(i + ">" + dates.get(i));
        }


    }

}
