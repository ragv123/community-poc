package com.ttn.community.journal.poc;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.TimeZone;

import org.apache.sling.api.resource.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.day.cq.wcm.api.Page;
import com.ttn.community.journal.util.CustomJournalUtils;

public class CustomJournalArchive
{
  private static final Logger LOG = LoggerFactory.getLogger(CustomJournalArchive.class);
  private final DateFormat titleFormat;
  private final List<Period> periods;
  private final Resource journalResource;
  private final int max;
  
  public CustomJournalArchive(Resource j, int m, DateFormat df)
  {
    this.journalResource = j;
    this.max = m;
    this.titleFormat = (df != null ? df : new SimpleDateFormat("MMMMM yyyy"));
    this.periods = new ArrayList();
  }
  
  public List<Period> getPeriods()
  {
    if (this.periods.size() == 0)
    {
      Iterator<Page> years = CustomJournalUtils.findPage(this.journalResource).listChildren();
      while (years.hasNext())
      {
        Page year = (Page)years.next();
        if ((!year.getName().equals("unlisted")) && 
        
          (year.getProperties().get("journalarchive", null) != null))
        {
          Iterator<Page> months = year.listChildren();
          while (months.hasNext())
          {
            Page month = (Page)months.next();
            String period = year.getName() + "/" + month.getName();
            Iterator<Page> blogs = month.listChildren();
            int count = 0;
            while (blogs.hasNext())
            {
              count++;
              blogs.next();
            }
            this.periods.add(getPeriod(period, count));
            if ((this.max > 0) && (this.periods.size() >= this.max)) {
              break;
            }
          }
        }
      }
    }
    Collections.sort(this.periods, new Comparator<CustomJournalArchive.Period>()
    {
      public int compare(CustomJournalArchive.Period p1, CustomJournalArchive.Period p2)
      {
        return -p1.getId().compareTo(p2.getId());
      }

    });
    return this.periods;
  }
  
  private Period getPeriod(String period, int count)
  {
    for (Period p : this.periods) {
      if (period.equals(p.getId())) {
        return p;
      }
    }
    return new Period(period, getPeriodTitle(period, this.titleFormat), "" + count);
  }
  
  private String getFilter(String period)
  {
    Calendar calendar1;
    Calendar calendar2;
    if (period.contains("/"))
    {
      String[] fields = period.split("/");
      int year = Integer.parseInt(fields[0]);
      int month = Integer.parseInt(fields[1]);
      calendar1 = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
      calendar1.set(year, month, 1, 0, 0, 0);
      
      calendar2 = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
      calendar2.set(year, month, calendar1.getActualMaximum(5), calendar1.getActualMaximum(11), calendar1.getActualMaximum(12), calendar1.getActualMaximum(13));
    }
    else
    {
      int year = Integer.parseInt(period);
      calendar1 = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
      calendar1.set(year, 1, 1, 0, 0, 0);
      
      calendar2 = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
      calendar2.set(year, calendar1.getActualMaximum(2), calendar1.getActualMaximum(5), calendar1.getActualMaximum(11), calendar1.getActualMaximum(12), calendar1.getActualMaximum(13));
    }
    String filter;
    try
    {
      SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
      format.setTimeZone(TimeZone.getTimeZone("GMT"));
      filter = "?filter=" + URLEncoder.encode(new StringBuilder().append("added gt '").append(format.format(calendar1.getTime())).append("'").toString(), "utf-8") + "&filter=" + URLEncoder.encode(new StringBuilder().append("added lt '").append(format.format(calendar2.getTime())).append("'").toString(), "utf-8");
    }
    catch (UnsupportedEncodingException e)
    {
      filter = null;
      LOG.error("Unable to create filter for Archive Period.", e);
    }
    return filter;
  }
  
  public static String getPeriodTitle(String period, DateFormat monthDf)
  {
    return getPeriodTitle(period, monthDf, null);
  }
  
  public static String getPeriodTitle(String period, DateFormat monthDf, DateFormat yearDf)
  {
    if (monthDf == null) {
      monthDf = new SimpleDateFormat("MMMMM yyyy");
    }
    try
    {
      if (period.contains("/"))
      {
        String[] fields = period.split("/");
        int year = Integer.parseInt(fields[0]);
        int month = Integer.parseInt(fields[1]);
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, 1);
        return monthDf.format(calendar.getTime());
      }
      if (yearDf == null) {
        yearDf = new SimpleDateFormat("yyyy");
      }
      int year = Integer.parseInt(period);
      Calendar calendar = Calendar.getInstance();
      calendar.set(year, 1, 1);
      return yearDf.format(calendar.getTime());
    }
    catch (NumberFormatException nfe) {}
    return period;
  }
  
  public class Period
  {
    private final String id;
    private final String url;
    private final String title;
    private final String count;
    
    public Period(String period, String t, String count)
    {
      this.id = period;
      this.title = t;
      this.url = getUrl(CustomJournalArchive.this.getFilter(period));
      this.count = count;
    }
    
    public String getUrl()
    {
      return this.url;
    }
    
    public String getTitle()
    {
      return this.title;
    }
    
    public String getId()
    {
      return this.id;
    }
    
    public String getCount()
    {
      return this.count;
    }
    
    private String getUrl(String filters)
    {
      return CustomJournalUtils.findPage(CustomJournalArchive.this.journalResource).getPath() + ".html" + filters;
    }
  }
}

