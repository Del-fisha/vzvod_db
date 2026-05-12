package com.company.vzvod.bot;

import com.company.vzvod.entity.StatusInService;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@Component
public class BotActiveUserChecker {

    private final JdbcTemplate jdbcTemplate;

    public BotActiveUserChecker(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void requireActive(UUID userId) {
        try {
            Integer statusId = jdbcTemplate.queryForObject(
                    "select status from service_info where user_id = ?",
                    Integer.class,
                    userId
            );
            if (!StatusInService.ACTIVE.getId().equals(statusId)) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "user inactive");
            }
        } catch (EmptyResultDataAccessException e) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "user inactive");
        }
    }
}
