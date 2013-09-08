package net.mindengine.galen.tests.runner;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import net.mindengine.galen.components.report.ReportingListenerTestUtils;
import net.mindengine.galen.reports.ConsoleReportingListener;
import net.mindengine.galen.reports.HtmlReportingListener;
import net.mindengine.galen.reports.TestngReportingListener;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.testng.annotations.Test;

import com.google.common.io.Files;

public class ReportingListenerTest {

    
    @Test public void shouldReport_inTestNgFormat_successfully() throws IOException {
        String reportPath = Files.createTempDir().getAbsolutePath() + "/report.xml";
        
        String expectedDate = new SimpleDateFormat("yyyy-MM-dd").format(new Date()) + "T00:00:00Z";
        
        TestngReportingListener listener = new TestngReportingListener(reportPath);
        ReportingListenerTestUtils.performSampleReporting(listener, listener);
        
        String expectedXml = IOUtils.toString(getClass().getResourceAsStream("/expected-reports/testng-report.xml"));
        
        listener.done();
        
        String realXml = FileUtils.readFileToString(new File(reportPath));
        
        assertThat(realXml.replaceAll("T([0-9]{2}:){2}[0-9]{2}Z", "T00:00:00Z"), 
                is(expectedXml.replace("{expected-date}", expectedDate)));
    }
    
    
    @Test public void shouldReport_inHtmlFormat_successfully() throws IOException {
        String reportDirPath = Files.createTempDir().getAbsolutePath();
        
        HtmlReportingListener listener = new HtmlReportingListener(reportDirPath + "/report.html");
        ReportingListenerTestUtils.performSampleReporting(listener, listener);
        
        String expectedHtml = IOUtils.toString(getClass().getResourceAsStream("/expected-reports/html-report-suffix.html"));
        
        listener.done();
        
        String realHtml = FileUtils.readFileToString(new File(reportDirPath + "/report.html"));
        assertThat(bodyPart(realHtml), is(expectedHtml));
        assertThat("Should place screenshot 1 in same folder", new File(reportDirPath + "/screenshot-1.png").exists(), is(true));
        assertThat("Should place screenshot 2 in same folder", new File(reportDirPath + "/screenshot-2.png").exists(), is(true));
    }

    @Test public void shouldReport_toConsole_successfully() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(baos);
        ConsoleReportingListener listener = new ConsoleReportingListener(ps, ps);
        ReportingListenerTestUtils.performSampleReporting(listener, listener);
        
        String expectedText = IOUtils.toString(getClass().getResourceAsStream("/expected-reports/console.txt"));
        assertThat(baos.toString("UTF-8"), is(expectedText));
    }

    private String bodyPart(String plainHtml) {
        int id1 = plainHtml.indexOf("<body>");
        int id2 = plainHtml.indexOf("</body>");
        
        return plainHtml.substring(id1, id2 + 7);
    }
    
    
}