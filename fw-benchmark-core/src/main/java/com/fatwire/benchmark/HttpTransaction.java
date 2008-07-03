package com.fatwire.benchmark;

public class HttpTransaction {
    private final HttpRequest request;

    private final HttpResponse response;

    private final long downloadTime;

    private final long startTime;

    private final String agentId;

    /**
     * @param request
     * @param response
     * @param downloadTime
     * @param startTime
     * @param agentId
     */
    public HttpTransaction(HttpRequest request, HttpResponse response,
            final long startTime, long downloadTime, String agentId) {
        super();
        this.startTime = startTime;
        this.request = request;
        this.response = response;
        this.downloadTime = downloadTime;
        this.agentId = agentId;
    }

    HttpRequest getRequest() {
        return request;
    }

    HttpResponse getResponse() {
        return response;
    }

    long getDownloadTime() {
        return downloadTime;
    }

    String getAgentId() {
        return agentId;
    }

    /**
     * @return the startTime
     */
    public long getStartTime() {
        return startTime;
    }

}
