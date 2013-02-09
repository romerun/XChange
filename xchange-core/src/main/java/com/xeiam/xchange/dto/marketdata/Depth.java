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
package com.xeiam.xchange.dto.marketdata;

import java.math.BigDecimal;
import java.util.Date;

import org.joda.money.BigMoney;

import com.xeiam.xchange.dto.Order.OrderType;

/**
 * Data object representing a Depth
 */
public final class Depth implements Comparable<Depth> {

  /**
   * Bid or a ask?
   */
  private final OrderType type;
  /**
   * The price
   */
  private final BigMoney price;
  /**
   * Change of volume (could be negative)
   */  
  private final BigDecimal volume;
  /**
   * An identifier that uniquely identifies the tradable
   */
  private final String tradableIdentifier;
  /**
   * The currency used to settle the market order transaction
   */
  private final String transactionCurrency;
  /**
   * order id
   */
  private final long id;
  private final Date timestamp;

  /**
   * Constructor
   * 
   * @param type
   * @param price
   * @param volume
   * @param tradableIdentifier
   * @param transactionCurrency
   * @param id
   * @param timestamp
   */
  public Depth(OrderType type, BigMoney price, BigDecimal volume, String tradableIdentifier, String transactionCurrency, long id, Date timestamp) {

    this.type = type;
    this.price = price;
    this.volume = volume;
    this.tradableIdentifier = tradableIdentifier;
    this.transactionCurrency = transactionCurrency;
    this.id = id;
    this.timestamp = timestamp;
  }

  public OrderType getType() {

    return type;
  }

  public BigMoney getPrice() {

    return price;
  }
  
  public BigDecimal getVolume() {

    return volume;
  }

  public String getTradableIdentifier() {

    return tradableIdentifier;
  }

  public String getTransactionCurrency() {

    return transactionCurrency;
  }

  public String getId() {

    return (new Long(id)).toString();
  }
  
  public Date getTimestamp() {

    return timestamp;
  }

  @Override
  public String toString() {

    return "Depth [type=" + type + ", price=" + price + ", volume=" + volume + ", tradableIdentifier=" + tradableIdentifier + ", transactionCurrency=" + transactionCurrency  
        + ", id=" + id + ", timestamp=" + timestamp + "]";
  }

  @Override
  public int compareTo(Depth depth) {

    return (this.id < depth.id ? -1 : this.id > depth.id ? 1 : 0);
  }

}
