package rms.library.googlepay.model;

import java.io.Serializable;
public class Transaction implements Serializable {

    String amount;
    String txID;
    String domain;
    String vkey;
    String url;
    String type;

    public Transaction(String amount, String txID, String domain, String vkey, String url, String type) {
        this.amount = amount;
        this.txID = txID;
        this.domain = domain;
        this.vkey = vkey;
        this.url = url;
        this.type = type;
    }

    public Transaction() {
    }


    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getTxID() {
        return txID;
    }

    public void setTxID(String txID) {
        this.txID = txID;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getVkey() {
        return vkey;
    }

    public void setVkey(String vkey) {
        this.vkey = vkey;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

}