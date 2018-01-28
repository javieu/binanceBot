package com.javieu.crypto.binance.trading;

import static com.binance.api.client.domain.account.NewOrder.marketBuy;
import static com.binance.api.client.domain.account.NewOrder.marketSell;

import org.apache.commons.lang3.StringUtils;

import com.binance.api.client.BinanceApiRestClient;
import com.binance.api.client.BinanceApiWebSocketClient;
import com.binance.api.client.domain.account.NewOrder;
import com.binance.api.client.domain.market.CandlestickInterval;
import com.javieu.crypto.binance.Log;
import com.javieu.crypto.binance.BinanceBot;
import com.javieu.crypto.binance.exceptions.GeneralException;
import com.javieu.crypto.binance.model.ExecutedOrder;

public class TradeTask implements Runnable {

	private String symbol;
	private Double alertPrice;
	private Double btcAmount;
	private Double stopLossPercentage;
	private boolean doTrailingStop;
	
	private ExecutedOrder order = null;
	private boolean error = false;
	private String errorMessage = "";

	private BinanceApiRestClient client;
	private BinanceApiWebSocketClient liveClient;
	
	private Long lastPriceLog = 0L;

	public TradeTask(BinanceApiRestClient client, BinanceApiWebSocketClient liveClient, String symbol, Double alertPrice, Double btcAmount,
			Double stopLossPercentage, boolean doTrailingStop) {
		this.symbol = symbol;
		this.alertPrice = alertPrice;
		this.btcAmount = btcAmount;
		this.stopLossPercentage = stopLossPercentage;
		this.doTrailingStop = doTrailingStop;
		
		this.client = client;
		this.liveClient = liveClient;
	}

	public void run() {
		// 1.- BUY, get order data - price and create ExecutedOrder with stoploss
		try {
			buy();
		} catch (GeneralException e) {
			Log.severe(getClass(), "Unable to create buy operation", e);
			error = true;
			errorMessage = e.getMessage();
			BinanceBot.closeOrder(symbol, null, e.getMessage());
		}
		// 2.- Suscribe to price ticks for the symbol, evaluate current price and update stoploss (if trailing stop)
		monitorPrice();
	}

	private void buy() throws GeneralException {
		String quantity = getAmount(alertPrice);
		Log.info(getClass(), "Trying to buy " + symbol + ", quantity: " + quantity);
		NewOrder newOrder = marketBuy(symbol, quantity);
		try {
			// By now we will not be creating real orders
			client.newOrderTest(newOrder);
			Log.info(getClass(), "Created BUY order: " + newOrder);
		} catch (Exception e) {
			throw new GeneralException(e);
		}

		order = new ExecutedOrder();
		order.setSymbol(symbol);
		order.setQuantity(quantity);
		order.setPrice(alertPrice);
		// current stop loss - used for trailing stop
		order.setCurrentStopLoss((100.0 - stopLossPercentage) * alertPrice / 100.0);
		order.setInitialStopLoss(order.getCurrentStopLoss());
		order.setCreationTime(System.currentTimeMillis());
	}

	private void sell(Double price) {
		try {
			NewOrder newOrder = marketSell(symbol, String.valueOf(order.getQuantity()));
			client.newOrderTest(newOrder);
			Log.info(getClass(), "Created SELL order: " + newOrder);
			order.setClosePrice(price);
			order.setCloseTime(System.currentTimeMillis());
		} catch (Exception e) {
			Log.severe(getClass(), "Unable to sell!", e);
		}
	}

	private String getAmount(Double price) {
		// This method should be refactored... there is a method in Binance API to get symbol info
		Double rawAmount = btcAmount / price;
		if (rawAmount > 1) {
			Integer iAmount = Integer.valueOf(rawAmount.intValue());
			return "" + iAmount;
		} else if (rawAmount < 1 && rawAmount >= 0.1) {
			return StringUtils.replaceAll(String.format("%.2f", rawAmount), ",", ".");
		} else {
			return StringUtils.replaceAll(String.format("%.3f", rawAmount), ",", ".");
		}
	}

	private void monitorPrice() {
		liveClient.onCandlestickEvent(symbol.toLowerCase(),
				CandlestickInterval.ONE_MINUTE, response -> {
					checkPrice(Double.valueOf(response.getClose()));
				});
	}

	private void checkPrice(Double price) {
		Long now = System.currentTimeMillis();
		// This is a bit harcoded, but just trying to avoid too many logs..
		if((now - lastPriceLog) > 60 * 1000L) {
			Log.info(getClass(),
					"Symbol: " + symbol + ". Current price: " + showPrice(price)
							+ ", buy price: " + showPrice(order.getPrice())
							+ ", stoploss: "
							+ showPrice(order.getCurrentStopLoss())
							+ ", current profit: " + order.getCurrentProfit(price) + "%");
			lastPriceLog = now;
		}
		if (order.trailingStopShouldCloseOrder(price) || BinanceBot.shouldCloseOrder(symbol)) {			
			sell(price);
			Log.info(getClass(), "Closed order for symbol: " + symbol 
					+ ". Current price: " + showPrice(price) + ", profit: " + order.getProfit());
			BinanceBot.closeOrder(symbol, order.getProfit(), null);
		}
		if (doTrailingStop && price > order.getPrice()) { 
			// Trailing stop, price is higher, update stoploss
			Double newStopLoss = (100.0 - stopLossPercentage) * price / 100.0;
			if (order.getCurrentStopLoss() < newStopLoss) {
				order.setCurrentStopLoss(newStopLoss);
			}
			Log.info(getClass(),"Symbol: " + symbol + ". Trailing stop; increasing stoploss to "
					+ showPrice(order.getCurrentStopLoss()));
		}
	}

	public synchronized String getErrorMessage() {
		return errorMessage;
	}

	public synchronized boolean isClosed() {
		if (error) {
			return true;
		}
		if (order != null && order.getCloseTime() == null) {
			return false;
		}
		return true;
	}

	public synchronized ExecutedOrder getOrder() {
		return order;
	}

	private String showPrice(Double price) {
		return String.format("%.8f", price);
	}

	public synchronized String getSymbol() {
		return symbol;
	}

}
