package com.redhat.exhort;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.function.BiPredicate;
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
      Map<ImageRefInput, AnalysisReport> imageRefAnalysisReportMapWithInputs =
          imageRefAnalysisReportMap.entrySet().stream()
              .collect(
                  Collectors.toMap(
                      entry ->
                          (ImageRefInput)
                              imageRefs.stream()
                                  .filter(member -> matchImageRefInput(entry, member))
                                  .findFirst()
                                  .get(),
                      entry -> entry.getValue()));
      Map<String, AnalysisReport> imageRefAnalysisReportMapTransformedNoDigestInInput =
          transformToMapWithPredicate(
              args,
              imageRefAnalysisReportMapWithInputs,
              (arguments, digest) -> !digestIsInArgs((String) digest, (String) arguments));
      Map<String, AnalysisReport> imageRefAnalysisReportMapTransformed =
          transformToMapWithPredicate(
              args,
              imageRefAnalysisReportMapWithInputs,
              (arguments, digest) -> digestIsInArgs((String) digest, (String) arguments));
      imageRefAnalysisReportMapTransformed.putAll(
          imageRefAnalysisReportMapTransformedNoDigestInInput);
      String result = om.writeValueAsString(imageRefAnalysisReportMapTransformed);
      System.out.println(new String(result));

    } else {
      byte[] htmlBytes = exhortApi.imageAnalysisHtml(imageRefs).get();
      System.out.println(new String(htmlBytes));
    }
  }

  private static boolean matchImageRefInput(
      Map.Entry<ImageRef, AnalysisReport> entry, ImageRef theMember) {
    ImageRefInput member = (ImageRefInput) theMember;
    return (member
                .getImage()
                .getNameWithoutTag()
                .equals(entry.getKey().getImage().getNameWithoutTag())
            && Objects.nonNull(member.getImage().getTag())
            && member
                .getImage()
                .getTag()
                .equals(Objects.requireNonNullElse(entry.getKey().getImage().getTag(), "")))
        || (member
                .getImage()
                .getNameWithoutTag()
                .equals(entry.getKey().getImage().getNameWithoutTag())
            && member.getOriginalArgument().contains(entry.getKey().getImage().getDigest()));
  }

  private static Map<String, AnalysisReport> transformToMapWithPredicate(
      String[] args,
      Map<ImageRefInput, AnalysisReport> imageRefAnalysisReportMap,
      BiPredicate argsContainsDigest) {
    return imageRefAnalysisReportMap.entrySet().stream()
        .filter(
            entry ->
                argsContainsDigest.test(
                    entry.getKey().getOriginalArgument(), entry.getKey().getImage().getDigest()))
        .collect(
            Collectors.toMap(
                imageRefAnalysisReportEntry -> transformString(imageRefAnalysisReportEntry),
                imageRefAnalysisReportEntry -> imageRefAnalysisReportEntry.getValue()));
  }

  private static String transformString(
      Map.Entry<ImageRefInput, AnalysisReport> imageRefAnalysisReportEntry) {
    if (imageRefAnalysisReportEntry.getKey().getOriginalArgument().contains("@sha256:")) {
      return imageRefAnalysisReportEntry.getKey().getImage().toString();
    } else {
      return imageRefAnalysisReportEntry
          .getKey()
          .getImage()
          .toString()
          .replaceAll("@sha256:[0-9a-f]{32,}$", "");
    }
  }

  private static boolean digestIsInArgs(String digest, String arg) {
    return (arg.contains(digest));
  }

  private static Set<ImageRef> parseArguments(String[] args) {
    Set<ImageRef> result = new HashSet<>();
    for (int i = 1; i < args.length; i++) {
      String[] parts = args[i].split(DELIMITER);
      ImageRef imageRef;
      if (parts[0].trim().equals(args[i])) {
        imageRef = new ImageRefInput(args[i], args[i], null);
      } else {
        if (parts.length == 2) {
          imageRef = new ImageRefInput(args[i], parts[0], parts[1]);
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
