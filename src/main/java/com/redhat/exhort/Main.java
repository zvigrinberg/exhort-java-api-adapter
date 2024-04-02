package com.redhat.exhort;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonArrayFormatVisitor;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeCreator;
import com.redhat.exhort.api.AnalysisReport;
import com.redhat.exhort.image.Image;
import com.redhat.exhort.image.ImageRef;
import com.redhat.exhort.impl.ExhortApi;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

public class Main {

    public static final String DELIMITER = "\\^\\^";

    public static void main(String[] args) throws IOException, ExecutionException, InterruptedException {

        String theReportType = args[0];
        ReportType reportType = ReportType.valueOf(theReportType.toUpperCase());
        ExhortApi exhortApi = new ExhortApi();
        Set<ImageRef> imageRefs = parseArguments(args);
        ObjectMapper om = new ObjectMapper();
        if (reportType.equals(ReportType.JSON)) {
            Map<ImageRef, AnalysisReport> imageRefAnalysisReportMap = exhortApi.imageAnalysis(imageRefs).get();
            String result = om.writeValueAsString(imageRefAnalysisReportMap);
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
                imageRef = new ImageRef(args[i],null);
            }
            else
            {
               if(parts.length == 2)
               {
                   imageRef = new ImageRef(parts[0],parts[1]);
               }
               else {
                   throw new IllegalArgumentException(String.format("Command line arguments [%s] contains an illegal argument --> %s , format should be either image^^architecture or image", Arrays.stream(args).collect(Collectors.joining(" ")),args[i]));
               }
            }
            result.add(imageRef);
        }
        return result;
    }


}