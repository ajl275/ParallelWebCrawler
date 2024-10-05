package com.udacity.webcrawler;

import com.udacity.webcrawler.json.CrawlResult;
import com.udacity.webcrawler.parser.PageParser;
import com.udacity.webcrawler.parser.PageParserFactory;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;
import java.util.concurrent.RecursiveTask;
import java.util.regex.Pattern;

public class CrawlTask extends RecursiveAction {
    private final Clock clock;
    private final Instant deadline;
    private final int maxDepth;
    private final List<Pattern> ignoredUrls;
    private final String url;
    private final Set<String> visitedUrls;
    private final PageParserFactory parserFactory;
    private final Map<String,Integer> counts;
    private final CrawlTaskFactory crawlFactory;

    public CrawlTask(String url,
                     Clock clock,
                     PageParserFactory parserFactory,
                     Set<String> visitedUrls,
                     Instant deadline,
                     int maxDepth,
                     List<Pattern> ignoredUrls,
                     Map<String,Integer> counts,
                     CrawlTaskFactory factory) {
        this.clock = clock;
        this.deadline = deadline;
        this.maxDepth = maxDepth;
        this.ignoredUrls = ignoredUrls;
        this.url = url;
        this.visitedUrls = visitedUrls;
        this.parserFactory = parserFactory;
        this.counts = counts;
        crawlFactory = factory;

    }
    @Override
    protected void compute() {

        if (maxDepth == 0 || clock.instant().isAfter(deadline)) {
            return;
        }
        for (Pattern pattern : ignoredUrls) {
            if (pattern.matcher(url).matches()) {
                return;
            }
        }
        if (!visitedUrls.add(url)) {
            return;
        }
        ;
        PageParser.Result result = parserFactory.get(url).parse();
        for (Map.Entry<String, Integer> e : result.getWordCounts().entrySet()) {
            if (counts.containsKey(e.getKey())) {
                counts.put(e.getKey(), e.getValue() + counts.get(e.getKey()));
            } else {
                counts.put(e.getKey(), e.getValue());
            }
        }

        List<CrawlTask> tasks = result.getLinks().stream().map(s -> crawlFactory.createTask(s,maxDepth-1)).toList();
        invokeAll(tasks);
    }
}
