package org.justforfun.db;

import java.util.Map;

public class TempScoreboardData {
    private String title;
    private Map<Integer, String> lines;

    public TempScoreboardData(String title, Map<Integer, String> lines) {
        this.title = title;
        this.lines = lines;
    }

    public String getTitle() {
        return title;
    }

    public Map<Integer, String> getLines() {
        return lines;
    }
}
