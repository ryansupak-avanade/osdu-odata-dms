/* Licensed Materials - Property of IBM              */
/* (c) Copyright IBM Corp. 2020. All Rights Reserved.*/

package org.opengroup.osdu.odatadms.provider.ibm.model;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Date;

/**
 * Responsible for calculating expiration dates
 */
@Component
public class ExpirationDateHelper {

  @Deprecated
  public Date getExpirationDate(int s3SignedUrlExpirationTimeInDays){
    Date expiration = new Date();
    long expTimeMillis = expiration.getTime();
    expTimeMillis += 1000 * 60 * 60 * 24 * s3SignedUrlExpirationTimeInDays;
    expiration.setTime(expTimeMillis);
    return expiration;
  }

  /**
   * Adds the timespan to the Local date and returns a Date object of that time
   * @param date - the start date
   * @param timeSpan - a length of time to calculate the future date
   * @return
   */
  public Date getExpiration(Instant date, Duration timeSpan) {
    Instant expiration = date.plus(timeSpan);
    return Date.from(expiration
            .atZone(ZoneId.systemDefault())
            .toInstant());
  }
}