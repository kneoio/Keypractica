package io.kneo.core.util;

public class RuntimeUtil {

    public static int countMaxPage(long colCount, int pageSize) {
        float mp = (float) colCount / (float) pageSize;
        float d = Math.round(mp);

        int maxPage = (int) d;
        if (mp > d) {
            maxPage++;
        }
        if (maxPage < 1) {
            maxPage = 1;
        }
        return maxPage;
    }

    public static int calcStartEntry(int pageNum, int pageSize) {
        int pageNumMinusOne = pageNum;
        pageNumMinusOne--;
        if (pageNumMinusOne < 0) {
            return 0;
        }
        return pageNumMinusOne * pageSize;
    }


}
