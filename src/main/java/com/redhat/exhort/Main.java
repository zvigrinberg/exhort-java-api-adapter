package com.redhat.exhort;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.redhat.exhort.api.AnalysisReport;
import com.redhat.exhort.image.ImageRef;
import com.redhat.exhort.impl.ExhortApi;

public class Main {

  public static final String DELIMITER = "\\^\\^";

  public static void main(String[] args)
      throws IOException, ExecutionException, InterruptedException {

    //    System.setProperty(
    //        "DEV_EXHORT_BACKEND_URL", "http://latest-exhort.apps.sssc-cl01.appeng.rhecoeng.com");
    //    System.setProperty("EXHORT_DEV_MODE", "true");
    String theReportType = args[0];
    ReportType reportType = ReportType.valueOf(theReportType.toUpperCase());
    ExhortApi exhortApi = new ExhortApi();
    Set<ImageRef> imageRefs = parseArguments(args);
    ObjectMapper om = new ObjectMapper();
    if (reportType.equals(ReportType.JSON)) {
      Map<ImageRef, AnalysisReport> imageRefAnalysisReportMap =
          exhortApi.imageAnalysis(imageRefs).get();
      Map<String, AnalysisReport> imageRefAnalysisReportMapTransformed =
          imageRefAnalysisReportMap.entrySet().stream()
              .collect(
                  Collectors.toMap(
                      imageRefAnalysisReportEntry ->
                          imageRefAnalysisReportEntry.getKey().getImage().toString(),
                      imageRefAnalysisReportEntry -> imageRefAnalysisReportEntry.getValue()));
      String result = om.writeValueAsString(imageRefAnalysisReportMapTransformed);
      System.out.println(new String(result));

    } else {
      byte[] htmlBytes = exhortApi.imageAnalysisHtml(imageRefs).get();
      System.out.println(new String(htmlBytes));
    }
  }

  private static Set<ImageRef> parseArguments(String[] args) {
    Set<ImageRef> result = new HashSet<>();
    for (int i = 1; i < args.length; i++) {
      String[] parts = args[i].split(DELIMITER);
      ImageRef imageRef;
      if (parts[0].trim().equals(args[i])) {
        imageRef = new ImageRef(args[i], null);
      } else {
        if (parts.length == 2) {
          imageRef = new ImageRef(parts[0], parts[1]);
        } else {
          throw new IllegalArgumentException(
              String.format(
                  "Command line arguments [%s] contains an illegal argument --> %s , format should"
                      + " be either image^^architecture or image",
                  Arrays.stream(args).collect(Collectors.joining(" ")), args[i]));
        }
      }
      result.add(imageRef);
    }
    return result;
  }
}
