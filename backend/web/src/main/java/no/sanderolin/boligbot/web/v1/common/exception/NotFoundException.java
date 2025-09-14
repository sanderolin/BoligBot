package no.sanderolin.boligbot.web.v1.common.exception;

public class NotFoundException extends RuntimeException{
    public NotFoundException(String message) { super(message); }
}
