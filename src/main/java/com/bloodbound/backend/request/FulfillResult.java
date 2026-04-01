package com.bloodbound.backend.request;

public class FulfillResult {

    public enum Status { SUCCESS, NOT_FOUND, FORBIDDEN, ALREADY_FULFILLED }

    private final Status status;
    private final RequestResponse dto;

    private FulfillResult(Status status, RequestResponse dto) {
        this.status = status;
        this.dto    = dto;
    }

    public static FulfillResult success(RequestResponse dto) {
        return new FulfillResult(Status.SUCCESS, dto);
    }
    public static FulfillResult notFound() {
        return new FulfillResult(Status.NOT_FOUND, null);
    }
    public static FulfillResult forbidden() {
        return new FulfillResult(Status.FORBIDDEN, null);
    }
    public static FulfillResult alreadyFulfilled() {
        return new FulfillResult(Status.ALREADY_FULFILLED, null);
    }

    public Status getStatus()       { return status; }
    public RequestResponse getDto() { return dto; }
}