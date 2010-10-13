package com.indexdata.utils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Utility class to test if one IP range set can be considered the superset
 * of another.
 * <p/>
 * A given set -- IP range set A -- is considered to 'enclose' another
 * set -- IP range set B -- if all possible IP numbers in set B is a subset of 
 * all possible IP numbers in set A (note).
 * <p/>
 * A IP range set is a space separated list of one or more IP ranges and/or 
 * single IP numbers.
 * <br/>
 * An IP range is a hyphen separated list of two IP numbers. 
 * 
 * 
 * Note: Is not guaranteed to handle IP ranges that are split in two with 
 * the end-point of one being back-to-back with the start-point of the next. 
 * 
 * @author Niels Erik Nielsen
 * 
 */
public class IpRangeSet {

  private List<IpRange> ipRanges = new ArrayList<IpRange>();

  public IpRangeSet(String ipRanges) {
    String[] ranges = ipRanges.split(" +");
    for (String aRange : ranges) {
      String[] endPoints = aRange.split("\\-");
      if (endPoints.length == 2) {
        this.ipRanges.add(new IpRange(endPoints[0], endPoints[1]));
      } else {
        this.ipRanges.add(new IpRange(endPoints[0], endPoints[0]));
      }
    }
  }

  public Iterator<IpRange> iterator() {
    return ipRanges.iterator();
  }

  /**
   * Determines if a given IP range set is the subset of this
   * IP range set.
   * @param anotherIpRangeSet
   * @return
   */
  public boolean encloses(IpRangeSet anotherIpRangeSet) {
    boolean doesEnclose = true;
    Iterator<IpRange> anotherIter = anotherIpRangeSet.iterator();
    while (anotherIter.hasNext()) {
      IpRange othersRange = anotherIter.next();
      boolean othersRangeEnclosed = false;
      Iterator<IpRange> thisIter = this.iterator();
      while (thisIter.hasNext()) {
        IpRange thisRange = thisIter.next();
        if (thisRange.encloses(othersRange)) {
          othersRangeEnclosed = true;
          break;
        }
      }
      if (!othersRangeEnclosed) {
        doesEnclose = false;
        break;
      }
    }
    return doesEnclose;
  }

  public String toString() {
    StringBuffer rangeSet = new StringBuffer("");
    Iterator<IpRange> iter = iterator();
    while (iter.hasNext()) {
      rangeSet.append(iter.next().toString() + " ");
    }
    return rangeSet.toString();
  }

  public class IpRange {

    private IpNumber start;
    private IpNumber end;

    public IpRange(String start, String end) {
      IpNumber ip1 = new IpNumber(start);
      IpNumber ip2 = new IpNumber(end);
      if (ip1.toLong() < ip2.toLong()) {
        this.start = ip1;
        this.end = ip2;
      } else {
        this.start = ip2;
        this.end = ip1;
      }
    }

    public IpNumber getStartPoint() {
      return start;
    }

    public IpNumber getEndPoint() {
      return end;
    }

    public boolean encloses(IpRange anotherIpRange) {
      return (encloses(anotherIpRange.getStartPoint()) && encloses(anotherIpRange.getEndPoint()));
    }

    public boolean encloses(IpNumber ipNumber) {
      return (start.toLong() <= ipNumber.toLong() && ipNumber.toLong() <= end.toLong());
    }

    public String toString() {
      return start.toString() + (start.equals(end) ? "" : "-" + end.toString());
    }

  }

  private class IpNumber {
    private String ipAddress;

    public IpNumber(String ipNumber) {
      ipAddress = ipNumber;
    }

    public long toLong() {
      String[] octets = ipAddress.split("\\.");
      long longIp = 0;
      for (int i = 0; i < 4; i++) {
        longIp = 256 * longIp + Integer.parseInt(octets[i]);
      }
      return longIp;
    }

    public String toString() {
      return ipAddress;
    }

    public boolean equals(Object o) {
      if (o instanceof IpNumber) {
        if (((IpNumber) o).toString().equals(this.toString())) {
          return true;
        }
      }
      return false;
    }

  }

}
