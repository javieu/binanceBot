package com.javieu.crypto.binance.util;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.LinkedList;
import java.util.List;

import org.ta4j.core.BaseStrategy;
import org.ta4j.core.BaseTick;
import org.ta4j.core.BaseTimeSeries;
import org.ta4j.core.Decimal;
import org.ta4j.core.Rule;
import org.ta4j.core.Strategy;
import org.ta4j.core.Tick;
import org.ta4j.core.TimeSeries;
import org.ta4j.core.indicators.EMAIndicator;
import org.ta4j.core.indicators.MACDIndicator;
import org.ta4j.core.indicators.SMAIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.trading.rules.CrossedDownIndicatorRule;
import org.ta4j.core.trading.rules.CrossedUpIndicatorRule;
import org.ta4j.core.trading.rules.OverIndicatorRule;

import com.binance.api.client.domain.market.Candlestick;

public class BinanceTa4jUtils {

	public static String MACD_STRATEGY = "MACD";

	public static TimeSeries convertToTimeSeries(
			List<Candlestick> candlesticks, String symbol, String period) {
		List<Tick> ticks = new LinkedList<Tick>();
		for (Candlestick candlestick : candlesticks) {
			ticks.add(convertToTa4jTick(candlestick));
		}
		return new BaseTimeSeries(symbol + "_" + period, ticks);
	}

	public static Tick convertToTa4jTick(Candlestick candlestick) {
		ZonedDateTime closeTime = getZonedDateTime(candlestick.getCloseTime());
		Duration candleDuration = Duration.ofMillis(candlestick.getCloseTime()
				- candlestick.getOpenTime());
		Decimal openPrice = Decimal.valueOf(candlestick.getOpen());
		Decimal closePrice = Decimal.valueOf(candlestick.getClose());
		Decimal highPrice = Decimal.valueOf(candlestick.getHigh());
		Decimal lowPrice = Decimal.valueOf(candlestick.getLow());
		Decimal volume = Decimal.valueOf(candlestick.getVolume());

		return new BaseTick(candleDuration, closeTime, openPrice, highPrice,
				lowPrice, closePrice, volume);
	}

	public static ZonedDateTime getZonedDateTime(Long timestamp) {
		return ZonedDateTime.ofInstant(Instant.ofEpochMilli(timestamp),
				ZoneId.systemDefault());
	}

	public static boolean isSameTick(Candlestick candlestick, Tick tick) {
		if (tick.getEndTime().equals(
				getZonedDateTime(candlestick.getCloseTime()))) {
			return true;
		}
		return false;
	}

	public static Strategy buildStrategy(TimeSeries series, String strategyCode) {
		if (MACD_STRATEGY.equals(strategyCode)) {
			return buildMacdStrategy(series);
		}
		return null;
	}

	private static Strategy buildMacdStrategy(TimeSeries series) {
		if (series == null) {
			throw new IllegalArgumentException("Series cannot be null");
		}

		ClosePriceIndicator closePrice = new ClosePriceIndicator(series);

		MACDIndicator macd = new MACDIndicator(closePrice, 12, 26);
		EMAIndicator emaMacd = new EMAIndicator(macd, 9);
		SMAIndicator shortTermSMA = new SMAIndicator(closePrice, 50);
		SMAIndicator longTermSMA = new SMAIndicator(closePrice, 100);

		Rule entryRule = new CrossedUpIndicatorRule(macd, emaMacd) // First signal
				.and(new OverIndicatorRule(shortTermSMA, longTermSMA)); // Second signal

		Rule exitRule = new CrossedDownIndicatorRule(macd, emaMacd);

		return new BaseStrategy(entryRule, exitRule);
	}
}
