package com.brandnewdata.mop.poc.operate.util;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class TreePathUtil {
    private StringBuffer treePath;

    public TreePathUtil() {
        this.treePath = new StringBuffer();
    }

    public TreePathUtil(String treePath) {
        this.treePath = new StringBuffer(treePath);
    }

    public TreePathUtil startTreePath(String processInstanceId) {
        this.treePath = new StringBuffer(String.format("%s_%s", TreePathUtil.TreePathEntryType.PI, processInstanceId));
        return this;
    }

    public TreePathUtil appendFlowNode(String newEntry) {
        if (newEntry != null) {
            this.treePath.append(String.format("/%s_%s", TreePathUtil.TreePathEntryType.FN, newEntry));
        }

        return this;
    }

    public TreePathUtil appendFlowNodeInstance(String newEntry) {
        if (newEntry != null) {
            this.treePath.append(String.format("/%s_%s", TreePathUtil.TreePathEntryType.FNI, newEntry));
        }

        return this;
    }

    public TreePathUtil appendProcessInstance(String newEntry) {
        if (newEntry != null) {
            this.treePath.append(String.format("/%s_%s", TreePathUtil.TreePathEntryType.PI, newEntry));
        }

        return this;
    }

    public TreePathUtil appendEntries(String callActivityId, String flowNodeInstanceId, String processInstanceId) {
        return this.appendFlowNode(callActivityId).appendFlowNodeInstance(flowNodeInstanceId).appendProcessInstance(processInstanceId);
    }

    public static String extractFlowNodeInstanceId(String treePath, String currentTreePath) {
        Pattern fniPattern = Pattern.compile(String.format("%s/FN_[^/]*/FNI_(\\d*)/.*", currentTreePath));
        Matcher matcher = fniPattern.matcher(treePath);
        matcher.matches();
        return matcher.group(1);
    }

    public String extractRootInstanceId() {
        Pattern piPattern = Pattern.compile("PI_(\\d*).*");
        Stream<String> stream = Arrays.stream(this.treePath.toString().split("/"));
        Objects.requireNonNull(piPattern);
        Optional<Matcher> firstMatch = stream.map(piPattern::matcher).filter(Matcher::matches).findFirst();
        return firstMatch.isPresent() ? ((Matcher)firstMatch.get()).group(1) : null;
    }

    public List<String> extractProcessInstanceIds() {
        List<String> processInstanceIds = new ArrayList<>();
        Pattern piPattern = Pattern.compile("PI_(\\d*)$");
        Stream<String> stream = Arrays.stream(this.treePath.toString().split("/"));
        Objects.requireNonNull(piPattern);
        stream.map(piPattern::matcher).filter(Matcher::matches).forEach((matcher) -> {
            processInstanceIds.add(matcher.group(1));
        });
        return processInstanceIds;
    }

    public List<String> extractFlowNodeInstanceIds() {
        List<String> flowNodeInstanceIds = new ArrayList<>();
        Pattern fniPattern = Pattern.compile("FNI_(\\d*)$");
        Stream<String> stream = Arrays.stream(this.treePath.toString().split("/"));
        Objects.requireNonNull(fniPattern);
        stream.map(fniPattern::matcher).filter(Matcher::matches).forEach((matcher) -> {
            flowNodeInstanceIds.add(matcher.group(1));
        });
        return flowNodeInstanceIds;
    }

    public String toString() {
        return this.treePath.toString();
    }

    public static enum TreePathEntryType {
        PI,
        FNI,
        FN;

        private TreePathEntryType() {
        }
    }
}
