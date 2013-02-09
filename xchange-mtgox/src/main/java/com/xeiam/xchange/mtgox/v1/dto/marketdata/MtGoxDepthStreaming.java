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
package com.xeiam.xchange.mtgox.v1.dto.marketdata;

import java.math.BigDecimal;
import java.util.Date;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;

/**
 * Data object representing a depth from Mt Gox streaming channel
 */

@JsonIgnoreProperties(ignoreUnknown = true)
public final class MtGoxDepthStreaming {

  private final BigDecimal price;
  private final BigDecimal volume;
  private final long priceInt;
  private final long volumeInt;
  private final long totalVolumeInt;
  private final String typeStr;
  private final String item;
  private final String currency;
  private final long now;

  /**
   * Constructor
   * 
   * @param price
   * @param volume
   * @param priceInt
   * @param volumeInt
   * @param totalVolumeInt
   * @param typeStr
   * @param item
   * @param currency
   * @param now
   */
  public MtGoxDepthStreaming(@JsonProperty("price") BigDecimal price,
      @JsonProperty("volume") BigDecimal volume,
      @JsonProperty("price_int") long priceInt,
      @JsonProperty("volume_int") long volumeInt,
      @JsonProperty("total_volume_int") long totalVolumeInt,
      @JsonProperty("type_str") String typeStr,
      @JsonProperty("item") String item,
      @JsonProperty("currency") String currency, @JsonProperty("now") long now) {

    this.price = price;
    this.volume = volume;
    this.priceInt = priceInt;
    this.volumeInt = volumeInt;
    this.totalVolumeInt = totalVolumeInt;
    this.typeStr = typeStr;
    this.item = item;
    this.currency = currency;
    this.now = now;
  }

  public BigDecimal getPrice() {

    return price;
  }

  public BigDecimal getVolume() {

    return volume;
  }

  public long getPriceInt() {

    return priceInt;
  }

  public long getVolumeInt() {

    return volumeInt;
  }
  
  public long getTotalVolumeInt() {

    return totalVolumeInt;
  }
  
  public String getTypeStr() {

    return typeStr;
  }

  public String getItem() {

    return item;
  }
  
  public String getCurrency() {

    return currency;
  }
  
  public long getNow() {

    return now;
  }

  @Override
  public String toString() {

    return "MtGoxDepthStreaming [price=" + price + ", volume=" + volume + ", priceInt=" + priceInt + ", volumeInt=" + volumeInt + ", totalVolumeInt=" + totalVolumeInt + ", typeStr=" + typeStr + ", item=" + item + ", currency=" + currency +", now=" + now + "]";
  }

}
