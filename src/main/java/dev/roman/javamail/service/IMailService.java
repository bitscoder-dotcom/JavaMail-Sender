package dev.roman.javamail.service;

import dev.roman.javamail.dto.EmailDto;

import java.io.IOException;

public interface IMailService {
    void sendEmail(EmailDto emailDto) throws IOException;
}
