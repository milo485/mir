package mir.util;

import java.util.*;

public class SQLQueryBuilder {
  private StringBuffer whereClause;
  private StringBuffer orderByClause;

  public SQLQueryBuilder(){
    whereClause = new StringBuffer();
    orderByClause = new StringBuffer();
  }

  public void appendDescendingOrder(String anOrder) {
    if (orderByClause.length()==0) {
      orderByClause.append(anOrder).append(" desc");
    }
    else {
      orderByClause.append(",").append(anOrder).append(" desc");
    }
  }

  public void appendAscendingOrder(String anOrder) {
    if (orderByClause.length()==0) {
      orderByClause.append(anOrder).append(" asc");
    }
    else {
      orderByClause=orderByClause.append(",").append(anOrder).append(" asc");
    }
  }

  public void appendAndCondition(String aQualifier) {
    if (whereClause.length()==0) {
      whereClause.append("(").append(aQualifier).append(")");
    }
    else {
      whereClause.append(" and (").append(aQualifier).append(")");
    }
  }

  public void appendOrCondition(String aQualifier) {
    if (whereClause.length()==0) {
      whereClause.append(aQualifier);
    }
    else {
      whereClause.append("(").append(whereClause).append(") or (").append(aQualifier).append(")");
    }
  }

  public String getWhereClause() {
    return whereClause.toString();
  }

  public String getOrderByClause() {
    return orderByClause.toString();
  }
}
