package com.neo.util.framework.rest.web.htmx;

import com.neo.util.framework.api.janitor.JanitorJob;
import jakarta.enterprise.context.ApplicationScoped;

import java.time.LocalDate;

@ApplicationScoped
public class JanitorJobTest implements JanitorJob {
    @Override public void execute(LocalDate now) {

    }
}
