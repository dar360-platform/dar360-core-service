package com.lending.dar360UserService.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotifyBodyDto {

    private String templateCode;
    private Map<String, String> variables;
    private List<String> sendToEmails;
}
