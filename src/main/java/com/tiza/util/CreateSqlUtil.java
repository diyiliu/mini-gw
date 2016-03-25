package com.tiza.util;

import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Description: CreateSqlUtil
 * Author: DIYILIU
 * Update: 2016-03-21 16:02
 */
public class CreateSqlUtil {

    //表名
    private String table;
    //表用户
    private String user;

    private Map values;

    private Map whereCase;

    private StringBuffer sql;

    private int sqlType; // 1 insert 2 update

    public int getSqlType() {
        return sqlType;
    }

    public void setSqlType(int sqlType) {
        this.sqlType = sqlType;
    }

    private CreateSqlUtil setSql() {
        this.sql = new StringBuffer();
        return this;
    }

    public Map getValues() {
        return values;
    }

    public void setValues(Map values) {
        this.values = values;
    }

    public Map getWhereCase() {
        return whereCase;
    }

    public void setWhereCase(Map whereCase) {
        this.whereCase = whereCase;
    }

    public String getSql() {
        return this.sql.toString();
    }

    public String getTable() {
        return table;
    }

    public void setTable(String table) {
        this.table = table;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String addParams() {
        return null;
    }

    /**
     *
     */
    public void createSql() {
        this.setSql().addSqlHead().addFieldAndValues();
        switch (this.sqlType) {
            case 1:
                this.setSql().addSqlHead().addFieldAndValues();
                break;
            case 2:
                this.setSql().addSqlHead().addFieldAndValues().addWhereCase();
                break;
            default:
                break;
        }
    }


    private CreateSqlUtil addSqlHead() {
        switch (this.sqlType) {
            case 1:
                this.addInsertHead();
                break;
            case 2:
                this.addUpdateHead();
                break;
            default:
                break;
        }

        return this;
    }

    private void addInsertHead() {
        this.sql.append("insert into ");
        if (!CommonUtil.isEmpty(this.user)) {
            this.sql.append(this.user).append(".");
        }
        this.sql.append(this.table);
    }

    private void addUpdateHead() {
        this.sql.append("update ");
        if (!CommonUtil.isEmpty(this.user)) {
            this.sql.append(this.user).append(".");
        }
        this.sql.append(this.table).append(" set ");
    }


    private CreateSqlUtil insertFieldAndValues() {
        this.sql.append(" (");
        int size = this.values.size();
        int i = 1;
        Set<Map.Entry<String, Object>> values = this.values.entrySet();
        StringBuffer tempKeys = new StringBuffer();
        StringBuffer tempValues = new StringBuffer();
        Iterator iterator = values.iterator();
        while (iterator.hasNext()) {
            Map.Entry res = (Map.Entry<String, Object>) iterator.next();
            if (i == size) {
                tempKeys.append(res.getKey());

                this.formateValue(tempValues, res.getValue());
            } else {
                tempKeys.append(res.getKey()).append(" , ");

                this.formateValue(tempValues, res.getValue());
                tempValues.append(" , ");
            }
            i++;
        }
        this.sql.append(tempKeys.toString()).append(" ) values ").append("(").append(tempValues.toString()).append(" ) ");
        return this;
    }

    private CreateSqlUtil updateFieldAndValues() {

        int size = this.values.size();
        int i = 1;
        Set<Map.Entry<String, Object>> values = this.values.entrySet();
        StringBuffer tempSet = new StringBuffer();
        Iterator iterator = values.iterator();
        while (iterator.hasNext()) {
            Map.Entry res = (Map.Entry<String, Object>) iterator.next();
            if (i == size) {
                tempSet.append(res.getKey()).append("=");
                this.formateValue(tempSet, res.getValue());
            } else {
                tempSet.append(res.getKey()).append("=");
                this.formateValue(tempSet, res.getValue());
                tempSet.append(" , ");
            }
            i++;
        }
        this.sql.append(tempSet.toString());
        return this;

    }

    private CreateSqlUtil addFieldAndValues() {
        switch (this.sqlType) {
            case 1:
                this.insertFieldAndValues();
                break;
            case 2:
                this.updateFieldAndValues();
                break;
            default:
                break;
        }
        return this;
    }

    private CreateSqlUtil addWhereCase() {
        if (this.whereCase == null) {
            return this;
        }
        this.sql.append(" where ");
        int size = this.whereCase.size();
        int i = 1;
        Set<Map.Entry<String, Object>> values = this.whereCase.entrySet();
        StringBuffer tempWhere = new StringBuffer();
        Iterator iterator = values.iterator();
        while (iterator.hasNext()) {
            Map.Entry res = (Map.Entry<String, Object>) iterator.next();
            if (i == size) {
                tempWhere.append(res.getKey()).append("=");
                this.formateValue(tempWhere, res.getValue());
            } else {
                tempWhere.append(res.getKey()).append("=");
                this.formateValue(tempWhere, res.getValue());
                tempWhere.append(" and ");
            }
            i++;
        }
        this.sql.append(tempWhere.toString());

        return this;
    }

    private void formateValue(StringBuffer sb, Object value) {
        if (value instanceof String) {
            sb.append("'").append(value).append("'");
        } else if (value instanceof Number) {
            sb.append(value);
        } else if (value instanceof Date) {
            sb.append("date_format('").append(DateUtil.dateToString((Date) value)).append("' , '%Y-%m-%d %H:%i:%s')");
        } else if (value == null) {
            sb.append(value);
        }
    }
}
