Binance Bot
=============

This is a pretty basic project. It consists of a bot that creates test orders in Binance based on TA4J indicators / strategies.

## Quickstart

1. Create a config.properties and include your setup (you wil need a Binance API Key / secret).
2. Compile and package:
```
mvn clean compile assembly:single
```
3. Run the generated JAR:
```
java -DLOGS_PATH=<PATH TO YOUR LOGS FOLDER> -DCONFIG_FILE_PATH=<PATH TO YOUR config.properties FILE> -jar binance-bot.jar
```

## Strategies customization

The TA strategy is included in BinanceTa4jUtils class. I have just included one first strategy based on moving average (crosses of MACD, etc.). New TA4J strategies can be included there - you will need to modify public static Strategy buildStrategy(TimeSeries series, String strategyCode) method.

