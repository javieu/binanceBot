package com.javieu.crypto.binance.model;

public class ExecutedOrder {
	
	private Long creationTime;
	private String symbol;
	private Double price;
	private String quantity;
	private Long closeTime;
	private Double closePrice;
	private Double initialStopLoss;
	private Double currentStopLoss;
	
	public Long getCreationTime() {
		return creationTime;
	}
	public void setCreationTime(Long creationTime) {
		this.creationTime = creationTime;
	}
	public String getSymbol() {
		return symbol;
	}
	public void setSymbol(String symbol) {
		this.symbol = symbol;
	}
	public Double getPrice() {
		return price;
	}
	public void setPrice(Double price) {
		this.price = price;
	}
	public String getQuantity() {
		return quantity;
	}
	public void setQuantity(String quantity) {
		this.quantity = quantity;
	}
	public Long getCloseTime() {
		return closeTime;
	}
	public void setCloseTime(Long closeTime) {
		this.closeTime = closeTime;
	}
	public Double getClosePrice() {
		return closePrice;
	}
	public void setClosePrice(Double closePrice) {
		this.closePrice = closePrice;
	}
	public Double getCurrentStopLoss() {
		return currentStopLoss;
	}
	public void setCurrentStopLoss(Double currentStopLoss) {
		this.currentStopLoss = currentStopLoss;
	}
	public Double getProfit() {
		return Double.valueOf(quantity) * (closePrice - price);
	}
	public Double getInitialStopLoss() {
		return initialStopLoss;
	}
	public void setInitialStopLoss(Double initialStopLoss) {
		this.initialStopLoss = initialStopLoss;
	}
	public Boolean trailingStopShouldCloseOrder(Double currentPrice) {
		if(currentPrice < currentStopLoss) {
			return true;
		}
		return false;
	}
	public String getCurrentProfit(Double currentPrice) {
		Double profitPercentage = ((currentPrice - price)/price)*100;
		return String.format("%.2f", profitPercentage);
	}
	
	@Override
	public String toString() {
		return "ExecutedOrder [creationTime=" + creationTime + ", symbol="
				+ symbol + ", price=" + price + ", quantity=" + quantity
				+ ", closeTime=" + closeTime + ", closePrice=" + closePrice
				+ ", initialStopLoss=" + initialStopLoss + ", currentStopLoss="
				+ currentStopLoss + "]";
	}

}
