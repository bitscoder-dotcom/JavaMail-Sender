package dev.roman.javamail.repository;

public interface EmailSenderRepository {
    public void sendEmail(String from, String to, String subject, String message);
}
