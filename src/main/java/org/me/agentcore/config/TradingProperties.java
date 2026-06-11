package org.me.agentcore.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Validated
@ConfigurationProperties(prefix = "trading")
public class TradingProperties {

    private boolean enabled = false;

    @NotNull
    private TradingMode mode = TradingMode.DRY_RUN;

    @NotEmpty
    private List<@NotBlank String> tickers = new ArrayList<>();

    @NotNull
    private Duration cycleInterval = Duration.ofSeconds(10);

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public TradingMode getMode() {
        return mode;
    }

    public void setMode(TradingMode mode) {
        this.mode = mode;
    }

    public List<String> getTickers() {
        return tickers;
    }

    public void setTickers(List<String> tickers) {
        this.tickers = tickers;
    }

    public Duration getCycleInterval() {
        return cycleInterval;
    }

    public void setCycleInterval(Duration cycleInterval) {
        this.cycleInterval = cycleInterval;
    }

    public enum TradingMode {
        DRY_RUN,
        TEST_CONTOUR
    }
}
