package com.udacity.webcrawler;

import com.udacity.webcrawler.parser.PageParserFactory;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * A factory for creating CrawlTasks to traverse a given url which all have the
 * same global configuration options loaded from the crawler config
 *
 * @author Amy Lanclos
 */
public class CrawlTaskFactory {

    private final Clock clock;
    private final Instant deadline;
    private final List<Pattern> ignoredUrls;
    private final Set<String> visitedUrls;
    private final PageParserFactory parserFactory;
    private final Map<String,Integer> counts;

    public CrawlTaskFactory(Clock clock,
                            PageParserFactory parserFactory,
                            Set<String> visitedUrls,
                            Instant deadline,
                            List<Pattern> ignoredUrls,
                            Map<String,Integer> counts) {
        this.clock = clock;
        this.deadline = deadline;
        this.ignoredUrls = ignoredUrls;
        this.visitedUrls = visitedUrls;
        this.parserFactory = parserFactory;
        this.counts = counts;
    }

    public CrawlTask createTask(String url, int depth) {
        return new CrawlTask(url,clock,parserFactory,visitedUrls,deadline,depth,ignoredUrls,counts,this);
    }
}
