/**
 * Copyright (C) 2012 - 2013 Xeiam LLC http://xeiam.com
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
 * of the Software, and to permit persons to whom the Software is furnished to do
 * so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.xeiam.xchange.mtgox.v1.service.marketdata.streaming.socketio;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;

import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xeiam.xchange.ExchangeException;
import com.xeiam.xchange.dto.marketdata.Depth;
import com.xeiam.xchange.dto.marketdata.Ticker;
import com.xeiam.xchange.dto.marketdata.Trade;
import com.xeiam.xchange.mtgox.v1.MtGoxAdapters;
import com.xeiam.xchange.mtgox.v1.dto.marketdata.MtGoxDepthStreaming;
import com.xeiam.xchange.mtgox.v1.dto.marketdata.MtGoxTicker;
import com.xeiam.xchange.mtgox.v1.dto.marketdata.MtGoxTrade;
import com.xeiam.xchange.rest.JSONUtils;
import com.xeiam.xchange.service.DefaultExchangeEvent;
import com.xeiam.xchange.service.ExchangeEvent;
import com.xeiam.xchange.service.ExchangeEventType;
import com.xeiam.xchange.service.RunnableExchangeEventListener;

/**
 * <p>
 * Exchange event listener to provide the following to MtGox:
 * </p>
 * <ul>
 * <li>Provision of dedicated Ticker queue</li>
 * <li>Provision of dedicated non-Ticker event queue</li>
 * </ul>
 */
public class MtGoxRunnableExchangeEventListener extends RunnableExchangeEventListener {

  private static final Logger log = LoggerFactory.getLogger(MtGoxRunnableExchangeEventListener.class);

  private ObjectMapper objectMapper = new ObjectMapper();

  private final BlockingQueue<Ticker> tickerQueue;
  private final BlockingQueue<ExchangeEvent> eventQueue;
  private final HashMap<String, Boolean> subscribedCurrencies;

  /**
   * Constructor
   * 
   * @param tickerQueue The consumer Ticker queue
   * @param eventQueue The consumer exchange event queue
   */
  public MtGoxRunnableExchangeEventListener(BlockingQueue<Ticker> tickerQueue, BlockingQueue<ExchangeEvent> eventQueue) {
    this.tickerQueue = tickerQueue;
    this.eventQueue = eventQueue;
    
    subscribedCurrencies = new HashMap<String, Boolean> ();
  }

  public void subscribeCurrency (String currency) {
    subscribedCurrencies.put(currency, new Boolean(true));
  }
  
  @Override
  public void handleEvent(ExchangeEvent exchangeEvent) {

    switch (exchangeEvent.getEventType()) {
    case CONNECT:
      log.debug("MtGox connected");
      addToEventQueue(exchangeEvent);
      break;
    case DISCONNECT:
      log.debug("MtGox disconnected");
      addToEventQueue(exchangeEvent);
      break;
    case MESSAGE:
      log.debug("Generic message. Length=" + exchangeEvent.getData().length());
      addToEventQueue(exchangeEvent);
      break;
    case JSON_MESSAGE:
      log.debug("JSON message. Length=" + exchangeEvent.getData().length());

      // Get raw JSON
      Map<String, Object> rawJSON = JSONUtils.getJsonGenericMap(exchangeEvent.getData(), objectMapper);

      // Determine what has been sent
      if (rawJSON.containsKey("ticker")) {

        // Get MtGoxTicker from JSON String
        MtGoxTicker mtGoxTicker = JSONUtils.getJsonObject(JSONUtils.getJSONString(rawJSON.get("ticker"), objectMapper), MtGoxTicker.class, objectMapper);

        // Adapt to XChange DTOs
        Ticker ticker = MtGoxAdapters.adaptTicker(mtGoxTicker);

        // TODO Remove this once ticker queue is removed
        addToTickerQueue(ticker);

        // Create a ticker event
        ExchangeEvent tickerEvent = new DefaultExchangeEvent(ExchangeEventType.TICKER, exchangeEvent.getData(), ticker);
        addToEventQueue(tickerEvent);
      } else if (rawJSON.containsKey("trade")) {

        // Get MtGoxTicker from JSON String
        MtGoxTrade mtGoxTrade = JSONUtils.getJsonObject(JSONUtils.getJSONString(rawJSON.get("trade"), objectMapper), MtGoxTrade.class, objectMapper);

        Trade trade = MtGoxAdapters.adaptTrade(mtGoxTrade);

        // Trade Event from MtGox include all currencies for some reasons
        if (subscribedCurrencies.containsKey(trade.getTransactionCurrency())) {
          ExchangeEvent tradeEvent = new DefaultExchangeEvent(ExchangeEventType.TRADE, exchangeEvent.getData(), trade);
          addToEventQueue(tradeEvent);
        }
      } else if (rawJSON.containsKey("depth")) {

        // Get MtGoxTicker from JSON String
        MtGoxDepthStreaming mtGoxDepth = JSONUtils.getJsonObject(JSONUtils.getJSONString(rawJSON.get("depth"), objectMapper), MtGoxDepthStreaming.class, objectMapper);

        Depth depth = MtGoxAdapters.adaptMtGoxDepthStreaming(mtGoxDepth);

        // Create a ticker event
        ExchangeEvent tradeEvent = new DefaultExchangeEvent(ExchangeEventType.DEPTH, exchangeEvent.getData(), depth);
        addToEventQueue(tradeEvent);
      } else {
        log.debug("MtGox operational message");
        addToEventQueue(exchangeEvent);
      }
      break;
    case ERROR:
      log.error("Error message. Length=" + exchangeEvent.getData().length());
      addToEventQueue(exchangeEvent);
      break;
    default:
      throw new IllegalStateException("Unknown ExchangeEventType " + exchangeEvent.getEventType().name());
    }

  }

  private void addToTickerQueue(Ticker ticker) {

    try {
      tickerQueue.put(ticker);
    } catch (InterruptedException e) {
      throw new ExchangeException("InterruptedException!", e);
    }
  }

  private void addToEventQueue(ExchangeEvent event) {

    try {
      eventQueue.put(event);
    } catch (InterruptedException e) {
      throw new ExchangeException("InterruptedException!", e);
    }
  }

}
