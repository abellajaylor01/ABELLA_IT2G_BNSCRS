package com.scc.ui;

import com.scc.dao.ActivityLogDao;
import com.scc.model.ActivityLog;

import java.util.List;

public class ActivityLogViewer {
    private final ActivityLogDao logDao = new ActivityLogDao();

    public void displayLogs() {
        List<ActivityLog> logs = logDao.getAllLogs();
        System.out.println("\n=== SYSTEM USER ACTIVITY & RECORD AUDIT LOG ===");
        for (ActivityLog log : logs) {
            System.out.printf("[%s] Log#%d | User#%d | %s | %s | IP: %s%n",
                    log.timestamp(), log.logId(), log.userId(),
                    log.actionPerformed(), log.eventDetails(), log.ipAddress());
        }
    }
}