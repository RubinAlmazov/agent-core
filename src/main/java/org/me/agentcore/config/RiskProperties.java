package org.me.agentcore.config;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;
import java.time.Duration;

@Validated
@ConfigurationProperties(prefix = "risk")
public class RiskProperties {

    @NotNull
    @DecimalMin(value = "0.0", inclusive = false)
    @DecimalMax("1.0")
    private BigDecimal maxPositionShare = new BigDecimal("0.25");

    @NotNull
    @DecimalMin(value = "0.0", inclusive = false)
    @DecimalMax("1.0")
    private BigDecimal maxOrderShare = new BigDecimal("0.10");

    @NotNull
    @DecimalMin(value = "0.0", inclusive = false)
    @DecimalMax("1.0")
    private BigDecimal dailyLossLimit = new BigDecimal("0.03");

    @NotNull
    @DecimalMin("0.0")
    @DecimalMax("1.0")
    private BigDecimal minConfidence = new BigDecimal("0.65");

    @NotNull
    private Duration cooldown = Duration.ofMinutes(5);

    @Positive
    private int maxTradesPerDay = 20;

    public BigDecimal getMaxPositionShare() {
        return maxPositionShare;
    }

    public void setMaxPositionShare(BigDecimal maxPositionShare) {
        this.maxPositionShare = maxPositionShare;
    }

    public BigDecimal getMaxOrderShare() {
        return maxOrderShare;
    }

    public void setMaxOrderShare(BigDecimal maxOrderShare) {
        this.maxOrderShare = maxOrderShare;
    }

    public BigDecimal getDailyLossLimit() {
        return dailyLossLimit;
    }

    public void setDailyLossLimit(BigDecimal dailyLossLimit) {
        this.dailyLossLimit = dailyLossLimit;
    }

    public BigDecimal getMinConfidence() {
        return minConfidence;
    }

    public void setMinConfidence(BigDecimal minConfidence) {
        this.minConfidence = minConfidence;
    }

    public Duration getCooldown() {
        return cooldown;
    }

    public void setCooldown(Duration cooldown) {
        this.cooldown = cooldown;
    }

    public int getMaxTradesPerDay() {
        return maxTradesPerDay;
    }

    public void setMaxTradesPerDay(int maxTradesPerDay) {
        this.maxTradesPerDay = maxTradesPerDay;
    }
}
